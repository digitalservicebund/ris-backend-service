import { expect } from "@playwright/test"
import { navigateToCategories, navigateToFiles } from "./e2e-utils"
import { caselawTest as test } from "./fixtures"

test.describe("test the different layout options", () => {
  test("preview panel is hidden without attached files", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await expect(page.locator("#odoc-panel-element")).toBeHidden()
  })

  test("close and open navigation sidebar", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await page.getByLabel("Navigation schließen").click()
    await expect(page).toHaveURL(/showNavigationPanel=false/)
    await expect(page.locator("aside", { hasText: "Rubriken" })).toBeHidden()

    await page.getByLabel("Navigation öffnen").click()
    await expect(page).toHaveURL(/showNavigationPanel=true/)
    await expect(page.getByRole("link", { name: "Rubriken" })).toBeVisible()
  })

  test("navigation state passed to other menu item", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await page.getByLabel("Navigation schließen").click()
    await expect(page).toHaveURL(/showNavigationPanel=false/)

    await navigateToFiles(page, documentNumber)
    await expect(page.locator("aside", { hasText: "Rubriken" })).toBeHidden()
    await expect(page).toHaveURL(/showNavigationPanel=false/)
  })
})
