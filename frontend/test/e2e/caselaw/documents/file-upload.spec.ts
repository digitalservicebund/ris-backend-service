import fs from "fs"
import { expect } from "@playwright/test"
import { navigateToFiles, uploadTestfile } from "../e2e-utils"
import { caselawTest as test } from "../fixtures"
import { createDataTransfer } from "~/e2e/shared/e2e-utils"

test.describe("upload an original document to a doc unit", () => {
  test.beforeEach(async ({ page, documentNumber }) => {
    await navigateToFiles(page, documentNumber)
  })

  test("upload and delete docx file per file chooser", async ({ page }) => {
    await uploadTestfile(page, "sample.docx")
    await expect(page.locator("text=Hochgeladen am")).toBeVisible()
    await expect(page.locator(`text=sample.docx`)).toBeVisible()

    const tableView = page.getByRole("cell", {
      name: "Dateiname",
      exact: true,
    })

    await expect(tableView).toBeVisible()

    // delete file
    await page.getByLabel("Datei löschen").click()
    await page.getByLabel("Löschen", { exact: true }).click() // confirm
    await expect(page.locator(`text=sample.docx`)).toBeHidden()

    await page.reload()
    await expect(page.getByText("Datei in diesen Bereich ziehen")).toBeVisible()
    await expect(tableView).toBeHidden()
  })

  test("upload and delete multiple docx files per file chooser", async ({
    page,
  }) => {
    await uploadTestfile(page, ["sample.docx", "some-formatting.docx"])
    await expect(page.locator("text=Hochgeladen am")).toBeVisible()
    await expect(page.locator(`text=sample.docx`).first()).toBeVisible()
    await expect(page.locator(`text=some-formatting.docx`)).toBeVisible()

    const tableView = page.getByRole("cell", {
      name: "Dateiname",
      exact: true,
    })
    await expect(tableView).toBeVisible()

    // show in side panel when toggled
    await page.getByLabel("Dokumentansicht öffnen").click()
    await expect(page.locator(`text=Die ist ein Test`)).toBeVisible()
    await page.getByLabel("Nächstes Dokument anzeigen").click()
    await expect(page.locator(`text=Die ist ein Test`)).toBeHidden()
    await expect(page.locator(`text=Subheadline`)).toBeVisible()

    // show in side panel when selected in table
    await page.getByLabel("Dokumentansicht schließen").click()
    await expect(page.locator(`text=Subheadline`)).toBeHidden()
    await page.getByText("some-formatting.docx").locator("visible=true").click()
    await expect(page.locator(`text=Subheadline`)).toBeVisible()

    // delete files
    await expect(page.getByLabel("Datei löschen")).toHaveCount(2)
    await page.getByLabel("Datei löschen").nth(0).click()
    await page.getByLabel("Löschen", { exact: true }).click() // confirm
    await expect(page.getByText("sample.docx").nth(0)).toBeHidden() // in table
    await expect(page.getByText("sample.docx").nth(1)).toBeHidden() // in sidepanel

    await expect(page.getByLabel("Datei löschen")).toHaveCount(1)
    await page.getByLabel("Datei löschen").nth(0).click()
    await page.getByLabel("Löschen", { exact: true }).click() // confirm
    await expect(page.locator(`text=some-formatting.docx`)).toBeHidden()

    await page.reload()
    await expect(page.getByText("Datei in diesen Bereich ziehen")).toBeVisible()
    await expect(tableView).toBeHidden()
  })

  test("upload non-docx file per file chooser", async ({ page }) => {
    await uploadTestfile(page, "sample.png")
    await expect(
      page.locator("text=Das ausgewählte Dateiformat ist nicht korrekt."),
    ).toBeVisible()
  })

  test("drag over docx file in upload area", async ({ page }) => {
    const docx = await fs.promises.readFile(
      "./test/e2e/caselaw/testfiles/sample.docx",
    )
    const dataTransfer = await createDataTransfer(
      page,
      docx,
      "sample.docx",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    )

    await page.dispatchEvent(".upload-drop-area", "dragover", { dataTransfer })
    await expect(
      page.locator("text=Datei in diesen Bereich ziehen"),
    ).toBeVisible()
  })

  test("drop docx file in upload area", async ({ page }) => {
    const docx = await fs.promises.readFile(
      "./test/e2e/caselaw/testfiles/sample.docx",
    )
    const dataTransfer = await createDataTransfer(
      page,
      docx,
      "sample.docx",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    )

    await page.dispatchEvent(".upload-drop-area", "drop", { dataTransfer })
    await expect(page.getByText("sample.docx")).toBeVisible()
  })

  test("drop non-docx file in upload area", async ({ page }) => {
    const png = await fs.promises.readFile(
      "./test/e2e/caselaw/testfiles/sample.png",
    )
    const dataTransfer = await createDataTransfer(
      page,
      png,
      "sample.png",
      "image/png",
    )

    await page.dispatchEvent("#upload-drop-area", "drop", { dataTransfer })
    await expect(
      page.locator("text=Das ausgewählte Dateiformat ist nicht korrekt."),
    ).toBeVisible()
  })
})
