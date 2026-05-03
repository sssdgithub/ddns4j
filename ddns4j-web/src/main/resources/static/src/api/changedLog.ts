import apiClient from './client'

export interface ChangedLog {
  timestamp: string
  content: string
}

export const changedLogApi = {
  getLogs() {
    return apiClient.post<string>('/changedLog/logs')
  },
}
