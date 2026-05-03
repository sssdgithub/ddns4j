import apiClient from './client'

export interface ParsingRecord {
  id?: number
  serviceProvider: number
  serviceProviderId: string
  serviceProviderSecret: string
  recordType: number
  ip?: string
  getIpMode: number
  getIpModeValue: string
  domain: string
  updateFrequency: number
  createDate?: string
  updateDate?: string
  creator?: number
  updater?: number
  serviceProviderName?: string
  recordTypeName?: string
  page?: number
  perPage?: number
}

export interface AmisPageResult<T> {
  total: number
  page: number
  items: T[]
}

export interface NetWorkSelectResponse {
  label: string
  value: string
}

export const parsingRecordApi = {
  getPage(params: ParsingRecord) {
    return apiClient.get<AmisPageResult<ParsingRecord>>('/parsingRecord/page', { params })
  },

  add(data: ParsingRecord) {
    return apiClient.post<string>('/parsingRecord/add', data)
  },

  modify(data: ParsingRecord) {
    return apiClient.post<string>('/parsingRecord/modify', data)
  },

  delete(id: number) {
    return apiClient.delete<string>(`/parsingRecord/delete/${id}`)
  },

  copy(data: ParsingRecord) {
    return apiClient.post<string>('/parsingRecord/copy', data)
  },

  getIpModeValue(getIpMode: number, recordType: number) {
    return apiClient.get<NetWorkSelectResponse[]>('/parsingRecord/getIpModeValue', {
      params: { getIpMode, recordType },
    })
  },
}
