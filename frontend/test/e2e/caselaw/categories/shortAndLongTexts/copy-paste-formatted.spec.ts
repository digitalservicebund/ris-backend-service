import { expect } from "@playwright/test"
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

test("copy-paste from side panel", async ({ page, documentNumber }) => {
  const leftAlignText = "I am left aligned"
  const rightAlignText = "I am right aligned"
  const centerAlignText = "I am centered"
  const justifyAlignText = "I am justify aligned"
  const leftAlignTextWithStyle = `<span style="color: rgb(0, 0, 0)">${leftAlignText}</span>`
  const rightAlignTextWithStyle = `<p style="text-align: right"><span style="color: rgb(0, 0, 0)">${rightAlignText}</span></p>`
  const centerAlignTextWithStyle = `<p style="text-align: center">${centerAlignText}</p>`
  const justifyAlignTextWithStyle = `<p style="text-align: justify">${justifyAlignText}</p>`

  // upload file
  await uploadTestfile(page, "some-text-aligment.docx")
  await expect(page.getByText("some-text-aligment.docx")).toBeVisible()
  await expect(page.getByLabel(`Datei löschen`)).toBeVisible()
  await expect(page.getByText(leftAlignText)).toBeVisible()
  await expect(page.getByText(rightAlignText)).toBeVisible()
  await expect(page.getByText(centerAlignText)).toBeVisible()
  await expect(page.getByText(justifyAlignText)).toBeVisible()

  // Click on "Rubriken" und check if original document loaded
  await navigateToCategories(page, documentNumber)

  await expect(page.getByLabel("Ladestatus")).toBeHidden()
  await expect(page.getByText(rightAlignText)).toBeVisible()
  await expect(page.getByText(centerAlignText)).toBeVisible()
  await expect(page.getByText(justifyAlignText)).toBeVisible()
  const originalFileParagraph = page.getByText("centered")
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

  // paste from clipboard into input field "Entscheidungsgründe"
  const inputField = page.locator("[data-testid='Leitsatz']")
  await inputField.click()
  await page.keyboard.press(`${modifier}+KeyV`)

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
