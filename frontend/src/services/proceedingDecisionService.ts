import httpClient, { ServiceResponse } from "./httpClient"
import DocumentUnit from "@/domain/documentUnit"
import ProceedingDecision from "@/domain/proceedingDecision"

interface ProceedingDecisionService {
  createProceedingDecision(
    uuid: string,
    proceedingDecision: ProceedingDecision
  ): Promise<ServiceResponse<ProceedingDecision[]>>
  linkProceedingDecision(
    parentUuid: string,
    childUuid: string
  ): Promise<ServiceResponse<ProceedingDecision[]>>
  removeProceedingDecision(
    parentUuid: string,
    childUuid: string
  ): Promise<ServiceResponse<unknown>>
}

const service: ProceedingDecisionService = {
  async createProceedingDecision(
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
      return {
        status: 500,
        error: {
          title: `Vorgehende Entscheidung konnte nicht zu
          Dokumentationseinheit ${uuid} hinzugefügt werden`,
        },
      }
    } else {
      return {
        status: 200,
        data: (response.data as ProceedingDecision[]).map(
          (decision) => new ProceedingDecision({ ...decision })
        ),
      }
    }
  },

  async linkProceedingDecision(parentUuid: string, childUuid: string) {
    const response = await httpClient.put<undefined, DocumentUnit>(
      `caselaw/documentunits/${parentUuid}/proceedingdecisions/${childUuid}`
    )
    if (response.status >= 300) {
      return {
        status: 500,
        error: {
          title: `Vorgehende Entscheidung ${childUuid} konnte der Dokumentationseinheit ${parentUuid} nicht hinzugefügt werden.`,
        },
      }
    } else {
      return {
        status: 200,
        data: (
          (response.data as DocumentUnit)
            .proceedingDecisions as ProceedingDecision[]
        ).map((decision) => new ProceedingDecision({ ...decision })),
      }
    }
  },

  async removeProceedingDecision(parentUuid: string, childUuid: string) {
    const response = await httpClient.delete(
      `caselaw/documentunits/${parentUuid}/proceedingdecisions/${childUuid}`
    )
    if (response.status >= 300) {
      response.error = {
        title: `Vorgehende Entscheidung ${childUuid} für die Dokumentationseinheit ${parentUuid} konnten nicht entfernt werden.`,
      }
    }

    return response
  },
}

export default service
