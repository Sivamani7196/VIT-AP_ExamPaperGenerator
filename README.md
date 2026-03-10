# VIT-AP Exam Paper Generator

A Spring Boot web application to manage subjects and question banks, then generate university exam papers with configurable difficulty distribution.

## Features

- User signup and login (Spring Security form-based authentication)
- Subject management (`/api/subjects`)
- Question management by subject (`/api/questions`)
- Generate papers by marks and difficulty split (`/api/papers/generate`)
- Export generated paper to:
  - PDF (`/api/papers/{subjectId}/pdf`)
  - DOCX (`/api/papers/{subjectId}/docx`)
- Thymeleaf-based UI pages for home, login, and signup

## Tech Stack

- Java 21
- Spring Boot 3.4.1
- Spring MVC + Thymeleaf
- Spring Security
- Spring Data JPA
- H2 (default development profile)
- MySQL Connector (runtime option)
- Apache PDFBox (PDF export)
- Apache POI (DOCX export)

## Project Structure

```text
src/main/java/com/university/qpg
  config/        # Security and password configuration
  controller/    # Auth, subject, question, paper endpoints
  model/         # JPA entities
  repository/    # Spring Data repositories
  service/       # Business logic and export services

src/main/resources
  templates/     # Thymeleaf views
  static/        # CSS and JS assets
  application*.properties
```

## Run Locally

### Prerequisites

- Java 21+
- Maven (or use the included Maven wrapper)

### Start the app

```bash
./mvnw spring-boot:run
```

On Windows CMD/PowerShell:

```powershell
mvnw.cmd spring-boot:run
```

Default URL:

- `http://localhost:9898`

## Build and Test

```bash
./mvnw clean test
./mvnw clean package
```

## Docker

Build image:

```bash
docker build -t qpg-app .
```

Run container:

```bash
docker run -p 10000:10000 -e PORT=10000 qpg-app
```

Open:

- `http://localhost:10000`

## API Snapshot

- `POST /api/subjects` - create subject
- `GET /api/subjects` - list subjects
- `POST /api/questions` - create question
- `GET /api/questions/subject/{subjectId}` - questions by subject
- `POST /api/papers/generate` - generate and store paper
- `GET /api/papers/{subjectId}/pdf` - export PDF
- `GET /api/papers/{subjectId}/docx` - export DOCX

## Resume Links

Use these in your resume:

- Repository: `https://github.com/Sivamani7196/VIT-AP_ExamPaperGenerator`
- Live Demo: add your deployed URL here, for example `https://vit-ap-exampapergenerator.onrender.com`

> Note: A true live demo link requires deployment (Render/Railway/Azure). GitHub repo link is not considered a live running demo.

## Author

Sivamani
