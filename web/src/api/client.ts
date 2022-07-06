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

export const apiClient = async (
  endpoint: string,
  options?: Partial<RequestInit>
) => {
  const baseUrl = `${import.meta.env.VITE_API_BASE || ""}/api/v1/`
  const defaultOptions: Partial<RequestInit> = {
    method: "GET",
  }
  const response = await fetch(baseUrl + endpoint, {
    ...defaultOptions,
    ...options,
  })
  try {
    if (response.body instanceof ReadableStream) {
      return await getReadableStreamResponse(response.body).then((response) =>
        response.json()
      )
    }
    return response.json()
  } catch (error) {
    console.error(error)
  }
}
