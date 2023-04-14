import { expect } from "@playwright/test"
import { generateString } from "../../test-helper/dataGenerators"
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

    const firstKeyword = generateString()
    const secondKeyword = generateString()

    await page.locator("[aria-label='Schlagwörter']").fill(firstKeyword)
    await page.keyboard.press("Enter")
    await expect(page.getByText(firstKeyword)).toBeVisible()

    await page.locator("[aria-label='Schlagwörter']").fill(secondKeyword)
    await page.keyboard.press("Enter")
    await expect(page.getByText(secondKeyword)).toBeVisible()
  })

  // eslint-disable-next-line playwright/no-skipped-test
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

  // eslint-disable-next-line playwright/no-skipped-test
  test("add same keyword not working", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    const keyword = generateString()

    await page.locator("[aria-label='Schlagwörter']").fill(keyword)
    await page.keyboard.press("Enter")
    await expect(page.getByText(keyword)).toBeVisible()

    await page.locator("[aria-label='Schlagwörter']").fill(keyword)
    await page.keyboard.press("Enter")

    await expect(page.getByText("Schlagwort bereits vergeben")).toBeVisible()
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

    await page.keyboard.press("ArrowRight")
    await page.keyboard.press("ArrowRight")
    await page.keyboard.press("Enter")

    await expect(page.getByText(firstKeyword)).toBeVisible()
    await expect(page.getByText(secondKeyword)).toBeHidden()
  })
})
