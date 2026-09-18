# My Notification — Product Requirements & Technical Specification

**Product name:** My Notification  
**Platform:** Android  
**Primary goal:** A personal notification-management, notification-history, blocking, filtering, scheduling, and analytics application focused on reducing unnecessary interruptions while preserving important notifications.

---

## 1. Product Vision

My Notification is a personal notification control center.

It should:

- Capture and store incoming device notifications in detail.
- Let the user view the complete notification history.
- Block notifications according to app, category, social-media grouping, keyword, schedule, or custom rules.
- Support both **block-all + whitelist** and **allow-all + selective-block** workflows.
- Protect important notifications such as OTPs and financial/security messages.
- Automatically suppress repetitive/spam-like SMS offers.
- Provide notification analytics for 1 hour, 5 hours, 24 hours, 1 day, 7 days, and other useful periods.
- Detect and deduplicate notifications that continuously update, such as calls, downloads, media playback, timers, and progress notifications.
- Give the user advanced custom rules instead of forcing a single blocking strategy.
- Keep the UX simple enough for everyday use.

---

# 2. Core Features — MUST HAVE

The following features are part of the product scope and must not be removed.

## 2.1 Notification History

Capture notifications through Android's notification-listener system.

For each captured notification, store as much useful information as Android makes available, subject to Android privacy/security restrictions.

Possible fields:

- Notification ID/key
- Package name
- Application name
- Notification title
- Notification text
- Big text
- Subtext
- Conversation/person information when available
- Timestamp posted
- Timestamp last updated
- Timestamp removed
- Notification category
- Channel ID
- Channel name
- Group key
- Notification flags
- Priority/importance when available
- Ongoing status
- Auto-cancel status
- Clearable status
- Progress value when available
- Maximum progress when available
- Indeterminate progress state
- Number/badge value when available
- Actions available
- Remote input availability when exposed
- Notification extras that are safe and useful to retain
- Source application icon/reference
- Whether it was blocked
- Whether it was allowed
- Which rule caused the block
- Whether it was a duplicate/update of an existing notification
- First-seen time
- Last-seen/update time
- Event count

### Important privacy rule

Do not assume Android exposes every piece of notification data. Store only information actually available through the Android APIs and permitted by the OS.

Sensitive notification contents should be protected locally with appropriate encryption/access controls.

---

# 3. Notification Blocking System

The blocking engine should support multiple modes.

## 3.1 Global Notification Block

User can enable:

> Block All Notifications

When enabled, notifications should be suppressed/cancelled as far as Android's APIs permit.

The app should clearly communicate that Android system limitations may prevent absolute interception of every system-level visual interruption, especially certain full-screen intents, calls, alarms, or OEM-specific behavior.

---

## 3.2 Block All Social Media

A predefined Social Media category.

Examples may include:

- Facebook
- Instagram
- TikTok
- X
- Snapchat
- Reddit
- Threads
- YouTube
- Messenger
- Discord
- Other apps classified as social media

The classification should be editable by the user.

Users can add/remove apps from categories.

---

## 3.3 Selective App Blocking

User can choose:

> Selected Apps Only

Then select individual applications.

Example:

- Instagram → blocked
- Facebook → blocked
- WhatsApp → allowed
- Gmail → allowed

There should be a searchable app picker.

---

## 3.4 Block All + App Whitelist

Workflow:

> Block Everything → Allow Selected Apps

Example:

- Everything blocked
- WhatsApp → whitelist
- Telegram → whitelist
- Phone → whitelist
- Gmail → whitelist

The whitelist overrides the global block.

---

## 3.5 Block All + Keyword Whitelist

Workflow:

> Block Everything → Allow Notifications Matching Specific Rules

Example keywords:

- OTP
- verification
- security code
- transaction
- payment
- received
- credited
- debited
- login
- authentication

If a blocked notification matches a whitelist rule, it becomes allowed.

---

# 4. Custom Notification Rules

Advanced rule engine.

A user should be able to create rules using combinations of:

