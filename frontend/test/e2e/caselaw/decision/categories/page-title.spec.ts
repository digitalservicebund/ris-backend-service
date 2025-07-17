import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToCategories,
  navigateToAttachments,
  navigateToPreview,
  navigateToHandover,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe("catagories page title", () => {
  test("document number is displayed in tab", async ({
    page,
    documentNumber,
  }) => {
    const expectedPageTitle =
      documentNumber + " · NeuRIS Rechtsinformationssystem"

    await navigateToCategories(page, documentNumber)
    expect(await page.title()).toBe(expectedPageTitle)

    await navigateToHandover(page, documentNumber)
    expect(await page.title()).toBe(expectedPageTitle)

    await navigateToAttachments(page, documentNumber)
    expect(await page.title()).toBe(expectedPageTitle)

    await navigateToPreview(page, documentNumber)
    expect(await page.title()).toBe(expectedPageTitle)
  })
})
