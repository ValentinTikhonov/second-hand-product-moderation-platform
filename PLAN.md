# Second-Hand Product Moderation Platform — 5-Day Execution Plan

## Problem Statement
Build a REST API using Java 21 + Spring Boot 3 for a second-hand product moderation platform.
Key concerns: clean domain modelling, state machine transitions, JWT auth, role-based access, event recording, reproducible seed data, and solid tests.

## Chosen Stack
| Concern | Technology | Reason |
|---|---|---|
| Runtime | Java 21 + Spring Boot 3.5 | Required |
| Persistence | H2 (embedded, file-mode) + Flyway | Zero-infra, reproducible, file persisted between runs |
| Migrations | Flyway | Already in pom.xml; schema is code |
| Auth | Spring Security + JJWT (io.jsonwebtoken) | Industry standard JWT |
| Mapping | MapStruct | Bonus credit; compile-time safe |
| Code gen | Lombok | Already in pom.xml |
| Docs | springdoc-openapi (Swagger UI) | Bonus credit |
| Tests | JUnit 5 + Mockito + AssertJ + MockMvc | Already in pom.xml |

---

## Architecture Blueprint

```
┌─────────────────────────────────────────────────────────────────────┐
│                         HTTP Clients                                │
│              (Postman / Browser / Integration Tests)                │
└───────────────────────────────┬─────────────────────────────────────┘
                                │  REST (JSON/JWT)
┌───────────────────────────────▼─────────────────────────────────────┐
│  INFRASTRUCTURE LAYER — Web                                         │
│                                                                     │
│  ┌─────────────────┐  ┌───────────────────┐  ┌──────────────────┐  │
│  │ AuthController  │  │ ProductController │  │ModerationCtrl   │  │
│  │ POST /auth/login│  │ POST /products    │  │POST .../claim    │  │
│  └────────┬────────┘  │ GET  /products    │  │POST .../approve  │  │
│           │           │ GET  /me/products │  │POST .../reject   │  │
│           │           │ GET  /products/:id│  └────────┬─────────┘  │
│           │           │ PATCH/DELETE      │           │            │
│           │           │ POST .../submit   │           │            │
│           │           └────────┬──────────┘           │            │
│           │                    │                      │            │
│  ┌────────▼────────────────────▼──────────────────────▼──────────┐ │
│  │ Request DTOs     (Bean Validation annotations)                │ │
│  │  LoginRequest · CreateProductRequest · UpdateProductRequest   │ │
│  │  RejectProductRequest                                         │ │
│  └────────────────────────────┬───────────────────────────────── ┘ │
│                               │  MapStruct (Request → Command)     │
└───────────────────────────────┼─────────────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────────────┐
│  APPLICATION LAYER — Use Cases (orchestration, no domain leakage)   │
│                                                                     │
│  Commands / Queries (plain records)                                 │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │CreateProductCmd  │  │UpdateProductCmd  │  │RejectProductCmd  │  │
│  │ListProductsQuery │  │ClaimProductCmd   │  │ApproveProductCmd │  │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘  │
│                                                                     │
│  Use Case Implementations                                           │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │ CreateProduct · UpdateProduct · DeleteProduct · SubmitProduct  │ │
│  │ GetProduct · ListProducts · ClaimProduct · ApproveProduct      │ │
│  │ RejectProduct · GetProductEvents                               │ │
│  └────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────┬─────────────────────────────────────┘
                                │  calls domain services + ports
┌───────────────────────────────▼─────────────────────────────────────┐
│  DOMAIN LAYER — Business models, rules, ports (no Spring deps)      │
│                                                                     │
│  Models (pure Java records/classes)                                 │
│  ┌────────────┐  ┌────────────┐  ┌──────────────┐  ┌────────────┐   │
│  │  Product   │  │   User     │  │ProductEvent  │  │  Price     │   │
│  │            │  │            │  │              │  │ (Value Obj)│   │
│  └─────┬──────┘  └─────┬──────┘  └──────┬───────┘  └────────────┘   │
│        │               │                │                           │
│  Enums: ProductState · UserRole · ProductCategory · ProductSize     │
│         ProductCondition · ProductEventType                         │
│                                                                     │
│  Domain Services                                                    │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ ProductStateTransitionValidator                              │   │
│  │  validate(current, target, actor) → throws or passes        │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  Ports (interfaces only, no impl)                                   │
│  ┌──────────────────────┐  ┌───────────────────────────────────┐   │
│  │  OUT-PORTS           │  │  IN-PORTS (Use Case interfaces)   │   │
│  │  ProductRepository   │  │  CreateProductUseCase             │   │
│  │  UserRepository      │  │  UpdateProductUseCase             │   │
│  │  ProductEventRepo    │  │  ClaimProductUseCase  (etc.)      │   │
│  │  ProductEventPublish │  └───────────────────────────────────┘   │
│  └──────────────────────┘                                           │
│                                                                     │
│  Domain Exceptions                                                  │
│  InvalidStateTransitionException · ProductNotFoundException         │
│  UnauthorizedProductAccessException · SellerBlockedException        │
└───────────────────────────────┬─────────────────────────────────────┘
                                │  implements ports
┌───────────────────────────────▼─────────────────────────────────────┐
│  INFRASTRUCTURE LAYER — Persistence                                 │
│                                                                     │
│  JPA Entities (separate from domain models)                        │
│  ┌───────────────┐  ┌───────────────┐  ┌────────────────────────┐  │
│  │ ProductEntity │  │ UserEntity    │  │ ProductEventEntity     │  │
│  └───────┬───────┘  └───────┬───────┘  └────────────┬───────────┘  │
│          │                  │                        │              │
│  Spring Data JPA Repositories (interface only)                     │
│  JpaProductRepository · JpaUserRepository · JpaProductEventRepo    │
│                                                                     │
│  Repository Adapters (implement domain ports)                       │
│  ProductRepositoryAdapter · UserRepositoryAdapter                   │
│  ProductEventRepositoryAdapter · ProductEventPublisherAdapter       │
│                                                                     │
│  MapStruct Entity Mappers (Entity ↔ Domain)                        │
│  ProductEntityMapper · UserEntityMapper · EventEntityMapper        │
│                                                                     │
│  Security                                                           │
│  ┌──────────────┐ ┌──────────────────┐ ┌───────────────────────┐   │
│  │  JwtService  │ │  JwtAuthFilter   │ │  SecurityConfig       │   │
│  │ sign/validate│ │ (OncePerRequest) │ │ (endpoint rules)      │   │
│  └──────────────┘ └──────────────────┘ └───────────────────────┘   │
│                                                                     │
│  Seed Loader                                                        │
│  SeedDataLoader (ApplicationRunner) — reads seed-file.json         │
└─────────────────────────────────────────────────────────────────────┘
```

