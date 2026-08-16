# MileagePay — Driver Mileage & Pay Tracker

**MileagePay** is a fast, lightweight Android app built for delivery and contract drivers whose pay is calculated directly from distance driven. It replaces messy paper logbooks with automated odometer calculations, Friday–Thursday weekly cheque grouping, and 100% offline local storage.

Built with **Kotlin**, **Jetpack Compose (Material 3)**, and **Room SQLite**.

---

## Key Features

- **Fast Odometer Logging**: Enter Start and End KM to see live distance and earnings calculated instantly.
- **Auto Start-KM Prefill**: Automatically pre-fills today's Start KM from your previous trip's End KM.
- **Friday to Thursday Pay Cycles**: Automatically groups daily trips into standard Friday–Thursday pay periods.
- **Cheque Settlement Status**: Toggle weekly totals between `Paid` and `Unpaid` with a single tap.
- **Quick Route Chips**: Tap quick destination tags (e.g., *Colombo Fort*, *Kandy Road*, *Peliyagoda*) to build route notes effortlessly.
- **Off-Day Toggle**: Quickly mark off days with a "No Work Today" switch.
- **Trip History & Log**: View, search, or delete past logs with safety confirmation dialogs that recalculate weekly cheque totals.
- **Local Backup & Restore**: Backup and restore your trip data locally via JSON or export to CSV.

---

## Business Rules & Calculations

- **Default Pay Rate**: **Rs. 104 per KM** (easily configurable in Settings).
- **Daily Calculation**: `Total KM = End KM - Start KM` | `Earnings = Total KM × Rate`.
- **Validation**: End KM must be greater than or equal to Start KM.
- **Weekly Cycle**: Runs strictly from **Friday (00:00)** through **Thursday (23:59)**.

---

## Database Architecture

The app uses Room (SQLite) with 100% local persistence:

- **`daily_trips`**: Stores daily entries, odometer readings, total KM, earnings, route text, notes, and off-day flags.
- **`weekly_cheques`**: Stores persistent settlement status (`is_paid`, `paid_at`, notes) for each Friday–Thursday week.

---

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3 Dynamic Color)
- **Database**: Room (SQLite)
- **Architecture**: MVVM + Coroutines & Flow
- **Compatibility**: Android 10+ (API 29+), targeted & optimized for Google Pixel 7 (Android 14+)

---

## How to Build

1. Clone the repository:
   ```bash
   git clone https://github.com/Iksura-Thilakarathna/DailyRunner.git
   cd DailyRunner
   ```

2. Build the Debug APK using Gradle:
   ```bash
   ./gradlew assembleDebug
   ```

The compiled APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.
