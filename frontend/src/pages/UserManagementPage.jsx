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
    { value: 'ENDUSER', label: 'End User' },
    { value: 'SUPPORTUSER', label: 'Support User' },
    { value: 'ADMINUSER', label: 'Admin User' }
  ];

  // Role display names
  const roleNames = {
    'ENDUSER': 'End User',
    'SUPPORTUSER': 'Support User',
    'ADMINUSER': 'Admin User'
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
        setError(errorData.message || 'Failed to load users');
      }
    } catch (error) {
      setError('Network error. Please check your connection and try again.');
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
        setError(errorData.message || 'Failed to update user role');
      }
    } catch (error) {
      setError('Network error while updating user role.');
    } finally {
      setUpdateLoading(prev => ({ ...prev, [userMail]: false }));
    }
  };

  if (isLoading) {
    return (
      <div className="page">
        <div className="container">
          <h1>User Management</h1>
          <div className="loading-message">Loading users...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="container">
        <h1>User Management</h1>

        {error && (
          <div className="message error">
            {error}
          </div>
        )}

        {users.length === 0 ? (
          <div className="empty-state">
            <h3>No users found</h3>
            <p>No users are currently registered in the system.</p>
          </div>
        ) : (
          <div className="users-table-container">
            <table className="users-table">
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Email</th>
                  <th>Current Role</th>
                  <th>Actions</th>
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
                        <span className="update-loading">Updating...</span>
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