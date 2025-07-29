# Library Catalog REST/AJAX Web Application (hw10-rest-ajax)

## Overview
This is a modern web application for managing a library book catalog, built with REST API architecture and AJAX functionality. The application provides a responsive web interface for managing books, authors, genres, and comments using Spring Boot REST controllers with JavaScript fetch API for dynamic content loading.

## Technologies Used
- Spring Boot 3.3.2
- Spring Web MVC with REST Controllers
- Spring Data MongoDB
- Thymeleaf (templating engine for page structure)
- JavaScript ES6+ with Fetch API (AJAX functionality)
- Bootstrap 5 (UI framework)
- MongoDB
- Maven
- JUnit 5 (testing)
- Mockito (mocking framework)

## Architecture
This application uses a **hybrid REST/AJAX architecture**:
- **REST API Controllers**: Handle data operations via JSON endpoints (`/api/*`)
- **Page Controllers**: Serve Thymeleaf templates for page structure
- **JavaScript AJAX**: Dynamic content loading and user interactions
- **Responsive UI**: Bootstrap-based interface with real-time updates

## Features Implemented
- **REST API Endpoints**: Complete RESTful API for books with proper HTTP methods
- **AJAX Operations**: Dynamic loading and updating without page refreshes
- **CRUD Operations for Books**: Create, Read, Update, Delete via REST API
- **Real-time UI Updates**: Instant feedback for all operations
- **Modern Web Interface**:
  - Dynamic books list with AJAX loading
  - Real-time book details loading
  - Inline delete confirmations
  - Form submissions via AJAX
  - Loading indicators and error handling
- **Responsive Design**: Mobile-friendly Bootstrap interface
- **Localization**: English and Russian language support
- **Comprehensive Testing**: @WebMvcTest coverage for both REST and page controllers

## How to Build and Run
1. Clone the repository
2. Navigate to the hw10-rest-ajax directory:
   ```
   cd hw10-rest-ajax
   ```
3. Start MongoDB using Docker Compose (from parent directory):
   ```
   cd ../hw08-mongo-db
   docker-compose up -d
   cd ../hw10-rest-ajax
   ```
4. Build the project using Maven:
   ```
   mvn clean package
   ```
5. Run the application:
   ```
   java -jar target/hw10-rest-ajax-0.0.1-SNAPSHOT.jar
   ```
6. Open your browser and navigate to: `http://localhost:8080`

## REST API Endpoints

### Books API (`/api/books`)
- **GET** `/api/books` - Get all books (returns JSON array)
- **GET** `/api/books/{id}` - Get book by ID (returns JSON object)
- **POST** `/api/books` - Create new book (accepts JSON body)
- **PUT** `/api/books/{id}` - Update existing book (accepts JSON body)
- **DELETE** `/api/books/{id}` - Delete book by ID (returns 204 No Content)

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

## MongoDB Document Structure
The application uses MongoDB with the following collections:
- **authors**: Stores information about book authors
- **genres**: Stores information about book genres  
- **books**: Stores information about books with references to authors and genres
- **comments**: Stores comments about books with references to the books they are about

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

### Controllers Architecture

#### REST API Controllers (`@RestController`)
- **BookController** (`/api/books`): RESTful API for book CRUD operations
  - Returns JSON responses
  - Handles HTTP methods: GET, POST, PUT, DELETE
  - Uses `@RequestBody` and `@ResponseEntity`

#### Page Controllers (`@Controller`)
- **BookPageController**: Serves Thymeleaf templates for book pages
  - Returns view names for template rendering
  - Provides data for form dropdowns (authors, genres)
- **AuthorController**: Handles author listing pages
- **GenreController**: Handles genre listing pages
- **CommentController**: Handles comment CRUD operations

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
- **@WebMvcTest**: Complete controller testing with mocked services
- **BookControllerTest**: Tests REST API endpoints with JSON requests/responses
- **BookPageControllerTest**: Tests page controller template rendering
- **AuthorControllerTest**: Tests author listing endpoints
- **GenreControllerTest**: Tests genre listing endpoints
- **CommentControllerTest**: Tests comment CRUD endpoints

## Requirements Compliance

This implementation fulfills all the specified requirements for REST/AJAX architecture:

✅ **REST API Endpoints**: Complete RESTful API with proper HTTP methods (GET, POST, PUT, DELETE)  
✅ **AJAX Operations**: JavaScript fetch API for dynamic content loading without page refreshes  
✅ **CRUD Operations**: Full Create, Read, Update, Delete functionality via REST endpoints  
✅ **Resource-style URLs**: RESTful URLs without verbs (`/api/books`, `/api/books/{id}`)  
✅ **HTTP Methods**: Actions expressed through HTTP methods, not URL verbs  
✅ **JSON Communication**: REST controllers return/accept JSON data  
✅ **Modern UI**: Bootstrap-based responsive interface with AJAX interactions  
✅ **Testing**: Complete `@WebMvcTest` coverage for both REST and page controllers  
✅ **Localization**: English and Russian language support maintained

## Project Structure
```
hw10-rest-ajax/
├── src/main/java/ru/otus/hw/
│   ├── controllers/          # REST and Page controllers
│   ├── dto/                 # Data Transfer Objects
│   ├── models/              # Domain models
│   ├── repositories/        # MongoDB repositories
│   └── services/            # Business logic services
├── src/main/resources/
│   ├── static/js/           # JavaScript AJAX functionality
│   ├── templates/           # Thymeleaf templates
│   ├── messages.properties  # English localization
│   └── application.yml      # Configuration
└── src/test/java/ru/otus/hw/
    └── controllers/         # REST and Page controller tests
```
