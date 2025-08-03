// Global variables
let currentBookId = null;
let currentCommentId = null;
let currentViewBookId = null;
let authors = [];
let genres = [];

// API Classes
class BooksAPI {
    constructor() {
        this.baseUrl = '/api/books';
    }

    async getAllBooks() {
        const response = await fetch(this.baseUrl, {
            headers: { 'Content-Type': 'application/json' }
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    }

    async getBook(id) {
        const response = await fetch(`${this.baseUrl}/${id}`, {
            headers: { 'Content-Type': 'application/json' }
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    }

    async createBook(bookData) {
        const response = await fetch(this.baseUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(bookData)
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    }

    async updateBook(id, bookData) {
        const response = await fetch(`${this.baseUrl}/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(bookData)
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    }

    async deleteBook(id) {
        const response = await fetch(`${this.baseUrl}/${id}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' }
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return true;
    }
}

class AuthorsAPI {
    async getAllAuthors() {
        const response = await fetch('/api/authors', {
            headers: { 'Content-Type': 'application/json' }
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    }
}

class GenresAPI {
    async getAllGenres() {
        const response = await fetch('/api/genres', {
            headers: { 'Content-Type': 'application/json' }
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    }
}

class CommentsAPI {
    async getCommentsByBookId(bookId) {
        const response = await fetch(`/api/books/${bookId}/comments`, {
            headers: { 'Content-Type': 'application/json' }
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    }

    async createComment(bookId, commentData) {
        const response = await fetch(`/api/books/${bookId}/comments`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(commentData)
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    }

    async updateComment(id, commentData) {
        const response = await fetch(`/api/comments/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(commentData)
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    }

    async deleteComment(id) {
        const response = await fetch(`/api/comments/${id}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' }
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return true;
    }
}

// Initialize APIs
const booksAPI = new BooksAPI();
const authorsAPI = new AuthorsAPI();
const genresAPI = new GenresAPI();
const commentsAPI = new CommentsAPI();

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    showSection('books');
});

// Navigation functions
function showSection(section) {
    // Hide all sections
    document.querySelectorAll('.content-section').forEach(s => s.style.display = 'none');
    document.querySelectorAll('.nav-link').forEach(n => n.classList.remove('active'));
    
    // Show selected section
    document.getElementById(section + '-section').style.display = 'block';
    document.getElementById('nav-' + section).classList.add('active');
    
    // Load data for the section
    switch(section) {
        case 'books':
            loadBooks();
            break;
        case 'authors':
            loadAuthors();
            break;
        case 'genres':
            loadGenres();
            break;
    }
}

// Books functions
async function loadBooks() {
    try {
        const books = await booksAPI.getAllBooks();
        displayBooks(books);
    } catch (error) {
        showError('Failed to load books: ' + error.message);
    }
}

function displayBooks(books) {
    const container = document.getElementById('books-container');
    if (books.length === 0) {
        container.innerHTML = '<p class="text-muted">No books found.</p>';
        return;
    }
    
    const booksHtml = books.map(book => `
        <div class="card mb-3">
            <div class="card-body">
                <h5 class="card-title">${book.title}</h5>
                <p class="card-text">
                    <strong>Author:</strong> ${book.author.fullName}<br>
                    <strong>Genres:</strong> ${book.genres.map(g => g.name).join(', ')}
                </p>
                <button class="btn btn-sm btn-outline-primary" onclick="viewBook('${book.id}')">View</button>
                <button class="btn btn-sm btn-outline-secondary" onclick="editBook('${book.id}')">Edit</button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteBook('${book.id}')">Delete</button>
            </div>
        </div>
    `).join('');
    
    container.innerHTML = booksHtml;
}

async function viewBook(id) {
    try {
        currentViewBookId = id;
        const book = await booksAPI.getBook(id);
        const comments = await commentsAPI.getCommentsByBookId(id);
        
        document.getElementById('bookViewTitle').textContent = book.title;
        document.getElementById('bookViewContent').innerHTML = `
            <p><strong>Author:</strong> ${book.author.fullName}</p>
            <p><strong>Genres:</strong> ${book.genres.map(g => g.name).join(', ')}</p>
            
            <hr>
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h6>Comments</h6>
                <button class="btn btn-sm btn-primary" onclick="showCommentForm()">Add Comment</button>
            </div>
            <div id="comments-container">
                ${displayComments(comments)}
            </div>
        `;
        new bootstrap.Modal(document.getElementById('bookViewModal')).show();
    } catch (error) {
        showError('Failed to load book details: ' + error.message);
    }
}

function displayComments(comments) {
    if (comments.length === 0) {
        return '<p class="text-muted">No comments yet.</p>';
    }
    
    return comments.map(comment => `
        <div class="card mb-2">
            <div class="card-body">
                <p class="card-text">${comment.text}</p>
                <div class="btn-group btn-group-sm">
                    <button class="btn btn-outline-secondary" onclick="editComment('${comment.id}', '${comment.text}')">Edit</button>
                    <button class="btn btn-outline-danger" onclick="deleteComment('${comment.id}')">Delete</button>
                </div>
            </div>
        </div>
    `).join('');
}

async function editBook(id) {
    try {
        const book = await booksAPI.getBook(id);
        currentBookId = id;
        document.getElementById('bookModalTitle').textContent = 'Edit Book';
        document.getElementById('bookTitle').value = book.title;
        
        // Load authors and genres for the form first
        await loadAuthorsForForm();
        await loadGenresForForm();
        
        // Then set the selected values
        document.getElementById('bookAuthor').value = book.author.id;
        
        // Set selected genres
        const genreSelect = document.getElementById('bookGenres');
        Array.from(genreSelect.options).forEach(option => {
            option.selected = book.genres.some(g => g.id === option.value);
        });
        
        new bootstrap.Modal(document.getElementById('bookModal')).show();
    } catch (error) {
        showError('Failed to load book for editing: ' + error.message);
    }
}

async function showBookForm() {
    currentBookId = null;
    document.getElementById('bookModalTitle').textContent = 'Add Book';
    document.getElementById('bookForm').reset();
    
    // Load authors and genres for the form
    await loadAuthorsForForm();
    await loadGenresForForm();
    
    new bootstrap.Modal(document.getElementById('bookModal')).show();
}

async function saveBook() {
    const title = document.getElementById('bookTitle').value;
    const authorId = document.getElementById('bookAuthor').value;
    const genreIds = Array.from(document.getElementById('bookGenres').selectedOptions).map(o => o.value);
    
    if (!title || !authorId || genreIds.length === 0) {
        showError('Please fill in all fields');
        return;
    }
    
    const bookData = { title, authorId, genreIds };
    
    try {
        if (currentBookId) {
            await booksAPI.updateBook(currentBookId, bookData);
            showSuccess('Book updated successfully');
        } else {
            await booksAPI.createBook(bookData);
            showSuccess('Book created successfully');
        }
        
        bootstrap.Modal.getInstance(document.getElementById('bookModal')).hide();
        loadBooks();
    } catch (error) {
        showError('Failed to save book: ' + error.message);
    }
}

async function deleteBook(id) {
    if (confirm('Are you sure you want to delete this book?')) {
        try {
            await booksAPI.deleteBook(id);
            showSuccess('Book deleted successfully');
            loadBooks();
        } catch (error) {
            showError('Failed to delete book: ' + error.message);
        }
    }
}

// Comment functions
function showCommentForm() {
    currentCommentId = null;
    document.getElementById('commentModalTitle').textContent = 'Add Comment';
    document.getElementById('commentForm').reset();
    new bootstrap.Modal(document.getElementById('commentModal')).show();
}

function editComment(id, text) {
    currentCommentId = id;
    document.getElementById('commentModalTitle').textContent = 'Edit Comment';
    document.getElementById('commentText').value = text;
    new bootstrap.Modal(document.getElementById('commentModal')).show();
}

async function saveComment() {
    const text = document.getElementById('commentText').value.trim();
    
    if (!text) {
        showError('Please enter a comment');
        return;
    }
    
    try {
        if (currentCommentId) {
            await commentsAPI.updateComment(currentCommentId, { text });
            showSuccess('Comment updated successfully');
        } else {
            await commentsAPI.createComment(currentViewBookId, { text });
            showSuccess('Comment added successfully');
        }
        
        bootstrap.Modal.getInstance(document.getElementById('commentModal')).hide();
        // Refresh the book view to show updated comments without reopening modal
        await refreshBookViewComments();
    } catch (error) {
        showError('Failed to save comment: ' + error.message);
    }
}

async function deleteComment(id) {
    if (confirm('Are you sure you want to delete this comment?')) {
        try {
            await commentsAPI.deleteComment(id);
            showSuccess('Comment deleted successfully');
            // Refresh the book view to show updated comments without reopening modal
            await refreshBookViewComments();
        } catch (error) {
            showError('Failed to delete comment: ' + error.message);
        }
    }
}

async function refreshBookViewComments() {
    try {
        const comments = await commentsAPI.getCommentsByBookId(currentViewBookId);
        const commentsContainer = document.getElementById('comments-container');
        if (commentsContainer) {
            commentsContainer.innerHTML = displayComments(comments);
        }
    } catch (error) {
        showError('Failed to refresh comments: ' + error.message);
    }
}

// Authors functions
async function loadAuthors() {
    try {
        const authors = await authorsAPI.getAllAuthors();
        displayAuthors(authors);
    } catch (error) {
        showError('Failed to load authors: ' + error.message);
    }
}

function displayAuthors(authors) {
    const container = document.getElementById('authors-container');
    if (authors.length === 0) {
        container.innerHTML = '<p class="text-muted">No authors found.</p>';
        return;
    }
    
    const authorsHtml = authors.map(author => `
        <div class="card mb-3">
            <div class="card-body">
                <h5 class="card-title">${author.fullName}</h5>
            </div>
        </div>
    `).join('');
    
    container.innerHTML = authorsHtml;
}

// Genres functions
async function loadGenres() {
    try {
        const genres = await genresAPI.getAllGenres();
        displayGenres(genres);
    } catch (error) {
        showError('Failed to load genres: ' + error.message);
    }
}

function displayGenres(genres) {
    const container = document.getElementById('genres-container');
    if (genres.length === 0) {
        container.innerHTML = '<p class="text-muted">No genres found.</p>';
        return;
    }
    
    const genresHtml = genres.map(genre => `
        <div class="card mb-3">
            <div class="card-body">
                <h5 class="card-title">${genre.name}</h5>
            </div>
        </div>
    `).join('');
    
    container.innerHTML = genresHtml;
}

// Form helper functions
async function loadAuthorsForForm() {
    try {
        authors = await authorsAPI.getAllAuthors();
        const select = document.getElementById('bookAuthor');
        select.innerHTML = '<option value="">Select Author</option>';
        authors.forEach(author => {
            select.innerHTML += `<option value="${author.id}">${author.fullName}</option>`;
        });
    } catch (error) {
        showError('Failed to load authors: ' + error.message);
    }
}

async function loadGenresForForm() {
    try {
        genres = await genresAPI.getAllGenres();
        const select = document.getElementById('bookGenres');
        select.innerHTML = '';
        genres.forEach(genre => {
            select.innerHTML += `<option value="${genre.id}">${genre.name}</option>`;
        });
    } catch (error) {
        showError('Failed to load genres: ' + error.message);
    }
}

// Utility functions
function showError(message) {
    const alertContainer = document.getElementById('alert-container');
    alertContainer.innerHTML = `
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
}

function showSuccess(message) {
    const alertContainer = document.getElementById('alert-container');
    alertContainer.innerHTML = `
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
}