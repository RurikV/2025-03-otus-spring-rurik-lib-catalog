# Library Catalog Web Application with Authorization and ACL (hw13-auth-acl)

## Overview
This is a secure web application for managing a library book catalog with comprehensive authorization and access control lists (ACL). The application implements both URL-based authorization and domain-based authorization using Spring Security. It provides role-based access control where ADMIN users can manage all resources while USER users have limited access and can only modify their own content.

## Technologies Used
- Spring Boot 3.3.2
- Spring Web MVC
- Spring Data MongoDB
- Spring Security 6 (Form-based authentication)
- Thymeleaf (templating engine)
- Thymeleaf Spring Security extras
- Bootstrap 5 (UI framework)
- MongoDB
- BCrypt password encoding
- Maven
- JUnit 5 (testing)
- Mockito (mocking framework)

## Features Implemented
- **Authentication & Authorization**: Form-based authentication with comprehensive Spring Security
- **URL-based Authorization**: Role-based access control for different endpoints
- **Domain-based Authorization**: Method-level security ensuring users can only modify their own content
- **User Management**: User entity with username, password, role, and enabled status
- **Password Security**: BCrypt password encoding
- **Role-based Access Control**: ADMIN and USER roles with different permission levels
- **CRUD Operations for Books**: Complete Create, Read, Update, Delete functionality
- **Display of Authors, Genres, and Comments**: Dedicated list and view pages
- **Web Pages**:
  - Login page with error/success messages
  - Books list page (main page)
  - Authors list page
  - Genres list page
  - Book creation/editing page
  - Book view page with comments
  - Comment management pages
- **Navigation**: Enhanced with user info and logout functionality
- **Deletion Functionality**: Uses confirmation pages with POST method (no GET deletion)
- **Form Controls**: Author/genre selection uses dropdowns, no manual ID input
- **Localization**: English and Russian language support
- **Comprehensive Testing**: @WebMvcTest coverage for all controllers + Security integration tests

## Demo Users
The application comes with pre-configured demo users with different access levels:
- **Admin User**: username: `admin`, password: `admin`, role: `ADMIN`
  - Full access to all application features
  - Can create, edit, and delete books
  - Can manage authors and genres
  - Can view and modify any comments
- **Regular User**: username: `user`, password: `user`, role: `USER`
  - Can view books, authors, and genres
  - Cannot create, edit, or delete books
  - Cannot manage authors and genres
  - Can create comments but can only edit/delete their own comments

## Authorization Rules

### URL-based Authorization
- **Public Access**: `/login`, static resources (`/css/**`, `/js/**`, `/images/**`)
- **Both ADMIN and USER**: 
  - View books: `/`, `/books`, `/books/{id}`
  - Create comments: `/books/{id}/comments/new`
  - Edit/delete own comments: `/comments/{id}/edit`, `/comments/{id}/delete`
- **ADMIN Only**:
  - Book management: `/books/new`, `/books/{id}/edit`, `/books/{id}/delete`
  - Author management: `/authors/**`
  - Genre management: `/genres/**`

### Domain-based Authorization
- **Comment Ownership**: Users can only edit/delete comments they created
- **Method-level Security**: `@PreAuthorize` annotations ensure proper access control
- **Admin Override**: ADMIN role can modify any comment regardless of ownership

## How to Build and Run
1. Clone the repository
2. Navigate to the hw13-auth-acl directory:
   ```
   cd hw13-auth-acl
   ```
3. Start MongoDB using Docker Compose:
   ```
   docker-compose up -d
   ```
4. Build the project using Maven:
   ```
   mvn clean package
   ```
5. Run the application:
   ```
   java -jar target/hw13-auth-acl-0.0.1-SNAPSHOT.jar
   ```
6. Open your browser and navigate to: `http://localhost:8080`
7. **Login Required**: You will be redirected to the login page. Use one of the demo accounts:
   - Username: `admin`, Password: `admin`
   - Username: `user`, Password: `user`

## MongoDB Document Structure
The application uses MongoDB with the following collections:
- **users**: Stores user authentication information (username, encrypted password, role, enabled status)
- **authors**: Stores information about book authors
- **genres**: Stores information about book genres  
- **books**: Stores information about books with references to authors and genres
- **comments**: Stores comments about books with references to the books they are about

## Web Interface

### Authentication
- **Login Page** (`/login`): Form-based authentication with error/success messages
- **Logout**: Available via navigation bar (POST to `/logout`)

### Main Pages (Protected - Requires Authentication)
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
- **LoginController**: Handles login page display with error/success messages
- **BookController**: Handles all book CRUD operations
- **AuthorController**: Handles author listing
- **GenreController**: Handles genre listing  
- **CommentController**: Handles comment CRUD operations

### Security Components
- **SecurityConfig**: Spring Security configuration with Form-based authentication and role-based authorization
  - URL-based authorization rules for different endpoints
  - Method-level security enabled with `@EnableMethodSecurity`
- **CustomUserDetailsService**: Custom implementation of UserDetailsService with role mapping
- **CommentServiceImpl**: Enhanced with `@PreAuthorize` annotations for domain-based authorization
- **DataInitializer**: Creates demo users with ADMIN and USER roles on application startup
- **User Entity**: User model with username, password, role, and enabled status
- **UserRepository**: MongoDB repository for user data
- **UserService**: Service layer for user operations
- **Comment Entity**: Enhanced with user ownership for domain-based authorization

### Templates Structure
```
templates/
├── login.html         # Login page with authentication form
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
└── layout.html        # Common layout template with authentication info
```

### Localization
- **messages.properties**: English translations
- **messages_ru.properties**: Russian translations
- Supports switching between languages

### Testing
- **@WebMvcTest**: Complete controller testing with mocked services
- **SecurityIntegrationTest**: Tests authentication and authorization requirements
- **RoleBasedAuthorizationTest**: Tests role-based access control for different endpoints
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
hw12-mvc-auth/
├── src/main/java/ru/otus/hw/
│   ├── controllers/          # Web controllers (including LoginController)
│   ├── models/              # Domain models (including User)
│   ├── repositories/        # MongoDB repositories (including UserRepository)
│   ├── services/            # Business logic services (including UserService)
│   ├── security/            # Security components (CustomUserDetailsService)
│   └── config/              # Configuration (SecurityConfig, DataInitializer)
├── src/main/resources/
│   ├── templates/           # Thymeleaf templates (including login.html)
│   ├── messages.properties  # English localization
│   ├── messages_ru.properties # Russian localization
│   └── application.yml      # Configuration
└── src/test/java/ru/otus/hw/
    └── controllers/         # Controller tests (including SecurityIntegrationTest)
```
