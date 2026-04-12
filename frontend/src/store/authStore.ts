import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface User {
    id: number;
    name: string;
    email: string;
    phone?: string;
    role: 'user' | 'admin';
}

interface AuthState {
    user: User | null;
    token: string | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (user: User, token: string) => void;
    logout: () => void;
    setUser: (user: User) => void;
    setLoading: (loading: boolean) => void;
    hydrate: () => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set, get) => ({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: true,

            login: (user, token) => {
                set({ user, token, isAuthenticated: true, isLoading: false });
            },

            logout: () => {
                localStorage.removeItem('auth-storage');
                set({ user: null, token: null, isAuthenticated: false, isLoading: false });
            },

            setUser: (user) => set({ user }),

            setLoading: (loading) => set({ isLoading: loading }),

            hydrate: () => {
                const stored = localStorage.getItem('auth-storage');
                if (stored) {
                    try {
                        const parsed = JSON.parse(stored);
                        if (parsed?.state?.user) {
                            set({
                                user: parsed.state.user,
                                token: parsed.state.token,
                                isAuthenticated: true,
                                isLoading: false
                            });
                        } else {
                            set({ isLoading: false });
                        }
                    } catch (error) {
                        console.error('Error hydrating auth state:', error);
                        set({ isLoading: false });
                    }
                } else {
                    set({ isLoading: false });
                }
            },
        }),
        {
            name: 'auth-storage',
            onRehydrateStorage: () => (state) => {
                if (state) {
                    state.isLoading = false;
                }
            },
        }
    )
);