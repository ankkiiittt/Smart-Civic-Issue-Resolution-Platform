# Smart Civic Issue Resolution Platform

A modern, full-stack spatial web application designed to empower citizens to report local civic issues (such as potholes, streetlight outages, water leaks, or illegal dumping) and enable municipal officers to manage and resolve them efficiently within designated service zones.

---

## 🚀 Key Features

### 1. Citizen Dashboard
*   **Interactive Spatial Mapping**: Interactive Map integration for selecting precise geolocation coordinates.
*   **Incident Reports**: Report issues with titles, categories, detailed descriptions, and image uploads.
*   **Upvoting System**: Community upvotes to prioritize urgent issues in local neighborhoods.
*   **Incident Management**: Citizens can delete their own reported incidents. The system safely cascades and cleans up associated photos, upvotes, and history logs from the database.

### 2. Municipal Officer Workspace
*   **Automatic Zone-Based Assignment**: System routes reported incidents to officers based on whether the geolocation coordinates fall within their department's spatial service zone using PostGIS `ST_Contains` spatial queries.
*   **Incident Lifecycle Management**: Transitions incident status through `REPORTED` ➔ `ACKNOWLEDGED` ➔ `IN_PROGRESS` ➔ `RESOLVED`.
*   **Auditing and Comments**: Update work progress comments and attach resolution evidence images.

### 3. Core Automation & Services
*   **SLA Deadline Tracking**: Automatically computes resolution deadlines based on incident category SLAs, flagging tickets that are overdue.
*   **Real-time WebSocket Notifications**: Pushes real-time alerts to logged-in users whenever ticket status changes or SLA deadlines breach.
*   **Interactive API Documentation**: Embedded Swagger UI/OpenAPI definitions for exploring and testing API endpoints.

---

## 🛠️ Technology Stack

### Backend
*   **Core**: Java 17 + Spring Boot 3.x
*   **Database**: PostgreSQL + PostGIS (Spatial extension)
*   **Security**: Spring Security + JWT Authentication
*   **Communication**: Spring WebSocket + STOMP Messaging
*   **Documentation**: Springdoc OpenAPI (Swagger UI)

### Frontend
*   **Core**: React + TypeScript
*   **Build Tool**: Vite
*   **Mapping**: Leaflet + React-Leaflet
*   **Icons**: Lucide React
*   **Styling**: Modern, premium CSS styling with dark glassmorphism layout

---

## ⚙️ Prerequisites & Setup

Ensure the following tools are installed on your machine:
*   **Java Development Kit (JDK)**: Version 17 or higher
*   **Node.js**: Version 18+ & npm
*   **PostgreSQL**: Version 13+ with **PostGIS** extension enabled
*   **VS Code** (Optional, but recommended)

---

## 🛠️ Installation & Execution Guide

### 1. Database Configuration
1. Start your local PostgreSQL server.
2. Create a database named `smart_civic`:
   ```sql
   CREATE DATABASE smart_civic;
   ```
3. Enable the PostGIS extension in the database:
   ```sql
   \c smart_civic;
   CREATE EXTENSION postgis;
   ```
4. Verify/update your connection details in `backend/src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/smart_civic
       username: postgres
       password: <your_postgres_password>
   ```

### 2. Running the Backend
1. Open a terminal and navigate to the `backend` folder:
   ```bash
   cd backend
   ```
2. Build and package the Spring Boot application (skipping tests):
   ```bash
   & ".\maven\bin\mvn.cmd" clean package -DskipTests
   ```
3. Run the Spring Boot application:
   ```bash
   & ".\maven\bin\mvn.cmd" spring-boot:run
   ```
   *The backend server will start on port `8080` and seed default categories, departments, and testing user profiles automatically.*

### 3. Running the Frontend
1. Open a new terminal and navigate to the `frontend` folder:
   ```bash
   cd frontend
   ```
2. Install the required Node dependencies:
   ```bash
   npm install
   ```
3. Launch the Vite development server:
   ```bash
   npm run dev
   ```
4. Open your browser and navigate to `http://localhost:5173/`.

---

## 👥 Seeded User Accounts (For Testing)

| Role | Username | Password | Purpose |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Access full system metrics & audit logs |
| **Citizen** | `citizen_test` | `citizen123` | Report new incidents, upvote, delete own reports |
| **Officer (Sanitation)** | `officer_sanitation` | `officer123` | Manage issues falling in Sanitation zones |
| **Officer (Water)** | `officer_water` | `officer123` | Manage issues falling in Water service zones |

---

## 📄 API Documentation
Once the backend is running, the interactive Swagger documentation is accessible at:
*   **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
*   **API Specs**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
