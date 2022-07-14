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
    await uploadTestfile(page, "screenshot.png")
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
