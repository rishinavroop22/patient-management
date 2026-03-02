# Patient Management System

A microservices-based patient management system built with Java 21 and Spring Boot. The system handles patient registration, authentication, billing, and analytics using gRPC for inter-service communication and Apache Kafka for event streaming.

## Architecture Overview

The system is composed of the following microservices:

| Service | Port | Description |
|---|---|---|
| **api-gateway** | `4004` | Spring Cloud Gateway — routes all external requests and validates JWT tokens |
| **patient-service** | `4000` | Core service for managing patient records (REST API + Kafka producer + gRPC client) |
| **auth-service** | `4005` | User authentication and JWT token generation/validation |
| **billing-service** | `4001` / `9001` (gRPC) | Manages patient billing accounts via gRPC |
| **analytics-service** | `4002` | Consumes patient events from Kafka for analytics and metrics |

### Communication
- **REST** — External clients communicate through the API Gateway
- **gRPC** — `patient-service` communicates with `billing-service` (port `9001`)
- **Kafka** — `patient-service` publishes events consumed by `analytics-service`

```
Client
  │
  ▼
api-gateway (:4004)
  ├── /auth/**         → auth-service (:4005)
  └── /api/patients/** → patient-service (:4000)
                              ├── gRPC → billing-service (:9001)
                              └── Kafka → analytics-service
```

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.x, Spring Cloud Gateway
- **Security:** Spring Security + JWT (JJWT)
- **Databases:** PostgreSQL (production), H2 (testing)
- **Messaging:** Apache Kafka
- **RPC:** gRPC (Protocol Buffers)
- **Infrastructure:** AWS CDK v2 (ECS Fargate, RDS, MSK, ALB, VPC)
- **Local Cloud Emulation:** LocalStack
- **API Documentation:** SpringDoc OpenAPI (Swagger UI)
- **Testing:** JUnit 5, REST Assured

## Project Structure

```
patient-management/
├── api-gateway/          # Spring Cloud Gateway with JWT filter
├── auth-service/         # Authentication service
├── patient-service/      # Patient record management
├── billing-service/      # Billing account management (gRPC server)
├── analytics-service/    # Event-driven analytics (Kafka consumer)
├── infrastructure/       # AWS CDK stack for cloud deployment
├── integration-tests/    # REST API integration tests (REST Assured)
├── grpc-requests/        # gRPC request utilities for billing service
└── api-request/          # API request utilities for auth and patient services
```

## Prerequisites

- Java 21
- Maven 3.9+
- Docker
- (Optional) LocalStack for local AWS emulation

## Building and Running

### Build a Service

Each service can be built independently using the included Maven wrapper:

```bash
cd <service-name>
./mvnw clean package
```

### Run a Service Locally

```bash
cd <service-name>
./mvnw spring-boot:run
```

### Build Docker Images

Each service includes a multi-stage `Dockerfile`:

```bash
cd <service-name>
docker build -t <service-name>:latest .
```

### Run Integration Tests

```bash
cd integration-tests
mvn clean test
```

> **Note:** Integration tests expect all services to be running and accessible through the API Gateway at `localhost:4004`.

## Infrastructure Deployment

The `infrastructure/` directory contains the AWS CDK stack that provisions:
- **VPC** with public and private subnets
- **ECS Fargate** cluster for all microservices
- **Amazon RDS** (PostgreSQL 17) for `auth-service` and `patient-service`
- **Amazon MSK** (Kafka 3.6) for event streaming
- **Application Load Balancer** for the API Gateway

### Deploy to LocalStack

```bash
cd infrastructure
mvn clean install
./localstack-deploy.sh
```

## API Documentation

When services are running, Swagger UI is available via the API Gateway:

| Spec | URL |
|---|---|
| Patient Service | `http://localhost:4004/api-docs/patients` |
| Auth Service | `http://localhost:4004/api-docs/auth` |

## Authentication

All `/api/patients/**` routes require a valid JWT token. Obtain a token from the auth service and pass it in the `Authorization` header:

```
Authorization: Bearer <token>
```

The API Gateway validates the token before forwarding requests to downstream services.
