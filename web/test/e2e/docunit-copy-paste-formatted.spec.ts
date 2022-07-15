import { test, expect } from "@playwright/test"
import {
  generateDocUnit,
  deleteDocUnit,
  pageReload,
  uploadTestfile,
} from "./e2e-utils"

// copy-paste only works in chromium
// see https://github.com/microsoft/playwright/issues/13037#issuecomment-1078208810
// and https://github.com/microsoft/playwright/issues/8114#issue-964733495
test.describe("test copy-pasting formatted content from odoc to KurzLangtexte-field", () => {
  let documentNumber: string

  test("generate doc unit", async ({ page, browserName }) => {
    if (browserName !== "chromium") return
    documentNumber = await generateDocUnit(page)
  })

  test.afterAll(async ({ page, browserName }) => {
    if (browserName !== "chromium") return
    await deleteDocUnit(page, documentNumber)
  })

  test("copy-paste from odoc to text-field", async ({ page, browserName }) => {
    if (browserName !== "chromium") return

    await page.goto("/")
    await page
      .locator(`a[href*="/jurisdiction/docunit/${documentNumber}/files"]`)
      .click()
    await uploadTestfile(page, "some-formatting.docx")
    await page.waitForSelector(
      ".fileviewer-info-panel-value >> text=some-formatting.docx"
    )
    await page.waitForSelector("text=Headline")
    await page
      .locator(`a[href*="/jurisdiction/docunit/${documentNumber}/categories"]`)
      .first()
      .click()

    await page.locator("id=odoc-open-element").click()
    let editorField = await page.locator(
      'div[aria-label="Entscheidungsgründe Editor Feld"] div.ProseMirror'
    )
    const odocEditor = await page.locator('div[element-id="odoc"] .ProseMirror')

    let odocContent = await odocEditor.innerHTML()

    const platform = await page.evaluate(() => navigator.platform)
    const modifier = platform.toLowerCase().includes("mac") ? "Meta" : "Control"

    await odocEditor.evaluate((element) => {
      const selection = window.getSelection()
      const elementChildsLength = element.childNodes.length
      const startOffset = 0
      const range = document.createRange()
      range.setStart(element.childNodes[0], startOffset)
      range.setEnd(element.childNodes[elementChildsLength - 1], startOffset)
      selection?.removeAllRanges()
      selection?.addRange(range)
    })
    await page.keyboard.press(`${modifier}+KeyC`)
    await editorField.click()
    await page.keyboard.press(`${modifier}+KeyV`)
    await page.locator("button >> text=Speichern").last().click()
    await pageReload(page)
    await page
      .locator(`a[href*="/jurisdiction/docunit/${documentNumber}/categories"]`)
      .first()
      .click()

    editorField = await page.locator(
      'div[aria-label="Entscheidungsgründe Editor Feld"] div.ProseMirror'
    )
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
