/**
 * User API Servisi
 * Kullanıcı yönetimi
 */

import apiClient from './api';

export const userService = {
  // Oturum açma
  login: async (email, password) => {
    try {
      const response = await apiClient.post('/auth/login', {
        email,
        password,
      });
      if (response.data.token) {
        localStorage.setItem('user', JSON.stringify(response.data));
        apiClient.defaults.headers.common['Authorization'] = `Bearer ${response.data.token}`;
      }
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Oturum kapat
  logout: () => {
    localStorage.removeItem('user');
    delete apiClient.defaults.headers.common['Authorization'];
  },

  // Kayıt ol
  register: async (email, password, name) => {
    try {
      const response = await apiClient.post('/auth/register', {
        email,
        password,
        name,
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Profil bilgisi
  getProfile: async () => {
    try {
      const response = await apiClient.get('/users/profile');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Profil güncelleme
  updateProfile: async (data) => {
    try {
      const response = await apiClient.put('/users/profile', data);
      return response.data;
    } catch (error) {
      throw error;
    }
  },
};
