import { Locator, test } from "@playwright/test"
import { navigateToCategories } from "./e2e-utils"

// Declare the types of your fixtures.
type MyFixtures = {
  documentNumber: string
  editorField: Locator
}

export const testWithDocumentUnit = test.extend<MyFixtures>({
  documentNumber: async ({ request }, use) => {
    const backendHost = process.env.E2E_BASE_URL ?? "http://127.0.0.1"
    const response = await request.post(
      `${backendHost}/api/v1/caselaw/documentunits`,
      {
        data: { documentationCenterAbbreviation: "foo", documentType: "X" },
      }
    )
    const { uuid, documentNumber } = await response.json()

    await use(documentNumber)

    await request.delete(`${backendHost}/api/v1/caselaw/documentunits/${uuid}`)
  },

  editorField: async ({ page, documentNumber }, use) => {
    await navigateToCategories(page, documentNumber)
    const editorField = page.locator("[data-testid='Entscheidungsname'] >> div")
    await editorField.click()
    await editorField.type("this is text")

    await use(editorField)
  },
})
