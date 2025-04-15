import { APIRequestContext, APIResponse, expect, Page } from "@playwright/test"
import { Browser } from "playwright"
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

export async function deleteDocumentUnit(page: Page, documentNumber: string) {
  const cookies = await page.context().cookies()
  const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
  const getResponse = await page.request.get(
    `/api/v1/caselaw/documentunits/${documentNumber}`,
    { headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" } },
  )
  expect(getResponse.ok()).toBeTruthy()

  const { uuid } = await getResponse.json()

  const deleteResponse = await page.request.delete(
    `/api/v1/caselaw/documentunits/${uuid}`,
    { headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" } },
  )
  expect(deleteResponse.ok()).toBeTruthy()
}

export async function deleteProcedure(page: Page, uuid: string) {
  const cookies = await page.context().cookies()
  const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
  const response = await page.request.delete(
    `/api/v1/caselaw/procedure/${uuid}`,
    { headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" } },
  )
  expect(response.ok()).toBeTruthy()
}

export async function deleteAllProcedures(
  browser: Browser,
  procedurePrefix: string,
) {
  const page = await browser.newPage()
  const response = await page.request.get(
    `/api/v1/caselaw/procedure?sz=50&pg=0&q=${procedurePrefix}&withDocUnits=false`,
  )
  const responseBody = await response.json()
  for (const procedure of responseBody.content) {
    const uuid = procedure.id
    await deleteProcedure(page, uuid)
  }
}
