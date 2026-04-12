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

    if (isLoading) {
        return <LoadingSpinner />;
    }
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (user && !allowedRoles.includes(user.role)) {

        if (user.role === 'admin') {
            return <Navigate to="/admin/dashboard" replace />;
        }
        if (user.role === 'user') {
            return <Navigate to="/user/dashboard" replace />;
        }

        return <Navigate to="/" replace />;
    }

    return <>{children}</>;
};

export default ProtectedRoute;