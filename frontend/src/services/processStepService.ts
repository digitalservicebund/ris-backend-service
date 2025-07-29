import httpClient, { ServiceResponse } from "./httpClient"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
import ProcessStep from "@/domain/processStep"
import errorMessages from "@/i18n/errors.json"

interface ProcessStepService {
  getProcessSteps(
    documentUnitId: string,
  ): Promise<ServiceResponse<DocumentationUnitProcessStep[]>>

  getNextProcessStep(
    documentUnitId: string,
  ): Promise<ServiceResponse<ProcessStep>>

  moveToNextProcessStep(
    documentUnitId: string,
    processStep: ProcessStep,
  ): Promise<ServiceResponse<DocumentationUnitProcessStep>>
}

const service: ProcessStepService = {
  async getProcessSteps(docUnitId: string) {
    const response = await httpClient.get<DocumentationUnitProcessStep[]>(
      `caselaw/processsteps/${docUnitId}/history`,
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

  async getNextProcessStep(docUnitId: string) {
    const response = await httpClient.get<ProcessStep>(
      `caselaw/processsteps/${docUnitId}/next`,
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

  async moveToNextProcessStep(docUnitId: string, processStep: ProcessStep) {
    const response = await httpClient.post<
      string,
      DocumentationUnitProcessStep
    >(
      `caselaw/processsteps/${docUnitId}/new`,
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      processStep.uuid,
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
