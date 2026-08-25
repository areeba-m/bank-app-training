# Bank Account Web Application

A full-stack banking web application built with Spring Boot and a JavaScript frontend, supporting secure authentication, role-based access control, account management, balances, transfers, transaction history, and spending insights.

## Architecture

* **Frontend:** JavaScript SPA deployed on Vercel.
* **Backend:** Spring Boot REST API, currently running locally during development.
* **Database:** PostgreSQL hosted on Supabase Cloud.
* **Architecture:** Layered architecture using Controllers, Services, Repositories, and JPA entities.
* **Database migrations:** Liquibase.
* **API documentation:** Swagger / OpenAPI.
* **Containerization:** Docker Compose support for running the backend and PostgreSQL locally.

## Features

* User registration and authentication.
* JWT-based authentication with access and refresh tokens.
* HttpOnly refresh-token cookies.
* Role-based access control.
* Account and balance management.
* Money transfers between accounts.
* Transaction history.
* Idempotency keys for transfer operations.
* Pessimistic locking for concurrent balance updates.
* Transaction categorization using Gemini AI.
* Spending insights based on transaction categories.
* Input validation and centralized exception handling.
* CSRF protection and security configuration.
* REST API with Swagger documentation.

## Testing

* **Unit tests** for application components and business logic.
* **Integration tests** covering Spring Boot application and database interactions.
* **End-to-end tests** using Playwright to validate complete frontend-to-backend user flows.
* **JaCoCo** for test coverage reporting.
* Separate test configuration and local PostgreSQL database for automated tests.

## Code Quality and Static Analysis

* **SpotBugs** for detecting potential Java bugs and code defects.
* **Checkstyle** for enforcing coding standards.
* **PMD** for detecting problematic code patterns and maintainability issues.
* **Spring Boot UI** for application architecture, dependency, security, and performance analysis.
* Code quality checks integrated into the Maven build.

## Docker

Docker support is provided for local backend and database development.

* Docker Compose runs:

  * Spring Boot backend.
  * PostgreSQL database.
* The frontend is not containerized.
* The application can be started with:

```bash
docker compose up --build
```

* Stop the containers with:

```bash
docker compose down
```

* Remove containers and database volumes with:

```bash
docker compose down -v
```

## Deployment

* **Frontend:** Deployed on Vercel.
* **Database:** PostgreSQL hosted on Supabase Cloud.
* **Backend:** Currently running locally during development.
* Environment-specific configuration is managed through Spring profiles and environment variables.
* Secrets and credentials are kept outside source control.

## Development Environment

* Java / Spring Boot
* Maven
* PostgreSQL
* Hibernate / JPA
* Liquibase
* Spring Security
* JWT
* Docker / Docker Compose
* Playwright
* JaCoCo
* SpotBugs
* Checkstyle
* PMD
* Swagger / OpenAPI
* Supabase
* Vercel
* Gemini API

## Aura Bank UI

<img width="1854" height="929" alt="01-login" src="https://github.com/user-attachments/assets/3581d8b8-6031-4c27-a66a-d405a9cfb640" />

<img width="1854" height="929" alt="02-admin-dashboard" src="https://github.com/user-attachments/assets/f988e832-5565-4f13-bee1-4d5391a0ce47" />

<img width="1854" height="929" alt="03-admin-user-details" src="https://github.com/user-attachments/assets/7e2bc8a5-9505-47c7-9424-dbff31da63f0" />

<img width="1854" height="929" alt="04-user-dashboard" src="https://github.com/user-attachments/assets/7f89727c-b91e-47d9-8ec9-f0a6d9eebe1e" />

<img width="1854" height="929" alt="05-user-transaction" src="https://github.com/user-attachments/assets/61e6ed9a-26a0-45dd-b4d4-5babe79fe8f1" />

<img width="1854" height="929" alt="06-user-spending-insight" src="https://github.com/user-attachments/assets/8a3c9d24-319c-4097-b219-5ea035d7806d" />




