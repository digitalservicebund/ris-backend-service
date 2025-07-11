import httpClient, {
  FailedValidationServerResponse,
  ServiceResponse,
} from "./httpClient"
import { Page } from "@/components/Pagination.vue"
import { Decision } from "@/domain/decision"
import { DocumentationUnit } from "@/domain/documentationUnit"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { EurlexParameters } from "@/domain/eurlex"
import { DuplicateRelationStatus } from "@/domain/managementData"
import PendingProceeding from "@/domain/pendingProceeding"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import { RisJsonPatch } from "@/domain/risJsonPatch"
import { SingleNormValidationInfo } from "@/domain/singleNorm"
import errorMessages from "@/i18n/errors.json"
import { DocumentationUnitCreationParameters } from "@/types/documentationUnitCreationParameters"
import { DocumentationUnitSearchParameter } from "@/types/documentationUnitSearchParameter"
import { isDecision, isPendingProceeding } from "@/utils/typeGuards"

interface DocumentUnitService {
  getByDocumentNumber(
    documentNumber: string,
  ): Promise<ServiceResponse<DocumentationUnit>>

  createNew(
    params?: DocumentationUnitCreationParameters,
    requestParams?: { [key: string]: string } | undefined,
  ): Promise<ServiceResponse<DocumentationUnit>>

  createNewOutOfEurlexDecision(
    params?: EurlexParameters,
  ): Promise<ServiceResponse<string[]>>

  update(
    documentUnitUuid: string,
    patch: RisJsonPatch,
  ): Promise<ServiceResponse<RisJsonPatch | FailedValidationServerResponse>>

  delete(documentUnitUuid: string): Promise<ServiceResponse<unknown>>

  takeOver(documentNumber: string): Promise<ServiceResponse<unknown>>

  bulkAssignProcedure(
    procedureLabel: string,
    documentationUnitIds: string[],
  ): Promise<ServiceResponse<unknown>>

  searchByRelatedDocumentation(
    query: RelatedDocumentation,
    requestParams?: { [key: string]: string } | undefined,
  ): Promise<ServiceResponse<Page<RelatedDocumentation>>>

  searchByDocumentUnitSearchInput(
    requestParams?: { [key: string]: string } | undefined,
  ): Promise<ServiceResponse<Page<DocumentUnitListEntry>>>

  validateSingleNorm(
    singleNormValidationInfo: SingleNormValidationInfo,
  ): Promise<ServiceResponse<unknown>>

  setDuplicateRelationStatus(
    originalDocNumber: string,
    duplicateDocNumber: string,
    status: DuplicateRelationStatus,
  ): Promise<{ error: boolean }>

  assignDocumentationOffice(
    documentUnitId: string,
    documentationOfficeId: string,
  ): Promise<ServiceResponse<unknown>>
}

