# Cosmo Wallet

Plataforma full stack de gestión financiera personal. Permite administrar cuentas, movimientos y transferencias mediante una API REST segura, documentada y preparada para ejecutarse con Docker.

> Proyecto personal desarrollado para aplicar arquitectura de software, seguridad de aplicaciones y prácticas de desarrollo backend en un caso de uso real.

## Stack tecnológico

- **Backend:** Java 21, Spring Boot 3, Spring Security, JWT, Spring Data JPA, Flyway y PostgreSQL.
- **Frontend:** React, TypeScript y Vite.
- **Calidad y documentación:** Swagger/OpenAPI, Actuator, validaciones, manejo global de errores y tests.
- **Ejecución local:** Docker, Docker Compose y variables de entorno.

## Funcionalidades

- Registro e inicio de sesión con autenticación JWT.
- Creación y consulta de cuentas.
- Depósitos, retiros y transferencias entre cuentas de la misma moneda.
- Persistencia relacional con PostgreSQL y migraciones versionadas mediante Flyway.
- Documentación de endpoints con Swagger/OpenAPI.
- Endpoint de salud mediante Spring Boot Actuator.
- Protección de endpoints sensibles con seguridad stateless y rate limiting.

## Arquitectura

El backend sigue una arquitectura hexagonal para separar el dominio, los casos de uso y los adaptadores de infraestructura:

~~~text
BackEnd/src/main/java/.../wallet
├── domain          # Entidades y reglas de negocio
├── application     # Casos de uso y servicios
├── infrastructure  # Persistencia, seguridad y configuración
└── interfaces/rest # Controladores y contratos HTTP
~~~

## Ejecutar el proyecto

1. Copiar .env.example a .env y completar las variables necesarias.
2. Levantar los servicios desde la raíz del repositorio:

~~~bash
docker compose up --build
~~~

También es posible iniciar el backend desde BackEnd:

~~~bash
./mvnw spring-boot:run
~~~

La documentación de la API queda disponible en http://localhost:8080/swagger-ui.html.

## Pruebas

~~~bash
cd BackEnd
./mvnw test
~~~

## Estructura del repositorio

~~~text
WalletDigital/
├── BackEnd/       # API Java/Spring Boot
├── FrontEnd/      # Interfaz React + Vite
├── docker-compose.yml
└── .env.example
~~~

## Enlaces

- [Repositorio del backend y documentación técnica](BackEnd/README.md)
- [Perfil de LinkedIn](https://www.linkedin.com/in/germanfrench-gf/)

---

Desarrollado por [Germán Emiliano French](https://github.com/GermanFrench) — Java Backend Developer.
