import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getToken, getAuthHeader, isLoggedIn } from '../utils/auth';
import './TicketsOverviewPage.css';

function TicketsOverviewPage() {
  const [tickets, setTickets] = useState([]);
  const [statusFilter, setStatusFilter] = useState('All');
  const [categoryFilter, setCategoryFilter] = useState('All');
  const [searchTerm, setSearchTerm] = useState('');
  const [sortField, setSortField] = useState('updateDate');
  const [sortDirection, setSortDirection] = useState('DESC');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  // Status filter options
  const statusOptions = [
    { value: 'All', label: 'All' },
    { value: 'UNASSIGNED', label: 'Unassigned' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'CLOSED', label: 'Closed' }
  ];

  // Category filter options
  const categoryOptions = [
    { value: 'All', label: 'All' },
    { value: 'ACCOUNT_MANAGEMENT', label: 'Account Management' },
    { value: 'HARDWARE', label: 'Hardware' },
    { value: 'PROGRAMS_TOOLS', label: 'Programs & Tools' },
    { value: 'NETWORK', label: 'Network' },
    { value: 'OTHER', label: 'Other' }
  ];

  // Sort field options
  const sortFieldOptions = [
    { value: 'updateDate', label: 'Last Updated' },
    { value: 'createDate', label: 'Created Date' }
  ];

  // Sort direction options
  const sortDirectionOptions = [
    { value: 'DESC', label: 'Newest First' },
    { value: 'ASC', label: 'Oldest First' }
  ];

  // Category display names
  const categoryNames = {
    'ACCOUNT_MANAGEMENT': 'Account Management',
    'HARDWARE': 'Hardware',
    'PROGRAMS_TOOLS': 'Programs & Tools',
    'NETWORK': 'Network',
    'OTHER': 'Other'
  };

  // State display names
  const stateNames = {
    'UNASSIGNED': 'Unassigned',
    'IN_PROGRESS': 'In Progress',
    'CLOSED': 'Closed'
  };

  // Debounced search effect
  useEffect(() => {
    // Check if user is logged in
    if (!isLoggedIn()) {
      navigate('/login');
      return;
    }

    // Debounce search input
    const timeoutId = setTimeout(() => {
      fetchTickets();
    }, 300);

    return () => clearTimeout(timeoutId);
  }, [navigate, statusFilter, categoryFilter, searchTerm, sortField, sortDirection]);

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

  const handleStatusFilterChange = (e) => {
    setStatusFilter(e.target.value);
  };

  const handleCategoryFilterChange = (e) => {
    setCategoryFilter(e.target.value);
  };

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
  };

  const handleSortFieldChange = (e) => {
    setSortField(e.target.value);
  };

  const handleSortDirectionChange = (e) => {
    setSortDirection(e.target.value);
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
  };

  if (isLoading) {
    return (
      <div className="page">
        <div className="container">
          <h1>All Tickets</h1>
          <div className="loading-message">Loading tickets...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="container">
        <h1>All Tickets</h1>

        {error && (
          <div className="message error">
            {error}
          </div>
        )}

        <div className="filters-section">
          <div className="filter-group">
            <label htmlFor="searchInput">Search:</label>
            <input
              id="searchInput"
              type="text"
              value={searchTerm}
              onChange={handleSearchChange}
              placeholder="Search tickets, creators, or assigned support..."
              className="filter-input"
            />
          </div>

          <div className="filter-group">
            <label htmlFor="statusFilter">Status:</label>
            <select
              id="statusFilter"
              value={statusFilter}
              onChange={handleStatusFilterChange}
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
            <label htmlFor="categoryFilter">Category:</label>
            <select
              id="categoryFilter"
              value={categoryFilter}
              onChange={handleCategoryFilterChange}
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
            <label htmlFor="sortField">Sort by:</label>
            <select
              id="sortField"
              value={sortField}
              onChange={handleSortFieldChange}
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
            <label htmlFor="sortDirection">Order:</label>
            <select
              id="sortDirection"
              value={sortDirection}
              onChange={handleSortDirectionChange}
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
                  <th>{sortField === 'createDate' ? 'Created' : 'Updated'}</th>
                </tr>
              </thead>
              <tbody>
                {tickets.map(ticket => (
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
                    <td className="ticket-updated-cell">
                      {formatDate(sortField === 'createDate' ? ticket.createDate : ticket.updateDate)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

export default TicketsOverviewPage;
