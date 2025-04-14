import httpClient, { ServiceResponse } from "./httpClient"

import { DocumentationUnitHistoryLog } from "@/domain/documentationUnitHistoryLog"
import errorMessages from "@/i18n/errors.json"

interface DocumentationUnitHistoryLogService {
  get(
    documentUnitUuid: string,
  ): Promise<ServiceResponse<DocumentationUnitHistoryLog[]>>
}

const service: DocumentationUnitHistoryLogService = {
  async get(documentUnitUuid: string) {
    const response = await httpClient.get<DocumentationUnitHistoryLog[]>(
      `caselaw/documentunits/${documentUnitUuid}/historylogs`,
    )
    if (response.status >= 300) {
      response.error = {
        title:
          errorMessages.DOCUMENTATION_UNIT_HISTORY_LOG_COULD_NOT_BE_LOADED
            .title,
      }
    }

    return response
  },
}

export default service
