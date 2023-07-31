import httpClient, { ServiceResponse } from "./httpClient"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import { Page, PageableService } from "@/shared/components/Pagination.vue"
import errorMessages from "@/shared/i18n/errors.json"

interface FieldOfLawService {
  getSelectedFieldsOfLaw(
    uuid: string,
  ): Promise<ServiceResponse<FieldOfLawNode[]>>
  addFieldOfLaw(
    uuid: string,
    identifier: string,
  ): Promise<ServiceResponse<FieldOfLawNode[]>>
  removeFieldOfLaw(
    uuid: string,
    identifier: string,
  ): Promise<ServiceResponse<FieldOfLawNode[]>>
  getChildrenOf(identifier: string): Promise<ServiceResponse<FieldOfLawNode[]>>
  getTreeForIdentifier(
    identifier: string,
  ): Promise<ServiceResponse<FieldOfLawNode>>
  searchForFieldsOfLaw: PageableService<FieldOfLawNode, string>
}

const service: FieldOfLawService = {
  async getSelectedFieldsOfLaw(uuid: string) {
    const response = await httpClient.get<FieldOfLawNode[]>(
      `caselaw/documentunits/${uuid}/contentrelatedindexing/fieldsoflaw`,
    )
    if (response.status >= 300) {
      response.error = {
        title:
          errorMessages.DOCUMENT_UNIT_FIELDS_OF_LAW_COULD_NOT_BE_LOADED.title.replace(
            "${uuid}",
            uuid,
          ),
      }
    }
    return response
  },
  async addFieldOfLaw(uuid: string, identifier: string) {
    const response = await httpClient.put<undefined, FieldOfLawNode[]>(
      `caselaw/documentunits/${uuid}/contentrelatedindexing/fieldsoflaw/${identifier}`,
    )
    if (response.status >= 300) {
      response.error = {
        title:
          errorMessages.DOCUMENT_UNIT_FIELDS_OF_LAW_COULD_NOT_BE_ADDED.title
            .replace("${identifier}", identifier)
            .replace("${uuid}", uuid),
      }
    }
    return response
  },
  async removeFieldOfLaw(uuid: string, identifier: string) {
    const response = await httpClient.delete<FieldOfLawNode[]>(
      `caselaw/documentunits/${uuid}/contentrelatedindexing/fieldsoflaw/${identifier}`,
    )
    if (response.status >= 300) {
      response.error = {
        title:
          errorMessages.DOCUMENT_UNIT_FIELDS_OF_LAW_COULD_NOT_BE_DELETED.title
            .replace("${identifier}", identifier)
            .replace("${uuid}", uuid),
      }
    }
    return response
  },
  async getChildrenOf(identifier: string) {
    const response = await httpClient.get<FieldOfLawNode[]>(
      `caselaw/fieldsoflaw/${identifier}/children`,
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.FIELDS_OF_LAW_COULD_NOT_BE_LOADED.title.replace(
          "${identifier}",
          identifier,
        ),
      }
    }
    return response
  },
  async getTreeForIdentifier(identifier: string) {
    const response = await httpClient.get<FieldOfLawNode>(
      `caselaw/fieldsoflaw/${identifier}/tree`,
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.FIELD_OF_LAW_COULD_NOT_BE_LOADED.title,
      }
    }
    return response
  },
  async searchForFieldsOfLaw(page: number, size: number, query?: string) {
    const response = await httpClient.get<Page<FieldOfLawNode>>(
      `caselaw/fieldsoflaw?pg=${page}&sz=${size}`,
      { params: { q: query ?? "" } },
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.FIELD_OF_LAW_SEARCH_FAILED.title,
      }
    }
    return response
  },
}

export default service
