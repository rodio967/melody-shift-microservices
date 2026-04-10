import { Button, Space, Divider, message } from 'antd';
import { SpotifyOutlined } from '@ant-design/icons';
import { authApi } from '../../api/auth';
import yandexIcon from '../../assets/icons/yandex_music.svg';

const OAuthButtons = () => {

    const handleOAuth = async (platform) => {
        const token = localStorage.getItem('token');

        if (!token) {
            message.warning('Please log in first!');
            return;
        }

        try {
            message.loading(`Redirecting to ${platform}...`, 1);

            const response = await authApi.getOAuthUrl(platform);

            window.location.href = response.redirectUrl;
        } catch (err) {
            console.error('OAuth error:', err);
            message.error(err.response?.data?.error || 'Authorization error');
        }
    };

    return (
        <>
            <Divider plain style={{ color: '#999', fontSize: '14px' }}>
                Connect music platform
            </Divider>

            <Space orientation="vertical" style={{ width: '100%' }} size="middle">
                <Button
                    block
                    icon={<SpotifyOutlined />}
                    size="large"
                    onClick={() => handleOAuth('spotify')}
                    style={{
                        backgroundColor: '#000000',
                        color: 'white',
                        border: 'none',
                        height: '48px',
                        borderRadius: '12px',
                    }}
                >
                    Connect Spotify
                </Button>

                <Button
                    block
                    icon={<img src={yandexIcon} alt="Yandex" style={{ width: 20, height: 20 }} />}
                    size="large"
                    onClick={() => handleOAuth('yandex')}
                    style={{
                        backgroundColor: '#000000',
                        color: 'white',
                        border: 'none',
                        height: '48px',
                        borderRadius: '12px',
                    }}
                >
                    Connect Yandex Music
                </Button>
            </Space>
        </>
    );
};

export default OAuthButtons;