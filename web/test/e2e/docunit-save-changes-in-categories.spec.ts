import { expect } from "@playwright/test"
import { navigateToCategories } from "./e2e-utils"
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
    await page.goto("/")

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

  test("test bold text input", async ({ page, editorField }) => {
    await editorField.click()

    const boldButton = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(2)
    await boldButton.click()
    await editorField.type("this is bold text")

    expect(await editorField.innerHTML()).toBe(
      "<p>this is text<strong>this is bold text</strong></p>"
    )
  })

  test("test italic test input", async ({ page, editorField }) => {
    await editorField.click()

    const italicButton = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(3)
    await italicButton.click()
    await editorField.type("this is italic text")

    expect(await editorField.innerHTML()).toBe(
      "<p>this is text<em>this is italic text</em></p>"
    )
  })

  test("test underlined test input", async ({ page, editorField }) => {
    await editorField.click()

    const underlineButton = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(4)
    await underlineButton.click()
    await editorField.type("this is underlined text")

    expect(await editorField.innerHTML()).toBe(
      "<p>this is text<u>this is underlined text</u></p>"
    )
  })

  test("test strike test input", async ({ page, editorField }) => {
    await editorField.click()

    const strikeButton = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(5)
    await strikeButton.click()
    await editorField.type("this is striked text")
    expect(await editorField.innerHTML()).toBe(
      "<p>this is text<s>this is striked text</s></p>"
    )
  })

  test("test superscript test input", async ({ page, editorField }) => {
    await editorField.click()

    const moreButton = await page.locator(
      "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('more_horiz')"
    )
    await moreButton.click()
    const superScriptButton = await page.locator("text=superscript").nth(1)
    await superScriptButton.click()

    await editorField.type("this is superscript text")
    expect(await editorField.innerHTML()).toBe(
      "<p>this is text<sup>this is superscript text</sup></p>"
    )
  })

  test("test subscript test input", async ({ page, editorField }) => {
    await editorField.click()

    const moreButton = await page.locator(
      "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('more_horiz')"
    )
    await moreButton.click()
    const subScriptButton = await page.locator("text=subscript").nth(1)
    await subScriptButton.click()
    await editorField.type("this is subscript text")

    expect(await editorField.innerHTML()).toBe(
      "<p>this is text<sub>this is subscript text</sub></p>"
    )
  })
  test("test right alignment text input", async ({ page, editorField }) => {
    const dropdownShowAlignmentButton = await page
      .locator(
        "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('format_align_leftarrow_drop_down')"
      )
      .nth(0)
    await dropdownShowAlignmentButton.click()
    const rightAligmentButton = await page
      .locator("text=format_align_right")
      .nth(0)
    await rightAligmentButton.click()
    expect(await editorField.innerHTML()).toBe(
      '<p style="text-align: right">this is text</p>'
    )
  })

  test("test center alignment text input", async ({ page, editorField }) => {
    const dropdownShowAlignmentButton = await page
      .locator(
        "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('format_align_leftarrow_drop_down')"
      )
      .nth(0)
    await dropdownShowAlignmentButton.click()
    const centerAligmentButton = await page
      .locator("text=format_align_center")
      .nth(0)
    await centerAligmentButton.click()

    expect(await editorField.innerHTML()).toBe(
      '<p style="text-align: center">this is text</p>'
    )
  })
  test("test left alignment text input", async ({ page, editorField }) => {
    const dropdownShowAlignmentButton = await page
      .locator(
        "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('format_align_leftarrow_drop_down')"
      )
      .nth(0)
    await dropdownShowAlignmentButton.click()
    const leftAligmentButton = await page
      .locator("text=format_align_left")
      .nth(0)
    await leftAligmentButton.click()

    expect(await editorField.innerHTML()).toBe("<p>this is text</p>")
  })
  test("test justify alignment text input", async ({ page, editorField }) => {
    const dropdownShowAlignmentButton = await page
      .locator(
        "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('format_align_leftarrow_drop_down')"
      )
      .nth(0)
    await dropdownShowAlignmentButton.click()
    const justifyAligmentButton = await page
      .locator("text=format_align_justify")
      .nth(0)
    await justifyAligmentButton.click()

    expect(await editorField.innerHTML()).toBe(
      '<p style="text-align: justify">this is text</p>'
    )
  })
})
