import httpClient, { ServiceResponse } from "./httpClient"

export const loginEndpoint = "/oauth2/authorization/oidcclient"

export async function isAuthenticated(): Promise<boolean> {
  const response = await httpClient.get("auth/me")
  if (response.status > 400) {
    return false
  }
  return true
}

export async function getName(): Promise<ServiceResponse<string>> {
  const response = await httpClient.get<string>("auth/me")
  if (response.status != 200) {
    response.error = {
      title: "Name konnte nicht geladen werden.",
    }
  }
  return response
}
