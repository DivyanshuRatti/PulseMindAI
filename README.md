# PulseMind AI 
### *Because Your Health Matters*

PulseMind AI is an Android health companion app built with Kotlin that helps users track medications, check symptoms using AI, find nearby clinics, and manage health emergencies — all in one place.

---

##  Features

### Authentication
- User registration with name, email and password
- Login with email and password validation
- Per-user data isolation — each user sees only their own data
- Secure logout clears all session data

###  Dashboard
- Dynamic greeting based on time of day (Good Morning / Afternoon / Evening)
- Real-time steps counter — tap to update daily steps
- Mood tracker — tap to cycle through mood emojis
- Live medication reminders count from database
- Medication reminder popup on app open
- Profile initials displayed in top right corner

###  AI Symptom Checker
- Powered by **Groq API** with **Llama 3.3 70B** model
- User describes symptoms and selects severity (Mild / Moderate / Severe)
- AI provides possible causes, recommended action, and urgency level
- Loading state while AI processes
- Medical disclaimer included in results

###  Nearby Clinics
- Real GPS location using FusedLocationProviderClient
- Google Maps integration with live map display
- Google Places API searches hospitals within 5km
- Shows clinic name, address, rating, open/closed status
- Get Directions button opens Google Maps navigation

###  Medication Tracker
- Add medications with name, dosage and time picker
- SQLite database for persistent storage
- Per-user medication filtering
- Delete medications with confirmation dialog
- Toggle reminders on/off per medication
- Live medication count display

###  Emergency SOS
- Large SOS button that dials 911
- Pulse animation on button press

###  Health Reports
- Upload health documents (PDF, JPG, PNG)
- View uploaded reports in app
- Delete reports
- Shows medication count summary

###  Profile
- Displays real user name and email from registration
- Shows today's steps and medication count
- Health goals summary
- Logout with session clearing

---

##  Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin |
| IDE | Android Studio |
| Min SDK | 25 (Android 7.1) |
| Target SDK | 36 |
| AI API | Groq API (Llama 3.3 70B) |
| Maps | Google Maps SDK for Android |
| Places | Google Places API |
| Location | FusedLocationProviderClient |
| Database | SQLite (via SQLiteOpenHelper) |
| Storage | SharedPreferences |
| UI | ConstraintLayout, CardView, Material Design |

---

##  Project Structure

```
app/src/main/java/com/example/pulsemindai/
├── MainActivity.kt              # Splash screen with typewriter animation
├── LoginPage.kt                 # Login with validation
├── RegisterActivity.kt          # User registration
├── DashboardActivity.kt         # Main hub with dynamic stats
├── SymptomCheckerActivity.kt    # Groq AI symptom analysis
├── NearbyClinicsActivity.kt     # Google Maps + Places API
├── MedicationActivity.kt        # SQLite medication tracker
├── MedicationDBHelper.kt        # SQLite database helper
├── Medication.kt                # Medication data class
├── SOSActivity.kt               # Emergency SOS
├── ReportsActivity.kt           # Health report upload
└── ProfileActivity.kt           # User profile + logout

app/src/main/res/layout/
├── activity_main.xml            # Splash screen
├── activity_login_page.xml      # Login screen
├── activity_register.xml        # Register screen
├── activity_dashboard.xml       # Dashboard
├── activity_symptom_checker.xml # Symptom checker
├── activity_nearby_clinics.xml  # Clinics with map
├── activity_medication.xml      # Medication list
├── activity_sos.xml             # SOS screen
├── activity_reports.xml         # Reports screen
└── activity_profile.xml         # Profile screen
```

---

##  Getting Started

### Prerequisites
- Android Studio 
- Android SDK 36
- Groq API key (free at console.groq.com)
- Google Maps API key (Google Cloud Console)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/DivyanshuRatti/PulseMindAI.git
cd PulseMindAI
git checkout feature/dashboard-and-login
```

2. **Add API keys to `local.properties`**
```
GROQ_API_KEY=your_groq_api_key_here
MAPS_API_KEY=your_google_maps_api_key_here
```

3. **Enable Google APIs**
   - Go to Google Cloud Console
   - Enable **Maps SDK for Android**
   - Enable **Places API**

4. **Open in Android Studio**
   - File → Open → select PulseMindAI folder
   - Wait for Gradle sync to complete

5. **Run the app**
   - Select emulator or connected Android device
   - Click the green Run button

---

##  API Keys Setup

### Groq API (Free)
1. Sign up at [console.groq.com](https://console.groq.com)
2. Go to API Keys → Create API Key
3. Copy key starting with `gsk_...`
4. Add to `local.properties`: `GROQ_API_KEY=gsk_...`

### Google Maps API
1. Go to [console.cloud.google.com](https://console.cloud.google.com)
2. Create project → Enable Maps SDK for Android + Places API
3. Go to Credentials → Create API Key
4. Restrict key to Android apps with package `com.example.pulsemindai`
5. Add to `local.properties`: `MAPS_API_KEY=AIza...`

---

##  Team

| Name | Role |
|---|---|
| Divyanshu Ratti | UI/UX / Integration |
| Nikita Gupta | Backend Logic |
|Racchpal Sidhu |  Database Integration | 

**Course:** INFO 3245 — Mobile Programming
**Institution:** Kwantlen Polytechnic University
**Semester:** Spring 2026

---

##  Future Features

- Push notifications for medication reminders at exact times
- Firebase cloud sync for data persistence across devices
- Biometric login (fingerprint / face ID)
- AI health tips based on medication history
- Heart rate and sleep tracking integration
- Prescription photo scanning with OCR
- Family account linking
- Weekly health summary reports
- Dark mode support
- Multi-language support

---


