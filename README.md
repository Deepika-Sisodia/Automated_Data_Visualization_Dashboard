# 📊 Automated Data Visualization & Dashboard Generator

## 🚀 Overview

The **Automated Data Visualization & Dashboard Generator** is a full-stack web application that converts raw data (CSV/Excel files) into interactive dashboards.

Instead of using a traditional database, this project uses **browser Local Storage** to store processed data, making it lightweight and easy to run without database setup.

---

## 🎯 Features

### 📥 Data Upload

* Upload CSV (.csv) or Excel (.xlsx) files
* Drag-and-drop support
* File validation and error handling

### ⚙️ Smart Data Processing

* Parses file into structured JSON
* Automatically detects:

  * Numerical data
  * Categorical data
  * Temporal (date) data

### 📊 Auto Dashboard Generation

* Generates charts automatically:

  * Bar Chart
  * Line Chart
  * Pie Chart
* Smart mapping of labels and values

### 🔍 Interactive Filtering

* Dynamic filters:

  * Dropdowns (categories)
  * Range sliders (numbers)
  * Date filters
* Real-time chart updates

### 💾 Local Storage Usage

* Parsed data stored in **browser Local Storage**
* No external database required
* Faster setup and lightweight architecture

### 📤 Export

* Download charts as images
* Export summary data

---

## 🛠️ Tech Stack

### Backend

* Java, Spring Boot
* File parsing using:

  * Apache POI (Excel)
  * OpenCSV (CSV)

### Frontend

* React.js (Hooks)
* Axios
* React Router
* Recharts / Chart.js

### Storage

* Browser Local Storage (instead of MySQL)

---

## 🏗️ System Architecture

```
Frontend (React)
    ↓
Spring Boot API (File Parsing)
    ↓
Processed JSON Data
    ↓
Stored in Browser Local Storage
    ↓
Dashboard Rendering (React Charts)
```

---

## 🔄 Workflow

1. User uploads CSV/Excel file
2. Backend parses data → converts to JSON
3. Frontend stores data in Local Storage
4. System detects column types
5. Dashboard is generated automatically
6. Filters applied → charts update instantly

---

## 📡 API Endpoints

### Upload File

```
POST /api/upload
```

### Get Parsed Data

```
GET /api/data
```

---

## 📁 Project Structure

### Backend

```
backend/
 ├── controller/
 ├── service/
 ├── util/
 └── resources/
```

### Frontend

```
frontend/
 ├── components/
 ├── pages/
 ├── services/
 └── App.js
```

---

## ▶️ Setup Instructions

### 🔧 Backend

1. Open in IDE (IntelliJ/Eclipse)
2. Run Spring Boot application

### 💻 Frontend

1. Install dependencies:

   ```
   npm install
   ```
2. Start React app:

   ```
   npm start
   ```

---

## ⚡ Advantages of Local Storage

* No database setup required
* Faster development and testing
* Easy deployment

---

## ⚠️ Limitations

* Data is stored only in browser
* Data is lost if Local Storage is cleared
* Not suitable for large-scale production apps

---

## 🌟 Future Enhancements

* Replace Local Storage with database (MySQL/MongoDB)
* Add authentication
* Cloud deployment

---

## 🎯 Conclusion

This project simplifies data visualization by automatically generating dashboards from uploaded files, using Local Storage for quick and efficient data handling.
