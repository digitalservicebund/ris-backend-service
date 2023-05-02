import { expect } from "@playwright/test"
import { generateString } from "../../../../test-helper/dataGenerators"
import {
  navigateToCategories,
  waitForSaving,
  waitForInputValue,
} from "../../e2e-utils"
import { testWithDocumentUnit as test } from "../../fixtures"

test.describe("keywords", () => {
  test("rendering", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await expect(
      page.getByRole("heading", { name: "Schlagwörter" })
    ).toBeVisible()
  })

  test("add keywords", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    const firstKeyword = generateString()
    const secondKeyword = generateString()

    await page.locator("[aria-label='Schlagwörter']").fill(firstKeyword)
    await page.keyboard.press("Enter")
    await expect(page.getByText(firstKeyword)).toBeVisible()

    await page.locator("[aria-label='Schlagwörter']").fill(secondKeyword)
    await page.keyboard.press("Enter")
    await expect(page.getByText(secondKeyword)).toBeVisible()
  })

  test("add keywords with special character", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const keywordWithSpecialCharacters = generateString() + "%&/"

    await page
      .locator("[aria-label='Schlagwörter']")
      .fill(keywordWithSpecialCharacters)
    await page.keyboard.press("Enter")
    await expect(page.getByText(keywordWithSpecialCharacters)).toBeVisible()
  })

  test("add same keyword not working", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    const keyword = generateString()

    // first
    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Schlagwörter']").fill(keyword)
        await waitForInputValue(page, "[aria-label='Schlagwörter']", keyword)
        await page.keyboard.press("Enter")
      },
      page,
      { clickSaveButton: true }
    )
    await expect(page.getByText(keyword)).toBeVisible()

    await page.locator("[aria-label='Schlagwörter']").fill(keyword)
    await waitForInputValue(page, "[aria-label='Schlagwörter']", keyword)
    await page.keyboard.press("Enter")

    await expect(page.getByText(/Schlagwort bereits vergeben/)).toBeVisible()
  })

  test("delete keywords", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    const firstKeyword = generateString()
    const secondKeyword = generateString()

    await page.locator("[aria-label='Schlagwörter']").fill(firstKeyword)
    await page.keyboard.press("Enter")
    await expect(page.getByText(firstKeyword)).toBeVisible()

    await page.locator("[aria-label='Schlagwörter']").fill(secondKeyword)
    await page.keyboard.press("Enter")
    await expect(page.getByText(secondKeyword)).toBeVisible()

    await page
      .locator("[aria-label='chip']", { hasText: firstKeyword })
      .getByLabel("Löschen")
      .click()

    await expect(await page.getByText(secondKeyword)).toBeVisible()
    await expect(await page.getByText(firstKeyword)).toBeHidden()
  })
})
