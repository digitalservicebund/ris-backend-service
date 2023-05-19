import httpClient, { ServiceResponse } from "./httpClient"
import { User } from "@/domain/user"

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
      title: "Name konnte nicht geladen werden.",
    }
  }
  return response
}
