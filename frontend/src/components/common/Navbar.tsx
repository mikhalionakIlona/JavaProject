import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import {
    LayoutDashboard,
    Users,
    Camera,
    Calendar,
    Sparkles,
    Image,
    LogOut,
    UserCircle,
    Menu,
    X
} from 'lucide-react';

const Navbar: React.FC = () => {
    const { user, logout } = useAuthStore();
    const navigate = useNavigate();
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [scrolled, setScrolled] = useState(false);

    useEffect(() => {
        const handleScroll = () => {
            setScrolled(window.scrollY > 20);
        };
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    // Админ навигация
    const adminNavItems = [
        { path: '/admin/dashboard', label: 'Главная', icon: LayoutDashboard },
        { path: '/admin/clients', label: 'Клиенты', icon: Users },
        { path: '/admin/photographers', label: 'Фотографы', icon: Camera },
        { path: '/admin/sessions', label: 'Фотосессии', icon: Calendar },
        { path: '/admin/services', label: 'Услуги', icon: Sparkles },
        { path: '/admin/photos', label: 'Фотографии', icon: Image },
    ];

    // Пользователь навигация - ТЕПЕРЬ ТРИ ВКЛАДКИ КАК У АДМИНА
    const userNavItems = [
        { path: '/user/dashboard', label: 'Главная', icon: LayoutDashboard },
        { path: '/user/photographers', label: 'Фотографы', icon: Camera },
        { path: '/user/services', label: 'Услуги', icon: Sparkles },
    ];

    const navItems = user?.role === 'admin' ? adminNavItems : userNavItems;

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const getHomePath = () => {
        if (user?.role === 'admin') return '/admin/dashboard';
        if (user?.role === 'user') return '/user/dashboard';
        return '/login';
    };

    return (
        <>
            <nav className={`fixed top-0 left-0 right-0 z-50 transition-all duration-500 ${
                scrolled ? 'bg-black/80 backdrop-blur-xl border-b border-purple-500/20' : 'bg-transparent'
            }`}>
                <div className="container mx-auto px-4">
                    <div className="flex justify-between items-center h-16">
                        <Link to={getHomePath()} className="flex items-center space-x-3 group">
                            <div className="w-10 h-10 bg-gradient-to-br from-purple-500 to-pink-500 rounded-xl flex items-center justify-center shadow-lg animate-float">
                                <span className="text-white text-xl">📸</span>
                            </div>
                            <div>
                                <span className="text-xl font-bold gradient-text">PhotoStudio</span>
                                {user?.role === 'admin' && (
                                    <span className="ml-2 px-2 py-0.5 bg-purple-500/20 text-purple-300 text-xs rounded-full border border-purple-500/30">
                                        ADMIN
                                    </span>
                                )}
                                {user?.role === 'user' && (
                                    <span className="ml-2 px-2 py-0.5 bg-rose-500/20 text-rose-300 text-xs rounded-full border border-rose-500/30">
                                        USER
                                    </span>
                                )}
                            </div>
                        </Link>

                        {/* Десктопное меню */}
                        <div className="hidden md:flex items-center space-x-1">
                            {navItems.map((item, idx) => {
                                const Icon = item.icon;
                                return (
                                    <Link
                                        key={item.path}
                                        to={item.path}
                                        className="relative px-4 py-2 rounded-xl text-white/70 hover:text-white hover:bg-white/5 transition-all duration-300 flex items-center gap-2 group"
                                    >
                                        <Icon className="w-4 h-4 group-hover:scale-110 transition-transform" />
                                        <span className="font-medium">{item.label}</span>
                                        <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-gradient-to-r from-purple-500 to-pink-500 group-hover:w-full transition-all duration-300"></span>
                                    </Link>
                                );
                            })}
                        </div>

                        <div className="flex items-center space-x-3">
                            <div className="hidden md:flex items-center space-x-3 px-3 py-1.5 bg-white/5 rounded-full border border-white/10 pulse-glow">
                                <UserCircle className="w-5 h-5 text-purple-400" />
                                <div className="text-sm">
                                    <p className="font-medium text-white">{user?.name || 'Гость'}</p>
                                    <p className="text-xs text-purple-400">{user?.role === 'admin' ? 'Администратор' : 'Клиент'}</p>
                                </div>
                            </div>
                            <button
                                onClick={handleLogout}
                                className="flex items-center gap-2 px-4 py-2 bg-red-500/20 hover:bg-red-500/30 text-red-300 rounded-xl transition-all border border-red-500/30 hover:scale-105 transform duration-200"
                            >
                                <LogOut className="w-4 h-4" />
                                <span className="hidden sm:inline">Выйти</span>
                            </button>

                            <button
                                onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                                className="md:hidden p-2 rounded-lg bg-white/10"
                            >
                                {isMobileMenuOpen ? <X className="w-5 h-5 text-white" /> : <Menu className="w-5 h-5 text-white" />}
                            </button>
                        </div>
                    </div>
                </div>
            </nav>

            {/* Мобильное меню */}
            {isMobileMenuOpen && (
                <div className="fixed top-16 left-0 right-0 z-40 bg-black/95 backdrop-blur-xl border-b border-purple-500/20 md:hidden animate-fade-right">
                    <div className="container mx-auto px-4 py-4 space-y-2">
                        {navItems.map((item) => {
                            const Icon = item.icon;
                            return (
                                <Link
                                    key={item.path}
                                    to={item.path}
                                    onClick={() => setIsMobileMenuOpen(false)}
                                    className="flex items-center gap-3 px-4 py-3 rounded-xl text-white/70 hover:text-white hover:bg-white/5 transition-all"
                                >
                                    <Icon className="w-5 h-5" />
                                    <span className="font-medium">{item.label}</span>
                                </Link>
                            );
                        })}
                        <div className="pt-4 mt-4 border-t border-white/10">
                            <div className="flex items-center gap-3 px-4 py-3">
                                <UserCircle className="w-8 h-8 text-purple-400" />
                                <div>
                                    <p className="font-medium text-white">{user?.name || 'Гость'}</p>
                                    <p className="text-xs text-purple-400">{user?.role === 'admin' ? 'Администратор' : 'Клиент'}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
};

export default Navbar;