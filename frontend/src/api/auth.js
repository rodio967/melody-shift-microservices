import apiClient from './client';

export const authApi = {
    // регистрация
    register: async (username, email, password, confirmPassword) => {
        const response = await apiClient.post('/auth/register', {
            username,
            email,
            password,
            confirmPassword,
        });
        return response.data;
    },

    // вход
    login: async (username, password) => {
        const response = await apiClient.post('/auth/login', {
            username,
            password,
        });
        return response.data;
    },

    // получить текущего пользователя
    getCurrentUser: async () => {
        const response = await apiClient.get('/auth/me');
        return response.data;
    },
};