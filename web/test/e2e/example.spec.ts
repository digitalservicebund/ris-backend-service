import { test, expect } from "@playwright/test"

test.describe("basic example test", () => {
  test("basic test", async ({ page }) => {
    await page.goto("http://localhost:4173/")
    await expect(page.locator("text=NeuRIS")).toBeVisible()
  })
})
