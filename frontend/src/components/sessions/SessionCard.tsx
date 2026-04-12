import React from 'react';
import { PhotoSession } from '../../types';
import { CalendarIcon, UserIcon, CameraIcon, SparklesIcon, TrashIcon } from '@heroicons/react/24/outline';

interface SessionCardProps {
    session: PhotoSession;
    onDelete: (id: number) => void;
}

const SessionCard: React.FC<SessionCardProps> = ({ session, onDelete }) => {
    return (
        <div className="bg-white rounded-2xl shadow-lg overflow-hidden hover:shadow-xl transition-all duration-300">
            <div className="p-6">
                <div className="flex justify-between items-start mb-4">
                    <div className="flex items-center gap-3">
                        <div className="w-12 h-12 bg-gradient-to-br from-secondary-100 to-secondary-200 rounded-xl flex items-center justify-center">
                            <span className="text-xl">📸</span>
                        </div>
                        <div>
                            <h3 className="font-semibold text-gray-800">Фотосессия #{session.id}</h3>
                            <p className="text-xs text-gray-400">
                                {new Date(session.date).toLocaleDateString()}
                            </p>
                        </div>
                    </div>
                    <button onClick={() => onDelete(session.id)} className="p-1.5 text-red-500 hover:bg-red-50 rounded-lg">
                        <TrashIcon className="w-4 h-4" />
                    </button>
                </div>

                <div className="space-y-3">
                    <div className="flex items-center gap-2 text-gray-600">
                        <UserIcon className="w-4 h-4 text-primary-500" />
                        <span className="text-sm">Клиент: <strong>{session.clientName} {session.clientLastName}</strong></span>
                    </div>
                    <div className="flex items-center gap-2 text-gray-600">
                        <CameraIcon className="w-4 h-4 text-primary-500" />
                        <span className="text-sm">Фотограф: <strong>{session.photographerName}</strong></span>
                    </div>
                    <div className="flex items-center gap-2 text-gray-600">
                        <SparklesIcon className="w-4 h-4 text-primary-500" />
                        <span className="text-sm">Услуга: <strong>{session.serviceName}</strong></span>
                    </div>
                    <div className="flex items-center gap-2 text-gray-600">
                        <CalendarIcon className="w-4 h-4 text-primary-500" />
                        <span className="text-sm">Дата: <strong>{new Date(session.date).toLocaleString()}</strong></span>
                    </div>
                </div>

                <div className="mt-4 pt-3 border-t border-gray-100">
                    <div className="flex justify-between items-center">
                        <span className="text-sm text-gray-500">Стоимость:</span>
                        <span className="text-lg font-bold text-primary-600">{session.totalPrice} ₽</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SessionCard;