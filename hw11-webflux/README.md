# Library Catalog Reactive Web Application (hw11-webflux)

## Overview
This is a modern reactive web application for managing a library book catalog, built with Spring WebFlux and reactive programming principles. The application provides a responsive and resilient web interface for managing books, authors, genres, and comments using functional endpoints, reactive streams, and non-blocking I/O operations.

## Technologies Used
- Spring Boot 3.3.2
- **Spring WebFlux** (reactive web framework)
- **Spring Data MongoDB Reactive** (reactive MongoDB support)
- **Spring Data R2DBC** (reactive relational database connectivity)
- **R2DBC H2** (reactive H2 database driver)
- Thymeleaf (templating engine for page structure)
- JavaScript ES6+ with Fetch API (AJAX functionality)
- Bootstrap 5 (UI framework)
- MongoDB (document database)
- H2 Database (relational database via R2DBC)
- Maven
- JUnit 5 (testing)
- Reactor Test (reactive streams testing)
- Testcontainers (integration testing)

## Architecture
This application uses a **reactive functional architecture**:
- **Functional Endpoints**: WebFlux functional routing instead of traditional controllers
- **Reactive Streams**: Non-blocking data processing with Mono/Flux
- **Dual Database Support**: MongoDB (reactive) for primary data, R2DBC H2 for relational operations
- **N+1 Problem Solution**: Custom R2DBC repositories with optimized queries
- **Responsive UI**: Bootstrap-based interface with reactive backend

## Features Implemented
- **Reactive Functional Endpoints**: WebFlux functional routing with RouterFunction
- **Non-blocking Operations**: All operations use reactive streams (Mono/Flux)
- **CRUD Operations for Books**: Fully reactive Create, Read, Update, Delete operations
- **Dual Database Architecture**:
  - MongoDB Reactive for document-based operations
  - R2DBC H2 for relational data with optimized queries
- **N+1 Problem Resolution**: Custom repositories using R2dbcEntityOperations
- **Modern Web Interface**:
  - Dynamic books list with reactive data loading
  - Real-time book details loading
  - Reactive form submissions
  - Error handling with reactive streams
- **Responsive Design**: Mobile-friendly Bootstrap interface
- **Localization**: English and Russian language support
- **Comprehensive Testing**: @WebFluxTest coverage with reactive test support

## How to Build and Run
1. Clone the repository
2. Navigate to the hw11-webflux directory:
   ```
   cd hw11-webflux
   ```
3. Start MongoDB using Docker Compose (from parent directory):
   ```
   cd ../hw08-mongo-db
   docker-compose up -d
   cd ../hw11-webflux
   ```
4. Build the project using Maven:
   ```
   mvn clean package
   ```
5. Run the application:
   ```
   java -jar target/hw11-webflux-0.0.1-SNAPSHOT.jar
   ```
6. Open your browser and navigate to: `http://localhost:8080`

### Database Configuration
The application uses dual database configuration:
- **MongoDB**: Reactive MongoDB for primary document storage (authors, genres, books, comments)
- **H2 Database**: R2DBC H2 for relational operations and N+1 problem resolution
- **Auto-initialization**: H2 schema and data are automatically created from `schema.sql`

## Reactive API Endpoints

### Functional Routing Architecture
The application uses **WebFlux Functional Endpoints** instead of traditional `@RestController` annotations:
- **RouterFunction**: Defines routes using functional programming approach
- **HandlerFunction**: Processes requests and returns reactive responses
- **Reactive Streams**: All endpoints return `Mono<ServerResponse>` or `Flux<ServerResponse>`

### Books API (`/api/books`)
- **GET** `/api/books` - Get all books (returns `Flux<BookDto>` as JSON stream)
- **GET** `/api/books/{id}` - Get book by ID (returns `Mono<BookDto>` as JSON)
- **POST** `/api/books` - Create new book (accepts JSON body, returns `Mono<BookDto>`)
- **PUT** `/api/books/{id}` - Update existing book (accepts JSON body, returns `Mono<BookDto>`)
- **DELETE** `/api/books/{id}` - Delete book by ID (returns `Mono<Void>` with 204 No Content)

### Authors API (`/api/authors`)
- **GET** `/api/authors` - Get all authors (returns `Flux<Author>` as JSON stream)

### Genres API (`/api/genres`)
- **GET** `/api/genres` - Get all genres (returns `Flux<Genre>` as JSON stream)

