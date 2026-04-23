import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { clientService } from '../services/clientService';
import { motion } from 'framer-motion';
import { Phone, Lock, User, Mail, LogIn, UserPlus, Camera } from 'lucide-react';
import toast from 'react-hot-toast';

const backgroundImage = 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=1920&h=1080&fit=crop';

const LoginPage: React.FC = () => {
    const [isLogin, setIsLogin] = useState(true);
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        phone: '',
        password: '',
    });
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { login, isAuthenticated, user, isLoading } = useAuthStore();

    useEffect(() => {
        if (!isLoading && isAuthenticated && user) {
            if (user.role === 'admin') {
                navigate('/admin/dashboard');
            } else {
                navigate('/user/dashboard');
            }
        }
    }, [isAuthenticated, user, isLoading, navigate]);

    const cleanPhoneNumber = (value: string) => {
        const hasPlus = value.startsWith('+');
        const cleaned = value.replace(/[^\d]/g, '');
        return hasPlus ? `+${cleaned}` : cleaned;
    };


    const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        let value = e.target.value;

        if (value.includes('+') && value.indexOf('+') !== 0) {
            value = value.replace(/\+/g, '');
        }

        const cleaned = cleanPhoneNumber(value);
        const maxLength = 13;
        const limited = cleaned.slice(0, maxLength);
        setFormData({ ...formData, phone: limited });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);

        try {
            if (isLogin) {
                const clients = await clientService.getAll();
                const client = clients.find(c => cleanPhoneNumber(c.phone) === formData.phone);

                if (client) {
                    const userData = {
                        id: client.id,
                        name: `${client.firstName} ${client.lastName}`,
                        email: client.email,
                        phone: client.phone,
                        role: 'user' as const
                    };
                    login(userData, 'mock-token');
                    toast.success(`Добро пожаловать, ${userData.name}!`);
                    navigate('/user/dashboard');
                }
                else if (formData.phone === '+375441234567' && formData.password === 'admin') {
                    const adminUser = {
                        id: 0,
                        name: 'Администратор',
                        email: 'admin@photostudio.com',
                        phone: '+375441234567',
                        role: 'admin' as const
                    };
                    login(adminUser, 'mock-token');
                    toast.success('Добро пожаловать, Администратор!');
                    navigate('/admin/dashboard');
                }
                else {
                    toast.error('Неверный номер телефона или пароль');
                }
            } else {
                if (!formData.name || !formData.email || !formData.phone || !formData.password) {
                    toast.error('Заполните все поля');
                    setLoading(false);
                    return;
                }

                const existingClients = await clientService.getAll();
                if (existingClients.some(c => cleanPhoneNumber(c.phone) === formData.phone)) {
                    toast.error('Пользователь с таким номером телефона уже существует');
                    setLoading(false);
                    return;
                }

                const nameParts = formData.name.trim().split(' ');
                const firstName = nameParts[0] || '';
                const lastName = nameParts.slice(1).join(' ') || '';

                const newClient = await clientService.create({
                    firstName: firstName,
                    lastName: lastName,
                    phone: formData.phone,
                    email: formData.email,
                });

                const userData = {
                    id: newClient.id,
                    name: `${newClient.firstName} ${newClient.lastName}`,
                    email: newClient.email,
                    phone: newClient.phone,
                    role: 'user' as const
                };
                login(userData, 'mock-token');
                toast.success('Регистрация успешна!');
                navigate('/user/dashboard');
            }
        } catch (error: any) {
            toast.error(error.message || 'Ошибка авторизации');
        } finally {
            setLoading(false);
        }
    };

    const getButtonContent = () => {
        if (loading) {
            return <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />;
        }
        return (
            <>
                {isLogin ? <LogIn className="w-5 h-5" /> : <UserPlus className="w-5 h-5" />}
                {isLogin ? 'Войти' : 'Зарегистрироваться'}
            </>
        );
    };

    const getTitleText = () => {
        return isLogin ? 'Войдите в аккаунт' : 'Создайте новый аккаунт';
    };

    const getToggleText = () => {
        return isLogin ? 'Нет аккаунта? Зарегистрируйтесь' : 'Уже есть аккаунт? Войдите';
    };

    if (isLoading) {
        return (
            <div className="min-h-screen bg-black flex items-center justify-center">
                <div className="w-12 h-12 border-4 border-purple-500/30 border-t-purple-500 rounded-full animate-spin"></div>
            </div>
        );
    }

    if (isAuthenticated && user) {
        return null;
    }

    return (
        <div
            className="min-h-screen flex items-center justify-center p-4 relative overflow-hidden"
            style={{
                backgroundImage: `url(${backgroundImage})`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
            }}
        >
            <div className="absolute inset-0 bg-black/60"></div>

            <div className="absolute inset-0 overflow-hidden">
                <div className="absolute top-20 left-10 w-72 h-72 bg-purple-500/20 rounded-full blur-3xl animate-float"></div>
                <div className="absolute bottom-20 right-10 w-96 h-96 bg-pink-500/20 rounded-full blur-3xl animate-float" style={{ animationDelay: '1s' }}></div>
            </div>

            <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                className="relative z-10 max-w-md w-full"
            >
                <div className="text-center mb-8">
                    <motion.div
                        animate={{ scale: [1, 1.05, 1] }}
                        transition={{ duration: 3, repeat: Infinity }}
                        className="inline-block"
                    >
                        <div className="w-24 h-24 bg-gradient-to-br from-purple-500 to-pink-500 rounded-2xl flex items-center justify-center shadow-2xl mx-auto">
                            <Camera className="w-12 h-12 text-white" />
                        </div>
                    </motion.div>
                    <h1 className="text-4xl font-bold text-white mt-4">PhotoStudio</h1>
                    <p className="text-white/70 mt-2">{getTitleText()}</p>
                </div>

                <motion.div
                    initial={{ opacity: 0, scale: 0.95 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ delay: 0.2 }}
                    className="bg-white/10 backdrop-blur-md rounded-2xl p-8 border border-white/20"
                >
                    <form onSubmit={handleSubmit} className="space-y-4">
                        {!isLogin && (
                            <>
                                <div className="relative">
                                    <User className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-white/40" />
                                    <input
                                        type="text"
                                        required
                                        value={formData.name}
                                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                        className="w-full pl-10 pr-4 py-3 bg-white/5 border border-white/20 rounded-xl text-white placeholder-white/30 focus:outline-none focus:border-purple-500 transition-all"
                                        placeholder="Иван Иванов"
                                    />
                                </div>
                                <div className="relative">
                                    <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-white/40" />
                                    <input
                                        type="email"
                                        required
                                        value={formData.email}
                                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                        className="w-full pl-10 pr-4 py-3 bg-white/5 border border-white/20 rounded-xl text-white placeholder-white/30 focus:outline-none focus:border-purple-500 transition-all"
                                        placeholder="ivan@example.com"
                                    />
                                </div>
                            </>
                        )}

                        {}
                        <div className="relative">
                            <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-white/40" />
                            <input
                                type="tel"
                                required
                                value={formData.phone}
                                onChange={handlePhoneChange}
                                className="w-full pl-10 pr-4 py-3 bg-white/5 border border-white/20 rounded-xl text-white placeholder-white/30 focus:outline-none focus:border-purple-500 transition-all"
                                placeholder="+375441234567"
                            />
                        </div>

                        <div className="relative">
                            <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-white/40" />
                            <input
                                type="password"
                                required
                                value={formData.password}
                                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                                className="w-full pl-10 pr-4 py-3 bg-white/5 border border-white/20 rounded-xl text-white placeholder-white/30 focus:outline-none focus:border-purple-500 transition-all"
                                placeholder="Пароль"
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full btn-primary py-3 flex items-center justify-center gap-2 disabled:opacity-50"
                        >
                            {getButtonContent()}
                        </button>
                    </form>

                    <div className="mt-6 text-center">
                        <button
                            onClick={() => {
                                setIsLogin(!isLogin);
                                setFormData({ name: '', email: '', phone: '', password: '' });
                            }}
                            className="text-purple-300 hover:text-purple-200 text-sm transition-colors"
                        >
                            {getToggleText()}
                        </button>
                    </div>
                </motion.div>
            </motion.div>
        </div>
    );
};

export { LoginPage };