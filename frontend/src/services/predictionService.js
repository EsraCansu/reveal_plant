/**
 * Prediction API Servisi
 * Tahmin API'si ile iletişim
 */

import apiClient from './api';

export const predictionService = {
  // Tahmin yap
  getPrediction: async (imageFile) => {
    const formData = new FormData();
    formData.append('file', imageFile);
    
    try {
      const response = await apiClient.post('/predictions', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Geçmiş tahminleri getir
  getPredictionHistory: async (limit = 10, page = 0) => {
    try {
      const response = await apiClient.get('/predictions', {
        params: { limit, page },
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Tekil tahmin detayı
  getPredictionDetail: async (id) => {
    try {
      const response = await apiClient.get(`/predictions/${id}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Tahmin sil
  deletePrediction: async (id) => {
    try {
      const response = await apiClient.delete(`/predictions/${id}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },
};
