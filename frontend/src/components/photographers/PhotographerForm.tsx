import React, { useEffect } from 'react';
import { Photographer, PhotographerCreateDto } from '../../types';

interface PhotographerFormProps {
    initialData?: Photographer;
    onSubmit: (data: PhotographerCreateDto) => void;
    onCancel: () => void;
    isLoading: boolean;
}

const PhotographerForm: React.FC<PhotographerFormProps> = ({
                                                               initialData,
                                                               onSubmit,
                                                               onCancel,
                                                               isLoading,
                                                           }) => {
    const [formData, setFormData] = React.useState<PhotographerCreateDto>({
        firstName: '',
        lastName: '',
        patronymic: '',
        phone: '',
        hourlyRate: 0,
    });

    useEffect(() => {
        if (initialData) {
            setFormData({
                firstName: initialData.firstName,
                lastName: initialData.lastName,
                patronymic: initialData.patronymic || '',
                phone: initialData.phone,
                hourlyRate: initialData.hourlyRate,
            });
        }
    }, [initialData]);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onSubmit(formData);
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
                <div>
                    <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-1">Имя *</label>
                    <input
                        id="firstName"
                        type="text"
                        required
                        value={formData.firstName}
                        onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                        className="input-field"
                        placeholder="Иван"
                    />
                </div>
                <div>
                    <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-1">Фамилия *</label>
                    <input
                        id="lastName"
                        type="text"
                        required
                        value={formData.lastName}
                        onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                        className="input-field"
                        placeholder="Иванов"
                    />
                </div>
            </div>
            <div>
                <label htmlFor="patronymic" className="block text-sm font-medium text-gray-700 mb-1">Отчество</label>
                <input
                    id="patronymic"
                    type="text"
                    value={formData.patronymic}
                    onChange={(e) => setFormData({ ...formData, patronymic: e.target.value })}
                    className="input-field"
                    placeholder="Иванович"
                />
            </div>
            <div>
                <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-1">Телефон *</label>
                <input
                    id="phone"
                    type="tel"
                    required
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    className="input-field"
                    placeholder="+375291234567"
                />
            </div>
            <div>
                <label htmlFor="hourlyRate" className="block text-sm font-medium text-gray-700 mb-1">Почасовая ставка *</label>
                <input
                    id="hourlyRate"
                    type="number"
                    required
                    min="0"
                    step="100"
                    value={formData.hourlyRate}
                    onChange={(e) => setFormData({ ...formData, hourlyRate: Number.parseFloat(e.target.value) })}
                    className="input-field"
                    placeholder="0"
                />
            </div>
            <div className="flex gap-3 justify-end pt-4">
                <button type="button" onClick={onCancel} className="px-4 py-2 bg-gray-200 rounded-lg hover:bg-gray-300">
                    Отмена
                </button>
                <button type="submit" disabled={isLoading} className="px-4 py-2 btn-primary">
                    {isLoading ? 'Сохранение...' : 'Сохранить'}
                </button>
            </div>
        </form>
    );
};

export { PhotographerForm };