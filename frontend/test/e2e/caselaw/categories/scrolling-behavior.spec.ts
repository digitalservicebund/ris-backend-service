import { expect } from "@playwright/test"
import { navigateToCategories } from "../e2e-utils"
import { caselawTest as test } from "../fixtures"

test.describe("scrolling behavior with hashes", () => {
  test("scroll to container with hash on same route", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const coreDataHeadline = "#coreData h1"
    const textsHeadline = "#texts h1"
    const textsNavItem = page.locator(
      `a[href*="/caselaw/documentunit/${documentNumber}/categories#texts"]`
    )

    await expect(page.locator(coreDataHeadline)).toBeInViewport()
    await expect(page.locator(textsHeadline)).not.toBeInViewport()

    await textsNavItem.click()

    await expect(page.locator(coreDataHeadline)).not.toBeInViewport()
    await expect(page.locator(textsHeadline)).toBeInViewport()
  })

  test("scroll to container with hash from different route", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const coreDataHeadline = "#coreData h1"
    const textsHeadline = "#texts h1"
    const coreDataNavItem = page.locator(
      `a[href*="/caselaw/documentunit/${documentNumber}/categories#coreData"]`
    )
    const textsNavItem = page.locator(
      `a[href*="/caselaw/documentunit/${documentNumber}/categories#texts"]`
    )

    await textsNavItem.click()
    await expect(page.locator(coreDataHeadline)).not.toBeInViewport()
    await expect(page.locator(textsHeadline)).toBeInViewport()

    await coreDataNavItem.click()
    await expect(page.locator(textsHeadline)).not.toBeInViewport()
    await expect(page.locator(coreDataHeadline)).toBeInViewport()
  })
})
