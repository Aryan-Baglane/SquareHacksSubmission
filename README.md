# 🇮🇳 INDRA: Intelligent Network for Data and Rural Assistance 🇮🇳

![INDRA Banner](https://placehold.co/1200x300/6200EE/FFFFFF?text=Project+INDRA)

**INDRA** is a cutting-edge Android application engineered to bridge the digital divide in rural environments. By combining real-time data collection, advanced geospatial intelligence, and Generative AI, INDRA empowers field operators with a robust, intuitive tool for water management, crop assessment, and community assistance.

---

## 🌟 Overview

Built with a focus on scalability and modern best practices, INDRA leverages the full power of the **Kotlin** ecosystem. It features a declarative UI, a reactive data layer, and seamless cloud integration to ensure reliability even in challenging field conditions.

> **Architecture Philosophy:** INDRA follows the **MVVM (Model-View-ViewModel)** architectural pattern, ensuring a clean separation of concerns, testability, and a maintainable codebase.

---

## ✨ Core Features

### 🎨 Modern & Declarative UI
- Fully built with **Jetpack Compose** and **Material 3**.
- Responsive layouts optimized for a wide range of Android devices.
- Engaging multi-step **Onboarding Flows** to simplify user entry.

### 🔐 Secure & Real-time Backend
- **Firebase Authentication**: Secure entry via Email/Password and **Google Sign-In**.
- **Real-time Sync**: Uses **Firebase Realtime Database** to ensure field data is instantly available across the ecosystem.

### 🗺️ Advanced Geospatial Intelligence
- **Dual Map Support**: Integrated with both **Mappls (MapmyIndia)** and **Google Maps SDK**.
- **Location Awareness**: High-precision, battery-efficient tracking via **Google Play Services**.

### 🤖 Generative AI Integration
- Powered by the **Google Generative AI SDK (Gemini)**.
- Provides contextual assistance, automated data insights, and intelligent field guidance.

### 📊 Field-Centric Dashboard
- A dedicated **IndraGramin** interface designed specifically for rural workflows.
- Standardized **Assessment Models** for systematic data gathering.

---

## 🛠️ Tech Stack

| Category | Technology |
| :--- | :--- |
| **Language** | [Kotlin](https://kotlinlang.org/) |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) |
| **Design System** | [Material 3](https://m3.material.io/) |
| **Backend** | [Firebase](https://firebase.google.com/) (Auth, RTDB) |
| **AI Engine** | [Google Gemini API](https://ai.google.dev/) |
| **Mapping** | [Mappls SDK](https://www.mappls.com/) & Google Maps SDK |
| **Architecture** | MVVM, Jetpack Navigation, Hilt (DI) |
| **Image Loading** | [Coil](https://coil-kt.github.io/coil/) |

---

## 📸 App Preview

| Onboarding | Dashboard | Map Intelligence |
| :---: | :---: | :---: |
| ![Onboarding](<img width="524" height="1114" alt="image" src="https://github.com/user-attachments/assets/4de75f5e-24e0-4ab5-ba48-1fba87a87bd0" />
) | ![Dashboard](<img width="524" height="1114" alt="image" src="https://github.com/user-attachments/assets/c35c53e6-7871-4b26-b288-9763796bd9b1" />
) | ![Market Price Tracker](<img width="524" height="1114" alt="image" src="https://github.com/user-attachments/assets/94590e66-b2f7-4426-a2e6-41248b839986" />
) | ![Crop Suggestion](<img width="524" height="1114" alt="image" src="<img width="524" height="1114" alt="image" src="https://github.com/user-attachments/assets/3114600a-1da1-43cf-99a0-22393c919c50" />
) |

---

## 🚀 Getting Started

### ✅ Prerequisites
- **Android Studio** (Iguana `2023.2.1` or newer)
- **JDK 17** or higher
- **API Keys**: You will need keys for Firebase, Mappls, and Google AI (Gemini).

### ⚙️ Setup Instructions

1. **Clone the Repository**
   ```bash
   git clone [https://github.com/your-username/INDRA-APP.git](https://github.com/your-username/INDRA-APP.git)
   cd INDRA-APP

