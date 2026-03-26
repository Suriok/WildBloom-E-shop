# WildBloom

A full-stack e-commerce web application for a flower shop, built with **Java, Spring Boot, PostgreSQL, and vanilla JavaScript**.

WildBloom is a layered web application that combines a secure backend, role-based access control, business workflow logic, and a responsive frontend for customers and administrators.


## Overview

WildBloom is a full-stack e-commerce application for a flower shop. Users can browse products, manage a shopping cart, and place orders, while administrators can manage products, categories, users, and order processing.

This project demonstrates:
- REST API design
- layered backend architecture
- role-based access control
- relational database integration
- business workflow implementation
- testing and validation



## Features

### Customer Features
- Browse the product catalog
- View product details
- Filter products by category
- Add and remove products from cart
- Place orders
- View order history

### Employee / Administrator Features
- Manage products
- Manage categories
- Manage users
- View and process customer orders
- Update order statuses
- Access administrative functionality

### Business Logic
- Role-based access control using Spring Security
- Controlled order lifecycle with status transitions
- Stock validation before order confirmation
- Automatic rollback when an order is cancelled



## Screenshots

### Product Catalog
<img width="1280" height="908" alt="Снимок экрана 2026-03-26 171142" src="https://github.com/user-attachments/assets/7753ae04-cebb-441d-ae97-f0ceae6c48c0" />


### Shopping Cart
<img width="1195" height="777" alt="Снимок экрана 2026-03-26 171236" src="https://github.com/user-attachments/assets/0ec6242d-27f9-4405-8424-d62cea9b74ab" />


### Orders
<img width="1374" height="914" alt="Снимок экрана 2026-03-26 170948" src="https://github.com/user-attachments/assets/a68dcc5f-d520-45e6-bd0a-aa0cb0d73731" />


### Admin Dashboard
<img width="1291" height="908" alt="Снимок экрана 2026-03-26 171011" src="https://github.com/user-attachments/assets/7ab3774f-85ea-4790-ad70-a742a14a824a" />




## Tech Stack

### Backend
- Java 17
- Spring Boot 3
- Spring Data JPA
- Spring Security
- MapStruct
- Maven

### Database
- PostgreSQL
- H2 (for testing / local scenarios if configured)

### Frontend
- HTML5
- CSS3
- Vanilla JavaScript (ES6+)

### Testing & Tools
- JUnit 5
- Mockito
- JaCoCo
- Postman
- Git



## Architecture

The application follows a layered architecture:

- **Controller layer** – handles HTTP requests and API endpoints
- **Service layer** – contains business logic
- **DAO / Repository layer** – handles persistence
- **Database layer** – stores application data

This separation improves code readability, maintainability, and scalability.



## Security

The application uses **Spring Security** with role-based access control.

Supported roles:
- `CUSTOMER`
- `EMPLOYEE`
- `ADMINISTRATOR`

Permissions are separated based on user role in order to protect administrative operations and sensitive workflows.



## Running the Project

### Prerequisites

Before starting, make sure you have installed:

- Java 17 or newer
- Maven
- PostgreSQL
- Git

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/wildbloom.git
cd wildbloom
