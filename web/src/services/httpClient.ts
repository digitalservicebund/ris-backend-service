import axios from "axios"

type RequestOptions = {
  headers?: {
    Accept?: string
    "Content-Type"?: string
    "X-Filename"?: string
  }
}

interface HttpClient {
  get<TResponse>(
    url: string,
    config?: RequestOptions
  ): Promise<ServiceResponse<TResponse>>

  post<TRequest, TResponse>(
    url: string,
    config?: RequestOptions,
    data?: TRequest
  ): Promise<ServiceResponse<TResponse>>

  put<TRequest, TResponse>(
    url: string,
    config?: RequestOptions,
    data?: TRequest
  ): Promise<ServiceResponse<TResponse>>

  delete<TResponse>(
    url: string,
    config?: RequestOptions
  ): Promise<ServiceResponse<TResponse>>
}

const backendHost = import.meta.env.VITE_BACKEND_HOST ?? ""
const baseHttp = async <T>(
  url: string,
  method: string,
  options?: RequestOptions,
  data?: T
) => {
  const response = await axios({
    method: method,
    url: `${backendHost}/api/v1/${url}`,
    validateStatus: () => true,
    data,
    ...options,
  })
  return {
    status: response.status,
    statusText: response.statusText,
    data: response?.data,
  }
}

const httpClient: HttpClient = {
  async get(url: string, options?: RequestOptions) {
    return baseHttp(url, "get", { ...options })
  },
  async post<T>(url: string, options: RequestOptions, data: T) {
    return baseHttp<T>(url, "post", { ...options }, data)
  },
  async put<T>(url: string, options: RequestOptions, data: T) {
    return baseHttp(url, "put", { ...options }, data)
  },
  async delete(url: string, options: RequestOptions) {
    return baseHttp(url, "delete", { ...options })
  },
}

type ErrorResponse = {
  title: string
  description?: string
}

export type ServiceResponse<T> = {
  status: number
  // statusText: string
  data: T
  error?: ErrorResponse
}

export default httpClient
