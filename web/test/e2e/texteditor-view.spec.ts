import { expect } from "@playwright/test"
import { navigateToCategories } from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("Text editor reponsive view", async () => {
  test.beforeEach(async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    const editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )
    await editorField.click()
  })
  /* Large: Button List
    + Undo
    + Redo
    + Bold
    + Italic
    + Underline
    + Strike Text
    + Heading
    + Subscript
    + Superscript
    + Left alignment
    + Right alignment
    + Center aligment
    + Justify aligment
    + Image left aligment
    + Image right aligment
    + Numbered list
    + Bullet list
    + Table chart
    + 123
    + Fullscreen
  */

  test("Text editor view in large screen", async ({ page }) => {
    page.setViewportSize({ width: 1900, height: 924 })
    const undoBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(0)
    expect(undoBtn).toBeVisible()
    const redoBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(1)
    expect(redoBtn).toBeVisible()
    const boldBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(2)
    expect(boldBtn).toBeVisible()
    const italicBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(3)
    expect(italicBtn).toBeVisible()
    const underlineBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(4)
    expect(underlineBtn).toBeVisible()
    const striketextBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(5)
    expect(striketextBtn).toBeVisible()
    const headingBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(6)
    expect(headingBtn).toBeVisible()
    const leftAlignBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(11)
    expect(leftAlignBtn).toBeVisible()
    const centerAlignBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(12)
    expect(centerAlignBtn).toBeVisible()
    const rightAlignBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(13)
    expect(rightAlignBtn).toBeVisible()
    const justifyAlignBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(14)
    expect(justifyAlignBtn).toBeVisible()
    const superScriptBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(15)
    expect(superScriptBtn).toBeVisible()
    const subScriptBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(16)
    expect(subScriptBtn).toBeVisible()
    const imgLeftAlignBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(26)
    expect(imgLeftAlignBtn).toBeVisible()
    const imgRightAlignBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(27)
    expect(imgRightAlignBtn).toBeVisible()
    const button123 = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(30)
    expect(button123).toBeVisible()
    const fullScreenBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(31)
    expect(fullScreenBtn).toBeVisible()
  })

  /* Medium: Button List
    + Undo
    + Redo
    + Bold
    + Italic
    + Underline
    + Strike Text
    + Heading
    + Subscript
    + Superscript
    + Dropdown text aligment
    + Dropdown image aligment
    + Dropdown list styles
    + Table chart
    + 123
    + Fullscreen
  */
  test("Text editor view in medium screen", async ({ page }) => {
    page.setViewportSize({ width: 1600, height: 720 })
    const undoBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(0)
    expect(undoBtn).toBeVisible()
    const redoBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(1)
    expect(redoBtn).toBeVisible()
    const boldBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(2)
    expect(boldBtn).toBeVisible()
    const italicBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(3)
    expect(italicBtn).toBeVisible()
    const underlineBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(4)
    expect(underlineBtn).toBeVisible()
    const striketextBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(5)
    expect(striketextBtn).toBeVisible()
    const headingBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(6)
    expect(headingBtn).toBeVisible()
    const superScriptBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(15)
    expect(superScriptBtn).toBeVisible()
    const subScriptBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(16)
    expect(subScriptBtn).toBeVisible()
    const tableChartButton = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(28)
    expect(tableChartButton).toBeVisible()
    const button123 = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(30)
    expect(button123).toBeVisible()
    const fullScreenBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(31)
    expect(fullScreenBtn).toBeVisible()

    // Dropdown text alignment
    const dropdownShowAlignmentButton = await page
      .locator(
        "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('format_align_leftarrow_drop_down')"
      )
      .nth(0)
    expect(dropdownShowAlignmentButton).toBeVisible()
    await dropdownShowAlignmentButton.click()
    const leftAligmentButton = await page
      .locator("text=format_align_left")
      .nth(0)
    const justifyAligmentButton = await page
      .locator("text=format_align_justify")
      .nth(0)
    const centerAligmentButton = await page
      .locator("text=format_align_center")
      .nth(0)
    const rightAligmentButton = await page
      .locator("text=format_align_right")
      .nth(0)
    expect(leftAligmentButton).toBeVisible()
    expect(rightAligmentButton).toBeVisible()
    expect(centerAligmentButton).toBeVisible()
    expect(justifyAligmentButton).toBeVisible()

    // Dropdown list styles
    const dropdownListStylesButton = await page
      .locator(
        "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('format_list_bulletedarrow_drop_down')"
      )
      .nth(0)
    expect(dropdownListStylesButton).toBeVisible()
    await dropdownListStylesButton.click()
    const numberedListBtn = await page
      .locator("text=format_list_numbered")
      .nth(0)
    const bulletListBtn = await page.locator("text=format_list_bulleted").nth(0)
    expect(numberedListBtn).toBeVisible()
    expect(bulletListBtn).toBeVisible()

    // Dropdown image aligment
    const dropdownImageAligmentButton = await page
      .locator(
        "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('vertical_splitarrow_drop_down')"
      )
      .nth(0)
    expect(dropdownImageAligmentButton).toBeVisible()
    await dropdownImageAligmentButton.click()
    const imageRightAligmentButton = await page
      .locator("text=vertical_split")
      .nth(0)
    const imageLeftAligmentButton = await page
      .locator("text=vertical_split")
      .nth(1)
    expect(imageRightAligmentButton).toBeVisible()
    expect(imageLeftAligmentButton).toBeVisible()
  })

  /* Small: First Line: Button List
    + Undo
    + Redo
    + Bold
    + Italic
    + Underline
    + Strike Text
    + Heading
    + Showmore
    + 123
    + Fullscreen
    Second Line: Button List
    + Subscript
    + Superscript
    + Left alignment
    + Right alignment
    + Center aligment
    + Justify aligment
    + Image left aligment
    + Image right aligment
    + Numbered list
    + Bullet list
    + Table chart
  */
  test("Text editor view in small screen", async ({ page }) => {
    page.setViewportSize({ width: 1200, height: 720 })
    const undoBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(0)
    expect(undoBtn).toBeVisible()
    const redoBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(1)
    expect(redoBtn).toBeVisible()
    const boldBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(2)
    expect(boldBtn).toBeVisible()
    const italicBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(3)
    expect(italicBtn).toBeVisible()
    const underlineBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(4)
    expect(underlineBtn).toBeVisible()
    const striketextBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(5)
    expect(striketextBtn).toBeVisible()
    const headingBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(6)
    expect(headingBtn).toBeVisible()
    const button123 = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(30)
    expect(button123).toBeVisible()
    const fullScreenBtn = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(31)
    expect(fullScreenBtn).toBeVisible()

    // Click auf more button to open seconde line.
    const moreButton = await page.locator(
      "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('more_horiz')"
    )
    expect(moreButton).toBeVisible()
    await moreButton.click()
    const superScriptBtn = await page.locator("text=superscript").nth(1)
    const subScriptBtn = await page.locator("text=subscript").nth(1)
    const numberedListBtn = await page
      .locator("text=format_list_numbered")
      .nth(1)
    const bulletListBtn = await page.locator("text=format_list_bulleted").nth(2)
    const imageLeftAligmentBtn = await page
      .locator("text=vertical_split")
      .nth(3)
    const imageRightAligmentBtn = await page
      .locator("text=vertical_split")
      .nth(4)
    const tableChartBtn = await page.locator("text=table_chart").nth(1)
    expect(superScriptBtn).toBeVisible()
    expect(subScriptBtn).toBeVisible()
    expect(numberedListBtn).toBeVisible()
    expect(bulletListBtn).toBeVisible()
    expect(tableChartBtn).toBeVisible()
    expect(imageLeftAligmentBtn).toBeVisible()
    expect(imageRightAligmentBtn).toBeVisible()
  })
})
