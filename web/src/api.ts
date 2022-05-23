type Endpoint = "version" | "docunit/upload" | "docunit/getAll"

const makeRequest = async (
  endpoint: Endpoint,
  options?: Partial<RequestInit>
) => {
  const defaultOptions: Partial<RequestInit> = {
    method: "GET",
  }
  return await fetch(
    `${import.meta.env.VITE_API_BASE || ""}/api/v1/${endpoint}`,
    {
      ...defaultOptions,
      ...options,
    }
  )
    .then((response) => {
      if (response.body instanceof ReadableStream) {
        return getReadableStreamResponse(response.body).then((response) =>
          response.json()
        )
      }
      return response.json()
    })
    .catch((error) => console.error(error))
}

const getReadableStreamResponse = async (responseBody: ReadableStream) => {
  const reader = responseBody.getReader()
  return new Response(
    new ReadableStream({
      start(controller) {
        return pump()
        function pump(): Promise<void> {
          return reader.read().then(({ done, value }) => {
            if (done) {
              controller.close()
              return
            }
            controller.enqueue(value)
            return pump()
          })
        }
      },
    })
  )
}

export const getVersion = async () => {
  return await makeRequest("version")
}

export const getAllDocUnits = async () => {
  return await makeRequest("docunit/getAll", {})
}

export const uploadDocUnit = async (file: File) => {
  const data = new FormData()
  data.append("fileToUpload", file)

  return await makeRequest("docunit/upload", {
    method: "POST",
    body: data,
  })
}
