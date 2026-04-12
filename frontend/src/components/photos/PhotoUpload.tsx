import React, { useState } from 'react';
import { PhotoSession } from '../../types';
import { PhotoIcon } from '@heroicons/react/24/outline';

interface PhotoUploadProps {
    sessions: PhotoSession[];
    onUpload: (fileName: string, filePath: string, sessionId: number) => void;
    isLoading: boolean;
}

const PhotoUpload: React.FC<PhotoUploadProps> = ({ sessions, onUpload, isLoading }) => {
    const [formData, setFormData] = useState({
        fileName: '',
        filePath: '',
        sessionId: 0,
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.fileName || !formData.filePath || formData.sessionId === 0) {
            return;
        }
        onUpload(formData.fileName, formData.filePath, formData.sessionId);
        setFormData({ fileName: '', filePath: '', sessionId: 0 });
    };

    return (
        <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-lg p-6">
            <div className="flex items-center gap-2 mb-4">
                <PhotoIcon className="w-5 h-5 text-primary-500" />
                <h3 className="text-lg font-semibold text-gray-800">Загрузить фотографию</h3>
            </div>

            <div className="space-y-4">
                <div>
                    <label htmlFor="fileName" className="block text-sm font-medium text-gray-700 mb-1">Название файла *</label>
                    <input
                        id="fileName"
                        type="text"
                        required
                        value={formData.fileName}
                        onChange={(e) => setFormData({ ...formData, fileName: e.target.value })}
                        className="input-field"
                        placeholder="wedding_photo_001.jpg"
                    />
                </div>
                <div>
                    <label htmlFor="filePath" className="block text-sm font-medium text-gray-700 mb-1">Путь к файлу (URL) *</label>
                    <input
                        id="filePath"
                        type="text"
                        required
                        value={formData.filePath}
                        onChange={(e) => setFormData({ ...formData, filePath: e.target.value })}
                        className="input-field"
                        placeholder="https://example.com/photos/photo.jpg"
                    />
                </div>
                <div>
                    <label htmlFor="sessionId" className="block text-sm font-medium text-gray-700 mb-1">Фотосессия *</label>
                    <select
                        id="sessionId"
                        required
                        value={formData.sessionId}
                        onChange={(e) => setFormData({ ...formData, sessionId: Number.parseInt(e.target.value, 10) })}
                        className="input-field"
                    >
                        <option value={0}>Выберите фотосессию</option>
                        {sessions.map((session) => (
                            <option key={session.id} value={session.id}>
                                #{session.id} - {session.clientName} {session.clientLastName} - {new Date(session.date).toLocaleDateString()}
                            </option>
                        ))}
                    </select>
                </div>
                <button type="submit" disabled={isLoading} className="w-full btn-primary">
                    {isLoading ? 'Загрузка...' : 'Загрузить фото'}
                </button>
            </div>
        </form>
    );
};

export { PhotoUpload };