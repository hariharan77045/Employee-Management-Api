# EmployeeHub API

Spring Boot backend for the EmployeeHub employee portal. The Angular frontend lives in
[Employee-Management](https://github.com/hariharan77045/Employee-Management).

- Spring Boot 4.1, Java 17, Spring Security 7 (stateless JWT), Spring Data JPA / Hibernate 7
- PostgreSQL 18

## Run it locally

1. Create a PostgreSQL database named `employee_management_project` containing the original
   tables (`employee`, `department`, `designation`, `project`, `employeeproject`, `address`,
   `attendance`, `leaverequest`, `payroll`, `performancereview`).
2. Copy `src/main/resources/application-local.properties.example` to
   `application-local.properties` in the same folder and fill in your database password and a
   random JWT secret. That file is git-ignored. You can use the environment variables
   `DB_PASSWORD` and `JWT_SECRET` instead.
3. Start the API on port 8080:

   ```bash
   ./mvnw spring-boot:run
   ```

On startup the app:

- runs `schema.sql`, which adds ID sequences and the `announcement`, `company_event`,
  `app_user` and `mock_data_seed` tables;
- runs `data.sql` once, adding mock leave, attendance, payslips, reviews, news and events dated
  around the first start;
- creates a login for every employee that has none (`UserAccountSeeder`).

## Practice sign-in accounts

Username is the employee's email; the password is `app.security.default-password`
(`Password@123` unless you set `DEFAULT_PASSWORD`).

| Account | Role |
|---|---|
| employee1@company.com | ADMIN |
| employee2@company.com | HR |
| employee3@company.com | MANAGER |
| every other employee | EMPLOYEE |

These shared demo credentials are for local practice only.

## Configuration

| Property | Environment variable | Default |
|---|---|---|
| `spring.datasource.url` | `DB_URL` | `jdbc:postgresql://localhost:5432/employee_management_project` |
| `spring.datasource.username` | `DB_USERNAME` | `postgres` |
| `spring.datasource.password` | `DB_PASSWORD` | none (required) |
| `app.jwt.secret` | `JWT_SECRET` | none (required, 32+ characters) |
| `app.jwt.expiry-minutes` | | `480` |
| `app.security.max-failed-attempts` / `lock-minutes` | | `5` / `15` |
| `app.cors.allowed-origins` | | `http://localhost:4200` |

## API overview

| Path | Access |
|---|---|
| `POST /api/auth/login`, `POST /api/auth/change-password` | Public / signed in |
| `/api/me`, `/api/dashboard`, `/api/me/leaves`, `/api/me/payslips` | Signed in |
| `GET /api/employees`, `GET /api/employees/{id}` | Signed in (private fields by role) |
| `POST/PUT/DELETE /api/employees` | ADMIN, HR |
| `GET /api/leaves`, `PATCH /api/leaves/{id}/status` | ADMIN, HR, MANAGER |
| `/api/announcements`, `/api/events` | Read: signed in · Write: ADMIN, HR |
| `/api/admin/users/**` | ADMIN |
