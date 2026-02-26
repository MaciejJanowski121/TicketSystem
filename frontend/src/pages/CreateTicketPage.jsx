import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getToken, getAuthHeader, getCurrentUser } from '../utils/auth';
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
      newErrors.title = 'Title is required';
    }

    // Description validation
    if (!formData.description.trim()) {
      newErrors.description = 'Description is required';
    }

    // Category validation
    if (!formData.ticketCategory) {
      newErrors.ticketCategory = 'Category is required';
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
      setMessage('You must be logged in to create tickets.');
      navigate('/login');
      return;
    }

    // Check if user has ENDUSER role
    const currentUser = getCurrentUser();
    if (!currentUser || currentUser.role !== 'ENDUSER') {
      setIsSuccess(false);
      setMessage('You are not authorized to create tickets.');
      return;
    }

    setIsLoading(true);
    setMessage('');
    setErrors({});

    try {
      const response = await fetch('http://localhost:8080/api/tickets', {
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
        setMessage('Ticket created successfully!');
        setFormData({
          title: '',
          description: '',
          ticketCategory: ''
        });
        
        // Optionally navigate to home after success
        setTimeout(() => {
          navigate('/');
        }, 2000);
      } else {
        setIsSuccess(false);

        // Handle different error types
        if (response.status === 400 && data.errors) {
          // Validation errors - set field-specific errors
          setErrors(data.errors);
          setMessage(data.message || 'Please fix the validation errors below.');
        } else if (response.status === 401 || response.status === 403) {
          setMessage('You are not authorized to create tickets.');
          // If token is invalid, redirect to login
          if (response.status === 401) {
            localStorage.removeItem('token');
            window.dispatchEvent(new Event('authStateChange'));
            navigate('/login');
          }
        } else if (response.status === 500) {
          setMessage('Server error occurred. Please try again later.');
        } else {
          setMessage(data.message || 'Ticket creation failed. Please try again.');
        }
      }
    } catch (error) {
      setIsSuccess(false);
      setMessage('Network error. Please check your connection and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="page-centered">
      <div className="container-narrow">
        <div className="card card-narrow">
          <h1>Create Ticket</h1>

          {message && (
            <div className={`message ${isSuccess ? 'success' : 'error'}`}>
              {message}
              {isSuccess && (
                <p className="success-note">
                  Your ticket has been submitted successfully. You will be redirected to the home page.
                </p>
              )}
            </div>
          )}

          <form onSubmit={handleSubmit} className="form">
            <div className="form-group">
              <label htmlFor="title">Title</label>
              <input
                type="text"
                id="title"
                name="title"
                value={formData.title}
                onChange={handleChange}
                className={errors.title ? 'error' : ''}
                disabled={isLoading}
                placeholder="Enter ticket title"
              />
              {errors.title && <span className="error-text">{errors.title}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="ticketCategory">Category</label>
              <select
                id="ticketCategory"
                name="ticketCategory"
                value={formData.ticketCategory}
                onChange={handleChange}
                className={errors.ticketCategory ? 'error' : ''}
                disabled={isLoading}
              >
                <option value="">Select a category</option>
                {categoryOptions.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
              {errors.ticketCategory && <span className="error-text">{errors.ticketCategory}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="description">Description</label>
              <textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleChange}
                className={errors.description ? 'error' : ''}
                disabled={isLoading}
                placeholder="Describe your issue in detail"
                rows="5"
              />
              {errors.description && <span className="error-text">{errors.description}</span>}
            </div>

            <button 
              type="submit" 
              className="btn btn-primary btn-full"
              disabled={isLoading}
            >
              {isLoading ? 'Creating Ticket...' : 'Create Ticket'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

export default CreateTicketPage;