import { test, expect } from "@playwright/test"
import { generateDocUnit, navigateToCategories } from "./e2e-utils"

test.describe("test the different layout options", () => {
  test.beforeEach(async ({ page }) => {
    const documentNumber = await generateDocUnit(page)
    await navigateToCategories(page, documentNumber)
  })

  test("ensure default layout", async ({ page }) => {
    await expect(
      page.locator("[aria-label='Navigation schließen']")
    ).toBeVisible()
    await expect(
      page.locator("[aria-label='Originaldokument öffnen']")
    ).toBeVisible()
    await expect(
      page.locator("text=Es wurde noch kein Originaldokument hochgeladen")
    ).not.toBeVisible()
  })

  test("open and close original document panel without attached files", async ({
    page,
  }) => {
    await page.locator("[aria-label='Originaldokument öffnen']").click()
    await expect(
      page.locator("text=Es wurde noch kein Originaldokument hochgeladen")
    ).toBeVisible()
    await expect(page).toHaveURL(/showDocPanel=true/)
  })

  test("close and open navigation sidebar", async ({ page }) => {
    await page.locator("[aria-label='Navigation schließen']").click()
    await expect(page).toHaveURL(/showNavBar=false/)
    await expect(page.locator("text=Bearbeitungsstand")).not.toBeVisible()

    await page.locator("[aria-label='Navigation öffnen']").click()
    await expect(page).toHaveURL(/showNavBar=true/)
    await expect(page.locator("text=Bearbeitungsstand")).toBeVisible()
  })

  test("persist toggle queries for new pages", async ({ page }) => {
    await page.locator("[aria-label='Originaldokument öffnen']").click()
    await expect(page).toHaveURL(/showDocPanel=true/)

    await page.locator("[aria-label='Navigation schließen']").click()
    await expect(page).toHaveURL(/showNavBar=false/)

    await page.locator("a >> text=Zum Upload").click()
    await expect(page).toHaveURL(/showDocPanel=true/)
    await expect(page).toHaveURL(/showNavBar=false/)
  })
})
