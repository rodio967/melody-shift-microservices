import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate, useParams } from 'react-router-dom';
import { Spin, Result, Button } from 'antd';

const OAuthCallbackPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { platform } = useParams();
    const [status, setStatus] = useState('loading');

    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const success = params.get('success');
        const error = params.get('error');

        if (success) {
            setStatus('success');
            setTimeout(() => navigate('/'), 2000);
        } else if (error) {
            setStatus('error');
        } else {
            navigate('/');
        }
    }, [navigate]);

    const containerStyle = {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 25%, #b83b5e 50%, #f08a5d 100%)',
    };

    if (status === 'loading') {
        return (
            <div style={containerStyle}>
                <Spin size="large" tip={`Connecting ${platform}...`} />
            </div>
        );
    }

    if (status === 'success') {
        return (
            <div style={containerStyle}>
                <Result
                    status="success"
                    title={`${platform} connected!`}
                    subTitle="Redirecting to profile..."
                    extra={
                        <Button type="primary" onClick={() => navigate('/')}>
                            Go to profile
                        </Button>
                    }
                />
            </div>
        );
    }

    return (
        <div style={containerStyle}>
            <Result
                status="error"
                title="Connection failed"
                subTitle="Could not connect platform"
                extra={
                    <Button type="primary" onClick={() => navigate('/')}>
                        Back to profile
                    </Button>
                }
            />
        </div>
    );
};

export default OAuthCallbackPage;