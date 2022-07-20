import { test, expect } from "@playwright/test"
import { generateDocUnit, deleteDocUnit, uploadTestfile } from "./e2e-utils"

test("copy-paste from side panel", async ({ page, browserName }) => {
  if (browserName !== "chromium") return
  // upload file
  const documentNumber = await generateDocUnit(page)
  await uploadTestfile(page, "some-formatting.docx")
  await page.waitForSelector("text=some-formatting.docx")
  await page.waitForSelector("text=Headline")

  // get html content from sidepanel
  await page.locator("a >> text=Rubriken").click()
  await page.locator("[aria-label='Originaldokument öffnen']").click()
  await expect(page.locator("text=Dokument wird geladen")).not.toBeVisible()
  const originalFile = page.locator('div[element-id="odoc"] .ProseMirror')
  const originalFileContent = await originalFile.innerHTML()

  // copy from sidepanel to clipboard
  const modifier = (await page.evaluate(() => navigator.platform))
    .toLowerCase()
    .includes("mac")
    ? "Meta"
    : "Control"
  await originalFile.evaluate((element) => {
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

  // paste from clipboard into input field "Entscheidungsgründe"
  const inputField = page.locator(
    '[aria-label="Entscheidungsgründe Editor Feld"] div.ProseMirror'
  )
  await inputField.click()
  await page.keyboard.press(`${modifier}+KeyV`)

  // save changes and refresh page
  await page
    .locator("[aria-label='Kurz- und Langtexte Speichern Button']")
    .click()
  await page.locator("a >> text=Dokumente").click()
  await page.locator("a >> text=Rubriken").click()
  await expect(page.locator("text=Entscheidungsgründe")).toBeVisible()

  // truncate and compare content of updated input field with content of side panel
  const updatedContent = await inputField.innerHTML()
  expect(
    updatedContent.replace(
      '<p><span style="font-size: 10px">',
      '<p style="font-size: 10px"><span style="font-size: 10px">'
    )
  ).toBe(originalFileContent)
  await deleteDocUnit(page, documentNumber)
})
