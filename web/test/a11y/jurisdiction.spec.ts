import { expect, test } from "@playwright/test"
import { checkA11y, injectAxe } from "axe-playwright"

test.describe("a11y of start page (/jurisdiction)", () => {
  test("docUnit list", async ({ page }) => {
    await page.goto("/")
    await expect(page.locator("text=Neue Dokumentationseinheit")).toBeVisible()
    await injectAxe(page)
    await checkA11y(page)
  })
})
