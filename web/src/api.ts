type Endpoint = "version" | "docunit/upload"

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

export const uploadDocUnit = async (file: File) => {
  const data = new FormData()
  data.append("fileToUpload", file)

  return await makeRequest("docunit/upload", {
    method: "post",
    body: data,
  })
}
