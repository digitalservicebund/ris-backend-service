import { expect } from "@playwright/test"
import { navigateToCategories } from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("test the different layout options", () => {
  test("ensure default layout", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await expect(
      page.locator("[aria-label='Navigation schließen']")
    ).toBeVisible()
    await expect(
      page.locator("[aria-label='Originaldokument öffnen']")
    ).toBeVisible()
    await expect(
      page.locator("text=Es wurde noch kein Originaldokument hochgeladen")
    ).toBeHidden()
  })

  test("open and close original document panel without attached files", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await page.locator("[aria-label='Originaldokument öffnen']").click()
    await expect(
      page.locator("text=Es wurde noch kein Originaldokument hochgeladen")
    ).toBeVisible()
    await expect(page).toHaveURL(/showDocPanel=true/)
  })

  test("close and open navigation sidebar", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await page.locator("[aria-label='Navigation schließen']").click()
    await expect(page).toHaveURL(/showNavBar=false/)
    await expect(page.locator("aside", { hasText: "Zurück" })).toBeHidden()

    await page.locator("[aria-label='Navigation öffnen']").click()
    await expect(page).toHaveURL(/showNavBar=true/)
    await expect(page.locator("aside", { hasText: "Zurück" })).toBeVisible()
  })

  test("persist toggle queries for new pages", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await page.locator("[aria-label='Originaldokument öffnen']").click()

    await page.locator("[aria-label='Navigation schließen']").click()
    await expect(page).toHaveURL(/showNavBar=false/)

    await page.locator("a >> text=Zum Upload").click()
    await expect(page).toHaveURL(/showDocPanel=true/)
    await expect(page).toHaveURL(/showNavBar=false/)
  })
})
