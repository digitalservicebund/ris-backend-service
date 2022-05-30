// type Endpoint = "docunit"

const makeRequest = async (
  endpoint: string,
  options?: Partial<RequestInit>
) => {
  const defaultOptions: Partial<RequestInit> = {
    method: "GET",
  }
  return fetch(`${import.meta.env.VITE_API_BASE || ""}/api/v1/${endpoint}/`, {
    ...defaultOptions,
    ...options,
  })
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

export const fetchAllDocUnits = async () => {
  return makeRequest("docunit", {})
}

export const fetchDocUnitById = async (id: number) => {
  return makeRequest("docunit/" + id, {})
}

export const uploadDocUnit = async (file: File) => {
  const data = new FormData()
  data.append("fileToUpload", file)

  return makeRequest("docunit", {
    method: "POST",
    body: data,
  })
}
