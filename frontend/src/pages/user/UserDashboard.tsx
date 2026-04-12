import React, { useState, useEffect, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '../../store/authStore';
import { sessionService } from '../../services/sessionService';
import { photographerService } from '../../services/photographerService';
import { serviceService } from '../../services/serviceService';
import { clientService } from '../../services/clientService';
import { motion, AnimatePresence } from 'framer-motion';
import {
    Calendar,
    Camera,
    Sparkles,
    Plus,
    X,
    Clock,
    Image as ImageIcon,
    Award
} from 'lucide-react';
import toast from 'react-hot-toast';

// Цены на услуги в BYN
const ServiceTypePricesBYN: Record<string, number> = {
    'WEDDING': 300,
    'PORTRAIT': 100,
    'PRODUCT': 50,
    'CORPORATE': 200,
    'FAMILY': 150,
};

// Статистическая карточка для пользователя
const UserStatCard: React.FC<{ label: string; value: string | number; icon: React.ElementType; delay: number; description?: string }> =
    ({ label, value, icon: Icon, delay, description }) => {
        return (
            <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay, duration: 0.5 }}
                whileHover={{ scale: 1.02, y: -5 }}
                className="glass-card p-6 cursor-pointer group"
            >
                <div className="flex items-center justify-between">
                    <div>
                        <p className="text-white/50 text-sm">{label}</p>
                        <p className="text-3xl font-bold gradient-text mt-2">{value}</p>
                        {description && (
                            <p className="text-xs text-white/40 mt-1">{description}</p>
                        )}
                    </div>
                    <div className="w-12 h-12 bg-gradient-to-br from-purple-500/20 to-pink-500/20 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
                        <Icon className="w-6 h-6 text-purple-400" />
                    </div>
                </div>
            </motion.div>
        );
    };

