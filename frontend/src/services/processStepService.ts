import httpClient, { ServiceResponse } from "./httpClient"
import ProcessStep from "@/domain/processStep"
import errorMessages from "@/i18n/errors.json"

interface ProcessStepService {
  getNextProcessStep(
    documentUnitId: string,
  ): Promise<ServiceResponse<ProcessStep>>
}

const service: ProcessStepService = {
  async getNextProcessStep(docUnitId: string) {
    const response = await httpClient.get<ProcessStep>(
      `caselaw/documentationUnits/${docUnitId}/processteps/next`,
    )
    if (response.status >= 300 || response.error) {
      response.data = undefined
      response.error = {
        title:
          errorMessages.DOCUMENTATION_UNIT_PROCESS_STEP_COULD_NOT_BE_LOADED
            .title,
      }
    }
    return response
  },
}

export default service
