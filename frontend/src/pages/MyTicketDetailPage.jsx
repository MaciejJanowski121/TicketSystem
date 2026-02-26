import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getToken, getAuthHeader, isLoggedIn } from '../utils/auth';
import './MyTicketDetailPage.css';

function MyTicketDetailPage() {
  const { ticketId } = useParams();
  const [ticket, setTicket] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
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
              <div className="comments-placeholder">
                <p>Comments will be implemented in the next step.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default MyTicketDetailPage;