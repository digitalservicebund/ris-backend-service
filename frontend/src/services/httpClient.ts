import axios, { AxiosError } from "axios"
import { ValidationError } from "@/domain"

type RequestOptions = {
  headers?: {
    Accept?: string
    "Content-Type"?: string
    "X-Filename"?: string
  }
  params?: {
    [key: string]: string
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

  patch<TRequest, TResponse>(
    url: string,
    config?: RequestOptions,
    data?: TRequest
  ): Promise<ServiceResponse<TResponse>>

  delete<TResponse>(
    url: string,
    config?: RequestOptions
  ): Promise<ServiceResponse<TResponse>>
}

const backendHost = process.env.BACKEND_HOST

async function baseHttp<T>(
  url: string,
  method: string,
  options?: RequestOptions,
  data?: T
) {
  try {
    const response = await axiosInstance.request({
      method: method,
      url: `${backendHost}/api/v1/${url}`,
      validateStatus: () => true,
      data,
      ...options,
    })
    return {
      status: response.status,
      data: response.data.content ? response.data.content : response.data,
    }
  } catch (error) {
    return {
      status: Number((error as AxiosError).code) || 500,
      error: {
        title: (error as AxiosError).status?.toString() || "Network Error",
        description: String((error as AxiosError).cause),
      },
    }
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
  async patch<T>(url: string, options: RequestOptions, data: T) {
    return baseHttp(url, "patch", { ...options }, data)
  },
  async delete(url: string, options: RequestOptions) {
    return baseHttp(url, "delete", { ...options })
  },
}

export type FailedValidationServerResponse = {
  errors: ValidationError[]
}

export type ResponseError = {
  title: string
  description?: string
  validationErrors?: ValidationError[]
}

export type ServiceResponse<T> = {
  status: number
} & (
  | {
      data: T
      error?: never
    }
  | {
      data?: never
      error: ResponseError
    }
)

export const axiosInstance = axios.create()
export default httpClient
