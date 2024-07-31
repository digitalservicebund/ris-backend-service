import { expect } from "@playwright/test"
import { navigateToCategories } from "../../e2e-utils"
import { caselawTest as test } from "../../fixtures"

test.describe("short and long texts", () => {
  test("text editor fields should have predefined height", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    // small
    const smallEditor = page.locator("[data-testid='Titelzeile']")
    const smallEditorHeight = await smallEditor.evaluate((element) =>
      window.getComputedStyle(element).getPropertyValue("height"),
    )
    expect(parseInt(smallEditorHeight)).toBeGreaterThanOrEqual(60)

    //medium
    const mediumEditor = page.locator("[data-testid='Leitsatz']")
    const mediumEditorHeight = await mediumEditor.evaluate((element) =>
      window.getComputedStyle(element).getPropertyValue("height"),
    )
    expect(parseInt(mediumEditorHeight)).toBeGreaterThanOrEqual(120)

    //large
    const largeEditor = page.locator("[data-testid='Gründe']")
    const largeEditorHeight = await largeEditor.evaluate((element) =>
      window.getComputedStyle(element).getPropertyValue("height"),
    )
    expect(parseInt(largeEditorHeight)).toBeGreaterThanOrEqual(320)
  })
})

test("toggle invisible characters", async ({ page, documentNumber }) => {
  await navigateToCategories(page, documentNumber)

  const guidingPrincipleInput = page.locator("[data-testid='Leitsatz']")
  await guidingPrincipleInput.click()
  await page.keyboard.type(`this is a test paragraph`)

  await expect(
    guidingPrincipleInput.locator("[class='ProseMirror-trailingBreak']"),
  ).toHaveCount(1)
  await page
    .locator(`[aria-label='invisible-characters']:not([disabled])`)
    .click()
  await expect(
    guidingPrincipleInput.locator("[class='ProseMirror-trailingBreak']"),
  ).toHaveCount(0)
})

test("text editor keyboard navigation", async ({ page, documentNumber }) => {
  await navigateToCategories(page, documentNumber)

  const guidingPrincipleInput = page.locator(
    "[data-testid='Leitsatz'] > div.tiptap",
  )
  await guidingPrincipleInput.click()
  await expect(guidingPrincipleInput).toBeFocused()

  // Navigate to toolbar -> first button is focused
  await page.keyboard.press("Shift+Tab")
  const firstButton = page.locator(`[aria-label='fullview']:not([disabled])`)
  await expect(firstButton).toBeFocused()

  // Navigate to bold button with arrow keys
  await page.keyboard.press("ArrowRight")
  await page.keyboard.press("ArrowRight")
  await page.keyboard.press("ArrowRight")
  await page.keyboard.press("ArrowLeft")
  const boldButton = page.locator(`[aria-label='bold']:not([disabled])`)
  await expect(boldButton).toBeFocused()

  // Pressing enter moves focus to the editor
  await page.keyboard.press("Enter")
  await expect(guidingPrincipleInput).toBeFocused()

  // Tiptap editor needs to finish internal focus magic
  // eslint-disable-next-line playwright/no-wait-for-timeout
  await page.waitForTimeout(100)

  // Tabbing back into the toolbar sets focus to last active button
  await page.keyboard.press("Shift+Tab")
  await expect(boldButton).toBeFocused()

  // Move to alignment submenu and open it with Enter
  await page.keyboard.press("ArrowRight")
  await page.keyboard.press("ArrowRight")
  await page.keyboard.press("ArrowRight")
  await page.keyboard.press("ArrowRight")
  await page.keyboard.press("ArrowRight")
  await page.keyboard.press("ArrowRight")
  await page.keyboard.press("Enter")

  // Navigate to submenu button
  await page.keyboard.press("ArrowRight")
  const leftButton = page.locator(`[aria-label='left']:not([disabled])`)
  await expect(leftButton).toBeFocused()

  // Close submenu with ESC
  await page.keyboard.press("Escape")
  await expect(leftButton).toBeHidden()
})
