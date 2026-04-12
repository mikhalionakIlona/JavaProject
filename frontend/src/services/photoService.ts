import api from './api';
import { Photo } from '../types';

export const photoService = {
    getAll: async (): Promise<Photo[]> => {
        const response = await api.get('/photos');
        return response.data;
    },

    getById: async (id: number): Promise<Photo> => {
        const response = await api.get(`/photos/${id}`);
        return response.data;
    },

    getBySessionId: async (sessionId: number): Promise<Photo[]> => {
        const response = await api.get(`/photos/by-session/${sessionId}`);
        return response.data;
    },

    create: async (fileName: string, filePath: string, sessionId: number): Promise<Photo> => {
        const response = await api.post('/photos', { fileName, filePath, sessionId });
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await api.delete(`/photos/${id}`);
    },
};