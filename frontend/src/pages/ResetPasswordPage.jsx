import React from 'react';
import { Navigate } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout';
import { useAuth } from '../context/AuthContext';

const ResetPasswordPage = () => {
  const { resetPassword, resetForm, setResetForm, loading } = useAuth();

  if (!resetForm.ready) {
    return <Navigate to="/login" />;
  }

  return (
    <AuthLayout title="Đổi mật khẩu" subtitle="Tạo mật khẩu mới cho tài khoản của bạn">
      <form className="auth-form" onSubmit={resetPassword}>
        <div className="form-group">
          <label className="form-label" htmlFor="newPassword">Mật khẩu mới</label>
          <input
            id="newPassword"
            type="password"
            className="form-input"
            placeholder="Tối thiểu 8 ký tự"
            value={resetForm.newPassword}
            onChange={(e) => setResetForm({ ...resetForm, newPassword: e.target.value })}
            required
          />
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor="confirmPassword">Nhập lại mật khẩu</label>
          <input
            id="confirmPassword"
            type="password"
            className="form-input"
            placeholder="Nhập lại mật khẩu"
            value={resetForm.confirmPassword}
            onChange={(e) => setResetForm({ ...resetForm, confirmPassword: e.target.value })}
            required
          />
        </div>
        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? 'Đang xử lý...' : 'Đổi mật khẩu'}
        </button>
      </form>
    </AuthLayout>
  );
};

export default ResetPasswordPage;
