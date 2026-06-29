The Challenge
Your task is to build a backend API for a second-hand product moderation platform.

Sellers can create second-hand products, submit them for review, and update them while they are still editable. Moderators can claim products pending review, approve them, reject them, and trigger the expected product state transitions.

We are interested in a pragmatic, well-structured solution. Prioritise clear domain modelling, good API contracts, validation, state management, reproducible local setup, and realistic tests over unnecessary infrastructure complexity.

Backend
Build a REST API using Java 21 and Spring Boot 3.

Maven, lombok anootations and Spring Boot starters are allowed. You may use any other libraries you find useful, but avoid unnecessary complexity.

Persistence is required, but the specific technology is flexible. You may use:

Local file storage.
keep the data model clear and intentional.
Data access must be isolated behind repositories or ports instead of coupling business logic directly to controllers, arrays, SQL queries, or external clients.

layered architecture and dependency injection for example:

domain          -> business models, rules, ports, pure services
application     -> use cases / orchestration
infrastructure  -> REST controllers, persistence adapters, mappers
boot            -> Spring Boot wiring/configuration
You do not need to use this exact folder structure, but your code should make the boundaries between API, application logic, domain rules, and persistence clear.

Required Features
1. Products
   Implement product creation, retrieval, listing, update and deletion.

Each product should include at least:

Product id.
Seller id.
Title.
Description.
Category.
Size.
Condition.
Price.
Image URLs.
Current state.
Creation and update timestamps.
Products should support filtering by at least:

State.
Seller id.
Category.
Visibility and ownership rules:

Any authenticated user can list and view products from any seller.
Sellers can list their own products explicitly.
Only the seller who owns a product can update, submit or delete it.
A seller cannot modify, submit or delete products owned by another seller.
Moderator actions are handled separately through the moderation endpoints.
Pagination is recommended but not mandatory.

2. Price
   Represent prices explicitly instead of using a plain decimal number.

Example:

{
"currency": "EUR",
"amount": 4599,
"exponent": 2
}
This represents 45.99 EUR.

Business rules:

Currency must be a 3-letter code.
Amount must be positive.
Exponent must be between 0 and 4.
3. Sellers
   The system must know whether a seller is blocked or not.

A blocked seller can still have products in the system, but if a moderator approves a product from a blocked seller, the product must not become active.

You may model sellers as a table, collection, in-memory repository, seed data, or any other clear persistence model.

4. Users And Authentication
   Authentication is required.

The API must provide a login endpoint that returns a JWT access token that can be used by the Postman collection.

Use at least these roles:

SELLER
MODERATOR
Use a single user identity model. A SELLER user's id is the sellerId used by products. A MODERATOR user's id is the reviewerId stored when a product is claimed for review.

JWT authentication is required.

The seed file contains plain-text passwords only as local fixture input. Passwords must not be stored or kept in plain text in the application's persistence model. When loading the seed, hash passwords with BCrypt or an equivalent one-way hashing mechanism before storing users.

The authentication implementation does not need to be production-grade, but it must be consistent enough to evaluate ownership and authorization rules.

5. Product Images
   Products must include image URLs.

Business rules:

A product must have between 3 and 10 images.
Image URLs must not be blank.
You do not need to upload, resize, download, or validate real image contents.

6. Product Attributes
   Use simple enum-like values for product classification. Do not create external category or size catalogues unless you explicitly want to and explain why.

Allowed categories:

CLOTHING
SHOES
BAGS
ACCESSORIES
Allowed sizes:

XS
S
M
L
XL
ONE_SIZE
Allowed conditions:

NEW_WITH_TAGS
VERY_GOOD
GOOD
ACCEPTABLE
Business rules:

Category is required and must be one of the allowed values.
Size is required and must be one of the allowed values.
Condition is required and must be one of the allowed values.
7. Product States
   Use at least the following states:

DRAFT
PENDING_REVIEW
IN_REVIEW
ACTIVE
REJECTED
PAUSED
SOLD
DELETED
You may add more states if you explain why in the README.

The following transitions are required:

