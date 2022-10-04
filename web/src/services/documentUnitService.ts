import DocumentUnit from "../domain/documentUnit"
import httpClient, { ServiceResponse } from "./httpClient"

interface DocumentUnitService {
  getAll(): Promise<ServiceResponse<DocumentUnit[]>>
  getByDocumentNumber(
    documentNumber: string
  ): Promise<ServiceResponse<DocumentUnit>>
  createNew(
    docCenter: string,
    docType: string
  ): Promise<ServiceResponse<DocumentUnit>>
  update(documentUnit: DocumentUnit): Promise<ServiceResponse<unknown>>
  delete(documentUnitUuid: string): Promise<ServiceResponse<unknown>>
}

const service: DocumentUnitService = {
  async getAll() {
    const response = await httpClient.get<DocumentUnit[]>("documentunits")
    if (response.status >= 300) {
      response.error = {
        title: "Dokumentationseinheiten konnten nicht geladen werden.",
      }
    }
    return response
  },

  async getByDocumentNumber(documentNumber: string) {
    const response = await httpClient.get<DocumentUnit>(
      `documentunits/${documentNumber}`
    )
    if (response.status >= 300 || response.error) {
      response.error = {
        title: "Dokumentationseinheit konnten nicht geladen werden.",
      }
    } else {
      response.data = new DocumentUnit(response.data.uuid, { ...response.data })
    }
    return response
  },

  async createNew(docCenter: string, docType: string) {
    const response = await httpClient.post<Partial<DocumentUnit>, DocumentUnit>(
      "documentunits",
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      JSON.stringify({
        documentationCenterAbbreviation: docCenter,
        documentType: docType,
      }) as Partial<DocumentUnit>
    )
    if (response.status >= 300) {
      response.error = {
        title: "Dokumentationseinheit konnten nicht erstellt werden.",
      }
    }
    return response
  },

  async update(documentUnit: DocumentUnit) {
    const response = await httpClient.put(
      `documentunits/${documentUnit.uuid}/docx`,
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      JSON.stringify(documentUnit)
    )
    if (response.status >= 300) {
      response.error = {
        title: "Neue Dokumentationseinheit konnten nicht erstellt werden",
      }
    }
    return response
  },

  async delete(documentUnitUuid: string) {
    const response = await httpClient.delete(
      `documentunits/${documentUnitUuid}`
    )
    if (response.status >= 300) {
      response.error = {
        title: "Dokumentationseinheit konnte nicht gelöscht werden",
      }
    }
    return response
  },
}

export default service
