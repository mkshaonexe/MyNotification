# My Notification — Full UI/UX Design System
## Dark Mode + Light Mode
### Product Design Specification for Android / Jetpack Compose

**Product:** My Notification  
**Platform:** Android  
**Design direction:** Minimal · Calm · Professional · Privacy-first · Data-driven  
**Primary visual reference:** The supplied 10-screen UI concept showing Home, Notification History, Blocking Rules, Analytics, and Settings in both dark and light themes.  
**Product specification basis:** The supplied My Notification Product Requirements & Technical Specification.

---

# 1. Design Intent

My Notification should feel like a **quiet control center for the phone**, not like a technical utility full of switches.

The product manages a sensitive information stream: notifications. The UI therefore needs to communicate three things immediately:

1. **Control** — the user knows what is being blocked and why.
2. **Safety** — important notifications such as OTP, financial, security, calls, and selected apps remain protected.
3. **Calm** — the interface reduces cognitive noise instead of adding more.

The visual reference establishes the core aesthetic:

- Large whitespace / breathing room.
- Rounded but restrained cards.
- One primary accent family.
- Strong typography hierarchy.
- Compact Android-style controls.
- Dark surfaces with subtle elevation.
- White surfaces with very light borders/shadows.
- Clear status chips such as **Blocked**, **Allowed**, **Important**, and **Granted**.
- Bottom navigation for high-frequency areas.
- Secondary features behind screens rather than crowding the dashboard.
- Data visualizations that are simple enough to understand within a few seconds.

The UI must remain consistent while switching between Dark Mode and Light Mode. **Only the color/surface treatment changes; layout, hierarchy, interaction patterns, component geometry, and information architecture stay stable.**

---

# 2. Product UX Principles

## 2.1 Calm by default

The app should visually reduce urgency.

Avoid:

- excessive red;
- unnecessary gradients;
- animated numbers everywhere;
- heavy shadows;
- giant titles;
- dense configuration forms;
- too many toggles visible simultaneously.

Use visual emphasis only when it represents a meaningful state.

---

## 2.2 Show the decision, hide the complexity

A user should first see:

> What is happening?

Then:

> What can I control?

Only after entering a deeper screen should the app expose:

> How exactly does the rule engine work?

Example:

**Home**

> Notification Blocker  
> ON  
> Block Social Media

Then, inside the rule editor:

> App → Instagram  
> Condition → Notification contains  
> Value → urgent  
> Schedule → 10:00 PM–8:00 AM  
> Action → Block

---

## 2.3 User control is visible

Never create a hidden blocking rule.

Whenever a notification is blocked, the UI should be able to explain the reason:

> Blocked by  
> Sleep Schedule → Global Block

Or:

> Allowed by  
> Keyword Whitelist → OTP

This follows the product requirement that the UI should explain why a notification was blocked.

---

## 2.4 Important notifications look protected

OTP, financial, security and emergency states should never look identical to promotional or distracting content.

The UI should visually distinguish:

- Important
- Security
- Financial
- OTP
- Promotional
- Blocked
- Allowed
- Ongoing

However, the design should avoid alarm-style visuals for ordinary protected notifications.

---

## 2.5 Progressive disclosure

The Home screen exposes the most important controls.

Advanced features belong inside:

- Blocking Rules
- Custom Rules
- Schedules
- Protection
- Permissions
- Data & Privacy
- Settings

Do not put the full rule engine on the Home screen.

---

# 3. Design Language

## 3.1 Overall aesthetic

The visual language should be:

> **Soft dark surfaces + clean white surfaces + electric blue primary action + restrained semantic colors.**

The reference image uses a dark navy/charcoal interface and a white interface with blue accents. This should become the product's visual foundation.

---

# 4. Theme Architecture

The app has two explicit appearance modes:

### Dark Mode

Designed for:

- low-light usage;
- night-time notification management;
- reduced visual glare;
- premium technical feel.

### Light Mode

Designed for:

- daylight;
- high readability;
- accessibility;
- clean professional productivity-tool appearance.

### System Default

Respect Android system appearance.

The theme selector should contain:

- Dark Mode
- Light Mode
- System Default

Do not create separate component designs for each theme. Components should use semantic tokens.

---

# 5. Color System

## 5.1 Core brand color

Use a restrained electric blue as the primary interaction color.

### Primary

```text
Primary 500: #4F6BFF
Primary 400: #6F86FF
Primary 600: #3E57E8
Primary 700: #3348C7
```

Recommended default interaction color:

```text
#4F6BFF
```

Use for:

- primary buttons;
- active navigation item;
- selected tabs;
- active toggles;
- focused fields;
- progress/rings;
- links;
- selected radio buttons;
- important positive interaction feedback.

Do not use blue for every icon. Neutral icons should remain neutral.

---

# 6. Dark Theme Color Tokens

## 6.1 Dark background

```text
Dark Background        #080D14
Dark Surface           #101720
Dark Surface Elevated  #151F2B
Dark Surface Strong    #1B2634
Dark Border            #263241
Dark Border Subtle     #1B2530
```

Hierarchy:

```text
Screen
  #080D14

Card
  #101720

Elevated Card
  #151F2B

Pressed / Expanded
  #1B2634
```

The background must not be pure black.

Avoid:

```text
#000000
```

for the main screen background because it creates overly harsh contrast with the soft premium reference.

---

## 6.2 Dark text

```text
Dark Text Primary     #F5F7FA
Dark Text Secondary   #A9B3C0
Dark Text Tertiary    #748091
Dark Text Disabled    #505B69
```

Text hierarchy:

```text
Primary    100%
Secondary   ~70%
Tertiary    ~50%
Disabled    ~30%
```

---

# 7. Light Theme Color Tokens

## 7.1 Light background

