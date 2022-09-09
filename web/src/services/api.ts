import axios from "axios"

type Response<T> = {
  status: number
  statusText: string
  data: T
}

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
  ): Promise<Response<TResponse>>
  post<TRequest, TResponse>(
    url: string,
    config?: RequestOptions,
    data?: TRequest
  ): Promise<Response<TResponse>>
  put<TRequest, TResponse>(
    url: string,
    config?: RequestOptions,
    data?: TRequest
  ): Promise<Response<TResponse>>
  delete<TResponse>(
    url: string,
    config?: RequestOptions
  ): Promise<Response<TResponse>>
}

const backendHost = import.meta.env.VITE_BACKEND_HOST ?? ""
const baseHttp = <T>(
  url: string,
  method: string,
  options?: RequestOptions,
  data?: T
) => {
  return axios({
    method: method,
    url: `${backendHost}/api/v1/${url}`,
    data,
    ...options,
  })
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
  delete(url: string, options: RequestOptions) {
    return baseHttp(url, "delete", { ...options })
  },
}

export default httpClient
