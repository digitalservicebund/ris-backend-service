import { ProceedingDecision } from "./../domain/documentUnit"
import httpClient, { ServiceResponse } from "./httpClient"

interface ProceedingDecisionService {
  getProceedingDecisions(uuid: string): Promise<ServiceResponse<{}>>
  addProceedingDecision(
    uuid: string,
    proceedingDecision: ProceedingDecision
  ): Promise<ServiceResponse<{}>>
}

const service: ProceedingDecisionService = {
  async getProceedingDecisions(uuid: string) {
    const response = await httpClient.get<{}>(
      `caselaw/documentunits/${uuid}/proceedingdecisions`
    )
    if (response.status >= 300) {
      response.error = {
        title: `Vorgehende Entscheidungen für die Dokumentationseinheit ${uuid} konnten nicht geladen werden.`,
      }
    }
    return response
  },
  async addProceedingDecision(
    uuid: string,
    proceedingDecision: ProceedingDecision
  ) {
    const response = await httpClient.put<{}, {}>(
      `caselaw/documentunits/${uuid}/proceedingdecisions`,
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      JSON.stringify(proceedingDecision)
    )
    if (response.status >= 300) {
      response.error = {
        title: `Vorgehende Entscheidung konnte nicht zu 
          Dokumentationseinheit ${uuid} hinzugefügt werden`,
      }
    }
    return response
  },
}

export default service
