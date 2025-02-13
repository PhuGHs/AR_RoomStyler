# AR Room Styler

## Contributors
Seminar các vấn đề hiện đại của CNPM – SE400.P11
1. Le Van Phu - 21522466
2. Le Hoai Hai - 21522032

## Overview
AR Room Styler is an augmented reality (AR) application that allows users to interact with 3D models in real-time. Users can place, move, and rotate virtual furniture in their real environment using ARCore technology.

## Features
- **Real-time 3D Model Placement:** Select and place 3D models into the real world through the camera.
- **Plane Detection:** Automatically detects flat surfaces like floors and tables to position furniture.
- **Object Movement:** Move objects across detected surfaces using ARCore.
- **Model Rotation:** Rotate models to view them from different angles.
- **Light Estimation:** Adjusts brightness and shadows dynamically based on real-world lighting conditions.
- **Depth API:** Enhances realism by utilizing depth perception for accurate object placement.
- **Gesture Handling:** Users can interact with objects using touch gestures (tap, drag, rotate).

## Tech Stack
- **Jetpack Compose** - Modern UI toolkit for building native Android interfaces.
- **ARCore** - Google's platform for building augmented reality experiences (https://github.com/SceneView/sceneview-android).
- **Kotlin** - Primary language for Android development.
## Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/PhuGHs/AR_RoomStyler.git
   ```
2. Open the project in Android Studio.
3. Build and run the application on an ARCore-supported device. Please make sure your devices are listed in this link https://developers.google.com/ar/devices

## Usage
- Open the app and grant necessary permissions.
- Scan the environment to detect surfaces.
- Select a 3D model and place it in the scene.
- Use gestures to move and rotate the model.
- Enjoy your AR experience!

## Product Images
![Product Image 1](https://res.cloudinary.com/daszajz9a/image/upload/v1739376307/project/1_f98g4g.png)
![Product Image 2](https://res.cloudinary.com/daszajz9a/image/upload/v1739376307/project/2_xt8aer.png)
![Product Image 3](https://res.cloudinary.com/daszajz9a/image/upload/v1739376305/project/3_irtcmr.png)
![Product Image 4](https://res.cloudinary.com/daszajz9a/image/upload/v1739376306/project/4_ghcvdp.png)

## Model sources
All of the models are downloaded from this website (https://poly.pizza/) and edited in blender (edit model scale)

## Repository
[GitHub Repository](https://github.com/PhuGHs/AR_RoomStyler.git)

## License
MIT License
