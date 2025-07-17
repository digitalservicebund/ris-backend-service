import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { navigateToCategories } from "~/e2e/caselaw/utils/e2e-utils"

test.describe("content related indexing", () => {
  test("render 'Inhaltliche Erschließung' on the page and in the menu", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await expect(
      page.getByRole("link", { name: "Inhaltliche Erschließung" }),
    ).toBeVisible()
    await expect(
      page.getByRole("heading", { name: "Inhaltliche Erschließung" }),
    ).toBeVisible()
  })

  test("click on 'Inhaltliche Erschließung' in the menu should bring the area into viewport", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.getByRole("link", { name: "Inhaltliche Erschließung" }).click()

    await expect(
      page.getByRole("heading", { name: "Inhaltliche Erschließung" }),
    ).toBeInViewport()
  })
})
