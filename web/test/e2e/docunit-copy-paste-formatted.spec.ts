import { test, Page, expect } from "@playwright/test"
import { deleteDocUnit, generateDocUnit } from "./docunit-lifecycle.spec"
import { uploadTestfile } from "./docunit-odoc-upload-delete.spec"
import { pageReload } from "./docunit-store-changes.spec"
import { getAuthenticatedPage } from "./e2e-utils"

test.describe("test copy-pasting formatted content from odoc to KurzLangtexte-field", () => {
  // SETUP

  let documentNumber: string
  let page: Page

  test.beforeAll(async ({ browser, browserName }) => {
    if (browserName !== "chromium") return

    page = await getAuthenticatedPage(browser)

    documentNumber = await generateDocUnit(page)
  })

  test.afterAll(async ({ browserName }) => {
    if (browserName !== "chromium") return

    await deleteDocUnit(page, documentNumber)
  })

  // TESTS

  test("copy-paste from odoc panel to Langtext-field", async ({
    browserName,
  }) => {
    // copy-paste only works in chromium
    // see https://github.com/microsoft/playwright/issues/13037#issuecomment-1078208810
    // and https://github.com/microsoft/playwright/issues/8114#issue-964733495
    if (browserName !== "chromium") return

    await page.goto("/")
    await page
      .locator(`a[href*="/rechtsprechung/${documentNumber}/dokumente"]`)
      .click()

    await uploadTestfile(page, "some-formatting.docx")
    await page.waitForSelector(
      ".fileviewer-info-panel-value >> text=some-formatting.docx"
    )
    await page.waitForSelector("text=Headline")

    await page
      .locator(`a[href*="/rechtsprechung/${documentNumber}/rubriken"]`)
      .first()
      .click()
    await page.locator("id=odoc-open-element").click()

    let editorField = await page.locator("id=gruende_editor >> div")

    const odocEditor = await page.locator("id=odoc_editor >> div")
    let odocContent = await odocEditor.innerHTML()

    await odocEditor.focus()
    await odocEditor.selectText()
    const platform = await page.evaluate(() => navigator.platform)
    const modifier = platform.toLowerCase().includes("mac") ? "Meta" : "Control"

    await page.keyboard.press(`${modifier}+KeyC`)
    await editorField.click()
    await page.keyboard.press(`${modifier}+KeyV`)

    await page.locator("button >> text=Speichern").last().click()

    await pageReload(page)

    await page
      .locator(`a[href*="/rechtsprechung/${documentNumber}/rubriken"]`)
      .first()
      .click()

    editorField = await page.locator("id=gruende_editor >> div")
    let editorFieldContent = await editorField.innerHTML()
    // This removal of the first tag is necessary because the original docx has an empty <p> tag with a font size
    // defined. The editor however seems to be stripping away the font size within this empty <p> tag upon pasting.
    // This makes the two HTML strings not equal. However, for the purpose of this comparison, it is sufficient
    // if all the rest of the HTML strings are equal.
    odocContent = odocContent.substring(odocContent.indexOf(">"))
    editorFieldContent = editorFieldContent.substring(
      editorFieldContent.indexOf(">")
    )

    expect(odocContent).toBe(editorFieldContent)
  })
})
