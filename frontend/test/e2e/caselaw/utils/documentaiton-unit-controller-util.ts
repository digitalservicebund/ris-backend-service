import { APIRequestContext, APIResponse, expect, Page } from "@playwright/test"
import DocumentUnit from "@/domain/documentUnit"

export async function updateDocumentationUnit(
  page: Page,
  documentUnit: DocumentUnit,
  request: APIRequestContext,
): Promise<APIResponse> {
  const cookies = await page.context().cookies()
  const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
  return await request.put(
    `/api/v1/caselaw/documentunits/${documentUnit.uuid}`,
    {
      data: {
        ...documentUnit,
      },
      headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
    },
  )
}

export async function addIgnoreWordToDocumentationUnit(
  page: Page,
  uuid: string,
  word: string,
) {
  const cookies = await page.context().cookies()
  const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
  const response = await page.request.post(
    `/api/v1/caselaw/documentunits/${uuid}/text-check/ignored-words/add`,
    {
      headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      data: {
        word: word,
      },
    },
  )
  expect(response.ok()).toBeTruthy()
}
