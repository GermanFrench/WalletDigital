# wallet-core

Backend de billetera digital construido con **Spring Boot 3**, **Java 21**, **PostgreSQL** y **arquitectura hexagonal**.

## Arquitectura

```text
com.tuusuario.wallet
├── domain
│   ├── enums
│   ├── model
│   └── repository
├── application
│   ├── service
│   └── usecase
├── infrastructure
│   ├── config
│   ├── persistence
│   └── security
└── interfaces
    └── rest
```

## Funcionalidades incluidas

- Creación de cuentas
- Consulta de cuenta y saldo
- Depósitos en cuentas existentes
- Retiros y transferencias entre cuentas de la misma moneda
- Persistencia con JPA sobre PostgreSQL
- Migraciones versionadas con Flyway
- Seguridad `stateless` con JWT
- Documentación Swagger/OpenAPI
- Actuator con endpoint `/health`
- Validación de requests y manejo global de errores

## Configuración

La aplicación usa estas variables con valores por defecto:

- `DB_URL=jdbc:postgresql://localhost:5432/walletdb`
- `DB_USERNAME=wallet_user`
- `DB_PASSWORD=wallet_pass`
- `SERVER_PORT=8080`
- `JWT_SECRET=0123456789012345678901234567890123456789012345678901234567890123`
- `JWT_EXPIRATION_MINUTES=60`

Usuario de desarrollo sembrado por migración:

- `email=admin@wallet.local`
- `password=ChangeMe123!`

## Ejecución

```powershell
.\mvnw.cmd spring-boot:run
```

Para desarrollo local sin PostgreSQL ni credenciales recordadas:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
.\mvnw.cmd spring-boot:run
```

## Pruebas

```powershell
.\mvnw.cmd test
```

## Endpoints principales

- `POST /api/auth/login`
- `POST /api/accounts`
- `GET /api/accounts/{accountId}`
- `POST /api/accounts/{accountId}/deposit`
- `POST /api/accounts/{accountId}/withdraw`
- `POST /api/accounts/{accountId}/transfer`
- `GET /health`
- `GET /swagger-ui.html`

## Ejemplo de creación de cuenta

```json
{
  "name": "Jane Doe",
  "email": "jane.doe@example.com",
  "currency": "USD"
}
```

## Ejemplo de depósito

```json
{
  "amount": 125.50
}
```

## Ejemplo de login

```json
{
  "email": "admin@wallet.local",
  "password": "ChangeMe123!"
}
```

## Ejemplo de transferencia

```json
{
  "destinationAccountId": "9e0fd835-5944-4d35-b115-6e0f3a9e9638",
  "amount": 25.00
}
```