- App
- Package
- Notification title
- Notification text
- Keyword
- Multiple keywords
- Exact phrase
- Contains
- Does not contain
- Starts with
- Ends with
- Notification category
- Notification channel
- Time range
- Day of week
- Ongoing/non-ongoing
- Progress notification
- OTP pattern
- Financial/security pattern

Example:

> Block Instagram notifications except messages containing "urgent".

Example:

> Block all notifications from 10:00 PM–8:00 AM except WhatsApp calls and OTPs.

Example:

> Block SMS promotional messages but allow financial transaction messages.

---

# 5. Scheduling System

Notification blocking can be scheduled.

Examples:

### Schedule A
10:00 PM → 8:00 AM  
Block notifications.

### Schedule B
9:00 AM → 4:00 PM  
Block social media.

### Schedule C
11:00 PM → 7:00 AM  
Block everything except whitelist.

### Schedule D
Class time  
Block everything except emergency/important rules.

Features:

- Multiple schedules
- Recurring schedules
- Specific weekdays
- Specific dates
- Start/end time
- Enable/disable schedule
- Priority
- Schedule-specific whitelist
- Schedule-specific blacklist
- Schedule conflict handling

---

# 6. Notification Filtering Priority

The rule engine should have a deterministic priority system.

Suggested evaluation order:

1. Emergency/system-critical exceptions
2. Explicit whitelist
3. Explicit keyword whitelist
4. Explicit app whitelist
5. Schedule rules
6. Explicit block rules
7. Category rules
8. Global block
9. Default allow/block policy

The exact priority should be configurable if necessary.

The UI must explain **why** a notification was blocked.

Example:

> Blocked by: Sleep Schedule → Global Block

or:

> Allowed by: Keyword Whitelist → "OTP"

---

# 7. OTP & Security Notification Protection

Important OTP notifications must remain visible when the user wants them to.

Support patterns such as:

- 4-digit OTP
- 6-digit OTP
- 8-digit OTP
- "OTP"
- "verification code"
- "security code"
- "one-time password"
- "verification"
- "authentication code"

The detector should use multiple signals rather than only digit counting.

Example:

`Your verification code is 482913`

→ Important OTP

But:

`Offer: 50GB for 7 days – 482913`

should not automatically become an OTP merely because it contains digits.

---

# 8. Financial Notification Protection

Important financial/security messages should be preserved.

Potential categories:

- bKash
- Nagad
- Bank apps
- Card/payment apps
- Transaction notifications
- Login/security alerts
- Payment confirmations
- Money received
- Money sent
- Account security

Examples:

> Money received

> Transaction successful

> OTP: 482913

> Your account was accessed

These should be distinguishable from promotional offers.

The user should be able to customize trusted financial apps.

---

# 9. SMS Spam / Promotional Offer Filtering

Promotional SMS notifications can be blocked.

Examples:

- Internet package offers
- Minutes offers
- SMS packages
- Recharge promotions
- Discount campaigns
- Marketing messages
- "Buy X GB for Y days"
- Operator advertisements

The system should distinguish:

### Promotional
"Get 20GB for 7 days..."

→ Block

### Financial/security
"Your OTP is 482913"

→ Allow

### Transaction
"Tk 500 received..."

→ Allow

This should be rule-based and user-editable.

### Android consideration

NotificationListenerService can process notifications generated by SMS/messaging apps. Direct SMS reading requires separate Android permissions and is subject to Google Play policy restrictions. Do not add SMS permissions unless the product genuinely qualifies for the relevant Play policy requirements.

---

# 10. Repetitive Notification / Update Deduplication

This is a major differentiating feature.

The app must NOT count every update of a single continuously updating notification as a new notification.

## 10.1 Missed Call Example

A missed/ongoing call notification may remain/update for around one minute.

Incorrect:

- Call notification #1
- Call notification #2
- Call notification #3
- ...
- 60 notifications

Correct:

> One notification event

Metadata:

- First seen: 2:14 PM
- Last updated: 2:15 PM
- Duration: 1 minute
- Updates: 60
- Logical notification count: 1

---

# 11. Download Progress Deduplication

Example:

A download takes five minutes.

The notification changes:

