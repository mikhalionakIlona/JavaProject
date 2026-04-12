export interface Client {
    id: number;
    firstName: string;
    lastName: string;
    phone: string;
    email: string;
    photoSessions?: PhotoSession[];
}

export interface Photographer {
    id: number;
    firstName: string;
    lastName: string;
    patronymic?: string;
    phone: string;
    hourlyRate: number;
    photoSessions?: PhotoSession[];
    services?: PhotoService[];
}

export interface PhotoService {
    id: number;
    serviceType: ServiceType;
    name: string;
    basePrice: number;
    photographers?: Photographer[];
    photoSessions?: PhotoSession[];
}

export interface PhotoSession {
    id: number;
    date: string;
    totalPrice: number;
    clientId: number;
    clientName?: string;
    clientLastName?: string;
    clientEmail?: string;
    photographerId: number;
    photographerName?: string;
    serviceId: number;
    serviceName?: string;
    status?: string;
    photos?: Photo[];
}

export interface Photo {
    id: number;
    fileName: string;
    filePath: string;
    uploadDate: string;
    sessionId: number;
}

export enum ServiceType {
    WEDDING = 'WEDDING',
    PORTRAIT = 'PORTRAIT',
    PRODUCT = 'PRODUCT',
    CORPORATE = 'CORPORATE',
    FAMILY = 'FAMILY'
}

export const ServiceTypeLabels: Record<ServiceType, string> = {
    [ServiceType.WEDDING]: 'Свадебная съемка',
    [ServiceType.PORTRAIT]: 'Портретная съемка',
    [ServiceType.PRODUCT]: 'Предметная съемка',
    [ServiceType.CORPORATE]: 'Корпоративная съемка',
    [ServiceType.FAMILY]: 'Семейная съемка'
};

export const ServiceTypePrices: Record<ServiceType, number> = {
    [ServiceType.WEDDING]: 5000,
    [ServiceType.PORTRAIT]: 3000,
    [ServiceType.PRODUCT]: 2500,
    [ServiceType.CORPORATE]: 4000,
    [ServiceType.FAMILY]: 3500
};

export interface ClientCreateDto {
    firstName: string;
    lastName: string;
    phone: string;
    email: string;
}

export interface PhotographerCreateDto {
    firstName: string;
    lastName: string;
    patronymic?: string;
    phone: string;
    hourlyRate: number;
}

export interface PhotoSessionCreateDto {
    date: string;
    clientId: number;
    photographerId: number;
    serviceId: number;
}