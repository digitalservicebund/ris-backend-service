import httpClient, { ServiceResponse } from "./httpClient"
import { ApiKey } from "@/domain/apiKey"
import { User } from "@/domain/user"
import errorMessages from "@/i18n/errors.json"

export const loginEndpoint = "/oauth2/authorization/oidcclient"

export async function isAuthenticated(): Promise<boolean> {
  const response = await httpClient.get("auth/me")
  if (response.status > 400) {
    return false
  }
  return true
}

export async function getName(): Promise<ServiceResponse<User>> {
  const response = await httpClient.get<User>("auth/me")
  if (response.status != 200) {
    response.error = {
      title: errorMessages.NAME_COULD_NOT_BE_LOADED.title,
    }
  }
  return response
}

export async function getImportApiKey(): Promise<ServiceResponse<ApiKey>> {
  const response = await httpClient.get<ApiKey>("auth/api-key/import")
  return response
}

export async function generateImportApiKey(): Promise<ServiceResponse<ApiKey>> {
  const response = await httpClient.put<void, ApiKey>("auth/api-key/import")
  if (response.status != 200) {
    response.error = {
      title: errorMessages.API_KEY_COULD_NOT_GENERATED.title,
    }
  }
  return response
}

export async function invalidateImportApiKey(
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
}
