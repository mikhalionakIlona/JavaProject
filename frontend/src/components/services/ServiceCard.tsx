import React from 'react';
import { PhotoService, ServiceTypePrices } from '../../types';
import { CurrencyDollarIcon, TrashIcon } from '@heroicons/react/24/outline';

interface ServiceCardProps {
    service: PhotoService;
    onDelete: (id: number) => void;
}

const ServiceCard: React.FC<ServiceCardProps> = ({ service, onDelete }) => {
    return (
        <div className="bg-white rounded-2xl shadow-lg overflow-hidden hover:shadow-xl transition-all duration-300 group">
            <div className="p-6">
                <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center gap-3">
                        <div className="w-12 h-12 bg-gradient-to-br from-primary-100 to-primary-200 rounded-xl flex items-center justify-center">
                            <span className="text-xl">✨</span>
                        </div>
                        <div>
                            <h3 className="font-semibold text-gray-800">{service.name}</h3>
                            <p className="text-xs text-gray-400">ID: {service.id}</p>
                        </div>
                    </div>
                    <button
                        onClick={() => onDelete(service.id)}
                        className="p-1.5 text-red-500 hover:bg-red-50 rounded-lg transition-colors opacity-0 group-hover:opacity-100"
                    >
                        <TrashIcon className="w-4 h-4" />
                    </button>
                </div>

                <div className="space-y-2">
                    <div className="flex items-center gap-2 text-gray-600">
                        <CurrencyDollarIcon className="w-4 h-4 text-primary-500" />
                        <span className="text-sm">Базовая цена: <strong>{ServiceTypePrices[service.serviceType]} ₽</strong></span>
                    </div>
                    <div className="flex items-center gap-2 text-gray-600">
                        <span className="text-sm">Тип: <strong>{service.serviceType}</strong></span>
                    </div>
                </div>

                <div className="mt-4 pt-3 border-t border-gray-100">
                    <p className="text-xs text-gray-500">
                        Фотографов: {service.photographers?.length || 0} | Фотосессий: {service.photoSessions?.length || 0}
                    </p>
                </div>
            </div>
        </div>
    );
};

export default ServiceCard;