---

## DTO & Relations Blueprint

```
┌─────────────────────────────────────────────────────────────────────┐
│  REQUEST DTOs  (infrastructure/web/dto/request)                     │
│                                                                     │
│  LoginRequest                                                       │
│  ┌──────────────────────────────────┐                              │
│  │ username: String @NotBlank       │                              │
│  │ password: String @NotBlank       │                              │
│  └──────────────────────────────────┘                              │
│                                                                     │
│  CreateProductRequest                                               │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ title:        String   @NotBlank                             │  │
│  │ description:  String   @NotBlank                             │  │
│  │ category:     String   @NotNull (CLOTHING/SHOES/BAGS/ACCESS) │  │
│  │ size:         String   @NotNull (XS/S/M/L/XL/ONE_SIZE)      │  │
│  │ condition:    String   @NotNull (NEW_WITH_TAGS/.../ACCEPTABL)│  │
│  │ termsAccepted:boolean  @AssertTrue                           │  │
│  │ price:        PriceDto @NotNull @Valid                       │  │
│  │ imageUrls:    List<String> @Size(min=3,max=10) @NotEmpty     │  │
│  └──────────────────────┬───────────────────────────────────────┘  │
│                         │ embeds                                    │
│  UpdateProductRequest   │                                          │
│  ┌──────────────────────▼───────────────────────────────────────┐  │
│  │ title:       String? (nullable = partial update)             │  │
│  │ description: String?                                         │  │
│  │ category:    String?                                         │  │
│  │ size:        String?                                         │  │
│  │ condition:   String?                                         │  │
│  │ price:       PriceDto? @Valid                                │  │
│  │ imageUrls:   List<String>? @Size(min=3,max=10) if present   │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  RejectProductRequest                                               │
│  ┌──────────────────────────────────┐                              │
│  │ reason: String @NotBlank         │                              │
│  └──────────────────────────────────┘                              │
│                                                                     │
│  PriceDto  (shared between request & response)                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ currency: String  @NotBlank @Size(min=3,max=3) @Pattern      │  │
│  │ amount:   Long    @Positive                                  │  │
│  │ exponent: Integer @Min(0) @Max(4)                            │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│  RESPONSE DTOs  (infrastructure/web/dto/response)                   │
│                                                                     │
│  LoginResponse                                                      │
│  ┌───────────────────────────────────────────────────────┐        │
│  │ accessToken: String                                   │        │
│  │ user: UserDto ─────────────────────────────────────┐  │        │
│  └───────────────────────────────────────────────────┬─┘        │
│                                                      │           │
│  UserDto                                             │           │
│  ┌───────────────────────────────────────────────────▼──────┐   │
│  │ id:       Long                                           │   │
│  │ username: String                                         │   │
│  │ role:     String (SELLER | MODERATOR)                   │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ProductResponse                                                    │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ id:          String                                          │  │
│  │ sellerId:    Long                                            │  │
│  │ title:       String                                          │  │
│  │ description: String                                          │  │
│  │ category:    String                                          │  │
│  │ size:        String                                          │  │
│  │ condition:   String                                          │  │
│  │ state:       String  (ProductState enum value)              │  │
│  │ price:       PriceDto ◄── embedded value object            │  │
│  │ imageUrls:   List<String>                                   │  │
│  │ reviewerId:  Long?  (null until IN_REVIEW)                  │  │
│  │ createdAt:   Instant                                         │  │
│  │ updatedAt:   Instant                                         │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ProductEventResponse                                               │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ id:        String (UUID)                                     │  │
│  │ productId: String                                            │  │
│  │ eventType: String  (CREATED/SUBMITTED/CLAIMED/APPROVED/etc) │  │
│  │ actorId:   Long?                                             │  │
│  │ timestamp: Instant                                           │  │
│  │ metadata:  Map<String, Object>? (reason, prev/new state)    │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ErrorResponse                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ code:    String  (e.g. "invalid-product-state-transition")  │  │
│  │ message: String  (human-readable description)               │  │
│  │ field:   String? (null if not field-specific)               │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  PagedResponse<T>  (generic wrapper)                               │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ content:       List<T>                                       │  │
│  │ page:          int                                           │  │
│  │ size:          int                                           │  │
│  │ totalElements: long                                          │  │
│  │ totalPages:    int                                           │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│  APPLICATION COMMANDS & QUERIES  (application/command)              │
│                                                                     │
│  CreateProductCommand                                               │
│  ┌───────────────────────────────────────────────────────────┐    │
│  │ sellerId, title, description, category, size, condition,  │    │
│  │ price (Price VO), imageUrls, termsAccepted                │    │
│  └───────────────────────────────────────────────────────────┘    │
│                                                                     │
│  UpdateProductCommand                                               │
│  ┌───────────────────────────────────────────────────────────┐    │
│  │ productId, sellerId (auth),                               │    │
│  │ title?, description?, category?, size?, condition?,       │    │
│  │ price?, imageUrls?                                        │    │
│  └───────────────────────────────────────────────────────────┘    │
│                                                                     │
│  RejectProductCommand                                               │
│  ┌───────────────────────────────────────────────────────────┐    │
│  │ productId, moderatorId (from JWT), reason                 │    │
│  └───────────────────────────────────────────────────────────┘    │
│                                                                     │
│  ListProductsQuery                                                  │
│  ┌───────────────────────────────────────────────────────────┐    │
│  │ state?, sellerId?, category?, page, size                  │    │
│  └───────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│  DOMAIN MODELS  (domain/model)  — no framework deps                 │
│                                                                     │
│  Product                  User                                     │
│  ┌────────────────────┐   ┌────────────────────────────────┐      │
│  │ id: String         │   │ id: Long                       │      │
│  │ sellerId: Long ────┼──►│ username: String               │      │
│  │ title: String      │   │ displayName: String            │      │
│  │ description: String│   │ passwordHash: String           │      │
│  │ category: enum     │   │ role: UserRole (enum)          │      │
│  │ size: enum         │   │ blocked: boolean               │      │
│  │ condition: enum    │   └────────────────────────────────┘      │
│  │ state: ProductState│                                            │
│  │ price: Price ──────┼──► Price (Value Object)                   │
│  │ imageUrls: List<>  │    ┌──────────────────┐                   │
│  │ reviewerId: Long? ─┼──► │ currency: String │                   │
│  │ termsAccepted: bool│    │ amount: Long     │                   │
│  │ createdAt: Instant │    │ exponent: Integer│                   │
│  │ updatedAt: Instant │    └──────────────────┘                   │
│  └────────────┬───────┘                                            │
│               │ 1:N                                                │
│  ProductEvent │                                                    │
│  ┌────────────▼───────────────────────────────────────────────┐   │
│  │ id: String (UUID)                                          │   │
│  │ productId: String  ◄── FK to Product                      │   │
│  │ eventType: ProductEventType (enum)                         │   │
│  │ actorId: Long?     ◄── FK to User (nullable)              │   │
│  │ timestamp: Instant                                         │   │
│  │ metadata: Map<String, String>? (rejection reason, states) │   │
│  └────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## State Machine

```
                    ┌──────────────────────────────────────┐
                    │         STATE TRANSITION TABLE        │
                    ├──────────────┬──────────────┬─────────┴──────────────────────┐
                    │ FROM         │ TO           │ ACTOR + GUARD                  │
                    ├──────────────┼──────────────┼────────────────────────────────┤
                    │ DRAFT        │ PENDING_REV  │ Seller owner (submit)          │
                    │ REJECTED     │ PENDING_REV  │ Seller owner (resubmit)        │
                    │ PENDING_REV  │ IN_REVIEW    │ Any Moderator (claim-next)     │
                    │ IN_REVIEW    │ ACTIVE       │ Assigned Mod + seller !blocked │
                    │ IN_REVIEW    │ PAUSED       │ Assigned Mod + seller blocked  │
                    │ IN_REVIEW    │ REJECTED     │ Assigned Mod + reason required │
                    │ DRAFT        │ DELETED      │ Seller owner (soft delete)     │
                    │ ACTIVE       │ DELETED      │ Seller owner (soft delete)     │
                    │ PAUSED       │ DELETED      │ Seller owner (soft delete)     │
                    │ REJECTED     │ DELETED      │ Seller owner (soft delete)     │
                    │ SOLD         │ —            │ Terminal — no transitions      │
                    │ DELETED      │ —            │ Terminal — no transitions      │
                    └──────────────┴──────────────┴────────────────────────────────┘

  DRAFT ──[submit]──────────────────► PENDING_REVIEW ──[claim]──► IN_REVIEW
   ▲ ▲                                     ▲                          │   │
   │ └────────────────────────────┐         │                          │   │
   │                         REJECTED ◄─────┼──────────[reject+reason]─┘   │
   │                              │         │                               │
   │                         [resubmit]     │                               ▼
   │                              │         │                      ACTIVE (seller !blocked)
   └─[DELETED is terminal]──────  │         │                      PAUSED (seller blocked)
                                  │         │
                    All deletable states (DRAFT, ACTIVE, PAUSED, REJECTED)
                                  │
                                  ▼
                              DELETED (terminal)
                    SOLD (terminal — no seller action moves here in scope)