```text
Light Background        #F7F9FC
Light Surface           #FFFFFF
Light Surface Elevated  #FFFFFF
Light Surface Soft      #F1F4F8
Light Border            #E4E9F0
Light Border Strong     #D5DCE6
```

The light theme should feel slightly off-white rather than pure white everywhere.

---

## 7.2 Light text

```text
Light Text Primary      #111827
Light Text Secondary    #5E6877
Light Text Tertiary     #8791A0
Light Text Disabled     #B0B7C2
```

---

# 8. Semantic Colors

Semantic colors should communicate state, not decoration.

## 8.1 Success / Allowed / Protected

```text
Success      #2CCB82
Success Soft #DDF8EC
```

Dark theme use:

```text
Success Dark Surface #123025
```

---

## 8.2 Warning

Use sparingly.

```text
Warning      #F5B84B
Warning Soft #FFF3D6
```

Dark theme:

```text
Warning Dark Surface #332A17
```

---

## 8.3 Error / Blocked

Blocked state should use a muted red rather than aggressive pure red.

```text
Error        #F05B67
Error Soft   #FDE5E7
```

Dark:

```text
Error Dark Surface #351B20
```

Use red for:

- blocked status;
- rule conflicts;
- permission failure;
- destructive action;
- system errors.

Do not color an entire card red just because it contains a blocked notification.

---

## 8.4 Security

```text
Security     #8B6CFF
```

Use for:

- security alerts;
- authentication;
- trusted security rules.

---

## 8.5 Informational

```text
Info         #55A8FF
```

Use for:

- sync;
- system information;
- neutral insights.

---

# 9. Typography

Recommended Android typography:

```text
Font: Inter
Fallback: Roboto
```

Use Android system font if Inter cannot be bundled.

## 9.1 Typography scale

```text
Display Large      32sp / 38sp
Display Medium     28sp / 34sp

Headline Large     24sp / 30sp
Headline Medium    20sp / 26sp
Headline Small     18sp / 24sp

Title Large        17sp / 23sp
Title Medium       16sp / 22sp
Title Small        14sp / 20sp

Body Large         16sp / 24sp
Body Medium        14sp / 21sp
Body Small         13sp / 19sp

Label Large        13sp / 18sp
Label Medium       12sp / 16sp
Label Small        11sp / 14sp
```

Font weights:

```text
Regular      400
Medium       500
SemiBold     600
Bold         700
```

Use 700 only for:

- hero metric;
- major page title;
- important numeric result.

---

# 10. Number Formatting

Analytics numbers should use tabular-looking alignment where possible.

Examples:

```text
1,284
812
326
110
```

Hero numbers:

```text
32sp / Bold
```

Secondary numbers:

```text
18–20sp / SemiBold
```

Avoid unnecessary decimal places.

Use:

```text
1.2K
29K
1,284
```

depending on available space.

---

# 11. Spacing System

Use a 4dp base spacing grid.

```text
4   xs
8   sm
12  small
16  md
20  lg
24  xl
32  2xl
40  3xl
48  4xl
64  5xl
```

Recommended screen padding:

```text
Horizontal: 20dp
```

Minimum content gap:

```text
8dp
```

Section gap:

```text
24dp
```

Large section gap:

```text
32dp
```

---

# 12. Corner Radius System

Keep the UI rounded but not toy-like.

```text
Small      8dp
Medium     12dp
Card       16dp
Large      20dp
Hero       24dp
Pill       999dp
```

Recommended:

- cards: 16dp;
- hero card: 20–24dp;
- chips: pill;
- buttons: 12–14dp;
- bottom sheet: 24dp top corners.

Avoid 28–32dp radii on ordinary cards.

---

# 13. Borders and Elevation

## Dark mode

Prefer subtle borders over heavy shadows.

Card:

```text
Background: #101720
Border: #263241
1dp
```

Elevation:

```text
0–2dp
```

## Light mode

Use:

```text
Background: #FFFFFF
Border: #E4E9F0
Shadow: very soft
```

Avoid floating every component.

---

# 14. Iconography

Use a consistent outline icon family.

Recommended:

- Material Symbols Rounded
- 24dp default icon size.

Sizes:

```text
16dp   inline metadata
20dp   secondary actions
24dp   standard navigation
28dp   feature icon
32dp   large feature icon
```

Use filled icons only for selected navigation or strong states.

Icons should have visual weight similar to the typography around them.

---

# 15. App Shell

The application uses a modern Android screen structure:

```text
System Status Bar
        ↓
Top App Bar
        ↓
Content
        ↓
Optional Bottom Navigation
```

Respect Android safe areas and window insets.

The product specification explicitly requires respecting status bar and safe-area/insets.

---

# 16. Navigation Architecture

## Primary navigation

Recommended bottom navigation:

```text
Home
History
Rules
Analytics
```

Settings lives behind the top-right settings/menu path and does not need to become a fifth primary tab.

### Bottom Navigation

Dark:

```text
Background #101720
Border Top #263241
```

Light:

```text
Background #FFFFFF
Border Top #E4E9F0
```

Selected item:

```text
Blue icon
Blue label
```

Unselected:

```text
Tertiary gray
```

---

# 17. Home / Dashboard

## 17.1 Purpose

The Home screen is the user's quick control center.

It should answer:

- Is notification blocking active?
- What has been blocked?
- What important notifications remain protected?
- What are today's notification levels?
- Where can I control the system?

---

## 17.2 Header

Reference style:

```text
My Notification                         ⚙
Less noise. More you.
```

Layout:

```text
[App icon / menu]  My Notification     [Settings]
                   Less noise...
```

Title:

```text
18sp / SemiBold
```

Subtitle:

```text
12–13sp / Secondary
```

The header should remain compact.

---

# 18. Home Hero Card

