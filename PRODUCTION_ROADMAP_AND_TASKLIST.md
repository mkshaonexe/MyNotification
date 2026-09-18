# My Notification — Production Roadmap & Implementation Task List
### Full Specification, Architecture & Phased TODO List for Production-Grade Android App
**Reference Documents:**
- [`My_Notification_Design_System.md`](file:///Users/mkshaon/playground/My_Notification_Design_System.md)
- [`My_Notification_Product_Specification.md`](file:///Users/mkshaon/playground/My_Notification_Product_Specification.md)
- Visual Concept Reference: 10 screens (Home, History, Blocking Rules, Analytics, Settings in Dark & Light Modes)

---

## 1. Codebase Audit & Gap Analysis

### 1.1 Existing Codebase Deficiencies
An in-depth audit of the repository (`/Users/mkshaon/playground/MyNotification-main`) revealed critical structural and functional divergences from the product specification:

1. **Fatal Notification Capture Bug in `MyNotificationListenerService.kt`**:
   - **Current Implementation**: Saves notifications to the database **only if** `shouldBlock == true` (`if (shouldBlock) { repository.insertNotification(...) }`).
   - **Specification Requirement**: **Every single incoming notification must be captured and persisted** locally as part of internal notification history regardless of whether the blocker is ON or OFF. The blocker only determines whether the notification is suppressed from the system tray and tags it with `wasBlocked = true` and `blockReason`.
2. **Missing Deduplication & Logical Event Tracking**:
   - The current listener treats every `onNotificationPosted()` callback as an isolated item.
   - Ongoing notifications (download progress, active calls, media players, timers) that update dozens or hundreds of times flood the database, distorting notification counts and analytics.
   - `onNotificationRemoved()` is completely unimplemented; removal timestamp, lifetime duration, and active status are lost.
3. **Inadequate Database Schema (`Database.kt`, `NotificationDao.kt`)**:
   - Only 2 rudimentary tables exist: `blocked_apps` and `saved_notifications`.
   - Missing tables for: `logical_events`, `event_updates`, `blocking_rules`, `rule_conditions`, `whitelisted_apps`, `whitelisted_keywords`, `schedules`, `notification_categories`, `analytics_snapshots`, and `retention_policies`.
   - `SavedNotification` lacks critical Android metadata: notification key, channel ID/name, category, importance, priority, flags, ongoing state, clearable state, progress values, first-seen time, last-seen time, update count, and responsible rule attribution.
4. **Navigation & Information Architecture Mismatch**:
   - Uses an obsolete drawer navigation (`ModalNavigationDrawer`) with non-specified screens: `VaultScreen` ("Secure Vault Inbox") and `ProfileScreen`.
   - The specification mandates a clean **4-tab Bottom Navigation** (`Home`, `History`, `Rules`, `Analytics`) and a dedicated **Settings** screen accessed via the top-right gear icon `⚙`.
5. **UI / UX Disconnect from Design System**:
   - The current UI does not implement the semantic color tokens (`#080D14`, `#101720`, `#151F2B`, `#4F6BFF`, `#2CCB82`, `#F05B67`, `#8B6CFF`, etc.).
   - The Hero ring card, 3-metric summary row, horizontal filter chips, interactive bar chart, app ranking progress bars, and calm dark/light mode parity are completely missing.
6. **Missing Business Engines**:
   - **Rule Engine**: No support for complex multi-condition rules (AND/OR, text contains, channels, times, days).
   - **Protection Center**: Incomplete OTP heuristics (only checks 4-8 digits regex without false-positive prevention against marketing SMS with numbers); zero financial protection logic (bKash, Nagad, banks); no promotional SMS spam filtering.
   - **Scheduling Engine**: No background schedule evaluation for quiet hours or class modes.
   - **Data Retention & Privacy**: No automated pruning (7d, 30d, 90d, 180d, 1y) or export/import.
   - **Onboarding Flow**: No multi-step permission and initial strategy wizard.

---

## 2. Target Architecture & Core Principles

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│   ┌──────────────┬──────────────────┬────────────────┬──────────────┐  │
│   │ Home Screen  │  History Screen  │  Rules Screen  │  Analytics   │  │
│   └──────────────┴──────────────────┴────────────────┴──────────────┘  │
│   ┌──────────────┬──────────────────┬────────────────┬──────────────┐  │
│   │   Settings   │  Rule Builder    │   Schedules    │  Onboarding  │  │
│   └──────────────┴──────────────────┴────────────────┴──────────────┘  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ StateFlow / Actions
┌───────────────────────────────────▼────────────────────────────────────┐
│                    ViewModel & State Management Layer                  │
│       HomeViewModel, HistoryViewModel, RulesViewModel, etc.            │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Clean Architecture UseCases
┌───────────────────────────────────▼────────────────────────────────────┐
│                             Domain Layer                               │
│  ┌─────────────────────────┐  ┌─────────────────────────────────────┐  │
│  │ NotificationRuleEngine  │  │ NotificationDeduplicationEngine     │  │
│  └─────────────────────────┘  └─────────────────────────────────────┘  │
│  ┌─────────────────────────┐  ┌─────────────────────────────────────┐  │
│  │ ProtectionEngine (OTP)  │  │ ScheduleEvaluationEngine            │  │
│  └─────────────────────────┘  └─────────────────────────────────────┘  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Repositories
┌───────────────────────────────────▼────────────────────────────────────┐
│                     Data Layer (Room + DataStore)                      │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ Room DB: logical_events, updates, rules, schedules, keywords   │   │
│   ├────────────────────────────────────────────────────────────────┤   │
│   │ DataStore: appearance, master toggles, onboarding, retention   │   │
│   └────────────────────────────────────────────────────────────────┘   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Coroutines Flow / Room Invalidation
┌───────────────────────────────────▼────────────────────────────────────┐
│                   Android OS Integration Services                      │
│   ┌─────────────────────────────────┐ ┌────────────────────────────┐   │
│   │ MyNotificationListenerService   │ │ WorkManager (Retention)    │   │
│   └─────────────────────────────────┘ └────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

### Core Product Directives
1. **Always Capture, Intelligently Filter**: All notifications are saved to local history. The blocker decides whether to suppress notification from device status bar/lock screen and flags it as `Blocked` or `Allowed`.
2. **Logical Events over Raw Callbacks**: Continuous updates (downloads, calls, timers, media) must be grouped into a single `NotificationEvent` with duration, update counter, and lifecycle timestamps.
3. **Deterministic Rule Priority**:
   1. Emergency & System-Critical Bypass
   2. Whitelisted Apps
   3. Whitelisted Keywords
   4. Active Schedules
   5. Explicit App / Condition Block Rules
   6. Predefined Categories (Social Media)
   7. Global Block All
   8. Default Policy (Allow)
4. **Transparent Attribution**: The user must always be able to see exactly why a notification was blocked (e.g., `Blocked by: Sleep Schedule → Global Block` or `Allowed by: Keyword Whitelist → OTP`).
5. **Absolute Local Privacy**: 100% offline, zero tracking, zero external network requests, user-controlled data retention.

---

## 3. Production Phase Breakdown & Detailed Task List

```
Phase 0 ──► Phase 1 ──► Phase 2 ──► Phase 3 ──► Phase 4 ──► Phase 5 ──► Phase 6 ──► Phase 7 ──► Phase 8
Cleanup     Data Layer  Listener    Rule Engine Design Sys  Primary UI  Subscreens  Onboarding  Hardening
```

---

### Phase 0: Legacy Codebase Cleanup & Foundation Restructuring
**Goal:** Remove deprecated concepts, establish target package structure, and prepare dependencies.

- [ ] **Task 0.1: Remove Deprecated UI & Unused Artifacts**
  - Delete `ui/screens/VaultScreen.kt` (non-specified "vault" concept).
  - Delete `ui/screens/ProfileScreen.kt` (non-specified profile screen).
  - Remove `ModalNavigationDrawer` and `ui/components/AppDrawerContent.kt`.
  - Clean out outdated string resources and unused drawable assets.
- [ ] **Task 0.2: Package Restructuring for Clean Architecture**
  - `core/designsystem/` (Tokens, Theme, Typography, Color, Shape, Icons, Components)
  - `core/database/` (Entities, Room Database, DAOs, TypeConverters)
  - `core/datastore/` (User Preferences, Onboarding State, Settings)
  - `core/domain/` (RuleEngine, DeduplicationEngine, ProtectionEngine, ScheduleEngine)
  - `core/service/` (NotificationListenerService, LifecycleManager)
  - `core/model/` (UI models, Enums, State contracts)
  - `feature/home/` (Dashboard, Hero ring, 3-metric row, fast navigation)
  - `feature/history/` (Notification Feed, Search, Filter chips, Detail modal/screen)
  - `feature/rules/` (Rule lists, Rule card toggles, Category manager, Priority viewer)
  - `feature/rules/builder/` (Custom rule creation & condition builder)
  - `feature/schedules/` (Schedule list, Schedule editor, recurrence selector)
  - `feature/analytics/` (Dashboard, Bar chart, Top apps ranking, Insights cards)
  - `feature/settings/` (Appearance, Permissions, Storage, Data Retention, Export/Import)
  - `feature/protection/` (OTP protection, Financial alerts, Promotional SMS filters)
  - `feature/onboarding/` (6-step permission and setup wizard)
- [ ] **Task 0.3: Dependency Verification & Build Configuration**
  - Verify Jetpack Compose BOM 2026.02+, Kotlin 2.2+, KSP, Hilt 2.59+.
  - Add `androidx.compose.material:material-icons-extended` for complete icon set (shield, lock, bell, clock, check, etc.).
  - Add `androidx.work:work-runtime-ktx` for automated retention cleanup and schedule triggers.

---

### Phase 1: Robust Data Layer & Schema Architecture (Room + DataStore)
**Goal:** Build a complete schema supporting logical events, update histories, advanced rule trees, schedules, and analytics snapshots.

- [ ] **Task 1.1: Design and Implement Room Entities**
  - `NotificationEventEntity`:
    - `id` (Long, auto-generate PK)
    - `notificationKey` (String, indexed)
    - `packageName` (String, indexed)
    - `appLabel` (String)
    - `initialTitle` (String), `latestTitle` (String)
    - `initialText` (String), `latestText` (String)
    - `subText` (String?), `bigText` (String?)
    - `channelId` (String), `channelName` (String?)
    - `category` (String?)
    - `flags` (Int), `importance` (Int)
    - `isOngoing` (Boolean), `isClearable` (Boolean)
    - `isProgress` (Boolean), `progress` (Int), `maxProgress` (Int)
    - `isOtp` (Boolean), `otpCode` (String?)
    - `isFinancial` (Boolean), `isPromotional` (Boolean)
    - `firstSeenAt` (Long, indexed), `lastUpdatedAt` (Long, indexed), `removedAt` (Long?)
    - `durationMs` (Long)
    - `updateCount` (Int, default 1)
    - `wasBlocked` (Boolean, indexed)
    - `blockReason` (String?)
    - `responsibleRuleId` (Long?)
    - `matchingRuleName` (String?)
    - `isRead` (Boolean, default false)
  - `NotificationUpdateEntity` (detailed timeline of continuous updates):
    - `id` (Long PK), `eventId` (Long, FK to NotificationEventEntity on-delete CASCADE)
    - `title` (String), `text` (String), `progress` (Int), `timestamp` (Long)
  - `BlockingRuleEntity`:
    - `id` (Long PK), `name` (String), `description` (String)
    - `ruleType` (Enum: GLOBAL_BLOCK, SOCIAL_MEDIA, SELECTED_APPS, CUSTOM, SCHEDULED, KEYWORD)
    - `action` (Enum: BLOCK, ALLOW)
    - `isEnabled` (Boolean)
    - `priority` (Int)
    - `createdAt` (Long), `updatedAt` (Long)
  - `RuleConditionEntity`:
    - `id` (Long PK), `ruleId` (Long, FK), `conditionType` (Enum: APP, TITLE, TEXT, KEYWORD, CHANNEL, TIME, DAY_OF_WEEK, ONGOING)
    - `operator` (Enum: EQUALS, CONTAINS, NOT_CONTAINS, STARTS_WITH, ENDS_WITH, IN_RANGE)
    - `value` (String)
  - `WhitelistedAppEntity`:
    - `packageName` (String PK), `appLabel` (String), `addedAt` (Long), `reason` (String?)
  - `WhitelistedKeywordEntity`:
    - `keyword` (String PK), `category` (String: OTP, Financial, Custom), `addedAt` (Long)
  - `ScheduleEntity`:
    - `id` (Long PK), `title` (String), `startHour` (Int), `startMinute` (Int), `endHour` (Int), `endMinute` (Int)
    - `repeatDays` (Int bitmask or comma-separated weekdays)
    - `action` (Enum: BLOCK_ALL, BLOCK_SOCIAL, CUSTOM)
    - `isEnabled` (Boolean)
    - `exceptionsJson` (String: whitelisted apps/keywords)
  - `NotificationCategoryEntity`:
    - `categoryId` (String PK: SOCIAL, FINANCIAL, MESSAGING, SHOPPING, WORK, ENTERTAINMENT, OTHER)
    - `displayName` (String), `isEditable` (Boolean)
    - `packageNamesJson` (String / List<String>)
- [ ] **Task 1.2: Implement DAOs with Comprehensive Flow Queries**
  - `NotificationDao`:
    - `observeRecentEvents(limit: Int)`: Flow<List<NotificationEventEntity>>
    - `observeFilteredEvents(status: String?, query: String?, category: String?, fromTime: Long, toTime: Long)`: Flow<List<NotificationEventEntity>>
    - `getEventByKey(key: String)`: NotificationEventEntity?
    - `insertEvent`, `updateEvent`, `deleteEvent`, `deleteEventsBefore(timestamp: Long)`
    - `observeAnalyticsStats(startTime: Long, endTime: Long)`: Total, Blocked, Allowed, Important counts
    - `observeTopApps(startTime: Long, endTime: Long, limit: Int)`: List of (packageName, appLabel, count, blockedCount)
    - `observeHourlyHistogram(startTime: Long, endTime: Long)`: List of (hour, count)
  - `RuleDao`, `ScheduleDao`, `CategoryDao`:
    - Full reactive CRUD operations for all blocking rules, conditions, whitelist tables.
- [ ] **Task 1.3: DataStore Settings Architecture**
  - `AppPreferencesRepository`:
    - `isBlockerMasterEnabled: Flow<Boolean>`
    - `activeBlockingMode: Flow<BlockingMode>` (SOCIAL_MEDIA, SELECTED_APPS, BLOCK_ALL, CUSTOM)
    - `themePreference: Flow<ThemeMode>` (DARK, LIGHT, SYSTEM)
    - `isOtpProtectionEnabled: Flow<Boolean>` (default true)
    - `isFinancialProtectionEnabled: Flow<Boolean>` (default true)
    - `isPromotionalSmsFilterEnabled: Flow<Boolean>` (default true)
    - `isEmergencyBypassEnabled: Flow<Boolean>` (default true)
    - `dataRetentionPeriod: Flow<RetentionPeriod>` (DAYS_7, DAYS_30, DAYS_90, DAYS_180, YEAR_1, FOREVER)
    - `isOnboardingCompleted: Flow<Boolean>`
    - `quickPauseUntil: Flow<Long>`

---

### Phase 2: Notification Capture Pipeline, Deduplication & Lifecycle Engine
**Goal:** Build a flawless Android notification listener that captures every notification, suppresses blocked ones, and intelligently deduplicates ongoing streams.

- [ ] **Task 2.1: Implement Smart Deduplication Engine (`NotificationDeduplicationEngine`)**
  - Maintain an in-memory active key index with thread-safe lookup (`ConcurrentHashMap`).
  - Extract identity signature: `packageName` + `sbn.id` + `sbn.tag` + `channelId`.
  - Distinguish between **New Notification Event** and **Notification Update**:
    - If key matches an active existing event and notification is marked `FLAG_ONGOING_EVENT` or has progress:
      - Update existing `NotificationEventEntity`: increment `updateCount`, update `latestTitle`, `latestText`, `progress`, `lastUpdatedAt = System.currentTimeMillis()`.
      - Record historical snapshot in `NotificationUpdateEntity` (sample rate throttled to max 1 update per 500ms to prevent SQLite locking).
    - If key is new or previously completed:
      - Instantiate a fresh `NotificationEventEntity` with `firstSeenAt = System.currentTimeMillis()`.
- [ ] **Task 2.2: Implement Notification Removal Tracking**
  - In `onNotificationRemoved(sbn: StatusBarNotification?, reason: Int)`:
    - Retrieve active `NotificationEventEntity`.
    - Update `removedAt = System.currentTimeMillis()`.
    - Compute `durationMs = removedAt - firstSeenAt`.
    - Mark active session completed.
- [ ] **Task 2.3: Redesign `MyNotificationListenerService.kt`**
  - **Unconditional Storage**: Every posted notification is routed into the deduplication and persistence engine.
  - **Rule Evaluation**: Before deciding to cancel notification, invoke `NotificationRuleEngine.evaluate(sbn)`.
  - **Safe Suppression**:
    - If `ruleResult.shouldBlock == true`:
      - Call `cancelNotification(sbn.key)`.
      - Set `wasBlocked = true`, `blockReason = ruleResult.reason`, `responsibleRuleId = ruleResult.ruleId`.
    - Else:
      - Leave notification in the Android status bar.
      - Set `wasBlocked = false`, `blockReason = ruleResult.reason` (e.g., "Allowed by Whitelist").
  - **Safe Error Fallback**: Catch `SecurityException` or OS-level prevention on system/OEM notifications and record `wasBlocked = false`, `blockReason = "Android prevented suppression"`.

---

### Phase 3: Rule Evaluation Engine & Protection Services
**Goal:** Deliver a rock-solid, multi-layered deterministic rule engine with advanced OTP, financial, and promotional SMS heuristics.

- [ ] **Task 3.1: Deterministic Multi-tier Rule Evaluator (`NotificationRuleEngine`)**
  - Implement sequential evaluation chain:
    1. **Emergency & System Call Bypass**: If incoming call, alarm, or emergency bypass contact -> `ALLOW (Emergency Bypass)`.
    2. **OTP & Security Protection**: If `isOtpProtectionEnabled` and detector matches -> `ALLOW (Protected OTP)`.
    3. **Financial Notification Protection**: If `isFinancialProtectionEnabled` and app/keyword matches trusted financial patterns -> `ALLOW (Protected Financial)`.
    4. **App Whitelist**: If `packageName` is in `WhitelistedAppEntity` -> `ALLOW (Whitelisted App)`.
    5. **Keyword Whitelist**: If title/text contains any active `WhitelistedKeywordEntity` -> `ALLOW (Whitelisted Keyword)`.
    6. **Promotional SMS Spam Filter**: If from SMS/Messaging app and matches promotional package/marketing patterns -> `BLOCK (Promotional SMS Filter)`.
    7. **Schedule Evaluation**: Check active recurring schedules matching current time and day of week -> `BLOCK / ALLOW (Active Schedule)`.
    8. **Active Master Blocker Mode**:
       - `BLOCK_ALL`: `BLOCK (Global Block All)`
       - `SOCIAL_MEDIA`: If package is in Social Media category -> `BLOCK (Social Media Block)`
       - `SELECTED_APPS`: If package is in custom blocked apps list -> `BLOCK (Selected App Block)`
       - `CUSTOM_RULES`: Evaluate custom predicate tree (AND/OR condition checks).
    9. **Default Fallback**: `ALLOW (Default Policy)`.
- [ ] **Task 3.2: Advanced OTP Detection Engine (`OtpDetector`)**
  - Replace naive digit regex with multi-signal contextual analyzer:
    - Signal 1: Context keywords (`otp`, `verification code`, `security code`, `authentication code`, `one-time password`, `pin code`).
    - Signal 2: Token boundaries (`\b\d{4,8}\b` or `\b[A-Z0-9]{6}\b`).
    - Anti-Signal (Negative filters): Reject if text contains marketing markers ("recharge bonus", "GB for", "validity", "call rate", "dial *", "discount offer").
    - Return `OtpDetectionResult(isOtp: Boolean, otpCode: String?, confidence: Float)`.
- [ ] **Task 3.3: Financial Alert Detector (`FinancialDetector`)**
  - Pre-configure trusted Bangladeshi & international financial apps: `bKash`, `Nagad`, `Rocket`, `Upay`, `Cellfin`, banking apps (`Citytouch`, `EBL Skybanking`, `SC Mobile`, etc.).
  - Content keywords: "received", "sent", "transferred", "credited", "debited", "balance", "txn", "statement", "payment confirmed".
- [ ] **Task 3.4: Promotional SMS Spam Detector (`PromotionalFilter`)**
  - Patterns: Telecom packages ("GB", "MB", "Days", "Tk recharge", "cashback offer", "limited time offer", "dial *121*").
  - Ensures legitimate SMS (personal messages, banking SMS) are never mistakenly categorized as promotional spam.

---

### Phase 4: Design System, Tokens, Typography & Reusable Components
**Goal:** Implement the exact visual identity specified in `My_Notification_Design_System.md` with complete Dark & Light mode parity.

- [ ] **Task 4.1: Color Tokens & Semantic Palettes (`ThemeColorTokens.kt`)**
  - **Dark Mode**:
    - Background: `#080D14`
    - Surface: `#101720`
    - Surface Elevated: `#151F2B`
    - Surface Strong: `#1B2634`
    - Border: `#263241`
    - Border Subtle: `#1B2530`
    - Text Primary: `#F5F7FA`
    - Text Secondary: `#A9B3C0`
    - Text Tertiary: `#748091`
  - **Light Mode**:
    - Background: `#F7F9FC`
    - Surface: `#FFFFFF`
    - Surface Soft: `#F1F4F8`
    - Border: `#E4E9F0`
    - Border Strong: `#D5DCE6`
    - Text Primary: `#111827`
    - Text Secondary: `#5E6877`
    - Text Tertiary: `#8791A0`
  - **Accent & Semantic Colors**:
    - Primary: `#4F6BFF` (Interactive blue)
    - Success / Allowed: `#2CCB82` (Soft Dark Surface: `#123025`, Soft Light: `#DDF8EC`)
    - Error / Blocked: `#F05B67` (Soft Dark Surface: `#351B20`, Soft Light: `#FDE5E7`)
    - Security / OTP: `#8B6CFF`
    - Warning: `#F5B84B`
- [ ] **Task 4.2: Typography System (`Type.kt`)**
  - Inter font family with fallback to system Roboto.
  - Display Large (32sp Bold), Headline Medium (20sp SemiBold), Title Medium (16sp SemiBold), Body Medium (14sp Regular), Label Medium (12sp Medium).
- [ ] **Task 4.3: Shape & Spacing System**
  - 4dp base spacing grid (`xs: 4dp`, `sm: 8dp`, `md: 16dp`, `lg: 20dp`, `xl: 24dp`).
  - Corner Radii: Cards `16dp`, Hero `20dp`, Buttons `12dp`, Chips `999dp` (pill).
- [ ] **Task 4.4: Reusable Design System Components (`core/designsystem/components/`)**
  - `AppTopBar`: Compact header with title, optional subtitle, back button / settings gear.
  - `AppBottomNavBar`: 4-destination bar (`Home`, `History`, `Rules`, `Analytics`) matching reference.
  - `HeroRingCard`: Large glowing circular indicator with center bell icon, status title ("Focused Mode" / "Active"), and pill toggle button.
  - `MetricCard`: Compact equal-width metric box (value + label).
  - `FeatureNavCard`: Chevron list card for primary navigation ("Notification History", "Blocking Rules", "Analytics").
  - `NotificationRow`: App icon, Title, snippet, timestamp, 3-dots, and colored status badge (`Blocked`, `Allowed`, `Important`).
  - `StatusChip` & `FilterChip`: Rounded pill chips for filtering and tags.
  - `SearchBar`: 44dp height, rounded search input with clear button.
  - `AppSwitch`: Compact Android-standard toggle with primary blue track when active.
  - `RuleCard`: Reusable card with leading icon badge, title, subtitle, and switch or chevron.
  - `BarChart`: Custom Compose canvas drawing vertical bars with subtle grid, date labels, and active highlight.
  - `AppRankingRow`: Row with app icon, app label, horizontal progress bar, and notification count.
  - `EmptyState` & `ErrorState`: Clean, calm instructional placeholder with actionable button.

---

### Phase 5: Primary Screen Implementation (10-Screen Parity)
**Goal:** Build the 5 core screens in both Dark and Light modes exactly as shown in the visual concept reference image.

- [ ] **Task 5.1: Screen 1 — Home / Dashboard (`HomeScreen.kt`)**
  - Header: "My Notification", Subtitle: "Less noise. More you.", Settings gear icon ⚙.
  - Hero Card:
    - Circular glowing ring with notification bell icon.
    - Title: "Focused Mode" / "Active" (or "Notification Blocker" / "Active" / "Paused").
    - Pill button: "Tap to disable" / "Tap to enable".
  - 3 Summary Metrics:
    - Card 1: `128` Blocked (red accent)
    - Card 2: `12` Allowed (green accent)
    - Card 3: `86` Important (blue/purple accent)
  - 3 Feature Navigation Cards:
    - "Notification History" — "View and search all notifications" → Navigates to History tab
    - "Blocking Rules" — "Apps, keywords, schedules" → Navigates to Rules tab
    - "Analytics" — "See your notification insights" → Navigates to Analytics tab
  - Scaffold with Bottom Navigation (`Home`, `History`, `Rules`, `Analytics`).
- [ ] **Task 5.2: Screen 2 — Notification History (`HistoryScreen.kt`)**
  - Header: Back arrow `←` (or tab header), Title: "Notification History".
  - Rounded search bar: "Search notifications...".
  - Horizontal filter chips: `All`, `Allowed`, `Blocked`, `Important`.
  - Date sections (e.g. "Today", "Yesterday").
  - Notification items feed:
    - App icon (colored squircle), App name, message text, timestamp (e.g. "9:40 PM").
    - Status badges: Red `Blocked` badge, Green `Allowed`, Blue/Purple `Important`.
    - Three-dot contextual menu (Mark as read, Copy text, Add to whitelist, Delete).
    - Tap item opens full **Notification Detail Screen**.
- [ ] **Task 5.3: Screen 3 — Blocking Rules (`BlockingRulesScreen.kt`)**
  - Header: Title: "Blocking Rules", Action button: `+` (Create Rule).
  - 6 Master Rule Cards:
    - Card 1: `Block Social Media` (group icon, subtitle: "Instagram, Facebook, TikTok...") + Switch.
    - Card 2: `Block Selected Apps` (apps grid icon, subtitle: "X apps selected") + Switch.
    - Card 3: `Allow Important Keywords` (shield icon, subtitle: "OTP, verification, payment...") + Switch.
    - Card 4: `Schedule Blocking` (clock icon, subtitle: "10:00 PM – 7:00 AM") + Switch.
    - Card 5: `Block Everything` (minus icon, subtitle: "Except whitelisted apps") + Switch.
    - Card 6: `Custom Rules` (slider icon, subtitle: "X custom rules") + Chevron `>`.
  - Tap card opens detailed editor for that rule type.
- [ ] **Task 5.4: Screen 4 — Analytics (`AnalyticsScreen.kt`)**
  - Header: Title: "Analytics", Dropdown chip: "Last 7 days ▼" (opens time range sheet: 1h, 5h, 24h, Today, Yesterday, 7 days, Custom).
  - Hero Card:
    - Large metric: `1,248` (Total Notifications).
    - Custom Compose Bar Chart (Mon - Sun volume breakdown with y-axis indicators 0, 100, 200, 300).
    - Breakdown metrics: `812` Blocked | `326` Allowed | `110` Important.
  - Top Apps section:
    - Header: "Top Apps", Action: "See all".
    - Ranked rows with app icon, app label, colored horizontal proportion bar, and exact count:
      - Instagram (320), Facebook (210), YouTube (180), WhatsApp (120), Gmail (90).
  - Smart Insights section:
    - Neutral cards: "68% of notifications came from 3 apps", "Most notifications arrived between 7 PM and 11 PM".
- [ ] **Task 5.5: Screen 5 — Settings (`SettingsScreen.kt`)**
  - Header: Title: "Settings".
  - Section 1: **Appearance**:
    - Radio rows: Dark Mode (moon icon), Light Mode (sun icon), System Default (circle icon).
  - Section 2: **General**:
    - `Notification Access` with status badge: `Granted` (green) / `Needs attention` (red).
    - `Battery Optimization` with status badge: `Ignore` / `Fix`.
    - `Data & Privacy` → Navigates to Data & Privacy.
    - `Backup & Restore` → Navigates to Backup & Restore.
    - `About` → Navigates to About.
  - Footer: Calming philosophy quote:
    - `"A quieter phone"`
    - `"A calmer mind."`

---

### Phase 6: Sub-Screens, Management Workflows & Custom Rule Builder
**Goal:** Build all secondary, management, and deep configuration screens required by the specification.

- [ ] **Task 6.1: Notification Detail Screen (`NotificationDetailScreen.kt`)**
  - App hero: App icon, App name, Received timestamp, Status badge (`Blocked` / `Allowed`).
  - Content Card: Title, Body text, Big text, Subtext.
  - Filtering Card: Status, Responsible rule name, Matched keyword, Evaluation details.
  - Technical Card: Notification Key, Channel ID, Channel Name, Category, Importance, Flags.
  - Lifecycle Card: First seen, Last updated, Removed at, Duration, Total updates count.
- [ ] **Task 6.2: Custom Rule Builder (`RuleBuilderScreen.kt`)**
  - Natural visual sentence blocks:
    - `WHEN` [App = Selected App] `AND` [Notification contains "keyword"] `AND` [Time between 10 PM - 8 AM]
    - `THEN` [BLOCK / ALLOW]
  - Rule simulation / preview: "This rule would have affected 16 notifications today".
- [ ] **Task 6.3: Schedules Manager & Schedule Editor (`ScheduleScreen.kt`, `ScheduleEditorScreen.kt`)**
  - Schedule List with active status toggles.
  - Editor: Name, Start/End time pickers, Weekday selector chips, Action (Block All, Block Social, Custom), Exceptions (Whitelisted apps, OTP, Financial).
- [ ] **Task 6.4: Protection Center Screen (`ProtectionCenterScreen.kt`)**
  - `OTP Protection` manager: Toggle ON/OFF, active keywords list, "Add keyword".
  - `Financial Notifications` manager: Toggle ON/OFF, trusted financial apps checklist.
  - `Promotional SMS Filter` manager: Toggle ON/OFF, sample blocked vs allowed rules.
  - `Emergency Bypass` manager: Emergency contacts/apps selector.
- [ ] **Task 6.5: App Picker Screen (`AppPickerScreen.kt`)**
  - Searchable list of installed apps with real app icons, labels, package names, category badges, and multi-select checkboxes.
- [ ] **Task 6.6: Whitelisted Apps & Whitelisted Keywords Screens**
  - `WhitelistedAppsScreen.kt`: List with "Add App", reason tags ("Always allowed"), delete swipe/action.
  - `WhitelistedKeywordsScreen.kt`: Chip list with "Add Keyword", usage count ("Used in 32 allowed notifications").
- [ ] **Task 6.7: Notification Categories Screen (`CategoriesScreen.kt`)**
  - Categorization of apps: Social Media, Financial, Messaging, Work, Shopping, Entertainment, Other.
  - User ability to move apps between categories.
- [ ] **Task 6.8: Rule Priority Visualizer (`RulePriorityScreen.kt`)**
  - Visual ordered list showing evaluation hierarchy from Emergency to Default policy so advanced users understand execution sequence.
- [ ] **Task 6.9: Help, Tutorial & Problem Reporting**
  - `HelpScreen.kt`: FAQ on notification access, OEM battery restrictions, Android suppression realities.
  - `TutorialScreen.kt`: Interactive walkthrough of the 5 core screens.
  - `ReportProblemScreen.kt`: Issue description form with auto-detected Android OS and device model (no notification content attached).

---

### Phase 7: Onboarding Flow, Data Retention & Backup/Restore
**Goal:** Build a smooth first-run onboarding experience, automated storage pruning, and local encrypted backup.

- [ ] **Task 7.1: Multi-Step Onboarding Wizard (`OnboardingScreen.kt`)**
  - **Step 1: Introduction**: "Less noise. More control. My Notification helps you manage what reaches you." -> CTA: "Get Started".
  - **Step 2: Notification Access**: Clear explanation why access is needed (view history, manage blocking, provide insights). CTA: "Enable Notification Access" (deep-links to `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`).
  - **Step 3: Default Strategy**: Radio choices: "Block Social Media", "Selected Apps", "Block All", "Allow All".
  - **Step 4: Important Apps Selection**: Multi-select picker with sensible defaults (WhatsApp, Phone, Gmail, bKash).
  - **Step 5: Protection Verification**: Quick switches: OTP Protection ON, Financial Protection ON.
  - **Step 6: Optional Schedule**: Quick Sleep mode prompt or "Skip for now".
  - **Step 7: Finish**: Sets `isOnboardingCompleted = true` and navigates to Home.
- [ ] **Task 7.2: Data Retention Engine (`RetentionManager` + `WorkManager`)**
  - Configurable retention in Settings: 7 days, 30 days, 90 days, 180 days, 1 year, Forever.
  - Background `PeriodicWorkRequest` (running once daily) to purge `notification_events` and `notification_updates` older than selected retention.
  - Storage stats UI: displays total events count and approximate database file size in MB.
  - "Delete all notification history" button with double confirmation dialog.
- [ ] **Task 7.3: Local Backup & Restore (`BackupManager`)**
  - Export rules, schedules, whitelists, and history to structured JSON format.
  - Import / Restore backup file with schema validation.

---

### Phase 8: Production Hardening, Battery Optimization, Edge Cases & Verification
**Goal:** Comprehensive testing across Android versions, OEM background optimization, and quality assurance.

- [ ] **Task 8.1: Battery Optimization & OEM Background Reliability**
  - Request ignore battery optimizations (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) if needed.
  - Guide users on OEM-specific killers (Xiaomi MIUI/HyperOS autostart, Samsung sleeping apps, OnePlus battery restrictions) in Settings.
- [ ] **Task 8.2: Android 14/15/16 Platform Compatibility**
  - Respect `POST_NOTIFICATIONS` permission (Android 13+).
  - Handle full-screen intent limitations gracefully without claiming impossible 100% blocking.
  - Provide accurate in-app explanations if Android prevents complete suppression.
- [ ] **Task 8.3: Unit & Component Testing**
  - `OtpDetectorTest`: Benchmark against positive OTPs and negative marketing text with digits.
  - `NotificationRuleEngineTest`: Verify deterministic evaluation hierarchy across conflicts.
  - `DeduplicationEngineTest`: Verify download progress and call notifications result in 1 logical event.
  - `AnalyticsCalculationTest`: Verify accurate counts and no inflated statistics.
- [ ] **Task 8.4: End-to-End Flow Verification & Design QA Checklist**
  - Verify layout insets, status bar padding, 48dp minimum touch targets, TalkBack semantics.
  - Complete Dark Mode and Light Mode visual check against reference screenshot.

---

## 4. Screen Mapping Matrix

| Screen Name | Route | Spec Section | Reference Image Position |
|---|---|---|---|
| **Home / Dashboard** | `home` | Spec §18, Design §17-21 | Screen 1 (Dark & Light) |
| **Notification History** | `history` | Spec §15-16, Design §23-26 | Screen 2 (Dark & Light) |
| **Blocking Rules** | `rules` | Spec §3-4, Design §28-29 | Screen 3 (Dark & Light) |
| **Analytics** | `analytics` | Spec §13-14, Design §38-44 | Screen 4 (Dark & Light) |
| **Settings** | `settings` | Spec §25, 30, Design §45-51 | Screen 5 (Dark & Light) |
| **Notification Detail** | `history/detail/{id}` | Spec §17, Design §27, 63 | Expansion of History |
| **Rule Builder** | `rules/create` | Spec §4, Design §30-31 | Expansion of Rules |
| **Schedule List & Editor** | `rules/schedules` | Spec §5, Design §32-33 | Expansion of Rules |
| **Protection Center** | `settings/protection` | Spec §7-9, Design §34-37 | Expansion of Settings |
| **Whitelisted Apps** | `rules/whitelist/apps` | Spec §3.4, Design §85 | Expansion of Rules |
| **Whitelisted Keywords** | `rules/whitelist/keywords`| Spec §3.5, Design §86 | Expansion of Rules |
| **Categories Manager** | `rules/categories` | Spec §3.2, Design §83 | Expansion of Rules |
| **App Picker** | `common/app-picker` | Spec §21, Design §84 | Reusable Selection Flow |
| **Data Retention & Storage**| `settings/privacy` | Spec §30-31, Design §50, 87 | Expansion of Settings |
| **Backup & Restore** | `settings/backup` | Spec §31, Design §89 | Expansion of Settings |
| **Onboarding Wizard** | `onboarding` | Spec §40, Design §76 | First-run Modal Flow |
| **Help & Tutorial** | `support/help` | Spec §24, Design §90-91 | Expansion of Menu/Settings |

---

## 5. Execution Strategy & Next Steps
1. **Repository Synchronization**: Commit and push this production roadmap to GitHub.
2. **Approval & Phase 0 Initiation**: Await user go-ahead to begin Phase 0 (cleanup legacy files) and Phase 1 (new Room database architecture).
3. **Iterative Build & Verification**: Implement each phase sequentially, running tests, ensuring spotless code quality, and executing git commit + push after every milestone.
