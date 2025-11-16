📘 FlamEdgeViewer

Real-Time Edge Detection on Android (Camera2 + JNI + OpenCV + OpenGL)
© 2025 Mohit Singh — All Rights Reserved

🔥 Overview

FlamEdgeViewer is a complete real-time edge-detection system for Android.
It uses Camera2 for frame capture, processes images in C++ (OpenCV) via JNI, and renders the final edge-detected output through OpenGL ES to achieve smooth GPU-accelerated performance.
Along with the mobile app, the project includes a standalone TypeScript + Vite Web Viewer capable of previewing processed frames with FPS and resolution overlays.
This project demonstrates advanced real-time computer vision infrastructure suitable for R&D, robotics, graphics, and native Android systems.

🧩 Key Features

📸 Real-time Camera2 API frame capture
⚙️ Efficient JNI bridge for buffer transfer
🧠 OpenCV C++ Canny edge detection
🎨 OpenGL ES GPU texture rendering
🌐 Web Viewer built with TypeScript + Vite
📈 Live FPS & resolution overlay (Web UI)
🧱 Clean modular architecture

🏗 System Architecture

Camera2  →  YUV → JNI  →  C++ OpenCV  → RGBA Buffer → OpenGL Texture → Display
                                                         │
                                                         └── Web Viewer (Static Preview)

📁 Project Structure

FlamEdgeViewer/
│
├── app/                          # Android camera, JNI, GL code
│   ├── camera/                   # Camera2 controller
│   ├── gl/                       # OpenGL ES renderer
│   └── jni/                      # Java <-> Native interface
│
├── jni/                          # Native C++ layer
│   ├── native-lib.cpp
│   ├── edge_processor.cpp
│   └── CMakeLists.txt
│
├── web/                          # Vite + TypeScript Web Viewer
│   ├── public/
│   │   └── sample-frame.png
│   ├── src/
│   │   ├── components/
│   │   └── ui/
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
│
└── README.md

📸 Screenshots
Android Live Edge Detection Output

Here is the output from the real-time Canny edge detection pipeline running on device-:
https://drive.google.com/file/d/1MCaBJM5jeOwg7hdpAWQdQwHH2wMTSppk/view?usp=sharing

⚙️ How to Build

Android App — Build Steps

Requirements-:
Android Studio (Hedgehog or newer),
NDK, CMake, LLDB installed,
OpenCV Android SDK (already included)

Steps-:

1.Clone this repository
2.Open the project in Android Studio
3.Let Gradle sync
4.Connect a device
5.Run the app

Real-time edge detection will appear instantly

🌐 Web Viewer — Build Steps

-cd web
-npm install
-npm run dev

Open:
👉 http://localhost:5173

To change the frame file:
web/public/sample-frame.png

🧠 Technical Highlights

-Native C++/OpenCV image processing
-Fast JNI bridge for minimal overhead
-OpenGL ES texture rendering for 30+ FPS
-Custom Camera2 pipeline with orientation correction
-Modular Vite + TypeScript Web UI
-Clean, scalable architecture suitable for R&D

🪪 Trademark & Credits

FlamEdgeViewer
Created by Mohit Singh
© 2025 Mohit Singh — All Rights Reserved

🤝 Developer

Mohit Singh
Native Android • OpenCV • C++ • OpenGL ES • TypeScript
GitHub: https://github.com/Mohit-glitch42

⭐ Final Remarks

FlamEdgeViewer delivers-:
✔ Real-time processing
✔ Native performance
✔ Modern GPU rendering
✔ Web visualization tool