- Downloading 1%
- Downloading 2%
- Downloading 3%
- ...
- Downloading 100%

This must be treated as:

> One logical notification event

Store:

- First seen
- Last updated
- Duration
- Final state
- Progress history if useful
- Number of updates

Analytics should count:

> 1 notification event

not:

> 300 notification events.

---

# 12. Notification Identity / Deduplication Engine

Use Android notification identity information where available.

Possible identity signals:

- Notification key
- Package name
- Notification ID
- Tag
- Group
- Channel
- Ongoing state
- Conversation ID
- Content signature

Maintain a logical event record.

Example data model:

```text
NotificationEvent
 ├── eventId
 ├── packageName
 ├── notificationKey
 ├── firstSeenAt
 ├── lastUpdatedAt
 ├── removedAt
 ├── updateCount
 ├── initialTitle
 ├── latestTitle
 ├── initialText
 ├── latestText
 ├── duration
 ├── isOngoing
 ├── isProgress
 ├── wasBlocked
 └── blockReason
```

When the same notification is updated, update the existing logical event instead of creating a new event.

If Android gives a genuinely new notification identity, create a new event.

---

# 13. Notification Analytics

Dashboard should show notification statistics.

## Time ranges

- Last 1 hour
- Last 5 hours
- Last 24 hours
- Today
- Yesterday
- Last 7 days
- Custom range

## Metrics

- Total notification events
- Total blocked
- Total allowed
- Total notifications by app
- Top notification-generating apps
- Top blocked apps
- Top keywords
- Notifications per hour
- Average notifications per hour
- Longest notification session
- Most active time
- Social-media notification count
- Promotional notification count
- OTP/security notification count
- Financial notification count

---

# 14. App Ranking

Example:

```text
Notification Sources

1. WhatsApp       42
2. Facebook       31
3. Gmail          18
4. Telegram       14
5. Instagram      11
```

Allow sorting by:

- Total
- Blocked
- Allowed
- Recent
- Percentage blocked

---

# 15. Notification Timeline

A chronological notification feed.

Each item should show:

- App icon
- App name
- Time
- Title
- Short preview
- Status
- Block/allow indicator

Tap → full notification details.

Filters:

- App
- Date
- Blocked
- Allowed
- Social
- OTP
- Financial
- Promotional
- Ongoing
- Search

---

# 16. "All Notifications" Screen

This screen must contain the complete captured history.

Requirement:

> No notification should silently disappear from My Notification's internal history unless it has been deleted according to the user's retention settings.

Even blocked notifications should be visible internally.

Example:

```text
Blocked — Instagram
"New reel from..."

Allowed — WhatsApp
"Rahim: Where are you?"

Blocked — Operator SMS
"Get 20GB for 7 days..."

Allowed — bKash
"OTP: 482913"
```

---

# 17. Notification Detail Screen

Show the maximum useful available detail.

Sections:

### Basic
- App
- Time
- Notification ID/key
- Status

### Content
- Title
- Text
- Expanded text
- Subtext

### Technical
- Channel
- Category
- Importance
- Flags
- Ongoing
- Clearable
- Group

### Lifecycle
- First seen
- Last updated
- Removed
- Duration
- Update count

### Filtering
- Allowed/Blocked
- Rule responsible
- Matching keyword
- Matching app/category

---

# 18. Home Screen UI/UX

## Status Bar

Respect the Android system status bar and safe-area/insets.

Content should begin below the status bar.

---

## Header

Left:

☰ Hamburger menu

Center/left:

**My Notification**

Right:

Profile icon

The header should be clean and compact.

---

# 19. Hero Section

After a small amount of spacing, show a hero card.

Primary insight:

> Total Blocked

Secondary insight:

> Important/Useful notification insight

Examples:

```text
1,284
Notifications blocked

You avoided 3h 42m of interruption
```

The secondary metric can be changed based on the strongest available insight.

---

# 20. Main Notification Blocker Section

Show:

> Notification Blocker

with a large ON/OFF toggle.

When OFF:

- Keep section compact.

When ON:

- Expand the section smoothly.
- Reveal blocking modes.

Options:

