"# Master-Dissertation-Backend

## Overview

This is the backend component of the SaaS Repair Service Management System, built with Spring Boot. It provides RESTful APIs for managing customers, technicians, appointments, repairs, inventory, billing, and more.

## Features

- User authentication and authorization with JWT
- Customer management
- Technician management
- Appointment scheduling
- Repair tracking
- Inventory management
- Billing and invoicing
- Dashboard analytics
- Email notifications
- OAuth2 integration

## Tech Stack

- **Framework**: Spring Boot 3.4.0
- **Language**: Java 20
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT
- **Build Tool**: Maven
- **Other**: Lombok, Kafka, OAuth2 Client, Mail

## Prerequisites

- Java 20 or higher
- Maven 3.6+
- PostgreSQL database

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/your-repo.git
   cd your-repo/Backend
   ```

2. Configure the database in `src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/your_db
       username: your_username
       password: your_password
   ```

3. Build the project:
   ```bash
   mvn clean install
   ```

## Running the Application

1. Start the application:
   ```bash
   mvn spring-boot:run
   ```

2. The API will be available at `http://localhost:8080`

## API Endpoints

- `POST /api/auth/login` - User login
- `GET /api/customers` - Get all customers
- `POST /api/appointments` - Create appointment
- And more... (Refer to the API documentation for full list)



