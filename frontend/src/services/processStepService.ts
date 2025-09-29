import httpClient, { ServiceResponse } from "./httpClient"
import ProcessStep from "@/domain/processStep"
import errorMessages from "@/i18n/errors.json"

interface ProcessStepService {
  /**
   * Retrieves process steps for the user's documentation office.
   *
   * @param assignableOnly If true, only assignable steps (excluding "Neu") are fetched.
   * Defaults to false if not provided.
   */
  getProcessSteps(
    assignableOnly?: boolean,
  ): Promise<ServiceResponse<ProcessStep[]>>

  getNextProcessStep(
    documentUnitId: string,
  ): Promise<ServiceResponse<ProcessStep | undefined>>
}

const service: ProcessStepService = {
  /**
   * Retrieves process steps for the user's documentation office.
   *
   * @param assignableOnly If true, only assignable steps (excluding "Neu") are fetched.
   * Defaults to false if not provided.
   */
  async getProcessSteps(assignableOnly: boolean = false) {
    let url = "caselaw/processsteps"
    if (assignableOnly) {
      url += "?assignableOnly=true"
    }

    const response = await httpClient.get<ProcessStep[]>(url)
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
