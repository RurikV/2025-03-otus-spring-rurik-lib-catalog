// Books JavaScript module for AJAX operations

class BooksAPI {
    constructor() {
        this.baseUrl = '/api/books';
    }

    async getAllBooks() {
        try {
            const response = await fetch(this.baseUrl, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error fetching books:', error);
            throw error;
        }
    }

    async getBook(id) {
        try {
            const response = await fetch(`${this.baseUrl}/${id}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error fetching book:', error);
            throw error;
        }
    }

    async createBook(bookData) {
        try {
            const response = await fetch(this.baseUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(bookData)
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error creating book:', error);
            throw error;
        }
    }

    async updateBook(id, bookData) {
        try {
            const response = await fetch(`${this.baseUrl}/${id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(bookData)
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error updating book:', error);
            throw error;
        }
    }

    async deleteBook(id) {
        try {
            const response = await fetch(`${this.baseUrl}/${id}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return true;
        } catch (error) {
            console.error('Error deleting book:', error);
            throw error;
        }
    }
}

// UI Helper functions
class BooksUI {
    constructor() {
        this.api = new BooksAPI();
    }

    async loadBooksList() {
        try {
            const books = await this.api.getAllBooks();
            this.renderBooksList(books);
        } catch (error) {
            this.showError('Failed to load books: ' + error.message);
        }
    }

    renderBooksList(books) {
        const container = document.getElementById('books-container');
        if (!container) return;

        if (books.length === 0) {
            container.innerHTML = '<div class="alert alert-info">No books found.</div>';
            return;
        }

        const booksHtml = books.map(book => `
            <div class="col-md-6 col-lg-4 mb-4">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">${this.escapeHtml(book.title)}</h5>
                        <p class="card-text">
                            <strong>Author:</strong> 
                            <span>${this.escapeHtml(book.author.fullName)}</span>
                        </p>
                        <p class="card-text">
                            <strong>Genres:</strong>
                            <span>${book.genres.map(genre => this.escapeHtml(genre.name)).join(', ')}</span>
                        </p>
                        <div class="btn-group" role="group">
                            <a href="/books/${book.id}" class="btn btn-outline-primary btn-sm">View</a>
                            <a href="/books/${book.id}/edit" class="btn btn-outline-secondary btn-sm">Edit</a>
                            <button onclick="booksUI.deleteBookConfirm('${book.id}')" class="btn btn-outline-danger btn-sm">Delete</button>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');

        container.innerHTML = `<div class="row">${booksHtml}</div>`;
    }

    async loadBookDetails(id) {
        try {
            const book = await this.api.getBook(id);
            this.renderBookDetails(book);
        } catch (error) {
            this.showError('Failed to load book details: ' + error.message);
        }
    }

    renderBookDetails(book) {
        const container = document.getElementById('book-details');
        if (!container) return;

        container.innerHTML = `
            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h2>${this.escapeHtml(book.title)}</h2>
                    <div class="btn-group">
                        <a href="/books/${book.id}/edit" class="btn btn-outline-secondary btn-sm">Edit</a>
                        <button onclick="booksUI.deleteBookConfirm('${book.id}')" class="btn btn-outline-danger btn-sm">Delete</button>
                    </div>
                </div>
                <div class="card-body">
                    <p class="card-text">
                        <strong>Author:</strong> 
                        <span>${this.escapeHtml(book.author.fullName)}</span>
                    </p>
                    <p class="card-text">
                        <strong>Genres:</strong>
                        <span>${book.genres.map(genre => this.escapeHtml(genre.name)).join(', ')}</span>
                    </p>
                </div>
            </div>
        `;
    }

    async deleteBookConfirm(id) {
        if (confirm('Are you sure you want to delete this book?')) {
            try {
                await this.api.deleteBook(id);
                this.showSuccess('Book deleted successfully');
                this.loadBooksList(); // Refresh the list
            } catch (error) {
                this.showError('Failed to delete book: ' + error.message);
            }
        }
    }

    async saveBook(formData, isEdit = false, bookId = null) {
        try {
            if (isEdit && bookId) {
                await this.api.updateBook(bookId, formData);
                this.showSuccess('Book updated successfully');
            } else {
                await this.api.createBook(formData);
                this.showSuccess('Book created successfully');
            }
            window.location.href = '/';
        } catch (error) {
            this.showError('Failed to save book: ' + error.message);
        }
    }

    showError(message) {
        this.showAlert(message, 'danger');
    }

    showSuccess(message) {
        this.showAlert(message, 'success');
    }

    showAlert(message, type) {
        const alertContainer = document.getElementById('alert-container') || document.body;
        const alert = document.createElement('div');
        alert.className = `alert alert-${type} alert-dismissible fade show`;
        alert.innerHTML = `
            ${this.escapeHtml(message)}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        alertContainer.insertBefore(alert, alertContainer.firstChild);
        
        // Auto-dismiss after 5 seconds
        setTimeout(() => {
            if (alert.parentNode) {
                alert.remove();
            }
        }, 5000);
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Global instance
const booksUI = new BooksUI();

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Load books list if on the main page
    if (document.getElementById('books-container')) {
        booksUI.loadBooksList();
    }
    
    // Load book details if on book view page
    const bookDetailsContainer = document.getElementById('book-details');
    if (bookDetailsContainer) {
        const bookId = bookDetailsContainer.dataset.bookId;
        if (bookId) {
            booksUI.loadBookDetails(bookId);
        }
    }
});