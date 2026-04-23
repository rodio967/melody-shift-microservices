import { Button, Form, Input } from 'antd';
import { UserOutlined, MailOutlined, LockOutlined } from '@ant-design/icons';

function RegisterForm({ onSubmit, loading }) {
    const [form] = Form.useForm();

    return (
        <Form
            form={form}
            name="register"
            onFinish={onSubmit}
            layout="vertical"
            size="large"
            style={{ maxWidth: 600 }}
            autoComplete="off"
        >
            <Form.Item
                label="Username"
                name="username"
                rules={[
                    { required: true, message: 'Please input your username!' },
                    { min: 3, message: 'Please enter at least 3 characters' }
                ]}
            >
                <Input prefix={<UserOutlined />} placeholder="Username" />
            </Form.Item>

            <Form.Item
                label="E-mail"
                name="email"
                rules={[
                    { required: true, message: 'Please input your e-mail!' },
                    { type: "email", message: 'Invalid email format :(' }
                ]}
            >
                <Input prefix={<MailOutlined />} placeholder="Email" />
            </Form.Item>

            <Form.Item
                label="Password"
                name="password"
                rules={[
                    { required: true, message: 'Please input your password!' },
                    { min: 6, message: 'Please enter at least 6 characters' }
                ]}
            >
                <Input.Password prefix={<LockOutlined />} placeholder="Password" />
            </Form.Item>

            <Form.Item
                label="Confirm Password"
                name="confirmPassword"
                dependencies={['password']}
                rules={[
                    { required: true, message: 'Please confirm your password!' },
                    ({ getFieldValue }) => ({
                        validator(_, value) {
                            if (!value || getFieldValue('password') === value) {
                                return Promise.resolve();
                            }
                            return Promise.reject(new Error('Passwords do not match!'));
                        },
                    }),
                ]}
            >
                <Input.Password prefix={<LockOutlined />} placeholder="Confirm Password" />
            </Form.Item>

            <Form.Item>
                <Button type="primary" htmlType="submit" block loading={loading}>
                    {loading ? 'Loading...' : 'Register'}
                </Button>
            </Form.Item>
        </Form>
    );
}

export default RegisterForm;