### Comments API (`/api/comments`)
- **GET** `/api/comments/book/{bookId}` - Get comments for book (returns `Flux<Comment>`)
- **POST** `/api/comments` - Create new comment (returns `Mono<Comment>`)
- **PUT** `/api/comments/{id}` - Update comment (returns `Mono<Comment>`)
- **DELETE** `/api/comments/{id}` - Delete comment (returns `Mono<Void>`)

### Request/Response Examples

#### Get All Books
```http
GET /api/books
Accept: application/json
```
Response:
```json
[
  {
    "id": "1",
    "title": "Book Title",
    "author": {
      "id": "1",
      "fullName": "Author Name"
    },
    "genres": [
      {
        "id": "1",
        "name": "Genre Name"
      }
    ]
  }
]
```

#### Create Book
```http
POST /api/books
Content-Type: application/json

{
  "title": "New Book",
  "authorId": "1",
  "genreIds": ["1", "2"]
}
```

## Database Architecture

### Dual Database Support
The application implements a **dual database architecture** to demonstrate different reactive data access patterns:

#### MongoDB (Reactive Document Database)
Primary storage using **Spring Data MongoDB Reactive** with the following collections:
- **authors**: Stores information about book authors
- **genres**: Stores information about book genres  
- **books**: Stores information about books with embedded author and genre references
- **comments**: Stores comments about books with `@DBRef` references to books

**MongoDB Models:**
- `Author`, `Genre`, `Book`, `Comment` - Document-based models with `@Document` annotations
- **ReactiveMongoRepository**: Provides reactive CRUD operations returning `Mono`/`Flux`

#### R2DBC H2 (Reactive Relational Database)
Secondary storage using **Spring Data R2DBC** for demonstrating N+1 problem solutions:
- **authors**: Relational table for authors
- **genres**: Relational table for genres
- **books**: Relational table for books with foreign key to authors
- **book_genres**: Junction table for many-to-many book-genre relationships
- **comments**: Relational table for comments with foreign key to books

**R2DBC Models:**
- `AuthorEntity`, `GenreEntity`, `BookEntity`, `CommentEntity` - Relational models with `@Table` annotations
- **Custom R2DBC Repositories**: Solve N+1 problems using `R2dbcEntityOperations` and optimized queries
- **BookWithRelations**: Aggregated model for efficient data fetching

## Web Interface

### Page Routes (Thymeleaf Templates)
- **Home Page** (`/`): Books list page with AJAX-loaded content
- **Books List** (`/books`): Alternative route to books list
- **Book View** (`/books/{id}`): Book details page with AJAX-loaded data
- **New Book Form** (`/books/new`): Form to create a new book
- **Edit Book Form** (`/books/{id}/edit`): Form to edit existing book
- **Delete Confirmation** (`/books/{id}/delete`): Confirmation page for book deletion
- **Authors List** (`/authors`): Displays all authors
- **Genres List** (`/genres`): Displays all genres

### AJAX Functionality
- **Dynamic Book Loading**: Books list loads via `/api/books` without page refresh
- **Real-time Book Details**: Book details load via `/api/books/{id}` dynamically
- **Inline Operations**: Create, update, delete operations via AJAX calls
- **Loading Indicators**: Spinner animations during data loading
- **Error Handling**: User-friendly error messages for failed operations
- **Success Notifications**: Confirmation messages for successful operations

### JavaScript Features (`/js/books.js`)
- **BooksAPI Class**: Handles all REST API communications
- **BooksUI Class**: Manages DOM manipulation and user interactions
- **Fetch API**: Modern JavaScript for HTTP requests
- **Dynamic Content Rendering**: Client-side HTML generation
- **Event Handling**: Interactive buttons and form submissions

## Key Implementation Details

### Reactive Architecture

#### Functional Endpoints (WebFlux)
- **RouterFunction**: Defines routes using functional programming approach
- **HandlerFunction**: Processes requests and returns `Mono<ServerResponse>`
- **Reactive Services**: All business logic returns `Mono`/`Flux` reactive streams
- **Non-blocking I/O**: Complete reactive stack from web layer to database

#### Service Layer Architecture
- **BookService**: Reactive book operations returning `Mono<BookDto>`/`Flux<BookDto>`
- **AuthorService**: Reactive author operations returning `Flux<Author>`
- **GenreService**: Reactive genre operations returning `Flux<Genre>`
- **CommentService**: Reactive comment operations returning `Mono<Comment>`/`Flux<Comment>`

