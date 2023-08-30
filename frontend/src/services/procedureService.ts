import httpClient, { ServiceResponse } from "./httpClient"
import { Procedure } from "@/domain/documentUnit"
import { Page } from "@/shared/components/Pagination.vue"
import errorMessages from "@/shared/i18n/errors.json"

interface ProcedureService {
  getAllWithDocumentUnits(
    size: number,
    page: number,
  ): Promise<ServiceResponse<Page<Procedure>>>
}

const service: ProcedureService = {
  async getAllWithDocumentUnits(size: number, page: number) {
    const response = await httpClient.get<Page<Procedure>>(
      `caselaw/procedure/searchWithDocumentUnits`,
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
}

export default service
