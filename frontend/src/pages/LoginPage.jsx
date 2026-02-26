import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { setToken } from '../utils/auth';
import './LoginPage.css';

function LoginPage() {
  const [formData, setFormData] = useState({
    login: '',
    password: ''
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

    // Login validation
    if (!formData.login.trim()) {
      newErrors.login = 'Username or email is required';
    }

    // Password validation
    if (!formData.password) {
      newErrors.password = 'Password is required';
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

    setIsLoading(true);
    setMessage('');
    setErrors({});

    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          login: formData.login,
          password: formData.password
        }),
      });

      const data = await response.json();

      if (response.ok) {
        // Store JWT token using auth utility
        setToken(data.token);

        setIsSuccess(true);
        setMessage('Login successful!');

        // Redirect to home page after a brief delay
        setTimeout(() => {
          navigate('/');
        }, 1000);
      } else {
        setIsSuccess(false);

        // Handle different error types
        if (response.status === 400 && data.errors) {
          // Validation errors - set field-specific errors
          setErrors(data.errors);
          setMessage(data.message || 'Please fix the validation errors below.');
        } else if (response.status === 401) {
          setMessage(data.message || 'Invalid credentials. Please try again.');
        } else if (response.status === 409) {
          setMessage(data.message || 'Conflict occurred. Please try again.');
        } else if (response.status === 500) {
          setMessage(data.message || 'Server error occurred. Please try again later.');
        } else {
          setMessage(data.message || 'Login failed. Please try again.');
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
          <h1>Login</h1>

          {message && (
            <div className={`message ${isSuccess ? 'success' : 'error'}`}>
              {message}
            </div>
          )}

          <form onSubmit={handleSubmit} className="form">
            <div className="form-group">
              <label htmlFor="login">Username or Email</label>
              <input
                type="text"
                id="login"
                name="login"
                value={formData.login}
                onChange={handleChange}
                className={errors.login ? 'error' : ''}
                disabled={isLoading}
                placeholder="Enter your username or email"
              />
              {errors.login && <span className="error-text">{errors.login}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="password">Password</label>
              <input
                type="password"
                id="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className={errors.password ? 'error' : ''}
                disabled={isLoading}
                placeholder="Enter your password"
              />
              {errors.password && <span className="error-text">{errors.password}</span>}
            </div>

            <button 
              type="submit" 
              className="btn btn-primary btn-full"
              disabled={isLoading}
            >
              {isLoading ? 'Logging in...' : 'Login'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
