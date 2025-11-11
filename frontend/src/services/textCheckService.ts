import errorMessages from "@/i18n/errors.json"
import httpClient, {
  ResponseError,
  ServiceResponse,
} from "@/services/httpClient"
import {
  IgnoredTextCheckWord,
  IgnoredTextCheckWordRequest,
  TextCheckAllResponse,
  TextCheckCategoryResponse,
  TextCheckResponse,
} from "@/types/textCheck"

interface TextCheckApiService {
  check(text: string): Promise<ServiceResponse<TextCheckResponse>>

  checkAll(id: string): Promise<ServiceResponse<TextCheckAllResponse>>

  checkCategory(
    id: string,
    category?: string,
  ): Promise<ServiceResponse<TextCheckCategoryResponse>>

  addLocalIgnore(
    id: string,
    word: string,
  ): Promise<ServiceResponse<IgnoredTextCheckWord>>

  removeLocalIgnore(id: string, word: string): Promise<ServiceResponse<void>>

  addGlobalIgnore(word: string): Promise<ServiceResponse<IgnoredTextCheckWord>>

  removeGlobalIgnore(word: string): Promise<ServiceResponse<void>>
}

const service: TextCheckApiService = {
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
  async addLocalIgnore(id: string, word: string) {
    const response = await httpClient.post<
      IgnoredTextCheckWordRequest,
      IgnoredTextCheckWord
    >(
      `caselaw/documentunits/${id}/text-check/ignored-word`,
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      { word },
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.IGNORED_TEXT_CHECK_WORD_COULD_NOT_BE_SAVED.title,
      } as ResponseError
    }
    return response
  },

  async removeLocalIgnore(id: string, word: string) {
    const response = await httpClient.delete<void>(
      `caselaw/documentunits/${id}/text-check/ignored-word/${word}`,
      {
        headers: {
          "Content-Type": "application/json",
        },
      },
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.IGNORED_TEXT_CHECK_WORD_COULD_NOT_BE_DELETED.title,
      } as ResponseError
    }
    return response
  },

  async addGlobalIgnore(word: string) {
    const response = await httpClient.post<
      IgnoredTextCheckWordRequest,
      IgnoredTextCheckWord
    >(
      `caselaw/text-check/ignored-word`,
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      { word },
    )
    if (response.status >= 300) {
      response.error = {
        title:
          errorMessages.IGNORED_TEXT_CHECK_WORD_GLOBAL_COULD_NOT_BE_SAVED.title,
      } as ResponseError
    }
    return response
  },

  async removeGlobalIgnore(word: string) {
    const response = await httpClient.delete<void>(
      `caselaw/text-check/ignored-word/${word}`,
      {
        headers: {
          "Content-Type": "application/json",
        },
      },
    )
    if (response.status >= 300) {
      response.error = {
        title:
          errorMessages.IGNORED_TEXT_CHECK_WORD_GLOBAL_COULD_NOT_BE_DELETED
            .title,
      } as ResponseError
    }
    return response
  },
}

export default service
