import { NavLink, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { isLoggedIn, getUsername, getCurrentUser, logout } from '../utils/auth';
import './Navbar.css';

function Navbar() {
  const [userLoggedIn, setUserLoggedIn] = useState(false);
  const [username, setUsername] = useState('');
  const [userRole, setUserRole] = useState('');
  const [showAccountDropdown, setShowAccountDropdown] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    // Check authentication status and get username and role
    const checkAuthStatus = () => {
      const loggedIn = isLoggedIn();
      setUserLoggedIn(loggedIn);

      if (loggedIn) {
        const currentUser = getCurrentUser();
        setUsername(currentUser?.username || '');
        setUserRole(currentUser?.role || '');
      } else {
        setUsername('');
        setUserRole('');
      }
    };

    // Initial check
    checkAuthStatus();

    // Listen for auth state changes
    const handleAuthStateChange = () => {
      checkAuthStatus();
      setShowAccountDropdown(false); // Close dropdown on auth change
    };

    window.addEventListener('storage', handleAuthStateChange);
    window.addEventListener('authStateChange', handleAuthStateChange);

    return () => {
      window.removeEventListener('storage', handleAuthStateChange);
      window.removeEventListener('authStateChange', handleAuthStateChange);
    };
  }, []);

  const handleLogout = () => {
    logout(navigate);
    setShowAccountDropdown(false);
  };

  const toggleAccountDropdown = () => {
    setShowAccountDropdown(!showAccountDropdown);
  };

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (!event.target.closest('.account-dropdown-container')) {
        setShowAccountDropdown(false);
      }
    };

    if (showAccountDropdown) {
      document.addEventListener('click', handleClickOutside);
    }

    return () => {
      document.removeEventListener('click', handleClickOutside);
    };
  }, [showAccountDropdown]);

  return (
    <nav className="navbar">
      <div className="navbar-container">
        {/* Left section - Brand */}
        <div className="navbar-left">
          <NavLink to="/" className="navbar-brand">
            TicketSystem
          </NavLink>
        </div>

        {/* Center section - Navigation Links */}
        <div className="navbar-center">
          <NavLink 
            to="/" 
            className={({ isActive }) => `navbar-nav-link ${isActive ? 'active' : ''}`}
          >
            Home
          </NavLink>
          {userLoggedIn && (
            <NavLink 
              to="/my-tickets" 
              className={({ isActive }) => `navbar-nav-link ${isActive ? 'active' : ''}`}
            >
              My Tickets
            </NavLink>
          )}
          {userLoggedIn && userRole === 'ENDUSER' && (
            <NavLink 
              to="/tickets/new" 
              className={({ isActive }) => `navbar-nav-link ${isActive ? 'active' : ''}`}
            >
              Create Ticket
            </NavLink>
          )}
          {/* Placeholder for future Tickets link */}
          {/* <NavLink 
            to="/tickets" 
            className={({ isActive }) => `navbar-nav-link ${isActive ? 'active' : ''}`}
          >
            Tickets
          </NavLink> */}
        </div>

        {/* Right section - Auth/Account Area */}
        <div className="navbar-right">
          {userLoggedIn ? (
            <div className="account-dropdown-container">
              <div className="user-info">
                {username && (
                  <span className="signed-in-text">
                    Signed in as <strong>{username}</strong>
                  </span>
                )}
                <button 
                  className="account-button"
                  onClick={toggleAccountDropdown}
                  aria-expanded={showAccountDropdown}
                >
                  Account
                  <span className={`dropdown-arrow ${showAccountDropdown ? 'open' : ''}`}>
                    ▼
                  </span>
                </button>
              </div>

              {showAccountDropdown && (
                <div className="account-dropdown">
                  <NavLink 
                    to="/change-password" 
                    className="dropdown-item"
                    onClick={() => setShowAccountDropdown(false)}
                  >
                    Change Password
                  </NavLink>
                  <button 
                    onClick={handleLogout} 
                    className="dropdown-item logout-item"
                  >
                    Logout
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="auth-links">
              <NavLink to="/login" className="navbar-auth-link">
                Login
              </NavLink>
              <NavLink to="/register" className="navbar-auth-link">
                Register
              </NavLink>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
