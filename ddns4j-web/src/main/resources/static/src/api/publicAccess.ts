import apiClient from './client'

export const publicAccessApi = {
  getPublicAccessDisabled() {
    return apiClient.get<{ publicAccessDisabled: boolean }>('/publicAccess/publicAccessDisabled')
  },

  setPublicAccessDisabled(value: boolean) {
    return apiClient.post<string>('/publicAccess/publicAccessDisabled', { value })
  },
}
