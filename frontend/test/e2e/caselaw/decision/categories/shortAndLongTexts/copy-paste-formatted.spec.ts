import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  clickCategoryButton,
  copyPasteTextFromAttachmentIntoEditor,
  navigateToAttachments,
  navigateToCategories,
  uploadTestfile,
} from "~/e2e/caselaw/utils/e2e-utils"

// eslint-disable-next-line playwright/no-skipped-test
test.skip(
  ({ browserName }) => browserName !== "chromium",
  "Skipping in engines other than chromium, reason playwright diriven for firefox and safari does not support copy paste type='text/html' from clipboard",
)

test.beforeEach(async ({ page, documentNumber }) => {
  await navigateToAttachments(page, documentNumber)
})

test("copy-paste text with different styles and alignments from side panel", async ({
  page,
  documentNumber,
}) => {
  const leftAlignText = "I am left aligned"
  const rightAlignText = "I am right aligned"
  const centerAlignText = "I am centered"
  const justifyAlignText = "I am justify aligned"
  const leftAlignTextWithStyle = `<span style="color: rgb(0, 0, 0);">${leftAlignText}</span>`
  const rightAlignTextWithStyle = `<p style="text-align: right;"><span style="color: rgb(0, 0, 0);">${rightAlignText}</span></p>`
  const centerAlignTextWithStyle = `<p style="text-align: center;">${centerAlignText}</p>`
  const justifyAlignTextWithoutStyle = `<p>${justifyAlignText}</p>`

  await test.step("upload document", async () => {
    await uploadTestfile(page, "some-text-aligment.docx")
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
    const attachmentLocator = page.getByText("centered").locator("..")
    await clickCategoryButton("Leitsatz", page)
    const inputField = page.getByTestId("Leitsatz")
    await copyPasteTextFromAttachmentIntoEditor(
      page,
      attachmentLocator,
      inputField,
    )

    // Check all text copied
    const inputFieldAllText = await inputField.allTextContents()
    expect(inputFieldAllText[0]).toContain(leftAlignText)
    expect(inputFieldAllText[0]).toContain(rightAlignText)
    expect(inputFieldAllText[0]).toContain(centerAlignText)
    expect(inputFieldAllText[0]).toContain(justifyAlignText)

    const inputFieldInnerHTML = await inputField.innerHTML()
    // Check all text copied with style
    expect(inputFieldInnerHTML).toContain(leftAlignTextWithStyle)
    expect(inputFieldInnerHTML).toContain(rightAlignTextWithStyle)
    expect(inputFieldInnerHTML).toContain(centerAlignTextWithStyle)
    expect(inputFieldInnerHTML).toContain(justifyAlignTextWithoutStyle)
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
    const noIndentation = /<p[^>]*>Abschnitt mit Einzug 0<\/p>/
    const singleIndentation =
      /<p\s+style="[^"]*margin-left:\s*40px !important[^"]*"\s*>Abschnitt mit Einzug 1<\/p>/
    const doubleIndentation =
      /<p\s+style="[^"]*margin-left:\s*80px !important[^"]*"\s*>Abschnitt mit Einzug 2<\/p>/
    const tripleIndentation =
      /<p\s+style="[^"]*margin-left:\s*120px !important[^"]*"\s*>Abschnitt mit Einzug 3<\/p>/

    await test.step("upload document", async () => {
      await uploadTestfile(page, "some-indentations.docx")
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
      const attachmentLocator = page
        .getByText("Text", { exact: true })
        .locator("..")
      await clickCategoryButton("Leitsatz", page)
      const inputField = page.getByTestId("Leitsatz")
      await copyPasteTextFromAttachmentIntoEditor(
        page,
        attachmentLocator,
        inputField,
      )

      // Check all text copied
      const inputFieldAllText = await inputField.allTextContents()
      expect(inputFieldAllText[0]).toContain(noIndentationText)
      expect(inputFieldAllText[0]).toContain(noIndentationText)
      expect(inputFieldAllText[0]).toContain(doubleIndentationText)
      expect(inputFieldAllText[0]).toContain(tripleIndentationText)

      const inputFieldInnerHTML = await inputField.innerHTML()
      // Check all text copied with style
      expect(inputFieldInnerHTML).toMatch(singleIndentation)
      expect(inputFieldInnerHTML).toMatch(doubleIndentation)
      expect(inputFieldInnerHTML).toMatch(tripleIndentation)
      expect(inputFieldInnerHTML).toMatch(noIndentation)
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
    const bulletList = `<ul class="list-disc" style="list-style-type: disc;"><li><p>This is a bullet list</p></li><li><p>Second bullet list item</p></li></ul>`
    const orderedList = `<ol style="list-style-type: decimal;"><li><p>This is an ordered list</p></li><li><p>Second ordered list item</p></li></ol>`

    await test.step("upload test file", async () => {
      await uploadTestfile(page, "some-lists.docx")
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
      const attachmentLocator = page
        .getByText("Text", { exact: true })
        .locator("..")
      await clickCategoryButton("Leitsatz", page)
      const inputField = page.getByTestId("Leitsatz")
      await copyPasteTextFromAttachmentIntoEditor(
        page,
        attachmentLocator,
        inputField,
      )

      // Check all text copied
      const inputFieldAllText = await inputField.allTextContents()
      expect(inputFieldAllText[0]).toContain(bulletListItemText)
      expect(inputFieldAllText[0]).toContain(bulletListSecondItemText)
      expect(inputFieldAllText[0]).toContain(orderedListItemText)
      expect(inputFieldAllText[0]).toContain(orderedListSecondItemText)

      const inputFieldInnerHTML = await inputField.innerHTML()
      // Check all text copied with style
      expect(inputFieldInnerHTML).toContain(bulletList)
      expect(inputFieldInnerHTML).toContain(orderedList)
    })
  },
)
