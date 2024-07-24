import { expect, Locator, Page } from "@playwright/test"
import {
  navigateToFiles,
  uploadTestfile,
  navigateToCategories,
} from "../../e2e-utils"
import { caselawTest as test } from "../../fixtures"

// eslint-disable-next-line playwright/no-skipped-test
test.skip(
  ({ browserName }) => browserName !== "chromium",
  "Skipping in engines other than chromium, reason playwright diriven for firefox and safari does not support copy paste type='text/html' from clipboard",
)

test.beforeEach(async ({ page, documentNumber }) => {
  await navigateToFiles(page, documentNumber)
})

test("copy-paste text with different styles and alignments from side panel", async ({
  page,
  documentNumber,
}) => {
  const leftAlignText = "I am left aligned"
  const rightAlignText = "I am right aligned"
  const centerAlignText = "I am centered"
  const justifyAlignText = "I am justify aligned"
  const leftAlignTextWithStyle = `<span style="color: rgb(0, 0, 0)">${leftAlignText}</span>`
  const rightAlignTextWithStyle = `<p style="text-align: right"><span style="color: rgb(0, 0, 0)">${rightAlignText}</span></p>`
  const centerAlignTextWithStyle = `<p style="text-align: center">${centerAlignText}</p>`
  const justifyAlignTextWithStyle = `<p style="text-align: justify">${justifyAlignText}</p>`

  await test.step("upload document", async () => {
    await uploadTestfile(page, "some-text-aligment.docx")
    await expect(page.getByText("some-text-aligment.docx")).toBeVisible()
    await expect(page.getByLabel(`Datei löschen`)).toBeVisible()
    await expect(page.getByLabel("Ladestatus")).toBeHidden()
    await expect(page.getByText(leftAlignText)).toBeVisible()
    await expect(page.getByText(rightAlignText)).toBeVisible()
    await expect(page.getByText(centerAlignText)).toBeVisible()
    await expect(page.getByText(justifyAlignText)).toBeVisible()
  })

  await test.step("navigate to categories", async () => {
    await navigateToCategories(page, documentNumber)
  })

  await test.step("copy and paste document text into text editor field, check that the style is applied", async () => {
    const originalFileParagraph = page.getByText("centered")
    const inputField = await copyPaste(originalFileParagraph, page)

    // Check all text copied
    const inputFieldAlleText = await inputField.allTextContents()
    expect(inputFieldAlleText[0].includes(leftAlignText)).toBeTruthy()
    expect(inputFieldAlleText[0].includes(rightAlignText)).toBeTruthy()
    expect(inputFieldAlleText[0].includes(centerAlignText)).toBeTruthy()
    expect(inputFieldAlleText[0].includes(justifyAlignText)).toBeTruthy()

    // hide invisible characters
    await inputField.click()
    await page.getByLabel("invisible-characters").click()

    const inputFieldInnerHTML = await inputField.innerHTML()
    // Check all text copied with style
    expect(inputFieldInnerHTML.includes(leftAlignTextWithStyle)).toBeTruthy()
    expect(inputFieldInnerHTML.includes(rightAlignTextWithStyle)).toBeTruthy()
    expect(inputFieldInnerHTML.includes(centerAlignTextWithStyle)).toBeTruthy()
    expect(inputFieldInnerHTML.includes(justifyAlignTextWithStyle)).toBeTruthy()
  })
})

test(
  "copy-paste indented text from side panel",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-4120",
    },
  },
  async ({ page, documentNumber }) => {
    const noIndentationText = "Abschnitt mit Einzug 0"
    const singleIndentationText = "Abschnitt mit Einzug 1"
    const doubleIndentationText = "Abschnitt mit Einzug 2"
    const tripleIndentationText = "Abschnitt mit Einzug 3"
    const noIndentation = `<p>Abschnitt mit Einzug 0</p>`
    const singleIndentation = `<p style="margin-left: 40px!important;">Abschnitt mit Einzug 1</p>`
    const doubleIndentation = `<p style="margin-left: 80px!important;">Abschnitt mit Einzug 2</p>`
    const tripleIndentation = `<p style="margin-left: 120px!important;">Abschnitt mit Einzug 3</p>`

    await test.step("upload document", async () => {
      await uploadTestfile(page, "some-indentations.docx")
      await expect(page.getByText("some-indentations.docx")).toBeVisible()
      await expect(page.getByLabel(`Datei löschen`)).toBeVisible()
      await expect(page.getByLabel("Ladestatus")).toBeHidden()
      await expect(page.getByText(noIndentationText)).toBeVisible()
      await expect(page.getByText(singleIndentationText)).toBeVisible()
      await expect(page.getByText(doubleIndentationText)).toBeVisible()
      await expect(page.getByText(tripleIndentationText)).toBeVisible()
    })

    await test.step("navigate to categories", async () => {
      await navigateToCategories(page, documentNumber)
    })

    await test.step("copy and paste document text into text editor field, check that the style is applied", async () => {
      const originalFileParagraph = page.getByText("Text", { exact: true })
      const inputField = await copyPaste(originalFileParagraph, page)

      // Check all text copied
      const inputFieldAlleText = await inputField.allTextContents()
      expect(inputFieldAlleText[0].includes(noIndentationText)).toBeTruthy()
      expect(inputFieldAlleText[0].includes(singleIndentationText)).toBeTruthy()
      expect(inputFieldAlleText[0].includes(doubleIndentationText)).toBeTruthy()
      expect(inputFieldAlleText[0].includes(tripleIndentationText)).toBeTruthy()

      // hide invisible characters
      await inputField.click()
      await page.getByLabel("invisible-characters").click()

      const inputFieldInnerHTML = await inputField.innerHTML()
      // Check all text copied with style
      expect(inputFieldInnerHTML.includes(singleIndentation)).toBeTruthy()
      expect(inputFieldInnerHTML.includes(doubleIndentation)).toBeTruthy()
      expect(inputFieldInnerHTML.includes(tripleIndentation)).toBeTruthy()
      expect(inputFieldInnerHTML.includes(noIndentation)).toBeTruthy()
    })
  },
)

