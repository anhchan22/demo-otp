import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout';
import { useAuth } from '../context/AuthContext';

const ForgotPasswordPage = () => {
  const { forgotPassword, loading } = useAuth();
  const [identifier, setIdentifier] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    forgotPassword(identifier);
  };

  return (
    <AuthLayout title="Quên mật khẩu" subtitle="Nhập thông tin để khôi phục tài khoản">
      <form className="auth-form forgot-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label" htmlFor="identifier">Tên đăng nhập / Email / SĐT</label>
          <input
            id="identifier"
            type="text"
            className="form-input"
            placeholder="Nhập thông tin"
            value={identifier}
            onChange={(e) => setIdentifier(e.target.value)}
            required
          />
        </div>
        <button type="submit" className="btn-primary" disabled={loading || !identifier}>
          {loading ? 'Đang xử lý...' : 'Tiếp tục'}
        </button>
      </form>
      <div className="auth-links">
        <Link to="/login" className="auth-link back-link">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          Quay lại đăng nhập
        </Link>
      </div>
    </AuthLayout>
  );
};

export default ForgotPasswordPage;
