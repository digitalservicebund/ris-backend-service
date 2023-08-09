import AxeBuilder from "@axe-core/playwright"
import { expect } from "@playwright/test"
import { caselawTest as test } from "../../e2e/caselaw/fixtures"

test.describe("a11y of start page (/caselaw)", () => {
  test("documentUnit list", async ({ page }) => {
    await page.goto("/")
    await expect(page.locator("text=Neue Dokumentationseinheit")).toBeVisible()
    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("delete documentUnit popup", async ({ page, documentNumber }) => {
    await page.goto("/")
    await expect(page.locator("text=Neue Dokumentationseinheit")).toBeVisible()
    await page
      .getByLabel("Dokumentnummer oder Aktenzeichen Suche")
      .fill(documentNumber)
    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
    //TODO: remove the timeout when search performance get better
    await expect(
      page.locator(".table-row", {
        hasText: documentNumber,
      }),
    ).toBeVisible({ timeout: 10000 })
    await page
      .locator(".table-row", {
        hasText: documentNumber,
      })
      .locator("[aria-label='Dokumentationseinheit löschen']")
      .click()
    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})
