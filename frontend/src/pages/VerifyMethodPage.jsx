import React from 'react';
import { Navigate, Link } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout';
import { useAuth } from '../context/AuthContext';
import './VerifyMethodPage.css';

const VerifyMethodPage = () => {
  const { recovery, chooseOtpChannel, loading } = useAuth();

  if (!recovery) {
    return <Navigate to="/login" />;
  }

  const iconMap = {
    EMAIL: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect width="20" height="16" x="2" y="4" rx="2"/>
        <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/>
      </svg>
    ),
    SMS: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        <path d="M8 9h8M8 13h5" strokeWidth="1.5"/>
      </svg>
    ),
    ZALO: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M7.9 20A9 9 0 1 0 4 16.1L2 22Z"/>
        <path d="M8 10h8M10 14h6" strokeWidth="1.5"/>
      </svg>
    )
  };
  const fallbackIcon = (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect width="18" height="11" x="3" y="11" rx="2" ry="2"/>
      <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
    </svg>
  );

  const labelMap = { EMAIL: 'Email', SMS: 'SMS Sandbox', ZALO: 'Zalo' };

  return (
    <AuthLayout title="Chọn phương thức xác thực" subtitle="Vui lòng chọn cách nhận mã OTP">
      <div className="channel-list">
        {(recovery.channels || []).map((item, index) => {
          const channelDescription = [
            item.destination,
            item.sandbox ? 'Sandbox' : null,
            !item.available && item.reason ? item.reason : null,
          ].filter(Boolean).join(' · ');

          return (
            <button
              key={item.channel}
              className="channel-card"
              style={{ animationDelay: `${index * 100}ms` }}
              onClick={() => chooseOtpChannel(item.channel)}
              disabled={loading}
            >
              <div className="channel-icon-box">
                {iconMap[item.channel] || fallbackIcon}
              </div>
              <div className="channel-info">
                <span className="channel-label">{labelMap[item.channel] || item.channel}</span>
                <span className="channel-dest">
                  {channelDescription || 'Kênh sẵn sàng kiểm tra'}
                </span>
              </div>
              <svg className="channel-arrow" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="m9 18 6-6-6-6"/>
              </svg>
            </button>
          );
        })}
        {!recovery.channels?.length && (
          <div className="auth-message auth-error">Không tìm thấy kênh nhận OTP.</div>
        )}
      </div>
      <div className="auth-links">
        <Link to="/forgot-password" className="auth-link back-link">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          Quay lại bước trước
        </Link>
      </div>
    </AuthLayout>
  );
};

export default VerifyMethodPage;
