import { test, Page } from "@playwright/test"
import { deleteDocUnit, generateDocUnit } from "./docunit-lifecycle.spec"
import { getAuthenticatedPage } from "./e2e-utils"

test.describe("upload an original document to a doc unit and delete it again", () => {
  // SETUP

  let documentNumber: string
  let page: Page

  test.beforeAll(async ({ browser }) => {
    page = await getAuthenticatedPage(browser)

    documentNumber = await generateDocUnit(page)
  })

  test.afterAll(async () => await deleteDocUnit(page, documentNumber))

  // TESTS

  test("upload original file", async () => {
    await page.goto("/")

    const selectDocUnit = page.locator(
      `tr td:nth-child(1) a[href*="/rechtsprechung/${documentNumber}/dokumente"]`
    )

    await selectDocUnit.click()

    await uploadTestfile(page, "sample.docx")

    await page.waitForSelector(
      ".fileviewer-info-panel-value >> text=sample.docx"
    )
    await page.waitForSelector("text=Die ist ein Test")
  })

  test("delete original file", async () => {
    await page.goto("/")

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

    await page.goto("/")

    await page.waitForSelector(
      `a[href*="/rechtsprechung/${documentNumber}/dokumente"]`
    )
  })
})

export const uploadTestfile = async (page: Page, filename: string) => {
  const [fileChooser] = await Promise.all([
    page.waitForEvent("filechooser"),
    page.locator("text=Festplatte durchsuchen").click(),
  ])
  await fileChooser.setFiles("./test/e2e/testfiles/" + filename)
}
