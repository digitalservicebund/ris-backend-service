import httpClient, { ServiceResponse } from "./httpClient"
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
