// Comments JavaScript module for AJAX operations

class CommentsAPI {
    constructor() {
        this.baseUrl = '/api';
    }

    async getCommentsByBookId(bookId) {
        try {
            const response = await fetch(`${this.baseUrl}/books/${bookId}/comments`, {
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
            console.error('Error fetching comments:', error);
            throw error;
        }
    }

    async getComment(id) {
        try {
            const response = await fetch(`${this.baseUrl}/comments/${id}`, {
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
            console.error('Error fetching comment:', error);
            throw error;
        }
    }

    async createComment(bookId, commentData) {
        try {
            const response = await fetch(`${this.baseUrl}/books/${bookId}/comments`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(commentData)
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error creating comment:', error);
            throw error;
        }
    }

    async updateComment(id, commentData) {
        try {
            const response = await fetch(`${this.baseUrl}/comments/${id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(commentData)
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error updating comment:', error);
            throw error;
        }
    }

    async deleteComment(id) {
        try {
            const response = await fetch(`${this.baseUrl}/comments/${id}`, {
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
            console.error('Error deleting comment:', error);
            throw error;
        }
    }
}

// UI Helper functions
class CommentsUI {
    constructor() {
        this.api = new CommentsAPI();
    }

    async loadCommentsByBookId(bookId) {
        try {
            const comments = await this.api.getCommentsByBookId(bookId);
            this.renderCommentsList(comments);
        } catch (error) {
            this.showError('Failed to load comments: ' + error.message);
        }
    }

    renderCommentsList(comments) {
        const container = document.getElementById('comments-container');
        if (!container) return;

        if (comments.length === 0) {
            container.innerHTML = '<div class="alert alert-info">No comments yet.</div>';
            return;
        }

        const commentsHtml = comments.map(comment => `
            <div class="card mb-3">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start">
                        <p class="card-text">${this.escapeHtml(comment.text)}</p>
                        <div class="btn-group btn-group-sm">
                            <a href="/comments/${comment.id}/edit" class="btn btn-outline-secondary btn-sm">Edit</a>
                            <button onclick="commentsUI.deleteCommentConfirm('${comment.id}')" class="btn btn-outline-danger btn-sm">Delete</button>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');

        container.innerHTML = commentsHtml;
    }

    async loadCommentDetails(id) {
        try {
            const comment = await this.api.getComment(id);
            return comment;
        } catch (error) {
            this.showError('Failed to load comment details: ' + error.message);
            throw error;
        }
    }

    async deleteCommentConfirm(id) {
        if (confirm('Are you sure you want to delete this comment?')) {
            try {
                await this.api.deleteComment(id);
                this.showSuccess('Comment deleted successfully');
                // Reload comments or redirect
                window.location.reload();
            } catch (error) {
                this.showError('Failed to delete comment: ' + error.message);
            }
        }
    }

    async saveComment(commentData, isEdit = false, commentId = null, bookId = null) {
        try {
            if (isEdit && commentId) {
                await this.api.updateComment(commentId, commentData);
                this.showSuccess('Comment updated successfully');
            } else if (bookId) {
                await this.api.createComment(bookId, commentData);
                this.showSuccess('Comment created successfully');
            }
            // Redirect back to book view
            const redirectBookId = bookId || (await this.api.getComment(commentId)).book.id;
            window.location.href = `/books/${redirectBookId}`;
        } catch (error) {
            this.showError('Failed to save comment: ' + error.message);
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
const commentsUI = new CommentsUI();