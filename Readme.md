# Vertex One

An enterprise-grade procurement, vendor management, and inventory platform built with Java, Spring Boot, and React.

## Project Architecture

Vertex One follows a **Modular Monolith Architecture** designed for scalability, maintainability, and future expansion.

### Technology Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot |
| Frontend | React, TypeScript |
| Database | PostgreSQL (Supabase) |
| ORM | Spring Data JPA, Hibernate |
| Database Migrations | Flyway |
| Security | Spring Security, JWT |
| API Documentation | OpenAPI / Swagger |
| Version Control | Git, GitHub |
| Containerization | Docker |

## Backend Architecture

The backend is organized into independent business modules.

```text
com.vertexone.prod
├── common
│   └── exception
├── identity
│   ├── entity
│   ├── repository
│   ├── dto
│   ├── service
│   └── config
├── organization
│   ├── entity
│   ├── repository
│   ├── dto
│   └── service
├── membership
│   ├── entity
│   ├── enums
│   ├── repository
│   ├── dto
│   └── service
└── ProdApplication.java
```

## Development Roadmap

Development follows a dependency-first approach.

| Phase | Module | Status |
|---|---|---|
| 1 | Organization and Identity Foundation | In Progress |
| 2 | DTOs and Exception Handling | In Progress |
| 3 | Service Layer | In Progress |
| 4 | REST Controllers | Planned |
| 5 | Authentication and Authorization | Planned |
| 6 | Vendor Management | Planned |
| 7 | Product and Catalog Management | Planned |
| 8 | Inventory Management | Planned |
| 9 | Procurement Management | Planned |
| 10 | Receiving Management | Planned |
| 11 | Finance Management | Planned |
| 12 | Audit Logging | Planned |
| 13 | Frontend Integration | Planned |
| 14 | Testing and Deployment | Planned |

## Multi-Tenant Architecture

Vertex One is designed to support multiple organizations.

- A user can belong to multiple organizations.
- Membership connects users with organizations.
- Each membership has an organization-specific role.
- Membership status controls organizational access.

### Core Relationship

```text
User
  |
  | One-to-Many
  |
Membership
  |
  | Many-to-One
  |
Organization
```

## Git Workflow

Git is used to track development progress and maintain a clear history of project changes.

Development cycle:

1. Implement a development layer.
2. Review the code and architecture.
3. Test the completed layer.
4. Commit the changes.
5. Push the changes to GitHub.

### Commit Message Convention

We follow Conventional Commits.

| Prefix | Purpose |
|---|---|
| feat | New functionality |
| fix | Bug fixes |
| refactor | Code restructuring |
| test | Adding or updating tests |
| chore | Configuration and maintenance |
| docs | Documentation updates |

### Example Commit Messages

```bash
feat: add identity and organization foundation
feat: add identity and organization DTOs
feat: add identity, organization, and membership services
feat: add authentication controllers
fix: resolve organization membership validation
test: add identity service integration tests
docs: update project architecture and roadmap
```

## Development Principles

- Modular monolith before microservices.
- Separation of concerns.
- DTO-based API communication.
- Database migrations managed through Flyway.
- Secure password hashing.
- Organization-level access control.
- Maintainable and testable code.
- Consistent Git version control.

## Project Status

Vertex One is currently under active backend development. The initial identity, organization, and membership foundations are being implemented before moving into vendor and procurement modules.

---

**Built as a long-term enterprise software engineering project.**