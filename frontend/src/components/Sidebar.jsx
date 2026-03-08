import { NavLink, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { isLoggedIn, getUsername, getCurrentUser, logout } from '../utils/auth';
import { 
  Home, 
  Headphones, 
  List, 
  Users, 
  LogIn, 
  UserPlus, 
  Ticket, 
  Plus, 
  Key, 
  LogOut,
  Ticket as TicketIcon,
  BookOpen
} from 'lucide-react';
import './Sidebar.css';

function Sidebar() {
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
    <aside className="sidebar">
      <div className="sidebar-header">
        <NavLink to="/" className="sidebar-brand">
          <div className="brand-icon">
            <TicketIcon size={20} />
          </div>
          <span className="brand-text">TicketSystem</span>
        </NavLink>
      </div>

      <nav className="sidebar-nav">
        <NavLink 
          to="/" 
          className={({ isActive }) => `sidebar-nav-link ${isActive ? 'active' : ''}`}
        >
          <span className="nav-icon">
            <Home size={18} />
          </span>
          <span className="nav-text">Startseite</span>
        </NavLink>

        <NavLink 
          to="/wiki" 
          className={({ isActive }) => `sidebar-nav-link ${isActive ? 'active' : ''}`}
        >
          <span className="nav-icon">
            <BookOpen size={18} />
          </span>
          <span className="nav-text">Hilfe & Erklärung</span>
        </NavLink>

        {!userLoggedIn && (
          <>
            <NavLink 
              to="/login" 
              className={({ isActive }) => `sidebar-nav-link ${isActive ? 'active' : ''}`}
            >
              <span className="nav-icon">
                <LogIn size={18} />
              </span>
              <span className="nav-text">Anmelden</span>
            </NavLink>
            <NavLink 
              to="/register" 
              className={({ isActive }) => `sidebar-nav-link ${isActive ? 'active' : ''}`}
            >
              <span className="nav-icon">
                <UserPlus size={18} />
              </span>
              <span className="nav-text">Registrieren</span>
            </NavLink>
          </>
        )}

        {userLoggedIn && userRole === 'ENDUSER' && (
          <>
            <NavLink 
              to="/tickets" 
              className={({ isActive }) => `sidebar-nav-link ${isActive ? 'active' : ''}`}
            >
              <span className="nav-icon">
                <Ticket size={18} />
              </span>
              <span className="nav-text">Tickets</span>
            </NavLink>
            <NavLink 
              to="/tickets/new" 
              className={({ isActive }) => `sidebar-nav-link ${isActive ? 'active' : ''}`}
            >
              <span className="nav-icon">
                <Plus size={18} />
              </span>
              <span className="nav-text">Ticket erstellen</span>
            </NavLink>
          </>
        )}

        {userLoggedIn && (userRole === 'SUPPORTUSER' || userRole === 'ADMINUSER') && (
          <NavLink 
            to="/tickets" 
            className={({ isActive }) => `sidebar-nav-link ${isActive ? 'active' : ''}`}
          >
            <span className="nav-icon">
              <Ticket size={18} />
            </span>
            <span className="nav-text">Tickets</span>
          </NavLink>
        )}

        {userLoggedIn && userRole === 'ADMINUSER' && (
          <NavLink 
            to="/admin/users" 
            className={({ isActive }) => `sidebar-nav-link ${isActive ? 'active' : ''}`}
          >
            <span className="nav-icon">
              <Users size={18} />
            </span>
            <span className="nav-text">Benutzerverwaltung</span>
          </NavLink>
        )}
      </nav>

      {userLoggedIn && (
        <div className="sidebar-footer">
          <div className="account-dropdown-container">
            <div 
              className="user-info"
              onClick={toggleAccountDropdown}
              aria-expanded={showAccountDropdown}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  toggleAccountDropdown();
                }
              }}
            >
              <div className="user-avatar">
                {username ? username.charAt(0).toUpperCase() : 'U'}
              </div>
              <div className="user-details">
                <span className="username">{username}</span>
                <span className="user-role">
                  {userRole === 'ENDUSER' ? 'Benutzer' : 
                   userRole === 'SUPPORTUSER' ? 'Support' : 
                   userRole === 'ADMINUSER' ? 'Admin' : userRole}
                </span>
              </div>
            </div>

            {showAccountDropdown && (
              <div className="account-dropdown">
                <NavLink 
                  to="/change-password" 
                  className="dropdown-item"
                  onClick={() => setShowAccountDropdown(false)}
                >
                  <span className="dropdown-icon">
                    <Key size={16} />
                  </span>
                  Passwort ändern
                </NavLink>
                <button 
                  onClick={handleLogout} 
                  className="dropdown-item logout-item"
                >
                  <span className="dropdown-icon">
                    <LogOut size={16} />
                  </span>
                  Abmelden
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </aside>
  );
}

export default Sidebar;
