import { Routes, Route } from 'react-router-dom';
import AuthPage from './pages/AuthPage';

function App() {
    return (
        <Routes>
            <Route path="/" element={<AuthPage />} />
            <Route path="/profile" element={<AuthPage />} />  {/* Можно отдельную страницу профиля */}
        </Routes>
    );
}

export default App;