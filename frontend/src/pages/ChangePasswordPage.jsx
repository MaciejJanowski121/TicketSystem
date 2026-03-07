import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getToken, getAuthHeader } from '../utils/auth';
import './ChangePasswordPage.css';

function ChangePasswordPage() {
  const [formData, setFormData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [isSuccess, setIsSuccess] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    // Current password validation
    if (!formData.currentPassword) {
      newErrors.currentPassword = 'Aktuelles Passwort ist erforderlich';
    }

    // New password validation
    if (!formData.newPassword) {
      newErrors.newPassword = 'Neues Passwort ist erforderlich';
    } else if (formData.newPassword.length < 8) {
      newErrors.newPassword = 'Passwort muss mindestens 8 Zeichen lang sein';
    }

    // Confirm password validation
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Bitte bestätigen Sie Ihr neues Passwort';
    } else if (formData.newPassword !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwörter stimmen nicht überein';
    }

    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const formErrors = validateForm();
    if (Object.keys(formErrors).length > 0) {
      setErrors(formErrors);
      return;
    }

    // Check if user is logged in
    const token = getToken();
    if (!token) {
      setIsSuccess(false);
      setMessage('Sie müssen angemeldet sein, um Ihr Passwort zu ändern.');
      navigate('/login');
      return;
    }

    setIsLoading(true);
    setMessage('');
    setErrors({});

    try {
      const response = await fetch('http://localhost:8080/api/auth/change-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        },
        body: JSON.stringify({
          currentPassword: formData.currentPassword,
          newPassword: formData.newPassword,
          confirmPassword: formData.confirmPassword
        }),
      });

      const data = await response.json();

      if (response.ok) {
        setIsSuccess(true);
        setMessage(data.message || 'Passwort erfolgreich geändert!');
        setFormData({
          currentPassword: '',
          newPassword: '',
          confirmPassword: ''
        });
      } else {
        setIsSuccess(false);

        // Handle different error types
        if (response.status === 400 && data.errors) {
          // Validation errors - set field-specific errors
          setErrors(data.errors);
          setMessage(data.message || 'Bitte beheben Sie die Validierungsfehler unten.');
        } else if (response.status === 401) {
          setMessage(data.message || 'Ungültiges aktuelles Passwort oder Authentifizierung fehlgeschlagen.');
          // If token is invalid, redirect to login
          if (data.message && data.message.includes('authentication')) {
            localStorage.removeItem('token');
            window.dispatchEvent(new Event('authStateChange'));
            navigate('/login');
          }
        } else if (response.status === 500) {
          setMessage(data.message || 'Serverfehler aufgetreten. Bitte versuchen Sie es später erneut.');
        } else {
          setMessage(data.message || 'Passwort-Änderung fehlgeschlagen. Bitte versuchen Sie es erneut.');
        }
      }
    } catch (error) {
      setIsSuccess(false);
      setMessage('Netzwerkfehler. Bitte überprüfen Sie Ihre Verbindung und versuchen Sie es erneut.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="page-centered">
      <div className="container-narrow">
        <div className="card card-narrow">
          <h1>Passwort ändern</h1>

          {message && (
            <div className={`message ${isSuccess ? 'success' : 'error'}`}>
              {message}
              {isSuccess && (
                <p className="success-note">
                  Ihr Passwort wurde erfolgreich aktualisiert.
                </p>
              )}
            </div>
          )}

          <form onSubmit={handleSubmit} className="form">
            <div className="form-group">
              <label htmlFor="currentPassword">Aktuelles Passwort</label>
              <input
                type="password"
                id="currentPassword"
                name="currentPassword"
                value={formData.currentPassword}
                onChange={handleChange}
                className={errors.currentPassword ? 'error' : ''}
                disabled={isLoading}
                placeholder="Geben Sie Ihr aktuelles Passwort ein"
              />
              {errors.currentPassword && <span className="error-text">{errors.currentPassword}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="newPassword">Neues Passwort</label>
              <input
                type="password"
                id="newPassword"
                name="newPassword"
                value={formData.newPassword}
                onChange={handleChange}
                className={errors.newPassword ? 'error' : ''}
                disabled={isLoading}
                placeholder="Geben Sie Ihr neues Passwort ein"
              />
              {errors.newPassword && <span className="error-text">{errors.newPassword}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword">Neues Passwort bestätigen</label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                className={errors.confirmPassword ? 'error' : ''}
                disabled={isLoading}
                placeholder="Bestätigen Sie Ihr neues Passwort"
              />
              {errors.confirmPassword && <span className="error-text">{errors.confirmPassword}</span>}
            </div>

            <button 
              type="submit" 
              className="btn btn-primary btn-full"
              disabled={isLoading}
            >
              {isLoading ? 'Passwort wird geändert...' : 'Passwort ändern'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

export default ChangePasswordPage;
