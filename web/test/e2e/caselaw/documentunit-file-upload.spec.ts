import fs from "fs"
import { expect } from "@playwright/test"
import { navigateToFiles, uploadTestfile } from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("upload an original document to a doc unit", () => {
  test.beforeEach(async ({ page, documentNumber }) => {
    await navigateToFiles(page, documentNumber)
  })

  test("upload and delete docx file per file chooser", async ({
    page,
    documentNumber,
  }) => {
    await uploadTestfile(page, "sample.docx")
    await expect(page.locator("text=Hochgeladen am")).toBeVisible()
    await page.waitForSelector("text=sample.docx")
    await page.waitForSelector("text=Die ist ein Test")

    // delete file
    await page.locator("text=Datei löschen").click()
    await page.locator("[role='dialog'] >> button:has-text('Löschen')").click()
    await expect(
      page.locator(
        "text=Aktuell ist keine Datei hinterlegt. Wählen Sie die Datei des Originaldokumentes aus"
      )
    ).toBeVisible()
    await page.goto("/")
    await expect(
      page.locator(`a[href*="/caselaw/documentunit/${documentNumber}/files"]`)
    ).toBeVisible()
    await page.locator(".table-row", {
      hasText: documentNumber,
    })
    await page.locator(".table-row", {
      hasText: "sample.docx",
    })
  })

  test("upload non-docx file per file chooser", async ({ page }) => {
    await uploadTestfile(page, "sample.png")
    await expect(
      page.locator("text=Das ausgewählte Dateiformat ist nicht korrekt.")
    ).toBeVisible()
  })

  test("drag over docx file in upload area", async ({ page }) => {
    const docx = await fs.promises.readFile(
      "./test/e2e/caselaw/testfiles/sample.docx",
      "utf-8"
    )
    const dataTransfer = await page.evaluateHandle((docx) => {
      const data = new DataTransfer()
      const file = new File([`${docx}`], "sample.docx", {
        type: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      })
      data.items.add(file)
      return data
    }, docx)

    await page.dispatchEvent(".upload-drop-area", "dragover", { dataTransfer })
    await expect(
      page.locator("text=Datei in diesen Bereich ziehen")
    ).toBeVisible()
  })

  test("drop docx file in upload area", async ({ page }) => {
    const docx = await fs.promises.readFile(
      "./test/e2e/caselaw/testfiles/sample.docx",
      "utf-8"
    )
    const dataTransfer = await page.evaluateHandle((docx) => {
      const data = new DataTransfer()
      const file = new File([`${docx}`], "sample.docx", {
        type: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      })
      data.items.add(file)
      return data
    }, docx)

    await page.dispatchEvent(".upload-drop-area", "drop", { dataTransfer })
  })

  test("drop non-docx file in upload area", async ({ page }) => {
    const png = await fs.promises.readFile(
      "./test/e2e/caselaw/testfiles/sample.png",
      "utf-8"
    )
    const dataTransfer = await page.evaluateHandle((png) => {
      const data = new DataTransfer()
      const file = new File([`${png}`], "sample.png", {
        type: "image/png",
      })
      data.items.add(file)
      return data
    }, png)

    await page.dispatchEvent("#upload-drop-area", "drop", { dataTransfer })
    await expect(
      page.locator("text=Das ausgewählte Dateiformat ist nicht korrekt.")
    ).toBeVisible()
  })
})