test(
  "copy-paste lists from side panel",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-4112",
    },
  },
  async ({ page, documentNumber }) => {
    const bulletListItemText = "This is a bullet list"
    const bulletListSecondItemText = "Second bullet list item"
    const orderedListItemText = "This is an ordered list"
    const orderedListSecondItemText = "Second ordered list item"
    const bulletList = `<ul style="list-style-type:disc;" class="list-disc"><li><p>This is a bullet list</p></li><li><p>Second bullet list item</p></li></ul>`
    const orderedList = `ol style="list-style-type:decimal;" class="list-decimal"><li><p>This is an ordered list</p></li><li><p>Second ordered list item</p></li></ol>`

    await test.step("upload test file", async () => {
      await uploadTestfile(page, "some-lists.docx")
      await expect(page.getByText("some-lists.docx")).toBeVisible()
      await expect(page.getByLabel(`Datei löschen`)).toBeVisible()
      await expect(page.getByLabel("Ladestatus")).toBeHidden()
      await expect(page.getByText(bulletListItemText)).toBeVisible()
      await expect(page.getByText(bulletListSecondItemText)).toBeVisible()
      await expect(page.getByText(orderedListItemText)).toBeVisible()
      await expect(page.getByText(orderedListSecondItemText)).toBeVisible()
    })

    await test.step("navigate to categories", async () => {
      await navigateToCategories(page, documentNumber)
    })

    await test.step("copy and paste document text into text editor field, check that the style is applied", async () => {
      const originalFileParagraph = page.getByText("Text", {
        exact: true,
      })
      const inputField = await copyPaste(originalFileParagraph, page)

      // Check all text copied
      const inputFieldAllText = await inputField.allTextContents()
      expect(inputFieldAllText[0].includes(bulletListItemText)).toBeTruthy()
      expect(
        inputFieldAllText[0].includes(bulletListSecondItemText),
      ).toBeTruthy()
      expect(inputFieldAllText[0].includes(orderedListItemText)).toBeTruthy()
      expect(
        inputFieldAllText[0].includes(orderedListSecondItemText),
      ).toBeTruthy()

      // hide invisible characters
      await inputField.click()
      await page.getByLabel("invisible-characters").click()

      const inputFieldInnerHTML = await inputField.innerHTML()
      // Check all text copied with style
      expect(inputFieldInnerHTML.includes(bulletList)).toBeTruthy()
      expect(inputFieldInnerHTML.includes(orderedList)).toBeTruthy()
    })
  },
)

async function copyPaste(originalFileParagraph: Locator, page: Page) {
  await expect(originalFileParagraph).toBeVisible()

  // Selected all text from sidepanel
  await originalFileParagraph.evaluate((element) => {
    const originalFile = element.parentElement

    if (!originalFile) {
      throw new Error("No original file available.")
    }

    const selection = window.getSelection()
    const elementChildsLength = originalFile.childNodes.length
    const startOffset = 0
    const range = document.createRange()
    range.setStart(originalFile.childNodes[0], startOffset)
    range.setEnd(originalFile.childNodes[elementChildsLength - 1], startOffset)
    selection?.removeAllRanges()
    selection?.addRange(range)
  })

  // copy from sidepanel to clipboard
  // eslint-disable-next-line playwright/no-conditional-in-test
  const modifier = (await page.evaluate(() => navigator.platform))
    .toLowerCase()
    .includes("mac")
    ? "Meta"
    : "Control"
  await page.keyboard.press(`${modifier}+KeyC`)

  // paste from clipboard into input field "Leitsatz"
  const inputField = page.locator("[data-testid='Leitsatz']")
  await inputField.click()
  await page.keyboard.press(`${modifier}+KeyV`)

  return inputField
}
