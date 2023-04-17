import AxeBuilder from "@axe-core/playwright"
import { expect } from "@playwright/test"
import { navigateToPublication } from "../../e2e/caselaw/e2e-utils"
import { testWithDocumentUnit as test } from "../../e2e/caselaw/fixtures"

test.describe("a11y of publication page (/caselaw/documentunit/{documentNumber}/publication)", () => {
  test("publication", async ({ page, documentNumber }) => {
    await navigateToPublication(page, documentNumber)
    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("publication not possible", async ({ page, documentNumber }) => {
    await navigateToPublication(page, documentNumber)

    await navigateToPublication(page, documentNumber)

    await page
      .locator("[aria-label='Empfängeradresse E-Mail']")
      .fill("wrong.email")
    await page.keyboard.down("Tab")

    await page
      .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
      .click()
    await page
      .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
      .click()
    await expect(page.locator("text=E-Mail-Adresse ungültig")).toBeVisible()

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})
