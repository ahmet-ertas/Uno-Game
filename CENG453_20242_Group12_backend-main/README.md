# CENG453_20242_Group12_backend

# 🎮 Game Server - Backend API Documentation

Welcome to the backend API documentation for our game server, developed as part of **Phase 2** of our project. This backend includes user authentication, leaderboard functionalities, and user management features, all documented and accessible via Swagger.

> **🔗 Live Swagger UI**  
> [https://ceng453-20242-group12-backend.onrender.com/swagger-ui/index.html](https://ceng453-20242-group12-backend.onrender.com/swagger-ui/index.html)

---

## 📌 Features

- User registration and login with hashed passwords  
- Password change functionality  
- Weekly, monthly, and all-time leaderboards  
- Publicly deployed backend (on Render)  
- Swagger UI documentation   

---

## 🛡️ Authentication APIs

### `POST /api/auth/register` – Register New User

Registers a new user with a unique username and securely hashed password.

**Request Example:**
```json
{
  "username": "player1",
  "password": "securePassword123"
}
```

---

### `POST /api/auth/login` – Login User

Authenticates the user and returns a token.

**Request Example:**
```json
{
  "username": "player1",
  "password": "securePassword123"
}
```

---

### `POST /api/auth/change-password` – Change Password

Allows an authenticated user to change their password.

**Request Example:**
```json
{
  "oldPassword": "securePassword123",
  "newPassword": "newSecurePassword456"
}
```

---

## 🏆 Leaderboard APIs

Players gain **+1 point** for a win and **-1 point** for a loss. These endpoints provide leaderboards based on those scores.

> ⚠️ Currently returns dummy data for testing purposes.

---

### `GET /api/leaderboard/weekly` – Weekly Leaderboard

Returns scores for the **last 7 days**.

---

### `GET /api/leaderboard/monthly` – Monthly Leaderboard

Returns scores for the **last 30 days**.

---

### `GET /api/leaderboard/all-time` – All-Time Leaderboard

Returns overall scores since account creation.

---

## 👤 User Management APIs

### `GET /api/users` – Get All Users

Returns a list of all registered users.  
Primarily intended for debugging or administrative purposes.

---

## 📚 Swagger UI Documentation

You can test and view all endpoints using Swagger:

**Swagger UI**  
[https://ceng453-20242-group12-backend.onrender.com/swagger-ui/index.html](https://ceng453-20242-group12-backend.onrender.com/swagger-ui/index.html)

---
