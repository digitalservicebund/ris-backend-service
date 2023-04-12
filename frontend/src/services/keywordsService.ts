import httpClient, { ServiceResponse } from "./httpClient"

interface KeywordService {
  getKeywords(uuid: string): Promise<ServiceResponse<string[]>>
  addKeyword(uuid: string, keyword: string): Promise<ServiceResponse<string[]>>
  deleteKeyword(
    uuid: string,
    keyword: string
  ): Promise<ServiceResponse<string[]>>
}

const service: KeywordService = {
  async getKeywords(uuid: string) {
    const response = await httpClient.get<string[]>(
      `caselaw/documentunits/${uuid}/contentrelatedindexing/keywords`
    )
    if (response.status >= 300) {
      response.error = {
        title: `Schlagwörter konnten nicht geladen werden.`,
      }
    }
    return response
  },
  async addKeyword(uuid: string, keyword: string) {
    const response = await httpClient.put<string, string[]>(
      `caselaw/documentunits/${uuid}/contentrelatedindexing/keywords`,
      {
        headers: { "Content-Type": "text/plain" },
      },
      keyword
    )
    if (response.status >= 300) {
      response.error = {
        title: `Schlagwort ${keyword} konnte nicht hinzugefügt werden`,
      }
    }
    return response
  },
  async deleteKeyword(uuid: string, keyword: string) {
    const response = await httpClient.delete<string[]>(
      `caselaw/documentunits/${uuid}/contentrelatedindexing/keywords/${keyword}`
    )
    if (response.status >= 300) {
      response.error = {
        title: `Schlagwort ${keyword} konnte nicht entfernt werden`,
      }
    }
    return response
  },
}

export default service
