# 🛞 MileagePay — Driver Mileage & Pay Ledger

An offline-first, high-performance Android mileage and weekly pay tracking application built with **Kotlin** and **Jetpack Compose (Material 3)**.

Designed specifically for hire and delivery drivers working on contract rates (e.g., George Steuart & Co. delivery routes), replacing traditional paper logbooks with automated start/end odometer calculations, Friday–Thursday weekly cheque grouping, and zero-latency local storage.

---

## ✨ Features

- **⚡ Instant Daily Logging:** Enter Start and End KM with live calculation previews ($\text{KM} \times \text{Rate}$).
- **🔄 Auto-Prefill:** Automatically sets today's Start KM from the previous day's logged End KM.
- **📅 Strict Weekly Pay Cycles:** Groups all records into standard **Friday to Thursday** pay periods.
- **💳 Cheque Settlement Tracking:** Toggle weekly totals between `Paid` and `Unpaid` with timestamp tracking.
- **🛡️ Guarded Record Management:** Quick edits for Today/Yesterday; confirmation-guarded deletions for historical logs with automatic cheque recalculations.
- **🔒 100% Offline & Private:** Built on Room SQLite with zero cloud dependencies or login requirements.
- **💾 Local Backup & Restore:** Export and import complete logs via JSON/CSV directly to device storage.
- **☀️ Driver-Centric UX:** High-contrast Material 3 interface, dynamic color support, large touch targets (minimum 56dp), and numeric-first inputs optimized for in-vehicle use.

---

## 🛠️ Tech Stack & Architecture

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3 Dynamic Color)
- **Architecture:** Clean Architecture + MVVM + Unidirectional Data Flow (UDF)
- **Database:** [Room Persistence Library](https://developer.android.com/training/data-storage/room) (SQLite)
- **Asynchronous / Reactive:** Kotlin Coroutines & `StateFlow`
- **Target OS:** Android 14+ (API Level 34+), optimized for Google Pixel 7 (90Hz+ displays)

---

## 📐 Business Logic & Formulas

### Daily Calculations
$$\text{Daily Distance (KM)} = \text{End KM} - \text{Start KM}$$
$$\text{Daily Earnings (LKR)} = \text{Daily Distance} \times \text{Configured Rate (Default: Rs. 104/KM)}$$

*Constraint: $\text{End KM} \ge \text{Start KM}$ must evaluate to `true` to commit entry.*

### Pay Cycle Definition
- **Cycle Start:** Friday (00:00:00)
- **Cycle End:** Thursday (23:59:59)
- **Cheque Total:** Sum of daily earnings within the Friday–Thursday cycle window.

---

## 🗄️ Database Architecture (Room SQLite)

### Table 1: `daily_trips`
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String (UUID) | Primary Key |
| `date` | String | ISO Date (`YYYY-MM-DD`, Unique Index) |
| `start_km` | Double | Starting Odometer reading |
| `end_km` | Double | Ending Odometer reading |
| `total_km` | Double | Computed ($\text{End KM} - \text{Start KM}$) |
| `rate_per_km` | Double | Rate applied (Default: 104.0) |
| `total_earnings` | Double | Computed ($\text{Total KM} \times \text{Rate}$) |
| `destinations` | String | Delivery route text with quick chips |
| `notes` | String? | Optional notes |
| `is_no_work` | Boolean | Off day indicator |
| `created_at` | Long | Creation epoch timestamp |
| `updated_at` | Long | Modification epoch timestamp |

### Table 2: `weekly_cheques`
| Field | Type | Description |
| :--- | :--- | :--- |
| `week_id` | String | Primary Key (e.g. `2026-08-08_2026-08-14`) |
| `start_date` | String | Friday ISO date |
| `end_date` | String | Thursday ISO date |
| `is_paid` | Boolean | Settlement flag (`Default: false`) |
| `paid_at` | Long? | Payment timestamp |
| `notes` | String? | Optional cheque/bank notes |

---

## 🚀 Building & Running

### Prerequisites
- JDK 17+
- Android SDK Platform 34 (Android 14+)
- Gradle 8.2+

### Command Line Build
```bash
# Clone the repository
git clone https://github.com/Iksura-Thilakarathna/DailyRunner.git
cd DailyRunner

# Build Debug APK
./gradlew assembleDebug
```

The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🔗 Repository
[GitHub Repository: Iksura-Thilakarathna/DailyRunner](https://github.com/Iksura-Thilakarathna/DailyRunner.git)