From	To	Actor	Reason
DRAFT	PENDING_REVIEW	Seller owner	Submit product for review
REJECTED	PENDING_REVIEW	Seller owner	Resubmit product after fixing rejection reasons
PENDING_REVIEW	IN_REVIEW	Moderator	Claim product for review
IN_REVIEW	ACTIVE	Assigned moderator	Approve product from a non-blocked seller
IN_REVIEW	PAUSED	Assigned moderator	Approve product from a blocked seller
IN_REVIEW	REJECTED	Assigned moderator	Reject product with a reason
DRAFT	DELETED	Seller owner	Soft delete product
ACTIVE	DELETED	Seller owner	Soft delete product
PAUSED	DELETED	Seller owner	Soft delete product
REJECTED	DELETED	Seller owner	Soft delete product
State rules:

SOLD is terminal for this challenge.
DELETED is terminal.
Products cannot move directly from DRAFT to ACTIVE.
Products cannot be edited once submitted for review.
Any transition not listed above should be rejected with a controlled error.
Include a short state transition table or diagram in the README. It does not need to be visually perfect, but it must make clear which transitions are valid, which actor can trigger them, and which guard conditions apply.

8. Product Creation
   Users should be able to create products.

Business rules:

Title is required.
Description is required.
Category must be valid.
Size must be valid.
Condition must be valid.
Price must be valid.
Terms and conditions must be accepted.
The product must have between 3 and 10 images.
A newly created product starts in DRAFT.
Example request:

{
"title": "Wool coat",
"description": "Used twice, excellent condition",
"category": "CLOTHING",
"size": "M",
"condition": "VERY_GOOD",
"termsAccepted": true,
"price": {
"currency": "EUR",
"amount": 4599,
"exponent": 2
},
"imageUrls": [
"https://example.com/coat-1.jpg",
"https://example.com/coat-2.jpg",
"https://example.com/coat-3.jpg"
]
}
9. Product Updates
   Users should be able to update editable products.

Business rules:

Only the seller who owns the product can update it.
Only products in DRAFT or REJECTED can be edited.
Products in PENDING_REVIEW, IN_REVIEW, ACTIVE, PAUSED, SOLD or DELETED cannot be edited.
Updates must preserve the same validation rules as creation.
Partial update is recommended, but full update is accepted if clearly documented.

10. Submit Product For Review
    Users should be able to submit a product for moderation.

Business rules:

Only the seller who owns the product can submit it for review.
Only products in DRAFT or REJECTED can be submitted.
Submitting a product changes its state to PENDING_REVIEW.
The product must be valid before it can be submitted.
11. Moderation
    Moderators should be able to claim, approve and reject products.

The moderator identity must come from the JWT access token.

Business rules:

A moderator can claim the next product in PENDING_REVIEW.
Claiming a product changes its state to IN_REVIEW.
The product stores the assigned moderator user id as reviewerId.
Two moderators must not be able to claim the same product as the result of the same pending review flow.
Only the assigned moderator can approve or reject the product.
Approving a product from a non-blocked seller changes its state to ACTIVE.
Approving a product from a blocked seller changes its state to PAUSED.
Rejecting a product changes its state to REJECTED.
Rejecting a product requires a non-empty reason.
Invalid transitions must return a controlled error response.
Example reject request:

{
"reason": "Images do not clearly show the product condition"
}
12. Product Deletion
    Implement soft delete.

Business rules:

Only the seller who owns the product can delete it.
Deleting a product changes its state to DELETED.
Only products in DRAFT, ACTIVE, PAUSED or REJECTED can be deleted.
Products in PENDING_REVIEW, IN_REVIEW, SOLD or DELETED cannot be deleted.
Deleted products cannot be edited, submitted, claimed, approved or rejected.
13. Product Events
    Record relevant product events.

At minimum, record events when:

A product is created.
A product is submitted for review.
A product is claimed by a moderator.
A product is approved.
A product is rejected.
A product is deleted.
Each event should include at least:

Event id.
Product id.
Event type.
Timestamp.
Actor id, when applicable.
Optional metadata, such as rejection reason or previous/new state.
You do not need to integrate with real queues, EventBridge, Kafka, SQS, or any external event system. A persisted event table/collection or an in-memory event repository is enough.

The design should still make the event publishing concern explicit, for example through a ProductEventPublisher port or equivalent abstraction.

Suggested API Endpoints
You may adjust the exact shape if your README explains the decisions.

POST   /auth/login