The hero card is the main visual anchor.

Recommended concept:

```text
┌─────────────────────────────────┐
│                                 │
│            [ring]               │
│                                 │
│         Focused Mode            │
│             Active              │
│                                 │
│       [ Tap to disable ]        │
│                                 │
└─────────────────────────────────┘
```

However, the product is notification control rather than focus-only software. Therefore the hero should use terminology consistent with actual product behavior.

Recommended default:

```text
Notification Blocker
Active
```

Secondary metric:

```text
128 notifications blocked
```

The circular ring can represent:

- blocking intensity;
- percentage blocked;
- current active protection;
- or simply active state.

Do not imply a percentage if a real percentage is not calculated.

---

# 19. Home Summary Metrics

Three compact metric cards:

```text
128
Blocked

12
Allowed

86
Important
```

Alternative based on available data:

```text
1,284   Total
812     Blocked
326     Allowed
```

Card design:

```text
16dp radius
12–16dp internal padding
Equal width
```

Avoid too many metrics.

---

# 20. Home Feature Cards

Use three primary feature cards:

### Notification History

Icon:

```text
history / notifications
```

Text:

> View and search all notifications

Chevron:

```text
→
```

### Blocking Rules

Text:

> Apps, keywords, schedules

### Analytics

Text:

> See your notification patterns

The reference UI uses this compact list-card pattern. Preserve it across the app.

---

# 21. Home Notification Blocker

This is the main actionable control.

Header:

```text
Notification Blocker                 ON
```

When OFF:

```text
Notification Blocker                OFF
Notifications are currently allowed
```

When ON:

Show mode options:

```text
● Block All Social Media

○ Selected Apps

○ Block All

○ Custom Rules
```

Use radio-style selection for mutually exclusive modes.

Do not use five separate toggles for mutually exclusive modes.

---

# 22. Blocking Mode Card

Each mode should have:

```text
Icon
Title
One-line explanation
Selection state
```

Example:

```text
[Social Icon]

Block All Social Media
Instagram, Facebook, TikTok, YouTube

                         ●
```

Selected:

- blue border;
- blue radio;
- subtle blue-tinted surface.

Unselected:

- neutral border;
- neutral surface.

---

# 23. Notification History

## Screen purpose

A chronological internal history of all captured logical notification events.

The product requirement says blocked notifications should remain visible internally.

---

## 23.1 Header

```text
←   Notification History
```

Right:

```text
Filter
```

or

```text
⋮
```

---

## 23.2 Search

Full-width rounded search bar:

```text
⌕  Search notifications...
```

Dark:

```text
#151F2B
```

Light:

```text
#F1F4F8
```

Height:

```text
44dp
```

---

# 24. History Filter Chips

Recommended order:

```text
All
Allowed
Blocked
Important
```

Scrollable horizontally.

Selected:

```text
Blue background / blue-tinted surface
```

Unselected:

```text
Neutral surface
```

Additional filters live in a filter sheet:

- Social
- OTP
- Financial
- Promotional
- Ongoing
- App
- Date

Do not display all filters simultaneously.

---

# 25. Notification Row

Reference style:

```text
[App icon]  WhatsApp                       9:40 PM
            2 new messages                 [•••]
```

For blocked:

```text
[Instagram]  New reel from...
             9:12 PM        [Blocked]
```

For important:

```text
[bKash]      You have received ৳500
             8:45 PM        [Important]
```

Row spacing:

```text
12–16dp vertical
16dp horizontal
```

App icon:

```text
32–36dp
```

---

# 26. Notification Status Badges

### Allowed

Green text:

```text
Allowed
```

### Blocked

Muted red:

```text
Blocked
```

### Important

Blue or purple:

```text
Important
```

### OTP

Purple/security semantic.

### Financial

Green.

Never rely only on color. Include text or icon.

---

# 27. Notification Detail Screen

When a notification is tapped:

```text
← Notification
```

Hero section:

```text
[App Icon]
WhatsApp
Allowed
```

Then:

### Content

```text
Title
Text
Expanded Text
```

### Metadata

```text
Channel
Category
Importance
Notification ID
```

### Lifecycle

```text
First seen
Last updated
Removed
Duration
Update count
```

### Filtering

```text
Status: Blocked
Rule: Sleep Schedule
Matched: Global Block
```

This directly supports the specification's requirement to explain the responsible rule.

---

# 28. Blocking Rules Screen

Reference design:

```text
Blocking Rules                         +
```

Each rule is a card.

---

## Rule Card 01

```text
[Social Icon]   Block Social Media            ON
                Instagram, Facebook, TikTok
```

---

## Rule Card 02

```text
[Apps Icon]     Block Selected Apps           ON
                3 apps selected
```

---

## Rule Card 03

```text
[Shield Icon]   Allow Important Keywords      ON
                OTP, verification, payment...
```

---

## Rule Card 04

```text
[Clock Icon]    Schedule Blocking              ON
                10:00 PM – 7:00 AM
```

---

## Rule Card 05

```text
[Minus Icon]    Block Everything              OFF
                Except whitelisted apps
```

---

## Rule Card 06

```text
[Custom Icon]   Custom Rules                  →
                2 custom rules
```

---

# 29. Rule Card Interaction

Tap the entire card:

```text
Open rule
```

Tap switch:

```text
Enable / Disable only
```

Long press:

```text
Optional contextual actions
```

Avoid putting three tiny action icons inside every card.

---

# 30. Add Rule Flow

Floating/action button or top-right plus:

```text
+
```

Opens:

```text
Create Rule
```

First choose action:

```text
Block
Allow
```

Then conditions:

```text
App
Title
Text
Keyword
Category
Channel
Time
Day
Ongoing
OTP
Financial
```

Then preview:

```text
This rule will affect:
Instagram
16 notifications today
```

