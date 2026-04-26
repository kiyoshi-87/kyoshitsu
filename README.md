# Kyoshitsu

Kyoshitsu is a Spring Boot backend for classroom management and real-time attendance tracking. It supports teacher and student accounts, JWT-based authentication, classroom creation and enrollment, and live attendance updates over WebSocket using STOMP and SockJS.

## Features

- JWT-based authentication
- Role-based access control for `teacher` and `student`
- Classroom creation and student assignment
- Classroom detail lookup for authorized users
- Live attendance session start/end flow for teachers
- Real-time attendance marking for students over WebSocket
- Duplicate attendance prevention per active session

## Tech Stack

- Java 21+
- Spring Boot 4
- Spring Security
- Spring Web MVC
- Spring WebSocket with STOMP + SockJS
- MongoDB
- Maven
- JUnit 5 + Mockito

## Project Structure

```text
src/main/java/com/kiyoshi87/application/kyoshitsu
|- auth
|- config
|- controller
|- event
|- exceptions
|- helper
|- model
|- repository
`- service
```

## Roles

The application currently uses these roles:

- `teacher`
- `student`
- `admin`

In the current functional flow, the main supported roles are `teacher` and `student`.

## Getting Started

### Prerequisites

- Java 21 or newer
- Maven 3.9+ or the included Maven wrapper
- Docker Desktop or Docker Engine

### Start MongoDB

The repository includes a `compose.yaml` file with MongoDB and Mongo Express.

Run:

```powershell
docker compose up -d
```

Services:

- MongoDB: `localhost:27017`
- Mongo Express: `http://localhost:8081`

Mongo Express credentials:

- Username: `mongoexpressuser`
- Password: `mongoexpresspass`

### Run the Application

Using Maven wrapper:

```powershell
.\mvnw spring-boot:run
```

Or with Maven installed:

```powershell
mvn spring-boot:run
```

By default, the application uses the MongoDB connection configured in `src/main/resources/application.yaml`.

### Configure CORS Per Environment

HTTP API CORS and WebSocket allowed origins are now driven by the same property.

You can create a local `.env` file in the project root from `.env.example` and set:

```properties
APP_CORS_ALLOWED_ORIGIN_PATTERNS=http://localhost:3000,http://localhost:5173,http://127.0.0.1:3000,http://127.0.0.1:5173
```

For other environments, set `APP_CORS_ALLOWED_ORIGIN_PATTERNS` to the comma-separated origins you want to allow.

## Authentication

Authentication is JWT-based for HTTP endpoints.

After login, send the token in the `Authorization` header:

```http
Authorization: Bearer <your-jwt-token>
```

## API Response Format

Most endpoints return the generic wrapper below:

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

On failure:

```json
{
  "success": false,
  "data": null,
  "error": [
    "Some error message"
  ]
}
```

## REST API

Default base URL:

```text
http://localhost:8080
```

### Auth Endpoints

#### `POST /api/v1/auth/signup`

Creates a new user account.

Authentication required: `No`

Request body:

```json
{
  "name": "John",
  "email": "john@example.com",
  "password": "password123",
  "role": "teacher"
}
```

Supported `role` values currently expected by the backend:

- `teacher`
- `student`
- `admin`

Success response:

```json
{
  "success": true,
  "data": {
    "name": "John",
    "email": "john@example.com",
    "role": "teacher"
  }
}
```

#### `POST /api/v1/auth/login`

Authenticates a user and returns a JWT.

Authentication required: `No`

Request body:

