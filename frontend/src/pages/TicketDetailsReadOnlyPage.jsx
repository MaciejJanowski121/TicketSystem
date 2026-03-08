import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getToken, getAuthHeader, isLoggedIn, getCurrentUser } from '../utils/auth';
import { normalizeComments } from '../utils/comments';
import './TicketDetailsReadOnlyPage.css';

function TicketDetailsReadOnlyPage() {
  const { ticketId } = useParams();
  const [ticket, setTicket] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionLoading, setActionLoading] = useState({});
  const [commentText, setCommentText] = useState('');
  const [commentSubmitting, setCommentSubmitting] = useState(false);
  const [commentError, setCommentError] = useState('');
  const [closeModal, setCloseModal] = useState({ show: false, ticketId: null, ticketTitle: '' });
  const [closeComment, setCloseComment] = useState('');
  const navigate = useNavigate();

  // Helper function to get back link based on user role
  const getBackLink = () => {
    const currentUser = getCurrentUser();
    if (currentUser && (currentUser.role === 'SUPPORTUSER' || currentUser.role === 'ADMINUSER')) {
      return {
        path: '/tickets',
        text: '← Zurück zu Tickets'
      };
    }
    return {
      path: '/tickets',
      text: '← Zurück zu Alle Tickets'
    };
  };

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
      setError('Bitte melden Sie sich an');
      navigate('/login');
      return;
    }

    if (!ticketId) {
      navigate('/tickets');
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
        setError('Bitte melden Sie sich an');
        navigate('/login');
        return;
      }

      const response = await fetch(`http://localhost:8080/api/tickets/${ticketId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        }
      });

      if (response.ok) {
        const data = await response.json();
        setTicket(data);
      } else if (response.status === 401) {
        // Token expired or invalid
        localStorage.removeItem('token');
        window.dispatchEvent(new Event('authStateChange'));
        setError('Bitte melden Sie sich an');
        navigate('/login');
      } else if (response.status === 404) {
        setError('Ticket nicht gefunden');
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Fehler beim Laden der Ticket-Details');
      }
    } catch (error) {
      setError('Netzwerkfehler. Bitte überprüfen Sie Ihre Verbindung und versuchen Sie es erneut.');
    } finally {
      setIsLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return null;
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

  const createComment = async (e) => {
    e.preventDefault();

    if (!commentText.trim()) {
      setCommentError('Kommentar darf nicht leer sein.');
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
        // Success - refresh ticket details and clear form
        setCommentText('');
        fetchTicketDetails();
      } else if (response.status === 401) {
        // Token expired or invalid
        localStorage.removeItem('token');
        window.dispatchEvent(new Event('authStateChange'));
        navigate('/login');
      } else if (response.status === 403) {
        const errorData = await response.json();
        setCommentError(errorData.message || 'Zugriff verweigert: Sie können dieses Ticket nicht kommentieren.');
      } else if (response.status === 404) {
        setCommentError('Ticket nicht gefunden.');
      } else {
        const errorData = await response.json();
        setCommentError(errorData.message || 'Fehler beim Erstellen des Kommentars');
      }
    } catch (error) {
      setCommentError('Netzwerkfehler. Bitte überprüfen Sie Ihre Verbindung und versuchen Sie es erneut.');
    } finally {
      setCommentSubmitting(false);
    }
  };

  const canComment = () => {
    const currentUser = getCurrentUser();
    if (!currentUser || !ticket) return false;

    // ENDUSER can comment on their own tickets
    if (currentUser.role === 'ENDUSER' && ticket.creatorEmail === currentUser.email) {
      return true;
    }

    // SUPPORTUSER can comment on tickets assigned to them
    if (currentUser.role === 'SUPPORTUSER' && ticket.assignedSupport === currentUser.username) {
      return true;
    }

    // ADMINUSER can comment on any ticket
    if (currentUser.role === 'ADMINUSER') {
      return true;
    }

    return false;
  };

  // Support action functions
  const handleSupportAction = async (action, comment = null) => {
    setActionLoading(prev => ({ ...prev, [action]: true }));
    setError('');

    try {
      const token = getToken();
      if (!token) {
        navigate('/login');
        return;
      }

      let url = `http://localhost:8080/api/support/tickets/${ticketId}`;
      let method = 'POST';
      let body = null;

      switch (action) {
        case 'assign':
          url += '/assign';
          break;
        case 'release':
          url += '/release';
          break;
        case 'close':
          url += '/close';
          body = { comment: comment };
          break;
        default:
          return;
      }

      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        },
        body: body ? JSON.stringify(body) : null
      });

      if (response.ok) {
        // Refresh ticket details after successful action
        fetchTicketDetails();
      } else {
        const errorData = await response.json();
        setError(errorData.message || `Fehler beim ${action === 'assign' ? 'Zuweisen' : action === 'release' ? 'Aufheben der Zuweisung' : 'Schließen'} des Tickets`);
      }
    } catch (error) {
      setError(`Netzwerkfehler beim Versuch, das Ticket zu ${action === 'assign' ? 'zuweisen' : action === 'release' ? 'entziehen' : 'schließen'}.`);
    } finally {
      setActionLoading(prev => ({ ...prev, [action]: false }));
    }
  };

  const handleUpdateStatus = async (newStatus) => {
    setActionLoading(prev => ({ ...prev, 'update-status': true }));
    setError('');

    try {
      const token = getToken();
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await fetch(`http://localhost:8080/api/support/tickets/${ticketId}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        },
        body: JSON.stringify({ ticketState: newStatus })
      });

      if (response.ok) {
        fetchTicketDetails();
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Fehler beim Aktualisieren des Ticket-Status');
      }
    } catch (error) {
      setError('Netzwerkfehler beim Aktualisieren des Ticket-Status.');
    } finally {
      setActionLoading(prev => ({ ...prev, 'update-status': false }));
    }
  };

  const handleUpdateCategory = async (newCategory) => {
    setActionLoading(prev => ({ ...prev, 'update-category': true }));
    setError('');

    try {
      const token = getToken();
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await fetch(`http://localhost:8080/api/support/tickets/${ticketId}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        },
        body: JSON.stringify({ ticketCategory: newCategory })
      });

      if (response.ok) {
        fetchTicketDetails();
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Fehler beim Aktualisieren der Ticket-Kategorie');
      }
    } catch (error) {
      setError('Netzwerkfehler beim Aktualisieren der Ticket-Kategorie.');
    } finally {
      setActionLoading(prev => ({ ...prev, 'update-category': false }));
    }
  };

  // Permission functions
  const canAssign = () => {
    const currentUser = getCurrentUser();
    return ticket && currentUser && 
           (currentUser.role === 'SUPPORTUSER' || currentUser.role === 'ADMINUSER') &&
           ticket.ticketState !== 'CLOSED' && 
           !ticket.assignedSupport;
  };

  const canRelease = () => {
    const currentUser = getCurrentUser();
    return ticket && currentUser && 
           ticket.ticketState !== 'CLOSED' && 
           ticket.assignedSupport && 
           (ticket.assignedSupport === currentUser.username || currentUser.role === 'ADMINUSER');
  };

  const canClose = () => {
    const currentUser = getCurrentUser();
    return ticket && currentUser && 
           ticket.ticketState !== 'CLOSED' && 
           (ticket.assignedSupport === currentUser.username || currentUser.role === 'ADMINUSER');
  };

  const canUpdate = () => {
    const currentUser = getCurrentUser();
    return ticket && currentUser && 
           (ticket.assignedSupport === currentUser.username || currentUser.role === 'ADMINUSER');
  };

  const handleCloseTicketClick = () => {
    setCloseModal({ 
      show: true, 
      ticketId: ticketId, 
      ticketTitle: ticket.title 
    });
    setCloseComment('');
  };

  const handleCloseModalCancel = () => {
    setCloseModal({ show: false, ticketId: null, ticketTitle: '' });
    setCloseComment('');
  };

  const handleCloseModalConfirm = async () => {
    if (!closeComment.trim()) {
      setError('Schließungskommentar ist erforderlich');
      return;
    }

    setCloseModal({ show: false, ticketId: null, ticketTitle: '' });
    await handleSupportAction('close', closeComment.trim());
    setCloseComment('');
  };

  if (isLoading) {
    return (
      <div className="page">
        <div className="container">
          <div className="ticket-detail-header">
            <Link to={getBackLink().path} className="back-link">
              {getBackLink().text}
            </Link>
          </div>
          <div className="loading-message">Ticket-Details werden geladen...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page">
        <div className="container">
          <div className="ticket-detail-header">
            <Link to={getBackLink().path} className="back-link">
              {getBackLink().text}
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
            <Link to={getBackLink().path} className="back-link">
              {getBackLink().text}
            </Link>
          </div>
          <div className="message error">
            Ticket nicht gefunden
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="container">
        <div className="ticket-detail-header">
          <Link to={getBackLink().path} className="back-link">
            {getBackLink().text}
          </Link>
        </div>

        <div className="ticket-detail-card">
          {/* Header */}
          <div className="ticket-header">
            <h1 className="ticket-title">{ticket.title}</h1>
            <div className="ticket-id">#{ticket.ticketId}</div>
          </div>

          <div className="ticket-content">
            {/* Basic Information */}
            <div className="basic-info">
              <div className="info-row">
                <div className="info-item">
                  <strong>Kategorie</strong>
                  <span>{categoryNames[ticket.ticketCategory] || ticket.ticketCategory}</span>
                </div>
                <div className="info-item">
                  <strong>Status</strong>
                  <span className={`ticket-state ${getStateClass(ticket.ticketState)}`}>
                    {stateNames[ticket.ticketState] || ticket.ticketState}
                  </span>
                </div>
                <div className="info-item">
                  {canAssign() && (
                    <button
                      className="assign-button"
                      onClick={() => handleSupportAction('assign')}
                      disabled={actionLoading['assign']}
                    >
                      {actionLoading['assign'] ? 'Wird zugewiesen...' : 'Ticket übernehmen'}
                    </button>
                  )}
                  {ticket.assignedSupport && (
                    <>
                      <strong>Zugewiesen an</strong>
                      <span>{ticket.assignedSupport}</span>
                    </>
                  )}
                </div>
              </div>
              <div className="info-row">
                <div className="info-item">
                  <strong>Ersteller</strong>
                  <span>{ticket.creatorUsername || 'Unbekannt'}</span>
                </div>
                <div className="info-item">
                  <strong>Erstellt-Datum</strong>
                  <span>{formatDate(ticket.createDate)}</span>
                </div>
                <div className="info-item">
                  <strong>Zuletzt geändert</strong>
                  <span>{formatDate(ticket.updateDate)}</span>
                </div>
              </div>
            </div>

            {/* Processing Options - Support/Admin only */}
            {(getCurrentUser()?.role === 'SUPPORTUSER' || getCurrentUser()?.role === 'ADMINUSER') && (
              <div className="processing-options">
                <h3>Bearbeitungs-Optionen</h3>
                <div className="action-buttons">
                  {canRelease() && (
                    <button
                      className="action-btn release-btn"
                      onClick={() => handleSupportAction('release')}
                      disabled={actionLoading['release']}
                    >
                      {actionLoading['release'] ? 'Wird aufgehoben...' : 'Zuweisung aufheben'}
                    </button>
                  )}
                  {canClose() && (
                    <button
                      className="action-btn close-btn"
                      onClick={handleCloseTicketClick}
                      disabled={actionLoading['close']}
                    >
                      {actionLoading['close'] ? 'Wird geschlossen...' : 'Ticket schließen'}
                    </button>
                  )}
                  {canUpdate() && (
                    <>
                      <select
                        className="action-select"
                        onChange={(e) => {
                          if (e.target.value) {
                            handleUpdateStatus(e.target.value);
                            e.target.value = '';
                          }
                        }}
                        disabled={actionLoading['update-status']}
                      >
                        <option value="">Status ändern</option>
                        <option value="UNASSIGNED">Nicht zugeordnet</option>
                        <option value="IN_PROGRESS">In Bearbeitung</option>
                        <option value="CLOSED">Abgeschlossen</option>
                      </select>
                      <select
                        className="action-select"
                        onChange={(e) => {
                          if (e.target.value) {
                            handleUpdateCategory(e.target.value);
                            e.target.value = '';
                          }
                        }}
                        disabled={actionLoading['update-category']}
                      >
                        <option value="">Kategorie ändern</option>
                        <option value="ACCOUNT_MANAGEMENT">Konto-Management</option>
                        <option value="HARDWARE">Hardware</option>
                        <option value="PROGRAMS_TOOLS">Programme und Tools</option>
                        <option value="NETWORK">Netzwerk</option>
                        <option value="OTHER">Sonstiges</option>
                      </select>
                    </>
                  )}
                </div>
              </div>
            )}

            {/* Description */}
            <div className="description-section">
              <h3>Beschreibung</h3>
              <div className="description-content">
                {ticket.description}
              </div>
            </div>

            {/* Comments */}
            <div className="comments-section">
              <h3>Kommentare</h3>
              {commentError && (
                <div className="message error">
                  {commentError}
                </div>
              )}

              {!ticket.comments || ticket.comments.length === 0 ? (
                <p className="no-comments">Noch keine Kommentare.</p>
              ) : (
                <div className="comments-list">
                  {normalizeComments(ticket.comments, 'ticket')
                    .map((comment, index) => {
                      const isSupport = comment.authorRole === 'SUPPORTUSER' || comment.authorRole === 'ADMINUSER';
                      return (
                        <div key={index} className={`comment-item ${isSupport ? 'comment-support' : 'comment-user'}`}>
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
                      );
                    })}
                </div>
              )}

              {canComment() && (
                <form onSubmit={createComment} className="comment-form">
                  <div className="form-group">
                    <textarea
                      value={commentText}
                      onChange={(e) => setCommentText(e.target.value)}
                      placeholder="Kommentar hinzufügen..."
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
                    {commentSubmitting ? 'Kommentar wird hinzugefügt...' : 'Kommentar hinzufügen'}
                  </button>
                </form>
              )}
            </div>
          </div>
        </div>

        {/* Close Ticket Modal */}
        {closeModal.show && (
          <div className="modal-overlay">
            <div className="modal-content">
              <h3>Ticket schließen</h3>
              <p>Möchten Sie das Ticket "{closeModal.ticketTitle}" wirklich schließen?</p>
              <div className="form-group">
                <label htmlFor="closeComment">Schließungskommentar (erforderlich):</label>
                <textarea
                  id="closeComment"
                  value={closeComment}
                  onChange={(e) => setCloseComment(e.target.value)}
                  placeholder="Bitte geben Sie einen Kommentar zum Schließen des Tickets ein..."
                  rows="4"
                  className="comment-textarea"
                />
              </div>
              <div className="modal-actions">
                <button 
                  onClick={handleCloseModalCancel}
                  className="btn btn-secondary"
                >
                  Abbrechen
                </button>
                <button 
                  onClick={handleCloseModalConfirm}
                  className="btn btn-danger"
                  disabled={!closeComment.trim()}
                >
                  Ticket schließen
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default TicketDetailsReadOnlyPage;
