import api from './api';
import { PhotoService, ServiceType } from '../types';

export const serviceService = {
    getAll: async (): Promise<PhotoService[]> => {
        const response = await api.get('/services');
        return response.data;
    },

    getById: async (id: number): Promise<PhotoService> => {
        const response = await api.get(`/services/${id}`);
        return response.data;
    },

    create: async (serviceType: ServiceType): Promise<PhotoService> => {
        const response = await api.post('/services', { serviceType });
        return response.data;
    },

    update: async (id: number, serviceType: ServiceType): Promise<PhotoService> => {
        const response = await api.put(`/services/${id}`, { serviceType });
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await api.delete(`/services/${id}`);
    },
};