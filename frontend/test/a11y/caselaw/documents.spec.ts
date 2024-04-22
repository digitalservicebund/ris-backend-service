import AxeBuilder from "@axe-core/playwright"
import { expect } from "@playwright/test"
import { caselawTest as test } from "../../e2e/caselaw/fixtures"
import {
  navigateToFiles,
  navigateToPublication,
  uploadTestfile,
} from "~/e2e/caselaw/e2e-utils"

test.describe("a11y of document page (/caselaw/documentunit/{documentNumber}/files)", () => {
  test("upload document", async ({ page, documentNumber }) => {
    await navigateToFiles(page, documentNumber)

    const tableView = page.getByRole("cell", {
      name: "Hochgeladen am",
      exact: true,
    })

    await uploadTestfile(page, "sample.docx")
    await expect(tableView).toBeVisible()
    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("delete document", async ({ page, documentNumber }) => {
    await navigateToFiles(page, documentNumber)
    const tableView = page.getByRole("cell", {
      name: "Hochgeladen am",
      exact: true,
    })
    await uploadTestfile(page, "sample.docx")
    await page.getByLabel("Datei löschen").click()
    await page.getByRole("button", { name: "Löschen", exact: true }).click()
    await expect(tableView).toBeHidden()

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("delete document modal", async ({ page, documentNumber }) => {
    await navigateToFiles(page, documentNumber)
    await uploadTestfile(page, "sample.docx")
    await page.getByLabel("Datei löschen").click()
    await page.locator("[aria-label='Anhang löschen']").click()

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
      page.locator(
        "text=sample.png - Das ausgewählte Dateiformat ist nicht korrekt. Versuchen Sie eine .docx-Version dieser Datei hochzuladen.",
      ),
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
      page.locator("text=Es sind noch nicht alle Pflichtfelder befüllt."),
    ).toBeVisible()

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})
