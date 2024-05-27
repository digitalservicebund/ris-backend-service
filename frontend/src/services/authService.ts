import httpClient, { ServiceResponse } from "./httpClient"
import { ApiKey } from "@/domain/apiKey"
import { User } from "@/domain/user"
import errorMessages from "@/i18n/errors.json"

interface AuthService {
  isAuthenticated(): Promise<boolean>

  getName(): Promise<ServiceResponse<User>>

  getImportApiKey(): Promise<ServiceResponse<ApiKey>>

  generateImportApiKey(): Promise<ServiceResponse<ApiKey>>

  invalidateImportApiKey(apiKey?: string): Promise<ServiceResponse<ApiKey>>

  getLoginEndpoint(): string
}

const service: AuthService = {
  async isAuthenticated(): Promise<boolean> {
    const response = await httpClient.get("auth/me")
    return response.status > 400 ? false : true
  },

  async getName(): Promise<ServiceResponse<User>> {
    const response = await httpClient.get<User>("auth/me")
    if (response.status != 200) {
      response.error = {
        title: errorMessages.NAME_COULD_NOT_BE_LOADED.title,
      }
    }
    return response
  },

  async getImportApiKey(): Promise<ServiceResponse<ApiKey>> {
    const response = await httpClient.get<ApiKey>("auth/api-key/import")
    return response
  },

  async generateImportApiKey(): Promise<ServiceResponse<ApiKey>> {
    const response = await httpClient.put<void, ApiKey>("auth/api-key/import")
    if (response.status != 200) {
      response.error = {
        title: errorMessages.API_KEY_COULD_NOT_GENERATED.title,
      }
    }
    return response
  },

  async invalidateImportApiKey(
    apiKey?: string,
  ): Promise<ServiceResponse<ApiKey>> {
    const response = await httpClient.post<void, ApiKey>(
      "auth/api-key/import/invalidate",
      { headers: { "X-API-KEY": apiKey } },
    )
    if (response.status != 200) {
      response.error = {
        title: errorMessages.API_KEY_COULD_NOT_INVALIDATED.title,
      }
    }

    return response
  },

  getLoginEndpoint(): string {
    return "/oauth2/authorization/oidcclient"
  },
}

export default service
