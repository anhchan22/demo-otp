import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout';
import { useAuth } from '../context/AuthContext';
import './RegisterPage.css';

const RegisterPage = () => {
  const { register, loading } = useAuth();
  const [formData, setFormData] = useState({
    fullName: '',
    username: '',
    email: '',
    phone: '',
    password: ''
  });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    register(formData);
  };

  return (
    <AuthLayout title="Đăng ký" subtitle="Tạo tài khoản để trải nghiệm Little Joys">
      <form className="auth-form register-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label" htmlFor="fullName">Họ tên</label>
          <input
            id="fullName"
            name="fullName"
            type="text"
            className="form-input"
            placeholder="Nhập họ và tên"
            value={formData.fullName}
            onChange={handleChange}
          />
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor="username">Tên đăng nhập *</label>
          <input
            id="username"
            name="username"
            type="text"
            className="form-input"
            placeholder="Nhập tên đăng nhập"
            value={formData.username}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor="email">Email *</label>
          <input
            id="email"
            name="email"
            type="email"
            className="form-input"
            placeholder="Nhập địa chỉ email"
            value={formData.email}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor="phone">Số điện thoại</label>
          <input
            id="phone"
            name="phone"
            type="tel"
            className="form-input"
            placeholder="Nhập số điện thoại"
            value={formData.phone}
            onChange={handleChange}
          />
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor="password">Mật khẩu *</label>
          <input
            id="password"
            name="password"
            type="password"
            className="form-input"
            placeholder="Tạo mật khẩu"
            value={formData.password}
            onChange={handleChange}
            required
          />
        </div>
        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? 'Đang đăng ký...' : 'Đăng ký'}
        </button>
      </form>
      <div className="auth-links">
        <Link to="/login" className="auth-link">Đã có tài khoản? Đăng nhập</Link>
      </div>
    </AuthLayout>
  );
};

export default RegisterPage;
