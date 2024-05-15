import { expect } from "@playwright/test"
import { caselawTest as test } from "../fixtures"
import {
  navigateToCategories,
  navigateToFiles,
  navigateToPreview,
  navigateToPublication,
} from "~/e2e/caselaw/e2e-utils"

test.describe("catagories page title", () => {
  test("document number is displayed in tab", async ({
    page,
    documentNumber,
  }) => {
    const expectedPageTitle =
      documentNumber + " · NeuRIS Rechtsinformationssystem"

    await navigateToCategories(page, documentNumber)
    expect(await page.title()).toBe(expectedPageTitle)

    await navigateToPublication(page, documentNumber)
    expect(await page.title()).toBe(expectedPageTitle)

    await navigateToFiles(page, documentNumber)
    expect(await page.title()).toBe(expectedPageTitle)

    await navigateToPreview(page, documentNumber)
    expect(await page.title()).toBe(expectedPageTitle)
  })
})
