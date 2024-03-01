import { expect } from "@playwright/test"
import { navigateToFiles, uploadTestfile } from "../../e2e-utils"
import { caselawTest as test } from "../../fixtures"

// eslint-disable-next-line playwright/no-skipped-test
test.skip(
  ({ browserName }) => browserName !== "chromium",
  "Skipping in engines other than chromium, reason playwright diriven for firefox and safari does not support copy paste type='text/html' from clipboard",
)

test.beforeEach(async ({ page, documentNumber }) => {
  await navigateToFiles(page, documentNumber)
})

/*
    1. Upload document with border numbers
    2. Copy border numbers into reasons
    3. Create a valid and an invalid border number link in the Leitsatz
    4. Check validation state after save
    5. Delete original border numbers in reasons
    6. Check validation state after save
*/
// eslint-disable-next-line playwright/no-skipped-test
test.skip("create and validate border number links", async ({ page }) => {
  // Copy border numbers from side panel into reasons to have reference data
  const documentOrigin = "Gründe:"
  const firstReason = "First reason"
  const secondReason = "Second reason"
  const thirdReason = "Third reason"

  // upload file
  await uploadTestfile(page, "some-border-numbers.docx")
  await expect(page.locator(`text=some-border-numbers.docx`)).toBeVisible()
  await expect(page.locator(`text=Datei löschen`)).toBeVisible()
  await expect(page.locator(`text=${firstReason}`)).toBeVisible()
  await expect(page.locator(`text=${secondReason}`)).toBeVisible()
  await expect(page.locator(`text=${thirdReason}`)).toBeVisible()

  // Click on "Rubriken" und check if original document loaded
  await page.locator("a >> text=Rubriken").click()
  await page.getByLabel("Originaldokument öffnen").click()
  await expect(page.getByLabel("Ladestatus")).toBeHidden()
  await expect(page.locator(`text=${firstReason}`)).toBeVisible()
  await expect(page.locator(`text=${secondReason}`)).toBeVisible()
  await expect(page.locator(`text=${thirdReason}`)).toBeVisible()
  const originalFileParagraph = page.locator(`text=${documentOrigin}`)
  await expect(originalFileParagraph).toBeVisible()

  // Selected all text from sidepanel
  await originalFileParagraph.evaluate((element) => {
    const originalFile = element.parentElement?.parentElement?.parentElement

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
  const inputField = page.locator("[data-testid='Gründe']")
  await inputField.click()
  await page.keyboard.press(`${modifier}+KeyV`)
  const inputFieldInnerHTML = await inputField.innerHTML()

  // Check all text copied
  const inputFieldAlleText = await inputField.allTextContents()
  expect(inputFieldAlleText[0].includes(firstReason)).toBeTruthy()
  expect(inputFieldAlleText[0].includes(secondReason)).toBeTruthy()
  expect(inputFieldAlleText[0].includes(thirdReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes(firstReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes(secondReason)).toBeTruthy()
  expect(inputFieldInnerHTML.includes(thirdReason)).toBeTruthy()

  // Create valid and invalid border number link in Leitsatz
  const guidingPrincipleInput = page.locator("[data-testid='Leitsatz']")
  await guidingPrincipleInput.click()
  await page.keyboard.type(`#1# #4# `)

  // save
  await page.getByText("Speichern").click()
  await page.waitForEvent("requestfinished")

  // valid border number link
  const borderNumberLink = page
    .locator("[data-testid='Leitsatz']")
    .locator("border-number-link")
  await expect(borderNumberLink.first()).toHaveAttribute("valid", "true")
  await expect(borderNumberLink.first()).toHaveClass(
    'font-bold text-white bg-blue-700 before:content-["Rd_"]',
  )

  // invalid border number link
  await expect(borderNumberLink.last()).toHaveAttribute("valid", "false")
  await expect(borderNumberLink.last()).toHaveClass(
    'font-bold text-red-900 bg-red-200 before:content-["⚠Rd_"]',
  )

  // Delete border numbers in reasons
  const reasons = page.locator("[data-testid='Gründe']")
  await reasons.click()
  await page.keyboard.press("Meta+A")
  await page.keyboard.press("Backspace")

  // save
  await page.getByText("Speichern").click()
  await page.waitForEvent("requestfinished")

  // check first border number link: should be invalid now
  await expect(borderNumberLink.first()).toHaveAttribute("valid", "false")
  await expect(borderNumberLink.first()).toHaveClass(
    'font-bold text-red-900 bg-red-200 before:content-["⚠Rd_"]',
  )
})
