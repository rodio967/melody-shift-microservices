import { useState } from 'react';
import { Card, Tabs, message } from 'antd';
import { authApi } from '../api/auth';
import { storage } from '../utils/storage';
import LoginForm from '../components/auth/LoginForm';
import RegisterForm from '../components/auth/RegisterForm';
import UserProfileCard from '../components/profile/UserProfileCard';

const AuthPage = () => {
    const [user, setUser] = useState(() => storage.getUser());
    const [loading, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState('login');

    const handleLogin = async (values) => {
        setLoading(true);
        try {
            const data = await authApi.login(values.username, values.password);
            storage.setToken(data.token);
            storage.setUser(data);
            setUser(data);
            message.success(`Welcome, ${data.username}!`);
        } catch (err) {
            message.error(err.response?.data?.error || 'Login error');
        } finally {
            setLoading(false);
        }
    };

    const handleRegister = async (values) => {
        setLoading(true);
        try {
            await authApi.register(
                values.username,
                values.email,
                values.password,
                values.confirmPassword
            );
            message.success('Registration successful! You can now log in.');
            setActiveTab('login');
        } catch (err) {
            message.error(err.response?.data?.error || 'Registration error');
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = () => {
        storage.clear();
        setUser(null);
        message.info('You have been logged out.');
    };

    if (user) {
        return (
            <div style={containerStyle}>
                <UserProfileCard user={user} onLogout={handleLogout} />
            </div>
        );
    }

    return (
        <div style={containerStyle}>
            <Card style={cardStyle}>
                <h1 style={titleStyle}>Melody Shift</h1>

                <Tabs
                    activeKey={activeTab}
                    onChange={setActiveTab}
                    centered
                    items={[
                        {
                            key: 'login',
                            label: 'Log in',
                            children: <LoginForm onSubmit={handleLogin} loading={loading} />
                        },
                        {
                            key: 'register',
                            label: 'Sign up',
                            children: <RegisterForm onSubmit={handleRegister} loading={loading} />
                        }
                    ]}
                />
            </Card>
        </div>
    );
};

// Стили
const containerStyle = {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: '100vh',
    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 25%, #b83b5e 50%, #f08a5d 100%)',
    backgroundSize: '400% 400%',
    padding: '20px'
};

const cardStyle = {
    width: 450,
    borderRadius: 24,
    background: 'rgba(255, 255, 255, 0.8)',
    backdropFilter: 'blur(10px)',
    border: '1px solid rgba(255, 255, 255, 0.3)',
    boxShadow: '0 8px 32px 0 rgba(31, 38, 135, 0.15)',
};

const titleStyle = {
    textAlign: 'center',
    marginBottom: 24,
    color: '#333'
};

export default AuthPage;