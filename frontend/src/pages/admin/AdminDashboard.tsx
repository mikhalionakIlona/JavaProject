import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { clientService } from '../../services/clientService';
import { photographerService } from '../../services/photographerService';
import { sessionService } from '../../services/sessionService';
import { serviceService } from '../../services/serviceService';
import { photoService } from '../../services/photoService';
import {
    Users,
    Camera,
    Calendar,
    Sparkles,
    Image,
    TrendingUp,
    Star,
    Clock,
    Award
} from 'lucide-react';

const monthNames = ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'];

// Компонент круговой диаграммы
const PieChart: React.FC<{ data: { name: string; value: number; color: string }[]; title: string; icon?: React.ReactNode }> =
    ({ data, title, icon }) => {
        const total = data.reduce((sum, item) => sum + item.value, 0);
        if (total === 0) {
            return (
                <div className="glass-card p-6">
                    <h3 className="text-lg font-semibold text-white mb-4 text-center flex items-center justify-center gap-2">
                        {icon}
                        {title}
                    </h3>
                    <div className="flex flex-col items-center justify-center h-64 text-white/40">
                        <p>Нет данных для отображения</p>
                    </div>
                </div>
            );
        }

        let startAngle = -90;

        const getPathCoordinates = (value: number, index: number) => {
            const angle = (value / total) * 360;
            const endAngle = startAngle + angle;

            const startRad = (startAngle * Math.PI) / 180;
            const endRad = (endAngle * Math.PI) / 180;

            const x1 = 100 + 80 * Math.cos(startRad);
            const y1 = 100 + 80 * Math.sin(startRad);
            const x2 = 100 + 80 * Math.cos(endRad);
            const y2 = 100 + 80 * Math.sin(endRad);

            const largeArcFlag = angle > 180 ? 1 : 0;

            const pathData = `M 100 100 L ${x1} ${y1} A 80 80 0 ${largeArcFlag} 1 ${x2} ${y2} Z`;

            startAngle = endAngle;

            return { pathData, color: data[index].color };
        };

        startAngle = -90;

        return (
            <div className="glass-card p-6">
                <h3 className="text-lg font-semibold text-white mb-4 text-center flex items-center justify-center gap-2">
                    {icon}
                    {title}
                </h3>
                <div className="flex flex-col items-center">
                    <svg width="220" height="220" viewBox="0 0 200 200" className="mb-4">
                        {data.map((item, idx) => {
                            const { pathData, color } = getPathCoordinates(item.value, idx);
                            return (
                                <g key={`pie-${item.name}-${idx}`}>
                                    <motion.path
                                        d={pathData}
                                        fill={color}
                                        stroke="#1a1a2e"
                                        strokeWidth="2"
                                        initial={{ opacity: 0, scale: 0 }}
                                        animate={{ opacity: 1, scale: 1 }}
                                        transition={{ delay: idx * 0.1, duration: 0.5 }}
                                    />
                                </g>
                            );
                        })}
                        <circle cx="100" cy="100" r="50" fill="#1a1a2e" stroke="#8b5cf6" strokeWidth="2" />
                        <text x="100" y="95" textAnchor="middle" className="text-sm font-bold fill-white">
                            {total}
                        </text>
                        <text x="100" y="112" textAnchor="middle" className="text-xs fill-white/50">
                            всего
                        </text>
                    </svg>
                    <div className="flex flex-wrap justify-center gap-3 mt-2">
                        {data.map((item, idx) => (
                            <motion.div
                                key={`legend-${item.name}-${idx}`}
                                className="flex items-center gap-1.5"
                                initial={{ opacity: 0, x: -10 }}
                                animate={{ opacity: 1, x: 0 }}
                                transition={{ delay: idx * 0.1 }}
                            >
                                <div className="w-3 h-3 rounded-full" style={{ backgroundColor: item.color }} />
                                <span className="text-xs text-white/60">{item.name}</span>
                                <span className="text-xs font-semibold text-white">{item.value}</span>
                            </motion.div>
                        ))}
                    </div>
                </div>
            </div>
        );
    };

