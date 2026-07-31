# Bank Account Web Application

A full stack banking web application built with Spring Boot and a JavaScript frontend, supporting role-based access control (RBAC), account management, and transaction history.

---

## Architecture Overview

The system follows a standard multi-tier architectural pattern:

*   **Frontend:** Single Page Application (SPA) / JavaScript frontend communicating over REST API.
*   **Backend:** Spring Boot application structured with Controllers, Services, and Repositories.
*   **Security Layer:** Role-Based Access Control (RBAC) supporting `ADMIN` and `ACCOUNT_HOLDER` personas.
*   **Persistence Layer:** Relational Database with entities for Accounts, Balances, and Transactions.
