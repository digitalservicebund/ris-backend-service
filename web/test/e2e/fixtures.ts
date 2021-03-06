import { test } from "@playwright/test"
import { navigateToCategories } from "./e2e-utils"

// Declare the types of your fixtures.
type MyFixtures = {
  documentNumber: string
  editorField
}

export const testWithDocUnit = test.extend<MyFixtures>({
  documentNumber: async ({ request }, use) => {
    const response = await request.post("api/v1/docunits", {
      data: { documentationCenterAbbreviation: "foo", documentType: "X" },
    })
    const { uuid, documentnumber: documentNumber } = await response.json()

    await use(documentNumber)

    await request.delete(`api/v1/docunits/${uuid}`)
  },

  editorField: async ({ page, documentNumber }, use) => {
    await navigateToCategories(page, documentNumber)
    const editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )
    await editorField.click()
    await editorField.type("this is text")

    await use(editorField)
  },
})
