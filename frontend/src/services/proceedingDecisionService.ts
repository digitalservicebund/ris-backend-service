import { ProceedingDecision } from "./../domain/documentUnit"
import httpClient, { ServiceResponse } from "./httpClient"

interface ProceedingDecisionService {
  getProceedingDecisions(
    uuid: string
  ): Promise<ServiceResponse<ProceedingDecision[]>>
  addProceedingDecision(
    uuid: string,
    proceedingDecision: ProceedingDecision
  ): Promise<ServiceResponse<ProceedingDecision[]>>
  searchForProceedingDecisions(
    proceedingDecision: ProceedingDecision
  ): Promise<ServiceResponse<ProceedingDecision[]>>
}

const service: ProceedingDecisionService = {
  async getProceedingDecisions(uuid: string) {
    const response = await httpClient.get<ProceedingDecision[]>(
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
    const response = await httpClient.put<
      ProceedingDecision,
      ProceedingDecision[]
    >(
      `caselaw/documentunits/${uuid}/proceedingdecisions`,
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      proceedingDecision
    )
    if (response.status >= 300) {
      response.error = {
        title: `Vorgehende Entscheidung konnte nicht zu
          Dokumentationseinheit ${uuid} hinzugefügt werden`,
      }
    }
    return response
  },
  async searchForProceedingDecisions(proceedingDecision: ProceedingDecision) {
    const response = await httpClient.put<
      ProceedingDecision,
      ProceedingDecision[]
    >(
      `caselaw/documentunits/search`,
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      proceedingDecision
    )
    if (response.status >= 300) {
      response.error = {
        title: `Die Suche nach passenden Dokumentationseinheit konnte nicht ausgeführt werden`,
      }
    }
    return response
  },
}

export default service
