import { expect } from "@playwright/test"
import {
  navigateToCategories,
  navigateToFiles,
  uploadTestfile,
} from "../../e2e-utils"
import { caselawTest as test } from "../../fixtures"

// eslint-disable-next-line playwright/no-skipped-test
test.skip(
  ({ browserName }) => browserName !== "chromium",
  "Skipping in engines other than chromium, reason playwright driven for firefox and safari does not support copy paste type='text/html' from clipboard",
)

test.beforeEach(async ({ page, documentNumber }) => {
  await navigateToFiles(page, documentNumber)
})

/*
    1. Upload document with border numbers
    2. Copy border numbers into reasons
    3. Delete all border numbers with button
    4. Delete only selected border numbers via button
    5. Delete border number via backspace in content
    6. delete border number via backspace in number
*/
// eslint-disable-next-line playwright/no-skipped-test
test("delete border numbers via button and backspace", async ({
  page,
  documentNumber,
}) => {
  // Copy border numbers from side panel into reasons to have reference data
  const documentOrigin = "Gründe:"
  const firstReason = "First reason"
  const secondReason = "Second reason"
  const thirdReason = "Third reason"

  // upload file
  await uploadTestfile(page, "some-border-numbers.docx")
  await expect(page.getByText("some-border-numbers.docx")).toBeVisible()
  await expect(page.getByLabel("Datei löschen")).toBeVisible()
  await expect(page.getByText(firstReason)).toBeVisible()
  await expect(page.getByText(secondReason)).toBeVisible()
  await expect(page.getByText(thirdReason)).toBeVisible()

  // Click on "Rubriken" und check if original document loaded
  await navigateToCategories(page, documentNumber)
  await expect(page.getByLabel("Ladestatus")).toBeHidden()
  await expect(page.getByText(firstReason)).toBeVisible()
  await expect(page.getByText(secondReason)).toBeVisible()
  await expect(page.getByText(thirdReason)).toBeVisible()
  const originalFileParagraph = page.getByText(documentOrigin)
  await expect(originalFileParagraph).toBeVisible()

  // Selected all text from sidepanel
  await originalFileParagraph.evaluate((element) => {
    const originalFile = element.parentElement?.parentElement?.parentElement

    if (!originalFile) {
      throw new Error("No original file available.")
    }

    const selection = window.getSelection()
    const elementChildsLength = originalFile.childNodes.length
    const range = document.createRange()
    range.setStart(originalFile.childNodes[0], 0)
    range.setEnd(originalFile.childNodes[elementChildsLength - 1], 0)
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
  const editor = page.locator("[data-testid='Gründe']")
  await editor.click()
  await page.keyboard.press(`${modifier}+KeyV`)
  let inputFieldInnerHTML = await editor.innerText()

  // Check all text copied
  expect(inputFieldInnerHTML.includes("1\n\n" + firstReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes("2\n\n" + secondReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes("3\n\n" + thirdReason)).toBeTruthy()

  // Select all and remove border numbers via button
  await page.keyboard.press(`${modifier}+KeyA`)
  await page.getByLabel("borderNumber").click()

  // Check border numbers have gone
  inputFieldInnerHTML = await editor.innerText()
  expect(inputFieldInnerHTML.includes("1\n\n" + firstReason)).toBeFalsy()
  expect(inputFieldInnerHTML.includes("2\n\n" + secondReason)).toBeFalsy()
  expect(inputFieldInnerHTML.includes("3\n\n" + thirdReason)).toBeFalsy()

  // Revert
  await page.keyboard.press(`${modifier}+KeyV`)

  inputFieldInnerHTML = await editor.innerText()
  expect(inputFieldInnerHTML.includes("1\n\n" + firstReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes("2\n\n" + secondReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes("3\n\n" + thirdReason)).toBeTruthy()

  // Select third reason
  await page.keyboard.down("Shift")
  for (let i = 0; i < 10; i++) {
    await page.keyboard.press("ArrowLeft")
  }
  await page.keyboard.up("Shift")

  // delete border number via button
  await page.getByLabel("borderNumber").click()
  inputFieldInnerHTML = await editor.innerText()
  expect(inputFieldInnerHTML.includes("1\n\n" + firstReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes("2\n\n" + secondReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes("3\n\n" + thirdReason)).toBeFalsy()

  // Revert
  await page.keyboard.press(`${modifier}+KeyA`)
  await page.keyboard.press(`${modifier}+KeyV`)
  inputFieldInnerHTML = await editor.innerText()
  expect(inputFieldInnerHTML.includes("1\n\n" + firstReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes("2\n\n" + secondReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes("3\n\n" + thirdReason)).toBeTruthy()

  // Navigate to the beginning of third reason content
  for (let i = 0; i < 13; i++) {
    await page.keyboard.press("ArrowLeft")
  }

  // Delete border number with backspace
  await page.keyboard.press("Backspace")
  inputFieldInnerHTML = await editor.innerText()
  expect(inputFieldInnerHTML.includes("1\n\n" + firstReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes("2\n\n" + secondReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes("3\n\n" + thirdReason)).toBeFalsy()

  // Revert
  await page.keyboard.press(`${modifier}+KeyA`)
  await page.keyboard.press(`${modifier}+KeyV`)
  inputFieldInnerHTML = await editor.innerText()
  expect(inputFieldInnerHTML.includes("1\n\n" + firstReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes("2\n\n" + secondReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes("3\n\n" + thirdReason)).toBeTruthy()

  // Navigate into the third reason border number
  for (let i = 0; i < 14; i++) {
    await page.keyboard.press("ArrowLeft")
  }

  // Delete border number with backspace
  await page.keyboard.press("Backspace")
  inputFieldInnerHTML = await editor.innerText()
  expect(inputFieldInnerHTML.includes("1\n\n" + firstReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes("2\n\n" + secondReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes("3\n\n" + thirdReason)).toBeFalsy()
})
