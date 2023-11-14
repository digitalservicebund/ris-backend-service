import httpClient, { ServiceResponse } from "./httpClient"
import errorMessages from "@/shared/i18n/errors.json"

interface KeywordService {
  getKeywords(uuid: string): Promise<ServiceResponse<string[]>>
  addKeyword(uuid: string, keyword: string): Promise<ServiceResponse<string[]>>
  deleteKeyword(
    uuid: string,
    keyword: string,
  ): Promise<ServiceResponse<string[]>>
}

const service: KeywordService = {
  async getKeywords(uuid: string) {
    const response = await httpClient.get<string[]>(
      `caselaw/documentunits/${uuid}/contentrelatedindexing/keywords`,
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.KEYWORDS_COULD_NOT_BE_LOADED.title,
      }
    }
    return response
  },
  async addKeyword(uuid: string, keyword: string) {
    const encodedkeyword = encodeURIComponent(keyword)
    const encodedString = `caselaw/documentunits/${uuid}/contentrelatedindexing/keywords/${encodedkeyword}`

    const response = await httpClient.put<string, string[]>(encodedString)
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.KEYWORD_COULD_NOT_BE_ADDED.title.replace(
          "${keyword}",
          keyword,
        ),
      }
    }
    return response
  },
  async deleteKeyword(uuid: string, keyword: string) {
    const encodedkeyword = encodeURIComponent(keyword)
    const encodedString = `caselaw/documentunits/${uuid}/contentrelatedindexing/keywords/${encodedkeyword}`

    const response = await httpClient.delete<string[]>(encodedString)
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.KEYWORD_COULD_NOT_BE_DELETED.title.replace(
          "${keyword}",
          keyword,
        ),
      }
    }
    return response
  },
}

export default service
