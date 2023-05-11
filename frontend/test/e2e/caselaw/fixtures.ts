import { Locator, test } from "@playwright/test"
import dayjs from "dayjs"
import timezone from "dayjs/plugin/timezone"
import DocumentUnit from "../../../src/domain/documentUnit"
import { generateString } from "../../test-helper/dataGenerators"
import { navigateToCategories } from "./e2e-utils"

dayjs.extend(timezone)

// Declare the types of your fixtures.
type MyFixtures = {
  documentNumber: string
  editorField: Locator
  secondaryDocumentUnit: DocumentUnit
}

export const testWithDocumentUnit = test.extend<MyFixtures>({
  documentNumber: async ({ request }, use) => {
    const response = await request.post(`/api/v1/caselaw/documentunits`, {
      data: { documentationCenterAbbreviation: "foo", documentType: "X" },
    })
    const { uuid, documentNumber } = await response.json()

    await use(documentNumber)

    await request.delete(`/api/v1/caselaw/documentunits/${uuid}`)
  },

  secondaryDocumentUnit: async ({ request }, use) => {
    const response = await request.post(`/api/v1/caselaw/documentunits`, {
      data: { documentationCenterAbbreviation: "foo", documentType: "X" },
    })
    const secondaryDocumentUnit = await response.json()
    const decisionDate = dayjs("2020-01-01")
      .tz("Europe/Berlin", true)
      .toISOString()
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
            decisionDate,
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
})
