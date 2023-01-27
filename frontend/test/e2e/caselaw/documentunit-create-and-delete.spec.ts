import { expect, test } from "@playwright/test"
import { testWithDocumentUnit } from "./fixtures"

test.describe("create a doc unit and delete it again", () => {
  const backendHost = process.env.E2E_BASE_URL ?? "http://127.0.0.1"
  test("create new doc unit", async ({ page, request }) => {
    await page.goto("/")
    await page.locator("button >> text=Neue Dokumentationseinheit").click()
    await page.waitForSelector("text=oder Datei auswählen")
    await expect(page).toHaveURL(
      /\/caselaw\/documentunit\/[A-Z0-9]{13}\/files$/
    )

    // Given the earlier expectation we can assume that the regex will match...
    const documentNumber = /caselaw\/documentunit\/(.*)\/files/g.exec(
      page.url()
    )?.[1]

    const response = await request.get(
      `${backendHost}/api/v1/caselaw/documentunits`
    )
    const units = await response.json()
    await Promise.all(
      units
        .filter((unit) => unit.documentnumber === documentNumber)
        .map((unit) =>
          request.delete(
            `${backendHost}/api/v1/caselaw/documentunits/${unit.uuid}`
          )
        )
    )
  })

  test("delete doc unit", async ({ page, request }) => {
    const response = await request.post(
      `${backendHost}/api/v1/caselaw/documentunits`,
      {
        data: { documentationCenterAbbreviation: "foo", documentType: "X" },
      }
    )
    const { documentNumber } = await response.json()
    await page.goto("/")
    await expect(
      page.locator(`a[href*="/caselaw/documentunit/${documentNumber}/files"]`)
    ).toBeVisible()
    await page
      .locator(".table-row", {
        hasText: documentNumber,
      })
      .locator("[aria-label='Dokumentationseinheit löschen']")
      .click()
    await page.locator('button:has-text("Löschen")').click()
    await expect(
      page.locator(`a[href*="/caselaw/documentunit/${documentNumber}/files"]`)
    ).toBeHidden()
  })

  testWithDocumentUnit(
    "cancel delete doc unit",
    async ({ page, documentNumber }) => {
      await page.goto("/")
      await expect(
        page.locator(`a[href*="/caselaw/documentunit/${documentNumber}/files"]`)
      ).toBeVisible()
      await page
        .locator(".table-row", {
          hasText: documentNumber,
        })
        .locator("[aria-label='Dokumentationseinheit löschen']")
        .click()
      await page.locator('button:has-text("Abbrechen")').click()
      await expect(
        page.locator(`a[href*="/caselaw/documentunit/${documentNumber}/files"]`)
      ).toBeVisible()
    }
  )
})
