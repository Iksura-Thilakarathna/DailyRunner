#  DailyRunner — Driver Mileage & Pay Tracker

**DailyRunner** is an Android application designed to help delivery and contract drivers record their daily driving distance and calculate their earnings based on the number of kilometres they travel.

The main purpose of this application is to replace manual paper-based logbooks with a simple digital solution. Users can enter their starting and ending odometer readings, calculate their daily earnings, view previous trip records, and track their weekly payments.

The application was developed using **Kotlin**, **Jetpack Compose**, and **Room Database (SQLite)**.

---

##  Key Features

* **Odometer Logging**
  Users can enter their Start KM and End KM to calculate the total distance and earnings.

* **Automatic Start KM**
  The Start KM can be automatically filled using the End KM from the user's previous trip.

* **Friday–Thursday Pay Cycle**
  Daily trips are automatically grouped into weekly payment periods from Friday to Thursday.

* **Payment Status Tracking**
  Users can mark a weekly payment as **Paid** or **Unpaid**.

* **Quick Route Selection**
  Users can select frequently used destinations such as Colombo Fort, Kandy Road, and Peliyagoda to make route recording easier.

* **Off-Day Option**
  Users can mark a day as an off day by selecting the **"No Work Today"** option.

* **Trip History**
  Users can view their previous trip records and delete records when necessary.

* **Weekly Earnings Tracking**
  The application calculates and displays the total earnings for each payment cycle.

* **Local Backup and Restore**
  Users can back up their trip information locally and restore it when required.

* **CSV Export**
  Trip information can be exported as a CSV file for further use.

---

##  Business Rules and Calculations

DailyRunner uses simple rules to calculate the driver's earnings.

### Default Pay Rate

The default payment rate is:

**Rs. 104 per KM**

The rate can be changed through the application settings.

### Daily Distance Calculation

```text
Total KM = End KM - Start KM
```

### Earnings Calculation

```text
Earnings = Total KM × Pay Rate
```

### Validation

The application checks that the **End KM is greater than or equal to the Start KM**.

For example:

```text
Start KM = 25,000
End KM   = 25,150

Total KM = 150 KM
```

If the rate is Rs. 104 per KM:

```text
Earnings = 150 × 104
         = Rs. 15,600
```

### Weekly Payment Cycle

DailyRunner uses a fixed weekly payment cycle:

**Friday 00:00 → Thursday 23:59**

All trips recorded during this period are included in the same weekly payment calculation.

---

##  Database

DailyRunner uses **Room Database**, which is built on top of SQLite, to store the application data locally.

The application mainly uses two database tables:

### `daily_trips`

This table stores information about individual daily trips, including:

* Trip date
* Start KM
* End KM
* Total KM
* Earnings
* Route
* Notes
* Off-day status

### `weekly_cheques`

This table stores information about weekly payment settlements, including:

* Payment cycle
* Paid/Unpaid status
* Payment date and time
* Additional notes

All data is stored locally on the user's device, so the application can work without requiring an internet connection.

---

##  Technology Stack

| Technology           | Purpose                          |
| -------------------- | -------------------------------- |
| **Kotlin**           | Main programming language        |
| **Jetpack Compose**  | Building the user interface      |
| **Material 3**       | UI components and design         |
| **Room Database**    | Local data storage               |
| **SQLite**           | Database technology used by Room |
| **MVVM**             | Application architecture         |
| **Coroutines**       | Asynchronous programming         |
| **Flow / StateFlow** | Reactive data handling           |
| **Gson**             | JSON backup and restore          |
| **Gradle**           | Project build system             |
| **KSP**              | Room code generation             |

### Android Compatibility

* **Minimum SDK:** Android 8.0 (API 26)
* **Compile SDK:** Android 14 (API 34)
* **Target SDK:** Android 14 (API 34)
* **Java:** JVM 17

---

##  Application Architecture

DailyRunner follows the **MVVM (Model-View-ViewModel)** architecture pattern.

The main layers of the application are:

### UI Layer

