# Library Catalog Application

## Overview
This is a single-user console application for managing a library book catalog. The application allows you to maintain a catalog of books, authors, and genres in a library.

## Technologies Used
- Spring Boot
- Spring Data MongoDB
- Spring Shell
- MongoDB
- Docker Compose (for MongoDB setup)
- Maven

## How to Build and Run
1. Clone the repository
2. Navigate to the project directory
3. Start MongoDB using Docker Compose:
   ```
   cd hw08-mongo-db
   docker-compose up -d
   ```
4. Build the project using Maven:
   ```
   mvn clean package
   ```
5. Run the application:
   ```
   java -jar target/hw08-mongo-db-0.0.1-SNAPSHOT.jar
   ```

## MongoDB Document Structure
The application uses MongoDB with the following collections:
- **authors**: Stores information about book authors
- **genres**: Stores information about book genres
- **books**: Stores information about books with references to authors and genres

The database is initialized with sample data when the application starts.

## Available Commands

### Book Commands
- **ab** - Find all books
  ```
  ab
  ```

- **bbid** - Find book by id
  ```
  bbid "60c72b2f5e8e7a1234567890"
  ```

- **bins** - Insert a new book
  ```
  bins "Book Title" "60c72b2f5e8e7a1234567890" "60c72b2f5e8e7a1234567891,60c72b2f5e8e7a1234567892"
  ```
  Parameters:
  - Book title (in quotes if it contains spaces)
  - Author ID (MongoDB ObjectId as string)
  - Genre IDs (comma-separated list of MongoDB ObjectIds as strings)

- **bupd** - Update an existing book
  ```
  bupd "60c72b2f5e8e7a1234567893" "Updated Book Title" "60c72b2f5e8e7a1234567894" "60c72b2f5e8e7a1234567895,60c72b2f5e8e7a1234567896"
  ```
  Parameters:
  - Book ID to update (MongoDB ObjectId as string)
  - New book title
  - New author ID (MongoDB ObjectId as string)
  - New genre IDs (comma-separated list of MongoDB ObjectIds as strings)

- **bdel** - Delete a book by id
  ```
  bdel "60c72b2f5e8e7a1234567897"
  ```
  Parameters:
  - Book ID to delete (MongoDB ObjectId as string)

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
   bbid "60c72b2f5e8e7a1234567890"
   ```

3. Add a new book:
   ```
   bins "The Great Gatsby" "60c72b2f5e8e7a1234567891" "60c72b2f5e8e7a1234567892,60c72b2f5e8e7a1234567893"
   ```
   This adds a new book titled "The Great Gatsby" with the specified author ID and genre IDs.

4. Update an existing book:
   ```
   bupd "60c72b2f5e8e7a1234567894" "Updated Title" "60c72b2f5e8e7a1234567895" "60c72b2f5e8e7a1234567896,60c72b2f5e8e7a1234567897"
   ```
   This updates the book with the specified ID, changing its title, author, and genres.

5. Delete a book:
   ```
   bdel "60c72b2f5e8e7a1234567898"
   ```
   This deletes the book with the specified ID.

### Managing Authors and Genres
1. List all authors:
   ```
   aa
   ```

2. List all genres:
   ```
   ag
   ```

## MongoDB Admin Interface
The application includes MongoDB Express, a web-based MongoDB admin interface, which can be accessed at:
```
http://localhost:8081
```

This interface allows you to view and manage the MongoDB database, collections, and documents.
