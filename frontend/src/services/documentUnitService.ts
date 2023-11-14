import httpClient, {
  ServiceResponse,
  FailedValidationServerResponse,
} from "./httpClient"
import DocumentUnit from "@/domain/documentUnit"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import LinkedDocumentUnit from "@/domain/linkedDocumentUnit"
import { SingleNormValidationInfo } from "@/domain/normReference"
import { PageableService, Page } from "@/shared/components/Pagination.vue"
import errorMessages from "@/shared/i18n/errors.json"

interface DocumentUnitService {
  getByDocumentNumber(
    documentNumber: string,
  ): Promise<ServiceResponse<DocumentUnit>>
  createNew(): Promise<ServiceResponse<DocumentUnit>>
  update(documentUnit: DocumentUnit): Promise<ServiceResponse<unknown>>
  delete(documentUnitUuid: string): Promise<ServiceResponse<unknown>>
  searchByLinkedDocumentUnit: PageableService<
    LinkedDocumentUnit,
    LinkedDocumentUnit
  >
  searchByDocumentUnitSearchInput(
    requestParams?: { [key: string]: string } | undefined,
  ): Promise<ServiceResponse<Page<DocumentUnitListEntry>>>
  validateSingleNorm(
    singleNormValidationInfo: SingleNormValidationInfo,
  ): Promise<ServiceResponse<unknown>>
}

const service: DocumentUnitService = {
  async getByDocumentNumber(documentNumber: string) {
    const response = await httpClient.get<DocumentUnit>(
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
    } else {
      response.data = new DocumentUnit(response.data.uuid, { ...response.data })
    }
    return response
  },

  async createNew() {
    const response = await httpClient.get<DocumentUnit>(
      "caselaw/documentunits/new",
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.DOCUMENT_UNIT_CREATION_FAILED.title,
      }
    } else {
      response.data = new DocumentUnit((response.data as DocumentUnit).uuid, {
        ...(response.data as DocumentUnit),
      })
    }
    return response
  },

  async update(documentUnit: DocumentUnit) {
    const response = await httpClient.put<
      DocumentUnit,
      DocumentUnit | FailedValidationServerResponse
    >(
      `caselaw/documentunits/${documentUnit.uuid}`,
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      documentUnit,
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
    } else {
      response.data = new DocumentUnit((response.data as DocumentUnit).uuid, {
        ...(response.data as DocumentUnit),
      })
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

  async searchByLinkedDocumentUnit(
    page: number,
    size: number,
    query = new LinkedDocumentUnit(),
  ) {
    const response = await httpClient.put<
      LinkedDocumentUnit,
      Page<LinkedDocumentUnit>
    >(
      `caselaw/documentunits/search-by-linked-documentation-unit?pg=${page}&sz=${size}`,
      {
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
    response.data = response.data as Page<LinkedDocumentUnit>
    return {
      status: response.status,
      data: {
        ...response.data,
        content: response.data.content.map(
          (decision: Partial<LinkedDocumentUnit> | undefined) =>
            new LinkedDocumentUnit({ ...decision }),
        ),
      },
    }
  },

  async searchByDocumentUnitSearchInput(requestParams = {}) {
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
}

export default service