```

---

## Package Structure

```
src/main/java/taylor/second_hand/product/moderation/platform/
├── domain/
│   ├── model/
│   │   ├── Product.java
│   │   ├── User.java
│   │   ├── ProductEvent.java
│   │   └── Price.java               (value object — record)
│   ├── enums/
│   │   ├── ProductState.java
│   │   ├── UserRole.java
│   │   ├── ProductCategory.java
│   │   ├── ProductSize.java
│   │   ├── ProductCondition.java
│   │   └── ProductEventType.java
│   ├── port/
│   │   ├── out/
│   │   │   ├── ProductRepository.java
│   │   │   ├── UserRepository.java
│   │   │   ├── ProductEventRepository.java
│   │   │   └── ProductEventPublisher.java
│   │   └── in/
│   │       ├── CreateProductUseCase.java
│   │       ├── UpdateProductUseCase.java
│   │       ├── DeleteProductUseCase.java
│   │       ├── SubmitProductUseCase.java
│   │       ├── GetProductUseCase.java
│   │       ├── ListProductsUseCase.java
│   │       ├── ClaimProductUseCase.java
│   │       ├── ApproveProductUseCase.java
│   │       ├── RejectProductUseCase.java
│   │       └── GetProductEventsUseCase.java
│   ├── service/
│   │   └── ProductStateTransitionValidator.java
│   └── exception/
│       ├── InvalidStateTransitionException.java
│       ├── ProductNotFoundException.java
│       ├── UserNotFoundException.java
│       ├── UnauthorizedProductAccessException.java
│       └── DuplicateClaimException.java
├── application/
│   ├── command/
│   │   ├── CreateProductCommand.java   (record)
│   │   ├── UpdateProductCommand.java   (record)
│   │   ├── RejectProductCommand.java   (record)
│   │   └── ListProductsQuery.java      (record)
│   └── usecase/
│       ├── CreateProductUseCaseImpl.java
│       ├── UpdateProductUseCaseImpl.java
│       ├── DeleteProductUseCaseImpl.java
│       ├── SubmitProductUseCaseImpl.java
│       ├── GetProductUseCaseImpl.java
│       ├── ListProductsUseCaseImpl.java
│       ├── ClaimProductUseCaseImpl.java
│       ├── ApproveProductUseCaseImpl.java
│       ├── RejectProductUseCaseImpl.java
│       └── GetProductEventsUseCaseImpl.java
├── infrastructure/
│   ├── web/
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── ProductController.java
│   │   │   ├── ModerationController.java
│   │   │   └── ProductEventController.java
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── CreateProductRequest.java
│   │   │   │   ├── UpdateProductRequest.java
│   │   │   │   └── RejectProductRequest.java
│   │   │   └── response/
│   │   │       ├── LoginResponse.java
│   │   │       ├── UserDto.java
│   │   │       ├── ProductResponse.java
│   │   │       ├── PriceDto.java
│   │   │       ├── ProductEventResponse.java
│   │   │       ├── ErrorResponse.java
│   │   │       └── PagedResponse.java
│   │   ├── mapper/
│   │   │   ├── ProductWebMapper.java    (MapStruct: domain ↔ response DTO)
│   │   │   └── EventWebMapper.java
│   │   ├── security/
│   │   │   ├── JwtService.java
│   │   │   ├── JwtAuthFilter.java
│   │   │   ├── UserDetailsServiceImpl.java
│   │   │   └── SecurityConfig.java
│   │   └── advice/
│   │       └── GlobalExceptionHandler.java   (@RestControllerAdvice)
│   └── persistence/
│       ├── entity/
│       │   ├── ProductEntity.java
│       │   ├── UserEntity.java
│       │   └── ProductEventEntity.java
│       ├── jpa/
│       │   ├── JpaProductRepository.java
│       │   ├── JpaUserRepository.java
│       │   └── JpaProductEventRepository.java
│       ├── adapter/
│       │   ├── ProductRepositoryAdapter.java
│       │   ├── UserRepositoryAdapter.java
│       │   ├── ProductEventRepositoryAdapter.java
│       │   └── ProductEventPublisherAdapter.java
│       └── mapper/
│           ├── ProductEntityMapper.java  (MapStruct: entity ↔ domain)
│           └── UserEntityMapper.java
└── boot/
    ├── Application.java
    ├── config/
    │   └── AppConfig.java   (bean wiring if needed)
    └── seed/
        ├── SeedDataLoader.java   (ApplicationRunner)
        └── SeedProperties.java

