import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { navigateToCategories } from "~/e2e/caselaw/utils/e2e-utils"

test.describe("scrolling behavior with hashes", () => {
  test("scroll to container with hash on same route", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const textsNavItem = page.locator(
      `a[href*="/caselaw/documentunit/${documentNumber}/categories#texts"]`,
    )

    await expect(
      page.getByRole("heading", { name: "Stammdaten" }),
    ).toBeInViewport()
    await expect(
      page.getByRole("heading", { name: "Kurz- & Langtexte" }),
    ).not.toBeInViewport()

    await textsNavItem.click()

    await expect(
      page.getByRole("heading", { name: "Stammdaten" }),
    ).not.toBeInViewport()
    await expect(
      page.getByRole("heading", { name: "Kurz- & Langtexte" }),
    ).toBeInViewport()
  })

  test("scroll to container with hash from different route", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const coreDataNavItem = page.locator(
      `a[href*="/caselaw/documentunit/${documentNumber}/categories#coreData"]`,
    )
    const textsNavItem = page.locator(
      `a[href*="/caselaw/documentunit/${documentNumber}/categories#texts"]`,
    )

    await textsNavItem.click()
    await expect(
      page.getByRole("heading", { name: "Stammdaten" }),
    ).not.toBeInViewport()
    await expect(
      page.getByRole("heading", { name: "Kurz- & Langtexte" }),
    ).toBeInViewport()

    await coreDataNavItem.click()
    await expect(
      page.getByRole("heading", { name: "Stammdaten" }),
    ).toBeInViewport()
    await expect(
      page.getByRole("heading", { name: "Kurz- & Langtexte" }),
    ).not.toBeInViewport()
  })
})
