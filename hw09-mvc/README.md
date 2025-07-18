# Library Catalog Web Application (hw09-mvc)

## Overview
This is a web application for managing a library book catalog, converted from a console application. The application provides a complete web interface for managing books, authors, genres, and comments in a library catalog using Spring MVC and Thymeleaf.

## Technologies Used
- Spring Boot 3.3.2
- Spring Web MVC
- Spring Data MongoDB
- Thymeleaf (templating engine)
- Bootstrap 5 (UI framework)
- MongoDB
- Maven
- JUnit 5 (testing)
- Mockito (mocking framework)

## Features Implemented
- **CRUD Operations for Books**: Complete Create, Read, Update, Delete functionality
- **Display of Authors, Genres, and Comments**: Dedicated list and view pages
- **Web Pages**:
  - Books list page (main page)
  - Authors list page
  - Genres list page
  - Book creation/editing page
  - Book view page with comments
  - Comment management pages
- **Deletion Functionality**: Uses confirmation pages with POST method (no GET deletion)
- **Navigation**: Home page accessible from anywhere in the application
- **Form Controls**: Author/genre selection uses dropdowns, no manual ID input
- **Localization**: English and Russian language support
- **Comprehensive Testing**: @WebMvcTest coverage for all controllers

## How to Build and Run
1. Clone the repository
2. Navigate to the hw09-mvc directory:
   ```
   cd hw09-mvc
   ```
3. Start MongoDB using Docker Compose (from parent directory):
   ```
   cd ../hw08-mongo-db
   docker-compose up -d
   cd ../hw09-mvc
   ```
4. Build the project using Maven:
   ```
   mvn clean package
   ```
5. Run the application:
   ```
   java -jar target/hw09-mvc-0.0.1-SNAPSHOT.jar
   ```
6. Open your browser and navigate to: `http://localhost:8080`

## MongoDB Document Structure
The application uses MongoDB with the following collections:
- **authors**: Stores information about book authors
- **genres**: Stores information about book genres  
- **books**: Stores information about books with references to authors and genres
- **comments**: Stores comments about books with references to the books they are about

## Web Interface

### Main Pages
- **Home Page** (`/`): Lists all books with view/edit/delete actions
- **Books List** (`/books`): Alternative route to books list
- **Authors List** (`/authors`): Displays all authors
- **Genres List** (`/genres`): Displays all genres

### Book Operations
- **View Book** (`/books/{id}`): Shows book details with comments
- **New Book** (`/books/new`): Form to create a new book
- **Edit Book** (`/books/{id}/edit`): Form to edit existing book
- **Delete Book** (`/books/{id}/delete`): Confirmation page for book deletion

### Comment Operations
- **Add Comment** (`/books/{bookId}/comments/new`): Form to add comment to book
- **Edit Comment** (`/comments/{id}/edit`): Form to edit existing comment
- **Delete Comment** (`/comments/{id}/delete`): Confirmation page for comment deletion

## Key Implementation Details

### Controllers
- **BookController**: Handles all book CRUD operations
- **AuthorController**: Handles author listing
- **GenreController**: Handles genre listing  
- **CommentController**: Handles comment CRUD operations

### Templates Structure
```
templates/
├── book/
│   ├── list.html      # Books list page
│   ├── view.html      # Book details with comments
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
```

### Localization
- **messages.properties**: English translations
- **messages_ru.properties**: Russian translations
- Supports switching between languages

### Testing
- **@WebMvcTest**: Complete controller testing with mocked services
- **BookControllerTest**: Tests all book CRUD endpoints
- **AuthorControllerTest**: Tests author listing
- **GenreControllerTest**: Tests genre listing
- **CommentControllerTest**: Tests comment CRUD endpoints

## Requirements Compliance

This implementation fulfills all the specified requirements:

✅ **CRUD Operations**: Complete CRUD for books with display of authors, genres, and comments  
✅ **Required Pages**: Books list, authors list, genres list, book create/edit, book view with comments  
✅ **Deletion**: Uses confirmation pages with POST method (no GET deletion)  
✅ **Navigation**: Home page accessible from anywhere via navigation bar  
✅ **Form Controls**: Author/genre selection uses `<select>` dropdowns  
✅ **Thymeleaf Views**: All templates use Thymeleaf with classic `@Controller`  
✅ **Testing**: Complete `@WebMvcTest` coverage for all CRUD operations  
✅ **Localization**: English and Russian language support implemented

## Project Structure
```
hw09-mvc/
├── src/main/java/ru/otus/hw/
│   ├── controllers/          # Web controllers
│   ├── models/              # Domain models
│   ├── repositories/        # MongoDB repositories
│   └── services/            # Business logic services
├── src/main/resources/
│   ├── templates/           # Thymeleaf templates
│   ├── messages.properties  # English localization
│   ├── messages_ru.properties # Russian localization
│   └── application.yml      # Configuration
└── src/test/java/ru/otus/hw/
    └── controllers/         # Controller tests
```