const UserDashboard: React.FC = () => {
    const { user } = useAuthStore();
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [clientId, setClientId] = useState<number | null>(null);
    const [clientData, setClientData] = useState<any>(null);
    const [formData, setFormData] = useState({
        date: '',
        photographerId: 0,
        serviceId: 0,
    });

    const queryClient = useQueryClient();

    const loadClientData = useCallback(async () => {
        if (user?.email && user?.role === 'user') {
            try {
                const clients = await clientService.getAll();
                const client = clients.find(c => c.email === user.email);
                if (client) {
                    setClientId(client.id);
                    setClientData(client);
                }
            } catch (error) {
                console.error('Error fetching client:', error);
            }
        }
    }, [user?.email, user?.role]);

    useEffect(() => {
        loadClientData().catch(error => {
            console.error('Error loading client data:', error);
        });
    }, [loadClientData]);

    const { data: allSessions, isLoading: sessionsLoading, refetch: refetchSessions } = useQuery({
        queryKey: ['sessions'],
        queryFn: sessionService.getAll,
    });

    const { data: photographers } = useQuery({
        queryKey: ['photographers'],
        queryFn: photographerService.getAll,
    });

    const { data: services } = useQuery({
        queryKey: ['services'],
        queryFn: serviceService.getAll,
    });

    const createSessionMutation = useMutation({
        mutationFn: sessionService.create,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['sessions'] });
            await refetchSessions();
            toast.success('Фотосессия успешно забронирована!');
            setIsModalOpen(false);
            setFormData({ date: '', photographerId: 0, serviceId: 0 });
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Ошибка при создании бронирования');
        },
    });

    const userSessions = allSessions?.filter(s => s.clientId === clientId) || [];

    const totalSessions = userSessions.length;
    const totalPhotos = userSessions.reduce((acc, s) => acc + (s.photos?.length || 0), 0);
    const upcomingSessions = userSessions.filter(s => new Date(s.date) > new Date()).length;
    const completedSessions = userSessions.filter(s => new Date(s.date) < new Date()).length;

    const stats = [
        { label: 'Мои фотосессии', value: totalSessions, icon: Calendar, delay: 0.1, description: 'Всего фотосессий' },
        { label: 'Всего фотографий', value: totalPhotos, icon: ImageIcon, delay: 0.2, description: 'Сделано фото' },
        { label: 'Предстоящие', value: upcomingSessions, icon: Clock, delay: 0.3, description: 'Запланировано' },
        { label: 'Завершенные', value: completedSessions, icon: Award, delay: 0.4, description: 'Проведено' },
    ];

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.date || formData.photographerId === 0 || formData.serviceId === 0) {
            toast.error('Заполните все поля');
            return;
        }
        if (!clientId) {
            toast.error('Клиент не найден');
            return;
        }
        const selectedDate = new Date(formData.date);
        if (selectedDate < new Date()) {
            toast.error('Дата должна быть в будущем');
            return;
        }
        createSessionMutation.mutate({
            date: formData.date,
            clientId: clientId,
            photographerId: formData.photographerId,
            serviceId: formData.serviceId,
        });
    };

    const getServicePrice = (serviceId: number) => {
        const service = services?.find(s => s.id === serviceId);
        return ServiceTypePricesBYN[service?.serviceType] || service?.basePrice || 0;
    };

    const getPhotographerRate = (photographerId: number) => {
        const photographer = photographers?.find(p => p.id === photographerId);
        return photographer?.hourlyRate || 0;
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('ru-RU', {
            day: 'numeric',
            month: 'long',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getSessionStatus = (dateString: string) => {
        const sessionDate = new Date(dateString);
        const now = new Date();

        if (sessionDate > now) {
            return { text: 'Запланирована', color: 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30' };
        }

        if (sessionDate < now) {
            return { text: 'Завершена', color: 'bg-green-500/20 text-green-400 border border-green-500/30' };
        }

        return { text: 'Сегодня', color: 'bg-rose-500/20 text-rose-400 border border-rose-500/30' };
    };

    const getWelcomeMessage = () => {
        if (user?.name) {
            return user.name;
        }
        if (clientData?.firstName) {
            return clientData.firstName;
        }
        return 'Гость';
    };

    const getBookingButtonContent = () => {
        if (createSessionMutation.isPending) {
            return (
                <span className="flex items-center gap-2">
                    <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    Бронирование...
                </span>
            );
        }
        return 'Забронировать';
    };

    const getEmptyStateContent = () => {
        return (
            <>
                <Calendar className="w-16 h-16 mx-auto mb-3 opacity-50" />
                <p className="text-lg">У вас пока нет фотосессий</p>
                <p className="text-sm mt-1">Нажмите кнопку "Забронировать фотосессию", чтобы создать первую</p>
                <button
                    onClick={() => setIsModalOpen(true)}
                    className="mt-4 text-rose-400 hover:text-rose-300 font-medium"
                >
                    Забронировать сейчас →
                </button>
            </>
        );
    };

    const getLoadingContent = () => {
        return (
            <>
                <div className="inline-block w-8 h-8 border-2 border-rose-500/30 border-t-rose-500 rounded-full animate-spin"></div>
                <p className="mt-2 text-white/60">Загрузка...</p>
            </>
        );
    };

    const renderSessionsContent = () => {
        if (sessionsLoading) {
            return <div className="text-center py-8">{getLoadingContent()}</div>;
        }

        if (userSessions.length === 0) {
            return <div className="text-center py-12 text-white/40">{getEmptyStateContent()}</div>;
        }

        return (
            <div className="space-y-4">
                {userSessions.map((session) => {
                    const status = getSessionStatus(session.date);
                    return (
                        <motion.div
                            key={session.id}
                            initial={{ opacity: 0, x: -20 }}
                            animate={{ opacity: 1, x: 0 }}
                            className="bg-white/5 rounded-xl p-4 hover:bg-white/10 transition-all border border-white/10"
                        >
                            <div className="flex flex-wrap justify-between items-start gap-4">
                                <div className="flex-1">
                                    <div className="flex items-center gap-3 flex-wrap mb-2">
                                        <p className="font-semibold text-white">Фотосессия #{session.id}</p>
                                        <span className={`text-xs px-2 py-0.5 rounded-full ${status.color}`}>
                                            {status.text}
                                        </span>
                                    </div>
                                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-sm text-white/60">
                                        <p className="flex items-center gap-1">
                                            <Calendar className="w-4 h-4 text-rose-400" />
                                            {formatDate(session.date)}
                                        </p>
                                        <p className="flex items-center gap-1">
                                            <Camera className="w-4 h-4 text-rose-400" />
                                            Фотограф: {session.photographerName || 'Не указан'}
                                        </p>
                                        <p className="flex items-center gap-1">
                                            <Sparkles className="w-4 h-4 text-rose-400" />
                                            Услуга: {session.serviceName || 'Не указана'}
                                        </p>
                                        <p className="flex items-center gap-1 font-medium text-green-400">
                                            💰 Стоимость: {session.totalPrice} BYN
                                        </p>
                                    </div>
                                </div>
                                <div className="text-right">
                                    <p className="text-xs text-white/40">
                                        Фото: {session.photos?.length || 0}
                                    </p>
                                </div>
                            </div>
                        </motion.div>
                    );
                })}
            </div>
        );
    };

    return (
        <div className="min-h-screen pt-20 px-4">
            <div className="container mx-auto">
                {/* Hero секция */}
                <motion.div
                    initial={{ opacity: 0, y: 30 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.6 }}
                    className="text-center mb-8"
                >
                    <motion.div
                        animate={{ scale: [1, 1.05, 1] }}
                        transition={{ duration: 2, repeat: Infinity }}
                        className="inline-block"
                    >
                        <div className="w-20 h-20 bg-gradient-to-br from-purple-500/20 to-pink-500/20 rounded-2xl flex items-center justify-center mx-auto border border-purple-500/30">
                            <Camera className="w-10 h-10 text-purple-400" />
                        </div>
                    </motion.div>
                    <h1 className="text-4xl font-bold gradient-text mt-4">Личный кабинет</h1>
                    <p className="text-white/50 mt-2">Управление фотосессиями</p>
                </motion.div>

                {/* Приветственная секция */}
                <motion.div
                    initial={{ opacity: 0, y: 30 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.6 }}
                    className="bg-gradient-to-r from-rose-500 to-purple-600 rounded-2xl p-8 text-white mb-8"
                >
                    <div className="flex justify-between items-center flex-wrap gap-4">
                        <div>
                            <h2 className="text-2xl font-bold mb-2">Добро пожаловать, {getWelcomeMessage()}!</h2>
                            <p className="text-white/80">Ваша персональная страница клиента фотостудии</p>
                            {clientData && (
                                <div className="mt-3 text-sm text-white/80">
                                    <p>📞 {clientData.phone}</p>
                                    <p>✉️ {clientData.email}</p>
                                </div>
                            )}
                        </div>
                        <button
                            onClick={() => setIsModalOpen(true)}
                            className="flex items-center gap-2 bg-white text-purple-600 px-5 py-2.5 rounded-xl font-semibold hover:bg-gray-100 transition-all shadow-md"
                        >
                            <Plus className="w-5 h-5" />
                            Забронировать фотосессию
                        </button>
                    </div>
                </motion.div>

                {/* Статистика */}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                    {stats.map((stat) => (
                        <UserStatCard key={stat.label} {...stat} />
                    ))}
                </div>

                {/* Список фотосессий */}
                <div className="glass-card p-6">
                    <h2 className="text-xl font-bold text-white mb-4 flex items-center gap-2">
                        <Calendar className="w-5 h-5 text-rose-400" />
                        Мои фотосессии
                    </h2>
                    {renderSessionsContent()}
                </div>

                {/* Модальное окно бронирования */}
                <AnimatePresence>
                    {isModalOpen && (
                        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center z-50 p-4">
                            <motion.div
                                initial={{ opacity: 0, scale: 0.95 }}
                                animate={{ opacity: 1, scale: 1 }}
                                exit={{ opacity: 0, scale: 0.95 }}
                                className="glass-card rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden"
                            >
                                <div className="flex justify-between items-center p-6 border-b border-white/10">
                                    <h2 className="text-xl font-bold text-white flex items-center gap-2">
                                        <Camera className="w-5 h-5 text-rose-400" />
                                        Забронировать фотосессию
                                    </h2>
                                    <button
                                        onClick={() => setIsModalOpen(false)}
                                        className="p-2 hover:bg-white/10 rounded-full transition"
                                    >
                                        <X className="w-5 h-5 text-white" />
                                    </button>
                                </div>

                                <form onSubmit={handleSubmit} className="p-6 space-y-4">
                                    <div>
                                        <label className="block text-sm font-medium text-white/70 mb-2">
                                            <Calendar className="w-4 h-4 inline mr-1" />
                                            Дата и время *
                                        </label>
                                        <input
                                            type="datetime-local"
                                            required
                                            value={formData.date}
                                            onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                                            className="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-xl text-white focus:outline-none focus:border-rose-500"
                                            min={new Date().toISOString().slice(0, 16)}
                                        />
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-white/70 mb-2">
                                            <Camera className="w-4 h-4 inline mr-1" />
                                            Выберите фотографа *
                                        </label>
                                        <select
                                            required
                                            value={formData.photographerId}
                                            onChange={(e) => setFormData({ ...formData, photographerId: Number.parseInt(e.target.value, 10) })}
                                            className="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-xl text-white focus:outline-none focus:border-rose-500"
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
                                        <label className="block text-sm font-medium text-white/70 mb-2">
                                            <Sparkles className="w-4 h-4 inline mr-1" />
                                            Выберите услугу *
                                        </label>
                                        <select
                                            required
                                            value={formData.serviceId}
                                            onChange={(e) => setFormData({ ...formData, serviceId: Number.parseInt(e.target.value, 10) })}
                                            className="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-xl text-white focus:outline-none focus:border-rose-500"
                                        >
                                            <option value={0}>Выберите услугу</option>
                                            {services?.map((service) => (
                                                <option key={service.id} value={service.id}>
                                                    {service.name} - {ServiceTypePricesBYN[service.serviceType] || service.basePrice} BYN
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    {formData.photographerId > 0 && formData.serviceId > 0 && (
                                        <div className="bg-white/5 rounded-xl p-4">
                                            <p className="text-sm font-medium text-white/70 mb-2">Предварительный расчет:</p>
                                            <div className="space-y-1 text-sm">
                                                <div className="flex justify-between">
                                                    <span className="text-white/40">Услуга:</span>
                                                    <span className="text-white">{getServicePrice(formData.serviceId)} BYN</span>
                                                </div>
                                                <div className="flex justify-between">
                                                    <span className="text-white/40">Работа фотографа (2 часа):</span>
                                                    <span className="text-white">{getPhotographerRate(formData.photographerId) * 2} BYN</span>
                                                </div>
                                                <div className="border-t pt-2 mt-2 flex justify-between font-bold">
                                                    <span>Итого:</span>
                                                    <span className="text-green-400 text-lg">
                                                        {getServicePrice(formData.serviceId) + getPhotographerRate(formData.photographerId) * 2} BYN
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                    )}

                                    <div className="flex gap-3 justify-end pt-4">
                                        <button
                                            type="button"
                                            onClick={() => setIsModalOpen(false)}
                                            className="px-4 py-2 bg-white/10 text-white rounded-xl hover:bg-white/20 transition"
                                        >
                                            Отмена
                                        </button>
                                        <button
                                            type="submit"
                                            disabled={createSessionMutation.isPending}
                                            className="px-4 py-2 bg-gradient-to-r from-rose-500 to-purple-600 text-white rounded-xl hover:from-rose-600 hover:to-purple-700 transition-all disabled:opacity-50"
                                        >
                                            {getBookingButtonContent()}
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

export { UserDashboard };