### Block All Social Media
Preconfigured social-media app category.

### Selected Apps Only
Choose individual apps.

### Block All
Everything blocked except configured exceptions.

### Custom
Advanced rule-based blocking.

---

# 21. Selected Apps Interface

Default app groups should be available.

Example:

```text
Social Media
☑ Instagram
☑ Facebook
☑ TikTok
☐ WhatsApp
☐ Telegram
```

User can press:

> + Add App

to add more apps.

Support search.

---

# 22. Menu Navigation

The hamburger menu should navigate to a **new screen**, not an overlay.

## Menu Screen Header

Left:

Back button

Next:

**Menu**

Right:

Profile icon

Three-dot menu

---

# 23. Menu Three-Dot Dropdown

When the user taps the three-dot button, show:

1. Help
2. Tutorial
3. Report a Problem

This should be a standard Android dropdown/menu.

---

# 24. Menu Screen Sections

Recommended structure:

### Notifications
- All Notifications
- Notification Analytics
- Blocked Notifications
- Allowed Notifications

### Blocking
- Notification Blocker
- Blocked Apps
- Whitelisted Apps
- Whitelisted Keywords
- Custom Rules
- Notification Categories

### Scheduling
- Schedules
- Active Schedule
- Schedule History

### Protection
- OTP Protection
- Financial Notifications
- Promotional SMS Filter
- Important Notifications

### System
- Permissions
- Notification Access
- App Settings
- Data & Storage

### Support
- Help
- App Tutorial
- Report a Problem

---

# 25. Permissions Screen

Show permission/access status clearly.

Potential permissions/access:

- Notification Access
- Post Notifications
- Battery optimization status
- Accessibility access, only if the implementation genuinely requires it
- SMS permission, only if legitimately needed and policy-compliant
- Other required system access

Each should show:

```text
Notification Access
✓ Enabled
```

or

```text
Notification Access
✗ Disabled
[Enable]
```

Do not request unnecessary permissions.

---

# 26. Default Smart Configuration

On first setup, the app can suggest sensible defaults.

Example:

### Important
- OTP/security messages
- Financial transactions
- Calls
- User-selected important apps

### Distracting
- Social media
- Promotional SMS
- Marketing notifications

The app may suggest rules based on observed notification patterns, but the user should remain in control.

Never silently create aggressive blocking rules without clear user consent.

---

# 27. Smart Insights

Examples:

> Instagram sent you 47 notifications today.

> 68% of your notifications came from 3 apps.

> Most notifications arrived between 7 PM and 11 PM.

> You blocked 82 promotional notifications this week.

> WhatsApp generated 34 notifications today.

Insights should be descriptive rather than judgmental.

---

# 28. Advanced Features

These can be included without removing the core requirements.

## 28.1 Quiet Hours

Temporary or recurring notification silence.

## 28.2 Emergency Bypass

Allow selected emergency contacts/apps/categories.

## 28.3 Important Notification Mode

Only important notifications appear.

## 28.4 Notification Digest

Blocked notifications can be summarized later.

Example:

> 42 notifications were blocked during Study Mode.

Tap → review.

## 28.5 Temporary Block

Options:

- 15 minutes
- 30 minutes
- 1 hour
- 2 hours
- Until tomorrow
- Custom

## 28.6 Context-Based Rules

Potential contexts:

- Study
- Class
- Sleep
- Work
- Meeting
- Driving

The user should be able to configure each context.

---

# 29. Search

Global notification search.

Search by:

- App
- Title
- Text
- Keyword
- Date
- Notification type

Example:

> Search "OTP"

→ Show all OTP-related notifications.

---

# 30. Data Retention

Provide configurable retention:

- 7 days
- 30 days
- 90 days
- 180 days
- 1 year
- Until manually deleted

Optional:

> Keep forever

Large notification databases should be managed carefully to prevent excessive storage usage.

---

# 31. Privacy & Security

This application handles highly sensitive notification content.

Therefore:

