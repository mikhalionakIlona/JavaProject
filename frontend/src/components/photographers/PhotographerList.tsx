import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { photographerService } from '../../services/photographerService';
import { Photographer } from '../../types';
import { useToast } from '../../hooks/useToast';
import LoadingSpinner from '../common/LoadingSpinner';
import { Modal } from '../common/Modal';
import {
    Plus,
    Pencil,
    Trash2,
    Phone,
    DollarSign
} from 'lucide-react';

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

const PhotographerList: React.FC = () => {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        patronymic: '',
        phone: '',
        hourlyRate: 0,
    });
    const [editingId, setEditingId] = useState<number | null>(null);

    const { showSuccess, showError } = useToast();
    const queryClient = useQueryClient();

    const { data: photographers, isLoading } = useQuery<Photographer[]>({
        queryKey: ['photographers'],
        queryFn: photographerService.getAll,
    });

    const createMutation = useMutation({
        mutationFn: photographerService.create,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['photographers'] });
            showSuccess('Фотограф успешно добавлен');
            closeModal();
        },
        onError: (error: Error) => {
            showError(error.message || 'Ошибка при добавлении фотографа');
        },
    });

    const updateMutation = useMutation({
        mutationFn: ({ id, data }: { id: number; data: any }) =>
            photographerService.update(id, data),
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['photographers'] });
            showSuccess('Фотограф успешно обновлен');
            closeModal();
        },
    });

    const deleteMutation = useMutation({
        mutationFn: photographerService.delete,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['photographers'] });
            showSuccess('Фотограф успешно удален');
        },
    });

    const openCreateModal = () => {
        setEditingId(null);
        setFormData({ firstName: '', lastName: '', patronymic: '', phone: '', hourlyRate: 0 });
        setIsModalOpen(true);
    };

    const openEditModal = (photographer: Photographer) => {
        setEditingId(photographer.id);
        setFormData({
            firstName: photographer.firstName,
            lastName: photographer.lastName,
            patronymic: photographer.patronymic || '',
            phone: photographer.phone,
            hourlyRate: photographer.hourlyRate,
        });
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setIsModalOpen(false);
        setEditingId(null);
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (editingId) {
            updateMutation.mutate({ id: editingId, data: formData });
        } else {
            createMutation.mutate(formData);
        }
    };

    if (isLoading) return <LoadingSpinner />;

    return (
        <div className="min-h-screen pt-20 px-4">
            <div className="container mx-auto">
                <div className="flex justify-between items-center mb-8 flex-wrap gap-4">
                    <div>
                        <h1 className="text-4xl font-bold bg-gradient-to-r from-rose-400 via-purple-400 to-indigo-400 bg-clip-text text-transparent">
                            Фотографы
                        </h1>
                        <p className="text-purple-300/70 mt-1">Управление фотографами</p>
                    </div>
                    <button
                        onClick={openCreateModal}
                        className="bg-gradient-to-r from-rose-500 to-purple-600 hover:from-rose-600 hover:to-purple-700 text-white px-5 py-2.5 rounded-xl font-semibold transition-all duration-300 flex items-center gap-2 shadow-lg shadow-rose-500/25"
                    >
                        <Plus className="w-5 h-5" />
                        Добавить фотографа
                    </button>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {photographers?.map((photographer) => (
                        <motion.div
                            key={photographer.id}
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            whileHover={{ scale: 1.02, y: -5 }}
                            className="glass-card p-6 cursor-pointer group"
                        >
                            <div className="flex items-center justify-between mb-4">
                                <div className="flex items-center gap-4">
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
                                <div className="flex gap-2 opacity-0 group-hover:opacity-100 transition-all duration-300">
                                    <button
                                        onClick={() => openEditModal(photographer)}
                                        className="p-2 bg-yellow-500/20 text-yellow-300 rounded-lg hover:bg-yellow-500/30 transition"
                                    >
                                        <Pencil className="w-4 h-4" />
                                    </button>
                                    <button
                                        onClick={() => deleteMutation.mutate(photographer.id)}
                                        className="p-2 bg-red-500/20 text-red-300 rounded-lg hover:bg-red-500/30 transition"
                                    >
                                        <Trash2 className="w-4 h-4" />
                                    </button>
                                </div>
                            </div>

                            <div className="space-y-2 mt-3">
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

                {photographers?.length === 0 && (
                    <div className="glass-card p-12 text-center">
                        <div className="text-6xl mb-4">📷</div>
                        <h3 className="text-xl font-semibold text-white mb-2">Нет фотографов</h3>
                        <p className="text-white/40">Добавьте первого фотографа</p>
                    </div>
                )}

                <Modal isOpen={isModalOpen} onClose={closeModal} title={editingId ? 'Редактирование фотографа' : 'Добавление фотографа'}>
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-1">Имя *</label>
                                <input
                                    id="firstName"
                                    type="text"
                                    required
                                    value={formData.firstName}
                                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-rose-500"
                                    placeholder="Иван"
                                />
                            </div>
                            <div>
                                <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-1">Фамилия *</label>
                                <input
                                    id="lastName"
                                    type="text"
                                    required
                                    value={formData.lastName}
                                    onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-rose-500"
                                    placeholder="Иванов"
                                />
                            </div>
                        </div>
                        <div>
                            <label htmlFor="patronymic" className="block text-sm font-medium text-gray-700 mb-1">Отчество</label>
                            <input
                                id="patronymic"
                                type="text"
                                value={formData.patronymic}
                                onChange={(e) => setFormData({ ...formData, patronymic: e.target.value })}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-rose-500"
                                placeholder="Иванович"
                            />
                        </div>
                        <div>
                            <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-1">Телефон *</label>
                            <input
                                id="phone"
                                type="tel"
                                required
                                value={formData.phone}
                                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-rose-500"
                                placeholder="+375291234567"
                            />
                        </div>
                        <div>
                            <label htmlFor="hourlyRate" className="block text-sm font-medium text-gray-700 mb-1">Почасовая ставка (BYN) *</label>
                            <input
                                id="hourlyRate"
                                type="number"
                                required
                                min="0"
                                step="100"
                                value={formData.hourlyRate}
                                onChange={(e) => setFormData({ ...formData, hourlyRate: Number.parseFloat(e.target.value) })}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-rose-500"
                                placeholder="0"
                            />
                        </div>
                        <div className="flex gap-3 justify-end pt-4">
                            <button type="button" onClick={closeModal} className="px-4 py-2 bg-gray-200 rounded-lg">Отмена</button>
                            <button type="submit" className="px-4 py-2 btn-primary">Сохранить</button>
                        </div>
                    </form>
                </Modal>
            </div>
        </div>
    );
};

export { PhotographerList };