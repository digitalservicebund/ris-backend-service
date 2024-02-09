import httpClient, { ServiceResponse } from "./httpClient"
import { Page } from "@/components/Pagination.vue"
import { Procedure } from "@/domain/documentUnit"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import errorMessages from "@/i18n/errors.json"

interface ProcedureService {
  get(
    size: number,
    page: number,
    filter?: string,
  ): Promise<ServiceResponse<Page<Procedure>>>
  getDocumentUnits(
    procedureId: string | undefined,
  ): Promise<ServiceResponse<DocumentUnitListEntry[]>>
}

const service: ProcedureService = {
  async get(size: number, page: number, filter?: string) {
    const response = await httpClient.get<Page<Procedure>>(
      `caselaw/procedure`,
      {
        params: {
          sz: size.toString(),
          pg: page.toString(),
          ...(filter ? { q: filter } : {}),
        },
      },
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.PROCEDURE_GET_ALL.title,
      }
    }
    return response
  },
  async getDocumentUnits(procedureId: string | undefined) {
    const response = await httpClient.get<DocumentUnitListEntry[]>(
      `caselaw/procedure/${procedureId}/documentunits`,
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.DOCUMENT_UNIT_COULD_NOT_BE_LOADED.title,
      }
    }
    return response
  },
}

export default service
