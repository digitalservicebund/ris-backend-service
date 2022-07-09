import { test, Page } from "@playwright/test"
import {
  deleteDocUnit,
  generateDocUnit,
  getAuthenticatedPage,
} from "./docunit-lifecycle.spec"

test.describe("upload an original document to a doc unit and delete it again", () => {
  let documentNumber: string
  let page: Page

  test.beforeAll(async ({ browser }) => {
    page = await getAuthenticatedPage(browser)

    documentNumber = await generateDocUnit(page)
  })

  test.afterAll(async () => await deleteDocUnit(page, documentNumber))

  test("upload original file", async () => {
    await page.goto("/rechtsprechung")

    const selectDocUnit = page.locator(
      `tr td:nth-child(1) a[href*="/rechtsprechung/${documentNumber}/dokumente"]`
    )

    await selectDocUnit.click()

    const [fileChooser] = await Promise.all([
      page.waitForEvent("filechooser"),
      page.locator("text=Festplatte durchsuchen").click(),
    ])
    await fileChooser.setFiles("./test/e2e/sample.docx")

    await page.waitForSelector(
      ".fileviewer-info-panel-value >> text=sample.docx"
    )
  })

  test("delete original file", async () => {
    await page.goto("/rechtsprechung")

    await page
      .locator(`a[href*="/rechtsprechung/${documentNumber}/rubriken"]`)
      .click()

    const documentLink = page.locator(
      'a[href*="/rechtsprechung/' +
        documentNumber +
        '/dokumente"] >> text=DOKUMENTE'
    )
    await documentLink.click()

    await page.locator("text=Datei löschen").click()

    await page.waitForSelector("text=Festplatte durchsuchen")

    await page.goto("/rechtsprechung")

    await page.waitForSelector(
      `a[href*="/rechtsprechung/${documentNumber}/dokumente"]`
    )
  })
})
