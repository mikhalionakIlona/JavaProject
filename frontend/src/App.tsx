import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { useAuthStore } from './store/authStore';
import { LoginPage } from './pages/LoginPage';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/common/Navbar';
import { UserDashboard } from './pages/user/UserDashboard';
import UserPhotographers from './components/user/UserPhotographers';
import { UserServices } from './components/user/UserServices';
import { AdminDashboard } from './pages/admin/AdminDashboard';
import { ClientsPage } from './pages/admin/ClientsPage';
import { PhotographersPage } from './pages/admin/PhotographersPage';
import { SessionsPage } from './pages/admin/SessionsPage';
import { ServicesPage } from './pages/admin/ServicesPage';
import { PhotosPage } from './pages/admin/PhotosPage';
import LoadingSpinner from './components/common/LoadingSpinner';

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            refetchOnWindowFocus: false,
            retry: 1,
            staleTime: 5 * 60 * 1000,
        },
    },
});

const FloatingParticles: React.FC = () => {
    useEffect(() => {
        const createParticle = () => {
            const particle = document.createElement('div');
            particle.className = 'floating-particle';
            const size = Math.random() * 4 + 2;
            particle.style.width = `${size}px`;
            particle.style.height = `${size}px`;
            particle.style.background = `radial-gradient(circle, ${['#8b5cf6', '#ec4899', '#06b6d4'][Math.floor(Math.random() * 3)]}, transparent)`;
            particle.style.borderRadius = '50%';
            particle.style.position = 'fixed';
            particle.style.left = `${Math.random() * 100}%`;
            particle.style.top = `${Math.random() * 100}%`;
            particle.style.opacity = `${Math.random() * 0.5 + 0.2}`;
            particle.style.animation = `float ${Math.random() * 5 + 3}s ease-in-out infinite`;
            document.body.appendChild(particle);

            setTimeout(() => {
                particle.remove();
            }, 10000);
        };

        const interval = setInterval(createParticle, 500);
        return () => clearInterval(interval);
    }, []);

    return null;
};

const AppLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const { isAuthenticated, isLoading } = useAuthStore();

    if (isLoading) {
        return (
            <div className="min-h-screen bg-black flex items-center justify-center">
                <LoadingSpinner />
            </div>
        );
    }

    if (!isAuthenticated) return <>{children}</>;

    return (
        <div className="min-h-screen bg-black">
            <FloatingParticles />
            <Navbar />
            <main className="relative z-10 pt-16">
                {children}
            </main>
        </div>
    );
};

function App() {
    const { hydrate, isLoading } = useAuthStore();

    useEffect(() => {
        hydrate();
    }, [hydrate]);

    if (isLoading) {
        return (
            <div className="min-h-screen bg-black flex items-center justify-center">
                <LoadingSpinner />
            </div>
        );
    }

    return (
        <QueryClientProvider client={queryClient}>
            <Router>
                <Toaster
                    position="top-right"
                    toastOptions={{
                        style: {
                            background: '#1a1a2e',
                            color: '#fff',
                            borderRadius: '1rem',
                            padding: '12px 20px',
                            border: '1px solid rgba(139, 92, 246, 0.3)',
                        },
                    }}
                />
                <AppLayout>
                    <Routes>
                        {}
                        <Route path="/login" element={<LoginPage />} />
                        <Route path="/" element={<Navigate to="/login" replace />} />

                        {}
                        <Route path="/user/dashboard" element={
                            <ProtectedRoute allowedRoles={['user']}>
                                <UserDashboard />
                            </ProtectedRoute>
                        } />
                        <Route path="/user/photographers" element={
                            <ProtectedRoute allowedRoles={['user']}>
                                <UserPhotographers />
                            </ProtectedRoute>
                        } />
                        <Route path="/user/services" element={
                            <ProtectedRoute allowedRoles={['user']}>
                                <UserServices />
                            </ProtectedRoute>
                        } />

                        {}
                        <Route path="/admin/dashboard" element={
                            <ProtectedRoute allowedRoles={['admin']}>
                                <AdminDashboard />
                            </ProtectedRoute>
                        } />
                        <Route path="/admin/clients" element={
                            <ProtectedRoute allowedRoles={['admin']}>
                                <ClientsPage />
                            </ProtectedRoute>
                        } />
                        <Route path="/admin/photographers" element={
                            <ProtectedRoute allowedRoles={['admin']}>
                                <PhotographersPage />
                            </ProtectedRoute>
                        } />
                        <Route path="/admin/sessions" element={
                            <ProtectedRoute allowedRoles={['admin']}>
                                <SessionsPage />
                            </ProtectedRoute>
                        } />
                        <Route path="/admin/services" element={
                            <ProtectedRoute allowedRoles={['admin']}>
                                <ServicesPage />
                            </ProtectedRoute>
                        } />
                        <Route path="/admin/photos" element={
                            <ProtectedRoute allowedRoles={['admin']}>
                                <PhotosPage />
                            </ProtectedRoute>
                        } />

                        {}
                        <Route path="/user" element={<Navigate to="/user/dashboard" replace />} />
                        <Route path="/admin" element={<Navigate to="/admin/dashboard" replace />} />
                        <Route path="*" element={<Navigate to="/login" replace />} />
                    </Routes>
                </AppLayout>
            </Router>
        </QueryClientProvider>
    );
}

export default App;