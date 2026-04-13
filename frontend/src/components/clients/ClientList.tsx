import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { clientService } from '../../services/clientService';
import { sessionService } from '../../services/sessionService';
import { Client, PhotoSession } from '../../types';
import { useToast } from '../../hooks/useToast';
import LoadingSpinner from '../common/LoadingSpinner';
import { Modal } from '../common/Modal';
import {
    Plus,
    Pencil,
    Trash2,
    Phone,
    Mail,
    Calendar,
    Eye,
    Search,
    X,
    Filter,
    Camera,
    Heart,
    User
} from 'lucide-react';

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

const ClientList: React.FC = () => {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isViewModalOpen, setIsViewModalOpen] = useState(false);
    const [selectedClient, setSelectedClient] = useState<Client | null>(null);
    const [clientSessions, setClientSessions] = useState<PhotoSession[]>([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [filterDate, setFilterDate] = useState('');
    const [filterService, setFilterService] = useState('');
    const [isLoadingSessions, setIsLoadingSessions] = useState(false);
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        phone: '',
        email: '',
    });
    const [editingId, setEditingId] = useState<number | null>(null);

    const { showSuccess, showError } = useToast();
    const queryClient = useQueryClient();

    const { data: clients, isLoading } = useQuery<Client[]>({
        queryKey: ['clients'],
        queryFn: clientService.getAll,
    });

    const { data: allSessions, refetch: refetchSessions } = useQuery<PhotoSession[]>({
        queryKey: ['sessions'],
        queryFn: sessionService.getAll,
    });

    const getClientSessionsCount = (clientId: number) => {
        return allSessions?.filter(session => session.clientId === clientId).length || 0;
    };

    const createMutation = useMutation({
        mutationFn: clientService.create,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['clients'] });
            showSuccess('Клиент успешно создан');
            closeModal();
        },
        onError: (error: Error) => {
            showError(error.message || 'Ошибка при создании клиента');
        },
    });

    const updateMutation = useMutation({
        mutationFn: ({ id, data }: { id: number; data: any }) =>
            clientService.update(id, data),
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['clients'] });
            showSuccess('Клиент успешно обновлен');
            closeModal();
        },
    });

    const deleteMutation = useMutation({
        mutationFn: clientService.delete,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['clients'] });
            showSuccess('Клиент успешно удален');
        },
    });

    const openCreateModal = () => {
        setEditingId(null);
        setFormData({ firstName: '', lastName: '', phone: '', email: '' });
        setIsModalOpen(true);
    };

    const openEditModal = (client: Client) => {
        setEditingId(client.id);
        setFormData({
            firstName: client.firstName,
            lastName: client.lastName,
            phone: client.phone,
            email: client.email,
        });
        setIsModalOpen(true);
    };

    const openViewModal = async (client: Client) => {
        setSelectedClient(client);
        setIsLoadingSessions(true);
        try {
            await refetchSessions();
            const sessions = allSessions?.filter(session => session.clientId === client.id) || [];
            setClientSessions(sessions);
            setIsViewModalOpen(true);
        } catch (error) {
            console.error('Error loading sessions:', error);
            showError('Ошибка загрузки фотосессий');
        } finally {
            setIsLoadingSessions(false);
        }
    };

    const closeModal = () => {
        setIsModalOpen(false);
        setEditingId(null);
        setFormData({ firstName: '', lastName: '', phone: '', email: '' });
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (editingId) {
            updateMutation.mutate({ id: editingId, data: formData });
        } else {
            createMutation.mutate(formData);
        }
    };

    const filteredClients = clients?.filter(client => {
        const fullName = `${client.firstName} ${client.lastName}`.toLowerCase();
        return fullName.includes(searchTerm.toLowerCase()) ||
            client.phone.includes(searchTerm) ||
            client.email.toLowerCase().includes(searchTerm.toLowerCase());
    });

    const getFilteredSessions = () => {
        if (!clientSessions || clientSessions.length === 0) return [];
        return clientSessions.filter(session => {
            const matchesDate = filterDate ? new Date(session.date).toDateString() === new Date(filterDate).toDateString() : true;
            const matchesService = filterService ? (session.serviceName || '').toLowerCase().includes(filterService.toLowerCase()) : true;
            return matchesDate && matchesService;
        });
    };

    const uniqueServices = [...new Set(clientSessions.map(s => s.serviceName).filter(Boolean))];
    const formatDate = (dateString: string) => {
        try {
            return new Date(dateString).toLocaleDateString('ru-RU', {
                day: 'numeric',
                month: 'long',
                year: 'numeric'
            });
        } catch {
            return dateString;
        }
    };

    if (isLoading) return <LoadingSpinner />;

    const filteredSessions = getFilteredSessions();

    return (
        <div className="min-h-screen pt-20 px-4">
            <div className="container mx-auto">
                {}
                <div className="flex justify-between items-center mb-8 flex-wrap gap-4">
                    <div>
                        <h1 className="text-4xl font-bold bg-gradient-to-r from-rose-400 via-purple-400 to-indigo-400 bg-clip-text text-transparent">
                            Клиенты
                        </h1>
                        <p className="text-purple-300/70 mt-1">Управление клиентами фотостудии</p>
                    </div>
                    <button
                        onClick={openCreateModal}
                        className="bg-gradient-to-r from-rose-500 to-purple-600 hover:from-rose-600 hover:to-purple-700 text-white px-5 py-2.5 rounded-xl font-semibold transition-all duration-300 flex items-center gap-2 shadow-lg shadow-rose-500/25"
                    >
                        <Plus className="w-5 h-5" />
                        Добавить клиента
                    </button>
                </div>

                {}
                <div className="mb-6">
                    <div className="relative group">
                        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-purple-400 group-focus-within:text-rose-400 transition-colors" />
                        <input
                            type="text"
                            placeholder="Поиск по имени, телефону или email..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-10 pr-4 py-3 bg-slate-800/50 backdrop-blur-sm border border-purple-500/30 rounded-xl text-white placeholder-purple-300/40 focus:outline-none focus:border-rose-500 focus:ring-2 focus:ring-rose-500/20 transition-all"
                        />
                    </div>
                </div>

                {}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {filteredClients?.map((client) => (
                        <motion.div
                            key={client.id}
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            whileHover={{ scale: 1.02, y: -5 }}
                            className="glass-card p-6 cursor-pointer group"
                        >
                            <div className="flex items-center justify-between mb-4">
                                <div className="flex items-center gap-3">
                                    <div className="w-12 h-12 bg-gradient-to-br from-purple-500/20 to-pink-500/20 rounded-xl flex items-center justify-center">
                                        <User className="w-6 h-6 text-purple-400" />
                                    </div>
                                    <div>
                                        <h3 className="font-semibold text-white text-lg">
                                            {client.lastName} {client.firstName}
                                        </h3>
                                    </div>
                                </div>
                                <div className="flex gap-2 opacity-0 group-hover:opacity-100 transition-all duration-300">
                                    <button
                                        onClick={() => openViewModal(client)}
                                        className="p-2 bg-blue-500/20 text-blue-300 rounded-lg hover:bg-blue-500/30 transition"
                                        title="Просмотр сессий"
                                    >
                                        <Eye className="w-4 h-4" />
                                    </button>
                                    <button
                                        onClick={() => openEditModal(client)}
                                        className="p-2 bg-yellow-500/20 text-yellow-300 rounded-lg hover:bg-yellow-500/30 transition"
                                    >
                                        <Pencil className="w-4 h-4" />
                                    </button>
                                    <button
                                        onClick={() => deleteMutation.mutate(client.id)}
                                        className="p-2 bg-red-500/20 text-red-300 rounded-lg hover:bg-red-500/30 transition"
                                    >
                                        <Trash2 className="w-4 h-4" />
                                    </button>
                                </div>
                            </div>

                            <div className="space-y-2">
                                <div className="flex items-center gap-2 text-white/60">
                                    <Phone className="w-4 h-4 text-purple-400" />
                                    <span className="text-sm">{client.phone}</span>
                                </div>
                                <div className="flex items-center gap-2 text-white/60">
                                    <Mail className="w-4 h-4 text-purple-400" />
                                    <span className="text-sm">{client.email}</span>
                                </div>
                            </div>

                            <div className="mt-4 pt-3 border-t border-white/10">
                                <p className="text-sm text-white/40 flex items-center gap-1">
                                    <Calendar className="w-4 h-4" />
                                    Фотосессий: <span className="text-purple-400 font-semibold">{getClientSessionsCount(client.id)}</span>
                                </p>
                            </div>
                        </motion.div>
                    ))}
                </div>

                {filteredClients?.length === 0 && (
                    <div className="glass-card p-12 text-center">
                        <div className="text-6xl mb-4">👥</div>
                        <h3 className="text-xl font-semibold text-white mb-2">Клиенты не найдены</h3>
                        <p className="text-white/40">Попробуйте изменить параметры поиска</p>
                    </div>
                )}

                {}
                {}
                <Modal isOpen={isModalOpen} onClose={closeModal} title={editingId ? 'Редактирование клиента' : 'Добавление клиента'}>
                    <form onSubmit={handleSubmit} className="booking-form">
                        <div className="form-grid">
                            <div className="form-group">
                                <label htmlFor="firstName" className="form-label">Имя *</label>
                                <input
                                    id="firstName"
                                    type="text"
                                    required
                                    value={formData.firstName}
                                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                                    className="form-input"
                                    placeholder="Иван"
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="lastName" className="form-label">Фамилия *</label>
                                <input
                                    id="lastName"
                                    type="text"
                                    required
                                    value={formData.lastName}
                                    onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                                    className="form-input"
                                    placeholder="Иванов"
                                />
                            </div>
                        </div>
                        <div className="form-group">
                            <label htmlFor="phone" className="form-label">Телефон *</label>
                            <input
                                id="phone"
                                type="tel"
                                required
                                value={formData.phone}
                                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                                className="form-input"
                                placeholder="+375291234567"
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="email" className="form-label">Email *</label>
                            <input
                                id="email"
                                type="email"
                                required
                                value={formData.email}
                                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                className="form-input"
                                placeholder="ivan@example.com"
                            />
                        </div>
                        <div className="form-actions">
                            <button type="button" onClick={closeModal} className="btn-cancel">
                                Отмена
                            </button>
                            <button type="submit" className="btn-save" disabled={createMutation.isPending || updateMutation.isPending}>
                                {createMutation.isPending || updateMutation.isPending ? 'Сохранение...' : 'Сохранить'}
                            </button>
                        </div>
                    </form>
                </Modal>

                {}
                <Modal
                    isOpen={isViewModalOpen}
                    onClose={() => {
                        setIsViewModalOpen(false);
                        setFilterDate('');
                        setFilterService('');
                        setClientSessions([]);
                    }}
                    title={`Фотосессии клиента: ${selectedClient?.lastName} ${selectedClient?.firstName}`}
                    size="lg"
                >
                    <div className="space-y-4">
                        {}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-3 pb-4 border-b border-rose-500/30">
                            <div className="relative">
                                <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-rose-400" />
                                <input
                                    type="date"
                                    value={filterDate}
                                    onChange={(e) => setFilterDate(e.target.value)}
                                    className="w-full pl-9 pr-3 py-2 bg-slate-800 border border-rose-500/30 rounded-lg text-white text-sm focus:outline-none focus:border-rose-500"
                                />
                            </div>
                            <div className="relative">
                                <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-rose-400" />
                                <select
                                    value={filterService}
                                    onChange={(e) => setFilterService(e.target.value)}
                                    className="w-full pl-9 pr-3 py-2 bg-slate-800 border border-rose-500/30 rounded-lg text-white text-sm focus:outline-none focus:border-rose-500 appearance-none cursor-pointer"
                                >
                                    <option value="">Все услуги</option>
                                    {uniqueServices.map((service) => (
                                        <option key={service} value={service}>{service}</option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        {}
                        {(filterDate || filterService) && (
                            <div className="flex justify-end">
                                <button
                                    onClick={() => {
                                        setFilterDate('');
                                        setFilterService('');
                                    }}
                                    className="text-sm text-rose-400 hover:text-rose-300 flex items-center gap-1"
                                >
                                    <X className="w-3 h-3" />
                                    Сбросить фильтры
                                </button>
                            </div>
                        )}

                        {}
                        {isLoadingSessions && (
                            <div className="flex justify-center py-8">
                                <div className="w-8 h-8 border-2 border-rose-500/30 border-t-rose-500 rounded-full animate-spin"></div>
                            </div>
                        )}

                        {}
                        {!isLoadingSessions && filteredSessions.length === 0 && (
                            <div className="text-center py-8 text-white/40">
                                <Camera className="w-12 h-12 mx-auto mb-2 opacity-50 text-rose-400" />
                                <p>У клиента нет фотосессий</p>
                            </div>
                        )}

                        {!isLoadingSessions && filteredSessions.length > 0 && (
                            <div className="space-y-3 max-h-96 overflow-y-auto">
                                {filteredSessions.map((session) => {
                                    const price = session.totalPrice || 0;
                                    const serviceName = getRussianServiceName(session.serviceName || '');

                                    return (
                                        <motion.div
                                            key={session.id}
                                            initial={{ opacity: 0, x: -20 }}
                                            animate={{ opacity: 1, x: 0 }}
                                            className="bg-gradient-to-r from-rose-900/50 to-purple-900/50 rounded-xl p-4 hover:from-rose-900/70 hover:to-purple-900/70 transition-all border border-rose-500/30"
                                        >
                                            <div className="flex justify-between items-start">
                                                <div className="flex-1">
                                                    <div className="flex items-center gap-2 mb-2">
                                                        <Heart className="w-4 h-4 text-rose-400" />
                                                        <p className="font-semibold text-white text-lg">
                                                            {serviceName}
                                                        </p>
                                                    </div>
                                                    <p className="text-sm text-gray-300 mt-2 flex items-center gap-2">
                                                        <Calendar className="w-4 h-4 text-rose-400" />
                                                        {formatDate(session.date)}
                                                    </p>
                                                    <p className="text-sm text-gray-300 mt-1 flex items-center gap-2">
                                                        <Camera className="w-4 h-4 text-rose-400" />
                                                        Фотограф: {session.photographerName || 'Не указан'}
                                                    </p>
                                                </div>
                                                <div className="text-right">
                                                    <p className="text-xl font-bold text-rose-400">
                                                        {price.toLocaleString()} BYN
                                                    </p>
                                                </div>
                                            </div>
                                        </motion.div>
                                    );
                                })}
                            </div>
                        )}

                        {}
                        {!isLoadingSessions && filteredSessions.length > 0 && (
                            <div className="pt-3 border-t border-rose-500/30">
                                <p className="text-sm text-gray-400">
                                    Всего фотосессий: {filteredSessions.length}
                                </p>
                            </div>
                        )}
                    </div>
                </Modal>
            </div>
        </div>
    );
};

export { ClientList };