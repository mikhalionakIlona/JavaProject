import React, { useEffect } from 'react';
import { PhotoSessionCreateDto, Client, Photographer, PhotoService } from '../../types';

interface SessionFormProps {
    initialData?: PhotoSessionCreateDto;
    clients: Client[];
    photographers: Photographer[];
    services: PhotoService[];
    onSubmit: (data: PhotoSessionCreateDto) => void;
    onCancel: () => void;
    isLoading: boolean;
}

const SessionForm: React.FC<SessionFormProps> = ({
                                                     initialData,
                                                     clients,
                                                     photographers,
                                                     services,
                                                     onSubmit,
                                                     onCancel,
                                                     isLoading,
                                                 }) => {
    const [formData, setFormData] = React.useState<PhotoSessionCreateDto>({
        date: '',
        clientId: 0,
        photographerId: 0,
        serviceId: 0,
    });

    useEffect(() => {
        if (initialData) {
            setFormData(initialData);
        }
    }, [initialData]);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onSubmit(formData);
    };

    return (
        <form onSubmit={handleSubmit} className="booking-form">
            <div className="form-group">
                <label htmlFor="date" className="form-label">Дата и время *</label>
                <input
                    id="date"
                    type="datetime-local"
                    required
                    value={formData.date}
                    onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                    className="form-input"
                />
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

            <div className="form-actions">
                <button type="button" onClick={onCancel} className="btn-cancel">
                    Отмена
                </button>
                <button type="submit" disabled={isLoading} className="btn-save">
                    {isLoading ? 'Сохранение...' : 'Создать'}
                </button>
            </div>
        </form>
    );
};

export { SessionForm };