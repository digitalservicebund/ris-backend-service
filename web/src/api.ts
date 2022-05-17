type Endpoint = "version"

const makeRequest = async (
  endpoint: Endpoint,
  options?: Partial<RequestInit>
) => {
  const defaultOptions: Partial<RequestInit> = {
    method: "get",
  }
  return await fetch(
    `${import.meta.env.VITE_API_BASE || ""}/api/v1/${endpoint}`,
    {
      ...defaultOptions,
      ...options,
    }
  )
    .then((response) => response.json())
    .catch((error) => console.error(error))
}

export const getVersion = async () => {
  return await makeRequest("version")
}
