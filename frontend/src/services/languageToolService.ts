import httpClient, { ServiceResponse } from "@/services/httpClient"
import { LanguageToolResponse } from "@/types/languagetool"

interface LanguageToolService {
  check(text: string): Promise<ServiceResponse<LanguageToolResponse>>
  checkAll(id: string): Promise<ServiceResponse<LanguageToolResponse>>
}

const service: LanguageToolService = {
  async check(text: string) {
    return await httpClient.post<string, LanguageToolResponse>(
      `caselaw/documentunits/text-check`,
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/x-www-form-urlencoded",
        },
      },
      text,
    )
  },
  async checkAll(id: string) {
    return await httpClient.get<LanguageToolResponse>(
      `caselaw/documentunits/${id}/text-check/all`,
    )
  },
}

export default service
