import { APIRequestContext, APIResponse, Page } from "@playwright/test"
import DocumentUnit from "@/domain/documentUnit"

async function updateDocumentationUnit(
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

export { updateDocumentationUnit }
