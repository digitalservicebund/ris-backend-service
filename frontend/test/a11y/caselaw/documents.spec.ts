import AxeBuilder from "@axe-core/playwright"
import { expect } from "@playwright/test"
import { checkA11y, injectAxe } from "axe-playwright"
import {
  navigateToFiles,
  navigateToPublication,
  uploadTestfile,
} from "../../e2e/caselaw/e2e-utils"
import { caselawTest as test } from "../../e2e/caselaw/fixtures"

test.describe("a11y of document page (/caselaw/documentunit/{documentNumber}/files)", () => {
  test("document", async ({ page, documentNumber }) => {
    await navigateToFiles(page, documentNumber)
    await injectAxe(page)
    await checkA11y(page)
  })

  test("upload document", async ({ page, documentNumber }) => {
    await navigateToFiles(page, documentNumber)

    await uploadTestfile(page, "sample.docx")
    await expect(page.locator("text=Hochgeladen am")).toBeVisible()

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("delete document", async ({ page, documentNumber }) => {
    await navigateToFiles(page, documentNumber)

    await uploadTestfile(page, "sample.docx")
    await expect(page.locator("text=Hochgeladen am")).toBeVisible()
    await page.locator("text=Datei löschen").click()

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("upload non-docx file per file chooser", async ({
    page,
    documentNumber,
  }) => {
    await navigateToFiles(page, documentNumber)
    await uploadTestfile(page, "sample.png")
    await expect(
      page.locator("text=Das ausgewählte Dateiformat ist nicht korrekt.")
    ).toBeVisible()

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})

test.describe("a11y of publication page (/caselaw/documentunit/{documentNumber}/publication)", () => {
  test("publication", async ({ page, documentNumber }) => {
    await navigateToPublication(page, documentNumber)
    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("publication not possible", async ({ page, documentNumber }) => {
    await navigateToPublication(page, documentNumber)
    await page
      .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
      .click()
    await expect(
      page.locator("text=Es sind noch nicht alle Pflichtfelder befüllt.")
    ).toBeVisible()

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})
