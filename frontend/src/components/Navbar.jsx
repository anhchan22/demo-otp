import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const { currentUser, logout, loading } = useAuth();
  const [scrolled, setScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const navigate = useNavigate();
  const dropdownRef = useRef(null);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/');
    } catch (error) {
      console.error('Failed to log out', error);
    }
  };

  const toggleMobileMenu = () => {
    setMobileMenuOpen(!mobileMenuOpen);
  };

  const displayName = currentUser?.fullName || currentUser?.username || '';
  const initial = displayName ? displayName.charAt(0).toUpperCase() : 'U';

  return (
    <>
      <nav className={`navbar ${scrolled ? 'scrolled' : ''}`}>
        <Link to="/" className="navbar-logo">
          Little Joys 🍵
        </Link>
        
        <div className="navbar-links">
          <a href="/#menu" className="nav-link">Menu</a>
          <a href="/#about" className="nav-link">Về chúng tôi</a>
          <a href="/#contact" className="nav-link">Liên hệ</a>
        </div>

        <div className="navbar-actions">
          {currentUser ? (
            <div className="user-profile" ref={dropdownRef} onClick={() => setDropdownOpen(!dropdownOpen)}>
              <div className="avatar-circle">{initial}</div>
              <div className={`user-dropdown ${dropdownOpen ? 'open' : ''}`}>
                <div className="dropdown-header">
                  <div className="dropdown-avatar">{initial}</div>
                  <div className="dropdown-user-info">
                    <span className="dropdown-name">{displayName}</span>
                    <span className="dropdown-role">Thành viên</span>
                  </div>
                </div>
                <div className="dropdown-divider"></div>
                <button onClick={handleLogout} className="dropdown-item dropdown-logout">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                    <polyline points="16 17 21 12 16 7"/>
                    <line x1="21" y1="12" x2="9" y2="12"/>
                  </svg>
                  Đăng xuất
                </button>
              </div>
            </div>
          ) : (
            <>
              <Link to="/login" className="btn-login">Đăng nhập</Link>
              <Link to="/register" className="btn-register">Đăng ký</Link>
            </>
          )}
        </div>

        <button className="hamburger" onClick={toggleMobileMenu}>
          {mobileMenuOpen ? '✕' : '☰'}
        </button>
      </nav>

      <div className={`mobile-menu ${mobileMenuOpen ? 'open' : ''}`}>
        <a href="/#menu" className="nav-link" onClick={toggleMobileMenu}>Menu</a>
        <a href="/#about" className="nav-link" onClick={toggleMobileMenu}>Về chúng tôi</a>
        <a href="/#contact" className="nav-link" onClick={toggleMobileMenu}>Liên hệ</a>
        {currentUser ? (
          <>
            <div className="nav-link mobile-user-name">Xin chào, {displayName}</div>
            <button onClick={() => { handleLogout(); toggleMobileMenu(); }} className="btn-login">Đăng xuất</button>
          </>
        ) : (
          <>
            <Link to="/login" className="btn-login" onClick={toggleMobileMenu}>Đăng nhập</Link>
            <Link to="/register" className="btn-register" onClick={toggleMobileMenu}>Đăng ký</Link>
          </>
        )}
      </div>
    </>
  );
};

export default Navbar;
