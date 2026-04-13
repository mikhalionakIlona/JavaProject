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
        <form onSubmit={handleSubmit} className="booking-form">
            <div className="form-grid">
                <div className="form-group">
                    <label htmlFor="firstName" className="form-label">Имя *</label>
                    <input
                        id="firstName"
                        type="text"
                        required
                        value={formData.firstName}
                        onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                        className="form-input"
                        placeholder="Иван"
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="lastName" className="form-label">Фамилия *</label>
                    <input
                        id="lastName"
                        type="text"
                        required
                        value={formData.lastName}
                        onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                        className="form-input"
                        placeholder="Иванов"
                    />
                </div>
            </div>

            <div className="form-group">
                <label htmlFor="patronymic" className="form-label">Отчество</label>
                <input
                    id="patronymic"
                    type="text"
                    value={formData.patronymic}
                    onChange={(e) => setFormData({ ...formData, patronymic: e.target.value })}
                    className="form-input"
                    placeholder="Иванович"
                />
            </div>

            <div className="form-group">
                <label htmlFor="phone" className="form-label">Телефон *</label>
                <input
                    id="phone"
                    type="tel"
                    required
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    className="form-input"
                    placeholder="+375291234567"
                />
            </div>

            <div className="form-group">
                <label htmlFor="hourlyRate" className="form-label">Почасовая ставка *</label>
                <input
                    id="hourlyRate"
                    type="number"
                    required
                    min="0"
                    step="100"
                    value={formData.hourlyRate}
                    onChange={(e) => setFormData({ ...formData, hourlyRate: Number.parseFloat(e.target.value) })}
                    className="form-input"
                    placeholder="0"
                />
            </div>

            <div className="form-actions">
                <button type="button" onClick={onCancel} className="btn-cancel">
                    Отмена
                </button>
                <button type="submit" disabled={isLoading} className="btn-save">
                    {isLoading ? 'Сохранение...' : 'Сохранить'}
                </button>
            </div>
        </form>
    );
};

export { PhotographerForm };