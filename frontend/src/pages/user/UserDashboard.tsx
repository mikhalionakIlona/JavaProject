import React, { useState, useEffect, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '../../store/authStore';
import { sessionService } from '../../services/sessionService';
import { photographerService } from '../../services/photographerService';
import { serviceService } from '../../services/serviceService';
import { clientService } from '../../services/clientService';
import { motion } from 'framer-motion';
import {
    Calendar,
    Camera,
    Sparkles,
    Plus,
    Clock,
    Image as ImageIcon,
    Award,
    X,
} from 'lucide-react';
import { useToast } from '../../hooks/useToast';

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

const LoadingSpinner: React.FC = () => {
    return (
        <div className="flex flex-col items-center justify-center min-h-[400px]">
            <div className="w-12 h-12 border-4 border-purple-500/30 border-t-purple-500 rounded-full animate-spin" />
            <p className="mt-4 text-white/60 font-medium">Загрузка...</p>
        </div>
    );
};

interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title: string;
    children: React.ReactNode;
    size?: 'sm' | 'md' | 'lg' | 'xl';
}

const Modal: React.FC<ModalProps> = ({ isOpen, onClose, title, children, size = 'md' }) => {
    useEffect(() => {
        if (isOpen) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = 'unset';
        }
        return () => {
            document.body.style.overflow = 'unset';
        };
    }, [isOpen]);

    const sizes = {
        sm: 'max-w-md',
        md: 'max-w-lg',
        lg: 'max-w-2xl',
        xl: 'max-w-4xl',
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 modal">
            <div
                className="fixed inset-0 bg-black/70 backdrop-blur-md cursor-pointer"
                onClick={onClose}
            />
            <div className={`fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 ${sizes[size]} w-full bg-black border border-purple-500/30 rounded-2xl shadow-2xl max-h-[90vh] overflow-hidden`}>
                <div className="flex justify-between items-center p-6 border-b border-purple-500/30">
                    <h2 className="text-xl font-bold gradient-text">{title}</h2>
                    <button
                        onClick={onClose}
                        className="p-2 hover:bg-white/10 rounded-full transition-all duration-200"
                        type="button"
                    >
                        <X className="w-5 h-5 text-white/60 hover:text-white" />
                    </button>
                </div>
                <div className="p-6 overflow-y-auto max-h-[calc(90vh-80px)]">
                    {children}
                </div>
            </div>
        </div>
    );
};

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
    const { showSuccess, showError } = useToast();

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
            showSuccess('Фотосессия успешно забронирована!');
            setIsModalOpen(false);
            setFormData({ date: '', photographerId: 0, serviceId: 0 });
        },
        onError: (error: Error) => {
            showError(error.message || 'Ошибка при создании бронирования');
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
            showError('Заполните все поля');
            return;
        }
        if (!clientId) {
            showError('Клиент не найден');
            return;
        }
        const selectedDate = new Date(formData.date);
        if (selectedDate < new Date()) {
            showError('Дата должна быть в будущем');
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

    if (sessionsLoading) return <LoadingSpinner />;

    return (
        <div className="min-h-screen pt-20 px-4">
            <div className="container mx-auto">
                {}
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

                {}
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
                            type="button"
                        >
                            <Plus className="w-5 h-5" />
                            Забронировать фотосессию
                        </button>
                    </div>
                </motion.div>

                {}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                    {stats.map((stat) => (
                        <UserStatCard key={stat.label} {...stat} />
                    ))}
                </div>

                {}
                <div className="glass-card p-6">
                    <h2 className="text-xl font-bold text-white mb-4 flex items-center gap-2">
                        <Calendar className="w-5 h-5 text-rose-400" />
                        Мои фотосессии
                    </h2>
                    {userSessions.length === 0 ? (
                        <div className="text-center py-12 text-white/40">
                            <Calendar className="w-16 h-16 mx-auto mb-3 opacity-50" />
                            <p className="text-lg">У вас пока нет фотосессий</p>
                            <p className="text-sm mt-1">Нажмите кнопку "Забронировать фотосессию", чтобы создать первую</p>
                            <button
                                onClick={() => setIsModalOpen(true)}
                                className="mt-4 text-rose-400 hover:text-rose-300 font-medium"
                                type="button"
                            >
                                Забронировать сейчас →
                            </button>
                        </div>
                    ) : (
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
                                                        Услуга: {getRussianServiceName(session.serviceName) || 'Не указана'}
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
                    )}
                </div>

                {}
                <Modal
                    isOpen={isModalOpen}
                    onClose={() => setIsModalOpen(false)}
                    title="Забронировать фотосессию"
                    size="md"
                >
                    <form onSubmit={handleSubmit} className="booking-form">
                        <div className="form-group">
                            <label htmlFor="bookingDate" className="form-label">
                                <Calendar className="w-4 h-4 inline mr-2" />
                                Дата и время *
                            </label>
                            <input
                                id="bookingDate"
                                type="datetime-local"
                                required
                                value={formData.date}
                                onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                                className="form-input"
                                min={new Date().toISOString().slice(0, 16)}
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="bookingPhotographer" className="form-label">
                                <Camera className="w-4 h-4 inline mr-2" />
                                Выберите фотографа *
                            </label>
                            <select
                                id="bookingPhotographer"
                                required
                                value={formData.photographerId}
                                onChange={(e) => setFormData({ ...formData, photographerId: Number.parseInt(e.target.value, 10) })}
                                className="form-input"
                            >
                                <option value={0}>Выберите фотографа</option>
                                {photographers?.map((photographer) => (
                                    <option key={photographer.id} value={photographer.id}>
                                        {photographer.firstName} {photographer.lastName} - {photographer.hourlyRate} BYN/час
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="form-group">
                            <label htmlFor="bookingService" className="form-label">
                                <Sparkles className="w-4 h-4 inline mr-2" />
                                Выберите услугу *
                            </label>
                            <select
                                id="bookingService"
                                required
                                value={formData.serviceId}
                                onChange={(e) => setFormData({ ...formData, serviceId: Number.parseInt(e.target.value, 10) })}
                                className="form-input"
                            >
                                <option value={0}>Выберите услугу</option>
                                {services?.map((service) => (
                                    <option key={service.id} value={service.id}>
                                        {getRussianServiceName(service.serviceType)} - {ServiceTypePricesBYN[service.serviceType] || service.basePrice} BYN
                                    </option>
                                ))}
                            </select>
                        </div>

                        {formData.photographerId > 0 && formData.serviceId > 0 && (
                            <motion.div
                                initial={{ opacity: 0, y: 10 }}
                                animate={{ opacity: 1, y: 0 }}
                                className="mt-2 p-4 bg-gradient-to-r from-green-500/20 to-emerald-500/20 rounded-xl border border-green-500/30"
                            >
                                <p className="text-sm font-medium text-white/70 mb-3">Предварительный расчет:</p>
                                <div className="space-y-2 text-sm">
                                    <div className="flex justify-between">
                                        <span className="text-white/60">Услуга:</span>
                                        <span className="text-white font-medium">{getServicePrice(formData.serviceId)} BYN</span>
                                    </div>
                                    <div className="flex justify-between">
                                        <span className="text-white/60">Работа фотографа (2 часа):</span>
                                        <span className="text-white font-medium">{getPhotographerRate(formData.photographerId) * 2} BYN</span>
                                    </div>
                                    <div className="border-t border-green-500/30 pt-2 mt-2 flex justify-between">
                                        <span className="text-white font-semibold">Итого:</span>
                                        <span className="text-xl font-bold text-green-400">
                                            {getServicePrice(formData.serviceId) + getPhotographerRate(formData.photographerId) * 2} BYN
                                        </span>
                                    </div>
                                </div>
                            </motion.div>
                        )}

                        <div className="form-actions mt-4">
                            <button
                                type="button"
                                onClick={() => setIsModalOpen(false)}
                                className="btn-cancel"
                            >
                                Отмена
                            </button>
                            <button
                                type="submit"
                                disabled={createSessionMutation.isPending}
                                className="btn-save"
                            >
                                {createSessionMutation.isPending ? 'Бронирование...' : 'Забронировать'}
                            </button>
                        </div>
                    </form>
                </Modal>
            </div>
        </div>
    );
};

export { UserDashboard };