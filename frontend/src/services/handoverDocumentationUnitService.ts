import httpClient, { ServiceResponse } from "./httpClient"
import EventRecord, {
  DeltaMigration,
  EventRecordType,
  HandoverMail,
  HandoverReport,
  Preview,
} from "@/domain/eventRecord"
import errorMessages from "@/i18n/errors.json"

interface HandoverDocumentationUnitService {
  handoverDocument(
    documentUnitUuid: string,
  ): Promise<ServiceResponse<HandoverMail>>
  getEventLog(documentUnitUuid: string): Promise<ServiceResponse<EventRecord[]>>
  getPreview(documentUnitUuid: string): Promise<ServiceResponse<Preview>>
}

const service: HandoverDocumentationUnitService = {
  async handoverDocument(documentUnitUuid: string) {
    const response = await httpClient.put<string, HandoverMail>(
      `caselaw/documentunits/${documentUnitUuid}/handover`,
      {
        headers: { "Content-Type": "text/plain" },
      },
    )

    if (response.status >= 300 || !response.data?.success) {
      response.error = {
        title: errorMessages.DOCUMENT_UNIT_HANDOVER_FAILED.title,
        description:
          response.data?.statusMessages &&
          response.data.statusMessages.length > 0
            ? response.data?.statusMessages
            : errorMessages.DOCUMENT_UNIT_HANDOVER_FAILED.description,
      }
    }

    return response
  },

  async getEventLog(documentUnitUuid: string) {
    const response = await httpClient.get<EventRecord[]>(
      `caselaw/documentunits/${documentUnitUuid}/handover`,
    )

    response.error =
      response.status >= 300
        ? {
            title: errorMessages.DOCUMENT_UNIT_LOADING_HANDOVER_FAILED.title,
            description:
              errorMessages.DOCUMENT_UNIT_LOADING_HANDOVER_FAILED.description,
          }
        : undefined

    const eventLog: EventRecord[] = []
    if (response.data) {
      for (const event of response.data) {
        if (event.type === EventRecordType.HANDOVER) {
          eventLog.push(new HandoverMail(event))
        } else if (event.type === EventRecordType.HANDOVER_REPORT) {
          eventLog.push(new HandoverReport(event))
        } else if (event.type === EventRecordType.MIGRATION) {
          eventLog.push(new DeltaMigration(event))
        }
      }
    }
    response.data = eventLog

    return response
  },

  async getPreview(documentUnitUuid: string) {
    return await httpClient.get<HandoverMail>(
      `caselaw/documentunits/${documentUnitUuid}/preview-xml`,
    )
  },
}

export default service