Then:

```text
Save Rule
```

---

# 31. Custom Rule Builder

The rule builder must be readable in plain language.

Instead of exposing technical syntax:

```text
WHEN
App = Instagram
AND
Text contains "urgent"
AND
Time = 10 PM–8 AM

THEN
BLOCK
```

Use stacked visual blocks.

---

# 32. Schedule Screen

Screen header:

```text
Schedules                              +
```

Schedule card:

```text
Sleep
10:00 PM – 8:00 AM
Every day

Block everything
Except OTP, calls, financial

                         ON
```

Second:

```text
Class
9:00 AM – 4:00 PM
Sun–Thu

Block social media

                         ON
```

---

# 33. Schedule Editor

Sections:

### Name

```text
Sleep
```

### Time

```text
Start     10:00 PM
End        8:00 AM
```

### Repeat

```text
Every day
Mon Tue Wed Thu Fri
```

### Action

```text
Block All
Block Social Media
Custom
```

### Exceptions

```text
Important Apps
OTP
Financial
Emergency
```

### Priority

Use simple numeric or drag ordering only if needed.

---

# 34. Protection Center

Protection should be a separate section/screen.

Cards:

```text
OTP Protection
Financial Notifications
Emergency Bypass
Important Notifications
```

Example:

```text
[Shield]

OTP Protection
Keep verification codes visible

                                  ON
```

---

# 35. OTP Protection UI

Hero:

```text
OTP Protection
Protected
```

Description:

> Verification codes matching trusted patterns can bypass blocking rules.

Patterns:

```text
OTP
Verification code
Security code
Authentication
```

Include:

```text
Add keyword
```

Do not imply that any message containing digits is automatically an OTP.

---

# 36. Financial Protection UI

Trusted financial apps:

```text
bKash
Nagad
Bank
Card / payment apps
```

Each:

```text
[App icon] bKash                    ON
```

Categories:

```text
Money received
Money sent
Transaction
Login
Security
Payment confirmation
```

Use a green security/protection treatment without making the screen look like a banking app.

---

# 37. Promotional SMS Filter

Header:

```text
Promotional SMS
```

Description:

> Reduce recurring marketing and package-offer notifications while preserving financial and security messages.

Example:

```text
Blocked
"Get 20GB for 7 days..."

Allowed
"Your OTP is 482913"

Allowed
"৳500 received..."
```

Use realistic examples to teach the feature.

---

# 38. Analytics Screen

The analytics UI should look like a lightweight professional dashboard.

Header:

```text
Analytics                       Last 7 days ▼
```

---

# 39. Analytics Hero

Primary:

```text
1,248
Total Notifications
```

Below:

```text
812  Blocked
326  Allowed
110  Important
```

Use three semantic colors with very small indicators.

---

# 40. Analytics Chart

Primary chart:

```text
Notifications per day
```

Simple vertical bars.

Avoid:

- gradients;
- 3D charts;
- excessive grid lines;
- overly saturated colors.

Recommended:

- blue main bars;
- faint grid;
- small axis labels;
- highlight selected day.

---

# 41. Time Range Selector

Supported product ranges:

```text
1 hour
5 hours
24 hours
Today
Yesterday
7 days
Custom
```

UI:

```text
Last 7 days ▼
```

opens a bottom sheet.

Selected range should persist during the session.

---

# 42. App Ranking

Card:

```text
Top Apps                              See all
```

Rows:

```text
Instagram    ████████████       320
Facebook     ████████           210
YouTube      ███████            180
WhatsApp     █████              120
Gmail        ███                 90
```

Each row:

- app icon;
- name;
- horizontal bar;
- count.

Use the app's icon only, not its brand color as the entire row background.

---

# 43. Analytics Insights

Use descriptive insights.

Examples:

```text
Instagram sent you 47 notifications today.

68% of your notifications came from 3 apps.

Most notifications arrived between 7 PM and 11 PM.

You blocked 82 promotional notifications this week.
```

Do not use judgmental language such as:

> You are addicted to notifications.

The product specification explicitly asks that insights remain descriptive rather than judgmental.

---

# 44. Smart Insight Card

Use a compact neutral card:

```text
Insight

68% of notifications came from
3 apps.

View details →
```

One insight at a time on Home.

Multiple insights on Analytics.

---

# 45. Settings Screen

The supplied visual reference uses a compact settings layout with grouped sections.

Header:

```text
Settings
```

---

# 46. Appearance Section

```text
Appearance

◐ Dark Mode
○ Light Mode
○ System Default
```

Preferred mobile UI:

A segmented control can replace radio rows:

```text
Dark | Light | System
```

But the reference uses individual rows, so radio rows are appropriate for the first version.

Selected:

- primary blue control;
- blue label/icon;
- subtle selected surface.

---

# 47. General Settings

Recommended:

```text
Notification Access        Granted
Battery Optimization       Ignore
Data & Privacy             →
Backup & Restore           →
About                      →
```

Status values are right-aligned.

Use:

```text
Granted
Enabled
Needs attention
```

Avoid technical wording where a friendlier label is possible.

---

# 48. Permissions Screen

Permission status should be extremely clear.

Example:

```text
Notification Access

Required to read notification events
and manage notification blocking.

                           ✓ Enabled
```

If disabled:

```text
✕ Disabled

My Notification cannot capture or manage
notifications until Notification Access
is enabled.

[Enable Access]
```

Never disguise a missing permission as a normal empty state.

---

# 49. Battery Optimization

The app depends on reliable background notification processing.

Card:

```text
Battery Optimization

For reliable notification monitoring,
allow My Notification to run without
aggressive background restrictions.

Status: Needs attention

[Fix]
```

Use a warning color only when action is actually needed.

---

# 50. Data & Privacy

