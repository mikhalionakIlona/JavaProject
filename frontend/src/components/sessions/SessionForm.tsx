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
        <form onSubmit={handleSubmit} className="space-y-4">
            <div>
                <label htmlFor="date" className="block text-sm font-medium text-gray-700 mb-1">Дата и время *</label>
                <input
                    id="date"
                    type="datetime-local"
                    required
                    value={formData.date}
                    onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                    className="input-field"
                />
            </div>
            <div>
                <label htmlFor="clientId" className="block text-sm font-medium text-gray-700 mb-1">Клиент *</label>
                <select
                    id="clientId"
                    required
                    value={formData.clientId}
                    onChange={(e) => setFormData({ ...formData, clientId: Number.parseInt(e.target.value, 10) })}
                    className="input-field"
                >
                    <option value={0}>Выберите клиента</option>
                    {clients.map((client) => (
                        <option key={client.id} value={client.id}>
                            {client.firstName} {client.lastName} - {client.phone}
                        </option>
                    ))}
                </select>
            </div>
            <div>
                <label htmlFor="photographerId" className="block text-sm font-medium text-gray-700 mb-1">Фотограф *</label>
                <select
                    id="photographerId"
                    required
                    value={formData.photographerId}
                    onChange={(e) => setFormData({ ...formData, photographerId: Number.parseInt(e.target.value, 10) })}
                    className="input-field"
                >
                    <option value={0}>Выберите фотографа</option>
                    {photographers.map((photographer) => (
                        <option key={photographer.id} value={photographer.id}>
                            {photographer.firstName} {photographer.lastName} - {photographer.hourlyRate} ₽/час
                        </option>
                    ))}
                </select>
            </div>
            <div>
                <label htmlFor="serviceId" className="block text-sm font-medium text-gray-700 mb-1">Услуга *</label>
                <select
                    id="serviceId"
                    required
                    value={formData.serviceId}
                    onChange={(e) => setFormData({ ...formData, serviceId: Number.parseInt(e.target.value, 10) })}
                    className="input-field"
                >
                    <option value={0}>Выберите услугу</option>
                    {services.map((service) => (
                        <option key={service.id} value={service.id}>
                            {service.name}
                        </option>
                    ))}
                </select>
            </div>
            <div className="flex gap-3 justify-end pt-4">
                <button type="button" onClick={onCancel} className="px-4 py-2 bg-gray-200 rounded-lg hover:bg-gray-300">
                    Отмена
                </button>
                <button type="submit" disabled={isLoading} className="px-4 py-2 btn-primary">
                    {isLoading ? 'Сохранение...' : 'Создать'}
                </button>
            </div>
        </form>
    );
};

export { SessionForm };