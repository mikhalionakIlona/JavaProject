import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { photoService } from '../../services/photoService';
import { sessionService } from '../../services/sessionService';
import { Photo, PhotoSession } from '../../types';
import { useToast } from '../../hooks/useToast';
import LoadingSpinner from '../common/LoadingSpinner';
import { Modal } from '../common/Modal';
import {
    Trash2,
    Calendar,
    User,
    Camera,
    ZoomIn,
    Upload
} from 'lucide-react';

// ВАШИ РЕАЛЬНЫЕ ФОТОГРАФИИ ИЗ ПАПКИ ASSETS
import svadba1 from '../../assets/svadba1.png';
import svadba2 from '../../assets/svadba2.png';
import svadba3 from '../../assets/svadba3.png';
import svadba4 from '../../assets/svadba4.png';
import port from '../../assets/port.png';
import port2 from '../../assets/port2.png';
import port3 from '../../assets/port3.png';
import predmet from '../../assets/predmet.png';
import predmet1 from '../../assets/predmet1.png';
import predmet2 from '../../assets/predmet2.png';
import predmet4 from '../../assets/predmet4.png';
import korparat1 from '../../assets/korparat1.png';
import korparat2 from '../../assets/korparat2.png';
import korparat3 from '../../assets/korparat3.png';
import family1 from '../../assets/family1.png';
import family2 from '../../assets/family2.png';
import family3 from '../../assets/family3.png';

// МАППИНГ: для каждого типа услуги - массив уникальных фотографий
const PHOTOS_BY_SERVICE: Record<string, string[]> = {
    'WEDDING': [svadba1, svadba2, svadba3, svadba4],
    'PORTRAIT': [port, port2, port3],
    'PRODUCT': [predmet, predmet1, predmet2, predmet4],
    'CORPORATE': [korparat1, korparat2, korparat3],
    'FAMILY': [family1, family2, family3],
};

// Функция для получения фотографии для типа услуги
const getPhotoForService = (serviceType: string, index: number): string => {
    const photos = PHOTOS_BY_SERVICE[serviceType];
    if (!photos || photos.length === 0) {
        return svadba1;
    }
    return photos[index % photos.length];
};

// Русские названия для услуг
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