POST   /products
GET    /products
GET    /me/products
GET    /products/{productId}
PATCH  /products/{productId}
DELETE /products/{productId}

POST   /products/{productId}/submit-review

POST   /moderation/products/claim-next
POST   /moderation/products/{productId}/approve
POST   /moderation/products/{productId}/reject

GET    /products/{productId}/events
Endpoint Permissions
Use the following permission model unless your README clearly explains an equivalent alternative.

Endpoint	Required actor
POST /auth/login	Public
POST /products	SELLER
GET /products	SELLER or MODERATOR
GET /me/products	SELLER
GET /products/{productId}	SELLER or MODERATOR
PATCH /products/{productId}	Product owner seller
DELETE /products/{productId}	Product owner seller
POST /products/{productId}/submit-review	Product owner seller
POST /moderation/products/claim-next	MODERATOR
POST /moderation/products/{productId}/approve	Assigned moderator
POST /moderation/products/{productId}/reject	Assigned moderator
GET /products/{productId}/events	Product owner seller or MODERATOR
Approve requests do not require a body. The assigned moderator is resolved from the JWT access token.

Example product response:

{
"id": "prod_001",
"sellerId": 1001,
"title": "Wool coat",
"description": "Used twice, excellent condition",
"category": "CLOTHING",
"size": "M",
"condition": "VERY_GOOD",
"state": "DRAFT",
"price": {
"currency": "EUR",
"amount": 4599,
"exponent": 2
},
"imageUrls": [
"https://example.com/coat-1.jpg",
"https://example.com/coat-2.jpg",
"https://example.com/coat-3.jpg"
],
"reviewerId": null,
"createdAt": "2026-07-10T10:00:00Z",
"updatedAt": "2026-07-10T10:00:00Z"
}
Authentication And Actor Context
Authentication must be implemented with JWT.

The API must expose POST /auth/login, validate user credentials, and return a JWT access token.

All endpoints except POST /auth/login must require a valid JWT access token.

The seed file contains plain-text passwords only as local fixture input. Passwords must not be stored or kept in plain text in the application's database or in-memory persistence model. When loading the seed, hash passwords with BCrypt or an equivalent one-way hashing mechanism before storing users.

If your seed import process stores pre-hashed passwords instead, document it clearly in the README.

Document how JWT signing is configured locally, including any required environment variables or default local secret.

At minimum, your API must distinguish between seller actions and moderator actions. Seller/moderator identity must come from the authenticated actor, not only from request body fields.

For seller-owned operations, the authenticated actor must match the product owner. For example, a token for seller 1001 can update products owned by seller 1001, but must receive 403 Forbidden when trying to update a product owned by seller 1002.

For product creation, the product owner must be resolved from the authenticated seller token. Clients must not be allowed to create products for another seller by sending a different sellerId in the request body.

Example login request:

{
"username": "seller1",
"password": "password123"
}
Example login response:

{
"accessToken": "sample-token",
"user": {
"id": 1001,
"username": "seller1",
"role": "SELLER"
}
}
Seed Data
Seed data is required.

We provide a seed-data.json file with sample sellers, moderators and products in different states. You may adapt it or create an equivalent seed file from scratch, but your solution must preserve the review scenarios needed to test the required flows.

Your application must include a clear way to load the seed data, either automatically at startup or through a documented command/script.

The provided seed covers at least these review scenarios:

Two seller users and two moderator users.
One non-blocked seller and one blocked seller.
A product in DRAFT owned by the first seller.
A product in PENDING_REVIEW ready to be claimed.
A product in IN_REVIEW assigned to a moderator.
An ACTIVE product to test invalid edits and soft delete.
A SOLD product to test terminal-state behaviour.
A DRAFT product owned by another seller to test ownership failures.
The reviewer must be able to reproduce this data locally without guessing.

Sample credentials for local review:

seller1 / password123
seller2 / password123
moderator1 / password123
moderator2 / password123
Include one of the following:

An automatic seed loader that runs on application startup.
A documented command, for example ./mvnw spring-boot:run -Dspring-boot.run.arguments=--seed.
SQL migration/data files such as schema.sql and data.sql.
A script such as scripts/load-seed.sh.
A documented import process if using a hosted service.
If you use a hosted database, do not commit real secrets. Include required environment variables and setup instructions.

Postman Collection
A Postman collection is mandatory.

