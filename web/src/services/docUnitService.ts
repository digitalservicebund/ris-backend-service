import DocumentUnit from "../domain/documentUnit"
import httpClient, { ServiceResponse } from "./httpClient"

interface DocUnitService {
  getAll(): Promise<ServiceResponse<DocumentUnit[]>>
  getByDocumentNumber(
    documentNumber: string
  ): Promise<ServiceResponse<DocumentUnit>>
  createNew(
    docCenter: string,
    docType: string
  ): Promise<ServiceResponse<DocumentUnit>>
  update(docUnit: DocumentUnit): Promise<ServiceResponse<unknown>>
  delete(docUnitUuid: string): Promise<ServiceResponse<unknown>>
}

const service: DocUnitService = {
  async getAll() {
    const response = await httpClient.get<DocumentUnit[]>("docunits")
    if (response.status >= 300) {
      response.error = {
        title: "Dokumentationseinheiten konnten nicht geladen werden.",
      }
    }
    return response
  },

  async getByDocumentNumber(documentNumber: string) {
    const response = await httpClient.get<DocumentUnit>(
      `docunits/${documentNumber}`
    )
    if (response.status >= 300) {
      response.error = {
        title: "Dokumentationseinheit konnten nicht geladen werden.",
      }
    }
    response.data = new DocumentUnit(response.data.uuid, { ...response.data })
    return response
  },

  async createNew(docCenter: string, docType: string) {
    const response = await httpClient.post<Partial<DocumentUnit>, DocumentUnit>(
      "docunits",
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

  async update(docUnit: DocumentUnit) {
    const response = await httpClient.put(
      `docunits/${docUnit.uuid}/docx`,
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      JSON.stringify(docUnit)
    )
    if (response.status >= 300) {
      response.error = {
        title: "Neue Dokumentationseinheit konnten nicht erstellt werden",
      }
    }
    return response
  },

  async delete(docUnitUuid: string) {
    const response = await httpClient.delete(`docunits/${docUnitUuid}`)
    if (response.status >= 300) {
      response.error = {
        title: "Dokumentationseinheit konnte nicht gelöscht werden",
      }
    }
    return response
  },
}

export default service
