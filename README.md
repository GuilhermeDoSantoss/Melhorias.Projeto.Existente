## 📡 Communication API

This is a RESTful API developed in Java using Spring Boot, designed to manage internal communication messages with integrated email notification support. It includes full documentation, DTO-Entity mapping, exception handling, Docker support, and unit testing.

---

## 🔗 Endpoints

- **GET /api/comunicacoes**  
  Returns all registered communications.

- **POST /api/comunicacoes**  
  Registers a new communication.  
  Request body:
  ```json
  {
    "titulo": "Important Update",
    "mensagem": "The system will be down for maintenance.",
    "emailDestino": "user@example.com"
  }
- **PUT /api/comunicacoes/{id}**
Updates an existing communication.

- **DELETE /api/comunicacoes/{id}**
Deletes a communication by ID.


## 🚀 Features

 **✅ Swagger UI**

Integrated with SpringDoc OpenAPI 3.
Access the live documentation at:
http://localhost:8080/swagger-ui.html

**✅ Email Notification Integration**

The API is integrated with an external email service to send notifications automatically upon communication creation or updates.

 **✅ MapStruct Integration**

Entity-DTO mapping is fully managed with MapStruct, providing clear separation between layers and reducing boilerplate code.

 **✅ Global Exception Handling**

Custom GlobalExceptionHandler implemented to standardize and improve error responses using @RestControllerAdvice.

 **✅ Docker Support**

Dockerfile and docker-compose ready for deployment.
Command to run:

docker compose up --build


 **✅ Unit Tests**

JUnit and Mockito used for unit testing the Controller and Service layers.


## 🛠️ Tech Stack

Java 17

Spring Boot

Spring Data JPA

OpenAPI/Swagger (SpringDoc)

MapStruct

Docker

JUnit 5 + Mockito

REST Template
