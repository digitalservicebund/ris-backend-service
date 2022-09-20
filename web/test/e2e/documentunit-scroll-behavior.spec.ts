import { expect } from "@playwright/test"
import { navigateToCategories, navigateToFiles } from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("test the scrolling behavior with hashes", () => {
  test("scroll to container with hash on same route", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const coreDataHeadline = "#coreData h1"
    const textsHeadline = "#texts h1"
    const textsNavItem = page.locator(
      `a[href*="/jurisdiction/documentunit/${documentNumber}/categories#texts"]`
    )

    await expect(page).toHaveInsideViewport(coreDataHeadline)
    await expect(page).toHaveOutsideViewport(textsHeadline)

    await textsNavItem.click()

    await expect(page).toHaveOutsideViewport(coreDataHeadline)
    await expect(page).toHaveInsideViewport(textsHeadline)
  })

  test("scroll to container with hash from different route", async ({
    page,
    documentNumber,
  }) => {
    await navigateToFiles(page, documentNumber)

    const coreDataHeadline = "#coreData h1"
    const textsHeadline = "#texts h1"
    const coreDataNavItem = page.locator(
      `a[href*="/jurisdiction/documentunit/${documentNumber}/categories#coreData"]`
    )
    const textsNavItem = page.locator(
      `a[href*="/jurisdiction/documentunit/${documentNumber}/categories#texts"]`
    )

    await textsNavItem.click()
    await expect(page).toHaveOutsideViewport(coreDataHeadline)
    await expect(page).toHaveInsideViewport(textsHeadline)

    await coreDataNavItem.click()
    await expect(page).toHaveOutsideViewport(textsHeadline)
    await expect(page).toHaveInsideViewport(coreDataHeadline)
  })
})
