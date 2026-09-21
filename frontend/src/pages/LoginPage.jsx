import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout';
import { useAuth } from '../context/AuthContext';
import './LoginPage.css';

const LoginPage = () => {
  const { login, loading } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    login(username, password);
  };

  return (
    <AuthLayout title="Đăng nhập" subtitle="Chào mừng bạn quay lại với Little Joys">
      <form className="auth-form login-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label" htmlFor="username">Tên đăng nhập</label>
          <input
            id="username"
            type="text"
            className="form-input"
            placeholder="Nhập tên đăng nhập"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor="password">Mật khẩu</label>
          <input
            id="password"
            type="password"
            className="form-input"
            placeholder="Nhập mật khẩu"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit" className="btn-primary login-btn" disabled={loading}>
          {loading ? <span className="spinner"></span> : 'Đăng nhập'}
        </button>
      </form>
      <div className="auth-links">
        <Link to="/forgot-password" className="auth-link">Quên mật khẩu?</Link>
        <Link to="/register" className="auth-link">Chưa có tài khoản? Đăng ký ngay</Link>
      </div>
    </AuthLayout>
  );
};

export default LoginPage;
