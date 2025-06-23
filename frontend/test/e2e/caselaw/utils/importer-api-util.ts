import * as fs from "fs" // Import the 'fs' module to read the file
import { APIRequestContext, APIResponse, Page } from "@playwright/test"

export async function importDocumentationUnitFromXml(
  page: Page,
  documentUnitXmlPath: string, // Changed to file path
  apiKey: string,
  username: string,
  password: string,
  request: APIRequestContext,
): Promise<APIResponse> {
  if (
    [process.env.IMPORTER_URL, username, password].some(
      (required) => required === undefined,
    )
  ) {
    throw new Error("Missing required value for import")
  }

  const cookies = await page.context().cookies()
  const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
  const usernamePassword = `${username}:${password}`
  const authHeader = `Basic ${Buffer.from(usernamePassword).toString("base64")}`

  // Create FormData
  const formData = new FormData()
  const fileContent = await fs.openAsBlob(documentUnitXmlPath)

  // Append the Buffer to FormData, specifying a filename
  formData.append("file", fileContent, "document.xml")

  return await request.post(
    process.env.IMPORTER_URL + `/api/v1/import`, // URL remains the same, no query parameter for file
    {
      headers: {
        "X-XSRF-TOKEN": csrfToken?.value ?? "",
        "X-API-KEY": apiKey,
        Authorization: authHeader,
      },
      multipart: formData,
    },
  )
}
