import AxeBuilder from "@axe-core/playwright"
import { expect } from "@playwright/test"
import { nagivateToHandover } from "../../e2e/caselaw/e2e-utils"
import { caselawTest as test } from "../../e2e/caselaw/fixtures"

test.describe("a11y of handover page (/caselaw/documentunit/{documentNumber}/handover)", () => {
  test("handover", async ({ page, documentNumber }) => {
    await nagivateToHandover(page, documentNumber)
    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("handover not possible", async ({ page, documentNumber }) => {
    await nagivateToHandover(page, documentNumber)
    await page
      .locator("[aria-label='Dokumentationseinheit an jDV übergeben']")
      .click()
    await expect(
      page.getByText("Es sind noch nicht alle Pflichtfelder befüllt."),
    ).toBeVisible()
    await expect(page.getByText("Email versendet.")).toBeHidden()

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})
