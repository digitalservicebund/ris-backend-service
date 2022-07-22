import fs from "fs"
import { test, expect } from "@playwright/test"
import { deleteDocUnit, generateDocUnit, uploadTestfile } from "./e2e-utils"

test.describe("upload an original document to a doc unit and delete it again", () => {
  let documentNumber: string

  test.beforeEach(async ({ page }) => {
    documentNumber = await generateDocUnit(page)
  })

  test.afterEach(async ({ page }) => {
    await deleteDocUnit(page, documentNumber)
  })

  test("upload docx file per file chooser", async ({ page }) => {
    await uploadTestfile(page, "sample.docx")
    await page.waitForSelector(
      ".fileviewer-info-panel-value >> text=sample.docx"
    )
    await page.waitForSelector("text=Die ist ein Test")
  })

  test("upload non-docx file per file chooser", async ({ page }) => {
    await uploadTestfile(page, "sample.png")
    await expect(
      page.locator("text=Das ausgewählte Dateiformat ist nicht korrekt.")
    ).toBeVisible()
  })

  test("drag over docx file in upload area", async ({ page }) => {
    const docx = await fs.promises.readFile(
      "./test/e2e/testfiles/sample.docx",
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

  test("drag over non-docx file in upload area", async ({ page }) => {
    const png = await fs.promises.readFile(
      "./test/e2e/testfiles/sample.png",
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
    await page.dispatchEvent(".upload-drop-area", "dragover", { dataTransfer })
    await expect(
      page.locator("text=Datei wird nicht unterstützt.")
    ).toBeVisible()
  })

  test("drop docx file in upload area", async ({ page }) => {
    const docx = await fs.promises.readFile(
      "./test/e2e/testfiles/sample.docx",
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
    await expect(page.locator("text=Upload läuft")).toBeVisible()
  })

  test("drop non-docx file in upload area", async ({ page }) => {
    const png = await fs.promises.readFile(
      "./test/e2e/testfiles/sample.png",
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