The UI is developed using **Jetpack Compose**. It displays information such as daily trips, earnings, payment cycles, and settings.

### ViewModel Layer

The ViewModel handles the application's business logic and manages the UI state. It also communicates with the repository and performs operations using Kotlin Coroutines.

### Repository Layer

The Repository acts as a connection between the ViewModel and the database. It provides a clean way for the application to access and modify data.

### Data Layer

The Data Layer contains the Room database, entities, and DAOs used to store and retrieve trip and payment information.

The application uses **Flow** and **StateFlow** to observe changes in the database and update the user interface automatically.

---

##  Project Structure

```text
DailyRunner/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/dailyrunner/drivertracker/
│   │       │   └── Kotlin source files
│   │       ├── res/
│   │       │   └── Android resources
│   │       └── AndroidManifest.xml
│   │
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── gradle/
│   └── Gradle wrapper and configuration files
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```

---

##  Getting Started

### Prerequisites

Before running the project, make sure the following are installed:

* **Android Studio**
* **JDK 17**
* **Android SDK 34**
* **Kotlin 1.9+**
* Android Emulator or a physical Android device

### Clone the Repository

Open the terminal and run:

```bash
git clone https://github.com/Iksura-Thilakarathna/DailyRunner.git
```

Then move into the project directory:

```bash
cd DailyRunner
```

Open the project in **Android Studio** and allow Gradle to sync the project.

---

##  Running the Application

After opening the project in Android Studio:

1. Connect an Android device or start an Android Emulator.
2. Make sure USB debugging is enabled if you are using a physical device.
3. Select the device from Android Studio.
4. Click the **Run ** button.
5. The DailyRunner application will be installed and launched.

The application can also be installed using the Gradle command:

```bash
./gradlew installDebug
```

For Windows:

```bash
gradlew.bat installDebug
```

---

##  Building the Project

### Build Debug APK

```bash
./gradlew assembleDebug
```

The generated APK will normally be available at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Build Release APK

```bash
./gradlew assembleRelease
```

The generated release APK will normally be available at:

```text
app/build/outputs/apk/release/app-release.apk
```

---

##  How the Application Works

The basic workflow of DailyRunner is:

```text
Enter Start KM
       ↓
Enter End KM
       ↓
Calculate Total KM
       ↓
Calculate Earnings
       ↓
Save Trip
       ↓
Add Trip to Weekly Cycle
       ↓
Calculate Weekly Earnings
       ↓
Mark Payment as Paid/Unpaid
```

For a new trip, the user enters the odometer readings and other relevant information. The application calculates the distance and earnings automatically and stores the information in the local database.

At the end of the payment cycle, the user can view the total amount earned and update the payment status.

---

##  Data Storage

DailyRunner mainly uses **local storage** to keep user information.

This provides several advantages:

* Data can be accessed without an internet connection.
* Trip information is stored directly on the device.
* The application can work offline.
* Users can create local backups of their data.
* Trip records can be restored when needed.

---

##  Future Enhancements

The following features could be added in future versions:

*  Cloud synchronization
*  Multi-device data synchronization
*  Receipt photo attachments
*  Weekly payment reminders and notifications
*  Trip and earnings analytics
*  Weekly and monthly earnings charts
*  Improved export options for accounting software
*  Enhanced dark mode
*  Support for multiple vehicles
*  User authentication and secure cloud backup

---

##  Academic Purpose

DailyRunner was developed as an **undergraduate software project** to apply software development concepts in a practical real-world scenario.

The project demonstrates the use of:

* Android application development
* Kotlin programming
* Jetpack Compose
* MVVM architecture
* Local database management
* CRUD operations
* Data validation
* State management
* Kotlin Coroutines and Flow
* Backup and restore functionality
* Software design and development practices

The project also provides an example of how a simple software solution can be used to improve a manual business process such as driver mileage and payment tracking.

---

##  Project

**Project Name:** DailyRunner
**Project Type:** Android Application
**Platform:** Android
**Language:** Kotlin
**Database:** Room / SQLite
**UI:** Jetpack Compose + Material 3
**Architecture:** MVVM
