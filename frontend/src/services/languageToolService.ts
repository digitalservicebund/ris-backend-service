import httpClient, { ServiceResponse } from "@/services/httpClient"
import {
  LanguageToolResponse,
  TextCheckAllResponse,
} from "@/types/languagetool"

interface LanguageToolService {
  check(text: string): Promise<ServiceResponse<LanguageToolResponse>>
  checkAll(id: string): Promise<ServiceResponse<TextCheckAllResponse>>
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
    return await httpClient.get<TextCheckAllResponse>(
      `caselaw/documentunits/${id}/text-check/all`,
    )
  },
}

export default service
