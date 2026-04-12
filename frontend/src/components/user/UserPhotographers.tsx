import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { photographerService } from '../../services/photographerService';
import { Phone, DollarSign } from 'lucide-react';
import LoadingSpinner from '../common/LoadingSpinner';

const photographerPhotos: { [key: number]: string } = {
    1: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&h=150&fit=crop',
    2: 'https://images.unsplash.com/photo-1568602471122-7832951cc4c5?w=150&h=150&fit=crop',
    3: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&h=150&fit=crop',
    4: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&h=150&fit=crop',
    5: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop',
};

const defaultPhoto = 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&h=150&fit=crop';

const getPhotographerPhoto = (id: number): string => {
    return photographerPhotos[id] || defaultPhoto;
};

const UserPhotographers: React.FC = () => {
    const { data: photographers, isLoading } = useQuery({
        queryKey: ['photographers'],
        queryFn: photographerService.getAll,
    });

    if (isLoading) return <LoadingSpinner />;

    if (!photographers?.length) {
        return (
            <div className="glass-card p-12 text-center">
                <div className="text-6xl mb-4">📷</div>
                <h3 className="text-xl font-semibold text-white mb-2">Нет фотографов</h3>
                <p className="text-white/40">Фотографы будут добавлены позже</p>
            </div>
        );
    }

    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {photographers?.map((photographer, idx) => (
                <motion.div
                    key={photographer.id}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: idx * 0.05 }}
                    whileHover={{ scale: 1.02, y: -5 }}
                    className="glass-card p-6 cursor-pointer group"
                >
                    <div className="flex items-center gap-4 mb-4">
                        <div className="relative">
                            <img
                                src={getPhotographerPhoto(photographer.id)}
                                alt={`${photographer.firstName} ${photographer.lastName}`}
                                className="w-16 h-16 rounded-full object-cover border-2 border-purple-500/50"
                            />
                            <div className="absolute -bottom-1 -right-1 w-5 h-5 bg-green-500 rounded-full border-2 border-slate-800"></div>
                        </div>
                        <div>
                            <h3 className="font-semibold text-white text-lg">
                                {photographer.firstName} {photographer.lastName}
                            </h3>
                            {photographer.patronymic && (
                                <p className="text-xs text-purple-300/60">{photographer.patronymic}</p>
                            )}
                        </div>
                    </div>
                    <div className="space-y-2">
                        <div className="flex items-center gap-2 text-white/60">
                            <Phone className="w-4 h-4 text-purple-400" />
                            <span className="text-sm">{photographer.phone}</span>
                        </div>
                        <div className="flex items-center gap-2 text-white/60">
                            <DollarSign className="w-4 h-4 text-green-400" />
                            <span className="text-sm">Ставка: <strong className="text-green-400">{photographer.hourlyRate} BYN/час</strong></span>
                        </div>
                    </div>
                </motion.div>
            ))}
        </div>
    );
};

export default UserPhotographers;