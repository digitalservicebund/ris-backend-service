import httpClient, { ServiceResponse } from "./httpClient"
import { DocumentUnit } from "@/domain/documentUnit"
import ProceedingDecision from "@/domain/previousDecision"
import errorMessages from "@/i18n/errors.json"

interface ProceedingDecisionService {
  createProceedingDecision(
    uuid: string,
    proceedingDecision: ProceedingDecision,
  ): Promise<ServiceResponse<ProceedingDecision[]>>
  linkProceedingDecision(
    parentUuid: string,
    childUuid: string,
  ): Promise<ServiceResponse<ProceedingDecision[]>>
  removeProceedingDecision(
    parentUuid: string,
    childUuid: string,
  ): Promise<ServiceResponse<unknown>>
}

const service: ProceedingDecisionService = {
  async createProceedingDecision(
    uuid: string,
    proceedingDecision: ProceedingDecision,
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
      proceedingDecision,
    )
    if (response.status >= 300) {
      return {
        status: 500,
        error: {
          title:
            errorMessages.PROCEEDING_DECISION_COULD_NOT_BE_ADDED.title.replace(
              "${uuid}",
              uuid,
            ),
        },
      }
    } else {
      return {
        status: 200,
        data: (response.data as ProceedingDecision[]).map(
          (decision) => new ProceedingDecision({ ...decision }),
        ),
      }
    }
  },

  async linkProceedingDecision(parentUuid: string, childUuid: string) {
    const response = await httpClient.put<undefined, DocumentUnit>(
      `caselaw/documentunits/${parentUuid}/proceedingdecisions/${childUuid}`,
    )
    if (response.status >= 300) {
      return {
        status: 500,
        error: {
          title:
            errorMessages.DOCUMENT_UNIT_PROCEEDING_DECISION_COULD_NOT_BE_ADDED.title
              .replace("${childUuid}", childUuid)
              .replace("${parentUuid}", parentUuid),
        },
      }
    } else {
      return {
        status: 200,
        data: (
          (response.data as DocumentUnit)
            .previousDecisions as ProceedingDecision[]
        ).map((decision) => new ProceedingDecision({ ...decision })),
      }
    }
  },

  async removeProceedingDecision(parentUuid: string, childUuid: string) {
    const response = await httpClient.delete(
      `caselaw/documentunits/${parentUuid}/proceedingdecisions/${childUuid}`,
    )
    if (response.status >= 300) {
      response.error = {
        title:
          errorMessages.DOCUMENT_UNIT_PROCEEDING_DECISION_COULD_NOT_BE_DELETED.title
            .replace("${childUuid}", childUuid)
            .replace("${parentUuid}", parentUuid),
      }
    }

    return response
  },
}

export default service
