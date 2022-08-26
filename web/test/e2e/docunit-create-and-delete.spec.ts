import { expect, test } from "@playwright/test"
import { testWithDocUnit } from "./fixtures"

test.describe("create a doc unit and delete it again", () => {
  const backendHost = process.env.E2E_BASE_URL ?? "http://localhost:8080"
  test("create new doc unit", async ({ page, request }) => {
    await page.goto("/")
    await page.locator("button >> text=Neue Dokumentationseinheit").click()
    await page.waitForSelector("text=Festplatte durchsuchen")
    await expect(page).toHaveURL(
      /\/jurisdiction\/docunit\/[A-Z0-9]{14}\/files$/
    )

    // Given the earlier expectation we can assume that the regex will match...
    const documentNumber = (/jurisdiction\/docunit\/(.*)\/files/g.exec(
      page.url()
    ) || [])[1]
    const response = await request.get(`${backendHost}/api/v1/docunits`)
    const units = await response.json()
    await Promise.all(
      units
        .filter((unit) => unit.documentnumber === documentNumber)
        .map((unit) =>
          request.delete(`${backendHost}/api/v1/docunits/${unit.uuid}`)
        )
    )
  })

  test("delete doc unit", async ({ page, request }) => {
    const response = await request.post(`${backendHost}/api/v1/docunits`, {
      data: { documentationCenterAbbreviation: "foo", documentType: "X" },
    })
    const { documentnumber: documentNumber } = await response.json()
    await page.goto("/")
    await expect(
      page.locator(`a[href*="/jurisdiction/docunit/${documentNumber}/files"]`)
    ).toBeVisible()
    await page
      .locator("tr", {
        hasText: documentNumber,
      })
      .locator("[aria-label='Dokumentationseinheit löschen']")
      .click()
    await page.locator('button:has-text("Löschen")').click()
    await expect(
      page.locator(`a[href*="/jurisdiction/docunit/${documentNumber}/files"]`)
    ).not.toBeVisible()
  })

  testWithDocUnit(
    "cancel delete doc unit",
    async ({ page, documentNumber }) => {
      await page.goto("/")
      await expect(
        page.locator(`a[href*="/jurisdiction/docunit/${documentNumber}/files"]`)
      ).toBeVisible()
      await page
        .locator("tr", {
          hasText: documentNumber,
        })
        .locator("[aria-label='Dokumentationseinheit löschen']")
        .click()
      await page.locator('button:has-text("Abbrechen")').click()
      await expect(
        page.locator(`a[href*="/jurisdiction/docunit/${documentNumber}/files"]`)
      ).toBeVisible()
    }
  )
})
