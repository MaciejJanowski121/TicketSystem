import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getToken, getAuthHeader, isLoggedIn, getCurrentUser } from '../utils/auth';
import './SupportTicketsPage.css';

function SupportTicketsPage() {
  const [activeTab, setActiveTab] = useState('all');
  const [tickets, setTickets] = useState([]);
  const [statusFilter, setStatusFilter] = useState('All');
  const [categoryFilter, setCategoryFilter] = useState('All');
  const [searchTerm, setSearchTerm] = useState('');
  const [sortField, setSortField] = useState('updateDate');
  const [sortDirection, setSortDirection] = useState('DESC');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionLoading, setActionLoading] = useState({});
  const [closeModal, setCloseModal] = useState({ show: false, ticketId: null, ticketTitle: '' });
  const [closeComment, setCloseComment] = useState('');
  const navigate = useNavigate();

  // Status filter options
  const statusOptions = [
    { value: 'All', label: 'Alle' },
    { value: 'UNASSIGNED', label: 'Nicht zugeordnet' },
    { value: 'IN_PROGRESS', label: 'In Bearbeitung' },
    { value: 'CLOSED', label: 'Abgeschlossen' }
  ];

  // Category filter options
  const categoryOptions = [
    { value: 'All', label: 'Alle' },
    { value: 'ACCOUNT_MANAGEMENT', label: 'Konto-Management' },
    { value: 'HARDWARE', label: 'Hardware' },
    { value: 'PROGRAMS_TOOLS', label: 'Programme und Tools' },
    { value: 'NETWORK', label: 'Netzwerk' },
    { value: 'OTHER', label: 'Sonstiges' }
  ];

  // Sort field options
  const sortFieldOptions = [
    { value: 'updateDate', label: 'Zuletzt aktualisiert' },
    { value: 'createDate', label: 'Erstellungsdatum' }
  ];

  // Sort direction options
  const sortDirectionOptions = [
    { value: 'DESC', label: 'Neueste zuerst' },
    { value: 'ASC', label: 'Älteste zuerst' }
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

  // Check authorization on component mount
  useEffect(() => {
    if (!isLoggedIn()) {
      navigate('/login');
      return;
    }

    const currentUser = getCurrentUser();
    if (!currentUser || (currentUser.role !== 'SUPPORTUSER' && currentUser.role !== 'ADMINUSER')) {
      navigate('/');
      return;
    }
  }, [navigate]);

  // Fetch tickets when filters change
  useEffect(() => {
    if (!isLoggedIn()) return;

    const timeoutId = setTimeout(() => {
      fetchTickets();
    }, 300);

    return () => clearTimeout(timeoutId);
  }, [activeTab, statusFilter, categoryFilter, searchTerm, sortField, sortDirection]);

  const fetchTickets = async () => {
    setIsLoading(true);
    setError('');

    try {
      const token = getToken();
      if (!token) {
        navigate('/login');
        return;
      }

      // Build query parameters
      const params = new URLSearchParams();
      if (statusFilter !== 'All') {
        params.append('state', statusFilter);
      }
      if (categoryFilter !== 'All') {
        params.append('category', categoryFilter);
      }
      if (searchTerm.trim()) {
        params.append('search', searchTerm.trim());
      }
      params.append('sort', sortField);
      params.append('direction', sortDirection);

      // Choose endpoint based on active tab
      const endpoint = activeTab === 'all' 
        ? '/api/tickets' 
        : '/api/support/tickets/my';

      const url = `http://localhost:8080${endpoint}${params.toString() ? '?' + params.toString() : ''}`;

      const response = await fetch(url, {
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
        localStorage.removeItem('token');
        window.dispatchEvent(new Event('authStateChange'));
        navigate('/login');
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Fehler beim Laden der Tickets');
      }
    } catch (error) {
      setError('Netzwerkfehler. Bitte überprüfen Sie Ihre Verbindung und versuchen Sie es erneut.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSupportAction = async (ticketId, action, comment = null) => {
    setActionLoading(prev => ({ ...prev, [`${ticketId}-${action}`]: true }));
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
        case 'update':
          method = 'PATCH';
          // This would need additional UI for status/category selection
          // For now, we'll implement basic actions
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
        // Refresh tickets after successful action
        fetchTickets();
      } else {
        const errorData = await response.json();
        setError(errorData.message || `Fehler beim ${action === 'assign' ? 'Zuweisen' : action === 'unassign' ? 'Aufheben der Zuweisung' : action} des Tickets`);
      }
    } catch (error) {
      setError(`Netzwerkfehler beim Versuch, das Ticket zu ${action === 'assign' ? 'zuweisen' : action === 'unassign' ? 'entziehen' : action}.`);
    } finally {
      setActionLoading(prev => ({ ...prev, [`${ticketId}-${action}`]: false }));
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

  const handleTicketClick = (ticketId) => {
    navigate(`/tickets/${ticketId}`);
    // Refresh the ticket list after a short delay to reflect lastViewed update
    setTimeout(() => {
      fetchTickets();
    }, 100);
  };

  const getCurrentUserEmail = () => {
    const currentUser = getCurrentUser();
    return currentUser?.email || '';
  };

  const canAssign = (ticket) => {
    return ticket.ticketState !== 'CLOSED' && !ticket.assignedSupportUsername;
  };

  const canRelease = (ticket) => {
    const currentUserEmail = getCurrentUserEmail();
    const currentUser = getCurrentUser();
    return ticket.ticketState !== 'CLOSED' && 
           ticket.assignedSupportUsername && 
           (ticket.assignedSupportUsername === currentUser?.username || currentUser?.role === 'ADMINUSER');
  };

  const canClose = (ticket) => {
    const currentUserEmail = getCurrentUserEmail();
    const currentUser = getCurrentUser();
    return ticket.ticketState !== 'CLOSED' && 
           (ticket.assignedSupportUsername === currentUser?.username || currentUser?.role === 'ADMINUSER');
  };

  const canUpdate = (ticket) => {
    const currentUser = getCurrentUser();
    return ticket.assignedSupportUsername === currentUser?.username || currentUser?.role === 'ADMINUSER';
  };

  const canDelete = (ticket) => {
    const currentUser = getCurrentUser();
    return currentUser?.role === 'ADMINUSER';
  };

  const handleCloseTicketClick = (ticket) => {
    setCloseModal({ 
      show: true, 
      ticketId: ticket.ticketId, 
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
      setError('Closing comment is required');
      return;
    }

    setCloseModal({ show: false, ticketId: null, ticketTitle: '' });
    await handleSupportAction(closeModal.ticketId, 'close', closeComment.trim());
    setCloseComment('');
  };

  const handleUpdateStatus = async (ticketId, newStatus) => {
    setActionLoading(prev => ({ ...prev, [`${ticketId}-update-status`]: true }));
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
        fetchTickets();
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Fehler beim Aktualisieren des Ticket-Status');
      }
    } catch (error) {
      setError('Netzwerkfehler beim Aktualisieren des Ticket-Status.');
    } finally {
      setActionLoading(prev => ({ ...prev, [`${ticketId}-update-status`]: false }));
    }
  };

  const handleUpdateCategory = async (ticketId, newCategory) => {
    setActionLoading(prev => ({ ...prev, [`${ticketId}-update-category`]: true }));
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
        fetchTickets();
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Fehler beim Aktualisieren der Ticket-Kategorie');
      }
    } catch (error) {
      setError('Netzwerkfehler beim Aktualisieren der Ticket-Kategorie.');
    } finally {
      setActionLoading(prev => ({ ...prev, [`${ticketId}-update-category`]: false }));
    }
  };

  const handleDeleteTicket = async (ticketId, ticketTitle) => {
    // Show confirmation dialog
    const confirmed = window.confirm(
      `Are you sure you want to delete the ticket "${ticketTitle}"?\n\nThis action cannot be undone and will permanently remove the ticket and all its comments.`
    );

    if (!confirmed) {
      return;
    }

    setActionLoading(prev => ({ ...prev, [`${ticketId}-delete`]: true }));
    setError('');

    try {
      const token = getToken();
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await fetch(`http://localhost:8080/api/admin/tickets/${ticketId}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        }
      });

      if (response.ok) {
        // Refresh tickets after successful deletion
        fetchTickets();
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Fehler beim Löschen des Tickets');
      }
    } catch (error) {
      setError('Network error while deleting ticket.');
    } finally {
      setActionLoading(prev => ({ ...prev, [`${ticketId}-delete`]: false }));
    }
  };

  if (isLoading) {
    return (
      <div className="page">
        <div className="tickets-page-container">
          <h1>Support-Bereich</h1>
          <div className="loading-message">Tickets werden geladen...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="tickets-page-container">
        <h1>Support-Bereich</h1>

        {error && (
          <div className="message error">
            {error}
          </div>
        )}

        {/* Tab Navigation */}
        <div className="tab-navigation">
          <button
            className={`tab-button ${activeTab === 'all' ? 'active' : ''}`}
            onClick={() => setActiveTab('all')}
          >
            Alle Tickets
          </button>
          <button
            className={`tab-button ${activeTab === 'my' ? 'active' : ''}`}
            onClick={() => setActiveTab('my')}
          >
            Meine Tickets
          </button>
        </div>

        {/* Filters Section */}
        <div className="filters-section">
          <div className="filter-group">
            <label htmlFor="searchInput">Suchen:</label>
            <input
              id="searchInput"
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Tickets, Ersteller oder zugewiesenen Support suchen..."
              className="filter-input"
            />
          </div>

          <div className="filter-group">
            <label htmlFor="statusFilter">Status:</label>
            <select
              id="statusFilter"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="filter-select"
            >
              {statusOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div className="filter-group">
            <label htmlFor="categoryFilter">Kategorie:</label>
            <select
              id="categoryFilter"
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
              className="filter-select"
            >
              {categoryOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div className="filter-group">
            <label htmlFor="sortField">Sortieren nach:</label>
            <select
              id="sortField"
              value={sortField}
              onChange={(e) => setSortField(e.target.value)}
              className="filter-select"
            >
              {sortFieldOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div className="filter-group">
            <label htmlFor="sortDirection">Reihenfolge:</label>
            <select
              id="sortDirection"
              value={sortDirection}
              onChange={(e) => setSortDirection(e.target.value)}
              className="filter-select"
            >
              {sortDirectionOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Tickets Table */}
        {tickets.length === 0 ? (
          <div className="empty-state">
            <h3>No tickets found</h3>
            <p>No tickets match the current filter criteria.</p>
          </div>
        ) : (
          <div className="tickets-table-container">
            <table className="tickets-table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Creator</th>
                  <th>Status</th>
                  <th>Category</th>
                  <th>Assigned Support</th>
                  <th>{sortField === 'createDate' ? 'Created' : 'Updated'}</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {tickets.map(ticket => (
                  <tr key={ticket.ticketId} className={ticket.unread ? 'unread-row' : ''}>
                    <td className="ticket-title-cell">
                      <span 
                        className="ticket-title clickable" 
                        onClick={() => handleTicketClick(ticket.ticketId)}
                      >
                        {ticket.title}
                        {ticket.unread && <span className="unread-badge">NEW</span>}
                      </span>
                    </td>
                    <td className="ticket-creator-cell">
                      {ticket.creatorUsername || 'Unknown'}
                    </td>
                    <td className="ticket-status-cell">
                      <span className={`ticket-state ${getStateClass(ticket.ticketState)}`}>
                        {stateNames[ticket.ticketState] || ticket.ticketState}
                      </span>
                    </td>
                    <td className="ticket-category-cell">
                      {categoryNames[ticket.ticketCategory] || ticket.ticketCategory}
                    </td>
                    <td className="ticket-assigned-cell">
                      {ticket.assignedSupportUsername || 'Unassigned'}
                    </td>
                    <td className="ticket-updated-cell">
                      {formatDate(sortField === 'createDate' ? ticket.createDate : ticket.updateDate)}
                    </td>
                    <td className="ticket-actions-cell">
                      <div className="action-buttons">
                        {canAssign(ticket) && (
                          <button
                            className="action-btn assign-btn"
                            onClick={() => handleSupportAction(ticket.ticketId, 'assign')}
                            disabled={actionLoading[`${ticket.ticketId}-assign`]}
                          >
                            {actionLoading[`${ticket.ticketId}-assign`] ? 'Assigning...' : 'Assign to me'}
                          </button>
                        )}
                        {canRelease(ticket) && (
                          <button
                            className="action-btn release-btn"
                            onClick={() => handleSupportAction(ticket.ticketId, 'release')}
                            disabled={actionLoading[`${ticket.ticketId}-release`]}
                          >
                            {actionLoading[`${ticket.ticketId}-release`] ? 'Releasing...' : 'Release'}
                          </button>
                        )}
                        {canClose(ticket) && (
                          <button
                            className="action-btn close-btn"
                            onClick={() => handleCloseTicketClick(ticket)}
                            disabled={actionLoading[`${ticket.ticketId}-close`]}
                          >
                            {actionLoading[`${ticket.ticketId}-close`] ? 'Closing...' : 'Close'}
                          </button>
                        )}
                        {canDelete(ticket) && (
                          <button
                            className="action-btn delete-btn"
                            onClick={() => handleDeleteTicket(ticket.ticketId, ticket.title)}
                            disabled={actionLoading[`${ticket.ticketId}-delete`]}
                          >
                            {actionLoading[`${ticket.ticketId}-delete`] ? 'Deleting...' : 'Delete'}
                          </button>
                        )}
                        {canUpdate(ticket) && (
                          <select
                            className="action-select update-status-select"
                            onChange={(e) => {
                              if (e.target.value) {
                                handleUpdateStatus(ticket.ticketId, e.target.value);
                                e.target.value = '';
                              }
                            }}
                            disabled={actionLoading[`${ticket.ticketId}-update-status`]}
                          >
                            <option value="">Update Status</option>
                            <option value="UNASSIGNED">Unassigned</option>
                            <option value="IN_PROGRESS">In Progress</option>
                            <option value="CLOSED">Closed</option>
                          </select>
                        )}
                        {canUpdate(ticket) && (
                          <select
                            className="action-select update-category-select"
                            onChange={(e) => {
                              if (e.target.value) {
                                handleUpdateCategory(ticket.ticketId, e.target.value);
                                e.target.value = '';
                              }
                            }}
                            disabled={actionLoading[`${ticket.ticketId}-update-category`]}
                          >
                            <option value="">Update Category</option>
                            <option value="ACCOUNT_MANAGEMENT">Account Management</option>
                            <option value="HARDWARE">Hardware</option>
                            <option value="PROGRAMS_TOOLS">Programs & Tools</option>
                            <option value="NETWORK">Network</option>
                            <option value="OTHER">Other</option>
                          </select>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Close Ticket Modal */}
      {closeModal.show && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Close Ticket</h3>
            <p>
              You are about to close ticket: <strong>{closeModal.ticketTitle}</strong>
            </p>
            <p>Bitte geben Sie einen Abschlusskommentar an (erforderlich):</p>
            <textarea
              value={closeComment}
              onChange={(e) => setCloseComment(e.target.value)}
              placeholder="Enter your closing comment here..."
              rows={4}
              className="close-comment-textarea"
            />
            <div className="modal-buttons">
              <button 
                className="modal-btn cancel-btn" 
                onClick={handleCloseModalCancel}
              >
                Cancel
              </button>
              <button 
                className="modal-btn confirm-btn" 
                onClick={handleCloseModalConfirm}
                disabled={!closeComment.trim()}
              >
                Close Ticket
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default SupportTicketsPage;