Include the collection in the repository, for example:

postman/product-moderation-api.postman_collection.json
postman/local.postman_environment.json
The collection must allow reviewers to execute the main API flows locally without manually building requests.

At minimum, include requests for:

Login as seller.
Login as moderator.
Create product.
List products.
Get product detail.
Update an editable product.
Attempt to update a product owned by another seller.
Attempt to update a non-editable product.
Submit product for review.
Claim next product for moderation.
Approve product from a non-blocked seller.
Approve product from a blocked seller.
Reject product with a reason.
Delete product.
Attempt an invalid transition.
Get product events.
The collection should use variables where useful, such as:

baseUrl
sellerAccessToken
moderatorAccessToken
productId
If your collection depends on a specific execution order, document it in the README.

Error Handling
Return controlled error responses for validation and business rule failures.

The exact shape is up to you, but it should be consistent.

Example:

{
"code": "invalid-product-state-transition",
"message": "Product prod_004 cannot be submitted for review from state ACTIVE",
"field": "state"
}
Use appropriate HTTP status codes, for example:

400 Bad Request for validation errors.
401 Unauthorized or 403 Forbidden for authorization errors.
404 Not Found for missing resources.
409 Conflict for invalid state transitions or conflicting operations.
Tests
Tests are required.

Prioritise meaningful tests over high coverage numbers.

At minimum, include unit tests for:

Successful login.
Unauthorized access without a token.
Forbidden seller operation on a product owned by another seller.
Product creation validation.
Price validation.
Image count validation.
Submit for review transition.
Claim product transition.
Two moderators cannot claim the same product.
Approve product from non-blocked seller.
Approve product from blocked seller.
Reject product with and without reason.
Invalid transition scenarios.
JUnit 5 is expected. Mockito, AssertJ, Spring Boot Test, Testcontainers or similar tools are allowed.

Domain/business rule tests should not require starting the full Spring context unless there is a clear reason.

Project Structure
You may organise the solution however you prefer, but the repository must be easy to review.

The README must explain:

How to install dependencies.
How to run the application locally.
How to load seed data.
How to run tests.
How to import and execute the Postman collection.
Which persistence option was chosen and why.
Any trade-offs or known limitations.
The product state transition table or diagram.
Technical Decision Notes
Include a short technical decision section in the README.

This section should briefly explain:

Why you chose your persistence approach.
How you modelled product state transitions.
How you enforce ownership and role-based permissions.
How seed data is loaded and whether the process is idempotent.
What you would improve with more time.
The goal is not to write a long architecture document. We want to understand how you think, what trade-offs you made, and whether you can explain the solution clearly.

AI Usage
Using AI tools to complete the challenge is accepted and encouraged. We care about how you use them, how you validate the output, and whether you can explain the final solution.

If you use AI, include a short section in the README explaining:

Which AI tools or agents you used.
What parts of the solution they helped with.
What you reviewed, changed, or rejected.
Any limitations or risks you identified in the generated output.
You may also include supporting evidence such as an AGENTS.md, custom agent instructions, prompts, or notes used during the implementation. This is not required to be perfect, but it should make your workflow transparent.

Do not submit code you cannot reason about.

Deliverables
Push the code to a public GitHub repository.
Include a README.md with local setup instructions.
Include the seed data file or seed script.
Include a Postman collection and environment file.
Include tests.
Include any required environment variable documentation.
Include a short explanation of technical decisions and trade-offs.
If AI tools were used, include the AI usage notes described above.
Bonus Points
OpenAPI/Swagger documentation.
MapStruct
Docker Compose for local persistence.
Database migrations with Flyway
Integration tests for API endpoints.
Testcontainers.
JWT expiration handling or refresh tokens.
Structured logging.
Basic observability endpoint using Spring Actuator.
Clear separation between domain exceptions and HTTP error responses.
What We Will Evaluate
Correctness of the main product and moderation flows.
Domain modelling and state transition design.
API design and request validation.
Error handling.
Persistence boundaries and repository design.
Separation of concerns between controllers, use cases, domain services and adapters.
How clearly technical decisions and trade-offs are explained.
Test quality and relevance.
README quality and developer experience.
Postman collection completeness and usability.
Seed data reproducibility.
Pragmatism: avoid unnecessary complexity while keeping the solution maintainable.