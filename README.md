# Views Tool - Ping-Pong View Automator

**Views Tool** is a production-grade Android application designed to automate the process of boosting views for short-form video content (TikTok, YouTube Shorts, etc.) in a "Ping-Pong" loop pattern. 

Built with a focus on clean Android architecture and system-level interaction, this tool uses a floating overlay and the Android Accessibility API to physically navigate through videos automatically.

---

## ✨ Features

- **🔄 Ping-Pong Looping Logic**: Automatically navigates in both directions (e.g., 1 ➔ 2 ➔ 3 ➔ 2 ➔ 1).
- **🖱️ Auto-Scroll Engine**: Uses the Accessibility API to inject high-precision swipe gestures (Swipe Up for Forward, Swipe Down for Backward).
- **⏱️ Smart Random Delays**: Set a min/max delay range; the tool generates a new random wait time for every video to simulate human behavior.
- **☁️ Mini-Strip Floating UI**: An ultra-compact, semi-transparent blue pill-shaped controller that stays on top of other apps.
- **⚡ Foreground Service**: Runs reliably in the background even while you are interacting with TikTok or Reels.
- **🚫 Zero Interference**: The fixed-position "Mini-Strip" ensures that auto-scroll gestures never accidentally interact with the controller itself.

---

## 🛠️ Technology Stack

- **Language**: Java / Android SDK
- **Architecture**: Service-based Architecture with logic separation.
- **APIs Used**:
  - `AccessibilityService`: For gesture injection and touch simulation.
  - `WindowManager`: For the system-layer floating overlay.
  - `ForegroundService`: To maintain process priority.
  - `PowerManager (WakeLock)`: To keep the screen active during long runs.

---

## 🚀 How to Use

1. **Download APK**: Go to the [Releases](https://github.com/USER_NAME/REPO_NAME/releases) page and download the latest `ViewsTool.apk`.
2. **Install**: Open the APK on your Android device and install it (you may need to allow "Unknown Sources").
3. **Grant Permissions**:
   - **Overlay Permission**: Allow the app to "Display over other apps".
   - **Accessibility Permission**: Enable **Views Tool** in **Settings > Accessibility**. (Required for the physical swiping to work).
4. **Configure**:
   - Enter the total number of items to review.
   - Set your minimum and maximum delay (e.g., 5s to 15s).
5. **Start Looping**:
   - Tap **Configure Overlay**.
   - Navigate to TikTok or YouTube Shorts.
   - Tap the circular **Play** button on the tiny blue strip in the top-left corner.
6. **Sit Back**: The tool will now automatically swipe through the videos in a ping-pong pattern!

---

## 👨‍💻 Author

Developed as a personal project for exploring view automation and Accessibility-based screen control.