const PhotoList: React.FC = () => {
    const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
    const [isViewModalOpen, setIsViewModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [selectedPhoto, setSelectedPhoto] = useState<Photo | null>(null);
    const [photoToDelete, setPhotoToDelete] = useState<Photo | null>(null);
    const [formData, setFormData] = useState({
        fileName: '',
        filePath: '',
        sessionId: 0,
    });
    const [previewUrl, setPreviewUrl] = useState('');

    const { showSuccess, showError } = useToast();
    const queryClient = useQueryClient();

    const { data: photos, isLoading } = useQuery<Photo[]>({
        queryKey: ['photos'],
        queryFn: photoService.getAll,
    });

    const { data: sessions } = useQuery<PhotoSession[]>({
        queryKey: ['sessions'],
        queryFn: sessionService.getAll,
    });

    const createMutation = useMutation({
        mutationFn: (data: { fileName: string; filePath: string; sessionId: number }) =>
            photoService.create(data.fileName, data.filePath, data.sessionId),
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['photos'] });
            showSuccess('Фотография успешно добавлена');
            setIsUploadModalOpen(false);
            resetForm();
        },
        onError: (error: Error) => {
            showError(error.message || 'Ошибка при добавлении фотографии');
        },
    });

    const deleteMutation = useMutation({
        mutationFn: photoService.delete,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['photos'] });
            showSuccess('Фотография успешно удалена');
            setIsDeleteModalOpen(false);
            setPhotoToDelete(null);
        },
        onError: (error: Error) => {
            showError(error.message || 'Ошибка при удалении фотографии');
        },
    });

    const resetForm = () => {
        setFormData({ fileName: '', filePath: '', sessionId: 0 });
        setPreviewUrl('');
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.fileName || !formData.filePath || formData.sessionId === 0) {
            showError('Заполните все поля');
            return;
        }
        createMutation.mutate(formData);
    };

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            const url = URL.createObjectURL(file);
            setPreviewUrl(url);
            setFormData({
                ...formData,
                fileName: file.name,
                filePath: url,
            });
        }
    };

    const handleDeleteClick = (photo: Photo, e: React.MouseEvent) => {
        e.stopPropagation();
        setPhotoToDelete(photo);
        setIsDeleteModalOpen(true);
    };

    const confirmDelete = () => {
        if (photoToDelete) {
            deleteMutation.mutate(photoToDelete.id);
        }
    };

    const getSessionInfo = (sessionId: number) => {
        const session = sessions?.find(s => s.id === sessionId);
        if (!session) return null;
        return {
            clientName: `${session.clientName} ${session.clientLastName}`,
            serviceName: getRussianServiceName(session.serviceName || ''),
            date: new Date(session.date).toLocaleDateString('ru-RU'),
        };
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

    // СОЗДАЕМ ФОТОГРАФИИ ДЛЯ ОТОБРАЖЕНИЯ ИЗ СЕССИЙ
    // Если в БД нет фото, показываем ваши PNG из assets
    const getDisplayPhotos = (): Photo[] => {
        if (!sessions || sessions.length === 0) return [];

        // Если в БД есть фото, используем их
        if (photos && photos.length > 0) {
            return photos;
        }

        // Иначе создаем временные фото из ваших PNG
        return sessions.map((session, idx) => {
            const serviceType = session.serviceName || 'WEDDING';
            const photoPath = getPhotoForService(serviceType, idx);
            return {
                id: session.id,
                fileName: `${getRussianServiceName(serviceType)}.png`,
                filePath: photoPath,
                uploadDate: new Date().toISOString(),
                sessionId: session.id,
            } as Photo;
        });
    };

    const displayPhotos = getDisplayPhotos();

    if (isLoading) return <LoadingSpinner />;

    return (
        <div className="min-h-screen pt-20 px-4">
            <div className="container mx-auto">
                {/* Header */}
                <div className="flex justify-between items-center mb-8 flex-wrap gap-4">
                    <div>
                        <h1 className="text-4xl font-bold bg-gradient-to-r from-rose-400 via-purple-400 to-indigo-400 bg-clip-text text-transparent">
                            Фотографии
                        </h1>
                        <p className="text-purple-300/70 mt-1">Управление фотографиями</p>
                    </div>
                    <button
                        onClick={() => setIsUploadModalOpen(true)}
                        className="bg-gradient-to-r from-rose-500 to-purple-600 hover:from-rose-600 hover:to-purple-700 text-white px-5 py-2.5 rounded-xl font-semibold transition-all duration-300 flex items-center gap-2 shadow-lg shadow-rose-500/25"
                    >
                        <Upload className="w-5 h-5" />
                        Загрузить фото
                    </button>
                </div>

                {/* Photos Grid */}
                {displayPhotos.length > 0 ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                        {displayPhotos.map((photo, idx) => {
                            const sessionInfo = getSessionInfo(photo.sessionId);
                            return (
                                <motion.div
                                    key={photo.id}
                                    initial={{ opacity: 0, y: 20 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    transition={{ delay: idx * 0.05 }}
                                    whileHover={{ y: -5 }}
                                    className="glass-card overflow-hidden cursor-pointer group"
                                    onClick={() => {
                                        setSelectedPhoto(photo);
                                        setIsViewModalOpen(true);
                                    }}
                                >
                                    {/* Фото */}
                                    <div className="relative h-48 overflow-hidden bg-gradient-to-br from-purple-900/50 to-pink-900/50">
                                        <img
                                            src={photo.filePath}
                                            alt={photo.fileName}
                                            className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                                            onError={(e) => {
                                                (e.target as HTMLImageElement).src = svadba1;
                                            }}
                                        />
                                        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>

                                        {/* Кнопки действий */}
                                        <div className="absolute top-2 right-2 flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                                            <button
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    setSelectedPhoto(photo);
                                                    setIsViewModalOpen(true);
                                                }}
                                                className="p-2 bg-black/50 rounded-full hover:bg-black/70 transition"
                                                title="Просмотр"
                                            >
                                                <ZoomIn className="w-4 h-4 text-white" />
                                            </button>
                                            <button
                                                onClick={(e) => handleDeleteClick(photo, e)}
                                                className="p-2 bg-red-500/70 rounded-full hover:bg-red-600 transition"
                                                title="Удалить"
                                            >
                                                <Trash2 className="w-4 h-4 text-white" />
                                            </button>
                                        </div>
                                    </div>

                                    {/* Информация */}
                                    <div className="p-4">
                                        <p className="font-semibold text-white text-sm truncate" title={photo.fileName}>
                                            {photo.fileName}
                                        </p>
                                        <p className="text-xs text-white/40 mt-1">
                                            {formatDate(photo.uploadDate)}
                                        </p>
                                        {sessionInfo && (
                                            <div className="mt-2 pt-2 border-t border-white/10">
                                                <p className="text-xs text-white/60 flex items-center gap-1">
                                                    <Calendar className="w-3 h-3" />
                                                    {sessionInfo.date}
                                                </p>
                                                <p className="text-xs text-white/60 flex items-center gap-1 mt-1">
                                                    <User className="w-3 h-3" />
                                                    {sessionInfo.clientName}
                                                </p>
                                                <p className="text-xs text-white/60 flex items-center gap-1 mt-1">
                                                    <Camera className="w-3 h-3" />
                                                    {sessionInfo.serviceName}
                                                </p>
                                            </div>
                                        )}
                                    </div>
                                </motion.div>
                            );
                        })}
                    </div>
                ) : (
                    <div className="glass-card p-12 text-center">
                        <div className="text-6xl mb-4">🖼️</div>
                        <h3 className="text-xl font-semibold text-white mb-2">Нет фотографий</h3>
                        <p className="text-white/40 mb-4">
                            {sessions && sessions.length > 0
                                ? 'Добавьте фотографии для фотосессий'
                                : 'Сначала создайте фотосессию'}
                        </p>
                        {sessions && sessions.length > 0 && (
                            <button
                                onClick={() => setIsUploadModalOpen(true)}
                                className="bg-gradient-to-r from-rose-500 to-purple-600 hover:from-rose-600 hover:to-purple-700 text-white px-5 py-2 rounded-xl font-semibold transition-all duration-300 flex items-center gap-2 mx-auto"
                            >
                                <Upload className="w-4 h-4" />
                                Загрузить фото
                            </button>
                        )}
                    </div>
                )}

                {/* Upload Modal */}
                <Modal
                    isOpen={isUploadModalOpen}
                    onClose={() => {
                        setIsUploadModalOpen(false);
                        resetForm();
                    }}
                    title="Загрузка фотографии"
                    size="md"
                >
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label htmlFor="fileInput" className="block text-sm font-medium text-gray-700 mb-2">
                                Выберите файл *
                            </label>
                            <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-rose-500 transition cursor-pointer">
                                <input
                                    type="file"
                                    accept="image/*"
                                    onChange={handleFileChange}
                                    className="hidden"
                                    id="fileInput"
                                />
                                <label htmlFor="fileInput" className="cursor-pointer block">
                                    {previewUrl ? (
                                        <img
                                            src={previewUrl}
                                            alt="Preview"
                                            className="max-h-48 mx-auto rounded-lg object-contain"
                                        />
                                    ) : (
                                        <div className="py-8">
                                            <Upload className="w-12 h-12 mx-auto text-gray-400 mb-2" />
                                            <p className="text-gray-500">Нажмите или перетащите файл</p>
                                            <p className="text-xs text-gray-400 mt-1">PNG, JPG, JPEG до 10MB</p>
                                        </div>
                                    )}
                                </label>
                            </div>
                        </div>

                        <div>
                            <label htmlFor="fileName" className="block text-sm font-medium text-gray-700 mb-2">
                                Название файла *
                            </label>
                            <input
                                id="fileName"
                                type="text"
                                required
                                value={formData.fileName}
                                onChange={(e) => setFormData({ ...formData, fileName: e.target.value })}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-rose-500 focus:border-transparent"
                                placeholder="wedding_photo_001.jpg"
                            />
                        </div>

                        <div>
                            <label htmlFor="sessionId" className="block text-sm font-medium text-gray-700 mb-2">
                                Фотосессия *
                            </label>
                            <select
                                id="sessionId"
                                required
                                value={formData.sessionId}
                                onChange={(e) => setFormData({ ...formData, sessionId: Number.parseInt(e.target.value, 10) })}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-rose-500 focus:border-transparent"
                            >
                                <option value={0}>Выберите фотосессию</option>
                                {sessions?.map((session) => {
                                    const sessionInfo = getSessionInfo(session.id);
                                    return (
                                        <option key={session.id} value={session.id}>
                                            #{session.id} - {sessionInfo?.clientName} - {sessionInfo?.serviceName} ({new Date(session.date).toLocaleDateString()})
                                        </option>
                                    );
                                })}
                            </select>
                        </div>

                        <div className="flex gap-3 justify-end pt-4">
                            <button
                                type="button"
                                onClick={() => {
                                    setIsUploadModalOpen(false);
                                    resetForm();
                                }}
                                className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition"
                            >
                                Отмена
                            </button>
                            <button
                                type="submit"
                                disabled={createMutation.isPending}
                                className="px-4 py-2 bg-gradient-to-r from-rose-500 to-purple-600 text-white rounded-lg hover:from-rose-600 hover:to-purple-700 transition-all disabled:opacity-50"
                            >
                                {createMutation.isPending ? 'Добавление...' : 'Добавить'}
                            </button>
                        </div>
                    </form>
                </Modal>

                {/* View Photo Modal */}
                <Modal
                    isOpen={isViewModalOpen}
                    onClose={() => setIsViewModalOpen(false)}
                    title={selectedPhoto?.fileName || 'Просмотр фото'}
                    size="lg"
                >
                    {selectedPhoto && (
                        <div className="space-y-4">
                            <img
                                src={selectedPhoto.filePath}
                                alt={selectedPhoto.fileName}
                                className="w-full rounded-lg object-contain max-h-96"
                                onError={(e) => {
                                    (e.target as HTMLImageElement).src = svadba1;
                                }}
                            />
                            <div className="bg-gray-50 rounded-lg p-4 space-y-2">
                                <p><span className="font-medium text-gray-700">Название:</span> {selectedPhoto.fileName}</p>
                                <p><span className="font-medium text-gray-700">Дата загрузки:</span> {formatDate(selectedPhoto.uploadDate)}</p>
                                <p><span className="font-medium text-gray-700">Фотосессия:</span> #{selectedPhoto.sessionId}</p>
                                {(() => {
                                    const sessionInfo = getSessionInfo(selectedPhoto.sessionId);
                                    return sessionInfo ? (
                                        <>
                                            <p><span className="font-medium text-gray-700">Клиент:</span> {sessionInfo.clientName}</p>
                                            <p><span className="font-medium text-gray-700">Услуга:</span> {sessionInfo.serviceName}</p>
                                            <p><span className="font-medium text-gray-700">Дата съемки:</span> {sessionInfo.date}</p>
                                        </>
                                    ) : null;
                                })()}
                            </div>
                            <div className="flex gap-3 justify-end">
                                <button
                                    onClick={() => setIsViewModalOpen(false)}
                                    className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition"
                                >
                                    Закрыть
                                </button>
                                <button
                                    onClick={() => {
                                        setIsViewModalOpen(false);
                                        if (selectedPhoto) {
                                            setPhotoToDelete(selectedPhoto);
                                            setIsDeleteModalOpen(true);
                                        }
                                    }}
                                    className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition"
                                >
                                    Удалить
                                </button>
                            </div>
                        </div>
                    )}
                </Modal>

                {/* Delete Confirmation Modal */}
                <Modal
                    isOpen={isDeleteModalOpen}
                    onClose={() => {
                        setIsDeleteModalOpen(false);
                        setPhotoToDelete(null);
                    }}
                    title="Подтверждение удаления"
                    size="sm"
                >
                    <div className="space-y-4">
                        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                            <p className="text-red-800 font-medium">
                                Вы уверены, что хотите удалить эту фотографию?
                            </p>
                            {photoToDelete && (
                                <p className="text-sm text-red-600 mt-2">
                                    {photoToDelete.fileName}
                                </p>
                            )}
                            <p className="text-xs text-red-500 mt-2">
                                Это действие нельзя отменить.
                            </p>
                        </div>
                        <div className="flex gap-3 justify-end">
                            <button
                                onClick={() => {
                                    setIsDeleteModalOpen(false);
                                    setPhotoToDelete(null);
                                }}
                                className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition"
                            >
                                Отмена
                            </button>
                            <button
                                onClick={confirmDelete}
                                disabled={deleteMutation.isPending}
                                className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition disabled:opacity-50"
                            >
                                {deleteMutation.isPending ? 'Удаление...' : 'Удалить'}
                            </button>
                        </div>
                    </div>
                </Modal>
            </div>
        </div>
    );
};

export { PhotoList };