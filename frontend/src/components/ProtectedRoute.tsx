import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import LoadingSpinner from './common/LoadingSpinner';

interface ProtectedRouteProps {
    children: React.ReactNode;
    allowedRoles: ('user' | 'admin')[];
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, allowedRoles }) => {
    const { isAuthenticated, user, isLoading } = useAuthStore();

    // Показываем спиннер пока проверяется авторизация
    if (isLoading) {
        return <LoadingSpinner />;
    }

    // Если не авторизован - на страницу входа
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    // Если роль пользователя не разрешена для этого маршрута
    if (user && !allowedRoles.includes(user.role)) {
        // Перенаправляем на соответствующий дашборд в зависимости от роли
        if (user.role === 'admin') {
            return <Navigate to="/admin/dashboard" replace />;
        }
        if (user.role === 'user') {
            return <Navigate to="/user/dashboard" replace />;
        }
        // Если роль неизвестна - на главную
        return <Navigate to="/" replace />;
    }

    return <>{children}</>;
};

export default ProtectedRoute;