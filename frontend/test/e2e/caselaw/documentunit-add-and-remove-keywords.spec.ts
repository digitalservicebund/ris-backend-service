import { expect } from "@playwright/test"
import { navigateToCategories } from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("Add and remove keywords to content related indexing", () => {
  test("rendering", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await expect(
      page.getByRole("heading", { name: "Schlagwörter" })
    ).toBeVisible()
  })

  test("add keywords", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Schlagwörter']").fill("one")
    await page.keyboard.press("Enter")

    await page.locator("[aria-label='Schlagwörter']").fill("two")
    await page.keyboard.press("Enter")

    await expect(page.locator("text=one").first()).toBeVisible()
    await expect(page.locator("text=two").first()).toBeVisible()

    await page.reload()

    await expect(page.locator("text=two").first()).toBeVisible()
    await expect(page.locator("text=one").first()).toBeVisible()
  })

  test("add same keyword not working", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Schlagwörter']").fill("one")
    await page.keyboard.press("Enter")

    await page.locator("[aria-label='Schlagwörter']").fill("one")
    await page.keyboard.press("Enter")

    await expect(
      page.locator("text=Schlagwort bereits vergeben").first()
    ).toBeVisible()
  })

  test("delete keywords", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Schlagwörter']").fill("one")
    await page.keyboard.press("Enter")

    await page.locator("[aria-label='Schlagwörter']").fill("two")
    await page.keyboard.press("Enter")

    await expect(page.locator("text=one").first()).toBeVisible()
    await expect(page.locator("text=two").first()).toBeVisible()

    await page.keyboard.press("Backspace")

    await page.reload()

    await expect(page.locator("text=two").first()).toBeHidden()
  })
})
