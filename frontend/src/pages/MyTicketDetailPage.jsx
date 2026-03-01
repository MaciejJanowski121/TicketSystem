import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getToken, getAuthHeader, isLoggedIn, getCurrentUser } from '../utils/auth';
import { normalizeComments } from '../utils/comments';
import './MyTicketDetailPage.css';

function MyTicketDetailPage() {
  const { ticketId } = useParams();
  const [ticket, setTicket] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [comments, setComments] = useState([]);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [commentText, setCommentText] = useState('');
  const [commentSubmitting, setCommentSubmitting] = useState(false);
  const [commentError, setCommentError] = useState('');
  const navigate = useNavigate();

  // Category display names
  const categoryNames = {
    'ACCOUNT_MANAGEMENT': 'Konto-Management',
    'HARDWARE': 'Hardware',
    'PROGRAMS_TOOLS': 'Programme und Tools',
    'NETWORK': 'Netzwerk',
    'OTHER': 'Sonstiges'
  };

  // State display names
  const stateNames = {
    'UNASSIGNED': 'nicht zugeordnet',
    'IN_PROGRESS': 'in Bearbeitung',
    'CLOSED': 'abgeschlossen'
  };

  useEffect(() => {
    // Check if user is logged in
    if (!isLoggedIn()) {
      navigate('/login');
      return;
    }

    if (!ticketId) {
      navigate('/my-tickets');
      return;
    }

    fetchTicketDetails();
  }, [ticketId, navigate]);

  const fetchTicketDetails = async () => {
    setIsLoading(true);
    setError('');

    try {
      const token = getToken();
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await fetch(`http://localhost:8080/api/tickets/my/${ticketId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        }
      });

      if (response.ok) {
        const data = await response.json();
        setTicket(data);
        // Fetch comments after ticket is loaded
        fetchComments();
      } else if (response.status === 401) {
        // Token expired or invalid
        localStorage.removeItem('token');
        window.dispatchEvent(new Event('authStateChange'));
        navigate('/login');
      } else if (response.status === 403) {
        setError('Access denied: This ticket does not belong to you.');
      } else if (response.status === 404) {
        setError('Ticket not found.');
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Failed to load ticket details');
      }
    } catch (error) {
      setError('Network error. Please check your connection and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const fetchComments = async () => {
    setCommentsLoading(true);
    setCommentError('');

    try {
      const token = getToken();
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await fetch(`http://localhost:8080/api/tickets/${ticketId}/comments`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        }
      });

      if (response.ok) {
        const data = await response.json();
        setComments(data);
      } else if (response.status === 401) {
        // Token expired or invalid
        localStorage.removeItem('token');
        window.dispatchEvent(new Event('authStateChange'));
        navigate('/login');
      } else if (response.status === 403) {
        setCommentError('Access denied: You cannot view comments on this ticket.');
      } else if (response.status === 404) {
        setCommentError('Ticket not found.');
      } else {
        const errorData = await response.json();
        setCommentError(errorData.message || 'Failed to load comments');
      }
    } catch (error) {
      setCommentError('Network error. Please check your connection and try again.');
    } finally {
      setCommentsLoading(false);
    }
  };

  const createComment = async (e) => {
    e.preventDefault();

    if (!commentText.trim()) {
      setCommentError('Comment cannot be empty.');
      return;
    }

    setCommentSubmitting(true);
    setCommentError('');

    try {
      const token = getToken();
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await fetch(`http://localhost:8080/api/tickets/${ticketId}/comments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        },
        body: JSON.stringify({
          comment: commentText.trim()
        })
      });

      if (response.ok) {
        // Success - refresh comments and clear form
        setCommentText('');
        fetchComments();
      } else if (response.status === 401) {
        // Token expired or invalid
        localStorage.removeItem('token');
        window.dispatchEvent(new Event('authStateChange'));
        navigate('/login');
      } else if (response.status === 403) {
        setCommentError('Access denied: You cannot comment on this ticket.');
      } else if (response.status === 404) {
        setCommentError('Ticket not found.');
      } else {
        const errorData = await response.json();
        setCommentError(errorData.message || 'Failed to create comment');
      }
    } catch (error) {
      setCommentError('Network error. Please check your connection and try again.');
    } finally {
      setCommentSubmitting(false);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('de-DE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStateClass = (state) => {
    switch (state) {
      case 'UNASSIGNED':
        return 'state-unassigned';
      case 'IN_PROGRESS':
        return 'state-in-progress';
      case 'CLOSED':
        return 'state-closed';
      default:
        return '';
    }
  };

  if (isLoading) {
    return (
      <div className="page">
        <div className="container">
          <div className="ticket-detail-header">
            <Link to="/my-tickets" className="back-link">
              ← Back to My Tickets
            </Link>
          </div>
          <div className="loading-message">Loading ticket details...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page">
        <div className="container">
          <div className="ticket-detail-header">
            <Link to="/my-tickets" className="back-link">
              ← Back to My Tickets
            </Link>
          </div>
          <div className="message error">
            {error}
          </div>
        </div>
      </div>
    );
  }

  if (!ticket) {
    return (
      <div className="page">
        <div className="container">
          <div className="ticket-detail-header">
            <Link to="/my-tickets" className="back-link">
              ← Back to My Tickets
            </Link>
          </div>
          <div className="message error">
            Ticket not found.
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="container">
        <div className="ticket-detail-header">
          <Link to="/my-tickets" className="back-link">
            ← Back to My Tickets
          </Link>
          <h1>Ticket Details</h1>
        </div>

        <div className="ticket-detail-card">
          <div className="ticket-detail-header-info">
            <div className="ticket-detail-title-section">
              <h2 className="ticket-detail-title">{ticket.title}</h2>
              <span className={`ticket-state ${getStateClass(ticket.ticketState)}`}>
                {stateNames[ticket.ticketState] || ticket.ticketState}
              </span>
            </div>
            <div className="ticket-detail-id">
              Ticket ID: #{ticket.ticketId}
            </div>
          </div>

          <div className="ticket-detail-content">
            <div className="ticket-detail-section">
              <h3>Description</h3>
              <div className="ticket-description">
                {ticket.description}
              </div>
            </div>

            <div className="ticket-detail-meta">
              <div className="meta-grid">
                <div className="meta-item">
                  <strong>Category:</strong>
                  <span>{categoryNames[ticket.ticketCategory] || ticket.ticketCategory}</span>
                </div>

                <div className="meta-item">
                  <strong>Status:</strong>
                  <span className={`ticket-state ${getStateClass(ticket.ticketState)}`}>
                    {stateNames[ticket.ticketState] || ticket.ticketState}
                  </span>
                </div>

                <div className="meta-item">
                  <strong>Created:</strong>
                  <span>{formatDate(ticket.createDate)}</span>
                </div>

                <div className="meta-item">
                  <strong>Last Updated:</strong>
                  <span>{formatDate(ticket.updateDate)}</span>
                </div>

                {ticket.assignedSupport && (
                  <div className="meta-item">
                    <strong>Assigned to:</strong>
                    <span>{ticket.assignedSupport}</span>
                  </div>
                )}
              </div>
            </div>

            <div className="ticket-detail-section">
              <h3>Comments</h3>

              {commentError && (
                <div className="message error">
                  {commentError}
                </div>
              )}

              {commentsLoading ? (
                <div className="loading-message">Loading comments...</div>
              ) : (
                <div className="comments-section">
                  {comments.length === 0 ? (
                    <p className="no-comments">No comments yet.</p>
                  ) : (
                    <div className="comments-list">
                      {normalizeComments(comments, 'comments').map((comment, index) => (
                        <div key={index} className="comment-item">
                          <div className="comment-header">
                            <span className="comment-author">
                              {comment.authorUsername}
                            </span>
                            <span className="comment-date">
                              {formatDate(comment.commentDate)}
                            </span>
                          </div>
                          <div className="comment-text">
                            {comment.comment}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}

                  <form onSubmit={createComment} className="comment-form">
                    <div className="form-group">
                      <textarea
                        value={commentText}
                        onChange={(e) => setCommentText(e.target.value)}
                        placeholder="Add a comment..."
                        rows="4"
                        className="comment-textarea"
                        disabled={commentSubmitting}
                      />
                    </div>
                    <button 
                      type="submit" 
                      className="btn btn-primary"
                      disabled={commentSubmitting || !commentText.trim()}
                    >
                      {commentSubmitting ? 'Adding Comment...' : 'Add Comment'}
                    </button>
                  </form>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default MyTicketDetailPage;
