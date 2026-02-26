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
      newErrors.currentPassword = 'Current password is required';
    }

    // New password validation
    if (!formData.newPassword) {
      newErrors.newPassword = 'New password is required';
    } else if (formData.newPassword.length < 8) {
      newErrors.newPassword = 'Password must be at least 8 characters';
    }

    // Confirm password validation
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your new password';
    } else if (formData.newPassword !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
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
      setMessage('You must be logged in to change your password.');
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
        setMessage(data.message || 'Password changed successfully!');
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
          setMessage(data.message || 'Please fix the validation errors below.');
        } else if (response.status === 401) {
          setMessage(data.message || 'Invalid current password or authentication failed.');
          // If token is invalid, redirect to login
          if (data.message && data.message.includes('authentication')) {
            localStorage.removeItem('token');
            window.dispatchEvent(new Event('authStateChange'));
            navigate('/login');
          }
        } else if (response.status === 500) {
          setMessage(data.message || 'Server error occurred. Please try again later.');
        } else {
          setMessage(data.message || 'Password change failed. Please try again.');
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
          <h1>Change Password</h1>

          {message && (
            <div className={`message ${isSuccess ? 'success' : 'error'}`}>
              {message}
              {isSuccess && (
                <p className="success-note">
                  Your password has been updated successfully.
                </p>
              )}
            </div>
          )}

          <form onSubmit={handleSubmit} className="form">
            <div className="form-group">
              <label htmlFor="currentPassword">Current Password</label>
              <input
                type="password"
                id="currentPassword"
                name="currentPassword"
                value={formData.currentPassword}
                onChange={handleChange}
                className={errors.currentPassword ? 'error' : ''}
                disabled={isLoading}
                placeholder="Enter your current password"
              />
              {errors.currentPassword && <span className="error-text">{errors.currentPassword}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="newPassword">New Password</label>
              <input
                type="password"
                id="newPassword"
                name="newPassword"
                value={formData.newPassword}
                onChange={handleChange}
                className={errors.newPassword ? 'error' : ''}
                disabled={isLoading}
                placeholder="Enter your new password"
              />
              {errors.newPassword && <span className="error-text">{errors.newPassword}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword">Confirm New Password</label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                className={errors.confirmPassword ? 'error' : ''}
                disabled={isLoading}
                placeholder="Confirm your new password"
              />
              {errors.confirmPassword && <span className="error-text">{errors.confirmPassword}</span>}
            </div>

            <button 
              type="submit" 
              className="btn btn-primary btn-full"
              disabled={isLoading}
            >
              {isLoading ? 'Changing Password...' : 'Change Password'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

export default ChangePasswordPage;
