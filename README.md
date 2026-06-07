# MediAlert 💊 | Smart AI-Powered Medicine Assistant

**MediAlert** is a high-performance, modern Android application designed to ensure medication adherence through intelligent scheduling and AI-driven insights. By combining precise alarm management with the power of Large Language Models (LLMs) via Groq Cloud, MediAlert serves as a comprehensive healthcare companion.

---

## 🌟 Product Overview

MediAlert was created to solve the common problem of medication non-adherence. It goes beyond standard alarm apps by providing context-aware medical information and an AI assistant to help users understand their prescriptions better, while maintaining a strict and reliable notification system.

### 🎯 Who is it for?
- **Patients with Chronic Conditions:** Managing multiple daily prescriptions.
- **Seniors & Caregivers:** Helping maintain strict adherence to complex medical regimes without confusion.
- **Health-Conscious Individuals:** Users seeking quick, AI-verified information about their medications.

---

## 🚀 Key Features

### 📅 Intelligent Scheduling & Reminders
*   **Flexible Frequencies:** Support for *Once daily*, *Twice daily*, *3× daily*, and *Custom* intervals.
*   **Time-of-Day Grouping:** Automatically organizes medication into **Morning ☀️**, **Afternoon 🌤**, **Evening 🌇**, and **Night 🌙** based on your schedule for a clutter-free dashboard.
*   **Exact Alarms:** Leverages Android's `AlarmManager` for high-precision notifications that fire even when the app is in the background or the device is idle.
*   **Interactive Notifications:** Take quick actions like **Mark as Taken** or **Snooze** directly from the notification shade without opening the app.

### 🤖 AI-Powered Insights (Groq Cloud)
*   **Instant Medicine Details:** Automatically fetch structured data including **Uses**, **Side Effects**, and **Warnings** for any medicine using Llama 3 models.
*   **AI Medical Chatbot:** A context-aware chatbot that "knows" your current medication list and can answer questions about drug interactions or general health tips.
*   **Smart Medical Tips:** AI-generated daily tips tailored to your specific medications to improve health outcomes.

### 📊 Adherence Tracking & History
*   **Dose Logging:** Every pill taken is logged with a precise timestamp in a local Room database.
*   **History View:** A comprehensive timeline to review past adherence, identify missed patterns, and calculate compliance percentages.
*   **Status Indicators:** Visual cues for Pending (Ready to take), Taken (Completed), and Missed (Past due) doses.

### 🎨 Premium UI/UX
*   **Material 3:** Built with the latest Jetpack Compose components and **Dynamic Color** support.
*   **Dark/Light Mode:** High-contrast, accessibility-focused themes designed for readability in any lighting condition.
*   **Custom Branding:** A unique MediAlert pill logo and a carefully selected brand color palette for a professional medical feel.

---

## 🛠 Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (100%) |
| **Design System** | Material Design 3 |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Database** | Room Persistence Library (KSP) |
| **Network** | Retrofit, OkHttp, GSON |
| **AI Integration** | Groq API (Llama-3.3-70b-versatile) |
| **Storage** | Jetpack DataStore (Preferences) |
| **Async** | Kotlin Coroutines & Flow |
| **System** | AlarmManager, NotificationManager, WorkManager |

---

## 📦 Installation & Setup

### Prerequisites
*   Android Studio **Ladybug** (2024.2.1) or newer.
*   Android SDK **API 26 (Oreo)** or higher.
*   A **Groq API Key** (Available at [console.groq.com](https://console.groq.com/)).

### Step-by-Step Guide

1.  **Clone the Repository**
    ```bash
    git clone https://github.com/sampurna744/medialert.git
    ```

2.  **Configure API Keys**
    Create a file named `local.properties` in your project's root directory and add your Groq API key:
    ```properties
    groq.api_key=your_actual_groq_api_key_here
    ```
    *Note: The project is pre-configured to read this key at build time using Gradle's BuildConfig generator.*

3.  **Sync & Build**
    *   Open the project in Android Studio.
    *   Perform a **Gradle Sync** to download dependencies.
    *   Run the `:app` module on your physical device or emulator.

---

## 📖 Usage Guidelines

1.  **Adding a Medicine:** Tap the `+` button on the Home screen. Enter the name, dosage, and frequency. The app will automatically calculate the next alarm time.
2.  **Taking a Dose:** When an alarm rings, tap "Mark as Taken" on the notification. Alternatively, use the checkmark on the Home screen.
3.  **AI Research:** Open a medicine's detail page to see AI-generated insights. Use the **Chat** tab for general medical questions.
4.  **Monitoring Progress:** Visit the **History** tab to see your weekly performance and log details.

---

## 🛡 Disclaimer
**Important:** MediAlert is an informational and scheduling tool. The AI-generated information is for reference only and is not a substitute for professional medical advice. **Always** follow your doctor's prescriptions and consult with a healthcare professional for serious medical decisions.

---

## 👨‍💻 Author
**Sampurna Simkhada**  
[GitHub](https://github.com/sampurna744)

---
*Developed as a modern solution for medication management and healthcare accessibility.*
