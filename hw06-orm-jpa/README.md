# Library Catalog Application

## Overview
This is a single-user console application for managing a library book catalog. The application allows you to maintain a catalog of books, authors, genres, and comments in a library.

## Technologies Used
- Spring Boot
- Spring JPA
- Spring Shell
- H2 Database (in-memory)
- Flyway for database migrations
- Maven

## How to Build and Run
1. Clone the repository
2. Navigate to the project directory
3. Build the project using Maven:
   ```
   mvn clean package
   ```
4. Run the application:
   ```
   java -jar target/hw06-orm-jpa-0.0.1-SNAPSHOT.jar
   ```

## Database Schema
The application uses an H2 in-memory database with the following schema:
- **authors**: Stores information about book authors
- **genres**: Stores information about book genres
- **books**: Stores information about books
- **books_genres**: Junction table for the many-to-many relationship between books and genres
- **comments**: Stores comments about books

The database schema is created and initialized using Flyway migrations.

## Available Commands

### Book Commands
- **ab** - Find all books
  ```
  ab
  ```

- **bbid** - Find book by id
  ```
  bbid 1
  ```

- **bins** - Insert a new book
  ```
  bins "Book Title" 1 1,2
  ```
  Parameters:
  - Book title (in quotes if it contains spaces)
  - Author ID
  - Genre IDs (comma-separated list)

- **bupd** - Update an existing book
  ```
  bupd 4 "Updated Book Title" 3 2,5
  ```
  Parameters:
  - Book ID to update
  - New book title
  - New author ID
  - New genre IDs (comma-separated list)

- **bdel** - Delete a book by id
  ```
  bdel 4
  ```
  Parameters:
  - Book ID to delete

### Author Commands
- **aa** - Find all authors
  ```
  aa
  ```

### Genre Commands
- **ag** - Find all genres
  ```
  ag
  ```

### Comment Commands
- **acbi** - Find all comments by book id
  ```
  acbi 1
  ```
  Parameters:
  - Book ID to find comments for

- **cbid** - Find comment by id
  ```
  cbid 1
  ```
  Parameters:
  - Comment ID to find

- **cins** - Insert a new comment
  ```
  cins "This is a great book!" 1
  ```
  Parameters:
  - Comment text (in quotes if it contains spaces)
  - Book ID to add the comment to

- **cupd** - Update an existing comment
  ```
  cupd 1 "Updated comment text"
  ```
  Parameters:
  - Comment ID to update
  - New comment text

- **cdel** - Delete a comment by id
  ```
  cdel 1
  ```
  Parameters:
  - Comment ID to delete

## Examples

### Managing Books
1. List all books:
   ```
   ab
   ```

2. Find a specific book by ID:
   ```
   bbid 1
   ```

3. Add a new book:
   ```
   bins "The Great Gatsby" 1 1,3
   ```
   This adds a new book titled "The Great Gatsby" with author ID 1 and genre IDs 1 and 3.

4. Update an existing book:
   ```
   bupd 2 "Updated Title" 2 2,4
   ```
   This updates the book with ID 2, changing its title to "Updated Title", author to ID 2, and genres to IDs 2 and 4.

5. Delete a book:
   ```
   bdel 3
   ```
   This deletes the book with ID 3.

### Managing Authors and Genres
1. List all authors:
   ```
   aa
   ```

2. List all genres:
   ```
   ag
   ```

### Managing Comments
1. List all comments for a book:
   ```
   acbi 1
   ```
   This lists all comments for the book with ID 1.

2. Find a specific comment by ID:
   ```
   cbid 1
   ```
   This finds the comment with ID 1.

3. Add a new comment:
   ```
   cins "I really enjoyed this book!" 2
   ```
   This adds a new comment to the book with ID 2.

4. Update an existing comment:
   ```
   cupd 3 "After rereading, I have a different opinion"
   ```
   This updates the comment with ID 3.

5. Delete a comment:
   ```
   cdel 4
   ```
   This deletes the comment with ID 4.

## Database Migration
The application uses Flyway for database migration. Migration scripts are located in:
- `src/main/resources/db/migration`

The migration process is automatically executed when the application starts.
