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
  await page.getByLabel("invisible-characters").click()
  await expect(
    guidingPrincipleInput.locator("[class='ProseMirror-trailingBreak']"),
  ).toHaveCount(0)
})
