import { expect, test } from "@playwright/test"
import { injectAxe, checkA11y } from "axe-playwright"

test("test start page (/jurisdiction)", async ({ page }) => {
  await page.goto("/")
  await expect(page.locator("text=Dok.-Nummer")).toBeVisible()
  await injectAxe(page)
  await checkA11y(page, null, { detailedReport: true })
})
