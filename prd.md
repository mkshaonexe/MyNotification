# MyNotification — Product Requirements Document (PRD)
### Version 1.0 | Native Android App | Privacy-First | No Backend Server

---

## Table of Contents

1. [Product Overview](#1-product-overview)
2. [Vision & Goals](#2-vision--goals)
3. [Target Users](#3-target-users)
4. [Technical Stack & Architecture](#4-technical-stack--architecture)
5. [Android Permissions Required](#5-android-permissions-required)
6. [Complete Feature Specification](#6-complete-feature-specification)
7. [Phase Roadmap](#7-phase-roadmap)
8. [TODO & Task List — Per Phase](#8-todo--task-list--per-phase)
9. [Database Schema](#9-database-schema)
10. [UI/UX Screen List](#10-uiux-screen-list)
11. [Non-Functional Requirements](#11-non-functional-requirements)

---

## 1. Product Overview

**App Name:** MyNotification  
**Platform:** Android (Native, Kotlin)  
**Minimum SDK:** API 26 (Android 8.0 Oreo)  
**Target SDK:** API 35 (Android 15)  
**Backend:** None — 100% local, offline-first  
**Privacy:** All notification data stored on-device only. No analytics, no tracking, no cloud sync.

**One-Line Description:**  
A powerful, privacy-first Android notification management system that lets users block, save, filter, schedule, and intelligently control every notification on their device.

---

## 2. Vision & Goals

### Vision
To be the most complete notification control system for Android — going beyond simple "Do Not Disturb" by giving users full power over when, how, and which notifications reach them.

### Primary Goals
- G1: Allow users to block notifications fully or selectively
- G2: Save blocked notifications in a private inbox for later review
- G3: Let users schedule focus modes with custom rules
- G4: Give per-app and per-channel granular control
- G5: Work 100% offline with no data leaving the device
- G6: Remain lightweight, battery-efficient, and fast

### Success Metrics (for personal tracking)
- Notifications blocked per day (shown in dashboard)
- Focus sessions completed
- Distraction-free hours logged
- Notification inbox review rate

---

## 3. Target Users

| User Type | Need |
|-----------|------|
| Students | Block distractions during study/exams |
| Professionals | Separate work from personal notifications |
| Privacy-conscious users | Keep notification data off the cloud |
| Productivity enthusiasts | Deep focus modes, streaks, analytics |
| General Android users | Simple "block everything" control |

---

## 4. Technical Stack & Architecture

### Language & Tools
- **Language:** Kotlin 100%
- **UI:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM + Clean Architecture
- **DI:** Hilt (Dagger)
- **Database:** Room (SQLite)
- **Background:** WorkManager + Foreground Service
- **Navigation:** Navigation Compose
- **Preferences:** DataStore (Proto)
- **Scheduling:** AlarmManager + WorkManager

### Core Android Services Used
| Service | Purpose |
|---------|---------|
| `NotificationListenerService` | Listen, intercept, and cancel notifications |
| `NotificationManager` | Manage Do Not Disturb / interruption filter |
| `AutomaticZenRule` | Schedule DND rules (Android 10+) |
| `NotificationChannel` | Per-channel blocking control |
| `AccessibilityService` | Detect app launch (for cooldown feature) |
| `BroadcastReceiver` | Boot-completed, schedule triggers |
| `AlarmManager` | Exact alarm scheduling (with permission) |
| `UsageStatsManager` | App usage data for analytics |

### Architecture Layers
```
UI Layer (Compose Screens)
    ↓
ViewModel Layer (StateFlow + UiState)
    ↓
Domain Layer (UseCases)
    ↓
Data Layer (Repository → Room DB + DataStore)
    ↓
Service Layer (NotificationListenerService + WorkManager)
```

---

## 5. Android Permissions Required

| Permission | Reason | Critical? |
|-----------|---------|-----------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Core: read & dismiss notifications | YES |
| `ACCESS_NOTIFICATION_POLICY` | Modify DND / interruption filter | YES |
| `SCHEDULE_EXACT_ALARM` | Exact scheduling for focus modes | YES (Android 12+) |
| `USE_EXACT_ALARM` | Exact alarms (Android 13+ fallback) | YES |
| `RECEIVE_BOOT_COMPLETED` | Restore schedules after reboot | YES |
| `FOREGROUND_SERVICE` | Keep listener alive | YES |
| `FOREGROUND_SERVICE_SPECIAL_USE` | For Android 14+ foreground service | YES |
| `PACKAGE_USAGE_STATS` | App usage stats for analytics | Optional |
| `BIND_ACCESSIBILITY_SERVICE` | Detect app opens (cooldown feature) | Optional |
| `POST_NOTIFICATIONS` | Post own app notifications | YES (Android 13+) |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Prevent service being killed | Recommended |
| `VIBRATE` | Custom notification behavior | YES |

---

## 6. Complete Feature Specification

---

### MODULE 1: NOTIFICATION CONTROL (Core)

#### F1.1 — Block All Notifications
- One-tap master toggle to block ALL notifications from ALL apps
- Persistent across device restarts
- Emergency bypass still works (whitelist)
- Visual indicator in status bar (optional)

#### F1.2 — Block Selected Apps
- List all installed apps with toggle per app
- Search bar to find apps quickly
- Sort by: name, recently used, most notifications
- Show app notification count next to each app

#### F1.3 — Allow Only Selected Apps (Allowlist Mode)
- Invert mode: block everything EXCEPT chosen apps
- Quick preset: "Only allow Calls + OTP + Banking"
- Easy toggle between blocklist and allowlist mode

#### F1.4 — Per-Channel Control
- Expand any app to see all its notification channels
- Toggle each channel individually
- Label channels: Spam, Important, Updates, Promo, etc.
- Android-native channel importance settings integration

#### F1.5 — Smart Whitelist
- Auto-detect and protect: Phone calls, SMS, Banking OTPs, Alarms
- User can add custom apps to whitelist
- Whitelist always overrides block rules
- Priority levels: Critical > High > Normal

#### F1.6 — Quick Block / Quick Allow
- Long-press an incoming notification → "Block this app / Block this channel"
- Integrate with notification action buttons if possible via NLS

---

### MODULE 2: NOTIFICATION VAULT (Save & Review)

#### F2.1 — Notification Vault / Inbox
- All blocked notifications saved in a private local inbox
- Grouped by: App name, Date, Priority category
- Each saved notification shows: App icon, Title, Body text, Timestamp, Channel name
- Mark as Read / Unread
- Delete individual or bulk delete
- Long-press to copy text

#### F2.2 — Notification Search
- Full-text search across all saved notifications
- Filter by: App, Date range, Category, Read/Unread status
- Sort by: Newest, Oldest, App name

#### F2.3 — Notification Categories
- Auto-tag saved notifications: Promo, Social, Update, OTP, News, Work, Entertainment
- Manual re-tagging by user
- Filter inbox by category

#### F2.4 — Notification Digest / Batching
- Instead of showing one-by-one, batch notifications
- Deliver summary every: 15 min / 30 min / 1 hour / 2 hours / custom
- Digest notification groups all pending alerts as a single summary
- Tapping digest opens vault inbox

#### F2.5 — Notification Pinning
- Pin important notifications in vault so they don't get buried
- Pinned items shown at top of inbox
- Max 20 pinned items

#### F2.6 — Auto-Clear Rules
- Auto-delete saved notifications after: 1 day / 3 days / 7 days / 30 days / never
- Category-specific auto-clear (e.g., Promos deleted after 1 day)

---

### MODULE 3: FOCUS MODES

#### F3.1 — Pre-Built Focus Modes

| Mode | Default Rules |
|------|--------------|
| **Study** | Block all except OTP, Calls, Alarms |
| **Sleep** | Block all except Alarm, Emergency calls |
| **Work** | Block Social, Entertainment; allow Work apps |
| **Prayer / Salah** | Block everything for set duration (e.g. 20 min) |
| **Exam** | Ultra-strict: block all except alarms, OTP |
| **Gaming** | Block all social; allow game-related notifications |
| **Commute** | Allow navigation, ride apps; block social |
| **Gym / Workout** | Block everything except music controls |
| **Meeting** | Block entertainment; allow calendar, calls |
| **Custom** | User-defined rules |

#### F3.2 — Custom Focus Mode Builder
- Name the mode
- Choose icon + color
- Set allowed apps list
- Set blocked apps list
- Set keywords to block
- Set duration (fixed or open-ended)
- Set schedule (automatic trigger)

#### F3.3 — Quick Focus Activation
- Home screen widget for one-tap focus mode
- Quick tile in notification shade
- From app: tap → pick mode → start

#### F3.4 — Auto-End & Reminders
- Focus mode auto-ends after set duration
- 5-minute warning before focus mode ends
- Option to extend focus mode
- Summary shown after focus mode ends: notifications blocked count, duration

---

### MODULE 4: SCHEDULING

#### F4.1 — Time-Based Schedule
- Set daily schedule: "Block from 10 PM to 7 AM"
- Weekday vs Weekend different rules
- Holiday override option
- Multiple schedules can be active simultaneously

#### F4.2 — Per-App Schedule
- App-specific quiet hours
- Example: Instagram blocked 8 AM – 6 PM; WhatsApp always allowed
- Per-app schedule shown in app detail screen

#### F4.3 — Focus Mode Auto-Trigger
- Trigger focus mode by time + day
- Example: "Study Mode every weekday 6 PM – 10 PM"
- Auto-start / auto-end with WorkManager + AlarmManager

#### F4.4 — Schedule Conflict Resolution
- If two schedules overlap, higher-priority one wins
- User can see and resolve conflicts in schedule screen
- Visual calendar view of all active schedules

#### F4.5 — Boot-Recovery
- All active schedules restored on device restart via BOOT_COMPLETED receiver
- Current mode state saved to DataStore (persistent)

---

### MODULE 5: SMART FILTERING

#### F5.1 — Keyword Filter
- Block notifications containing specific words/phrases
- Case-insensitive matching
- Wildcards: "sale*", "*promo*"
- Sample blocklist: "sale", "offer", "live", "reel", "new video", "urgent", "don't miss", "limited time"
- Global keyword rules + per-app keyword rules

#### F5.2 — Smart Priority Engine
- User marks apps as: Critical / High / Normal / Low / Silent
- Critical: always shown, never blocked
- High: shown even in focus mode (with brief popup)
- Normal: standard blocking rules apply
- Low: always save to vault without notification
- Silent: receive but show 0 UI

#### F5.3 — Local Notification Classifier
- Classify saved notifications using local rule-based logic (NO cloud AI)
- Categories: Urgent, Social, Promo, Update, OTP, Entertainment, News, Work
- Classification based on: keywords, app name, notification channel name
- User can override classification

#### F5.4 — OTP Auto-Detect & Extract
- Detect notifications containing OTPs (6-digit codes)
- Store in separate OTP section of vault
- Show OTP with large, easy-to-read text
- Auto-delete OTPs after 10 minutes
- Option to copy OTP from vault

#### F5.5 — Duplicate Filter
- Detect and collapse duplicate notifications (same app, same title/body)
- Show count instead of repeating: "3 duplicate notifications from Instagram"

---

### MODULE 6: ANTI-DISTRACTION TOOLS

#### F6.1 — Social Media Shield Preset
- One-tap to block: Instagram, Facebook, YouTube, TikTok, Twitter/X, Snapchat, Pinterest, Reddit, LinkedIn (personal use)
- Smart detection of social apps already installed
- User can customize what counts as "social media"

#### F6.2 — App-Open Cooldown
- After opening a distracting app, lock it from notifications for X minutes
- Uses AccessibilityService to detect app launch
- Cooldown durations: 5 / 10 / 15 / 30 minutes
- Per-app cooldown settings

#### F6.3 — Anti-Distraction Streaks
- Track consecutive hours/days of focus
- Show streak counter on home screen
- Celebrate milestones: "3-day streak!", "100 notifications blocked!"
- Weekly/monthly summary

#### F6.4 — Notification Limit Per App
- Set max X notifications allowed per hour per app
- Beyond limit → save to vault automatically
- Example: "WhatsApp max 10 per hour"

#### F6.5 — Calm Mode
- Enable to reduce all visual noise: no badges, no sounds, no banners — only vault storage
- Still lets critical through (whitelist)

---

### MODULE 7: EMERGENCY & BYPASS

#### F7.1 — Emergency Bypass
- Designated contacts can always reach through
- Trusted app list that always bypasses block
- "Call me 3 times to break through" rule (repeating caller bypass)

#### F7.2 — PIN Bypass
- Set a PIN to temporarily disable all blocks
- Auto-resume blocking after bypass duration
- 4-digit or 6-digit PIN

#### F7.3 — Quick Pause
- Pause all blocking rules for: 15 min / 30 min / 1 hour / until tomorrow
- Visible countdown timer in notification shade
- One tap to cancel pause early

#### F7.4 — Panic Mode Override
- If device owner is in emergency, voice command or shake gesture to disable all blocks immediately
- Confirmation required to prevent accidental trigger

---

### MODULE 8: STATISTICS & ANALYTICS

#### F8.1 — Main Dashboard
- Total notifications blocked today / this week / this month
- Most-blocked apps (bar chart)
- Focus mode usage time
- Active streaks
- Vault inbox status (unread count)

#### F8.2 — Notification History Log
- Full log of every notification intercepted
- Columns: App, Title, Time, Action (Blocked / Saved / Allowed / Digested)
- Searchable, filterable, exportable
- Per-app breakdown

#### F8.3 — Blocking Timeline
- Heatmap showing notification activity by hour of day
- See when you get spammed the most
- Compare blocked vs received over time

#### F8.4 — Weekly & Monthly Reports
- Auto-generated summary saved locally
- App-by-app breakdown
- Focus sessions list
- Can be exported as text or shared

#### F8.5 — App Notification Score
- Score each app: Spammy / Normal / Respectful
- Based on frequency, duplicate rate, time of sending
- Shown in app list to help users decide what to block

---

### MODULE 9: SETTINGS & CUSTOMIZATION

#### F9.1 — App Appearance
- Light / Dark / System default theme
- Material You dynamic color (Android 12+)
- Custom accent color picker

#### F9.2 — Notification Behavior Settings
- Notification sound for digest delivery
- Vibration patterns for priority levels
- LED blink settings (where supported)

#### F9.3 — Service Health
- Show if NotificationListenerService is running
- Show if scheduling service is active
- Battery optimization warning if service might be killed
- One-tap to re-enable if service stopped

#### F9.4 — Data Management
- Export all notification vault data as JSON
- Export settings as JSON backup
- Import settings from backup
- Clear all vault data
- Clear specific date range
- Storage usage report

#### F9.5 — Language Support
- English (default)
- Bengali / Bangla
- Other languages via translation strings

#### F9.6 — Accessibility
- Large text support
- High contrast mode
- TalkBack-compatible UI labels
- Minimum touch target sizes

---

### MODULE 10: WIDGETS & QUICK CONTROLS

#### F10.1 — Home Screen Widget (Small — 2x1)
- Shows current mode (Active/Off)
- One-tap toggle
- Blocked count today

#### F10.2 — Home Screen Widget (Medium — 4x2)
- Mode selector with 4 quick modes
- Vault unread count
- Quick pause button

#### F10.3 — Quick Settings Tile
- Notification shade tile: "MyNotification ON/OFF"
- Long-press to open app

#### F10.4 — Lock Screen Widget (Android 13+)
- Show current focus mode on lock screen
- Blocked count display

---

## 7. Phase Roadmap

---

### PHASE 1 — MVP (Core Functionality) 🎯
**Goal:** Usable app that blocks and saves notifications  
**Timeline Estimate:** 3–4 weeks

**In Scope:**
- Project setup, architecture, DI (Hilt), Room, Navigation
- NotificationListenerService setup + permission flow
- Block All toggle
- Block selected apps (per-app toggle list)
- Notification Vault (save blocked notifications locally)
- Basic Vault Inbox screen (list, read/unread, delete)
- Smart Whitelist (calls, OTP, alarms — hardcoded defaults)
- Quick Pause (15 min / 1 hour)
- Service health screen (check if listener is running)
- Basic Settings (light/dark theme)

**MVP Completion = App is functional and usable**

---

### PHASE 2 — Focus Modes & Scheduling 📅
**Goal:** Smart scheduling and preset focus modes  
**Timeline Estimate:** 2–3 weeks

**In Scope:**
- 6 pre-built Focus Modes (Study, Sleep, Work, Prayer, Exam, Gaming)
- Custom Focus Mode builder
- Time-based daily schedule (block from X to Y)
- Per-app quiet hours
- Schedule list screen (view/edit/delete)
- Boot-completed receiver for schedule recovery
- Focus mode widget (2x1 home screen)
- Quick Settings tile
- Auto-end focus mode with summary

---

### PHASE 3 — Smart Filtering 🔍
**Goal:** Keyword and channel-level intelligence  
**Timeline Estimate:** 2–3 weeks

**In Scope:**
- Keyword filter (global + per-app)
- Per-channel control (expand app → see channels → toggle each)
- Smart Priority Engine (Critical / High / Normal / Low / Silent)
- Notification Digest / Batching (every 30 min / 1 hour)
- Duplicate notification filter
- OTP auto-detect & extract (separate OTP vault section)
- Category auto-tagging (local rule-based classifier)
- Notification search in vault (full-text search)

---

### PHASE 4 — Anti-Distraction & Social Shield 🛡️
**Goal:** Deep focus tools and distraction prevention  
**Timeline Estimate:** 2 weeks

**In Scope:**
- Social Media Shield preset
- Notification limit per app (max N per hour)
- Anti-distraction streaks (daily/weekly)
- App-open cooldown (AccessibilityService)
- Calm Mode
- Emergency bypass (trusted contacts)
- PIN bypass for temporary disable
- Repeating caller bypass rule

---

### PHASE 5 — Statistics & Analytics 📊
**Goal:** Give users insights on their notification habits  
**Timeline Estimate:** 2 weeks

**In Scope:**
- Main dashboard with charts (Recharts-equivalent using MPAndroidChart)
- Blocking timeline heatmap
- Per-app notification frequency score
- Notification history log (full searchable log)
- Weekly / monthly report generation
- App notification "spam score"
- Focus session history list
- Streak analytics and milestones

---

### PHASE 6 — Advanced Features & Polish ✨
**Goal:** Premium features and app completeness  
**Timeline Estimate:** 3 weeks

**In Scope:**
- Notification pinning in vault
- Auto-clear rules (time-based vault cleanup)
- Schedule conflict detection & resolution
- Full schedule calendar view (visual timeline)
- Commute / Gym / Meeting focus modes
- Notification batching digest improvements
- Export vault data as JSON / shareable text
- Import/export app settings backup
- Notification sound & vibration customization
- Material You dynamic color (Android 12+)
- Home screen widget (4x2)
- Lock screen widget (Android 13+)
- Bengali language support
- Accessibility improvements (TalkBack)
- Panic mode override (shake gesture)
- Storage usage screen
- Full onboarding flow (first launch walkthrough)

---

### PHASE 7 — Power User & Automation Features ⚡ (Future)
**Goal:** Advanced automation and power-user controls  
**Timeline Estimate:** 3–4 weeks

**In Scope:**
- Tasker / Automate integration (via broadcast intents)
- Notification rules with conditions (IF app = X AND time = Y THEN block)
- Rule priority and ordering system
- Custom notification sound per app
- Snooze notification (remind me in 1 hour)
- Notification tags and labels (user-created)
- Markdown/rich text notes attachable to saved notifications
- Share notification content to other apps from vault
- Notification archiving (long-term storage with compression)
- Advanced keyword rules with regex support
- Biometric lock for vault (fingerprint / face unlock)
- App lock integration for vault screen

---

## 8. TODO & Task List — Per Phase

---

### PHASE 1 TASKS (MVP)

#### Setup & Architecture
- [ ] Create Android project with Kotlin + Jetpack Compose
- [ ] Add Gradle dependencies: Hilt, Room, Navigation Compose, DataStore, WorkManager
- [ ] Set up Hilt application class + module setup
- [ ] Set up Room database with initial entities
- [ ] Set up DataStore for app settings
- [ ] Set up Navigation Compose with NavHost and routes
- [ ] Create base theme (Light + Dark) with Material 3
- [ ] Create app logo and launcher icon

#### Permission & Service Layer
- [ ] Create `NotificationListenerService` subclass
- [ ] Handle `onNotificationPosted()` callback
- [ ] Handle `onNotificationRemoved()` callback
- [ ] Create Permission Check screen (NotificationListenerService binding)
- [ ] Create Permission Request screen (POST_NOTIFICATIONS for Android 13+)
- [ ] Create SCHEDULE_EXACT_ALARM permission request flow
- [ ] Create BATTERY_OPTIMIZATION ignore request
- [ ] Create Foreground Service for persistent listener (Android 14+)
- [ ] Create BOOT_COMPLETED BroadcastReceiver

#### Core Blocking Logic
- [ ] Create `BlockingEngine` class (decides: block / save / allow)
- [ ] Implement "Block All" toggle in BlockingEngine
- [ ] Implement per-app block rule lookup
- [ ] Implement whitelist check (calls, OTP, alarms)
- [ ] Save BlockingRule to Room database
- [ ] Load rules on service start from Room
- [ ] Cancel notification via `cancelNotification()` in NLS

#### Notification Vault
- [ ] Create `SavedNotification` Room entity
- [ ] Create `NotificationDao` with CRUD operations
- [ ] Save blocked notification: app name, icon, title, body, timestamp, channel, package
- [ ] App icon fetching utility (from PackageManager)
- [ ] Create Vault Inbox screen (LazyColumn of grouped notifications)
- [ ] Mark as Read / Unread functionality
- [ ] Delete single notification
- [ ] Delete all notifications (with confirmation dialog)
- [ ] Empty state UI for vault

#### App List & Rules Screen
- [ ] Create `InstalledApp` data class
- [ ] Create utility to fetch all installed apps with icons
- [ ] Filter system apps (optional toggle)
- [ ] App list screen with search bar
- [ ] Per-app toggle (block / allow)
- [ ] Show notification count per app
- [ ] Sort options (name / count)

#### Quick Controls
- [ ] Quick Pause button (bottom sheet: 15 min / 1 hr / until tomorrow)
- [ ] Store pause state in DataStore
- [ ] Countdown timer for active pause
- [ ] Cancel pause early
- [ ] Master Block All toggle on home screen
- [ ] Service health indicator on home screen

#### Basic Settings
- [ ] Settings screen with theme selector (Light / Dark / System)
- [ ] About screen (version, open source licenses)
- [ ] Service health check screen (is NLS running?)
- [ ] Link to System > Notification Access settings

---

### PHASE 2 TASKS (Focus Modes & Scheduling)

#### Focus Mode Data Layer
- [ ] Create `FocusMode` Room entity (id, name, icon, color, allowedApps, blockedApps, isActive)
- [ ] Create `FocusModeDao`
- [ ] Seed 6 pre-built modes at first launch (Study, Sleep, Work, Prayer, Exam, Gaming)
- [ ] `FocusModeRepository` CRUD

#### Focus Mode UI
- [ ] Focus Mode home card (shows active mode or "No active mode")
- [ ] Focus Mode list screen (grid of available modes)
- [ ] Focus Mode detail screen (rules, schedule, start button)
- [ ] Custom Focus Mode builder screen (name, icon, apps, schedule)
- [ ] Start / Stop focus mode logic
- [ ] Auto-end focus mode (via WorkManager one-time task)
- [ ] End-of-mode summary bottom sheet (duration, notifications blocked)
- [ ] 5-minute warning notification before mode ends

#### Scheduling
- [ ] Create `Schedule` Room entity (id, name, startTime, endTime, days, linkedFocusMode)
- [ ] Create `ScheduleDao`
- [ ] Schedule list screen (list of all schedules)
- [ ] Add/Edit Schedule screen (time picker, day selector)
- [ ] Schedule activation via AlarmManager (exact or inexact based on permission)
- [ ] Schedule deactivation alarm
- [ ] Boot recovery: re-register all alarms on BOOT_COMPLETED
- [ ] Per-app quiet hours screen (app → set quiet hours)

#### Widget & Tile
- [ ] 2×1 home screen widget (AppWidgetProvider)
- [ ] Widget shows: current mode name + toggle button + blocked count
- [ ] Quick Settings tile (TileService)
- [ ] Tile shows ON/OFF state, toggle on click

---

### PHASE 3 TASKS (Smart Filtering)

#### Keyword Filter
- [ ] Create `KeywordRule` Room entity (keyword, appPackage nullable, isEnabled)
- [ ] Keyword manager screen (list, add, edit, delete keywords)
- [ ] Global keyword toggle
- [ ] Per-app keyword section in app detail screen
- [ ] Integrate keyword check in BlockingEngine
- [ ] Wildcard matching utility (*word*, word*)

#### Per-Channel Control
- [ ] Fetch notification channels per app (NotificationManager.getNotificationChannels)
- [ ] Create `ChannelRule` Room entity
- [ ] App detail screen → expand to show channels → toggle each
- [ ] Channel label editor (user can rename/tag channels)
- [ ] BlockingEngine checks channel rules

#### Priority Engine
- [ ] Create `AppPriority` enum (Critical / High / Normal / Low / Silent)
- [ ] Priority setting in app detail screen
- [ ] BlockingEngine priority check (Critical always passes)
- [ ] Priority badge shown in app list

#### Notification Digest
- [ ] Create `DigestSettings` in DataStore
- [ ] Digest interval picker (15 / 30 / 60 / 120 min)
- [ ] WorkManager periodic task for digest
- [ ] Post digest notification with count summary
- [ ] Tapping digest opens vault inbox

#### OTP Vault
- [ ] OTP regex detector (6-digit pattern + keywords like "OTP", "code", "verify")
- [ ] Separate OTP section in vault
- [ ] Large-text OTP display
- [ ] Copy OTP button
- [ ] Auto-delete OTP after 10 minutes

#### Vault Improvements
- [ ] Full-text search in vault (SQLite FTS or LIKE query)
- [ ] Filter by app, date range, category, read/unread
- [ ] Sort by newest / oldest / app name
- [ ] Category auto-tagger (local rule-based, no AI)
- [ ] Duplicate notification collapse

---

### PHASE 4 TASKS (Anti-Distraction & Social Shield)

#### Social Media Shield
- [ ] Hardcoded list of popular social app package names
- [ ] "Social Shield" one-tap preset on home screen
- [ ] Detect which social apps are installed
- [ ] Show Social Shield status card
- [ ] User can customize which apps are in the shield

#### Cooldown System
- [ ] Request AccessibilityService permission with explanation
- [ ] Create AccessibilityService subclass
- [ ] Detect app foreground event (window state changed)
- [ ] Trigger cooldown on configured app opens
- [ ] `AppCooldown` settings in DataStore
- [ ] Cooldown duration picker per app (5 / 10 / 15 / 30 min)
- [ ] Active cooldown indicator in notification shade

#### Streaks & Gamification
- [ ] Create `FocusSession` Room entity (startTime, endTime, mode, blockedCount)
- [ ] Calculate current streak from session history
- [ ] Streak display card on home screen
- [ ] Milestone celebrations (local notification or in-app toast)
- [ ] Streak reset logic (missed a day)

#### Limits & Calm Mode
- [ ] `NotificationLimit` Room entity (appPackage, maxPerHour)
- [ ] Counter in BlockingEngine (track count per app per hour)
- [ ] Per-app limit setter in app detail screen
- [ ] Calm Mode toggle (no sounds, no banners, vault only)

#### Emergency & PIN Bypass
- [ ] Trusted contacts screen (save phone numbers)
- [ ] Repeating caller rule logic (detect 2nd call from same number within 5 min)
- [ ] PIN setup screen (4 or 6 digit)
- [ ] PIN entry screen for bypass
- [ ] Bypass duration picker (15 min / 30 min / 1 hr)
- [ ] Auto-resume blocking after bypass expires

---

### PHASE 5 TASKS (Statistics & Analytics)

#### Dashboard
- [ ] Add MPAndroidChart or Vico library
- [ ] Daily blocked count bar chart (last 7 days)
- [ ] Top 5 blocked apps (horizontal bar chart)
- [ ] Focus time this week (progress ring)
- [ ] Streak card on dashboard
- [ ] Vault unread count badge

#### Logging & History
- [ ] Create `NotificationLog` Room entity (timestamp, app, title, action, category)
- [ ] Write log entry for every intercepted notification
- [ ] Notification history screen (searchable, filterable)
- [ ] Export log to JSON (save to Downloads folder)
- [ ] Clear log option

#### Reports
- [ ] Weekly report generator (aggregate data from Room)
- [ ] Monthly report summary
- [ ] Show report as scrollable summary screen
- [ ] Share report as text or save to Downloads

#### App Notification Score
- [ ] Score algorithm: frequency + duplicate rate + night-time spam
- [ ] Score shown as label (Respectful / Normal / Spammy) in app list
- [ ] Badge color indicator per score

#### Analytics Heatmap
- [ ] Notification heatmap by hour of day (24-hour chart)
- [ ] Color intensity = notification count
- [ ] Per-app heatmap option

---

### PHASE 6 TASKS (Polish & Advanced)

#### Vault Improvements
- [ ] Pin/unpin notifications
- [ ] Auto-clear rules screen (category-specific, time-based)
- [ ] Notification text copy action

#### Schedule Improvements
- [ ] Visual schedule calendar (weekly view, shows active times)
- [ ] Schedule conflict detector and resolver UI
- [ ] Holiday override option

#### New Focus Modes
- [ ] Commute mode preset
- [ ] Gym / Workout mode preset
- [ ] Meeting mode preset

#### Settings Expansion
- [ ] Export app settings as JSON backup
- [ ] Import settings from JSON backup file
- [ ] Storage usage screen (vault size, log size)
- [ ] Notification sound customization per focus mode
- [ ] Vibration pattern setting

#### Theming & UI
- [ ] Material You dynamic color integration
- [ ] Accent color picker
- [ ] 4×2 home screen widget
- [ ] Lock screen widget (Android 13+)
- [ ] App icon variants (monochrome for themed icons)

#### Onboarding
- [ ] First-launch walkthrough (3–4 screens)
- [ ] Permission request flow as part of onboarding
- [ ] Quick setup wizard (choose primary use case: Student / Professional / Privacy)
- [ ] Onboarding sets default rules based on use case

#### Localization
- [ ] Extract all strings to strings.xml
- [ ] Bengali / Bangla translation (bn.xml)
- [ ] RTL layout check

#### Accessibility
- [ ] Content descriptions on all icons
- [ ] Minimum touch target sizes
- [ ] High contrast mode detection
- [ ] TalkBack navigation audit

---

### PHASE 7 TASKS (Power User)

- [ ] Broadcast Intent API for Tasker / Automate integration
- [ ] Conditional rule builder (IF/THEN rule screen)
- [ ] Rule priority ordering (drag to reorder)
- [ ] Notification snooze (remind me in X minutes)
- [ ] User-created tags/labels for notifications in vault
- [ ] Regex support in keyword rules
- [ ] Biometric lock for vault screen (BiometricPrompt)
- [ ] Share notification from vault to other apps
- [ ] Notification archive with local compression
- [ ] Custom notification sounds per app

---

## 9. Database Schema

### Table: blocked_apps
| Column | Type | Notes |
|--------|------|-------|
| package_name | TEXT PK | App package identifier |
| app_label | TEXT | Human-readable name |
| is_blocked | INTEGER | 0 or 1 |
| priority | TEXT | Critical/High/Normal/Low/Silent |
| max_per_hour | INTEGER | 0 = no limit |
| cooldown_minutes | INTEGER | 0 = disabled |
| created_at | INTEGER | Unix timestamp |
| updated_at | INTEGER | Unix timestamp |

### Table: saved_notifications
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER PK AUTOINCREMENT | |
| package_name | TEXT | Source app |
| app_label | TEXT | App name at time of receipt |
| title | TEXT | Notification title |
| body | TEXT | Notification body text |
| channel_id | TEXT | Notification channel |
| category | TEXT | Auto-tagged category |
| is_read | INTEGER | 0 or 1 |
| is_pinned | INTEGER | 0 or 1 |
| is_otp | INTEGER | 0 or 1 |
| otp_code | TEXT | Extracted OTP if detected |
| received_at | INTEGER | Unix timestamp |
| deleted_at | INTEGER | Soft delete timestamp |

### Table: focus_modes
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER PK AUTOINCREMENT | |
| name | TEXT | Mode name |
| icon_name | TEXT | Material icon name |
| color_hex | TEXT | Mode accent color |
| allowed_packages | TEXT | JSON array |
| blocked_packages | TEXT | JSON array |
| is_preset | INTEGER | 0=custom, 1=built-in |
| is_active | INTEGER | 0 or 1 |
| created_at | INTEGER | |

### Table: schedules
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER PK AUTOINCREMENT | |
| name | TEXT | |
| start_hour | INTEGER | 0–23 |
| start_minute | INTEGER | 0–59 |
| end_hour | INTEGER | |
| end_minute | INTEGER | |
| days_of_week | TEXT | JSON: [1,2,3,4,5] Mon–Fri |
| focus_mode_id | INTEGER | FK → focus_modes |
| is_enabled | INTEGER | |

### Table: keyword_rules
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER PK AUTOINCREMENT | |
| keyword | TEXT | The keyword/phrase |
| package_name | TEXT NULLABLE | null = global |
| use_wildcard | INTEGER | 0 or 1 |
| is_enabled | INTEGER | |

### Table: notification_log
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER PK AUTOINCREMENT | |
| package_name | TEXT | |
| app_label | TEXT | |
| title | TEXT | |
| action | TEXT | BLOCKED/SAVED/ALLOWED/DIGESTED |
| category | TEXT | |
| timestamp | INTEGER | |

### Table: focus_sessions
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER PK AUTOINCREMENT | |
| focus_mode_id | INTEGER | |
| focus_mode_name | TEXT | |
| start_time | INTEGER | |
| end_time | INTEGER | |
| notifications_blocked | INTEGER | |
| was_completed | INTEGER | 0=interrupted, 1=completed |

### Table: channel_rules
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER PK AUTOINCREMENT | |
| package_name | TEXT | |
| channel_id | TEXT | |
| channel_name | TEXT | |
| user_label | TEXT NULLABLE | |
| is_blocked | INTEGER | |

---

## 10. UI/UX Screen List

| Screen | Route | Phase |
|--------|-------|-------|
| Onboarding / Welcome | onboarding | 6 |
| Permission Setup | permission_setup | 1 |
| Home Dashboard | home | 1 |
| App List (Block Rules) | app_list | 1 |
| App Detail | app_detail/{package} | 1 |
| Vault Inbox | vault | 1 |
| Vault OTP Section | vault/otp | 3 |
| Quick Pause Sheet | (bottom sheet) | 1 |
| Focus Mode List | focus_modes | 2 |
| Focus Mode Detail | focus_mode/{id} | 2 |
| Focus Mode Builder | focus_mode/create | 2 |
| Schedule List | schedules | 2 |
| Add/Edit Schedule | schedule/{id} | 2 |
| Per-App Schedule | app_schedule/{pkg} | 2 |
| Keyword Rules | keyword_rules | 3 |
| Priority Manager | priority_settings | 3 |
| Digest Settings | digest_settings | 3 |
| Social Shield | social_shield | 4 |
| Cooldown Settings | cooldown_settings | 4 |
| Streaks & Stats | streaks | 4 |
| Analytics Dashboard | analytics | 5 |
| Notification History | notification_history | 5 |
| Weekly Report | report/{week} | 5 |
| Schedule Calendar | schedule_calendar | 6 |
| Backup & Restore | backup_restore | 6 |
| Storage Info | storage_info | 6 |
| Settings | settings | 1 |
| Theme Settings | settings/theme | 1 |
| Language Settings | settings/language | 6 |
| About | settings/about | 1 |
| Service Health | settings/service_health | 1 |

---

## 11. Non-Functional Requirements

### Performance
- NFR1: App startup time < 1.5 seconds cold start
- NFR2: NotificationListenerService processes each notification in < 50ms
- NFR3: Vault inbox loads first 50 items within 200ms
- NFR4: Scheduling alarms accurate within ±1 minute

### Battery
- NFR5: Foreground service uses < 0.5% battery per hour
- NFR6: WorkManager tasks are batched and not run more than needed
- NFR7: No wake locks held unnecessarily

### Storage
- NFR8: Notification log auto-purge after 90 days (configurable)
- NFR9: OTPs auto-deleted after 10 minutes
- NFR10: Total app size < 30 MB installed

### Privacy
- NFR11: Zero network calls made by the app
- NFR12: All notification content stays on device
- NFR13: No analytics SDKs, no crash reporting SDKs
- NFR14: User can delete all data from settings at any time
- NFR15: Exported data is human-readable JSON (user owns their data)

### Reliability
- NFR16: Service must recover automatically after device restart
- NFR17: Rules must persist even if app process is killed
- NFR18: Graceful degradation if permission is revoked mid-session

### Compatibility
- NFR19: Support Android 8.0 (API 26) and above
- NFR20: Test on Android 10, 12, 13, 14, 15
- NFR21: Support tablet layouts (responsive UI)

---

*Document Version: 1.0 — MyNotification Android App PRD*  
*Last Updated: June 2026*  
*Author: Development Team*