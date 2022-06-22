import { DocUnit } from "../types/DocUnit"
import { apiClient } from "./client"

export const fetchAllDocUnits = async () => {
  return apiClient("docunits")
}

export const fetchDocUnitById = async (id: string) => {
  return apiClient(`docunits/${id}`)
}

export const updateDocUnit = async (docUnit: DocUnit | null) => {
  if (!docUnit) return
  return apiClient(`docunits/${docUnit.id}/docx`, {
    method: "PUT",
    body: JSON.stringify(docUnit),
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
  })
}

export const createNewDocUnit = async (
  documentationCenterAbbreviation: string,
  documentType: string
) => {
  return apiClient("docunits", {
    method: "POST",
    body: JSON.stringify({
      documentationCenterAbbreviation: documentationCenterAbbreviation,
      documentType: documentType,
    }),
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
  })
}

// this throws "SyntaxError: JSON.parse: unexpected end of data at line 1 column 1 of the JSON data" TODO
// in apiClient(), maybe has to do with just one String in the ReadableStream? does the right thing though
export const deleteDocUnit = async (docUnitId: string | undefined) => {
  if (!docUnitId) return
  return apiClient(`docunits/${docUnitId}`, {
    method: "DELETE",
  })
}

export const uploadFile = async (docUnitId: string | undefined, file: File) => {
  if (!docUnitId) return // not cool, do this properly TODO
  return apiClient(`docunits/${docUnitId}/file`, {
    method: "PUT",
    body: file,
    headers: {
      "Content-Type":
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      "X-Filename": file.name,
    },
  })
}

export const deleteFile = async (docUnitId: string | undefined) => {
  if (!docUnitId) return
  return apiClient(`docunits/${docUnitId}/file`, {
    method: "DELETE",
  })
}

export const getAllDocxFiles = async () => {
  return apiClient("docunitdocx")
}

export const getDocxFileAsHtml = async (fileName: string) => {
  return apiClient(`docunitdocx/${fileName}`)
}
