import axios from 'axios';

const apiClient = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000,  // таймаут 10 секунд
});

// перехватчик запросов — добавляет токен автоматически
apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// перехватчик ответов — обработка ошибок
apiClient.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        // если 401 (не авторизован) — чистим токен
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            // перенаправление на страницу входа
            window.location.href = '/';
        }

        return Promise.reject(error);
    }
);

export default apiClient;