src/main/resources/
├── application.yml
└── db/migration/
    ├── V1__create_schema.sql
    └── V2__seed_data.sql      (optional — or use SeedDataLoader)

src/test/java/.../
├── domain/
│   ├── ProductStateTransitionValidatorTest.java
│   └── PriceValidationTest.java
├── application/
│   ├── CreateProductUseCaseTest.java
│   ├── ClaimProductUseCaseTest.java
│   ├── ApproveProductUseCaseTest.java
│   ├── RejectProductUseCaseTest.java
│   └── SubmitProductUseCaseTest.java
└── infrastructure/
    ├── web/
    │   ├── AuthControllerTest.java     (MockMvc)
    │   ├── ProductControllerTest.java  (MockMvc)
    │   └── ModerationControllerTest.java
    └── persistence/
        └── ProductRepositoryAdapterTest.java  (H2)

postman/
├── product-moderation-api.postman_collection.json
└── local.postman_environment.json
```

---

## DB Schema (Flyway V1)

```sql
-- users
CREATE TABLE users (
    id            BIGINT PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    display_name  VARCHAR(200),
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,   -- SELLER | MODERATOR
    blocked       BOOLEAN      NOT NULL DEFAULT FALSE
);

-- products
CREATE TABLE products (
    id            VARCHAR(50)  PRIMARY KEY,
    seller_id     BIGINT       NOT NULL REFERENCES users(id),
    title         VARCHAR(255) NOT NULL,
    description   TEXT         NOT NULL,
    category      VARCHAR(20)  NOT NULL,
    size          VARCHAR(20)  NOT NULL,
    condition     VARCHAR(20)  NOT NULL,
    state         VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    price_currency VARCHAR(3)  NOT NULL,
    price_amount  BIGINT       NOT NULL,
    price_exponent INT         NOT NULL,
    terms_accepted BOOLEAN     NOT NULL DEFAULT FALSE,
    reviewer_id   BIGINT       REFERENCES users(id),
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

-- product_image_urls (1:N)
CREATE TABLE product_image_urls (
    id         BIGINT       PRIMARY KEY AUTO_INCREMENT,
    product_id VARCHAR(50)  NOT NULL REFERENCES products(id),
    url        VARCHAR(500) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0
);

-- product_events
CREATE TABLE product_events (
    id         VARCHAR(50)  PRIMARY KEY,
    product_id VARCHAR(50)  NOT NULL REFERENCES products(id),
    event_type VARCHAR(50)  NOT NULL,
    actor_id   BIGINT       REFERENCES users(id),
    timestamp  TIMESTAMP    NOT NULL,
    metadata   TEXT         -- JSON string for flexibility
);
```

---

## Endpoint Permission Matrix

```
┌─────────────────────────────────────────┬─────────────────────────────┐
│ Endpoint                                │ Required Actor               │
├─────────────────────────────────────────┼─────────────────────────────┤
│ POST   /auth/login                      │ Public                       │
│ POST   /products                        │ SELLER                       │
│ GET    /products                        │ SELLER or MODERATOR          │
│ GET    /me/products                     │ SELLER                       │
│ GET    /products/{id}                   │ SELLER or MODERATOR          │
│ PATCH  /products/{id}                   │ Product owner seller         │
│ DELETE /products/{id}                   │ Product owner seller         │
│ POST   /products/{id}/submit-review     │ Product owner seller         │
│ POST   /moderation/products/claim-next  │ MODERATOR                    │
│ POST   /moderation/products/{id}/approve│ Assigned moderator (JWT)     │
│ POST   /moderation/products/{id}/reject │ Assigned moderator (JWT)     │
│ GET    /products/{id}/events            │ Owner seller or MODERATOR    │
│ GET    /actuator/health                 │ Public                       │
└─────────────────────────────────────────┴─────────────────────────────┘
```

---

## 5-Day Execution Plan

### Day 1 — Foundation: Domain Model + Project Wiring

**Goal:** The project compiles, domain model is complete, and the state machine is the authoritative source of truth for transitions.

**Tasks:**
1. Add missing Maven dependencies to `pom.xml`:
   - `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` (JWT)
   - `org.mapstruct:mapstruct` + `mapstruct-processor`
   - `org.springdoc:springdoc-openapi-starter-webmvc-ui` (Swagger)
   - Configure MapStruct annotation processor alongside Lombok in `maven-compiler-plugin`

2. Create `application.yml`:
   - H2 datasource (file mode: `jdbc:h2:file:./data/moderation`)
   - Flyway config
   - JWT secret + expiry env-var wiring (`${JWT_SECRET:local-dev-secret-32chars}`)
   - Actuator health endpoint exposure

3. Create all domain **enums**: `ProductState`, `UserRole`, `ProductCategory`, `ProductSize`, `ProductCondition`, `ProductEventType`

4. Create **domain models** (pure Java, no Spring):
   - `Price` (Java record — value object)
   - `User` (class with all fields)
   - `Product` (class with all fields)
   - `ProductEvent` (class with all fields)

5. Create **domain exceptions** (5 classes extending RuntimeException)

6. Create **port interfaces** (out-ports: `ProductRepository`, `UserRepository`, `ProductEventRepository`, `ProductEventPublisher`; in-ports: all 10 use case interfaces)

7. Implement `ProductStateTransitionValidator` — builds an allowed transition map and `validate()` throws `InvalidStateTransitionException` for unknown transitions

8. Write `V1__create_schema.sql` Flyway migration (4 tables as above)

9. **Smoke test:** `./mvnw compile` must succeed

---

### Day 2 — Application Layer: Use Cases

**Goal:** All business logic lives here; controllers and persistence are not yet needed. Use cases are fully unit-testable.

**Tasks:**
1. Create application **command/query records** (4 classes)

2. Implement use cases (each class: inject repos + publisher + state validator):
   - `CreateProductUseCaseImpl` — generate UUID id, set DRAFT, publish CREATED event
   - `UpdateProductUseCaseImpl` — check ownership, check editable state (DRAFT/REJECTED), apply partial fields
   - `SubmitProductUseCaseImpl` — validate transition DRAFT/REJECTED→PENDING_REVIEW, publish SUBMITTED event
   - `DeleteProductUseCaseImpl` — validate deletable states, set DELETED, publish DELETED event
   - `GetProductUseCaseImpl` — find by id or 404
   - `ListProductsUseCaseImpl` — delegate to repo with filter params + paging
   - `ClaimProductUseCaseImpl` — use `@Transactional` pessimistic lock or `findFirstByStateOrderByCreatedAt`, set IN_REVIEW, set reviewerId, publish CLAIMED event
   - `ApproveProductUseCaseImpl` — check reviewer match, check seller blocked flag → ACTIVE or PAUSED, publish APPROVED event
   - `RejectProductUseCaseImpl` — check reviewer match, require reason, set REJECTED, publish REJECTED event with metadata
   - `GetProductEventsUseCaseImpl` — return events for product ordered by timestamp

3. **Concurrency guard for claim:** use `SELECT ... FOR UPDATE` via `@Lock(PESSIMISTIC_WRITE)` on the JPA query, or optimistic locking with `@Version` field on `ProductEntity`

4. Wire `ProductEventPublisherAdapter` stub (saves events to repo) — the port abstraction makes this testable without real persistence

---

### Day 3 — Infrastructure: Persistence + Security + Auth

**Goal:** Data flows from HTTP → use case → H2; JWT tokens work.

**Tasks:**
1. Create **JPA entities** with Lombok (`@Entity`, `@Table`, `@Id`, `@Embedded` for Price, `@ElementCollection` for imageUrls, `@Version` for optimistic lock on Product)

2. Implement **Spring Data JPA repositories**:
   - `JpaProductRepository` — add `findByStateAndSellerIdAndCategory(...)` with `@Query` or derived methods; `findFirstByStateOrderByCreatedAtAsc` for claim-next
   - `JpaUserRepository` — `findByUsername`
   - `JpaProductEventRepository` — `findByProductIdOrderByTimestampAsc`

3. Implement **MapStruct entity mappers** (`@Mapper(componentModel = "spring")`) — entity ↔ domain

4. Implement **repository adapters** (implement domain port interfaces, inject JPA repo + entity mapper)

5. Implement `ProductEventPublisherAdapter` — on publish, save event entity via JPA repo

6. **JWT Security:**
   - `JwtService` — `generateToken(User)`, `extractUserId()`, `extractRole()`, `isTokenValid()`
   - `JwtAuthFilter` extends `OncePerRequestFilter` — read `Authorization: Bearer <token>`, validate, set `SecurityContextHolder`
   - `UserDetailsServiceImpl` — load by username, return `UserDetails` with role
   - `SecurityConfig` — disable CSRF, stateless session, permit `/auth/login` + `/actuator/health`, require auth on all else

7. `AuthController` — POST `/auth/login`, validate credentials, return `LoginResponse` with JWT

8. **Verify:** Start the app, hit `POST /auth/login` — get a token back

---

### Day 4 — REST Controllers + Error Handling + Seed Data

**Goal:** All endpoints work end-to-end; error responses are consistent; seed data loads on startup.

**Tasks:**
1. Implement **request DTOs** with Bean Validation annotations (4 classes)

2. Implement **response DTOs** (6 classes + PagedResponse)

3. Implement **MapStruct web mappers** (domain → response DTO)

4. Implement `ProductController` (all 6 product endpoints + submit-review):
   - Extract authenticated user id from `SecurityContextHolder` / inject `@AuthenticationPrincipal`
   - Map request → command → use case → response

5. Implement `ModerationController` (claim-next, approve, reject)

6. Implement `ProductEventController` (GET events — authorization check: owner or moderator)

7. Implement `GlobalExceptionHandler` (`@RestControllerAdvice`):
   - Map each domain exception → HTTP status + `ErrorResponse`
   - `InvalidStateTransitionException` → 409
   - `ProductNotFoundException` / `UserNotFoundException` → 404
   - `UnauthorizedProductAccessException` → 403
   - `DuplicateClaimException` → 409
   - `MethodArgumentNotValidException` → 400 with field errors
   - Generic 500 fallback

8. Implement `SeedDataLoader` (`ApplicationRunner`):
   - Parse `seed-file.json` with `ObjectMapper`
   - Hash passwords with `BCryptPasswordEncoder`
   - Use `UserRepository` and `ProductRepository` to upsert; idempotent (skip if id already exists)
   - Load on every startup so H2 file mode persists between restarts

9. Configure **OpenAPI/Swagger** (`springdoc-openapi`) — accessible at `/swagger-ui.html`

10. Manual Postman test of all flows

---

### Day 5 — Tests + Postman Collection + README + Polish

**Goal:** Required tests pass; deliverables are complete; README is clear.

**Tasks:**
1. **Domain unit tests** (no Spring context):
   - `ProductStateTransitionValidatorTest` — all valid transitions pass, all invalid transitions throw
   - `PriceValidationTest` — boundary tests for currency length, amount, exponent

2. **Application use case tests** (Mockito mocks for repositories):
   - `CreateProductUseCaseTest` — happy path + validation
   - `SubmitProductUseCaseTest` — valid transitions, invalid transitions
   - `ClaimProductUseCaseTest` — claim increments reviewerId; second claim on same product throws
   - `ApproveProductUseCaseTest` — non-blocked seller → ACTIVE; blocked seller → PAUSED
   - `RejectProductUseCaseTest` — with reason → REJECTED; without reason → exception
   - `DeleteProductUseCaseTest` — deletable states; non-deletable states throw

3. **Web/integration tests** (MockMvc + `@WebMvcTest` or `@SpringBootTest`):
   - `AuthControllerTest` — successful login returns JWT; wrong password returns 401
   - `ProductControllerTest`:
     - No token → 401
     - Wrong seller updates another's product → 403
     - Update non-editable product → 409
     - Product creation validation (missing title, bad price, bad image count)
   - `ModerationControllerTest`:
     - Two moderators cannot claim same product (concurrent test)
     - Approve from non-blocked seller → ACTIVE
     - Approve from blocked seller → PAUSED
     - Reject without reason → 400
     - Non-assigned moderator tries to approve → 403
     - Invalid transition → 409

4. **Postman collection** (`postman/product-moderation-api.postman_collection.json`):
   - Variables: `baseUrl`, `sellerAccessToken`, `moderatorAccessToken`, `productId`
   - All 16+ scenarios from CHALLENGE.md
   - Include test scripts for status codes + token capture

5. **README.md** — must include:
   - Quick start (clone → `./mvnw spring-boot:run` → seed auto-loads)
   - Test command (`./mvnw test`)
   - JWT config (env var `JWT_SECRET` + default local secret)
   - Postman import instructions
   - State transition diagram (text table)
   - Technical decisions section
   - AI usage section

6. **Final checks:**
   - `./mvnw test` all pass
   - `./mvnw spring-boot:run` starts cleanly
   - Seed data loads idempotently on second start
   - Swagger UI shows all endpoints

---

## Key Technical Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Persistence | H2 file-mode + Flyway | Zero infrastructure, schema-as-code, deterministic migrations |
| Concurrency for claim-next | `@Lock(PESSIMISTIC_WRITE)` | Prevents two moderators claiming same product without distributed locking |
| Product images | `@ElementCollection` | Avoids a separate entity for simple string list; clean schema |
| Partial update | Nullable fields in `UpdateProductRequest` | More realistic API; only provided fields overwrite |
| Seller ID resolution | From JWT, not request body | Security requirement — clients cannot spoof ownership |
| Event storage | Persisted `ProductEventEntity` (not in-memory) | Survives restarts; satisfies event auditability |
| Event publisher port | `ProductEventPublisher` interface | Decouples domain from persistence; swappable (queue, outbox, etc.) |
| Password hashing | BCrypt (Spring Security default) | Industry standard; one-way hash, no plaintext in DB |
