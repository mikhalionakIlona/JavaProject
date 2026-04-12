import React, { useState, useCallback, useRef, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { serviceService } from '../../services/serviceService';
import { PhotoService, ServiceType } from '../../types';
import LoadingSpinner from '../common/LoadingSpinner';
import { Modal } from '../common/Modal';
import { DollarSign, ChevronLeft, ChevronRight } from 'lucide-react';

import weddingImg1 from '../../assets/svadba1.png';
import weddingImg2 from '../../assets/svadba2.png';
import weddingImg3 from '../../assets/svadba3.png';
import weddingImg4 from '../../assets/svadba4.png';
import portraitImg1 from '../../assets/port.png';
import portraitImg2 from '../../assets/port2.png';
import portraitImg3 from '../../assets/port3.png';
import productImg1 from '../../assets/predmet1.png';
import productImg2 from '../../assets/predmet2.png';
import corporateImg1 from '../../assets/korparat1.png';
import corporateImg2 from '../../assets/korparat2.png';
import familyImg1 from '../../assets/family1.png';
import familyImg2 from '../../assets/family2.png';
import familyImg3 from '../../assets/family3.png';


const ServiceTypePricesBYN: Record<ServiceType, number> = {
    [ServiceType.WEDDING]: 300,
    [ServiceType.PORTRAIT]: 100,
    [ServiceType.PRODUCT]: 50,
    [ServiceType.CORPORATE]: 200,
    [ServiceType.FAMILY]: 150,
};

const serviceGalleries: Record<ServiceType, { url: string; title: string }[]> = {
    [ServiceType.WEDDING]: [
        { url: weddingImg1, title: 'Свадебная фотосессия 1' },
        { url: weddingImg2, title: 'Свадебная фотосессия 2' },
        { url: weddingImg3, title: 'Свадебная фотосессия 3' },
        { url: weddingImg4, title: 'Свадебная фотосессия 4' },
    ],
    [ServiceType.PORTRAIT]: [
        { url: portraitImg1, title: 'Портретная съемка 1' },
        { url: portraitImg2, title: 'Портретная съемка 2' },
        { url: portraitImg3, title: 'Портретная съемка 3' },
    ],
    [ServiceType.PRODUCT]: [
        { url: productImg1, title: 'Предметная съемка 1' },
        { url: productImg2, title: 'Предметная съемка 2' },
    ],
    [ServiceType.CORPORATE]: [
        { url: corporateImg1, title: 'Корпоративная съемка 1' },
        { url: corporateImg2, title: 'Корпоративная съемка 2' },
    ],
    [ServiceType.FAMILY]: [
        { url: familyImg1, title: 'Семейная съемка 1' },
        { url: familyImg2, title: 'Семейная съемка 2' },
        { url: familyImg3, title: 'Семейная съемка 3' },
    ],
};

const getServicePreviewImage = (serviceType: ServiceType): string => {
    return serviceGalleries[serviceType]?.[0]?.url || '';
};

const getServiceIcon = (serviceType: string) => {
    const icons: Record<string, string> = {
        WEDDING: '💍',
        PORTRAIT: '👤',
        PRODUCT: '📦',
        CORPORATE: '🏢',
        FAMILY: '👨‍👩‍👧‍👦',
    };
    return icons[serviceType] || '✨';
};

const getServiceColor = (serviceType: string) => {
    const colors: Record<string, string> = {
        WEDDING: 'from-pink-500 to-rose-500',
        PORTRAIT: 'from-blue-500 to-cyan-500',
        PRODUCT: 'from-green-500 to-emerald-500',
        CORPORATE: 'from-purple-500 to-indigo-500',
        FAMILY: 'from-orange-500 to-amber-500',
    };
    return colors[serviceType] || 'from-purple-500 to-pink-500';
};

const ServiceList: React.FC = () => {
    const [selectedService, setSelectedService] = useState<PhotoService | null>(null);
    const [currentImageIndex, setCurrentImageIndex] = useState(0);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const modalRef = useRef<HTMLDivElement>(null);

    const { data: services, isLoading } = useQuery<PhotoService[]>({
        queryKey: ['services'],
        queryFn: serviceService.getAll,
    });

    const openGallery = useCallback((service: PhotoService) => {
        setSelectedService(service);
        setCurrentImageIndex(0);
        setIsModalOpen(true);
    }, []);

    const closeGallery = useCallback(() => {
        setIsModalOpen(false);
        setSelectedService(null);
        setCurrentImageIndex(0);
    }, []);

    const nextImage = useCallback(() => {
        if (selectedService) {
            const gallery = serviceGalleries[selectedService.serviceType];
            if (gallery && currentImageIndex < gallery.length - 1) {
                setCurrentImageIndex(currentImageIndex + 1);
            }
        }
    }, [selectedService, currentImageIndex]);

    const prevImage = useCallback(() => {
        if (selectedService && currentImageIndex > 0) {
            setCurrentImageIndex(currentImageIndex - 1);
        }
    }, [selectedService, currentImageIndex]);

    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            if (!isModalOpen) return;
            if (e.key === 'ArrowLeft') {
                prevImage();
            } else if (e.key === 'ArrowRight') {
                nextImage();
            } else if (e.key === 'Escape') {
                closeGallery();
            }
        };

        globalThis.addEventListener('keydown', handleKeyDown);
        return () => globalThis.removeEventListener('keydown', handleKeyDown);
    }, [isModalOpen, prevImage, nextImage, closeGallery]);

    if (isLoading) return <LoadingSpinner />;

    const currentGallery = selectedService ? serviceGalleries[selectedService.serviceType] : [];

    return (
        <div className="min-h-screen pt-20 px-4">
            <div className="container mx-auto">
                {}
                <div className="flex justify-between items-center mb-8 flex-wrap gap-4">
                    <div>
                        <h1 className="text-4xl font-bold bg-gradient-to-r from-rose-400 via-purple-400 to-indigo-400 bg-clip-text text-transparent">
                            Управление услугами
                        </h1>
                        <p className="text-purple-300/70 mt-1">Выберите услугу для просмотра портфолио</p>
                    </div>
                </div>

                {}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {services?.map((service) => {
                        const servicePrice = ServiceTypePricesBYN[service.serviceType];
                        const serviceIcon = getServiceIcon(service.serviceType);
                        const serviceColor = getServiceColor(service.serviceType);
                        const previewImage = getServicePreviewImage(service.serviceType);
                        const galleryCount = serviceGalleries[service.serviceType]?.length || 0;

                        return (
                            <motion.div
                                key={service.id}
                                initial={{ opacity: 0, y: 20 }}
                                animate={{ opacity: 1, y: 0 }}
                                whileHover={{ scale: 1.02, y: -5 }}
                                className="glass-card overflow-hidden cursor-pointer group"
                                onClick={() => openGallery(service)}
                                onKeyDown={(e) => {
                                    if (e.key === 'Enter' || e.key === ' ') {
                                        e.preventDefault();
                                        openGallery(service);
                                    }
                                }}
                                role="button"
                                tabIndex={0}
                                aria-label={`Открыть галерею ${service.name}`}
                            >
                                {}
                                <div className="relative h-48 overflow-hidden bg-gray-800">
                                    <img
                                        src={previewImage}
                                        alt={service.name}
                                        className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                                    />
                                    <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
                                    <div className="absolute bottom-3 left-3 right-3">
                                        <div className="flex items-center gap-2">
                                            <div className={`w-8 h-8 bg-gradient-to-br ${serviceColor} rounded-lg flex items-center justify-center text-lg shadow-lg`}>
                                                {serviceIcon}
                                            </div>
                                            <h3 className="font-semibold text-white text-lg drop-shadow-md">
                                                {service.name}
                                            </h3>
                                        </div>
                                    </div>
                                    {}
                                    {galleryCount > 0 && (
                                        <div className="absolute top-3 right-3 bg-black/60 backdrop-blur-sm rounded-full px-2 py-1 text-xs text-white">
                                            📷 {galleryCount} фото
                                        </div>
                                    )}
                                </div>

                                <div className="p-5">
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center gap-2 text-white/60">
                                            <DollarSign className="w-4 h-4 text-green-400" />
                                            <span className="text-sm">Стоимость:</span>
                                        </div>
                                        <span className="text-xl font-bold text-green-400">{servicePrice} BYN</span>
                                    </div>

                                    {}
                                    <div className="mt-4 pt-3 border-t border-white/10">
                                        <div className="flex items-center justify-between text-xs text-white/30">
                                            <span>Доступно для заказа</span>
                                            <span className="text-green-400">✓</span>
                                        </div>
                                    </div>
                                </div>
                            </motion.div>
                        );
                    })}
                </div>

                {services?.length === 0 && (
                    <div className="glass-card p-12 text-center">
                        <div className="text-6xl mb-4">📸</div>
                        <h3 className="text-xl font-semibold text-white mb-2">Нет услуг</h3>
                        <p className="text-white/40">Услуги будут добавлены автоматически</p>
                    </div>
                )}

                {}
                <Modal
                    isOpen={isModalOpen}
                    onClose={closeGallery}
                    title={selectedService?.name || 'Галерея'}
                    size="lg"
                >
                    {selectedService && currentGallery.length > 0 && (
                        <div className="space-y-4" ref={modalRef}>
                            {}
                            <div className="relative">
                                <img
                                    src={currentGallery[currentImageIndex].url}
                                    alt={currentGallery[currentImageIndex].title}
                                    className="w-full h-96 object-contain bg-gray-100 rounded-lg"
                                />
                                <p className="text-center text-sm text-gray-500 mt-2">
                                    {currentGallery[currentImageIndex].title}
                                </p>

                                {}
                                {currentGallery.length > 1 && (
                                    <>
                                        <button
                                            onClick={prevImage}
                                            disabled={currentImageIndex === 0}
                                            className="absolute left-2 top-1/2 transform -translate-y-1/2 bg-black/50 hover:bg-black/70 text-white rounded-full p-2 transition disabled:opacity-50"
                                            aria-label="Предыдущее фото"
                                        >
                                            <ChevronLeft className="w-6 h-6" />
                                        </button>
                                        <button
                                            onClick={nextImage}
                                            disabled={currentImageIndex === currentGallery.length - 1}
                                            className="absolute right-2 top-1/2 transform -translate-y-1/2 bg-black/50 hover:bg-black/70 text-white rounded-full p-2 transition disabled:opacity-50"
                                            aria-label="Следующее фото"
                                        >
                                            <ChevronRight className="w-6 h-6" />
                                        </button>
                                    </>
                                )}
                            </div>

                            {}
                            {currentGallery.length > 1 && (
                                <div className="grid grid-cols-4 gap-2 mt-4">
                                    {currentGallery.map((img, idx) => (
                                        <button
                                            key={`${img.title}-${idx}`}
                                            onClick={() => setCurrentImageIndex(idx)}
                                            className={`cursor-pointer rounded-lg overflow-hidden border-2 transition ${
                                                currentImageIndex === idx ? 'border-rose-500' : 'border-transparent'
                                            }`}
                                            aria-label={`Перейти к фото ${idx + 1}`}
                                        >
                                            <img
                                                src={img.url}
                                                alt={img.title}
                                                className="w-full h-20 object-cover"
                                            />
                                        </button>
                                    ))}
                                </div>
                            )}

                            {}
                            <div className="bg-gray-50 rounded-lg p-4 mt-4">
                                <div className="flex justify-between items-center">
                                    <span className="text-gray-600">Стоимость услуги:</span>
                                    <span className="text-2xl font-bold text-green-600">
                                        {ServiceTypePricesBYN[selectedService.serviceType]} BYN
                                    </span>
                                </div>
                            </div>
                        </div>
                    )}
                </Modal>
            </div>
        </div>
    );
};

export { ServiceList };