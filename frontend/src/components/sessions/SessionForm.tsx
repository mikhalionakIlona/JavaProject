import React, { useEffect, useState } from 'react';
import { PhotoSessionCreateDto, Client, Photographer, PhotoService } from '../../types';
import { AlertCircle, CheckCircle } from 'lucide-react';

interface SessionFormProps {
    initialData?: PhotoSessionCreateDto;
    clients: Client[];
    photographers: Photographer[];
    services: PhotoService[];
    onSubmit: (data: PhotoSessionCreateDto) => void;
    onCancel: () => void;
    isLoading: boolean;
    existingSessions?: any[];
}

const SessionForm: React.FC<SessionFormProps> = ({
                                                     initialData,
                                                     clients,
                                                     photographers,
                                                     services,
                                                     onSubmit,
                                                     onCancel,
                                                     isLoading,
                                                     existingSessions = [],
                                                 }) => {
    const [formData, setFormData] = useState<PhotoSessionCreateDto>({
        date: '',
        clientId: 0,
        photographerId: 0,
        serviceId: 0,
    });
    const [availabilityMessage, setAvailabilityMessage] = useState<{ type: 'error' | 'success'; text: string } | null>(null);
    const [isChecking, setIsChecking] = useState(false);

    useEffect(() => {
        if (initialData) {
            setFormData(initialData);
        }
    }, [initialData]);

    const checkPhotographerAvailability = (photographerId: number, dateTime: string): { available: boolean; message?: string } => {
        if (!photographerId || !dateTime || !existingSessions.length) {
            return { available: true };
        }

        const selectedDate = new Date(dateTime);
        const selectedEndTime = new Date(selectedDate.getTime() + 2 * 60 * 60 * 1000);

        const conflictingSession = existingSessions.find((session: any) => {
            if (session.photographerId !== photographerId) return false;

            const sessionDate = new Date(session.date);
            const sessionEndTime = new Date(sessionDate.getTime() + 2 * 60 * 60 * 1000);

            return (selectedDate < sessionEndTime && selectedEndTime > sessionDate);
        });

        if (conflictingSession) {
            const photographer = photographers.find(p => p.id === photographerId);
            const photographerName = photographer ? `${photographer.firstName} ${photographer.lastName}` : 'Фотограф';

            const conflictDate = new Date(conflictingSession.date).toLocaleString('ru-RU', {
                day: 'numeric',
                month: 'long',
                hour: '2-digit',
                minute: '2-digit'
            });

            return {
                available: false,
                message: `❌ ${photographerName} уже занят ${conflictDate}. Сессия длится 2 часа. Пожалуйста, выберите другое время.`
            };
        }

        return { available: true };
    };

    useEffect(() => {
        if (formData.photographerId && formData.date) {
            setIsChecking(true);
            setAvailabilityMessage(null);

            const timer = setTimeout(() => {
                const result = checkPhotographerAvailability(formData.photographerId, formData.date);
                if (!result.available) {
                    setAvailabilityMessage({ type: 'error', text: result.message! });
                } else {
                    setAvailabilityMessage({ type: 'success', text: '✓ Фотограф свободен в выбранное время' });
                }
                setIsChecking(false);
            }, 300);

            return () => clearTimeout(timer);
        } else {
            setAvailabilityMessage(null);
        }
    }, [formData.photographerId, formData.date, existingSessions]);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        if (formData.photographerId && formData.date) {
            const result = checkPhotographerAvailability(formData.photographerId, formData.date);
            if (!result.available) {
                setAvailabilityMessage({ type: 'error', text: result.message! });
                return;
            }
        }

        onSubmit(formData);
    };

    const getMinDateTime = () => {
        const now = new Date();
        now.setMinutes(0);
        now.setSeconds(0);
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(Math.ceil(now.getMinutes() / 30) * 30).padStart(2, '0');
        return `${year}-${month}-${day}T${hours}:${minutes}`;
    };

    return (
        <form onSubmit={handleSubmit} className="booking-form">
            <div className="form-group">
                <label htmlFor="date" className="form-label">Дата и время *</label>
                <input
                    id="date"
                    type="datetime-local"
                    required
                    min={getMinDateTime()}
                    value={formData.date}
                    onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                    className="form-input"
                />
                <p className="text-xs text-white/40 mt-1">
                    ⏰ Длительность сессии: 2 часа
                </p>
            </div>

            <div className="form-group">
                <label htmlFor="clientId" className="form-label">Клиент *</label>
                <select
                    id="clientId"
                    required
                    value={formData.clientId}
                    onChange={(e) => setFormData({ ...formData, clientId: Number.parseInt(e.target.value, 10) })}
                    className="form-input"
                >
                    <option value={0}>Выберите клиента</option>
                    {clients.map((client) => (
                        <option key={client.id} value={client.id}>
                            {client.firstName} {client.lastName} - {client.phone}
                        </option>
                    ))}
                </select>
            </div>

            <div className="form-group">
                <label htmlFor="photographerId" className="form-label">Фотограф *</label>
                <select
                    id="photographerId"
                    required
                    value={formData.photographerId}
                    onChange={(e) => setFormData({ ...formData, photographerId: Number.parseInt(e.target.value, 10) })}
                    className="form-input"
                >
                    <option value={0}>Выберите фотографа</option>
                    {photographers.map((photographer) => (
                        <option key={photographer.id} value={photographer.id}>
                            {photographer.firstName} {photographer.lastName} - {photographer.hourlyRate} BYN/час
                        </option>
                    ))}
                </select>
            </div>

            <div className="form-group">
                <label htmlFor="serviceId" className="form-label">Услуга *</label>
                <select
                    id="serviceId"
                    required
                    value={formData.serviceId}
                    onChange={(e) => setFormData({ ...formData, serviceId: Number.parseInt(e.target.value, 10) })}
                    className="form-input"
                >
                    <option value={0}>Выберите услугу</option>
                    {services.map((service) => (
                        <option key={service.id} value={service.id}>
                            {service.name}
                        </option>
                    ))}
                </select>
            </div>

            {isChecking && (
                <div className="bg-blue-500/20 border border-blue-500/30 rounded-lg p-3">
                    <p className="text-blue-400 text-sm">⏳ Проверка доступности фотографа...</p>
                </div>
            )}

            {availabilityMessage && availabilityMessage.type === 'error' && (
                <div className="bg-red-500/20 border border-red-500/30 rounded-lg p-3 flex items-start gap-2">
                    <AlertCircle className="w-5 h-5 text-red-400 flex-shrink-0 mt-0.5" />
                    <div>
                        <p className="text-red-400 text-sm font-medium">{availabilityMessage.text}</p>
                    </div>
                </div>
            )}

            {availabilityMessage && availabilityMessage.type === 'success' && (
                <div className="bg-green-500/20 border border-green-500/30 rounded-lg p-3 flex items-start gap-2">
                    <CheckCircle className="w-5 h-5 text-green-400 flex-shrink-0 mt-0.5" />
                    <div>
                        <p className="text-green-400 text-sm font-medium">{availabilityMessage.text}</p>
                    </div>
                </div>
            )}

            <div className="form-actions">
                <button type="button" onClick={onCancel} className="btn-cancel">
                    Отмена
                </button>
                <button
                    type="submit"
                    disabled={isLoading || isChecking || (availabilityMessage?.type === 'error')}
                    className="btn-save"
                >
                    {isLoading ? 'Сохранение...' : 'Забронировать'}
                </button>
            </div>
        </form>
    );
};

export { SessionForm };