import { expect } from "@playwright/test"
import { navigateToCategories } from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("Inhaltliche Erschließung", () => {
  test("render 'Inhaltliche Erschließung' on the page and in the menu", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await expect(
      page.getByRole("link", { name: "Inhaltliche Erschließung" })
    ).toBeVisible()
    await expect(
      page.getByRole("heading", { name: "Inhaltliche Erschließung" })
    ).toBeVisible()
  })

  test("click on 'Inhaltliche Erschließung' in the menu should bring the area into viewport", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    page.getByRole("link", { name: "Inhaltliche Erschließung" }).click()

    await expect(page).toHaveInsideViewport("#contentRelatedIndexing > h1")
  })
})
