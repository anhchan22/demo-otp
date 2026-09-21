import React from 'react';
import { Navigate, Link } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout';
import { useAuth } from '../context/AuthContext';
import './VerifyOtpPage.css';

const VerifyOtpPage = () => {
  const { otpState, otp, setOtp, submitOtp, resendOtp, loading } = useAuth();

  if (!otpState) {
    return <Navigate to="/login" />;
  }

  const formatTime = (seconds) => {
    const m = String(Math.floor(seconds / 60)).padStart(2, '0');
    const s = String(seconds % 60).padStart(2, '0');
    return `${m}:${s}`;
  };

  return (
    <AuthLayout title="Xác thực OTP" subtitle="Nhập mã 6 chữ số vừa được gửi đến bạn">
      <form className="auth-form otp-form" onSubmit={submitOtp}>
        <div className="otp-destination">
          Mã OTP đã được gửi tới <strong>{otpState.destination}</strong>
        </div>
        <div className="otp-input-container">
          <input
            type="text"
            className="otp-input"
            maxLength={6}
            inputMode="numeric"
            placeholder="000000"
            value={otp}
            onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
            autoFocus
            required
          />
        </div>

        <div className="timer-container">
          <span className="timer-text">
            Hết hạn sau <strong>{formatTime(otpState.expires || 0)}</strong>
          </span>
          <span className="timer-purpose">
            {otpState.purpose === 'RESET_PASSWORD' ? 'Đặt lại mật khẩu' : 'Xác thực tài khoản'}
          </span>
        </div>

        <button type="submit" className="btn-primary" disabled={loading || otp.length !== 6}>
          {loading ? 'Đang xác thực...' : 'Xác nhận OTP'}
        </button>
      </form>

      <div className="auth-links">
        <button
          className="btn-secondary"
          onClick={resendOtp}
          disabled={loading || !otpState.challengeId || otpState.cooldown > 0}
        >
          Gửi lại mã {otpState.cooldown > 0 && `(${otpState.cooldown}s)`}
        </button>
        <Link to="/verify-method" className="auth-link back-link" style={{ marginTop: '8px' }}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          Quay lại chọn phương thức
        </Link>
      </div>
    </AuthLayout>
  );
};

export default VerifyOtpPage;
