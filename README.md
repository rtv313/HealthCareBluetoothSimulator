# Healthcare Bluetooth Simulator

This project is a comprehensive **Android Bluetooth Low Energy (BLE) Simulator and Client**. It is designed to act as both a **Peripheral (Sensor Emulator)** and a **Central (Data Collector)**, providing a full end-to-end environment for testing and learning BLE development on Android.

It is specifically tailored as a **tutorial resource**, featuring clean architecture, detailed educational comments, and standardized logging.

---

## 🚀 Key Features

### 1. Peripheral Emulator (The Sensor)
- **GATT Server Implementation:** Simulates a professional health sensor providing Heart Rate, Sensor Status, and Device Name.
- **Dynamic Telemetry:** Real-time updates for BPM and status changes through a foreground service.
- **Custom Security:** Features a custom 4-digit PIN authentication flow that bypasses intrusive system pairing dialogs for a seamless in-app experience.

### 2. Central Client (The Monitor)
- **Real-time Scanner:** Discovered available health sensors with RSSI (signal strength) filtering and automatic stale device cleanup.
- **Interactive UI:** Provides a guided connection flow including:
    - **Loading States:** Professional progress dialogs during service discovery.
    - **Secure Entry:** Dedicated PIN entry modal with real-time error feedback ("Wrong PIN").
    - **Guardrail Protection:** 10-second connection timeouts to prevent app hangs.
- **Telemetry Dashboard:** A dedicated panel to visualize live Heart Rate data and sensor metadata.

### 3. Architecture & Tech Stack
- **Modern UI:** Built entirely with **Jetpack Compose** and Material 3.
- **Clean Architecture:** Strict separation of concerns using **Domain Use Cases**, **Repositories**, and **Data Sources**.
- **Dependency Injection:** Powered by **Dagger Hilt**.
- **Reactive Streams:** Uses **Kotlin Coroutines and Flow** for high-performance, asynchronous BLE communication.

---

## 🛠️ Project Structure

- **`ble/`**: Contains the Peripheral/Server logic (`BleManagerImpl`) and GATT constant definitions.
- **`ble_connect/`**: Contains the Central/Client logic (`BleClientManagerImpl`) and Scanning logic.
- **`domain/`**: Pure business logic, models, and interface definitions.
- **`data/`**: Implementation of repositories and data sources (hardware abstraction).
- **`presentation/`**: Compose UI layers and ViewModels for both the Emulator and Client screens.
- **`service/`**: Foreground service management for maintaining BLE advertising in the background.

---

## 📖 Educational Refactoring
The codebase has been specifically refactored for new developers:
- **Standardized Logging:** All logs use a consistent `TAG` constant derived from the class name.
- **GATT Documentation:** Detailed comments explain the lifecycle of service discovery, characteristic reads/writes, and CCCD (Descriptor) configurations.
- **State Management:** Clear implementation of Sealed Classes for managing complex asynchronous connection states.

---

## 🏁 Getting Started
1. **Bluetooth Permissions:** The app includes a robust permission helper to handle `BLUETOOTH_SCAN`, `BLUETOOTH_ADVERTISE`, and `BLUETOOTH_CONNECT`.
2. **Emulator Mode:** Toggle the "Start Simulation" button to begin broadcasting as a "Health Sensor".
3. **Client Mode:** Use the Scanner screen to find the sensor and connect using the configured PIN (default validation logic included).
