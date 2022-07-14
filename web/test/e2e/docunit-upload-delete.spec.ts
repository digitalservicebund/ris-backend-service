import fs from "fs"
import { test, Page, expect } from "@playwright/test"
import { authenticate, deleteDocUnit, generateDocUnit } from "./e2e-utils"

test.describe("upload an original document to a doc unit and delete it again", () => {
  test.beforeAll(async ({ browser }) => {
    authenticate(browser)
  })

  // TESTS

  test("upload docx file per file chooser", async ({ page }) => {
    const documentNumber = await generateDocUnit(page)
    await uploadTestfile(page, "sample.docx")
    await page.waitForSelector(
      ".fileviewer-info-panel-value >> text=sample.docx"
    )
    await page.waitForSelector("text=Die ist ein Test")
    await deleteDocUnit(page, documentNumber)
  })

  test("upload non-docx file per file chooser", async ({ page }) => {
    const documentNumber = await generateDocUnit(page)
    await uploadTestfile(page, "sample.png")
    await expect(
      await page.locator("text=Das ausgewählte Dateiformat ist nicht korrekt.")
    ).toBeVisible()
    await deleteDocUnit(page, documentNumber)
  })

  test("drag over docx file in upload area", async ({ page }) => {
    const documentNumber = await generateDocUnit(page)
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
      await page.locator("text=Datei in diesen Bereich ziehen")
    ).toBeVisible()
    await deleteDocUnit(page, documentNumber)
  })

  test("drag over non-docx file in upload area", async ({ page }) => {
    const documentNumber = await generateDocUnit(page)
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
      await page.locator("text=Datei wird nicht unterstützt.")
    ).toBeVisible()
    await deleteDocUnit(page, documentNumber)
  })

  test("drop docx file in upload area", async ({ page }) => {
    const documentNumber = await generateDocUnit(page)
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
    await expect(
      await page.locator("text=Die Datei sample.docx wird hochgeladen")
    ).toBeVisible()
    await deleteDocUnit(page, documentNumber)
  })

  test("drop non-docx file in upload area", async ({ page }) => {
    const documentNumber = await generateDocUnit(page)
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
    await page.dispatchEvent(".upload-drop-area", "drop", { dataTransfer })
    await expect(
      await page.locator("text=Das ausgewählte Dateiformat ist nicht korrekt.")
    ).toBeVisible()
    await deleteDocUnit(page, documentNumber)
  })
})

export const uploadTestfile = async (page: Page, filename: string) => {
  const [fileChooser] = await Promise.all([
    page.waitForEvent("filechooser"),
    page.locator("text=Festplatte durchsuchen").click(),
  ])
  await fileChooser.setFiles("./test/e2e/testfiles/" + filename)
}
