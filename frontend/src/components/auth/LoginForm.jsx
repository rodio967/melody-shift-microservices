import { Button, Form, Input } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';

function LoginForm({ onSubmit, loading }) {
    const [form] = Form.useForm();

    return (
        <Form
            form={form}
            name="login"
            onFinish={onSubmit}
            layout="vertical"
            size="large"
            style={{ maxWidth: 600 }}
            autoComplete="off"
        >
            <Form.Item
                label="Username"
                name="username"
                rules={[{ required: true, message: 'Please input your username!' }]}
            >
                <Input prefix={<UserOutlined />} placeholder="Username" />
            </Form.Item>

            <Form.Item
                label="Password"
                name="password"
                rules={[{ required: true, message: 'Please input your password!' }]}
            >
                <Input.Password prefix={<LockOutlined />} placeholder="Password" />
            </Form.Item>

            <Form.Item>
                <Button type="primary" htmlType="submit" block loading={loading}>
                    {loading ? 'Loading...' : 'Login'}
                </Button>
            </Form.Item>
        </Form>
    );
}

export default LoginForm;