Because notification contents are sensitive, make privacy visible.

Screen content:

```text
Your notification data stays on your device
by default.

Notification history
Local

Analytics
Local

Cloud sync
Off
```

Controls:

```text
Retention
7 days
30 days
90 days
180 days
1 year
Until manually deleted
```

Danger zone:

```text
Delete all notification history
```

The destructive action must require confirmation.

---

# 51. Privacy Confirmation

Dialog:

```text
Delete all notification history?

This will permanently remove your stored
notification history from this device.

[Cancel]     [Delete]
```

Delete is the destructive action.

Do not use a giant red modal.

---

# 52. Menu Screen

The product specification requires the hamburger menu to open a **new screen**, not an overlay.

Header:

```text
←   Menu                               ⋮
```

Sections:

### Notifications

```text
All Notifications
Notification Analytics
Blocked Notifications
Allowed Notifications
```

### Blocking

```text
Notification Blocker
Blocked Apps
Whitelisted Apps
Whitelisted Keywords
Custom Rules
Notification Categories
```

### Scheduling

```text
Schedules
Active Schedule
Schedule History
```

### Protection

```text
OTP Protection
Financial Notifications
Promotional SMS Filter
Important Notifications
```

### System

```text
Permissions
Notification Access
App Settings
Data & Storage
```

### Support

```text
Help
App Tutorial
Report a Problem
```

---

# 53. Three-dot Menu

Use native Android-style dropdown behavior.

Items:

```text
Help
Tutorial
Report a Problem
```

Do not use a custom giant modal.

Position near the three-dot button.

---

# 54. Search System

Global notification search:

```text
⌕ Search notifications...
```

Searchable fields:

- app;
- title;
- text;
- keyword;
- date;
- notification type.

Search results must retain the same notification row component as History.

---

# 55. Empty States

Empty states should be calm and instructional.

## No notifications

```text
No notifications yet

When notifications arrive,
they will appear here.

[Check Notification Access]
```

## No blocked notifications

```text
Nothing blocked

Your current rules haven't blocked
any notifications in this period.
```

## No search results

```text
No matching notifications

Try a different app, keyword, or date.
```

Never use an alarming icon.

---

# 56. Error States

## Notification access revoked

Use:

```text
Warning icon

Notification Access is off

History and blocking may not work
until access is restored.

[Enable]
```

---

## Android prevented suppression

```text
Android prevented this notification
from being fully suppressed.

Some calls, alarms, full-screen intents,
or OEM-specific notifications may behave
differently.
```

This is important because the product specification explicitly states that absolute universal blocking must not be promised.

---

# 57. Loading States

Prefer skeleton loading for analytics and history.

Example:

```text
██████████
██████
████████████████
```

Do not display spinners for every small operation.

Use progress indicators only for:

- initial database loading;
- long-running operation;
- data export/import;
- advanced processing.

---

# 58. Toggle Design

Switch dimensions should be Android-standard and compact.

State:

```text
ON:
Blue track
White thumb

OFF:
Neutral track
Gray/white thumb
```

Do not make giant iOS-style switches.

Every switch needs an adjacent label explaining what it controls.

---

# 59. Button Design

Primary:

```text
Background #4F6BFF
Text #FFFFFF
Height 48dp
Radius 12dp
```

Secondary:

```text
Transparent / Surface
Border
Height 44–48dp
```

Tertiary:

```text
Text button
```

Destructive:

```text
Neutral surface + red text
```

Only use a solid red button when necessary.

---

# 60. Chip Design

Height:

```text
32–36dp
```

Radius:

```text
999dp
```

Use chips for:

- All;
- Allowed;
- Blocked;
- Important;
- OTP;
- Financial;
- Social;
- Promotional.

---

# 61. Bottom Sheets

Use bottom sheets for:

- filters;
- time range;
- quick actions;
- schedule selection;
- temporary block duration.

Structure:

```text
Drag handle
Title
Options
Primary action if required
```

Sheet corners:

```text
24dp
```

Dark sheet:

```text
#151F2B
```

Light sheet:

```text
#FFFFFF
```

---

# 62. Dialogs

Use dialogs only for:

- destructive confirmation;
- irreversible settings;
- critical permission explanation.

Do not use dialogs for regular navigation.

---

# 63. Notification Detail Information Architecture

Detailed view:

```text
Notification
│
├── App
├── Status
├── Content
│   ├── Title
│   ├── Text
│   ├── Expanded Text
│   └── Subtext
│
├── Technical
│   ├── Channel
│   ├── Category
│   ├── Importance
│   ├── Flags
│   ├── Ongoing
│   ├── Clearable
│   └── Group
│
├── Lifecycle
│   ├── First Seen
│   ├── Last Updated
│   ├── Removed
│   ├── Duration
│   └── Update Count
│
└── Filtering
    ├── Status
    ├── Rule
    ├── Keyword
    ├── App
    └── Category
```

---

# 64. Logical Notification Event Presentation

The backend may receive many raw lifecycle callbacks for a single logical event.

The UI should show:

```text
Download complete

First seen
2:14 PM

Last updated
2:19 PM

Updates
300

Logical notification
1
```

Do not show 300 separate notifications.

This supports the product requirement that analytics should use logical notification events rather than raw callbacks.

---

# 65. Ongoing Notification UI

For calls, timers, downloads, playback and progress notifications:

Status:

```text
Ongoing
```

Add compact timeline:

```text
2:14 PM ───────────── 2:19 PM
```

Optional:

```text
300 updates
```

The notification still occupies one logical event.

---

# 66. Dark Mode Component Rules

Dark mode should not become "black + neon."

Use three surface levels:

```text
Background
#080D14

Surface
#101720

Elevated
#151F2B
```

Primary blue is the only strong recurring accent.

