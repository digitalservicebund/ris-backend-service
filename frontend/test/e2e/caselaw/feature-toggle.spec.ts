import { expect, test } from "@playwright/test"

test.describe("test the feature toggle", () => {
  test("enabled feature toggle", async ({ page }) => {
    await page.goto("/")
    const locator = page.getByText("Rechtsinformationen")
    const color = await locator.evaluate((e) => getComputedStyle(e).color)
    expect(color).toBe("rgb(0, 128, 0)")
  })
})
