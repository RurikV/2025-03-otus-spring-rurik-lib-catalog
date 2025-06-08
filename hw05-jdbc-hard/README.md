# Library Catalog Application

## Overview
This is a single-user console application for managing a library book catalog. The application allows you to maintain a catalog of books, authors, and genres in a library.

## Technologies Used
- Spring Boot
- Spring JDBC
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
   java -jar target/hw05-jdbc-0.0.1-SNAPSHOT.jar
   ```

## Database Schema
The application uses an H2 in-memory database with the following schema:
- **authors**: Stores information about book authors
- **genres**: Stores information about book genres
- **books**: Stores information about books
- **books_genres**: Junction table for the many-to-many relationship between books and genres

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

## Database Migration
The application uses Flyway for database migration. Migration scripts are located in:
- `src/main/resources/db/migration`

The migration process is automatically executed when the application starts.