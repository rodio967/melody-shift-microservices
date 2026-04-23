import React from 'react';
import { Card, Space, Button, Typography, Divider } from 'antd';
import {
    UserOutlined,
    MailOutlined,
    IdcardOutlined,
    LogoutOutlined
} from '@ant-design/icons';
import OAuthButtons from "../auth/OAuthButtons.jsx";

const { Text, Paragraph } = Typography;

const cardStyle = {
    width: 450,
    borderRadius: 24,
    background: 'rgba(255, 255, 255, 0.8)',
    backdropFilter: 'blur(10px)',
    border: '1px solid rgba(255, 255, 255, 0.3)',
    boxShadow: '0 8px 32px 0 rgba(31, 38, 135, 0.15)',
};

const UserProfileCard = ({ user, onLogout }) => {
    return (
        <Card
            title={
                <Space>
                    <UserOutlined />
                    <span>User profile</span>
                </Space>
            }
            extra={
                <Button
                    type="text"
                    danger
                    icon={<LogoutOutlined />}
                    onClick={onLogout}
                >
                    Log out
                </Button>
            }
            style={cardStyle}
        >
            <Space orientation="vertical" size="middle" style={{ width: '100%' }}>

                {/* ID */}
                <div>
                    <Space>
                        <IdcardOutlined style={{ color: '#c76fff' }} />
                        <Text type="secondary">ID:</Text>
                    </Space>
                    <Paragraph>
                        <Text>{user.userId}</Text>
                    </Paragraph>
                </div>

                <Divider style={{ margin: '8px 0' }} />

                {/* Имя пользователя */}
                <div>
                    <Space>
                        <UserOutlined style={{ color: '#c76fff' }} />
                        <Text type="secondary">Name:</Text>
                    </Space>
                    <Paragraph>
                        <Text>{user.username}</Text>
                    </Paragraph>
                </div>

                <Divider style={{ margin: '8px 0' }} />

                {/* Email */}
                <div>
                    <Space>
                        <MailOutlined style={{ color: '#c76fff' }} />
                        <Text type="secondary">Email:</Text>
                    </Space>
                    <Paragraph>
                        <Text>{user.email}</Text>
                    </Paragraph>
                </div>

                <OAuthButtons />

            </Space>
        </Card>
    );
};

export default UserProfileCard;