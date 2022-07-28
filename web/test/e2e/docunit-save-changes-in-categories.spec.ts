import { expect } from "@playwright/test"
import { navigateToCategories, pageReload } from "./e2e-utils"
import { testWithDocUnit as test } from "./fixtures"

test.describe("save changes in core data and texts and verify it persists", () => {
  test("test core data change", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Aktenzeichen']").fill("abc")

    page.once("dialog", async (dialog) => {
      expect(dialog.message()).toBe("Dokumentationseinheit wurde gespeichert")
      await dialog.accept()
    })
    await page.locator("[aria-label='Stammdaten Speichern Button']").click()
    await page.waitForTimeout(500)

    await pageReload(page) // to make sure the data needs to come fresh from the server

    // 1. verify that the change is visible in the docunit list on /jurisdiction
    const aktenzeichenColumn = page
      .locator("tr", {
        has: page.locator(`a >> text=${documentNumber}`),
      })
      .locator("td >> text=abc")

    await expect(aktenzeichenColumn).toBeVisible()
    expect(await aktenzeichenColumn.innerText()).toBe("abc")

    // 2. verify that the change is visible in Rubriken
    await navigateToCategories(page, documentNumber)
    expect(await page.inputValue("[aria-label='Aktenzeichen']")).toBe("abc")
  })

  test("test bold text input", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    let editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )

    await editorField.click()

    const boldButton = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(2)
    await boldButton.click()

    await editorField.type("this is bold text")
    await page
      .locator("[aria-label='Kurz- und Langtexte Speichern Button']")
      .click()
    await page.waitForTimeout(500)

    await pageReload(page)

    // verify that the change is visible in the Kurz- und Langtexte field
    await navigateToCategories(page, documentNumber)
    editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )
    expect(await editorField.innerHTML()).toBe(
      "<p><strong>this is bold text</strong></p>"
    )
  })

  test("test italic test input", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    let editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )

    await editorField.click()

    const italicButton = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(3)
    await italicButton.click()

    await editorField.type("this is italic text")
    await page
      .locator("[aria-label='Kurz- und Langtexte Speichern Button']")
      .click()
    await page.waitForTimeout(500)

    await pageReload(page)

    // verify that the change is visible in the Kurz- und Langtexte field
    await navigateToCategories(page, documentNumber)
    editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )
    expect(await editorField.innerHTML()).toBe(
      "<p><em>this is italic text</em></p>"
    )
  })

  test("test underlined test input", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    let editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )

    await editorField.click()

    const underlineButton = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(4)
    await underlineButton.click()

    await editorField.type("this is underlined text")
    await page
      .locator("[aria-label='Kurz- und Langtexte Speichern Button']")
      .click()
    await page.waitForTimeout(500)

    await pageReload(page)

    // verify that the change is visible in the Kurz- und Langtexte field
    await navigateToCategories(page, documentNumber)
    editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )
    expect(await editorField.innerHTML()).toBe(
      "<p><u>this is underlined text</u></p>"
    )
  })

  test("test strike test input", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    let editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )

    await editorField.click()

    const strikeButton = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(5)
    await strikeButton.click()

    await editorField.type("this is striked text")
    await page
      .locator("[aria-label='Kurz- und Langtexte Speichern Button']")
      .click()
    await page.waitForTimeout(500)

    await pageReload(page)

    // verify that the change is visible in the Kurz- und Langtexte field
    await navigateToCategories(page, documentNumber)
    editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )
    expect(await editorField.innerHTML()).toBe(
      "<p><s>this is striked text</s></p>"
    )
  })

  test("test superscript test input", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    let editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )

    await editorField.click()

    const moreButton = await page.locator(
      "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('more_horiz')"
    )
    await moreButton.click()
    // There are 2 icons of superscript button
    // The frist one shows only in screen-xl
    // The second one shows when more button clicked
    const superScriptButton = await page.locator("text=superscript").nth(1)
    await superScriptButton.click()

    await editorField.type("this is superscript text")
    await page
      .locator("[aria-label='Kurz- und Langtexte Speichern Button']")
      .click()
    await page.waitForTimeout(500)

    await pageReload(page)

    // verify that the change is visible in the Kurz- und Langtexte field
    await navigateToCategories(page, documentNumber)
    editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )
    expect(await editorField.innerHTML()).toBe(
      "<p><sup>this is superscript text</sup></p>"
    )
  })

  test("test subscript test input", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    let editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )

    await editorField.click()

    const moreButton = await page.locator(
      "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('more_horiz')"
    )
    await moreButton.click()
    // There are 2 icons of subscript button
    // The frist one shows only in screen-xl
    // The second one shows when more button clicked
    const subScriptButton = await page.locator("text=subscript").nth(1)
    await subScriptButton.click()

    await editorField.type("this is subscript text")
    await page
      .locator("[aria-label='Kurz- und Langtexte Speichern Button']")
      .click()
    await page.waitForTimeout(500)

    await pageReload(page)

    // verify that the change is visible in the Kurz- und Langtexte field
    await navigateToCategories(page, documentNumber)
    editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )
    expect(await editorField.innerHTML()).toBe(
      "<p><sub>this is subscript text</sub></p>"
    )
  })
})
