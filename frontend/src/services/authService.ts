import httpClient, { ServiceResponse } from "./httpClient"

function redirectToLogin() {
  location.href = "/oauth2/authorization/oidcclient"
}

async function isAuthenticated(): Promise<boolean> {
  const response = await httpClient.get("auth/me")
  if (response.status === 401 || response.status === 403) {
    redirectToLogin()
    return false
  }

  return true
}

async function getName(): Promise<ServiceResponse<string>> {
  const response = await httpClient.get<string>("auth/me")
  if (response.status >= 300) {
    response.error = {
      title: "Name konnte nicht geladen werden.",
    }
  }
  return response
}

export default { isAuthenticated, getName }
