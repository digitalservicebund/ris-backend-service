import httpClient, { ServiceResponse } from "@/services/httpClient"
import { LanguageToolResponse } from "@/types/languagetool"

interface LanguageToolService {
  check(text: string): Promise<ServiceResponse<LanguageToolResponse>>
}

const service: LanguageToolService = {
  async check(text: string) {
    return await httpClient.post<string, LanguageToolResponse>(
      `caselaw/languagetool/check`,
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/x-www-form-urlencoded",
        },
      },
      text,
    )
  },
}

export default service
