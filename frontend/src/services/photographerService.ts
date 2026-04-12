import api from './api';
import { Photographer, PhotographerCreateDto } from '../types';

export const photographerService = {
    getAll: async (): Promise<Photographer[]> => {
        const response = await api.get('/photographers');
        return response.data;
    },

    getById: async (id: number): Promise<Photographer> => {
        const response = await api.get(`/photographers/${id}`);
        return response.data;
    },

    create: async (data: PhotographerCreateDto): Promise<Photographer> => {
        const response = await api.post('/photographers', data);
        return response.data;
    },

    update: async (id: number, data: Partial<PhotographerCreateDto>): Promise<Photographer> => {
        const response = await api.put(`/photographers/${id}`, data);
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await api.delete(`/photographers/${id}`);
    },
    
    addService: async (photographerId: number, serviceId: number): Promise<Photographer> => {
        const response = await api.post(`/photographers/${photographerId}/services/${serviceId}`);
        return response.data;
    },

    removeService: async (photographerId: number, serviceId: number): Promise<Photographer> => {
        const response = await api.delete(`/photographers/${photographerId}/services/${serviceId}`);
        return response.data;
    },

    getServices: async (photographerId: number): Promise<any[]> => {
        const response = await api.get(`/photographers/${photographerId}/services`);
        return response.data;
    },
};