Semantic colors remain restrained.

---

# 67. Light Mode Component Rules

Light mode should not become "white everywhere."

Use:

```text
Background
#F7F9FC

Card
#FFFFFF

Soft Surface
#F1F4F8
```

Cards should be visually distinct through:

- background;
- border;
- tiny shadow.

Never rely on shadow alone.

---

# 68. Theme Parity Rules

Every component needs a Dark and Light token.

Example:

```text
Component: Card

Dark:
background #101720
border #263241
title #F5F7FA
body #A9B3C0

Light:
background #FFFFFF
border #E4E9F0
title #111827
body #5E6877
```

This should be implemented through semantic theme objects, not screen-specific hard-coded values.

---

# 69. Recommended Theme Tokens for Compose

Suggested conceptual structure:

```kotlin
data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceSoft: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val primary: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
    val security: Color
)
```

The exact implementation is flexible, but screens should consume semantic tokens.

---

# 70. Motion Design

Motion should be subtle.

Use:

```text
150–200ms
```

for micro-interactions.

Use:

```text
200–300ms
```

for screen/card expansion.

Recommended:

- card expansion;
- toggle state;
- filter selection;
- bottom sheet;
- page transition;
- analytics range switch.

Avoid:

- bouncing cards;
- excessive parallax;
- constant pulsing;
- decorative confetti;
- attention-seeking animations.

The product is intended to reduce interruption; the UI itself should not become an interruption.

---

# 71. Hero Ring Motion

The hero ring can have a subtle activation transition:

```text
Inactive → Active
```

Animate the ring once.

After activation:

- remain still;
- no continuous pulse.

This prevents the UI from feeling noisy.

---

# 72. Accessibility

Minimum targets:

```text
Touch target ≥ 48dp
```

Text should scale with Android accessibility settings.

Never rely only on color.

Examples:

```text
Blocked
```

not just:

```text
red dot
```

Support:

- TalkBack;
- large text;
- high contrast;
- reduced motion.

---

# 73. Accessibility Contrast

Primary text should maintain strong contrast.

Avoid:

```text
gray text on slightly lighter gray
```

particularly in dark mode.

Tertiary text can be lower contrast only when it remains readable under accessibility settings.

---

# 74. Content Writing Style

Tone:

- calm;
- direct;
- neutral;
- human;
- concise.

Preferred:

> Notification Access is off.

Not:

> ERROR!!! Permission Failure!!!

Preferred:

> 82 notifications blocked this week.

Not:

> Amazing! You defeated 82 distractions!!!

The product is a utility, not a gamified productivity app.

---

# 75. Microcopy Examples

### Blocking

```text
Block Social Media
Reduce notifications from selected social apps.
```

### Whitelist

```text
Allowed apps always bypass the global block.
```

### OTP

```text
Keep verification codes visible.
```

### Financial

```text
Protect transaction and account-security alerts.
```

### Schedule

```text
Silence notifications during selected times.
```

### Privacy

```text
Notification history stays on this device by default.
```

---

# 76. Onboarding

Keep onboarding to the minimum necessary steps.

## Step 1 — Introduction

```text
Less noise.
More control.

My Notification helps you manage
what reaches you.
```

CTA:

```text
Get Started
```

---

## Step 2 — Notification Access

Explain exactly why the permission is needed.

```text
See your notification history
Manage notification blocking
Understand notification patterns
```

CTA:

```text
Enable Notification Access
```

---

## Step 3 — Default Strategy

Choice:

```text
Block Social Media
Selected Apps
Block Everything
Allow Everything
```

The user must explicitly choose.

---

## Step 4 — Important Apps

Select:

```text
WhatsApp
Phone
Gmail
bKash
Nagad
Bank
```

Do not automatically whitelist apps without consent.

---

## Step 5 — Protection

```text
OTP Protection       ON
Financial Protection ON
```

Explain each in one line.

---

## Step 6 — Schedule

Optional.

```text
Set a schedule
Skip for now
```

Do not force advanced configuration.

---

# 77. First-Run Home

After onboarding, Home should feel complete immediately.

Show:

```text
Notification Blocker
Active

3 important protections enabled

0 notifications blocked today
```

Avoid an empty dashboard with no explanation.

---

# 78. Blocked Notification Digest

When a user enters the app after many notifications were blocked:

```text
While you were away

42 notifications were blocked

Social Media       27
Promotional SMS     9
Other               6

[Review blocked notifications]
```

This should be informational, not addictive.

---

# 79. Temporary Block

Entry point:

```text
Block notifications temporarily
```

Options:

```text
15 min
30 min
1 hour
2 hours
Until tomorrow
Custom
```

After activation:

```text
Blocked until 6:30 PM
[Change]
[End now]
```

---

# 80. Emergency Bypass

The UI should clearly separate emergency bypass from normal whitelist.

```text
Emergency Bypass
Allow selected emergency apps/contacts
to interrupt active blocking periods.
```

Use a shield icon.

---

# 81. Conflict Handling

When rules conflict, the UI should explain it.

Example:

```text
Conflict detected

Global Block is active,
but WhatsApp is whitelisted.

Result:
WhatsApp notifications will be allowed.

View rule priority →
```

Never silently resolve confusing conflicts without explanation.

---

# 82. Rule Priority Visualization

A compact screen can show:

```text
1  Emergency / system critical
2  App whitelist
3  Keyword whitelist
4  Schedule
5  Explicit block
6  Category
7  Global block
8  Default policy
```

This is especially useful for advanced users.

---

# 83. Notification Category Screen

Example:

```text
Social Media       8 apps
Financial          4 apps
Messaging          6 apps
Work               5 apps
Shopping           9 apps
Other               21
```

Users can add/remove apps from categories.

The UI must not make categories permanent or hidden.

