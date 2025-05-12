import httpClient, { ServiceResponse } from "./httpClient"
import { Page } from "@/components/Pagination.vue"
import EURLexResult from "@/domain/eurlex"
import errorMessages from "@/i18n/errors.json"

interface EURLexService {
  get(
    pageNumber?: number,
    fileNumber?: string,
    celex?: string,
    court?: string,
    startDate?: string,
    endDate?: string,
  ): Promise<ServiceResponse<Page<EURLexResult>>>
  transform(uri: string): Promise<ServiceResponse<string>>
}

const service: EURLexService = {
  async get(
    pageNumber?: number,
    fileNumber?: string,
    celex?: string,
    court?: string,
    startDate?: string,
    endDate?: string,
  ) {
    const queryParams: { [key: string]: string } = {}

    if (pageNumber) {
      queryParams.page = "" + pageNumber
    }
    if (fileNumber) {
      queryParams["file-number"] = fileNumber
    }
    if (celex) {
      queryParams.celex = celex
    }
    if (court) {
      queryParams.court = court
    }
    if (startDate) {
      queryParams["start-date"] = startDate
    }
    if (endDate) {
      queryParams["end-date"] = endDate
    }

    const response = await httpClient.get<Page<EURLexResult>>(
      "caselaw/eurlex",
      {
        params: queryParams,
      },
    )

    if (response.status >= 300) {
      response.error = {
        title: errorMessages.EURLEX_SEARCH_FAILED.title,
      }
    }
    return response
  },
  async transform(uri: string) {
    const response = await httpClient.post<string, string>(
      "caselaw/eurlex",
      undefined,
      uri,
    )

    if (response.status >= 300) {
      response.error = {
        title: errorMessages.EURLEX_TRANSFORMATION_FAILED.title,
      }
    }
    return response
  },
}

export default service
