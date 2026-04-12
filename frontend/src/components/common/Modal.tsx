import React, { useEffect } from 'react';
import { XMarkIcon } from '@heroicons/react/24/outline';

interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title: string;
    children: React.ReactNode;
    size?: 'sm' | 'md' | 'lg' | 'xl';
}

const Modal: React.FC<ModalProps> = ({ isOpen, onClose, title, children, size = 'md' }) => {
    useEffect(() => {
        if (isOpen) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = 'unset';
        }
        return () => {
            document.body.style.overflow = 'unset';
        };
    }, [isOpen]);

    const sizes = {
        sm: 'max-w-md',
        md: 'max-w-lg',
        lg: 'max-w-2xl',
        xl: 'max-w-4xl',
    };

    if (!isOpen) return null;

    const handleBackdropKeyDown = (e: React.KeyboardEvent<HTMLButtonElement>) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            onClose();
        }
    };

    return (
        <div className="fixed inset-0 z-50">
            <button
                className="fixed inset-0 bg-black/50 backdrop-blur-sm cursor-pointer"
                onClick={onClose}
                onKeyDown={handleBackdropKeyDown}
                aria-label="Close modal"
                tabIndex={0}
            />
            <div className={`fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 ${sizes[size]} w-full bg-white rounded-2xl shadow-2xl max-h-[90vh] overflow-hidden`}>
                <div className="flex justify-between items-center p-6 border-b border-gray-100 bg-gradient-to-r from-gray-50 to-white">
                    <h2 className="text-xl font-bold text-gray-800">{title}</h2>
                    <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition-all duration-200">
                        <XMarkIcon className="w-5 h-5 text-gray-500" />
                    </button>
                </div>
                <div className="p-6 overflow-y-auto max-h-[calc(90vh-80px)]">
                    {children}
                </div>
            </div>
        </div>
    );
};

export { Modal };