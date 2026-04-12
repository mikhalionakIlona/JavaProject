import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { motion, AnimatePresence } from 'framer-motion';
import { sessionService } from '../../services/sessionService';
import { clientService } from '../../services/clientService';
import { photographerService } from '../../services/photographerService';
import { serviceService } from '../../services/serviceService';
import { PhotoSession, PhotoSessionCreateDto } from '../../types';
import { useToast } from '../../hooks/useToast';
import LoadingSpinner from '../common/LoadingSpinner';
import {
    Plus,
    Pencil,
    Trash2,
    Calendar,
    User,
    Camera,
    Sparkles,
    X,
    Search,
    Filter,
    Clock,
    CheckCircle,
    Heart
} from 'lucide-react';

const ServiceTypePricesBYN: Record<string, number> = {
    'WEDDING': 300,
    'PORTRAIT': 100,
    'PRODUCT': 50,
    'CORPORATE': 200,
    'FAMILY': 150,
};

const getRussianServiceName = (englishName: string): string => {
    if (!englishName) return 'Не указана';
    const names: Record<string, string> = {
        'WEDDING': 'Свадебная съемка',
        'PORTRAIT': 'Портретная съемка',
        'PRODUCT': 'Предметная съемка',
        'CORPORATE': 'Корпоративная съемка',
        'FAMILY': 'Семейная съемка',
    };
    return names[englishName] || englishName;
};

const calculateTotalPrice = (servicePrice: number, hourlyRate: number, hours: number = 2): number => {
    return servicePrice + (hourlyRate * hours);
};