// Анимированная статистическая карточка
const StatCard: React.FC<{ label: string; value: string | number; icon: React.ElementType; delay: number; trend?: string }> =
    ({ label, value, icon: Icon, delay, trend }) => {
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
                        {trend && (
                            <p className="text-xs text-green-400 mt-1 flex items-center gap-1">
                                <TrendingUp className="w-3 h-3" />
                                {trend}
                            </p>
                        )}
                    </div>
                    <div className="w-12 h-12 bg-gradient-to-br from-purple-500/20 to-pink-500/20 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
                        <Icon className="w-6 h-6 text-purple-400" />
                    </div>
                </div>
            </motion.div>
        );
    };

const AdminDashboard: React.FC = () => {
    const { data: clients } = useQuery({ queryKey: ['clients'], queryFn: clientService.getAll });
    const { data: photographers } = useQuery({ queryKey: ['photographers'], queryFn: photographerService.getAll });
    const { data: sessions } = useQuery({ queryKey: ['sessions'], queryFn: sessionService.getAll });
    const { data: services } = useQuery({ queryKey: ['services'], queryFn: serviceService.getAll });
    const { data: photos } = useQuery({ queryKey: ['photos'], queryFn: photoService.getAll });

    // Прямое использование цен без конвертации
    const totalRevenue = sessions?.reduce((sum, s) => sum + s.totalPrice, 0) || 0;

    const stats = [
        { label: 'Клиентов', value: clients?.length || 0, icon: Users, delay: 0.1, trend: '+12%' },
        { label: 'Фотографов', value: photographers?.length || 0, icon: Camera, delay: 0.2, trend: '+5%' },
        { label: 'Фотосессий', value: sessions?.length || 0, icon: Calendar, delay: 0.3, trend: '+8%' },
        { label: 'Услуг', value: services?.length || 0, icon: Sparkles, delay: 0.4, trend: '+2' },
        { label: 'Фотографий', value: photos?.length || 0, icon: Image, delay: 0.5, trend: '+15%' },
        { label: 'Выручка', value: `${totalRevenue.toLocaleString()} BYN`, icon: TrendingUp, delay: 0.6, trend: '+23%' },
    ];

    // Подготовка данных для круговой диаграммы по услугам
    const serviceColors = ['#8b5cf6', '#ec4899', '#06b6d4', '#f59e0b', '#10b981'];
    const serviceDistribution = services?.map((service, idx) => ({
        name: service.name.length > 15 ? service.name.substring(0, 12) + '...' : service.name,
        value: sessions?.filter(s => s.serviceId === service.id).length || 0,
        color: serviceColors[idx % serviceColors.length],
    })).filter(s => s.value > 0) || [];

    // Подготовка данных для круговой диаграммы по месяцам
    const sessionsByMonth: { [key: string]: number } = {};
    sessions?.forEach(session => {
        const date = new Date(session.date);
        const monthKey = `${date.getFullYear()}-${date.getMonth()}`;
        sessionsByMonth[monthKey] = (sessionsByMonth[monthKey] || 0) + 1;
    });

    const monthColors = ['#8b5cf6', '#ec4899', '#06b6d4', '#f59e0b', '#10b981', '#ef4444', '#3b82f6', '#14b8a6', '#a855f7', '#eab308', '#ec4899', '#6366f1'];

    const monthDistribution = Object.entries(sessionsByMonth)
        .map(([key, count]) => {
            const [year, month] = key.split('-');
            const monthIndex = Number.parseInt(month, 10);
            return {
                name: `${monthNames[monthIndex]} ${year}`,
                value: count,
                color: monthColors[monthIndex % monthColors.length],
            };
        })
        .sort((a, b) => {
            const [aMonth, aYear] = [a.name.split(' ')[0], a.name.split(' ')[1]];
            const [bMonth, bYear] = [b.name.split(' ')[0], b.name.split(' ')[1]];
            const monthOrder = monthNames;
            if (aYear !== bYear) return Number.parseInt(bYear, 10) - Number.parseInt(aYear, 10);
            return monthOrder.indexOf(bMonth) - monthOrder.indexOf(aMonth);
        });

    // Популярные услуги
    const popularServices = services?.map(service => ({
        name: service.name,
        count: sessions?.filter(s => s.serviceId === service.id).length || 0,
    })).sort((a, b) => b.count - a.count).slice(0, 3) || [];

    // Предстоящие фотосессии
    const upcomingSessions = sessions?.filter(s => new Date(s.date) > new Date())
        .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())
        .slice(0, 5) || [];

    // Лучшие фотографы
    const topPhotographers = photographers?.map(photographer => ({
        name: `${photographer.firstName} ${photographer.lastName}`,
        sessionsCount: sessions?.filter(s => s.photographerId === photographer.id).length || 0,
    })).sort((a, b) => b.sessionsCount - a.sessionsCount).slice(0, 3) || [];

    return (
        <div className="min-h-screen pt-20 px-4">
            <div className="container mx-auto">
                {/* Hero секция */}
                <motion.div
                    initial={{ opacity: 0, y: 30 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.6 }}
                    className="text-center mb-12"
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
                    <h1 className="text-4xl font-bold gradient-text mt-4">Центр управления</h1>
                    <p className="text-white/50 mt-2">Статистика и аналитика фотостудии</p>
                </motion.div>

                {/* Статистика */}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                    {stats.map((stat) => (
                        <StatCard key={stat.label} {...stat} />
                    ))}
                </div>

                {/* Круговые диаграммы */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                    {monthDistribution.length > 0 && (
                        <PieChart
                            data={monthDistribution}
                            title="Фотосессии по месяцам"
                            icon={<Calendar className="w-4 h-4 text-purple-400" />}
                        />
                    )}
                    {serviceDistribution.length > 0 && (
                        <PieChart
                            data={serviceDistribution}
                            title="Распределение по услугам"
                            icon={<Sparkles className="w-4 h-4 text-purple-400" />}
                        />
                    )}
                </div>

                {/* Дополнительные блоки */}
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    {/* Популярные услуги */}
                    <motion.div
                        initial={{ opacity: 0, x: -20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ delay: 0.7, duration: 0.5 }}
                        className="glass-card p-6"
                    >
                        <h2 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
                            <Star className="w-5 h-5 text-yellow-400" />
                            Популярные услуги
                        </h2>
                        <div className="space-y-3">
                            {popularServices.map((service, idx) => (
                                <div key={`popular-${service.name}-${idx}`} className="flex items-center justify-between p-3 bg-white/5 rounded-xl">
                                    <span className="text-white/80">{service.name}</span>
                                    <span className="text-purple-400 font-semibold">{service.count} фотосессий</span>
                                </div>
                            ))}
                            {popularServices.length === 0 && (
                                <p className="text-white/40 text-center py-4">Нет данных</p>
                            )}
                        </div>
                    </motion.div>

                    {/* Предстоящие фотосессии */}
                    <motion.div
                        initial={{ opacity: 0, y: -20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ delay: 0.8, duration: 0.5 }}
                        className="glass-card p-6"
                    >
                        <h2 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
                            <Clock className="w-5 h-5 text-blue-400" />
                            Ближайшие съемки
                        </h2>
                        <div className="space-y-3">
                            {upcomingSessions.map((session) => (
                                <div key={session.id} className="flex items-center justify-between p-3 bg-white/5 rounded-xl">
                                    <div>
                                        <p className="text-white/80 text-sm">{session.clientName} {session.clientLastName}</p>
                                        <p className="text-white/40 text-xs">{session.serviceName}</p>
                                    </div>
                                    <span className="text-blue-400 text-sm">{new Date(session.date).toLocaleDateString()}</span>
                                </div>
                            ))}
                            {upcomingSessions.length === 0 && (
                                <p className="text-white/40 text-center py-4">Нет запланированных съемок</p>
                            )}
                        </div>
                    </motion.div>

                    {/* Лучшие фотографы */}
                    <motion.div
                        initial={{ opacity: 0, x: 20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ delay: 0.9, duration: 0.5 }}
                        className="glass-card p-6"
                    >
                        <h2 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
                            <Award className="w-5 h-5 text-amber-400" />
                            Топ фотографы
                        </h2>
                        <div className="space-y-3">
                            {topPhotographers.map((photographer, idx) => (
                                <div key={`top-${photographer.name}-${idx}`} className="flex items-center justify-between p-3 bg-white/5 rounded-xl">
                                    <div className="flex items-center gap-2">
                                        <span className="text-amber-400 font-bold">#{idx + 1}</span>
                                        <span className="text-white/80">{photographer.name}</span>
                                    </div>
                                    <span className="text-purple-400 font-semibold">{photographer.sessionsCount} съемок</span>
                                </div>
                            ))}
                            {topPhotographers.length === 0 && (
                                <p className="text-white/40 text-center py-4">Нет данных</p>
                            )}
                        </div>
                    </motion.div>
                </div>
            </div>
        </div>
    );
};

export { AdminDashboard };