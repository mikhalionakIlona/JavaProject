import api from './api';
import { PhotoSession, PhotoSessionCreateDto } from '../types';

export const sessionService = {
    getAll: async (): Promise<PhotoSession[]> => {
        const response = await api.get('/photo-sessions');
        return response.data;
    },

    getById: async (id: number): Promise<PhotoSession> => {
        const response = await api.get(`/photo-sessions/${id}`);
        return response.data;
    },

    getByClientId: async (clientId: number): Promise<PhotoSession[]> => {
        const response = await api.get(`/photo-sessions/client/${clientId}`);
        return response.data;
    },

    create: async (data: PhotoSessionCreateDto): Promise<PhotoSession> => {
        const response = await api.post('/photo-sessions', data);
        return response.data;
    },

    update: async (id: number, data: Partial<PhotoSessionCreateDto>): Promise<PhotoSession> => {
        const response = await api.put(`/photo-sessions/${id}`, data);
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await api.delete(`/photo-sessions/${id}`);
    },
};