// ProtectedRoute.jsx

import React from 'react';
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('token'); // Adjust this line based on how you're storing the token

  if (!token) {
    // User is not logged in, redirect to LoginPage
    return <Navigate to="/" replace />;
  }

  // User is logged in, render the requested route
  return children;
};

export default ProtectedRoute;