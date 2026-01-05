# Simply-Works

An open-source, modern Android application to control Candy "Simply-Fi" enabled washing machines. This app was born out of frustration with the official Candy Simply-Fi app, which is slow, unreliable, and constantly sends usage statistics to third-party servers.

Simply-Works is the result of reverse-engineering the official API to create a replacement that is **fast, stable, and respects your privacy.**

## ✨ Why Use Simply-Works?

-   **🚀 Fast and Responsive:** No more waiting for slow servers or unresponsive UI. The app is built with a modern tech stack for a snappy experience. No logins, no delays.
-   **🔒 Privacy-Focused:** The app communicates only what is necessary to control your machine. It does **not** send your usage data or other statistics to external servers.
-   **✅ Bloat-less:** A streamlined and stable implementation that focuses on core functionality without the bloat.

## ⚠️ Current Limitations

As a privacy-focused application that communicates directly with your machine, there are a couple of trade-offs and features that are not yet implemented:

-   **Local Network Only:** The app can only control the washing machine when your phone is connected to the **same local Wi-Fi network**. Remote control over the internet is not supported.
-   **No Push Notifications (TODO):** Wash completion notifications are not yet implemented. This is a planned feature.
-   **Unknown Device Support:** I have no way of knowing which Candy's Simply-Fi washing machines are supported. All communication was done via reverse engineering of my own model (CSO 1285TE-S).

## 🌟 Features

-   **Live Machine Status:** Get real-time updates on your washing machine's status, including current program, phase (washing, spinning), remaining time, and temperature.
-   **Request a Wash:** Select and start wash programs directly from the app.
-   **Usage Statistics:** Keep track of your washing cycles locally and on-demand. (This is the data Candy's app keeps sending to their servers.)

## 🚀 Setup & Build

To build and run the project, follow these steps:

1. Clone the repository 
2. In MainActivity.kt, set the `MACHINE_IP` constant to the local IP address of your Candy Simply-Fi washing machine.
3. **Build using gradle:**
   ```bash
   ./gradlew assembleDebug
   ```
4. Install the APK on your Android device.