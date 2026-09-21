import React from 'react';
import { useAuth } from '../context/AuthContext';
import './AuthLayout.css';

const AuthLayout = ({ children, title, subtitle }) => {
  const { error, notice } = useAuth();

  return (
    <div className="auth-layout">
      <div className="auth-panel auth-panel-left animate-slide-in-left">
        <div className="auth-card">
          <div className="auth-header">
            {title && <h1 className="auth-title">{title}</h1>}
            {subtitle && <p className="auth-subtitle">{subtitle}</p>}
          </div>
          
          {error && <div className="auth-message auth-error">{error}</div>}
          {notice && <div className="auth-message auth-notice">{notice}</div>}
          
          <div className="auth-content">
            {children}
          </div>
        </div>
      </div>
      <div className="auth-panel auth-panel-right animate-slide-in-right">
        <div className="auth-decorative">
          <div className="floating-shape shape-1"></div>
          <div className="floating-shape shape-2"></div>
          <div className="floating-shape shape-3"></div>
          <div className="brand-content">
            <h2 className="brand-name">Little Joys</h2>
            <p className="brand-tagline">Hương vị Matcha Đích thực</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AuthLayout;
