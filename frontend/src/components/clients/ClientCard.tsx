import React from 'react';
import { Client } from '../../types';
import { PhoneIcon, EnvelopeIcon, PencilIcon, TrashIcon, EyeIcon, CameraIcon } from '@heroicons/react/24/outline';

interface ClientCardProps {
    client: Client;
    sessionsCount: number;
    onEdit: (client: Client) => void;
    onDelete: (id: number) => void;
    onView: (client: Client) => void;
}

const ClientCard: React.FC<ClientCardProps> = ({ client, sessionsCount, onEdit, onDelete, onView }) => {
    return (
        <div className="glass-card p-6 cursor-pointer group">
            <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-3">
                    <div className="w-12 h-12 bg-gradient-to-br from-purple-500/20 to-pink-500/20 rounded-xl flex items-center justify-center">
                        <span className="text-2xl">👤</span>
                    </div>
                    <div>
                        <h3 className="font-semibold text-white text-lg">
                            {client.firstName} {client.lastName}
                        </h3>
                        <p className="text-xs text-white/40">ID: {client.id}</p>
                    </div>
                </div>
                <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                        onClick={() => onView(client)}
                        className="p-1.5 text-blue-400 hover:bg-blue-500/20 rounded-lg transition-colors"
                        title="Просмотр сессий"
                    >
                        <EyeIcon className="w-4 h-4" />
                    </button>
                    <button
                        onClick={() => onEdit(client)}
                        className="p-1.5 text-yellow-400 hover:bg-yellow-500/20 rounded-lg transition-colors"
                    >
                        <PencilIcon className="w-4 h-4" />
                    </button>
                    <button
                        onClick={() => onDelete(client.id)}
                        className="p-1.5 text-red-400 hover:bg-red-500/20 rounded-lg transition-colors"
                    >
                        <TrashIcon className="w-4 h-4" />
                    </button>
                </div>
            </div>

            <div className="space-y-2">
                <div className="flex items-center gap-2 text-white/60">
                    <PhoneIcon className="w-4 h-4 text-purple-400" />
                    <span className="text-sm">{client.phone}</span>
                </div>
                <div className="flex items-center gap-2 text-white/60">
                    <EnvelopeIcon className="w-4 h-4 text-purple-400" />
                    <span className="text-sm">{client.email}</span>
                </div>
            </div>

            <div className="mt-4 pt-3 border-t border-white/10">
                <div className="flex items-center gap-2">
                    <CameraIcon className="w-4 h-4 text-purple-400" />
                    <p className="text-sm font-medium text-white/60">
                        Фотосессий: <span className="text-purple-400 font-bold">{sessionsCount}</span>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default ClientCard;