const service: DocumentUnitService = {
  async getByDocumentNumber(documentNumber: string) {
    const response = await httpClient.get<DocumentationUnit>(
      `caselaw/documentunits/${documentNumber}`,
    )
    if (response.status >= 300 || response.error) {
      response.data = undefined
      response.error = {
        title:
          response.status == 403
            ? errorMessages.DOCUMENT_UNIT_NOT_ALLOWED.title
            : errorMessages.DOCUMENT_UNIT_COULD_NOT_BE_LOADED.title,
      }
    } else if (isDecision(response.data)) {
      response.data = new Decision(response.data.uuid, {
        ...response.data,
      })
    } else if (isPendingProceeding(response.data)) {
      response.data = new PendingProceeding(response.data.uuid, {
        ...response.data,
      })
    }
    return response
  },

  async createNew(
    parameters?: DocumentationUnitCreationParameters,
    requestParams?: { [key: string]: string } | undefined,
  ) {
    const response = await httpClient.put<
      DocumentationUnitCreationParameters,
      DocumentationUnit
    >(
      "caselaw/documentunits/new",
      {
        params: requestParams,
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      parameters,
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.DOCUMENT_UNIT_CREATION_FAILED.title,
      }
    } else if (isDecision(response.data)) {
      response.data = new Decision(response.data.uuid, {
        ...response.data,
      })
    } else if (isPendingProceeding(response.data)) {
      response.data = new PendingProceeding(response.data.uuid, {
        ...response.data,
      })
    }
    return response
  },
  async createNewOutOfEurlexDecision(parameters?: EurlexParameters) {
    const response = await httpClient.put<EurlexParameters, string[]>(
      "caselaw/documentunits/new/eurlex",
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      parameters,
    )

    if (response.status >= 300) {
      response.error = {
        title: errorMessages.DOCUMENT_UNIT_CREATION_FAILED.title,
      }
    }

    return response
  },

  async update(documentUnitUuid: string, patch: RisJsonPatch) {
    const response = await httpClient.patch<
      RisJsonPatch,
      RisJsonPatch | FailedValidationServerResponse
    >(
      `caselaw/documentunits/${documentUnitUuid}`,
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      patch,
    )

    if (response.status >= 300) {
      response.error = {
        title:
          response.status == 403
            ? errorMessages.NOT_ALLOWED.title
            : errorMessages.DOCUMENT_UNIT_UPDATE_FAILED.title,
      }
      // good enough condition to detect validation errors (@Valid)?
      if (
        response.status == 400 &&
        JSON.stringify(response.data).includes("Validation failed")
      ) {
        response.error.validationErrors = (
          response.data as FailedValidationServerResponse
        ).errors
      } else {
        response.data = undefined
      }
    }
    return response
  },

  async delete(documentUnitUuid: string) {
    const response = await httpClient.delete(
      `caselaw/documentunits/${documentUnitUuid}`,
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.DOCUMENT_UNIT_DELETE_FAILED.title,
      }
    }
    return response
  },

  async takeOver(documentNumber: string) {
    const response = await httpClient.put<string, Decision>(
      `caselaw/documentunits/${documentNumber}/takeover`,
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.DOCUMENT_UNIT_TAKEOVER_FAILED.title,
        description: errorMessages.DOCUMENT_UNIT_TAKEOVER_FAILED.description,
      }
    }
    return response
  },

  async bulkAssignProcedure(
    procedureLabel: string,
    documentationUnitIds: string[],
  ) {
    const response = await httpClient.patch<
      { procedureLabel: string; documentationUnitIds: string[] },
      unknown
    >(
      `caselaw/documentunits/bulk-assign-procedure`,
      {},
      { procedureLabel, documentationUnitIds },
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.BULK_PROCEDURE_ASSIGNMENT_FAILED.title,
      }
    }
    return response
  },

  async searchByRelatedDocumentation(
    query: RelatedDocumentation = new RelatedDocumentation(),
    requestParams: { [K in DocumentationUnitSearchParameter]?: string } = {},
  ) {
    const response = await httpClient.put<
      RelatedDocumentation,
      Page<RelatedDocumentation>
    >(
      `caselaw/documentunits/search-linkable-documentation-units`,
      {
        params: requestParams,
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      query,
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.DOCUMENT_UNIT_SEARCH_FAILED.title,
        description: errorMessages.DOCUMENT_UNIT_SEARCH_FAILED.description,
      }
    }
    response.data = response.data as Page<RelatedDocumentation>
    return {
      status: response.status,
      data: {
        ...response.data,
        content: response.data.content.map(
          (decision: Partial<RelatedDocumentation> | undefined) =>
            new RelatedDocumentation({ ...decision }),
        ),
      },
    }
  },

  async searchByDocumentUnitSearchInput(
    requestParams: { [K in DocumentationUnitSearchParameter]?: string } = {},
  ) {
    const response = await httpClient.get<Page<DocumentUnitListEntry>>(
      `caselaw/documentunits/search`,
      {
        params: requestParams,
      },
    )

    if (response.status >= 300) {
      response.error = {
        title: errorMessages.DOCUMENT_UNIT_SEARCH_FAILED.title,
        description: errorMessages.DOCUMENT_UNIT_SEARCH_FAILED.description,
      }
    }
    response.data = response.data as Page<DocumentUnitListEntry>

    return response
  },

  async validateSingleNorm(singleNormValidationInfo: SingleNormValidationInfo) {
    const response = await httpClient.post(
      `caselaw/documentunits/validateSingleNorm`,
      {
        headers: {
          Accept: "text/plain",
          "Content-Type": "application/json",
        },
      },
      singleNormValidationInfo,
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.NORM_COULD_NOT_BE_VALIDATED.title,
      }
    }
    return response
  },

  async setDuplicateRelationStatus(
    originalDocNumber: string,
    duplicateDocNumber: string,
    status: DuplicateRelationStatus,
  ) {
    const response = await httpClient.put<
      { status: DuplicateRelationStatus },
      string
    >(
      `caselaw/documentunits/${originalDocNumber}/duplicate-status/${duplicateDocNumber}`,
      {},
      { status },
    )

    return response.status >= 400 ? { error: true } : { error: false }
  },

  async assignDocumentationOffice(
    documentUnitId: string,
    documentationOfficeId: string,
  ) {
    const response = await httpClient.put(
      `caselaw/documentunits/${documentUnitId}/assign/${documentationOfficeId}`,
    )

    if (response.status >= 300) {
      response.error = {
        title:
          errorMessages
            .DOCUMENTATION_UNIT_DOCUMENTATION_OFFICE_COULD_NOT_BE_ASSIGNED
            .title,
        description:
          errorMessages
            .DOCUMENTATION_UNIT_DOCUMENTATION_OFFICE_COULD_NOT_BE_ASSIGNED
            .description,
      }
    }
    return response
  },
}

export default service
