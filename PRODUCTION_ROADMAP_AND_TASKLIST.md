# My Notification — Complete Production Master Specification, Codebase Audit & Implementation Roadmap

> **Platform:** Android (Native Kotlin · Jetpack Compose · Room · DataStore · WorkManager · Hilt)  
> **Design Authority:** [`My_Notification_Design_System.md`](file:///Users/mkshaon/playground/My_Notification_Design_System.md)  
> **Product Authority:** [`My_Notification_Product_Specification.md`](file:///Users/mkshaon/playground/My_Notification_Product_Specification.md)  
> **Visual Reference:** 10-screen visual reference concept (Home, History, Blocking Rules, Analytics, Settings across Dark and Light modes)

---

## 1. Executive Summary & Core Mandate

**My Notification** is a personal notification operating layer for Android devices. Its purpose is to give the user absolute control over their notification stream:
1. **Always Capture & Store Everything**: Regardless of whether the notification blocker is turned ON or OFF, all incoming device notifications are captured and persisted locally into internal notification history upon granting permissions during onboarding.
2. **Deterministic Suppression When Blocker Is ON**: When the blocker is enabled, notifications are evaluated against a prioritized rule hierarchy (Emergency Bypass → Whitelists → Schedules → Explicit Rules → Categories → Global Block) and suppressed from the system tray if blocked.
3. **Transparent Rule Attribution**: Every single notification in history clearly indicates whether it was **Allowed** or **Blocked**, with the exact rule responsible displayed (e.g. `Blocked by: Block Social Media → Instagram` or `Allowed by: Whitelisted Keyword → OTP`).
4. **Logical Event Deduplication**: Continuous notifications (download progress, phone calls, timers, media playback) updating dozens or hundreds of times are merged into a single logical notification event with lifecycle duration, update counters, and progress tracking, eliminating inflated analytics.
5. **Zero Cloud / Absolute Local Privacy**: 100% offline, on-device SQLite Room database, zero third-party trackers, no cloud sync, configurable retention periods (7 days to forever), and an irreversible local data wipe option.
6. **Unified Dual-Theme Design System**: Clean, calm, professional aesthetic with strict color token parity across Dark Mode (`#080D14`, `#101720`, `#151F2B`) and Light Mode (`#F7F9FC`, `#FFFFFF`, `#F1F4F8`), electric blue accents (`#4F6BFF`), and semantic status indicators.

---

## 2. In-Depth Codebase Audit & Gap Analysis

A rigorous line-by-line audit of the existing repository (`/Users/mkshaon/playground/MyNotification-main`) identified the following file-by-file status and architectural defects:

### 2.1 File-by-File Audit Matrix

| Existing File | Current Role | Deficiencies & Specification Conflicts | Required Action |
|---|---|---|---|
| `service/MyNotificationListenerService.kt` | Android NotificationListenerService | **Fatal Bug:** Only inserts notifications if `shouldBlock == true` (`if (shouldBlock) { repository.insertNotification(...) }`). If blocker is OFF or app is allowed, notifications are lost. Missing `onNotificationRemoved` tracking. No deduplication. Hardcoded checks for package strings (`dialer`, `sms`). | **Complete Rewrite:** Unconditional capture of all notifications into deduplication pipeline; call deterministic `NotificationRuleEngine`; cancel notification only if blocked; record removal timestamp and duration in `onNotificationRemoved`. |
| `data/Database.kt` | Room Database Definition | Only 2 rudimentary entities: `BlockedApp` and `SavedNotification`. Missing 7 required tables (`notification_events`, `notification_updates`, `blocking_rules`, `rule_conditions`, `whitelisted_apps`, `whitelisted_keywords`, `schedules`, `notification_categories`). Missing critical notification metadata (key, groupKey, channel, flags, ongoing, progress, updateCount, rule attribution). | **Replace with New Schema:** Upgrade to Room Version 2 with complete relational entities and indexes. |
| `data/NotificationDao.kt` | Data Access Object | Only basic CRUD for `blocked_apps` and `saved_notifications`. No multi-filter search queries (Allowed, Blocked, Important), no time-range analytics aggregation queries, no top-apps grouping. | **Replace with Comprehensive DAOs:** Split into `NotificationDao` (events, updates, analytics) and `RuleDao` (rules, conditions, whitelists, schedules, categories). |
| `data/SettingsDataStore.kt` | User Preferences | Only contains `isBlockAllEnabled`, `quickPauseUntil`, `themePreference`. Missing onboarding status, master blocker toggle, active blocking mode, OTP protection, financial protection, promotional SMS filter, emergency bypass, and retention policy. | **Expand Preferences:** Add all required DataStore keys and reactive Flows. |
| `data/AppRepository.kt` | Repository Layer | Tied to legacy `SavedNotification` and minimal methods. | **Refactor:** Expose reactive domain streams for events, rules, whitelists, schedules, categories, and analytics. |
| `di/DiModules.kt` | Hilt Dependency Injection | Only provides `AppDatabase`, `NotificationDao`, `SettingsDataStore`, `AppRepository`. | **Update:** Provide `RuleDao` and inject into `AppRepository` and domain engines. |
| `MainActivity.kt` | Entry Activity | Uses `ModalNavigationDrawer` with legacy routes. Edge-to-edge status bar handling is incomplete. | **Refactor:** Remove drawer; implement 4-tab `BottomNavigation` (`Home`, `History`, `Rules`, `Analytics`) and toolbar navigation to `Settings`. |
| `ui/MainViewModel.kt` | View Model | Monolithic, mixes package loading with rudimentary block toggling. | **Refactor / Modularize:** Split into focused ViewModels (`HomeViewModel`, `HistoryViewModel`, `RulesViewModel`, `AnalyticsViewModel`, `SettingsViewModel`). |
| `ui/Screen.kt` | Navigation Sealed Class | Contains deprecated routes: `Vault`, `Profile`. | **Replace:** Define full navigation routes matching the screen hierarchy. |
| `ui/components/AppDrawerContent.kt` | Side Navigation Drawer | **Spec Violation:** Product spec explicitly mandates bottom navigation and screen-based navigation, not an overlay drawer. | **Delete File.** |
| `ui/components/AppHeader.kt` | Top App Bar | Uses legacy styling and profile icon button. | **Refactor:** Implement `AppTopBar` with subtitle and Settings gear icon `⚙`. |
| `ui/screens/DashboardScreen.kt` | Main Screen | Incorrect UI layout; missing Hero circular ring, 3-metric summary row, and feature navigation cards. | **Replace with New `HomeScreen.kt`** matching visual reference. |
| `ui/screens/VaultScreen.kt` | "Secure Vault Inbox" | **Spec Violation:** "Vault" concept does not exist in product specification or design system. | **Delete File.** |
| `ui/screens/ProfileScreen.kt` | User Profile Screen | **Spec Violation:** Unused profile screen not part of product scope. | **Delete File.** |
| `ui/screens/AppListScreen.kt` | App List Screen | Basic switch list without proper app icons, search filtering, or category grouping. | **Refactor into `AppPickerScreen.kt`** and `SelectedAppsScreen.kt`. |
| `ui/screens/SettingsScreen.kt` | Settings Screen | Does not match visual reference layout or sections (Appearance, General, Quote footer). | **Replace with New `SettingsScreen.kt`** matching visual reference. |
| `ui/theme/Color.kt`, `Theme.kt`, `Type.kt` | Theme & Design Tokens | Uses default Material3 colors instead of exact design system tokens. | **Complete Rewrite:** Implement full semantic design tokens for Dark and Light modes. |

---

## 3. Target System Architecture & Data Model

### 3.1 Logical Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                               Android Jetpack Compose UI                               │
│  ┌───────────────────────┬───────────────────────┬───────────────────┬──────────────┐  │
│  │   Home / Dashboard    │ Notification History  │   Blocking Rules  │  Analytics   │  │
│  └───────────────────────┴───────────────────────┴───────────────────┴──────────────┘  │
│  ┌───────────────────────┬───────────────────────┬───────────────────┬──────────────┐  │
│  │       Settings        │  Notification Detail  │   Rule Builder    │  Schedules   │  │
│  └───────────────────────┴───────────────────────┴───────────────────┴──────────────┘  │
│  ┌───────────────────────┬───────────────────────┬───────────────────┬──────────────┐  │
│  │   Protection Center   │      App Picker       │  Data Retention   │  Onboarding  │  │
│  └───────────────────────┴───────────────────────┴───────────────────┴──────────────┘  │
└───────────────────────────────────────────┬────────────────────────────────────────────┘
                                            │ StateFlow & UI Events
┌───────────────────────────────────────────▼────────────────────────────────────────────┐
│                                  ViewModel Layer                                       │
│       HomeViewModel · HistoryViewModel · RulesViewModel · AnalyticsViewModel           │
└───────────────────────────────────────────┬────────────────────────────────────────────┘
                                            │ Clean Architecture Interactors
┌───────────────────────────────────────────▼────────────────────────────────────────────┐
│                                   Domain Layer                                         │
│   ┌───────────────────────────┐ ┌───────────────────────────┐ ┌────────────────────┐   │
│   │   NotificationRuleEngine  │ │ NotificationDeduplicator  │ │    OtpDetector     │   │
│   └───────────────────────────┘ └───────────────────────────┘ └────────────────────┘   │
│   ┌───────────────────────────┐ ┌───────────────────────────┐ ┌────────────────────┐   │
│   │     FinancialDetector     │ │     PromotionalFilter     │ │   ScheduleEngine   │   │
│   └───────────────────────────┘ └───────────────────────────┘ └────────────────────┘   │
└───────────────────────────────────────────┬────────────────────────────────────────────┘
                                            │ Reactive Flow Streams
┌───────────────────────────────────────────▼────────────────────────────────────────────┐
│                                    Data Layer                                          │
│   ┌───────────────────────────────────────────────┐ ┌──────────────────────────────┐   │
│   │ Room Database (v2):                           │ │ SettingsDataStore            │   │
│   │  • notification_events    • schedules         │ │  • isMasterBlockerEnabled    │   │
│   │  • notification_updates   • whitelisted_apps  │ │  • activeBlockingMode        │   │
│   │  • blocking_rules         • whitelisted_keys  │ │  • themePreference           │   │
│   │  • rule_conditions        • blocked_apps      │ │  • isOtpProtectionEnabled    │   │
│   │  • notification_categories                    │ │  • dataRetentionDays         │   │
│   └───────────────────────────────────────────────┘ └──────────────────────────────┘   │
└───────────────────────────────────────────┬────────────────────────────────────────────┘
                                            │ Lifecycle Callbacks
┌───────────────────────────────────────────▼────────────────────────────────────────────┐
│                           Android System Services Layer                                │
│   ┌───────────────────────────────────────────────┐ ┌──────────────────────────────┐   │
│   │ MyNotificationListenerService                 │ │ WorkManager Retention Worker │   │
│   │  • onNotificationPosted() -> Unconditional DB │ │  • Daily purge of old events │   │
│   │  • onNotificationRemoved() -> Duration Calc   │ │  • Storage calculation       │   │
│   └───────────────────────────────────────────────┘ └──────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Relational Database Schema (Room v2)

```sql
-- 1. Logical Notification Events (Deduplicated)
CREATE TABLE notification_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    notification_key TEXT NOT NULL,
    package_name TEXT NOT NULL,
    app_label TEXT NOT NULL,
    initial_title TEXT NOT NULL,
    latest_title TEXT NOT NULL,
    initial_text TEXT NOT NULL,
    latest_text TEXT NOT NULL,
    big_text TEXT,
    sub_text TEXT,
    channel_id TEXT NOT NULL,
    channel_name TEXT,
    category TEXT,
    flags INTEGER NOT NULL DEFAULT 0,
    importance INTEGER NOT NULL DEFAULT 0,
    is_ongoing INTEGER NOT NULL DEFAULT 0,
    is_clearable INTEGER NOT NULL DEFAULT 1,
    is_progress INTEGER NOT NULL DEFAULT 0,
    progress INTEGER NOT NULL DEFAULT 0,
    max_progress INTEGER NOT NULL DEFAULT 0,
    is_otp INTEGER NOT NULL DEFAULT 0,
    otp_code TEXT,
    is_financial INTEGER NOT NULL DEFAULT 0,
    is_promotional INTEGER NOT NULL DEFAULT 0,
    first_seen_at INTEGER NOT NULL,
    last_updated_at INTEGER NOT NULL,
    removed_at INTEGER,
    duration_ms INTEGER NOT NULL DEFAULT 0,
    update_count INTEGER NOT NULL DEFAULT 1,
    was_blocked INTEGER NOT NULL DEFAULT 0,
    block_reason TEXT,
    responsible_rule_id INTEGER,
    matching_rule_name TEXT,
    is_read INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX index_events_key ON notification_events(notification_key);
CREATE INDEX index_events_pkg ON notification_events(package_name);
CREATE INDEX index_events_updated ON notification_events(last_updated_at);
CREATE INDEX index_events_blocked ON notification_events(was_blocked);

-- 2. Ongoing Notification Update Snapshots
CREATE TABLE notification_updates (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    event_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    text TEXT NOT NULL,
    progress INTEGER NOT NULL DEFAULT 0,
    timestamp INTEGER NOT NULL,
    FOREIGN KEY(event_id) REFERENCES notification_events(id) ON DELETE CASCADE
);
CREATE INDEX index_updates_event_id ON notification_updates(event_id);

-- 3. Master Blocking Rules
CREATE TABLE blocking_rules (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    rule_type TEXT NOT NULL, -- GLOBAL_BLOCK, SOCIAL_MEDIA, SELECTED_APPS, ALLOW_IMPORTANT_KEYWORDS, SCHEDULE_BLOCKING, BLOCK_EVERYTHING, CUSTOM
    action TEXT NOT NULL DEFAULT 'BLOCK', -- BLOCK, ALLOW
    is_enabled INTEGER NOT NULL DEFAULT 1,
    priority INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

-- 4. Custom Rule Conditions
CREATE TABLE rule_conditions (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    rule_id INTEGER NOT NULL,
    condition_type TEXT NOT NULL, -- APP, TITLE, TEXT, KEYWORD, CHANNEL, TIME_RANGE, DAY_OF_WEEK, ONGOING
    operator TEXT NOT NULL, -- EQUALS, CONTAINS, NOT_CONTAINS, STARTS_WITH, ENDS_WITH, IN_RANGE
    value TEXT NOT NULL,
    FOREIGN KEY(rule_id) REFERENCES blocking_rules(id) ON DELETE CASCADE
);
CREATE INDEX index_conditions_rule_id ON rule_conditions(rule_id);

-- 5. Whitelisted Apps
CREATE TABLE whitelisted_apps (
    package_name TEXT PRIMARY KEY NOT NULL,
    app_label TEXT NOT NULL,
    reason TEXT NOT NULL DEFAULT 'Always allowed',
    added_at INTEGER NOT NULL
);

-- 6. Whitelisted Keywords
CREATE TABLE whitelisted_keywords (
    keyword TEXT PRIMARY KEY NOT NULL,
    category TEXT NOT NULL DEFAULT 'OTP',
    added_at INTEGER NOT NULL
);

-- 7. Blocked Apps (Selected Apps Mode)
CREATE TABLE blocked_apps (
    package_name TEXT PRIMARY KEY NOT NULL,
    app_label TEXT NOT NULL,
    is_blocked INTEGER NOT NULL DEFAULT 1,
    priority TEXT NOT NULL DEFAULT 'Normal',
    category TEXT NOT NULL DEFAULT 'Selected',
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

-- 8. Schedules
CREATE TABLE schedules (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    title TEXT NOT NULL,
    start_hour INTEGER NOT NULL,
    start_minute INTEGER NOT NULL,
    end_hour INTEGER NOT NULL,
    end_minute INTEGER NOT NULL,
    repeat_days TEXT NOT NULL DEFAULT 'ALL',
    action TEXT NOT NULL DEFAULT 'BLOCK_ALL',
    is_enabled INTEGER NOT NULL DEFAULT 1,
    exceptions TEXT NOT NULL DEFAULT 'OTP,Financial,Calls',
    created_at INTEGER NOT NULL
);

-- 9. Notification Categories
CREATE TABLE notification_categories (
    category_id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    package_names TEXT NOT NULL,
    is_editable INTEGER NOT NULL DEFAULT 1
);
```

---

## 4. UI/UX Specification & Visual Reference Mapping

The UI is strictly based on the 10-screen visual reference concept in both Dark Mode and Light Mode:

```
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ 1. Home (Dark)  │ │ 2. History (Dark│ │ 3. Rules (Dark) │ │4. Analytics(Dark│ │ 5. Settings(Dark│
├─────────────────┤ ├─────────────────┤ ├─────────────────┤ ├─────────────────┤ ├─────────────────┤
│ 1. Home (Light) │ │ 2. History(Light│ │ 3. Rules (Light)│ │4.Analytics(Light│ │ 5. Settings(Ligh│
└─────────────────┘ └─────────────────┘ └─────────────────┘ └─────────────────┘ └─────────────────┘
```

### 4.1 Theme Token Specifications

```kotlin
// Dark Mode Palette
val DarkBackground       = Color(0xFF080D14)
val DarkSurface          = Color(0xFF101720)
val DarkSurfaceElevated  = Color(0xFF151F2B)
val DarkSurfaceStrong    = Color(0xFF1B2634)
val DarkBorder           = Color(0xFF263241)
val DarkBorderSubtle     = Color(0xFF1B2530)
val DarkTextPrimary      = Color(0xFFF5F7FA)
val DarkTextSecondary    = Color(0xFFA9B3C0)
val DarkTextTertiary     = Color(0xFF748091)

// Light Mode Palette
val LightBackground      = Color(0xFFF7F9FC)
val LightSurface         = Color(0xFFFFFFFF)
val LightSurfaceSoft     = Color(0xFFF1F4F8)
val LightBorder          = Color(0xFFE4E9F0)
val LightBorderStrong    = Color(0xFFD5DCE6)
val LightTextPrimary     = Color(0xFF111827)
val LightTextSecondary   = Color(0xFF5E6877)
val LightTextTertiary    = Color(0xFF8791A0)

// Semantic Accents (Shared)
val PrimaryBlue          = Color(0xFF4F6BFF) // Core interactive action
val SuccessGreen         = Color(0xFF2CCB82) // Allowed status
val SuccessGreenDarkBg   = Color(0xFF123025)
val SuccessGreenLightBg  = Color(0xFFDDF8EC)
val ErrorRed             = Color(0xFFF05B67) // Blocked status
val ErrorRedDarkBg       = Color(0xFF351B20)
val ErrorRedLightBg      = Color(0xFFFDE5E7)
val SecurityPurple       = Color(0xFF8B6CFF) // OTP / Security status
val WarningOrange        = Color(0xFFF5B84B)
```

### 4.2 Detailed Screen Blueprint

#### Screen 1: Home / Dashboard (`HomeScreen.kt`)
- **Top App Bar**:
  - Left: Title "My Notification" (18sp SemiBold), Subtitle "Less noise. More you." (13sp Secondary).
  - Right: Settings gear icon button ⚙ (`Modifier.clickable { onNavigateToSettings() }`).
- **Hero Ring Card**:
  - Background: `surface`, Border: `border`, Radius: `20dp`.
  - Center: Circular glowing progress ring (`PrimaryBlue`) with notification bell icon.
  - State Label: "Focused Mode" / "Active" (or "Notification Blocker" / "Inactive").
  - Action: Pill button "Tap to disable" (or "Tap to enable").
- **3-Metric Summary Row**:
  - 3 equal-width cards (`16dp` radius):
    - Card 1: `128` Blocked (Red text `#F05B67`).
    - Card 2: `12` Allowed (Green text `#2CCB82`).
    - Card 3: `86` Important/Saved (Blue/Purple text `#8B6CFF`).
- **Feature Navigation Cards**:
  - Row 1: "Notification History" — Subtitle: "View and search all notifications" — Leading icon: clock/bell, Trailing: chevron `>`.
  - Row 2: "Blocking Rules" — Subtitle: "Apps, keywords, schedules" — Leading icon: shield/sliders, Trailing: chevron `>`.
  - Row 3: "Analytics" — Subtitle: "See your notification insights" — Leading icon: bar-chart, Trailing: chevron `>`.
- **Bottom Navigation**:
  - 4 destinations: `Home` (selected), `History`, `Rules`, `Analytics`.

#### Screen 2: Notification History (`HistoryScreen.kt`)
- **Header & Search**:
  - Top Bar: Back arrow `←`, Title "Notification History".
  - Search Input: Full-width rounded field (`44dp` height) with search icon and placeholder "Search notifications...".
- **Filter Chips**:
  - Horizontal scrollable row: `All` (selected blue pill), `Allowed`, `Blocked`, `Important`.
- **Grouped Notification Feed**:
  - Section header: "Today", "Yesterday".
  - Notification Item Row:
    - Leading: App Icon (36dp rounded squircle with real app drawable).
    - Title: Bold app name or sender (e.g. "WhatsApp", "YouTube", "Gmail", "Instagram", "bKash", "Facebook").
    - Snippet: Body text preview (e.g. "2 new messages", "New video from MK Shaon", "seniorvibes liked your reel").
    - Trailing: Timestamp ("9:40 PM") + 3-dots action icon OR status badge (`Blocked` in red pill, `Important` in blue/purple pill).
  - Click notification row -> navigates to **Notification Detail Screen**.

#### Screen 3: Blocking Rules (`BlockingRulesScreen.kt`)
- **Header**:
  - Title "Blocking Rules", Trailing: `+` Add Custom Rule button.
- **6 Master Rule Cards**:
  - **Card 1: Block Social Media**
    - Leading icon: Social/users icon with orange/pink background.
    - Title: "Block Social Media", Subtitle: "Instagram, Facebook, TikTok...".
    - Trailing: Switch toggle `ON/OFF`. Click card opens category app selector.
  - **Card 2: Block Selected Apps**
    - Leading icon: Grid/apps icon with green background.
    - Title: "Block Selected Apps", Subtitle: "3 apps selected".
    - Trailing: Switch toggle `ON/OFF`. Click card opens App Picker.
  - **Card 3: Allow Important Keywords**
    - Leading icon: Shield icon with green background.
    - Title: "Allow Important Keywords", Subtitle: "OTP, verification, payment...".
    - Trailing: Switch toggle `ON/OFF`. Click card opens Keyword Whitelist editor.
  - **Card 4: Schedule Blocking**
    - Leading icon: Clock icon with blue background.
    - Title: "Schedule Blocking", Subtitle: "10:00 PM – 7:00 AM".
    - Trailing: Switch toggle `ON/OFF`. Click card opens Schedules list/editor.
  - **Card 5: Block Everything**
    - Leading icon: Minus/cancel icon with orange background.
    - Title: "Block Everything", Subtitle: "Except whitelisted apps".
    - Trailing: Switch toggle `OFF`. Click card opens Whitelisted Apps editor.
  - **Card 6: Custom Rules**
    - Leading icon: Sliders/list icon with purple background.
    - Title: "Custom Rules", Subtitle: "2 custom rules".
    - Trailing: Chevron `>`. Click card opens Custom Rule Builder list.

#### Screen 4: Analytics (`AnalyticsScreen.kt`)
- **Header**:
  - Title "Analytics", Trailing: Time range dropdown chip "Last 7 days ▼" (opens bottom sheet: 1 hour, 5 hours, 24 hours, Today, Yesterday, 7 days, Custom).
- **Hero Analytics Card**:
  - Big metric: `1,248` (32sp Bold), Label: "Total Notifications".
  - **Interactive Bar Chart**: 7 vertical bars (Mon, Tue, Wed, Thu, Fri, Sat, Sun) rendered via Compose Canvas with y-axis indicators (0, 100, 200, 300) and active day highlight.
  - Stat Split Row: `812` Blocked (red) | `326` Allowed (green) | `110` Important (blue/purple).
- **Top Apps Section**:
  - Section title: "Top Apps", Trailing: "See all".
  - App Ranking Rows:
    - Instagram: horizontal pink/red bar, count `320`.
    - Facebook: horizontal blue bar, count `210`.
    - YouTube: horizontal red bar, count `180`.
    - WhatsApp: horizontal green bar, count `120`.
    - Gmail: horizontal blue bar, count `90`.
- **Smart Insights**:
  - Descriptive cards: "68% of notifications came from 3 apps.", "Most notifications arrived between 7 PM and 11 PM."

#### Screen 5: Settings (`SettingsScreen.kt`)
- **Header**:
  - Title "Settings".
- **Appearance Section**:
  - Radio options:
    - `Dark Mode` (Moon icon, radio/toggle).
    - `Light Mode` (Sun icon, radio).
    - `System Default` (Circle icon, radio).
- **General Section**:
  - `Notification Access`: Trailing status badge `Granted` (green) / `Needs attention` (red).
  - `Battery Optimization`: Trailing status badge `Ignore` / `Fix`.
  - `Data & Privacy`: Trailing chevron `>`.
  - `Backup & Restore`: Trailing chevron `>`.
  - `About`: Trailing chevron `>`.
- **Quote Footer**:
  - Centered calm text:
    `"A quieter phone"`  
    `"A calmer mind."`

---

## 5. Granular Phase Breakdown & Execution Task List

```
Phase 0 ──► Phase 1 ──► Phase 2 ──► Phase 3 ──► Phase 4 ──► Phase 5 ──► Phase 6 ──► Phase 7 ──► Phase 8
Cleanup     Data Layer  Listener    Rule Engine Design Sys  Primary UI  Subscreens  Onboarding  Hardening
```

---

### Phase 0: Legacy Cleanup & Foundation Architecture
**Goal:** Strip out deprecated code, delete unauthorized screens, establish clean architecture packages, and prepare project dependencies.

- [x] **Task 0.1: Remove Deprecated UI & Unused Artifacts**
  - Delete `app/src/main/java/com/my/notificationai/ui/screens/VaultScreen.kt` (non-specified "vault" concept).
  - Delete `app/src/main/java/com/my/notificationai/ui/screens/ProfileScreen.kt` (non-specified profile screen).
  - Delete `app/src/main/java/com/my/notificationai/ui/components/AppDrawerContent.kt` (drawer navigation forbidden by spec).
  - Remove unused routes from `Screen.kt`.
- [x] **Task 0.2: Establish Clean Architecture Package Structure**
  - Create `core/designsystem/` (theme, tokens, components, canvas charts).
  - Create `core/database/` (entities, DAOs, Room database).
  - Create `core/datastore/` (preferences, settings, onboarding).
  - Create `core/domain/` (rule engine, deduplication, OTP detector, financial detector).
  - Create `core/service/` (listener service, notification lifecycle tracker).
  - Create `feature/home/`, `feature/history/`, `feature/rules/`, `feature/analytics/`, `feature/settings/`, `feature/onboarding/`.
- [x] **Task 0.3: Dependency & Gradle Setup**
  - Verify Jetpack Compose BOM `2026.02.01`, Kotlin `2.2.10`, Hilt `2.59.2`, Room `2.8.4`.
  - Add `androidx.work:work-runtime-ktx` for automated retention cleanup and schedule evaluation.
  - Add `androidx.compose.material:material-icons-extended` for complete icon availability.

---

### Phase 1: Robust Data Layer & Schema Architecture (Room + DataStore)
**Goal:** Implement the complete relational Room schema (v2) supporting logical events, update histories, multi-condition rules, schedules, and analytics snapshots.

- [x] **Task 1.1: Room Entity Implementation (`Database.kt`)**
  - Implement `NotificationEvent` entity with 32 fields (key, packageName, initial/latest title & text, channel, flags, ongoing, progress, isOtp, otpCode, isFinancial, isPromotional, timestamps, updateCount, wasBlocked, blockReason, ruleId).
  - Implement `NotificationUpdate` entity for continuous update tracking.
  - Implement `BlockingRule` entity for master blocking modes and custom rules.
  - Implement `RuleCondition` entity for multi-condition rule predicates.
  - Implement `WhitelistedApp` and `WhitelistedKeyword` entities.
  - Implement `BlockedApp` entity for Selected Apps mode.
  - Implement `Schedule` entity with days-of-week recurrence and start/end time.
  - Implement `NotificationCategory` entity with preconfigured social media apps.
- [x] **Task 1.2: Comprehensive DAO Implementation (`NotificationDao.kt`, `RuleDao.kt`)**
  - `NotificationDao`:
    - Reactive Flow queries for all events ordered by `last_updated_at DESC`.
    - Multi-filter query supporting search text, `ALL`, `BLOCKED`, `ALLOWED`, and `IMPORTANT` chips.
    - Analytics aggregation queries: total, blocked, allowed, important counts within `[startTime, endTime]`.
    - Top apps grouping query (`SELECT package_name, app_label, COUNT(*), SUM(was_blocked) GROUP BY package_name`).
    - Update history CRUD and cutoff-based retention deletion (`deleteEventsOlderThan(cutoff)`).
  - `RuleDao`:
    - Full CRUD for master rules, conditions, whitelist tables, blocked apps, schedules, and categories.
- [x] **Task 1.3: DataStore Preferences Expansion (`SettingsDataStore.kt`)**
  - Implement reactive preference flows for:
    - `isMasterBlockerEnabled: Flow<Boolean>` (default true)
    - `activeBlockingMode: Flow<String>` (default "SOCIAL_MEDIA")
    - `themePreference: Flow<String>` (default "DARK")
    - `isOtpProtectionEnabled: Flow<Boolean>` (default true)
    - `isFinancialProtectionEnabled: Flow<Boolean>` (default true)
    - `isPromotionalSmsFilterEnabled: Flow<Boolean>` (default true)
    - `isEmergencyBypassEnabled: Flow<Boolean>` (default true)
    - `dataRetentionDays: Flow<Int>` (default 90)
    - `isOnboardingCompleted: Flow<Boolean>` (default false)
    - `quickPauseUntil: Flow<Long>` (default 0L)
- [x] **Task 1.4: AppRepository & Hilt DI Integration**
  - Implement unified `AppRepository` exposing clean domain flows and suspending methods.
  - Provide `AppDatabase`, `NotificationDao`, `RuleDao`, and `SettingsDataStore` in `DiModules.kt`.

---

### Phase 2: Notification Capture Pipeline, Deduplication & Lifecycle Engine
**Goal:** Build the listener service that captures every notification, suppresses blocked notifications, and deduplicates continuous updates into single logical events.

- [x] **Task 2.1: Smart Deduplication Engine (`NotificationDeduplicationEngine.kt`)**
  - Thread-safe active notification key index using `ConcurrentHashMap`.
  - Distinguish between **New Event** vs **Ongoing Update**:
    - If key matches active event and notification has `FLAG_ONGOING_EVENT` or progress:
      - Update existing `NotificationEvent`: increment `updateCount`, update `latestTitle`, `latestText`, `progress`, `lastUpdatedAt`.
      - Record snapshot in `NotificationUpdate` (throttled to max 1 update / 500ms to prevent SQLite contention).
    - If key is new:
      - Create a new `NotificationEvent` with `firstSeenAt = System.currentTimeMillis()`.
- [x] **Task 2.2: Removal & Duration Tracking**
  - Override `onNotificationRemoved(sbn: StatusBarNotification?, reason: Int)`.
  - Calculate active duration (`durationMs = System.currentTimeMillis() - firstSeenAt`).
  - Update `removedAt` in database and close active deduplication session.
- [x] **Task 2.3: Redesign `MyNotificationListenerService.kt`**
  - Unconditionally capture and persist all incoming notifications.
  - Invoke `NotificationRuleEngine.evaluate(sbn)`.
  - If `ruleResult.shouldBlock == true`:
    - Suppress notification from status bar via `cancelNotification(sbn.key)`.
    - Tag record with `wasBlocked = true`, `blockReason = ruleResult.reason`, `responsibleRuleId = ruleResult.ruleId`.
  - If `ruleResult.shouldBlock == false`:
    - Leave notification in status bar.
    - Tag record with `wasBlocked = false`, `blockReason = ruleResult.reason` (e.g. "Allowed by Whitelist").
  - Wrap cancellation in try-catch to handle Android system / OEM protected notifications, recording `blockReason = "Android prevented suppression"`.

---

### Phase 3: Rule Evaluation Engine & Protection Services
**Goal:** Deliver a deterministic multi-tier rule evaluation engine with contextual OTP, financial, and promotional SMS heuristics.

- [x] **Task 3.1: Deterministic Multi-tier Rule Evaluator (`NotificationRuleEngine.kt`)**
  - Sequential evaluation order:
    1. **Emergency & Phone Call Bypass**: Incoming calls, alarms, emergency bypass contacts -> `ALLOW`.
    2. **OTP & Security Protection**: If OTP detector matches -> `ALLOW (Protected OTP)`.
    3. **Financial Protection**: If trusted financial app or transaction alert -> `ALLOW (Protected Financial)`.
    4. **App Whitelist**: If package is in `whitelisted_apps` -> `ALLOW (Whitelisted App)`.
    5. **Keyword Whitelist**: If title/text contains whitelisted keyword -> `ALLOW (Whitelisted Keyword)`.
    6. **Promotional SMS Spam Filter**: If marketing package/discount from SMS app -> `BLOCK (Promotional SMS Filter)`.
    7. **Schedule Evaluation**: If active recurring schedule matches current time and day -> `BLOCK / ALLOW (Schedule)`.
    8. **Master Blocker Mode**:
       - `BLOCK_ALL`: `BLOCK (Global Block All)`.
       - `SOCIAL_MEDIA`: If package is in Social Media category -> `BLOCK (Social Media Block)`.
       - `SELECTED_APPS`: If package is in `blocked_apps` -> `BLOCK (Selected App Block)`.
       - `CUSTOM_RULES`: Evaluate custom conditions (AND/OR tree).
    9. **Default Fallback**: `ALLOW (Default Policy)`.
- [x] **Task 3.2: Contextual Multi-Signal OTP Detector (`OtpDetector.kt`)**
  - Signal 1: Context keywords (`otp`, `verification code`, `security code`, `one-time password`).
  - Signal 2: Token boundaries (`\b\d{4,8}\b` or `\b[A-Z0-9]{5,8}\b`).
  - Anti-Signal (Negative filters): Reject if text contains marketing bundles ("GB for", "recharge offer", "validity", "dial *", "cashback offer").
- [x] **Task 3.3: Financial Alert Detector (`FinancialDetector.kt`)**
  - Pre-configure trusted Bangladeshi and international financial packages (`bKash`, `Nagad`, `Rocket`, `Upay`, `Cellfin`, `Citytouch`, `EBL Skybanking`, etc.).
  - Content keywords: "received", "sent", "transferred", "credited", "debited", "balance", "txn", "statement", "payment confirmed".
- [x] **Task 3.4: Promotional SMS Spam Detector (`PromotionalFilter.kt`)**
  - Detect telecom bundle spam: `\b\d+\s*(gb|mb|min|sms)\b`, `recharge offer`, `dial *121*`.
  - Ensure legitimate personal and banking SMS are never classified as promotional spam.

---

### Phase 4: Design System, Tokens, Typography & Reusable Components
**Goal:** Implement the exact design language from `My_Notification_Design_System.md` with complete Dark & Light mode parity.

- [x] **Task 4.1: Color Tokens & Semantic Palettes (`ThemeColorTokens.kt`)**
  - Dark Mode tokens: Background `#080D14`, Surface `#101720`, Elevated `#151F2B`, Border `#263241`, Primary `#4F6BFF`.
  - Light Mode tokens: Background `#F7F9FC`, Surface `#FFFFFF`, Surface Soft `#F1F4F8`, Border `#E4E9F0`, Text `#111827`.
  - Semantic tokens: Success `#2CCB82`, Error `#F05B67`, Security `#8B6CFF`, Warning `#F5B84B`.
- [x] **Task 4.2: Typography System (`Type.kt`)**
  - Inter font family with fallback to Roboto.
  - Scales: Display Large 32sp Bold, Headline Medium 20sp SemiBold, Title Medium 16sp SemiBold, Body Medium 14sp Regular, Label Medium 12sp Medium.
- [x] **Task 4.3: Shape & Elevation Standards**
  - 4dp grid spacing (`4dp`, `8dp`, `12dp`, `16dp`, `20dp`, `24dp`).
  - Corner Radii: Cards `16dp`, Hero Card `20dp`, Buttons `12dp`, Chips `999dp` (pill).
  - Elevation: Subtle `0-2dp` with borders preferred over heavy shadows.
- [x] **Task 4.4: Reusable Component Library**
  - `AppTopBar`: Compact header with title, subtitle, and Settings gear icon ⚙.
  - `AppBottomNavBar`: 4-tab bar (`Home`, `History`, `Rules`, `Analytics`).
  - `HeroRingCard`: Large circular glowing ring with bell icon, status title, and pill toggle button.
  - `MetricCard`: Compact equal-width metric box (count + label).
  - `FeatureNavCard`: Chevron list card for navigating to History, Rules, Analytics.
  - `NotificationRow`: App icon, Title, text snippet, timestamp, 3-dots, and colored status badge (`Blocked`, `Allowed`, `Important`).
  - `StatusChip` & `FilterChip`: Pill chips for filtering and status tags.
  - `SearchBar`: 44dp height rounded search field with clear action.
  - `AppSwitch`: Compact Android-standard toggle with primary blue active track.
  - `BarChart`: Custom Compose Canvas drawing daily/hourly vertical bars with subtle grid, date labels, and active highlight.
  - `AppRankingRow`: App icon, app label, colored horizontal proportion bar, and notification count.

---

### Phase 5: Primary Screen Implementation (10-Screen Visual Parity)
**Goal:** Build the 5 core screens in both Dark and Light modes exactly matching the user's reference image.

- [x] **Task 5.1: Screen 1 — Home / Dashboard (`HomeScreen.kt`)**
  - Top Bar: "My Notification", Subtitle: "Less noise. More you.", Settings gear icon ⚙.
  - Hero Ring Card: Glowing circular ring with bell icon, "Focused Mode" / "Active", and "Tap to disable" pill button.
  - 3 Summary Metrics: `128 Blocked`, `12 Allowed`, `86 Important/Saved`.
  - 3 Feature Cards: "Notification History", "Blocking Rules", "Analytics".
  - Scaffold with 4-tab Bottom Navigation.
- [x] **Task 5.2: Screen 2 — Notification History (`HistoryScreen.kt`)**
  - Rounded search bar: "Search notifications...".
  - Horizontal filter chips: `All` (selected), `Allowed`, `Blocked`, `Important`.
  - Date-grouped feed ("Today", "Yesterday").
  - Notification items with app icons, timestamps, status badges (`Blocked` in red, `Important` in blue/purple).
  - Tap notification row opens **Notification Detail Screen**.
- [x] **Task 5.3: Screen 3 — Blocking Rules (`BlockingRulesScreen.kt`)**
  - Top Bar: Title "Blocking Rules", `+` Add Rule action button.
  - 6 Master Rule Cards:
    - Card 1: `Block Social Media` (Instagram, Facebook, TikTok...) + Switch.
    - Card 2: `Block Selected Apps` (3 apps selected) + Switch.
    - Card 3: `Allow Important Keywords` (OTP, verification, payment...) + Switch.
    - Card 4: `Schedule Blocking` (10:00 PM – 7:00 AM) + Switch.
    - Card 5: `Block Everything` (Except whitelisted apps) + Switch.
    - Card 6: `Custom Rules` (2 custom rules) + Chevron `>`.
  - Tap card opens editor for that rule.
- [x] **Task 5.4: Screen 4 — Analytics (`AnalyticsScreen.kt`)**
  - Top Bar: Title "Analytics", Time range selector chip "Last 7 days ▼".
  - Hero Metric Card: `1,248 Total Notifications`, Compose Canvas Bar Chart (Mon-Sun), Breakdown: `812 Blocked | 326 Allowed | 110 Important`.
  - Top Apps section: Ranked rows with horizontal proportion bars (Instagram 320, Facebook 210, YouTube 180, WhatsApp 120, Gmail 90).
  - Smart Insights cards: "68% of notifications came from 3 apps."
- [x] **Task 5.5: Screen 5 — Settings (`SettingsScreen.kt`)**
  - Top Bar: Title "Settings".
  - Appearance section: Radio rows for `Dark Mode`, `Light Mode`, `System Default`.
  - General section:
    - `Notification Access` with `Granted` (green) badge.
    - `Battery Optimization` with `Ignore` badge.
    - `Data & Privacy` → Navigates to Data & Privacy.
    - `Backup & Restore` → Navigates to Backup & Restore.
    - `About` → Navigates to About.
  - Footer quote: `"A quieter phone" / "A calmer mind."`.

---

### Phase 6: Sub-Screens, Management Workflows & Custom Rule Builder
**Goal:** Build all secondary configuration, detail, and management screens required by the product specification.

- [x] **Task 6.1: Notification Detail Screen (`NotificationDetailScreen.kt`)**
  - App header with icon, app label, timestamp, and status badge.
  - Content Card: Title, Body text, Big text, Subtext.
  - Rule Explanation Card: Detailed reason why notification was blocked or allowed (e.g. `Blocked by: Block Social Media → Instagram` or `Allowed by: Whitelisted Keyword → OTP`).
  - Technical Metadata Card: Notification Key, Channel ID/Name, Category, Importance, Flags.
  - Lifecycle Card: First seen, Last updated, Removed at, Total duration, Update count.
- [x] **Task 6.2: Custom Rule Builder (`RuleBuilderScreen.kt`)**
  - Visual sentence builder: `WHEN [App = X] AND [Text contains "Y"] AND [Time = Z] THEN [BLOCK / ALLOW]`.
  - Rule simulation preview: "This rule would have affected 16 notifications today".
- [x] **Task 6.3: Schedules Manager & Schedule Editor (`ScheduleScreen.kt`, `ScheduleEditorScreen.kt`)**
  - List of schedules with individual enable/disable toggles.
  - Schedule Editor: Name, Start/End time pickers, Weekdays selector chips, Action (Block All, Block Social, Custom), Exceptions (OTP, Financial, Calls).
- [x] **Task 6.4: Protection Center Screen (`ProtectionCenterScreen.kt`)**
  - OTP Protection manager (enable/disable, keywords list, add keyword).
  - Financial Notifications manager (enable/disable, trusted apps checklist).
  - Promotional SMS Filter manager (enable/disable, sample rules).
  - Emergency Bypass manager (emergency apps / contacts).
- [x] **Task 6.5: App Picker Screen (`AppPickerScreen.kt`)**
  - Searchable list of installed apps with real app icons, labels, package names, and multi-select checkboxes.
- [x] **Task 6.6: Whitelisted Apps & Whitelisted Keywords Screens**
  - `WhitelistedAppsScreen.kt`: List of whitelisted apps with "Add App" button and delete action.
  - `WhitelistedKeywordsScreen.kt`: Chip list of keywords with "Add Keyword" button and usage counts.
- [x] **Task 6.7: Notification Categories Screen (`CategoriesScreen.kt`)**
  - Categorization of apps: Social Media, Financial, Messaging, Work, Shopping, Other.
  - User ability to add/remove apps from categories.
- [x] **Task 6.8: Rule Priority Visualizer (`RulePriorityScreen.kt`)**
  - Visual ordered list showing evaluation hierarchy from Emergency to Default policy.
- [x] **Task 6.9: Help, Tutorial & Problem Reporting**
  - `HelpScreen.kt`: FAQ on notification access, OEM battery restrictions, Android suppression realities.
  - `TutorialScreen.kt`: Walkthrough of core features.
  - `ReportProblemScreen.kt`: Problem description form with auto-detected Android OS and device model.

---

### Phase 7: Onboarding Flow, Data Retention & Backup/Restore
**Goal:** Implement the first-run onboarding experience, automated storage pruning via WorkManager, and local encrypted backup.

- [x] **Task 7.1: Multi-Step Onboarding Wizard (`OnboardingScreen.kt`)**
  - **Step 1: Introduction**: "Less noise. More control." -> CTA: "Get Started".
  - **Step 2: Notification Access**: Clear explanation why access is needed -> CTA: "Enable Notification Access" (deep-links to system settings).
  - **Step 3: Default Strategy**: Radio choices: "Block Social Media", "Selected Apps", "Block All", "Allow All".
  - **Step 4: Important Apps Selection**: Multi-select picker with defaults (WhatsApp, Phone, Gmail, bKash).
  - **Step 5: Protection Verification**: Quick switches: OTP Protection ON, Financial Protection ON.
  - **Step 6: Optional Schedule**: Quick Sleep mode prompt or "Skip for now".
  - **Step 7: Finish**: Sets `isOnboardingCompleted = true` and navigates to Home.
- [x] **Task 7.2: Data Retention Engine (`RetentionManager` + `WorkManager`)**
  - Configurable retention in Settings: 7 days, 30 days, 90 days, 180 days, 1 year, Forever.
  - Background `PeriodicWorkRequest` (running once daily) to purge events and updates older than selected retention.
  - Storage stats UI: displays total events count and approximate database file size in MB.
  - "Delete all notification history" button with double confirmation dialog.
- [x] **Task 7.3: Local Backup & Restore (`BackupManager`)**
  - Export rules, schedules, whitelists, and history to structured JSON format.
  - Import / Restore backup file with schema validation.

---

### Phase 8: Production Hardening, Battery Optimization, Edge Cases & Verification
**Goal:** Comprehensive testing across Android versions, OEM background optimization, and quality assurance.

- [x] **Task 8.1: Battery Optimization & OEM Background Reliability**
  - Request ignore battery optimizations (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) if needed.
  - Guide users on OEM-specific killers (Xiaomi MIUI/HyperOS autostart, Samsung sleeping apps, OnePlus battery restrictions) in Settings.
- [x] **Task 8.2: Android 14/15/16 Platform Compatibility**
  - Respect `POST_NOTIFICATIONS` permission (Android 13+).
  - Handle full-screen intent limitations gracefully without claiming impossible 100% blocking.
  - Provide accurate in-app explanations if Android prevents complete suppression.
- [x] **Task 8.3: Unit & Component Testing**
  - `OtpDetectorTest`: Benchmark against positive OTPs and negative marketing text with digits.
  - `NotificationRuleEngineTest`: Verify deterministic evaluation hierarchy across conflicts.
  - `DeduplicationEngineTest`: Verify download progress and call notifications result in 1 logical event.
  - `AnalyticsCalculationTest`: Verify accurate counts and no inflated statistics.
- [x] **Task 8.4: End-to-End Flow Verification & Design QA Checklist**
  - Verify layout insets, status bar padding, 48dp minimum touch targets, TalkBack semantics.
  - Complete Dark Mode and Light Mode visual check against reference screenshot.

---

## 6. Complete Screen Navigation Route Mapping

| Route | Screen Name | Screen Category | Visual Reference Position |
|---|---|---|---|
| `home` | **Home / Dashboard** | Primary Tab | Screen 1 (Dark & Light) |
| `history` | **Notification History** | Primary Tab | Screen 2 (Dark & Light) |
| `rules` | **Blocking Rules** | Primary Tab | Screen 3 (Dark & Light) |
| `analytics` | **Analytics** | Primary Tab | Screen 4 (Dark & Light) |
| `settings` | **Settings** | Top-Level Screen (Gear ⚙) | Screen 5 (Dark & Light) |
| `history/detail/{eventId}` | **Notification Detail** | History Expansion | Deep Sub-screen |
| `rules/builder` | **Custom Rule Builder** | Rules Expansion | Deep Sub-screen |
| `rules/schedules` | **Schedules List & Editor** | Rules Expansion | Deep Sub-screen |
| `rules/category/social` | **Social Media Apps Picker** | Rules Expansion | Deep Sub-screen |
| `rules/selected-apps` | **Selected Apps Picker** | Rules Expansion | Deep Sub-screen |
| `rules/whitelist/apps` | **Whitelisted Apps** | Rules Expansion | Deep Sub-screen |
| `rules/whitelist/keywords` | **Whitelisted Keywords** | Rules Expansion | Deep Sub-screen |
| `rules/priority` | **Rule Priority Visualizer** | Rules Expansion | Deep Sub-screen |
| `settings/protection` | **Protection Center (OTP/Financial)** | Settings Expansion | Deep Sub-screen |
| `settings/privacy` | **Data & Privacy / Retention** | Settings Expansion | Deep Sub-screen |
| `settings/backup` | **Backup & Restore** | Settings Expansion | Deep Sub-screen |
| `settings/about` | **About** | Settings Expansion | Deep Sub-screen |
| `onboarding` | **Onboarding Wizard** | First-Run Flow | Fullscreen Modal |
| `support/help` | **Help & FAQ** | Support | Deep Sub-screen |
| `support/tutorial` | **Tutorial Walkthrough** | Support | Deep Sub-screen |
| `support/report` | **Report a Problem** | Support | Deep Sub-screen |

---

## 7. Execution Protocol

This specification document is the **single source of truth** for all subsequent implementation work.
- When development commences, tasks must be executed phase-by-phase in order (`Phase 0` through `Phase 8`).
- After completing each task milestone, the code must be built, tested, committed, and pushed to `origin/main`.
