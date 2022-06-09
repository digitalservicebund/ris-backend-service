import { DocUnit } from "./types/DocUnit"

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
  return makeRequest("docunits")
}

export const fetchDocUnitById = async (id: number) => {
  return makeRequest(`docunits/${id}`)
}

export const updateDocUnit = async (docUnit: DocUnit | null) => {
  if (!docUnit) return
  return makeRequest(`docunits/${docUnit.id}/docx`, {
    method: "PUT",
    body: JSON.stringify(docUnit),
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
  })
}

export const createNewDocUnit = async () => {
  return makeRequest("docunits", {
    method: "POST",
  })
}

export const uploadFile = async (docUnitId: number | undefined, file: File) => {
  if (!docUnitId) return // not cool, do this properly TODO
  return makeRequest(`docunits/${docUnitId}/file`, {
    method: "PUT",
    body: file,
    headers: {
      "Content-Type":
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      filename: file.name,
    },
  })
}

export const deleteFile = async (docUnitId: number | undefined) => {
  if (!docUnitId) return
  return makeRequest(`docunits/${docUnitId}/file`, {
    method: "DELETE",
  })
}

export const getAllDocxFiles = async () => {
  return makeRequest("docunitdocx")
}

export const getDocxFileAsHtml = async (fileName: string) => {
  console.log(fileName)
  return makeRequest(`docunitdocx/${fileName}`)
}
