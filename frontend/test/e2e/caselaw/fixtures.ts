import { Locator, Page, test } from "@playwright/test"
import dayjs from "dayjs"
import utc from "dayjs/plugin/utc"
import DocumentUnit from "../../../src/domain/documentUnit"
import { generateString } from "../../test-helper/dataGenerators"
import { navigateToCategories } from "./e2e-utils"

dayjs.extend(utc)

// Declare the types of your fixtures.
type MyFixtures = {
  documentNumber: string
  editorField: Locator
  secondaryDocumentUnit: DocumentUnit
  pageWithBghUser: Page
}

export const testWithDocumentUnit = test.extend<MyFixtures>({
  documentNumber: async ({ request }, use) => {
    const response = await request.get(`/api/v1/caselaw/documentunits/new`)
    const { uuid, documentNumber } = await response.json()

    await use(documentNumber)

    await request.delete(`/api/v1/caselaw/documentunits/${uuid}`)
  },

  secondaryDocumentUnit: async ({ request }, use) => {
    const response = await request.get(`/api/v1/caselaw/documentunits/new`)
    const secondaryDocumentUnit = await response.json()
    const updateResponse = await request.put(
      `/api/v1/caselaw/documentunits/${secondaryDocumentUnit.uuid}`,
      {
        data: {
          ...secondaryDocumentUnit,
          coreData: {
            ...secondaryDocumentUnit.coreData,
            court: {
              type: "AG",
              location: "Aachen",
            },
            fileNumbers: [generateString()],
            documentType: { jurisShortcut: "AnU", label: "Anerkenntnisurteil" },
            decisionDate: "2019-12-31T23:00:00Z",
          },
        },
      }
    )

    await use(await updateResponse.json())

    await request.delete(
      `/api/v1/caselaw/documentunits/${secondaryDocumentUnit.uuid}`
    )
  },

  editorField: async ({ page, documentNumber }, use) => {
    await navigateToCategories(page, documentNumber)
    const editorField = page.locator("[data-testid='Entscheidungsname'] >> div")
    await editorField.click()
    await editorField.type("this is text")

    await use(editorField)
  },

  pageWithBghUser: async ({ browser }, use) => {
    const bghContext = await browser.newContext({
      storageState: `test/e2e/shared/.auth/user_bgh.json`,
    })
    const pageWithBghUser = await bghContext.newPage()

    await use(pageWithBghUser)

    await bghContext.close()
  },
})
