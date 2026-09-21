import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../utils/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const navigate = useNavigate();
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  // Recovery & OTP flow state
  const [recovery, setRecovery] = useState(null);
  const [otpState, setOtpState] = useState(null);
  const [otp, setOtp] = useState('');
  const [resetForm, setResetForm] = useState({ ready: false, newPassword: '', confirmPassword: '' });

  const clearMessages = useCallback(() => {
    setError('');
    setNotice('');
  }, []);

  // OTP countdown timer
  useEffect(() => {
    if (!otpState?.challengeId && !otpState?.channel) return undefined;
    const timer = window.setInterval(() => setOtpState((current) => current && ({
      ...current,
      cooldown: Math.max(0, current.cooldown - 1),
      expires: Math.max(0, current.expires - 1),
    })), 1000);
    return () => window.clearInterval(timer);
  }, [otpState?.challengeId, otpState?.channel]);

  const loadChannels = async () => {
    const result = await api('/otp/channels', { method: 'POST' });
    return result.channels;
  };

  const run = async (action, onError) => {
    setLoading(true);
    setError('');
    try {
      await action();
    } catch (exception) {
      if (onError) await onError(exception);
      else setError(exception.message);
    } finally {
      setLoading(false);
    }
  };

  const login = (username, password) => {
    run(async () => {
      const result = await api('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      });
      setCurrentUser({ username: result.username, fullName: result.fullName });
      setNotice('');
      navigate('/');
    }, async (exception) => {
      if (exception.code === 'USR-4031') {
        const channels = exception.channels || await loadChannels();
        setRecovery({
          channels,
          purpose: 'VERIFY_ACCOUNT',
        });
        setNotice('Tài khoản chưa được xác thực. Hãy chọn Email để nhận OTP.');
        navigate('/verify-method');
      } else {
        setError(exception.message);
      }
    });
  };

  const register = (userData) => {
    run(async () => {
      const result = await api('/auth/register', {
        method: 'POST',
        body: JSON.stringify(userData),
      });
      setRecovery({ channels: result.channels || [], purpose: 'VERIFY_ACCOUNT' });
      setNotice('Đăng ký thành công. Hãy chọn phương thức nhận OTP.');
      navigate('/verify-method');
    });
  };

  const logout = () => {
    api('/auth/logout', { method: 'POST' }).catch(() => undefined);
    setCurrentUser(null);
    clearMessages();
    navigate('/');
  };

  const forgotPassword = (identifier) => {
    run(async () => {
      const result = await api('/auth/forgot-password', {
        method: 'POST',
        body: JSON.stringify({ identifier }),
      });
      setRecovery({ channels: result.channels || [], purpose: 'RESET_PASSWORD' });
      navigate('/verify-method');
    });
  };

  const chooseOtpChannel = (channel) => {
    if (!recovery) return;
    const purpose = recovery.purpose || 'VERIFY_ACCOUNT';
    const selectedChannel = recovery.channels?.find((item) => item.channel === channel);

    // Navigate immediately - show OTP screen while request is in-flight
    setOtpState({
      challengeId: null,
      channel,
      destination: selectedChannel?.destination || channel,
      purpose,
      cooldown: 0,
      expires: 300,
      sendPending: true,
    });
    setOtp('');
    navigate('/verify-otp');

    api('/otp/send', { method: 'POST', body: JSON.stringify({ channel }) })
      .then((result) => {
        setOtpState((current) => current && ({
          ...current,
          ...result,
          purpose,
          cooldown: result.resendAfter,
          expires: result.expiresIn,
          sendPending: false,
        }));
      })
      .catch((exception) => {
        setOtpState((current) => current && ({ ...current, sendPending: false, sendError: exception.message }));
        setError(`Không gửi được OTP qua ${channel}. Bạn vẫn có thể nhập mã ở màn này.`);
      });
  };

  const submitOtp = (e) => {
    e?.preventDefault();
    if (!otpState?.challengeId) {
      setError('Chưa có phiên OTP từ backend để xác thực mã này.');
      return;
    }
    run(async () => {
      const result = await api('/otp/verify', {
        method: 'POST',
        body: JSON.stringify({ challengeId: otpState.challengeId, otp }),
      });
      if (result.purpose === 'RESET_PASSWORD') {
        setResetForm((prev) => ({ ...prev, ready: true }));
        navigate('/reset-password');
      } else {
        setNotice('Tài khoản đã được xác thực. Bạn có thể đăng nhập.');
        navigate('/login');
      }
    });
  };

  const resendOtp = () => {
    if (!otpState || otpState.cooldown > 0) return;
    run(async () => {
      const result = await api('/otp/resend', {
        method: 'POST',
        body: JSON.stringify({ challengeId: otpState.challengeId }),
      });
      setOtpState({
        ...result,
        purpose: otpState.purpose,
        cooldown: result.resendAfter,
        expires: result.expiresIn,
      });
      setOtp('');
    });
  };

  const resetPassword = (e) => {
    e?.preventDefault();
    run(async () => {
      await api('/auth/reset-password', {
        method: 'POST',
        body: JSON.stringify({
          newPassword: resetForm.newPassword,
          confirmPassword: resetForm.confirmPassword,
        }),
      });
      setNotice('Đổi mật khẩu thành công. Hãy đăng nhập bằng mật khẩu mới.');
      navigate('/login');
    });
  };

  return (
    <AuthContext.Provider value={{
      currentUser, loading, error, notice, recovery, otpState, otp, resetForm,
      setOtp, setResetForm, setError, setNotice, clearMessages,
      login, logout, register, forgotPassword, resetPassword,
      chooseOtpChannel, submitOtp, resendOtp,
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
