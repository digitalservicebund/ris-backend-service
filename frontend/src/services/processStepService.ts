import httpClient, { ServiceResponse } from "./httpClient"
import ProcessStep from "@/domain/processStep"
import errorMessages from "@/i18n/errors.json"

interface ProcessStepService {
  getProcessSteps(): Promise<ServiceResponse<ProcessStep[]>>

  getNextProcessStep(
    documentUnitId: string,
  ): Promise<ServiceResponse<ProcessStep | undefined>>
}

const service: ProcessStepService = {
  async getProcessSteps() {
    const response = await httpClient.get<ProcessStep[]>("caselaw/processsteps")
    if (response.status >= 300 || response.error) {
      response.data = undefined
      response.error =
        errorMessages.PROCESS_STEPS_OF_DOCUMENTATION_OFFICE_COULD_NOT_BE_LOADED
    }
    return response
  },

  async getNextProcessStep(docUnitId: string) {
    const response = await httpClient.get<ProcessStep>(
      `caselaw/documentationUnits/${docUnitId}/processsteps/next`,
    )
    if (response.status >= 300 || response.error) {
      response.data = undefined
      response.error =
        errorMessages.NEXT_PROCESS_STEP_FOR_DOCUMENATION_UNIT_COULD_NOT_BE_LOADED
    }
    return response
  },
}

export default service
