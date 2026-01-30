import { expect } from "@playwright/test"
import { useAxeBuilder } from "~/a11y/caselaw/a11y.utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToAttachments,
  navigateToHandover,
  uploadTestfile,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe("a11y of document page (/caselaw/documentunit/{documentNumber}/attachments)", () => {
  test("upload document", async ({ page, documentNumber }) => {
    await navigateToAttachments(page, documentNumber)

    const tableView = page.getByRole("cell", {
      name: "Hochgeladen am",
      exact: true,
    })

    await uploadTestfile(page, "sample.docx")
    await expect(tableView).toBeVisible()
    const accessibilityScanResults = await useAxeBuilder(page).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("delete document", async ({ page, documentNumber }) => {
    await navigateToAttachments(page, documentNumber)
    const tableView = page.getByRole("cell", {
      name: "Hochgeladen am",
      exact: true,
    })
    await uploadTestfile(page, "sample.docx")
    await page.getByLabel("Datei löschen").click()
    await page
      .getByRole("button", { name: "Anhang löschen", exact: true })
      .click()
    await expect(tableView).toBeHidden()

    const accessibilityScanResults = await useAxeBuilder(page).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("delete document modal", async ({ page, documentNumber }) => {
    await navigateToAttachments(page, documentNumber)
    await uploadTestfile(page, "sample.docx")
    await page.getByLabel("Datei löschen").click()
    const dialog = page.getByRole("dialog")
    await dialog.getByLabel("Anhang löschen", { exact: true }).click()

    const accessibilityScanResults = await useAxeBuilder(page).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("upload non-docx file per file chooser", async ({
    page,
    documentNumber,
  }) => {
    await navigateToAttachments(page, documentNumber)
    await uploadTestfile(page, "sample.png", { skipAssert: true })
    await expect(
      page.locator(
        "text='sample.png' hat ein falsches Format. Laden Sie eine .docx-Version hoch.",
      ),
    ).toBeVisible()

    const accessibilityScanResults = await useAxeBuilder(page).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})

test.describe("a11y of handover page (/caselaw/documentunit/{documentNumber}/handover)", () => {
  test("handover", async ({ page, documentNumber }) => {
    await navigateToHandover(page, documentNumber)
    const accessibilityScanResults = await useAxeBuilder(page).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("handover not possible", async ({ page, documentNumber }) => {
    await navigateToHandover(page, documentNumber)
    await expect(
      page.getByLabel("Dokumentationseinheit an jDV übergeben"),
    ).toBeDisabled()

    const accessibilityScanResults = await useAxeBuilder(page).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})
