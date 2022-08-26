import { expect } from "@playwright/test"
import { navigateToCategories, isInViewport } from "./e2e-utils"
import { testWithDocUnit as test } from "./fixtures"

test.describe("test the scrolling behavior with hashes", () => {
  test("scroll to container with hash on same route", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const coreDataHeadline = "#coreData h2"
    const textsHeadline = "#texts h2"
    const textsNavItem = await page.locator(
      `a[href*="/jurisdiction/docunit/${documentNumber}/categories#texts"]`
    )
    await expect(await isInViewport(page, coreDataHeadline)).toBeTruthy()
    await expect(await isInViewport(page, textsHeadline)).not.toBeTruthy()

    await textsNavItem.click()

    // wait for scrolling transition
    await new Promise((resolve) => setTimeout(resolve, 1000))
    await expect(await isInViewport(page, coreDataHeadline)).not.toBeTruthy()
    await expect(await isInViewport(page, textsHeadline)).toBeTruthy()
  })

  test("scroll to container with hash from different route", async ({
    page,
    documentNumber,
  }) => {
    await page.goto("/")
    await page
      .locator(`a[href*="/jurisdiction/docunit/${documentNumber}/files"]`)
      .click()

    const coreDataHeadline = "#coreData h2"
    const textsHeadline = "#texts h2"
    const coreDataNavItem = await page.locator(
      `a[href*="/jurisdiction/docunit/${documentNumber}/categories#coreData"]`
    )
    const textsNavItem = await page.locator(
      `a[href*="/jurisdiction/docunit/${documentNumber}/categories#texts"]`
    )

    await textsNavItem.click()

    // wait for scrolling transition
    await new Promise((resolve) => setTimeout(resolve, 1000))
    await expect(await isInViewport(page, coreDataHeadline)).not.toBeTruthy()
    await expect(await isInViewport(page, textsHeadline)).toBeTruthy()

    await coreDataNavItem.click()

    // wait for scrolling transition
    await new Promise((resolve) => setTimeout(resolve, 1000))
    await expect(await isInViewport(page, coreDataHeadline)).toBeTruthy()
    await expect(await isInViewport(page, textsHeadline)).not.toBeTruthy()
  })
})
