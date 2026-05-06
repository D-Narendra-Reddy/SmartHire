# SmartHire - Job Portal System

SmartHire is a full-stack job portal application built with Spring Boot and Vanilla Web Technologies (HTML/CSS/JS). It provides role-based access for Candidates, Recruiters, and Administrators.

## Tech Stack
* **Backend:** Java 17, Spring Boot 3, Spring Security, JWT, Spring Data JPA, Hibernate, MySQL, Cloudinary.
* **Frontend:** HTML5, CSS3 (Vanilla), JavaScript, Fetch API.

## Features
* **Role-Based Authentication:** Secured endpoints and UI based on `CANDIDATE`, `RECRUITER`, and `ADMIN` roles using JWT.
* **Recruiter Actions:** Create company profiles, post jobs, manage job listings, and review/update candidate applications.
* **Candidate Actions:** Update user profiles, upload resumes (via Cloudinary), search and filter jobs, save jobs, and apply for jobs.
* **Admin Actions:** Monitor and delete users and jobs to maintain platform integrity.
* **Responsive UI:** A modern, clean, and professional interface with a customized CSS design system.

## Project Structure
* `/src/main/java/com/smarthire`: Spring Boot Backend (Controllers, Services, Repositories, Entities, Configs).
* `/frontend`: Frontend UI files (HTML, CSS, JS).

## Setup Instructions

### 1. Database Configuration
1. Ensure MySQL is running locally on port `3306`.
2. Create a database named `smarthire`:
   ```sql
   CREATE DATABASE smarthire;
   ```
3. The application will automatically create the tables and insert initial roles and an admin user (configured in `data.sql`).

### 2. Cloudinary Configuration
To enable resume uploads, you must set up a Cloudinary account.
1. Update `src/main/resources/application.properties` with your credentials:
   ```properties
   cloudinary.cloud-name=YOUR_CLOUD_NAME
   cloudinary.api-key=YOUR_API_KEY
   cloudinary.api-secret=YOUR_API_SECRET
   ```

### 3. Running the Backend
1. Open the project in your IDE (IntelliJ, Eclipse, or VS Code).
2. Run `SmartHireApplication.java` as a Java Application.
3. The server will start on `http://localhost:8080`.

### 4. Running the Frontend
1. You can simply open `frontend/index.html` in your browser.
2. For the best experience and to avoid CORS issues with local files, serve the `frontend` folder using an HTTP server (e.g., VS Code Live Server extension).

### 5. Testing the Application
* **Admin Login:** `admin@smarthire.com` / `admin123`
* **Test the APIs:** A Postman collection (`postman_collection.json`) is provided in the project root to test all available backend endpoints.
