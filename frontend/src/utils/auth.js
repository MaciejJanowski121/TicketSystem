// Auth utility functions for token management and JWT decoding

/**
 * Get token from localStorage
 */
export const getToken = () => {
  return localStorage.getItem('token');
};

/**
 * Set token in localStorage
 */
export const setToken = (token) => {
  localStorage.setItem('token', token);
  // Dispatch custom event to notify components of auth state change
  window.dispatchEvent(new Event('authStateChange'));
};

/**
 * Remove token from localStorage
 */
export const removeToken = () => {
  localStorage.removeItem('token');
  // Dispatch custom event to notify components of auth state change
  window.dispatchEvent(new Event('authStateChange'));
};

/**
 * Check if user is logged in
 */
export const isLoggedIn = () => {
  const token = getToken();
  if (!token) return false;
  
  try {
    const payload = decodeJWT(token);
    // Check if token is expired
    if (payload.exp && payload.exp < Date.now() / 1000) {
      removeToken();
      return false;
    }
    return true;
  } catch (error) {
    // Invalid token
    removeToken();
    return false;
  }
};

/**
 * Decode JWT token (simple base64 decode - for display purposes only)
 * Note: This is not for security validation, just for extracting user info
 */
export const decodeJWT = (token) => {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) {
      throw new Error('Invalid JWT format');
    }
    
    const payload = parts[1];
    // Add padding if needed
    const paddedPayload = payload + '='.repeat((4 - payload.length % 4) % 4);
    const decoded = atob(paddedPayload);
    return JSON.parse(decoded);
  } catch (error) {
    throw new Error('Failed to decode JWT');
  }
};

/**
 * Get current user info from token
 */
export const getCurrentUser = () => {
  const token = getToken();
  if (!token) return null;
  
  try {
    const payload = decodeJWT(token);
    return {
      username: payload.sub || payload.username,
      email: payload.email,
      role: payload.role,
      exp: payload.exp
    };
  } catch (error) {
    return null;
  }
};

/**
 * Get username from token
 */
export const getUsername = () => {
  const user = getCurrentUser();
  return user ? user.username : null;
};

/**
 * Logout user - remove token and redirect
 */
export const logout = (navigate) => {
  removeToken();
  if (navigate) {
    navigate('/');
  }
};

/**
 * Check if token is expired
 */
export const isTokenExpired = () => {
  const token = getToken();
  if (!token) return true;
  
  try {
    const payload = decodeJWT(token);
    if (payload.exp && payload.exp < Date.now() / 1000) {
      return true;
    }
    return false;
  } catch (error) {
    return true;
  }
};

/**
 * Get authorization header for API requests
 */
export const getAuthHeader = () => {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
};