const SessionList: React.FC = () => {
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [selectedSession, setSelectedSession] = useState<PhotoSession | null>(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('all');
    const [serviceFilter, setServiceFilter] = useState('all');
    const [formData, setFormData] = useState({
        date: '',
        clientId: 0,
        photographerId: 0,
        serviceId: 0,
    });
    const [editFormData, setEditFormData] = useState({
        date: '',
        clientId: 0,
        photographerId: 0,
        serviceId: 0,
    });

    const { showSuccess, showError } = useToast();
    const queryClient = useQueryClient();

    const { data: sessions, isLoading } = useQuery<PhotoSession[]>({
        queryKey: ['sessions'],
        queryFn: sessionService.getAll,
    });

    const { data: clients } = useQuery({
        queryKey: ['clients'],
        queryFn: clientService.getAll,
    });

    const { data: photographers } = useQuery({
        queryKey: ['photographers'],
        queryFn: photographerService.getAll,
    });

    const { data: services } = useQuery({
        queryKey: ['services'],
        queryFn: serviceService.getAll,
    });

    const createMutation = useMutation({
        mutationFn: sessionService.create,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['sessions'] });
            showSuccess('Фотосессия успешно создана');
            setIsCreateModalOpen(false);
            setFormData({ date: '', clientId: 0, photographerId: 0, serviceId: 0 });
        },
        onError: (error: Error) => {
            showError(error.message || 'Ошибка при создании фотосессии');
        },
    });

    const updateMutation = useMutation({
        mutationFn: ({ id, data }: { id: number; data: Partial<PhotoSessionCreateDto> }) =>
            sessionService.update(id, data),
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['sessions'] });
            showSuccess('Фотосессия успешно обновлена');
            setIsEditModalOpen(false);
            setSelectedSession(null);
        },
        onError: (error: Error) => {
            showError(error.message || 'Ошибка при обновлении фотосессии');
        },
    });

    const deleteMutation = useMutation({
        mutationFn: sessionService.delete,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['sessions'] });
            showSuccess('Фотосессия успешно удалена');
        },
    });

    const handleCreate = (e: React.FormEvent) => {
        e.preventDefault();
        if (formData.clientId === 0 || formData.photographerId === 0 || formData.serviceId === 0) {
            showError('Заполните все поля');
            return;
        }
        createMutation.mutate(formData);
    };

    const handleUpdate = (e: React.FormEvent) => {
        e.preventDefault();
        if (selectedSession) {
            updateMutation.mutate({
                id: selectedSession.id,
                data: editFormData,
            });
        }
    };

    const openEditModal = (session: PhotoSession) => {
        setSelectedSession(session);
        setEditFormData({
            date: session.date.slice(0, 16),
            clientId: session.clientId,
            photographerId: session.photographerId,
            serviceId: session.serviceId,
        });
        setIsEditModalOpen(true);
    };

    const getStatusBadge = (date: string) => {
        const sessionDate = new Date(date);
        const now = new Date();
        if (sessionDate > now) {
            return { text: 'Запланирована', icon: Clock, className: 'bg-amber-500/20 text-amber-400 border border-amber-500/30' };
        }
        if (sessionDate < now) {
            return { text: 'Завершена', icon: CheckCircle, className: 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' };
        }
        return { text: 'Сегодня', icon: Heart, className: 'bg-rose-500/20 text-rose-400 border border-rose-500/30' };
    };

    const getClientName = (clientId: number) => {
        const client = clients?.find(c => c.id === clientId);
        return client ? `${client.firstName} ${client.lastName}` : 'Не указан';
    };

    const getPhotographerName = (photographerId: number) => {
        const photographer = photographers?.find(p => p.id === photographerId);
        return photographer ? `${photographer.firstName} ${photographer.lastName}` : 'Не указан';
    };

    const getPhotographerRate = (photographerId: number) => {
        const photographer = photographers?.find(p => p.id === photographerId);
        return photographer?.hourlyRate || 0;
    };

    const getServiceName = (serviceId: number) => {
        const service = services?.find(s => s.id === serviceId);
        return service ? getRussianServiceName(service.serviceType) : 'Не указана';
    };

    const getServicePrice = (serviceId: number) => {
        const service = services?.find(s => s.id === serviceId);
        if (service?.serviceType) {
            return ServiceTypePricesBYN[service.serviceType] || 0;
        }
        return 0;
    };

    const getSessionPrice = (session: PhotoSession) => {
        const servicePrice = getServicePrice(session.serviceId);
        const photographerRate = getPhotographerRate(session.photographerId);
        if (session.totalPrice && session.totalPrice > 0) {
            return session.totalPrice;
        }
        return calculateTotalPrice(servicePrice, photographerRate);
    };

    const formatDate = (dateString: string) => {
        try {
            return new Date(dateString).toLocaleDateString('ru-RU', {
                day: 'numeric',
                month: 'long',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch {
            return dateString;
        }
    };

    const filteredSessions = sessions?.filter(session => {
        const matchesSearch =
            getClientName(session.clientId).toLowerCase().includes(searchTerm.toLowerCase()) ||
            getPhotographerName(session.photographerId).toLowerCase().includes(searchTerm.toLowerCase()) ||
            getServiceName(session.serviceId).toLowerCase().includes(searchTerm.toLowerCase());

        const sessionDate = new Date(session.date);
        const now = new Date();
        let matchesStatus = true;
        if (statusFilter === 'upcoming') matchesStatus = sessionDate > now;
        if (statusFilter === 'completed') matchesStatus = sessionDate < now;
        if (statusFilter === 'today') matchesStatus = sessionDate.toDateString() === now.toDateString();

        const matchesService = serviceFilter === 'all' || session.serviceId.toString() === serviceFilter;

        return matchesSearch && matchesStatus && matchesService;
    });

    const uniqueServices = services?.map(s => ({ id: s.id, name: getRussianServiceName(s.serviceType) })) || [];

    if (isLoading) return <LoadingSpinner />;

    return (
        <div className="min-h-screen pt-20 px-4">
            <div className="container mx-auto">
                {}
                <div className="flex justify-between items-center mb-8 flex-wrap gap-4">
                    <div>
                        <h1 className="text-4xl font-bold bg-gradient-to-r from-rose-400 via-purple-400 to-indigo-400 bg-clip-text text-transparent">
                            Фотосессии
                        </h1>
                        <p className="text-purple-300/70 mt-1">Управление фотосессиями</p>
                    </div>
                    <button
                        onClick={() => setIsCreateModalOpen(true)}
                        className="bg-gradient-to-r from-rose-500 to-purple-600 hover:from-rose-600 hover:to-purple-700 text-white px-5 py-2.5 rounded-xl font-semibold transition-all duration-300 flex items-center gap-2 shadow-lg shadow-rose-500/25"
                    >
                        <Plus className="w-5 h-5" />
                        Создать фотосессию
                    </button>
                </div>

                {}
                <div className="glass-card p-6 mb-6">
                    <div className="flex items-center gap-2 mb-4">
                        <Filter className="w-5 h-5 text-purple-400" />
                        <h3 className="text-white font-semibold">Фильтры</h3>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                        <div className="relative">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-purple-400" />
                            <input
                                type="text"
                                placeholder="Поиск по клиенту, фотографу, услуге..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="w-full pl-9 pr-3 py-2 bg-slate-800 border border-purple-500/30 rounded-lg text-white text-sm focus:outline-none focus:border-rose-500"
                            />
                        </div>
                        <select
                            value={statusFilter}
                            onChange={(e) => setStatusFilter(e.target.value)}
                            className="w-full px-3 py-2 bg-slate-800 border border-purple-500/30 rounded-lg text-white text-sm focus:outline-none focus:border-rose-500"
                        >
                            <option value="all">Все статусы</option>
                            <option value="upcoming">Запланированные</option>
                            <option value="today">Сегодня</option>
                            <option value="completed">Завершенные</option>
                        </select>
                        <select
                            value={serviceFilter}
                            onChange={(e) => setServiceFilter(e.target.value)}
                            className="w-full px-3 py-2 bg-slate-800 border border-purple-500/30 rounded-lg text-white text-sm focus:outline-none focus:border-rose-500"
                        >
                            <option value="all">Все услуги</option>
                            {uniqueServices.map((service) => (
                                <option key={service.id} value={service.id.toString()}>
                                    {service.name}
                                </option>
                            ))}
                        </select>
                        {(searchTerm || statusFilter !== 'all' || serviceFilter !== 'all') && (
                            <button
                                onClick={() => {
                                    setSearchTerm('');
                                    setStatusFilter('all');
                                    setServiceFilter('all');
                                }}
                                className="px-3 py-2 bg-rose-500/20 text-rose-400 rounded-lg hover:bg-rose-500/30 transition text-sm"
                            >
                                Сбросить фильтры
                            </button>
                        )}
                    </div>
                </div>

                {}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {filteredSessions?.map((session) => {
                        const status = getStatusBadge(session.date);
                        const StatusIcon = status.icon;
                        const price = getSessionPrice(session);
                        const serviceName = getServiceName(session.serviceId);
                        const sessionNumber = session.id;

                        return (
                            <motion.div
                                key={session.id}
                                initial={{ opacity: 0, y: 20 }}
                                animate={{ opacity: 1, y: 0 }}
                                whileHover={{ scale: 1.02, y: -5 }}
                                className="glass-card p-6 cursor-pointer group"
                            >
                                <div className="flex items-center justify-between mb-4">
                                    <div className="flex items-center gap-3">
                                        <div className="w-12 h-12 bg-gradient-to-br from-purple-500/20 to-pink-500/20 rounded-xl flex items-center justify-center">
                                            <Camera className="w-6 h-6 text-purple-400" />
                                        </div>
                                        <div>
                                            <h3 className="font-semibold text-white text-lg">
                                                Фотосессия #{sessionNumber}
                                            </h3>
                                            <div className="flex items-center gap-2 mt-1">
                        <span className={`inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full ${status.className}`}>
                          <StatusIcon className="w-3 h-3" />
                            {status.text}
                        </span>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="flex gap-2 opacity-0 group-hover:opacity-100 transition-all duration-300">
                                        <button
                                            onClick={() => openEditModal(session)}
                                            className="p-2 bg-yellow-500/20 text-yellow-300 rounded-lg hover:bg-yellow-500/30 transition"
                                        >
                                            <Pencil className="w-4 h-4" />
                                        </button>
                                        <button
                                            onClick={() => deleteMutation.mutate(session.id)}
                                            className="p-2 bg-red-500/20 text-red-300 rounded-lg hover:bg-red-500/30 transition"
                                        >
                                            <Trash2 className="w-4 h-4" />
                                        </button>
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <div className="flex items-center gap-2 text-white/60">
                                        <User className="w-4 h-4 text-purple-400" />
                                        <span className="text-sm">Клиент: <strong className="text-white">{getClientName(session.clientId)}</strong></span>
                                    </div>
                                    <div className="flex items-center gap-2 text-white/60">
                                        <Camera className="w-4 h-4 text-purple-400" />
                                        <span className="text-sm">Фотограф: <strong className="text-white">{getPhotographerName(session.photographerId)}</strong></span>
                                    </div>
                                    <div className="flex items-center gap-2 text-white/60">
                                        <Sparkles className="w-4 h-4 text-purple-400" />
                                        <span className="text-sm">Услуга: <strong className="text-white">{serviceName}</strong></span>
                                    </div>
                                    <div className="flex items-center gap-2 text-white/60">
                                        <Calendar className="w-4 h-4 text-purple-400" />
                                        <span className="text-sm">Дата: <strong className="text-white">{formatDate(session.date)}</strong></span>
                                    </div>
                                </div>

                                <div className="mt-4 pt-3 border-t border-white/10">
                                    <div className="flex justify-between items-center">
                                        <span className="text-sm text-white/40">Стоимость:</span>
                                        <span className="text-xl font-bold text-green-400">{price.toLocaleString()} BYN</span>
                                    </div>
                                </div>
                            </motion.div>
                        );
                    })}
                </div>

                {filteredSessions?.length === 0 && (
                    <div className="glass-card p-12 text-center">
                        <div className="text-6xl mb-4">📸</div>
                        <h3 className="text-xl font-semibold text-white mb-2">Нет фотосессий</h3>
                        <p className="text-white/40 mb-4">Создайте первую фотосессию или измените параметры фильтрации</p>
                        <button
                            onClick={() => setIsCreateModalOpen(true)}
                            className="bg-gradient-to-r from-rose-500 to-purple-600 hover:from-rose-600 hover:to-purple-700 text-white px-5 py-2 rounded-xl font-semibold transition-all duration-300 flex items-center gap-2 mx-auto"
                        >
                            <Plus className="w-4 h-4" />
                            Создать фотосессию
                        </button>
                    </div>
                )}

                {}
                <AnimatePresence>
                    {isCreateModalOpen && (
                        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center z-50 p-4">
                            <motion.div
                                initial={{ opacity: 0, scale: 0.95 }}
                                animate={{ opacity: 1, scale: 1 }}
                                exit={{ opacity: 0, scale: 0.95 }}
                                className="bg-white rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden"
                            >
                                <div className="flex justify-between items-center p-6 border-b border-gray-200 bg-gradient-to-r from-gray-50 to-white">
                                    <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
                                        <Camera className="w-5 h-5 text-rose-500" />
                                        Создание фотосессии
                                    </h2>
                                    <button
                                        onClick={() => setIsCreateModalOpen(false)}
                                        className="p-2 hover:bg-gray-100 rounded-full transition"
                                    >
                                        <X className="w-5 h-5 text-gray-500" />
                                    </button>
                                </div>

                                <form onSubmit={handleCreate} className="p-6 space-y-4">
                                    <div>
                                        <label htmlFor="createDate" className="block text-sm font-medium text-gray-700 mb-2">
                                            <Calendar className="w-4 h-4 inline mr-1 text-gray-500" />
                                            Дата и время *
                                        </label>
                                        <input
                                            id="createDate"
                                            type="datetime-local"
                                            required
                                            value={formData.date}
                                            onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                                            className="w-full px-4 py-2 border border-gray-300 rounded-lg text-gray-700 focus:ring-2 focus:ring-rose-500 focus:border-transparent"
                                            min={new Date().toISOString().slice(0, 16)}
                                        />
                                    </div>

                                    <div>
                                        <label htmlFor="createClientId" className="block text-sm font-medium text-gray-700 mb-2">
                                            <User className="w-4 h-4 inline mr-1 text-gray-500" />
                                            Клиент *
                                        </label>
                                        <select
                                            id="createClientId"
                                            required
                                            value={formData.clientId}
                                            onChange={(e) => setFormData({ ...formData, clientId: Number.parseInt(e.target.value, 10) })}
                                            className="w-full px-4 py-2 border border-gray-300 rounded-lg text-gray-700 focus:ring-2 focus:ring-rose-500 focus:border-transparent"
                                        >
                                            <option value={0}>Выберите клиента</option>
                                            {clients?.map((client) => (
                                                <option key={client.id} value={client.id}>
                                                    {client.firstName} {client.lastName} - {client.phone}
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    <div>
                                        <label htmlFor="createPhotographerId" className="block text-sm font-medium text-gray-700 mb-2">
                                            <Camera className="w-4 h-4 inline mr-1 text-gray-500" />
                                            Фотограф *
                                        </label>
                                        <select
                                            id="createPhotographerId"
                                            required
                                            value={formData.photographerId}
                                            onChange={(e) => setFormData({ ...formData, photographerId: Number.parseInt(e.target.value, 10) })}
                                            className="w-full px-4 py-2 border border-gray-300 rounded-lg text-gray-700 focus:ring-2 focus:ring-rose-500 focus:border-transparent"
                                        >
                                            <option value={0}>Выберите фотографа</option>
                                            {photographers?.map((photographer) => (
                                                <option key={photographer.id} value={photographer.id}>
                                                    {photographer.firstName} {photographer.lastName} - {photographer.hourlyRate} BYN/час
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    <div>
                                        <label htmlFor="createServiceId" className="block text-sm font-medium text-gray-700 mb-2">
                                            <Sparkles className="w-4 h-4 inline mr-1 text-gray-500" />
                                            Услуга *
                                        </label>
                                        <select
                                            id="createServiceId"
                                            required
                                            value={formData.serviceId}
                                            onChange={(e) => setFormData({ ...formData, serviceId: Number.parseInt(e.target.value, 10) })}
                                            className="w-full px-4 py-2 border border-gray-300 rounded-lg text-gray-700 focus:ring-2 focus:ring-rose-500 focus:border-transparent"
                                        >
                                            <option value={0}>Выберите услугу</option>
                                            {services?.map((service) => (
                                                <option key={service.id} value={service.id}>
                                                    {getRussianServiceName(service.serviceType)} - {getServicePrice(service.id).toLocaleString()} BYN
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    <div className="flex gap-3 justify-end pt-4">
                                        <button
                                            type="button"
                                            onClick={() => setIsCreateModalOpen(false)}
                                            className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition"
                                        >
                                            Отмена
                                        </button>
                                        <button
                                            type="submit"
                                            disabled={createMutation.isPending}
                                            className="px-4 py-2 bg-gradient-to-r from-rose-500 to-purple-600 text-white rounded-lg hover:from-rose-600 hover:to-purple-700 transition-all disabled:opacity-50"
                                        >
                                            {createMutation.isPending ? 'Создание...' : 'Создать фотосессию'}
                                        </button>
                                    </div>
                                </form>
                            </motion.div>
                        </div>
                    )}
                </AnimatePresence>

                {}
                <AnimatePresence>
                    {isEditModalOpen && selectedSession && (
                        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center z-50 p-4">
                            <motion.div
                                initial={{ opacity: 0, scale: 0.95 }}
                                animate={{ opacity: 1, scale: 1 }}
                                exit={{ opacity: 0, scale: 0.95 }}
                                className="bg-white rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden"
                            >
                                <div className="flex justify-between items-center p-6 border-b border-gray-200 bg-gradient-to-r from-gray-50 to-white">
                                    <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
                                        <Pencil className="w-5 h-5 text-rose-500" />
                                        Редактирование фотосессии
                                    </h2>
                                    <button
                                        onClick={() => setIsEditModalOpen(false)}
                                        className="p-2 hover:bg-gray-100 rounded-full transition"
                                    >
                                        <X className="w-5 h-5 text-gray-500" />
                                    </button>
                                </div>

                                <form onSubmit={handleUpdate} className="p-6 space-y-4">
                                    <div>
                                        <label htmlFor="editDate" className="block text-sm font-medium text-gray-700 mb-2">
                                            <Calendar className="w-4 h-4 inline mr-1 text-gray-500" />
                                            Дата и время *
                                        </label>
                                        <input
                                            id="editDate"
                                            type="datetime-local"
                                            required
                                            value={editFormData.date}
                                            onChange={(e) => setEditFormData({ ...editFormData, date: e.target.value })}
                                            className="w-full px-4 py-2 border border-gray-300 rounded-lg text-gray-700 focus:ring-2 focus:ring-rose-500 focus:border-transparent"
                                        />
                                    </div>

                                    <div>
                                        <label htmlFor="editClientId" className="block text-sm font-medium text-gray-700 mb-2">
                                            <User className="w-4 h-4 inline mr-1 text-gray-500" />
                                            Клиент *
                                        </label>
                                        <select
                                            id="editClientId"
                                            required
                                            value={editFormData.clientId}
                                            onChange={(e) => setEditFormData({ ...editFormData, clientId: Number.parseInt(e.target.value, 10) })}
                                            className="w-full px-4 py-2 border border-gray-300 rounded-lg text-gray-700 focus:ring-2 focus:ring-rose-500 focus:border-transparent"
                                        >
                                            <option value={0}>Выберите клиента</option>
                                            {clients?.map((client) => (
                                                <option key={client.id} value={client.id}>
                                                    {client.firstName} {client.lastName} - {client.phone}
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    <div>
                                        <label htmlFor="editPhotographerId" className="block text-sm font-medium text-gray-700 mb-2">
                                            <Camera className="w-4 h-4 inline mr-1 text-gray-500" />
                                            Фотограф *
                                        </label>
                                        <select
                                            id="editPhotographerId"
                                            required
                                            value={editFormData.photographerId}
                                            onChange={(e) => setEditFormData({ ...editFormData, photographerId: Number.parseInt(e.target.value, 10) })}
                                            className="w-full px-4 py-2 border border-gray-300 rounded-lg text-gray-700 focus:ring-2 focus:ring-rose-500 focus:border-transparent"
                                        >
                                            <option value={0}>Выберите фотографа</option>
                                            {photographers?.map((photographer) => (
                                                <option key={photographer.id} value={photographer.id}>
                                                    {photographer.firstName} {photographer.lastName} - {photographer.hourlyRate} BYN/час
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    <div>
                                        <label htmlFor="editServiceId" className="block text-sm font-medium text-gray-700 mb-2">
                                            <Sparkles className="w-4 h-4 inline mr-1 text-gray-500" />
                                            Услуга *
                                        </label>
                                        <select
                                            id="editServiceId"
                                            required
                                            value={editFormData.serviceId}
                                            onChange={(e) => setEditFormData({ ...editFormData, serviceId: Number.parseInt(e.target.value, 10) })}
                                            className="w-full px-4 py-2 border border-gray-300 rounded-lg text-gray-700 focus:ring-2 focus:ring-rose-500 focus:border-transparent"
                                        >
                                            <option value={0}>Выберите услугу</option>
                                            {services?.map((service) => (
                                                <option key={service.id} value={service.id}>
                                                    {getRussianServiceName(service.serviceType)} - {getServicePrice(service.id).toLocaleString()} BYN
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    <div className="flex gap-3 justify-end pt-4">
                                        <button
                                            type="button"
                                            onClick={() => setIsEditModalOpen(false)}
                                            className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition"
                                        >
                                            Отмена
                                        </button>
                                        <button
                                            type="submit"
                                            disabled={updateMutation.isPending}
                                            className="px-4 py-2 bg-gradient-to-r from-rose-500 to-purple-600 text-white rounded-lg hover:from-rose-600 hover:to-purple-700 transition-all disabled:opacity-50"
                                        >
                                            {updateMutation.isPending ? 'Сохранение...' : 'Сохранить изменения'}
                                        </button>
                                    </div>
                                </form>
                            </motion.div>
                        </div>
                    )}
                </AnimatePresence>
            </div>
        </div>
    );
};

export { SessionList };