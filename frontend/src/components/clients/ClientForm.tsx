import React, { useEffect } from 'react';
import { Client, ClientCreateDto } from '../../types';

interface ClientFormProps {
    initialData?: Client;
    onSubmit: (data: ClientCreateDto) => void;
    onCancel: () => void;
    isLoading: boolean;
}

const ClientForm: React.FC<ClientFormProps> = ({
                                                   initialData,
                                                   onSubmit,
                                                   onCancel,
                                                   isLoading,
                                               }) => {
    const [formData, setFormData] = React.useState<ClientCreateDto>({
        firstName: '',
        lastName: '',
        phone: '',
        email: '',
    });

    useEffect(() => {
        if (initialData) {
            setFormData({
                firstName: initialData.firstName,
                lastName: initialData.lastName,
                phone: initialData.phone,
                email: initialData.email,
            });
        }
    }, [initialData]);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onSubmit(formData);
    };

    const idPrefix = 'client-form';
    const firstNameId = `${idPrefix}-first-name`;
    const lastNameId = `${idPrefix}-last-name`;
    const phoneId = `${idPrefix}-phone`;
    const emailId = `${idPrefix}-email`;

    return (
        <form onSubmit={handleSubmit} className="booking-form">
            {}
            <div className="form-group">
                <label htmlFor={firstNameId} className="form-label">
                    Имя *
                </label>
                <input
                    id={firstNameId}
                    type="text"
                    required
                    value={formData.firstName}
                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                    className="form-input"
                    placeholder="Иван"
                    aria-required="true"
                />
            </div>

            {}
            <div className="form-group">
                <label htmlFor={lastNameId} className="form-label">
                    Фамилия *
                </label>
                <input
                    id={lastNameId}
                    type="text"
                    required
                    value={formData.lastName}
                    onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                    className="form-input"
                    placeholder="Иванов"
                    aria-required="true"
                />
            </div>

            {}
            <div className="form-group">
                <label htmlFor={phoneId} className="form-label">
                    Телефон *
                </label>
                <input
                    id={phoneId}
                    type="tel"
                    required
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    className="form-input"
                    placeholder="+375291234567"
                    aria-required="true"
                />
            </div>

            {}
            <div className="form-group">
                <label htmlFor={emailId} className="form-label">
                    Email *
                </label>
                <input
                    id={emailId}
                    type="email"
                    required
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    className="form-input"
                    placeholder="ivan@example.com"
                    aria-required="true"
                />
            </div>

            {}
            <div className="form-actions">
                <button
                    type="button"
                    onClick={onCancel}
                    className="btn-cancel"
                >
                    Отмена
                </button>
                <button
                    type="submit"
                    disabled={isLoading}
                    className="btn-save"
                >
                    {isLoading ? 'Сохранение...' : 'Сохранить'}
                </button>
            </div>
        </form>
    );
};

export { ClientForm };