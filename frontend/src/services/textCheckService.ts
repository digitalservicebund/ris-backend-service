import errorMessages from "@/i18n/errors.json"
import httpClient, {
  ResponseError,
  ServiceResponse,
} from "@/services/httpClient"
import {
  IgnoredTextCheckWord,
  TextCheckAllResponse,
  TextCheckCategoryResponse,
  TextCheckResponse,
} from "@/types/textCheck"

interface TextCheckService {
  check(text: string): Promise<ServiceResponse<TextCheckResponse>>

  checkAll(id: string): Promise<ServiceResponse<TextCheckAllResponse>>

  checkCategory(
    id: string,
    category?: string,
  ): Promise<ServiceResponse<TextCheckCategoryResponse>>

  addIgnoredWordForDocumentationOffice(
    id: string,
    ignoredTextCheckWord: IgnoredTextCheckWord,
  ): Promise<ServiceResponse<IgnoredTextCheckWord>>
}

const service: TextCheckService = {
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
    const response = await httpClient.get<TextCheckCategoryResponse>(
      `caselaw/documentunits/${id}/text-check`,
      category !== undefined ? { params: { category } } : {},
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.TEXT_CHECK_FAILED.title,
      }
    }
    return response
  },

  async addIgnoredWordForDocumentationOffice(
    id: string,
    ignoredTextCheckWord: IgnoredTextCheckWord,
  ) {
    const response = await httpClient.post<
      IgnoredTextCheckWord,
      IgnoredTextCheckWord
    >(
      `caselaw/documentunits/${id}/text-check/ignored-words/add`,
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      ignoredTextCheckWord,
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.IGNORED_TEXT_CHECK_WORD_COULD_NOT_BE_SAVED.title,
      } as ResponseError
    }
    return response
  },
}

export default service
