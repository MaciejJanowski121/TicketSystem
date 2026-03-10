import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getToken, getAuthHeader, isLoggedIn, getCurrentUser } from '../utils/auth';
import './MyTicketsPage.css';

function EndUserTicketsPage() {
  const [activeTab, setActiveTab] = useState('meine');
  const [myTickets, setMyTickets] = useState([]);
  const [allTickets, setAllTickets] = useState([]);
  const [filteredMyTickets, setFilteredMyTickets] = useState([]);
  const [filteredAllTickets, setFilteredAllTickets] = useState([]);
  const [myTicketsFilter, setMyTicketsFilter] = useState('ALL');
  const [allTicketsStatusFilter, setAllTicketsStatusFilter] = useState('All');
  const [allTicketsCategoryFilter, setAllTicketsCategoryFilter] = useState('All');
  const [allTicketsSearchTerm, setAllTicketsSearchTerm] = useState('');
  const [allTicketsSortField, setAllTicketsSortField] = useState('updateDate');
  const [allTicketsSortDirection, setAllTicketsSortDirection] = useState('DESC');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  // Filter options for My Tickets
  const myTicketsFilterOptions = [
    { value: 'ALL', label: 'Alle Tickets' },
    { value: 'UNASSIGNED', label: 'Nicht zugeordnet' },
    { value: 'IN_PROGRESS', label: 'In Bearbeitung' },
    { value: 'CLOSED', label: 'Abgeschlossen' }
  ];

  // Filter options for All Tickets
  const statusOptions = [
    { value: 'All', label: 'Alle' },
    { value: 'UNASSIGNED', label: 'Nicht zugeordnet' },
    { value: 'IN_PROGRESS', label: 'In Bearbeitung' },
    { value: 'CLOSED', label: 'Abgeschlossen' }
  ];

  const categoryOptions = [
    { value: 'All', label: 'Alle' },
    { value: 'ACCOUNT_MANAGEMENT', label: 'Konto-Management' },
    { value: 'HARDWARE', label: 'Hardware' },
    { value: 'PROGRAMS_TOOLS', label: 'Programme und Tools' },
    { value: 'NETWORK', label: 'Netzwerk' },
    { value: 'OTHER', label: 'Sonstiges' }
  ];

  const sortFieldOptions = [
    { value: 'updateDate', label: 'Zuletzt aktualisiert' },
    { value: 'createDate', label: 'Erstellungsdatum' }
  ];

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

  useEffect(() => {
    // Check if user is logged in and is ENDUSER
    if (!isLoggedIn()) {
      navigate('/login');
      return;
    }

    const currentUser = getCurrentUser();
    if (currentUser?.role !== 'ENDUSER') {
      navigate('/');
      return;
    }

    fetchMyTickets();
  }, [navigate]);

  useEffect(() => {
    // Fetch all tickets when switching to "alle" tab
    if (activeTab === 'alle') {
      fetchAllTickets();
    }
  }, [activeTab, allTicketsStatusFilter, allTicketsCategoryFilter, allTicketsSearchTerm, allTicketsSortField, allTicketsSortDirection]);

  useEffect(() => {
    // Apply filter for My Tickets
    let filtered;
    if (myTicketsFilter === 'ALL') {
      filtered = myTickets;
    } else {
      filtered = myTickets.filter(ticket => ticket.ticketState === myTicketsFilter);
    }

    // Sort tickets: unread first, then by updateDate descending
    filtered.sort((a, b) => {
      if (a.unread !== b.unread) {
        return a.unread ? -1 : 1;
      }
      return new Date(b.updateDate) - new Date(a.updateDate);
    });

    setFilteredMyTickets(filtered);
  }, [myTickets, myTicketsFilter]);

  useEffect(() => {
    // All tickets are already filtered on the server side
    setFilteredAllTickets(allTickets);
  }, [allTickets]);

  const fetchMyTickets = async () => {
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
        setMyTickets(data);
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

  const fetchAllTickets = async () => {
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
      if (allTicketsStatusFilter !== 'All') {
        params.append('state', allTicketsStatusFilter);
      }
      if (allTicketsCategoryFilter !== 'All') {
        params.append('category', allTicketsCategoryFilter);
      }
      if (allTicketsSearchTerm.trim()) {
        params.append('search', allTicketsSearchTerm.trim());
      }
      params.append('sort', allTicketsSortField);
      params.append('direction', allTicketsSortDirection);

      const url = `http://localhost:8080/api/tickets${params.toString() ? '?' + params.toString() : ''}`;

      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        }
      });

      if (response.ok) {
        const data = await response.json();
        setAllTickets(data);
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
    if (activeTab === 'meine') {
      navigate(`/my-tickets/${ticketId}`);
      // Refresh the ticket list after a short delay to reflect lastViewed update
      setTimeout(() => {
        fetchMyTickets();
      }, 100);
    } else {
      navigate(`/tickets/${ticketId}`);
    }
  };

  if (isLoading) {
    return (
      <div className="page">
        <div className="tickets-page-container">
          <h1>Tickets</h1>
          <div className="loading-message">Tickets werden geladen...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="tickets-page-container">
        <h1>Tickets</h1>

        {error && (
          <div className="message error">
            {error}
          </div>
        )}

        {/* Tab Navigation */}
        <div className="tab-navigation">
          <button 
            className={`tab-button ${activeTab === 'meine' ? 'active' : ''}`}
            onClick={() => setActiveTab('meine')}
          >
            Meine Tickets
          </button>
          <button 
            className={`tab-button ${activeTab === 'alle' ? 'active' : ''}`}
            onClick={() => setActiveTab('alle')}
          >
            Alle Tickets
          </button>
        </div>

        {/* My Tickets Tab Content */}
        {activeTab === 'meine' && (
          <>
            <div className="tickets-header">
              <div className="filter-section">
                <label htmlFor="myTicketsStatusFilter">Nach Status filtern:</label>
                <select
                  id="myTicketsStatusFilter"
                  value={myTicketsFilter}
                  onChange={(e) => setMyTicketsFilter(e.target.value)}
                  className="filter-select"
                >
                  {myTicketsFilterOptions.map(option => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>
              <div className="tickets-count">
                {filteredMyTickets.length} Ticket{filteredMyTickets.length !== 1 ? 's' : ''}
              </div>
            </div>

            {filteredMyTickets.length === 0 ? (
              <div className="empty-state">
                <h3>Keine Tickets gefunden</h3>
                <p>
                  {myTicketsFilter === 'ALL' 
                    ? "Sie haben noch keine Tickets erstellt." 
                    : `Keine Tickets mit diesem Status vorhanden.`
                  }
                </p>
                {myTicketsFilter === 'ALL' && (
                  <Link to="/tickets/new" className="btn btn-primary">
                    Erstes Ticket erstellen
                  </Link>
                )}
              </div>
            ) : (
              <div className="tickets-grid">
                {filteredMyTickets.map(ticket => (
                  <div key={ticket.ticketId} className={`ticket-card ${ticket.unread ? 'unread' : ''}`}>
                    <div className="ticket-header">
                      <h3 className="ticket-title">
                        {ticket.title}
                        {ticket.unread && <span className="unread-badge">NEU</span>}
                      </h3>
                      <span className={`ticket-state ${getStateClass(ticket.ticketState)}`}>
                        {stateNames[ticket.ticketState] || ticket.ticketState}
                      </span>
                    </div>

                    <div className="ticket-meta">
                      <div className="ticket-category">
                        <strong>Kategorie:</strong> {categoryNames[ticket.ticketCategory] || ticket.ticketCategory}
                      </div>
                      <div className="ticket-dates">
                        <div><strong>Erstellt:</strong> {formatDate(ticket.createDate)}</div>
                        <div><strong>Zuletzt geändert:</strong> {formatDate(ticket.updateDate)}</div>
                      </div>
                      {ticket.assignedSupport && (
                        <div className="ticket-assigned">
                          <strong>Zugewiesen an:</strong> {ticket.assignedSupport}
                        </div>
                      )}
                    </div>

                    <div className="ticket-actions">
                      <button 
                        onClick={() => handleTicketClick(ticket.ticketId)}
                        className="btn btn-outline btn-small"
                      >
                        Details anzeigen
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </>
        )}

        {/* All Tickets Tab Content */}
        {activeTab === 'alle' && (
          <>
            <div className="filters-section">
              <div className="filter-group">
                <label htmlFor="allTicketsSearchInput">Suchen:</label>
                <input
                  id="allTicketsSearchInput"
                  type="text"
                  value={allTicketsSearchTerm}
                  onChange={(e) => setAllTicketsSearchTerm(e.target.value)}
                  placeholder="Tickets, Ersteller oder zugewiesenen Support suchen..."
                  className="filter-input"
                />
              </div>

              <div className="filter-group">
                <label htmlFor="allTicketsStatusFilter">Status:</label>
                <select
                  id="allTicketsStatusFilter"
                  value={allTicketsStatusFilter}
                  onChange={(e) => setAllTicketsStatusFilter(e.target.value)}
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
                <label htmlFor="allTicketsCategoryFilter">Kategorie:</label>
                <select
                  id="allTicketsCategoryFilter"
                  value={allTicketsCategoryFilter}
                  onChange={(e) => setAllTicketsCategoryFilter(e.target.value)}
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
                <label htmlFor="allTicketsSortField">Sortieren nach:</label>
                <select
                  id="allTicketsSortField"
                  value={allTicketsSortField}
                  onChange={(e) => setAllTicketsSortField(e.target.value)}
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
                <label htmlFor="allTicketsSortDirection">Reihenfolge:</label>
                <select
                  id="allTicketsSortDirection"
                  value={allTicketsSortDirection}
                  onChange={(e) => setAllTicketsSortDirection(e.target.value)}
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

            {filteredAllTickets.length === 0 ? (
              <div className="empty-state">
                <h3>Keine Tickets gefunden</h3>
                <p>Keine Tickets entsprechen den aktuellen Filterkriterien.</p>
              </div>
            ) : (
              <div className="tickets-table-container">
                <table className="tickets-table">
                  <thead>
                    <tr>
                      <th>Titel</th>
                      <th>Ersteller</th>
                      <th>Status</th>
                      <th>Kategorie</th>
                      <th>{allTicketsSortField === 'createDate' ? 'Erstellt' : 'Aktualisiert'}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredAllTickets.map(ticket => (
                      <tr key={ticket.ticketId}>
                        <td className="ticket-title-cell">
                          <span 
                            className="ticket-title clickable" 
                            onClick={() => handleTicketClick(ticket.ticketId)}
                          >
                            {ticket.title}
                          </span>
                        </td>
                        <td className="ticket-creator-cell">
                          {ticket.creatorUsername || 'Unbekannt'}
                        </td>
                        <td className="ticket-status-cell">
                          <span className={`ticket-state ${getStateClass(ticket.ticketState)}`}>
                            {stateNames[ticket.ticketState] || ticket.ticketState}
                          </span>
                        </td>
                        <td className="ticket-category-cell">
                          {categoryNames[ticket.ticketCategory] || ticket.ticketCategory}
                        </td>
                        <td className="ticket-date-cell">
                          {formatDate(allTicketsSortField === 'createDate' ? ticket.createDate : ticket.updateDate)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default EndUserTicketsPage;