import React from 'react';
import { PhotoService, ServiceTypePrices } from '../../types';
import { DollarSign, TrashIcon } from 'lucide-react';

interface ServiceCardProps {
    service: PhotoService;
    onDelete: (id: number) => void;
}

const ServiceCard: React.FC<ServiceCardProps> = ({ service, onDelete }) => {
    const getServiceIcon = (serviceType: string) => {
        const icons: Record<string, string> = {
            WEDDING: '💍',
            PORTRAIT: '👤',
            PRODUCT: '📦',
            CORPORATE: '🏢',
            FAMILY: '👨‍👩‍👧‍👦',
        };
        return icons[serviceType] || '✨';
    };

    const getServiceColor = (serviceType: string) => {
        const colors: Record<string, string> = {
            WEDDING: 'from-pink-500 to-rose-500',
            PORTRAIT: 'from-blue-500 to-cyan-500',
            PRODUCT: 'from-green-500 to-emerald-500',
            CORPORATE: 'from-purple-500 to-indigo-500',
            FAMILY: 'from-orange-500 to-amber-500',
        };
        return colors[serviceType] || 'from-purple-500 to-pink-500';
    };

    const serviceIcon = getServiceIcon(service.serviceType);
    const serviceColor = getServiceColor(service.serviceType);
    const servicePrice = ServiceTypePrices[service.serviceType] || 0;

    return (
        <div className="glass-card p-6 cursor-pointer group">
            <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-3">
                    <div className={`w-12 h-12 bg-gradient-to-br ${serviceColor} rounded-xl flex items-center justify-center text-2xl shadow-lg`}>
                        {serviceIcon}
                    </div>
                    <div>
                        <h3 className="font-semibold text-white text-lg">
                            {service.name}
                        </h3>
                        <p className="text-xs text-white/40">ID: {service.id}</p>
                    </div>
                </div>
                <button
                    onClick={() => onDelete(service.id)}
                    className="p-1.5 text-red-400 hover:bg-red-500/20 rounded-lg transition-colors opacity-0 group-hover:opacity-100"
                >
                    <TrashIcon className="w-4 h-4" />
                </button>
            </div>

            <div className="space-y-2">
                {}
                <div className="flex items-center justify-between bg-gradient-to-r from-green-500/20 to-emerald-500/20 rounded-lg p-3">
                    <div className="flex items-center gap-2">
                        <DollarSign className="w-4 h-4 text-green-400" />
                        <span className="text-sm font-medium text-white/80">Базовая цена:</span>
                    </div>
                    <span className="text-xl font-bold text-green-400">
                        {servicePrice} BYN
                    </span>
                </div>

                <div className="flex items-center gap-2 text-white/60 pt-2">
                    <span className="text-sm">Тип услуги:</span>
                    <span className="text-sm font-semibold text-purple-400">{service.serviceType}</span>
                </div>
            </div>

            <div className="mt-4 pt-3 border-t border-white/10">
                <p className="text-xs text-white/40">
                    Фотографов: {service.photographers?.length || 0} | Фотосессий: {service.photoSessions?.length || 0}
                </p>
            </div>
        </div>
    );
};

export default ServiceCard;