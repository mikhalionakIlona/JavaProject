import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { clientService } from '../services/clientService';
import { photographerService } from '../services/photographerService';
import { sessionService } from '../services/sessionService';
import { serviceService } from '../services/serviceService';
import { UsersIcon, CameraIcon, CalendarIcon, SparklesIcon } from '@heroicons/react/24/outline';

const Dashboard: React.FC = () => {
    const { data: clients } = useQuery({ queryKey: ['clients'], queryFn: clientService.getAll });
    const { data: photographers } = useQuery({ queryKey: ['photographers'], queryFn: photographerService.getAll });
    const { data: sessions } = useQuery({ queryKey: ['sessions'], queryFn: sessionService.getAll });
    const { data: services } = useQuery({ queryKey: ['services'], queryFn: serviceService.getAll });

    const stats = [
        { label: 'Клиентов', value: clients?.length || 0, icon: UsersIcon, color: 'from-blue-500 to-blue-600' },
        { label: 'Фотографов', value: photographers?.length || 0, icon: CameraIcon, color: 'from-purple-500 to-purple-600' },
        { label: 'Фотосессий', value: sessions?.length || 0, icon: CalendarIcon, color: 'from-green-500 to-green-600' },
        { label: 'Услуг', value: services?.length || 0, icon: SparklesIcon, color: 'from-orange-500 to-orange-600' },
    ];

    return (
        <div className="space-y-8 animate-fade-in">
            <div className="bg-gradient-to-r from-primary-500 to-primary-700 rounded-2xl p-8 text-white shadow-xl">
                <h1 className="text-3xl font-bold mb-2">Добро пожаловать в PhotoStudio!</h1>
                <p className="text-primary-100">Управление фотостудией в одном месте</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {stats.map((stat, idx) => (
                    <motion.div
                        key={stat.label}
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ delay: idx * 0.1 }}
                        className="bg-white rounded-2xl shadow-lg p-6 hover:shadow-xl transition-all cursor-pointer"
                    >
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-gray-500 text-sm">{stat.label}</p>
                                <p className="text-3xl font-bold text-gray-800 mt-2">{stat.value}</p>
                            </div>
                            <div className={`w-12 h-12 bg-gradient-to-r ${stat.color} rounded-xl flex items-center justify-center shadow-lg`}>
                                <stat.icon className="w-6 h-6 text-white" />
                            </div>
                        </div>
                    </motion.div>
                ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="bg-white rounded-2xl shadow-lg p-6">
                    <h2 className="text-xl font-bold text-gray-800 mb-4">Последние фотосессии</h2>
                    <div className="space-y-3">
                        {sessions?.slice(-5).reverse().map((session) => (
                            <div key={session.id} className="flex justify-between items-center p-3 bg-gray-50 rounded-xl">
                                <div>
                                    <p className="font-medium">{session.clientName} {session.clientLastName}</p>
                                    <p className="text-sm text-gray-500">{session.serviceName}</p>
                                </div>
                                <div className="text-right">
                                    <p className="text-sm text-gray-500">{new Date(session.date).toLocaleDateString()}</p>
                                    <p className="text-sm font-semibold text-primary-600">{session.totalPrice} ₽</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="bg-white rounded-2xl shadow-lg p-6">
                    <h2 className="text-xl font-bold text-gray-800 mb-4">Наши фотографы</h2>
                    <div className="space-y-3">
                        {photographers?.map((photographer) => (
                            <div key={photographer.id} className="flex justify-between items-center p-3 bg-gray-50 rounded-xl">
                                <div>
                                    <p className="font-medium">{photographer.firstName} {photographer.lastName}</p>
                                    <p className="text-sm text-gray-500">{photographer.phone}</p>
                                </div>
                                <div className="text-right">
                                    <p className="text-sm font-semibold text-primary-600">{photographer.hourlyRate} ₽/час</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;