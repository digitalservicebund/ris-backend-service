import { expect, test } from "@playwright/test"
import { testWithDocumentUnit } from "./fixtures"

test.describe("create a doc unit and delete it again", () => {
  const backendHost = process.env.E2E_BASE_URL ?? "http://localhost:8080"
  test("create new doc unit", async ({ page, request }) => {
    await page.goto("/")
    await page.locator("button >> text=Neue Dokumentationseinheit").click()
    await page.waitForSelector("text=Festplatte durchsuchen")
    await expect(page).toHaveURL(
      /\/jurisdiction\/documentunit\/[A-Z0-9]{14}\/files$/
    )

    // Given the earlier expectation we can assume that the regex will match...
    const documentNumber = (/jurisdiction\/documentunit\/(.*)\/files/g.exec(
      page.url()
    ) || [])[1]
    const response = await request.get(`${backendHost}/api/v1/documentunits`)
    const units = await response.json()
    await Promise.all(
      units
        .filter((unit) => unit.documentnumber === documentNumber)
        .map((unit) =>
          request.delete(`${backendHost}/api/v1/documentunits/${unit.uuid}`)
        )
    )
  })

  test("delete doc unit", async ({ page, request }) => {
    const response = await request.post(`${backendHost}/api/v1/documentunits`, {
      data: { documentationCenterAbbreviation: "foo", documentType: "X" },
    })
    const { documentNumber } = await response.json()
    await page.goto("/")
    await expect(
      page.locator(
        `a[href*="/jurisdiction/documentunit/${documentNumber}/files"]`
      )
    ).toBeVisible()
    await page
      .locator("tr", {
        hasText: documentNumber,
      })
      .locator("[aria-label='Dokumentationseinheit löschen']")
      .click()
    await page.locator('button:has-text("Löschen")').click()
    await expect(
      page.locator(
        `a[href*="/jurisdiction/documentunit/${documentNumber}/files"]`
      )
    ).not.toBeVisible()
  })

  testWithDocumentUnit(
    "cancel delete doc unit",
    async ({ page, documentNumber }) => {
      await page.goto("/")
      await expect(
        page.locator(
          `a[href*="/jurisdiction/documentunit/${documentNumber}/files"]`
        )
      ).toBeVisible()
      await page
        .locator("tr", {
          hasText: documentNumber,
        })
        .locator("[aria-label='Dokumentationseinheit löschen']")
        .click()
      await page.locator('button:has-text("Abbrechen")').click()
      await expect(
        page.locator(
          `a[href*="/jurisdiction/documentunit/${documentNumber}/files"]`
        )
      ).toBeVisible()
    }
  )
})
