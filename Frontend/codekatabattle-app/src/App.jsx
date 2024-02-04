import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'; // Use Routes instead of Switch for v6
import LoginPage from './LoginPage';
import HomePage from './HomePage';
import SignupPage from './SignupPage';
import './custom.scss';

// ... imports ...

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage/>} />
        <Route path="/home" element={<HomePage />} />
        {/* ... other routes ... */}
      </Routes>
    </Router>
  );
}

export default App;
