import httpClient from "./httpClient"

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

export default { isAuthenticated }
