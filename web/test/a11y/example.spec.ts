import { test } from "@playwright/test"
// import { injectAxe, checkA11y } from "axe-playwright"

test.describe("basic example a11y test", () => {
  test("test start page", async ({ page }) => {
    // homepage forwards to /rechtsprechung TODO
    await page.goto("http://localhost:4173/")
    // await injectAxe(page)
    // await checkA11y(page)
  })
})
