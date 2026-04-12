import api from './api';
import { Client, ClientCreateDto } from '../types';

export const clientService = {
    getAll: async (): Promise<Client[]> => {
        const response = await api.get('/clients');
        // Убедимся, что у каждого клиента есть массив photoSessions
        const clients = response.data;
        return clients.map((client: Client) => ({
            ...client,
            photoSessions: client.photoSessions || []
        }));
    },

    getById: async (id: number): Promise<Client> => {
        const response = await api.get(`/clients/${id}`);
        const client = response.data;
        return {
            ...client,
            photoSessions: client.photoSessions || []
        };
    },

    getWithSessions: async (id: number): Promise<Client> => {
        // Если у вас есть специальный эндпоинт для получения клиента с сессиями
        try {
            const response = await api.get(`/clients/${id}/with-sessions`);
            return response.data;
        } catch (error) {
            // Если нет, получаем отдельно
            console.warn('Failed to get client with sessions from dedicated endpoint, falling back to separate requests:', error);
            const client = await clientService.getById(id);
            const sessionsResponse = await api.get(`/photo-sessions/client/${id}`);
            return {
                ...client,
                photoSessions: sessionsResponse.data || []
            };
        }
    },

    getByEmail: async (email: string): Promise<Client | null> => {
        try {
            const clients = await clientService.getAll();
            return clients.find(client => client.email === email) || null;
        } catch (error) {
            console.error('Error getting client by email:', error);
            return null;
        }
    },

    create: async (data: ClientCreateDto): Promise<Client> => {
        const response = await api.post('/clients', data);
        return {
            ...response.data,
            photoSessions: []
        };
    },

    update: async (id: number, data: Partial<ClientCreateDto>): Promise<Client> => {
        const response = await api.put(`/clients/${id}`, data);
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await api.delete(`/clients/${id}`);
    },
};