---

# 84. App Picker

Requirements:

- searchable;
- app icon;
- app name;
- selected state;
- category if useful.

Example:

```text
Search apps...

☑ Instagram
☐ WhatsApp
☐ Gmail
☑ TikTok
☐ YouTube
```

Use checkbox selection for multi-select.

---

# 85. Whitelisted Apps

Screen:

```text
Whitelisted Apps                    +

WhatsApp
Always allowed

Phone
Always allowed

Gmail
Important alerts
```

Use short reason text.

---

# 86. Whitelisted Keywords

Rows:

```text
OTP
verification
security code
transaction
payment
received
credited
debited
login
authentication
```

Each item:

```text
Keyword
Used in 32 allowed notifications
```

---

# 87. Data Retention Screen

Use a simple selection list:

```text
Keep notification history

○ 7 days
○ 30 days
● 90 days
○ 180 days
○ 1 year
○ Until manually deleted
```

Optional advanced:

```text
Keep forever
```

Explain storage implications.

---

# 88. Storage Usage

Show:

```text
Notification History
184 MB

Events
48,392

Retention
90 days

[Manage Storage]
```

Do not overemphasize technical database metrics.

---

# 89. Import / Export

If implemented:

```text
Backup & Restore

Export rules
Export settings
Export notification history

Restore backup
```

Cloud sync must be explicitly opt-in because notification content is sensitive.

---

# 90. Help Screen

Short topic cards:

```text
How notification access works
Why a notification wasn't blocked
Why Android can prevent suppression
How to create a rule
How OTP protection works
How to protect financial notifications
```

Use simple language.

---

# 91. Tutorial

Interactive tutorial should use the actual UI.

Recommended steps:

```text
1. Notification Blocker
2. Notification History
3. Blocking Rules
4. Analytics
5. Protection
```

Do not create a long carousel.

---

# 92. Report a Problem

Form:

```text
What happened?

[Describe the problem]

Screen
[Optional]

Device
[Auto-detected]

Android version
[Auto-detected]
```

CTA:

```text
Submit
```

Never attach notification contents automatically without explicit user action.

---

# 93. Home Screen Responsive Layout

Phone width should remain the primary target.

For normal Android phones:

```text
Horizontal padding: 20dp
```

For wider devices:

```text
max content width: 600–720dp
center content
```

Tablet layouts may switch to a two-column arrangement.

---

# 94. Tablet / Large Screen

Recommended:

```text
Navigation Rail
        +
Main content
```

For Analytics:

```text
┌──────────────┬─────────────────────────────┐
│              │                             │
│ Navigation   │ Analytics                  │
│              │ Charts / Apps / Insights   │
│              │                             │
└──────────────┴─────────────────────────────┘
```

Do not simply stretch the phone layout to full tablet width.

---

# 95. Component Inventory

The design system should contain these reusable components:

```text
AppTopBar
BottomNavigation
HeroCard
MetricCard
FeatureRowCard
RuleCard
NotificationRow
StatusChip
FilterChip
SearchBar
AppPickerRow
AppIcon
ToggleRow
RadioRow
SettingRow
SectionHeader
AnalyticsCard
BarChart
InsightCard
EmptyState
ErrorState
PermissionCard
BottomSheet
ConfirmDialog
RuleBuilder
ScheduleCard
ProtectionCard
```

---

# 96. Component Naming

Use semantic names instead of screen-specific names.

Bad:

```text
HomeInstagramCard
```

Good:

```text
NotificationRow
```

Bad:

```text
DarkBlueToggle
```

Good:

```text
AppSwitch
```

Theme logic belongs inside tokens, not component names.

---

# 97. Screen Inventory — Full App

The complete UI should cover:

### Primary

```text
01 Home
02 Notification History
03 Notification Detail
04 Blocking Rules
05 Create/Edit Rule
06 Schedules
07 Schedule Editor
08 Analytics
09 Settings
10 Menu
```

### Protection

```text
11 OTP Protection
12 Financial Protection
13 Promotional SMS Filter
14 Important Notifications
15 Emergency Bypass
```

### Management

```text
16 Blocked Apps
17 Whitelisted Apps
18 Whitelisted Keywords
19 Notification Categories
20 App Picker
21 Rule Priority
22 Search
23 Data Retention
24 Storage Management
```

### System

```text
25 Permissions
26 Notification Access
27 Battery Optimization
28 Data & Privacy
29 Backup & Restore
30 About
```

### Support

```text
31 Help
32 Tutorial
33 Report a Problem
```

---

# 98. Screen Relationship Map

```text
Home
 ├── Notification History
 │    └── Notification Detail
 │
 ├── Blocking Rules
 │    ├── Create/Edit Rule
 │    ├── Blocked Apps
 │    ├── Whitelisted Apps
 │    ├── Whitelisted Keywords
 │    ├── Notification Categories
 │    └── Rule Priority
 │
 ├── Analytics
 │    └── App Details
 │
 └── Settings
      ├── Permissions
      ├── Protection
      │    ├── OTP
      │    ├── Financial
      │    └── Promotional SMS
      ├── Data & Privacy
      ├── Backup & Restore
      └── About
```

---

# 99. Dark Mode — Screen-by-Screen Visual Direction

## Home

Background:

```text
#080D14
```

Hero:

```text
#101720
```

Ring:

```text
#4F6BFF
```

Text:

```text
#F5F7FA
```

Secondary:

```text
#A9B3C0
```

Cards have subtle borders.

---

## History

Use slightly elevated cards against the background.

Search:

```text
#151F2B
```

Blocked badge:

```text
Error dark surface
Muted red text
```

Allowed:

```text
Success dark surface
```

Important:

```text
Blue / purple accent
```

---

## Blocking Rules

Each rule card remains neutral.