- Store data locally by default.
- Do not upload notification contents to a server by default.
- Do not send notification contents to analytics providers.
- Avoid logging notification text in production logs.
- Encrypt sensitive local storage where practical.
- Provide "Delete all notification history".
- Provide retention controls.
- Make privacy policy explicit.
- Clearly explain Notification Access permission.

If cloud backup/sync is ever added, it must be explicitly opt-in and strongly protected.

---

# 32. Technical Architecture

Recommended Android architecture:

```text
Kotlin
Jetpack Compose
MVVM / Clean Architecture
Room Database
DataStore
NotificationListenerService
WorkManager
Android AlarmManager where appropriate
Kotlin Coroutines + Flow
```

Suggested layers:

```text
UI
 ↓
ViewModel
 ↓
Domain / Rule Engine
 ↓
Notification Repository
 ↓
NotificationListenerService
 ↓
Android Notification APIs
```

---

# 33. Core Services

## NotificationCaptureService

Responsible for:

- Detecting posted notifications
- Detecting removed notifications
- Detecting updates
- Extracting metadata
- Sending events to the repository

## NotificationRuleEngine

Responsible for:

- App matching
- Keyword matching
- Category matching
- Schedule matching
- Whitelist evaluation
- Block evaluation
- Rule priority

## NotificationDeduplicationEngine

Responsible for:

- Identifying notification updates
- Grouping continuous notifications
- Preventing inflated counts

## AnalyticsEngine

Responsible for:

- Aggregations
- Time-range calculations
- App ranking
- Trend detection

---

# 34. Database Concept

Suggested tables:

```text
apps
notifications
notification_events
notification_updates
blocking_rules
rule_conditions
whitelisted_apps
whitelisted_keywords
blocked_apps
schedules
notification_categories
analytics_snapshots
user_settings
```

A separate `notification_updates` table can preserve detailed update history while `notification_events` stores the logical notification event.

---

# 35. Rule Engine Example

```text
IF
    globalBlock = true

AND
    app NOT IN whitelist

AND
    notification DOES NOT match important rules

THEN
    BLOCK
```

Another:

```text
IF
    app = Instagram
    AND
    time = 22:00–08:00
THEN
    BLOCK
```

Another:

```text
IF
    globalBlock = true
    AND
    text contains "OTP"
THEN
    ALLOW
```

---

# 36. Important Android Technical Reality

The product should be designed around what Android actually permits.

### Notification access

`NotificationListenerService` can observe notification lifecycle events and can cancel notifications where Android permits.

### Full-screen notifications

A notification listener is not a guaranteed universal "prevent every visual interruption before it happens" mechanism.

Some full-screen intents, calls, alarms, OEM behaviors, and system-level notifications can behave differently.

Therefore the product should:

1. Cancel/remove notifications when possible.
2. Detect and explain cases where Android prevents complete suppression.
3. Test heavily across Pixel/AOSP and major OEMs.
4. Avoid claiming impossible "100% blocking" behavior.

### Notification channels

Some notification behavior is controlled by Android notification channels and importance settings. The app should expose channel information and guide the user to system settings when necessary.

---

# 37. OEM Compatibility

Test separately on:

- Google Pixel
- Samsung
- OnePlus
- Xiaomi/Redmi
- Oppo
- Vivo
- Realme
- Huawei where relevant

Pay special attention to:

- Background restrictions
- Battery optimization
- Notification listener reliability
- Full-screen notifications
- Calls
- Messaging apps
- Aggressive OEM process killing

---

# 38. UX Principles

The app should feel:

- Calm
- Minimal
- Fast
- Professional
- Privacy-first
- Data-driven
- Non-confusing

Avoid overwhelming the user with technical settings on the home screen.

Advanced functionality should be available progressively.

---

# 39. Home Screen Information Hierarchy

Recommended:

```text
┌─────────────────────────────────┐
│ ☰   My Notification        👤  │
├─────────────────────────────────┤
│                                 │
│  1,284                          │
│  Notifications Blocked         │
│                                 │
│  ↓ 68% less interruption        │
│                                 │
├─────────────────────────────────┤
│                                 │
│ Notification Blocker       ON   │
│                                 │
│ ● Block All Social Media        │
│ ○ Selected Apps                 │
│ ○ Block All                     │
│ ○ Custom Rules                  │
│                                 │
├─────────────────────────────────┤
│                                 │
│ Today's Notifications           │
│                                 │
│  143 total     87 blocked       │
│                                 │
└─────────────────────────────────┘
```

