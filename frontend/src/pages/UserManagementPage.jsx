import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getToken, getAuthHeader, isLoggedIn, getCurrentUser } from '../utils/auth';
import './UserManagementPage.css';

function UserManagementPage() {
  const [users, setUsers] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [updateLoading, setUpdateLoading] = useState({});
  const navigate = useNavigate();

  // Role options
  const roleOptions = [
    { value: 'ENDUSER', label: 'Endbenutzer' },
    { value: 'SUPPORTUSER', label: 'Support-Benutzer' },
    { value: 'ADMINUSER', label: 'Administrator' }
  ];

  // Role display names
  const roleNames = {
    'ENDUSER': 'Endbenutzer',
    'SUPPORTUSER': 'Support-Benutzer',
    'ADMINUSER': 'Administrator'
  };

  // Check authorization on component mount
  useEffect(() => {
    if (!isLoggedIn()) {
      navigate('/login');
      return;
    }

    const currentUser = getCurrentUser();
    if (!currentUser || currentUser.role !== 'ADMINUSER') {
      navigate('/');
      return;
    }

    fetchUsers();
  }, [navigate]);

  const fetchUsers = async () => {
    setIsLoading(true);
    setError('');

    try {
      const token = getToken();
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await fetch('http://localhost:8080/api/admin/users', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        }
      });

      if (response.ok) {
        const data = await response.json();
        setUsers(data);
      } else if (response.status === 401) {
        localStorage.removeItem('token');
        window.dispatchEvent(new Event('authStateChange'));
        navigate('/login');
      } else if (response.status === 403) {
        navigate('/');
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Fehler beim Laden der Benutzer');
      }
    } catch (error) {
      setError('Netzwerkfehler. Bitte überprüfen Sie Ihre Verbindung und versuchen Sie es erneut.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleRoleChange = async (userMail, newRole) => {
    setUpdateLoading(prev => ({ ...prev, [userMail]: true }));
    setError('');

    try {
      const token = getToken();
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await fetch(`http://localhost:8080/api/admin/users/${encodeURIComponent(userMail)}/role`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        },
        body: JSON.stringify({ role: newRole })
      });

      if (response.ok) {
        // Refresh the user list
        fetchUsers();
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Fehler beim Aktualisieren der Benutzerrolle');
      }
    } catch (error) {
      setError('Netzwerkfehler beim Aktualisieren der Benutzerrolle.');
    } finally {
      setUpdateLoading(prev => ({ ...prev, [userMail]: false }));
    }
  };

  if (isLoading) {
    return (
      <div className="page">
        <div className="tickets-page-container">
          <h1>Benutzerverwaltung</h1>
          <div className="loading-message">Benutzer werden geladen...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="tickets-page-container">
        <h1>Benutzerverwaltung</h1>

        {error && (
          <div className="message error">
            {error}
          </div>
        )}

        {users.length === 0 ? (
          <div className="empty-state">
            <h3>Keine Benutzer gefunden</h3>
            <p>Derzeit sind keine Benutzer im System registriert.</p>
          </div>
        ) : (
          <div className="users-table-container">
            <table className="users-table">
              <thead>
                <tr>
                  <th>Benutzername</th>
                  <th>E-Mail</th>
                  <th>Aktuelle Rolle</th>
                  <th>Aktionen</th>
                </tr>
              </thead>
              <tbody>
                {users.map(user => (
                  <tr key={user.mail}>
                    <td className="user-username-cell">
                      {user.username}
                    </td>
                    <td className="user-email-cell">
                      {user.mail}
                    </td>
                    <td className="user-role-cell">
                      <span className={`role-badge role-${user.role.toLowerCase()}`}>
                        {roleNames[user.role] || user.role}
                      </span>
                    </td>
                    <td className="user-actions-cell">
                      <select
                        className="role-select"
                        value={user.role}
                        onChange={(e) => {
                          if (e.target.value !== user.role) {
                            handleRoleChange(user.mail, e.target.value);
                          }
                        }}
                        disabled={updateLoading[user.mail]}
                      >
                        {roleOptions.map(option => (
                          <option key={option.value} value={option.value}>
                            {option.label}
                          </option>
                        ))}
                      </select>
                      {updateLoading[user.mail] && (
                        <span className="update-loading">Wird aktualisiert...</span>
                      )}
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

export default UserManagementPage;
