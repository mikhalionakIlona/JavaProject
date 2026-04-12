import React from 'react';
import { Photographer } from '../../types';
import { PhoneIcon, CurrencyDollarIcon, PencilIcon, TrashIcon } from '@heroicons/react/24/outline';

interface PhotographerCardProps {
    photographer: Photographer;
    onEdit: (photographer: Photographer) => void;
    onDelete: (id: number) => void;
}

const PhotographerCard: React.FC<PhotographerCardProps> = ({ photographer, onEdit, onDelete }) => {
    return (
        <div className="bg-white rounded-2xl shadow-lg overflow-hidden hover:shadow-xl transition-all duration-300 group">
            <div className="p-6">
                <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center gap-3">
                        <div className="w-12 h-12 bg-gradient-to-br from-secondary-100 to-secondary-200 rounded-xl flex items-center justify-center">
                            <span className="text-xl">📷</span>
                        </div>
                        <div>
                            <h3 className="font-semibold text-gray-800">
                                {photographer.firstName} {photographer.lastName}
                            </h3>
                            {photographer.patronymic && (
                                <p className="text-xs text-gray-400">{photographer.patronymic}</p>
                            )}
                        </div>
                    </div>
                    <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                        <button
                            onClick={() => onEdit(photographer)}
                            className="p-1.5 text-yellow-500 hover:bg-yellow-50 rounded-lg transition-colors"
                        >
                            <PencilIcon className="w-4 h-4" />
                        </button>
                        <button
                            onClick={() => onDelete(photographer.id)}
                            className="p-1.5 text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                        >
                            <TrashIcon className="w-4 h-4" />
                        </button>
                    </div>
                </div>

                <div className="space-y-2">
                    <div className="flex items-center gap-2 text-gray-600">
                        <PhoneIcon className="w-4 h-4 text-primary-500" />
                        <span className="text-sm">{photographer.phone}</span>
                    </div>
                    <div className="flex items-center gap-2 text-gray-600">
                        <CurrencyDollarIcon className="w-4 h-4 text-primary-500" />
                        <span className="text-sm">Ставка: <strong>{photographer.hourlyRate} ₽/час</strong></span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PhotographerCard;