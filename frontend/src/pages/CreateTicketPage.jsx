import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getToken, getAuthHeader, getCurrentUser } from '../utils/auth';
import { API_BASE_URL } from '../utils/config';
import './CreateTicketPage.css';

function CreateTicketPage() {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    ticketCategory: ''
  });
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [isSuccess, setIsSuccess] = useState(false);
  const navigate = useNavigate();

  // Category options with enum values and German labels
  const categoryOptions = [
    { value: 'ACCOUNT_MANAGEMENT', label: 'Konto-Management' },
    { value: 'HARDWARE', label: 'Hardware' },
    { value: 'PROGRAMS_TOOLS', label: 'Programme und Tools' },
    { value: 'NETWORK', label: 'Netzwerk' },
    { value: 'OTHER', label: 'Sonstiges' }
  ];

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

    // Title validation
    if (!formData.title.trim()) {
      newErrors.title = 'Titel ist erforderlich';
    }

    // Description validation
    if (!formData.description.trim()) {
      newErrors.description = 'Beschreibung ist erforderlich';
    }

    // Category validation
    if (!formData.ticketCategory) {
      newErrors.ticketCategory = 'Kategorie ist erforderlich';
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
      setMessage('Sie müssen angemeldet sein, um Tickets zu erstellen.');
      navigate('/login');
      return;
    }

    // Check if user has ENDUSER role
    const currentUser = getCurrentUser();
    if (!currentUser || currentUser.role !== 'ENDUSER') {
      setIsSuccess(false);
      setMessage('Sie sind nicht berechtigt, Tickets zu erstellen.');
      return;
    }

    setIsLoading(true);
    setMessage('');
    setErrors({});

    try {
      const response = await fetch(`${API_BASE_URL}/api/tickets`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        },
        body: JSON.stringify({
          title: formData.title,
          description: formData.description,
          ticketCategory: formData.ticketCategory
        }),
      });

      const data = await response.json();

      if (response.ok) {
        setIsSuccess(true);
        setMessage('Ticket erfolgreich erstellt!');
        setFormData({
          title: '',
          description: '',
          ticketCategory: ''
        });

        // Navigate to the newly created ticket details page
        setTimeout(() => {
          navigate(`/my-tickets/${data.ticketId}`);
        }, 2000);
      } else {
        setIsSuccess(false);

        // Handle different error types
        if (response.status === 400 && data.errors) {
          // Validation errors - set field-specific errors
          setErrors(data.errors);
          setMessage(data.message || 'Bitte beheben Sie die Validierungsfehler unten.');
        } else if (response.status === 401 || response.status === 403) {
          setMessage('Sie sind nicht berechtigt, Tickets zu erstellen.');
          // If token is invalid, redirect to login
          if (response.status === 401) {
            localStorage.removeItem('token');
            window.dispatchEvent(new Event('authStateChange'));
            navigate('/login');
          }
        } else if (response.status === 500) {
          setMessage('Serverfehler aufgetreten. Bitte versuchen Sie es später erneut.');
        } else {
          setMessage(data.message || 'Ticket-Erstellung fehlgeschlagen. Bitte versuchen Sie es erneut.');
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
    <div className="page-dashboard">
      <div className="content-section">
        <div className="page-header">
          <h1>Ticket erstellen</h1>
          <p>Erstellen Sie ein neues Support-Ticket für Ihr Anliegen</p>
        </div>

        <div className="card">
          {message && (
            <div className={`message ${isSuccess ? 'success' : 'error'}`}>
              {message}
              {isSuccess && (
                <p className="success-note">
                  Ihr Ticket wurde erfolgreich eingereicht. Sie werden zu den Ticket-Details weitergeleitet.
                </p>
              )}
            </div>
          )}

          <form onSubmit={handleSubmit} className="form create-ticket-form">
            <div className="form-group">
              <label htmlFor="title">Titel</label>
              <input
                type="text"
                id="title"
                name="title"
                value={formData.title}
                onChange={handleChange}
                className={errors.title ? 'error' : ''}
                disabled={isLoading}
                placeholder="Titel eingeben"
              />
              {errors.title && <span className="error-text">{errors.title}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="ticketCategory">Kategorie</label>
              <select
                id="ticketCategory"
                name="ticketCategory"
                value={formData.ticketCategory}
                onChange={handleChange}
                className={errors.ticketCategory ? 'error' : ''}
                disabled={isLoading}
              >
                <option value="">Kategorie auswählen</option>
                {categoryOptions.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
              {errors.ticketCategory && <span className="error-text">{errors.ticketCategory}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="description">Beschreibung</label>
              <textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleChange}
                className={errors.description ? 'error' : ''}
                disabled={isLoading}
                placeholder="Beschreiben Sie Ihr Problem im Detail"
                rows="5"
              />
              {errors.description && <span className="error-text">{errors.description}</span>}
            </div>

            <button 
              type="submit" 
              className="btn btn-primary btn-full"
              disabled={isLoading}
            >
              {isLoading ? 'Ticket wird erstellt...' : 'Ticket erstellen'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

export default CreateTicketPage;
