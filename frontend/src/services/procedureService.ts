import httpClient, { ServiceResponse } from "./httpClient"
import { Procedure } from "@/domain/documentUnit"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { Page } from "@/shared/components/Pagination.vue"
import errorMessages from "@/shared/i18n/errors.json"

interface ProcedureService {
  getAll(size: number, page: number): Promise<ServiceResponse<Page<Procedure>>>
  getDocumentUnits(
    procedureLabel: string,
  ): Promise<ServiceResponse<DocumentUnitListEntry[]>>
}

const service: ProcedureService = {
  async getAll(size: number, page: number) {
    const response = await httpClient.get<Page<Procedure>>(
      `caselaw/procedure`,
      {
        params: { sz: size.toString(), pg: page.toString() },
      },
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.PROCEDURE_GET_ALL.title,
      }
    }
    return response
  },
  async getDocumentUnits(procedureLabel: string) {
    const response = await httpClient.get<DocumentUnitListEntry[]>(
      `caselaw/procedure/${procedureLabel}/documentunits`,
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