#### Repository Layer
**MongoDB Reactive Repositories:**
- `BookRepository`, `AuthorRepository`, `GenreRepository`, `CommentRepository`
- Extend `ReactiveMongoRepository<T, String>`
- Custom queries using `@Query` annotations

**R2DBC Custom Repositories:**
- `BookR2dbcRepository` with custom implementation
- Uses `R2dbcEntityOperations` for optimized queries
- Solves N+1 problems with batch operations and joins

### Templates & Static Resources Structure
```
templates/
├── book/
│   ├── list.html      # Books list page with AJAX loading
│   ├── view.html      # Book details with AJAX loading
│   ├── form.html      # Create/edit book form
│   └── delete.html    # Delete confirmation
├── author/
│   └── list.html      # Authors list
├── genre/
│   └── list.html      # Genres list
├── comment/
│   ├── form.html      # Add comment form
│   ├── edit.html      # Edit comment form
│   └── delete.html    # Delete confirmation
└── layout.html        # Common layout template

static/
└── js/
    └── books.js       # AJAX functionality for books
```

### Localization
- **messages.properties**: English translations
- **messages_ru.properties**: Russian translations
- Supports switching between languages

### Testing

#### Reactive Testing Framework
- **@WebFluxTest**: Tests functional endpoints with reactive streams
- **WebTestClient**: Reactive web client for testing HTTP endpoints
- **StepVerifier**: Reactor testing utility for verifying reactive streams
- **Testcontainers**: Integration testing with real MongoDB instances

#### Test Coverage
- **BookControllerTest**: Tests functional endpoints with `WebTestClient`
- **BookServiceTest**: Tests reactive service layer with `StepVerifier`
- **BookRepositoryTest**: Tests reactive MongoDB repositories
- **BookR2dbcRepositoryTest**: Tests R2DBC custom repositories
- **Integration Tests**: End-to-end testing with Testcontainers
- **GlobalExceptionHandlerTest**: Tests reactive error handling

## Requirements Compliance

This implementation fulfills all the specified requirements for WebFlux reactive architecture:

✅ **Functional Endpoints**: WebFlux functional routing instead of traditional `@RestController`  
✅ **Reactive Streams**: All operations use `Mono`/`Flux` reactive streams  
✅ **Spring Data MongoDB Reactive**: Reactive MongoDB repositories and operations  
✅ **Spring Data R2DBC**: Reactive relational database connectivity with H2  
✅ **N+1 Problem Resolution**: Custom R2DBC repositories with `R2dbcEntityOperations`  
✅ **Non-blocking I/O**: Complete reactive stack from web layer to database  
✅ **No block()/subscribe()**: Reactive streams used throughout (except in tests/migrations)  
✅ **Dual Database Support**: MongoDB for documents, R2DBC H2 for relational data  
✅ **Comprehensive Testing**: `@WebFluxTest` with `WebTestClient` and `StepVerifier`  
✅ **Responsive & Resilient**: Built for high concurrency and fault tolerance

## Project Structure
```
hw11-webflux/
├── src/main/java/ru/otus/hw/
│   ├── config/              # Configuration classes
│   │   ├── MongoConfig.java # MongoDB reactive configuration
│   │   └── R2dbcConfig.java # R2DBC configuration
│   ├── controllers/         # Functional endpoint handlers
│   ├── dto/                 # Data Transfer Objects
│   ├── exceptions/          # Custom exceptions and handlers
│   ├── models/              # MongoDB document models
│   │   └── r2dbc/          # R2DBC entity models
│   ├── repositories/        # Reactive repositories
│   │   └── r2dbc/          # Custom R2DBC repositories
│   └── services/            # Reactive business logic services
├── src/main/resources/
│   ├── static/js/           # JavaScript functionality
│   ├── templates/           # Thymeleaf templates
│   ├── messages.properties  # English localization
│   ├── messages_ru.properties # Russian localization
│   ├── schema.sql          # R2DBC H2 database schema
│   └── application.yml      # Configuration
└── src/test/java/ru/otus/hw/
    ├── controllers/         # WebFlux functional endpoint tests
    ├── repositories/        # Repository integration tests
    └── services/           # Reactive service tests
```