---

# 40. Onboarding

Suggested onboarding:

### Step 1
Explain the product.

### Step 2
Enable Notification Access.

### Step 3
Choose default blocking strategy.

### Step 4
Select important apps.

### Step 5
Enable OTP/financial protection.

### Step 6
Configure optional schedule.

### Step 7
Finish.

Do not force every advanced setting during onboarding.

---

# 41. Error Handling

If Notification Access is disabled:

> My Notification cannot capture or manage notifications until Notification Access is enabled.

If permission is revoked:

> Notification Access was disabled. Notification history and blocking may not work until it is restored.

If Android/OEM prevents cancellation:

> Android prevented this notification from being fully suppressed.

The app should never silently pretend that blocking succeeded.

---

# 42. Analytics Definitions

Use **logical notification events**, not raw lifecycle callbacks, for user-facing analytics.

Example:

One WhatsApp notification updated 15 times:

```text
Raw callbacks: 15
Logical notifications: 1
Updates: 15
```

A five-minute download:

```text
Raw updates: potentially hundreds
Logical notification: 1
Duration: 5 minutes
```

This prevents analytics from becoming misleading.

---

# 43. Notification Event Lifecycle

```text
POSTED
  ↓
CAPTURE
  ↓
IDENTIFY
  ↓
MATCH EXISTING EVENT?
  ├── YES → UPDATE EVENT
  └── NO  → CREATE EVENT
  ↓
EVALUATE RULES
  ↓
ALLOW / BLOCK
  ↓
STORE RESULT
  ↓
ANALYTICS
```

---

# 44. Recommended MVP Order

Even though the final product contains all requirements, implementation can be phased.

### Phase 1
- Notification access
- Notification capture
- Local database
- All Notifications
- Basic blocker
- App-based blocking
- Basic whitelist

### Phase 2
- Global block
- Social media category
- Keyword rules
- OTP protection
- Financial protection
- Promotional SMS filtering

### Phase 3
- Deduplication engine
- Ongoing notification handling
- Download/progress handling
- Notification lifecycle tracking

### Phase 4
- Scheduling
- Custom rule engine
- Advanced whitelist
- Context modes

### Phase 5
- Analytics
- Insights
- Advanced UI
- Search
- Retention management

### Phase 6
- OEM compatibility testing
- Battery optimization handling
- Edge cases
- Reliability hardening
- Privacy/security audit

---

# 45. Non-Goals / Things Not to Do

These are not removals of requested functionality; they are implementation boundaries.

Do NOT:

- Upload notification contents to a server by default.
- Assume every Android notification exposes identical data.
- Count every `onNotificationPosted()` callback as a separate notification.
- Promise universal full-screen notification blocking when Android does not guarantee it.
- Request unnecessary permissions.
- Automatically delete notification history without user-controlled retention.
- Hide blocked notifications from the internal history.
- Make important/OTP detection depend only on the presence of digits.
- Create hidden rules the user cannot understand.
- Overload the home screen with advanced technical controls.

---

# 46. Final Product Definition

**My Notification** is a privacy-first Android notification operating layer for the user's personal device.

It combines:

> Notification History  
> + Notification Blocking  
> + App Whitelisting  
> + Keyword Whitelisting  
> + Social Media Filtering  
> + OTP Protection  
> + Financial Notification Protection  
> + Promotional SMS Filtering  
> + Smart Deduplication  
> + Scheduling  
> + Custom Rules  
> + Notification Analytics  
> + Smart Insights  
> + Notification Search  
> + Detailed Notification Lifecycle  
> + Privacy Controls

The key product principle is:

> **Block what the user doesn't need, preserve what matters, and accurately record what actually happened.**

The system should always distinguish between a **raw Android notification callback** and a **logical notification event**. This distinction is essential for accurate analytics and is one of the most important technical requirements of My Notification.
