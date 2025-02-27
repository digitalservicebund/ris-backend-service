import errorMessages from "@/i18n/errors.json"
import httpClient, { ServiceResponse } from "@/services/httpClient"
import {
  TextCheckAllResponse,
  TextCheckCategoryResponse,
  TextCheckResponse,
} from "@/types/languagetool"

interface LanguageToolService {
  check(text: string): Promise<ServiceResponse<TextCheckResponse>>

  checkAll(id: string): Promise<ServiceResponse<TextCheckAllResponse>>

  checkCategory(
    id: string,
    category?: string,
  ): Promise<ServiceResponse<TextCheckCategoryResponse>>
}

const service: LanguageToolService = {
  async check(text: string) {
    return await httpClient.post<string, TextCheckResponse>(
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
    const response = await httpClient.get<TextCheckAllResponse>(
      `caselaw/documentunits/${id}/text-check/all`,
    )

    if (response.status >= 300) {
      response.error = {
        title: errorMessages.TEXT_CHECK_FAILED.title,
      }
    }

    return response
  },
  async checkCategory(id: string, category?: string) {
    return await httpClient.get<TextCheckCategoryResponse>(
      `caselaw/documentunits/${id}/text-check`,
      category !== undefined ? { params: { category } } : {},
    )
  },
}

export default service
