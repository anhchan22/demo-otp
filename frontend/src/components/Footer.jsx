import React from 'react';
import { Link } from 'react-router-dom';
import './Footer.css';

const Footer = () => {
  return (
    <footer id="contact" className="footer">
      <div className="footer-content">
        <div className="footer-col">
          <span className="footer-logo">Little Joys</span>
          <p className="footer-tagline">Little joys, everywhere you go</p>
          <p className="footer-desc">
            We believe the smallest details are the ones that matter most. 
            Turn an ordinary day into something worth remembering with our premium matcha milk teas.
          </p>
        </div>
        
        <div className="footer-col">
          <h3 className="footer-col-title">Khám phá</h3>
          <ul className="footer-links">
            <li><a href="#menu">Menu</a></li>
            <li><Link to="/register">Đăng ký</Link></li>
            <li><Link to="/login">Đăng nhập</Link></li>
            <li><a href="#about">Về chúng tôi</a></li>
          </ul>
        </div>
        
        <div className="footer-col">
          <h3 className="footer-col-title">Liên hệ</h3>
          <div className="footer-contact">
            <p>📍 123 Matcha Lane, Tea City</p>
            <p>✉️ hello@littlejoys.com</p>
            <p>📞 1900 123 456</p>
          </div>
        </div>
      </div>
      
      <div className="footer-bottom">
        <p>&copy; 2026 Little Joys. All rights reserved.</p>
      </div>
    </footer>
  );
};

export default Footer;
