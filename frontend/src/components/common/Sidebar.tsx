import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
    HomeIcon,
    UsersIcon,
    CameraIcon,
    CalendarIcon,
    SparklesIcon,
    PhotoIcon,
    XMarkIcon
} from '@heroicons/react/24/outline';

interface SidebarProps {
    isOpen: boolean;
    onClose: () => void;
}

const Sidebar: React.FC<SidebarProps> = ({ isOpen, onClose }) => {
    const location = useLocation();

    const navItems = [
        { path: '/', label: 'Главная', icon: HomeIcon },
        { path: '/clients', label: 'Клиенты', icon: UsersIcon },
        { path: '/photographers', label: 'Фотографы', icon: CameraIcon },
        { path: '/sessions', label: 'Фотосессии', icon: CalendarIcon },
        { path: '/services', label: 'Услуги', icon: SparklesIcon },
        { path: '/photos', label: 'Фотографии', icon: PhotoIcon },
    ];

    const isActive = (path: string) => {
        if (path === '/') return location.pathname === '/';
        return location.pathname.startsWith(path);
    };

    const handleOverlayKeyDown = (e: React.KeyboardEvent<HTMLButtonElement>) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            onClose();
        }
    };

    return (
        <>
            {}
            {isOpen && (
                <button
                    className="fixed inset-0 bg-black/50 backdrop-blur-sm z-40 md:hidden cursor-pointer"
                    onClick={onClose}
                    onKeyDown={handleOverlayKeyDown}
                    aria-label="Close sidebar"
                    tabIndex={0}
                />
            )}

            {}
            <div className={`fixed top-0 left-0 h-full w-64 bg-white shadow-2xl z-50 transform transition-transform duration-300 ease-in-out ${
                isOpen ? 'translate-x-0' : '-translate-x-full'
            } md:translate-x-0`}>
                <div className="flex flex-col h-full">
                    {}
                    <div className="p-4 border-b border-gray-100 flex justify-between items-center">
                        <div className="flex items-center space-x-2">
                            <div className="w-8 h-8 bg-gradient-to-r from-primary-500 to-secondary-500 rounded-lg flex items-center justify-center">
                                <span className="text-white text-lg">📸</span>
                            </div>
                            <span className="font-bold text-lg gradient-text">PhotoStudio</span>
                        </div>
                        <button onClick={onClose} className="md:hidden p-1 rounded-lg hover:bg-gray-100">
                            <XMarkIcon className="w-5 h-5 text-gray-500" />
                        </button>
                    </div>

                    {}
                    <nav className="flex-1 p-4 space-y-1">
                        {navItems.map((item) => {
                            const active = isActive(item.path);
                            const Icon = item.icon;
                            return (
                                <Link
                                    key={item.path}
                                    to={item.path}
                                    onClick={onClose}
                                    className={`flex items-center space-x-3 px-3 py-2 rounded-xl transition-all duration-200 ${
                                        active
                                            ? 'bg-primary-50 text-primary-600'
                                            : 'text-gray-600 hover:bg-gray-50'
                                    }`}
                                >
                                    <Icon className="w-5 h-5" />
                                    <span className="font-medium">{item.label}</span>
                                </Link>
                            );
                        })}
                    </nav>

                    {}
                    <div className="p-4 border-t border-gray-100">
                        <div className="text-xs text-gray-400 text-center">
                            PhotoStudio v1.0
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export { Sidebar };