import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'

export interface ApiResponse<T = any> {
  status: number
  msg: string
  data: T
}

// Custom axios instance with typed interceptor
interface ApiClient {
  get<T = any>(url: string, config?: any): Promise<T>
  post<T = any>(url: string, data?: any, config?: any): Promise<T>
  put<T = any>(url: string, data?: any, config?: any): Promise<T>
  delete<T = any>(url: string, config?: any): Promise<T>
}

const axiosInstance: AxiosInstance = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

axiosInstance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { data } = response
    if (data.status === 0) {
      return data.data
    } else {
      throw new Error(data.msg || '请求失败')
    }
  },
  (error) => {
    const message = error.response?.data?.msg || error.message || '网络错误'
    return Promise.reject(new Error(message))
  }
)

// Create typed API client
const apiClient: ApiClient = {
  get: <T = any>(url: string, config?: any) => axiosInstance.get(url, config) as Promise<T>,
  post: <T = any>(url: string, data?: any, config?: any) => axiosInstance.post(url, data, config) as Promise<T>,
  put: <T = any>(url: string, data?: any, config?: any) => axiosInstance.put(url, data, config) as Promise<T>,
  delete: <T = any>(url: string, config?: any) => axiosInstance.delete(url, config) as Promise<T>,
}

export default apiClient
