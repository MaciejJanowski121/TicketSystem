import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getToken, getAuthHeader, isLoggedIn } from '../utils/auth';
import './MyTicketsPage.css';

function MyTicketsPage() {
  const [tickets, setTickets] = useState([]);
  const [filteredTickets, setFilteredTickets] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  // Filter options
  const filterOptions = [
    { value: 'ALL', label: 'All Tickets' },
    { value: 'UNASSIGNED', label: 'Unassigned' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'CLOSED', label: 'Closed' }
  ];

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

    fetchTickets();
  }, [navigate]);

  useEffect(() => {
    // Apply filter when tickets or filter changes
    if (filter === 'ALL') {
      setFilteredTickets(tickets);
    } else {
      setFilteredTickets(tickets.filter(ticket => ticket.ticketState === filter));
    }
  }, [tickets, filter]);

  const fetchTickets = async () => {
    setIsLoading(true);
    setError('');

    try {
      const token = getToken();
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await fetch('http://localhost:8080/api/tickets/my', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        }
      });

      if (response.ok) {
        const data = await response.json();
        setTickets(data);
      } else if (response.status === 401) {
        // Token expired or invalid
        localStorage.removeItem('token');
        window.dispatchEvent(new Event('authStateChange'));
        navigate('/login');
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Failed to load tickets');
      }
    } catch (error) {
      setError('Network error. Please check your connection and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleFilterChange = (e) => {
    setFilter(e.target.value);
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
          <h1>My Tickets</h1>
          <div className="loading-message">Loading tickets...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="container">
        <h1>My Tickets</h1>

        {error && (
          <div className="message error">
            {error}
          </div>
        )}

        <div className="tickets-header">
          <div className="filter-section">
            <label htmlFor="statusFilter">Filter by Status:</label>
            <select
              id="statusFilter"
              value={filter}
              onChange={handleFilterChange}
              className="filter-select"
            >
              {filterOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div className="tickets-count">
            {filteredTickets.length} ticket{filteredTickets.length !== 1 ? 's' : ''}
          </div>
        </div>

        {filteredTickets.length === 0 ? (
          <div className="empty-state">
            <h3>No tickets found</h3>
            <p>
              {filter === 'ALL' 
                ? "You haven't created any tickets yet." 
                : `You have no tickets with status "${filterOptions.find(f => f.value === filter)?.label}".`
              }
            </p>
            <Link to="/tickets/new" className="btn btn-primary">
              Create Your First Ticket
            </Link>
          </div>
        ) : (
          <div className="tickets-grid">
            {filteredTickets.map(ticket => (
              <div key={ticket.ticketId} className="ticket-card">
                <div className="ticket-header">
                  <h3 className="ticket-title">{ticket.title}</h3>
                  <span className={`ticket-state ${getStateClass(ticket.ticketState)}`}>
                    {stateNames[ticket.ticketState] || ticket.ticketState}
                  </span>
                </div>
                
                <div className="ticket-meta">
                  <div className="ticket-category">
                    <strong>Category:</strong> {categoryNames[ticket.ticketCategory] || ticket.ticketCategory}
                  </div>
                  <div className="ticket-dates">
                    <div><strong>Created:</strong> {formatDate(ticket.createDate)}</div>
                    <div><strong>Updated:</strong> {formatDate(ticket.updateDate)}</div>
                  </div>
                  {ticket.assignedSupport && (
                    <div className="ticket-assigned">
                      <strong>Assigned to:</strong> {ticket.assignedSupport}
                    </div>
                  )}
                </div>

                <div className="ticket-actions">
                  <Link 
                    to={`/my-tickets/${ticket.ticketId}`} 
                    className="btn btn-outline btn-small"
                  >
                    View Details
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default MyTicketsPage;