import { expect } from "@playwright/test"
import { navigateToFiles, uploadTestfile } from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.skip(
  ({ browserName }) => browserName !== "chromium",
  "Skipping in engines other than chromium, reason playwright diriven for firefox and safari does not support copy paste type='text/html' from clipboard"
)

test.beforeEach(async ({ page, documentNumber }) => {
  await navigateToFiles(page, documentNumber)
  await page.goto("/")
  await page
    .locator(`a[href*="/jurisdiction/documentunit/${documentNumber}/files"]`)
    .click()
})

test("copy-paste from side panel", async ({ page }) => {
  const leftAlignText = "I am left aligned"
  const rightAlignText = "I am right aligned"
  const centerAlignText = "I am centered"
  const justifyAlignText = "I am justify aligned"
  const leftAlignTextWithStyle = `<p>${leftAlignText}</p>`
  const rightAlignTextWithStyle = `<p style="text-align: right">${rightAlignText}</p>`
  const centerAlignTextWithStyle = `<p style="text-align: center">${centerAlignText}</p>`
  const justifyAlignTextWithStyle = `<p style="text-align: justify">${justifyAlignText}</p>`

  // upload file
  await uploadTestfile(page, "some-text-aligment.docx")
  await page.waitForSelector("text=some-text-aligment.docx")
  await page.waitForSelector("text=Datei löschen")
  await expect(page.locator(`text=${leftAlignText}`)).toBeVisible()
  await expect(page.locator(`text=${rightAlignText}`)).toBeVisible()
  await expect(page.locator(`text=${centerAlignText}`)).toBeVisible()
  await expect(page.locator(`text=${justifyAlignText}`)).toBeVisible()

  // Click on "Rubriken" und check if original document loaded
  await page.locator("a >> text=Rubriken").click()
  await page.locator("[aria-label='Originaldokument öffnen']").click()
  await expect(page.locator("text=Dokument wird geladen")).not.toBeVisible()
  await expect(page.locator(`text=${rightAlignText}`)).toBeVisible()
  await expect(page.locator(`text=${centerAlignText}`)).toBeVisible()
  await expect(page.locator(`text=${justifyAlignText}`)).toBeVisible()
  const originalFileParagraph = page.locator(`text=${leftAlignText}`)
  await expect(originalFileParagraph).toBeVisible()

  // Selected all text from sidepanel
  await originalFileParagraph.evaluate((element) => {
    const originalFile = element.parentElement.parentElement
    if (originalFile !== null) {
      const selection = window.getSelection()
      const elementChildsLength = originalFile.childNodes.length
      const startOffset = 0
      const range = document.createRange()
      range.setStart(originalFile.childNodes[0], startOffset)
      range.setEnd(
        originalFile.childNodes[elementChildsLength - 1],
        startOffset
      )
      selection?.removeAllRanges()
      selection?.addRange(range)
    }
  })
  // copy from sidepanel to clipboard
  const modifier = (await page.evaluate(() => navigator.platform))
    .toLowerCase()
    .includes("mac")
    ? "Meta"
    : "Control"
  await page.keyboard.press(`${modifier}+KeyC`)

  // paste from clipboard into input field "Entscheidungsgründe"
  const inputField = page.locator(
    '[aria-label="Entscheidungsgründe Editor Feld"]'
  )
  await inputField.click()
  await page.keyboard.press(`${modifier}+KeyV`)
  const inputFieldInnerHTML = await inputField.innerHTML()

  // Check all text copied
  const inputFieldAlleText = await inputField.allTextContents()
  expect(inputFieldAlleText[0].includes(leftAlignText)).toBeTruthy()
  expect(inputFieldAlleText[0].includes(rightAlignText)).toBeTruthy()
  expect(inputFieldAlleText[0].includes(centerAlignText)).toBeTruthy()
  expect(inputFieldAlleText[0].includes(justifyAlignText)).toBeTruthy()

  // Check all text copied with style
  expect(inputFieldInnerHTML.includes(leftAlignTextWithStyle)).toBeTruthy()
  expect(inputFieldInnerHTML.includes(rightAlignTextWithStyle)).toBeTruthy()
  expect(inputFieldInnerHTML.includes(centerAlignTextWithStyle)).toBeTruthy()
  expect(inputFieldInnerHTML.includes(justifyAlignTextWithStyle)).toBeTruthy()
})