```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

Success response:

```json
{
  "success": true,
  "data": {
    "token": "<jwt-token>"
  }
}
```

#### `GET /api/v1/auth/me`

Returns the currently authenticated user.

Authentication required: `Yes`

Allowed roles:

- `teacher`
- `student`
- `admin`

Success response:

```json
{
  "success": true,
  "data": {
    "id": "user-id",
    "name": "John",
    "email": "john@example.com",
    "role": "teacher"
  }
}
```

### Classroom Endpoints

#### `POST /api/v1/class?className=<name>`

Creates a new classroom for the authenticated teacher.

Authentication required: `Yes`

Allowed roles:

- `teacher`

Example:

```http
POST /api/v1/class?className=Math%20101
Authorization: Bearer <jwt-token>
```

Success response:

```json
{
  "success": true,
  "data": {
    "classId": "class-id",
    "className": "Math 101",
    "teacherId": "teacher-id",
    "studentIds": []
  }
}
```

#### `POST /api/v1/class/add-students`

Adds students to an existing classroom.

Authentication required: `Yes`

Allowed roles:

- `teacher`

Request body:

```json
{
  "classId": "class-id",
  "studentIds": [
    "student-1",
    "student-2"
  ]
}
```

Behavior:

- Only the teacher who owns the class can add students
- Duplicate student IDs are ignored in the final saved class list
- All provided IDs must belong to existing student users

#### `GET /api/v1/class/{id}`

Returns classroom details.

Authentication required: `Yes`

Allowed roles:

- `teacher`
- `student`

Access rules:

- Teacher must own the classroom
- Student must be enrolled in the classroom

Success response:

```json
{
  "success": true,
  "data": {
    "classId": "class-id",
    "className": "Math 101",
    "teacherId": "teacher-id",
    "students": [
      {
        "id": "student-1",
        "name": "Student One",
        "email": "student1@example.com",
        "role": "student"
      }
    ]
  }
}
```

### General Endpoint

#### `GET /api/v1/students`

Returns all registered student users.

Authentication required: `Yes`

Allowed roles:

- `teacher`

Use case:

- Populate the student selection UI before assigning students to a class

### Attendance Session Endpoints

#### `POST /api/v1/attendance/start?classId=<classId>`

Starts an attendance session for a classroom.

Authentication required: `Yes`

Allowed roles:

- `teacher`

Behavior:

- Validates that the teacher owns the class
- Prevents starting a new session if one is already active
- Creates an active session
- Broadcasts a WebSocket event to `/topic/attendance/{classId}`

Success response:

```json
{
  "success": true,
  "data": {
    "classId": "class-id",
    "startTime": "2026-04-12T15:00:00Z",
    "endTime": null
  }
}
```

#### `POST /api/v1/attendance/end?classId=<classId>`

Ends the currently active attendance session for a classroom.

Authentication required: `Yes`

Allowed roles:

- `teacher`

Behavior:

- Validates that the teacher owns the class
- Validates that a session is active
- Marks the session inactive
- Sets `endTime`
- Broadcasts a WebSocket event to `/topic/attendance/{classId}`

## WebSocket API

Kyoshitsu uses STOMP over WebSocket with SockJS support for live attendance updates.

### Connection Endpoint

Connect to:

```text
/ws
```

Application destination prefix:

```text
/app
```

Broker destination prefix:

```text
/topic
```

### Topic Subscription

Subscribe students and teachers to:

```text
/topic/attendance/{classId}
```

This topic receives real-time attendance events for a specific class.

### Client Send Destination

Students send attendance marks to:

```text
/app/attendance.mark
```

Payload:

```json
{
  "classId": "class-id"
}
```

### Attendance Event Payload

The server publishes attendance events using the `AttendanceEvent` model:

```json
{
  "type": "STARTED",
  "sessionId": "session-id",
  "classId": "class-id",
  "studentId": "student-id"
}
```

Notes:

- For `STARTED`, `studentId` is `null`
- For `ENDED`, `studentId` is `null`
- For `MARKED`, `studentId` contains the student who just marked attendance

### Real-Time Attendance Flow

1. Teacher starts a session using `POST /api/v1/attendance/start`.
2. Server creates a `ClassSession`.
3. Server publishes a `STARTED` event to `/topic/attendance/{classId}`.
4. Students subscribed to that topic become aware that attendance is open.
5. A student sends a STOMP message to `/app/attendance.mark`.
6. Server validates that:
   - the class session is active
   - the student has not already marked attendance for the same session
7. Server saves an `AttendanceRecord`.
8. Server publishes a `MARKED` event to `/topic/attendance/{classId}`.
9. Teacher ends the session using `POST /api/v1/attendance/end`.
10. Server publishes an `ENDED` event.

## Functional Rules

### Authentication Rules

- `signup` and `login` are public
- all other HTTP endpoints require authentication
- role checks are enforced using method-level security

### Classroom Rules

- only teachers can create classrooms
- only the classroom owner can add students
- only enrolled students or the owning teacher can view classroom details

### Attendance Rules

- only the owning teacher can start or end a session
- only one active session is allowed per class
- students can only mark attendance when a session is active
- duplicate attendance is prevented per `sessionId + studentId`

## Example HTTP Workflow

### 1. Create teacher account

```http
POST /api/v1/auth/signup
Content-Type: application/json

{
  "name": "Teacher One",
  "email": "teacher@example.com",
  "password": "pass123",
  "role": "teacher"
}
```

### 2. Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "teacher@example.com",
  "password": "pass123"
}
```

### 3. Create class

```http
POST /api/v1/class?className=Physics
Authorization: Bearer <jwt-token>
```

### 4. Add students

```http
POST /api/v1/class/add-students
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "classId": "class-id",
  "studentIds": ["student-1", "student-2"]
}
```

### 5. Start attendance session

```http
POST /api/v1/attendance/start?classId=class-id
Authorization: Bearer <jwt-token>
```

### 6. Students mark attendance over WebSocket

Send to:

```text
/app/attendance.mark
```

Payload:

```json
{
  "classId": "class-id"
}
```

### 7. End attendance session

```http
POST /api/v1/attendance/end?classId=class-id
Authorization: Bearer <jwt-token>
```

## Testing

Run all tests:

```powershell
mvn test
```

Run the application tests only:

```powershell
mvn -Dtest=KyoshitsuApplicationTests test
```

## Implementation Notes

- JWT secret is currently hardcoded in `src/main/java/com/kiyoshi87/application/kyoshitsu/auth/JwtUtil.java` and should be externalized before production use.
- WebSocket allowed origins are configurable in `src/main/java/com/kiyoshi87/application/kyoshitsu/config/WebSocketConfig.java`.
- The project currently uses MongoDB from Docker Compose by default.

## Future Improvements

- Externalize JWT secret and database settings to environment variables
- Add Swagger/OpenAPI documentation
- Add dedicated WebSocket authentication handshake support if the frontend requires token-based socket authentication
- Add integration tests for WebSocket message flow
- Add attendance history and reporting endpoints