Only the active state receives blue emphasis.

Do not make every rule colorful.

---

## Analytics

Dark chart background should remain the same as card surface.

Bars:

```text
Primary blue
```

Secondary semantic metrics use restrained colors.

---

## Settings

Most rows are neutral.

Permission states receive small status colors.

Do not color entire settings sections.

---

# 100. Light Mode — Screen-by-Screen Visual Direction

## Home

Background:

```text
#F7F9FC
```

Hero:

```text
#FFFFFF
```

Border:

```text
#E4E9F0
```

Blue ring remains the visual focus.

---

## History

White notification rows on a soft gray page.

Blocked badge:

```text
#FDE5E7
```

Allowed:

```text
#DDF8EC
```

Important:

```text
Blue tinted
```

---

## Blocking Rules

Cards:

```text
#FFFFFF
```

Background:

```text
#F7F9FC
```

Switches use the primary blue.

---

## Analytics

Charts use clean white surfaces and subtle grid lines.

Numbers stay dark.

Blue remains the strongest accent.

---

## Settings

White rows.

Subtle separators.

Blue radio selection.

Status chips remain semantic.

---

# 101. Visual Density

Target density:

> Medium-light.

The user should be able to scan a screen quickly.

Home:

```text
~5–7 major visual blocks
```

History:

```text
~6–10 notification rows
```

Rules:

```text
~5–7 rule cards
```

Analytics:

```text
Hero
Chart
Top Apps
Insights
```

Settings:

```text
3–6 grouped sections
```

Do not turn each screen into a giant settings spreadsheet.

---

# 102. Do / Don't

## Do

- Use whitespace.
- Keep titles short.
- Use one primary accent.
- Use semantic state colors.
- Explain rule decisions.
- Keep advanced settings deeper.
- Preserve theme parity.
- Use real notification examples.
- Make privacy visible.

## Don't

- Use neon gradients everywhere.
- Put every feature on Home.
- Make every card colorful.
- Use giant toggles.
- Hide rule logic.
- Promise 100% Android notification blocking.
- Make every event look unique when it is an update.
- Upload sensitive notification content by default.
- Use judgmental analytics language.

---

# 103. Reference Image Mapping

The supplied visual reference establishes five core screen patterns in Dark and Light:

```text
1. Home / Dashboard
2. Notification History
3. Blocking Rules
4. Analytics
5. Settings
```

The full application should preserve these five patterns as the visual anchor.

Expansion screens should inherit the same:

- top bar;
- typography;
- card radius;
- spacing;
- icon language;
- chips;
- switches;
- semantic states;
- theme tokens.

Do not redesign every new screen independently.

---

# 104. Design QA Checklist

Before shipping each screen:

### Layout

- [ ] Respects status bar/insets.
- [ ] 20dp horizontal content padding.
- [ ] Consistent 4dp spacing grid.
- [ ] No accidental clipping.
- [ ] Bottom navigation does not cover content.

### Typography

- [ ] One clear title.
- [ ] Secondary text has lower emphasis.
- [ ] Numbers use consistent formatting.
- [ ] No unnecessary bold text.

### Color

- [ ] Uses semantic tokens.
- [ ] No hard-coded screen-specific theme colors.
- [ ] Dark mode avoids pure black.
- [ ] Light mode avoids pure-white-on-white without separation.
- [ ] Red used only for relevant states.

### Interaction

- [ ] Touch targets are at least 48dp.
- [ ] Switches have clear labels.
- [ ] Selection state is visible.
- [ ] Error states include next action.
- [ ] Destructive actions require confirmation.

### Accessibility

- [ ] TalkBack labels exist.
- [ ] Color is not the only status signal.
- [ ] Large text does not break layouts.
- [ ] Reduced motion is respected.

---

# 105. UX QA Checklist

Test these flows end-to-end:

```text
Onboarding
→ Notification Access
→ Home
→ Enable Blocker
→ Block Social Media
→ Receive notification
→ Notification History
→ Notification Detail
→ Why blocked
```

Then:

```text
Global Block
→ Add WhatsApp whitelist
→ Receive WhatsApp notification
→ Verify Allowed
```

Then:

```text
Global Block
→ OTP whitelist
→ Receive OTP
→ Verify Allowed
```

Then:

```text
Schedule
→ Active period
→ Notification blocked
→ End schedule
→ Notification allowed
```

Then:

```text
Notification update
→ Multiple callbacks
→ One logical event
→ Analytics count = 1
```

---

# 106. Design System Summary

## Brand feel

```text
Calm
Minimal
Professional
Private
Trustworthy
Technical without being intimidating
```

## Primary visual hierarchy

```text
1. State
2. Important metric
3. Main action
4. Supporting information
5. Advanced configuration
```

## Core theme identity

```text
Dark:
#080D14
#101720
#151F2B
#4F6BFF

Light:
#F7F9FC
#FFFFFF
#F1F4F8
#4F6BFF
```

## Primary product statement

> Block what the user does not need, preserve what matters, and accurately record what actually happened.

This principle should be visible in the UX architecture even when it is not written on screen.

---

# 107. Final Implementation Direction

The supplied reference image should be treated as the **visual north star**, not as a literal pixel-perfect template.

Keep:

- minimal dashboard;
- compact top bar;
- soft cards;
- blue primary accent;
- status chips;
- clean analytics;
- professional settings;
- strong dark/light parity.

Expand it into a complete product system with:

- notification history;
- notification detail;
- blocking rules;
- custom rules;
- schedules;
- OTP protection;
- financial protection;
- promotional SMS filtering;
- notification categories;
- analytics;
- search;
- privacy;
- permissions;
- data retention;
- help;
- tutorial;
- support.

The final implementation should feel like **one coherent Android product**, not a collection of independently designed screens.
