import { test, expect } from "@playwright/test"
import { authenticate, generateDocUnit, deleteDocUnit } from "./e2e-utils"

test.describe("create a doc unit and delete it again", () => {
  test.beforeAll(async ({ browser }) => {
    authenticate(browser)
  })

  test("generate doc unit", async ({ page }) => {
    await generateDocUnit(page)
  })

  test("delete doc unit", async ({ page }) => {
    const documentNumber = await generateDocUnit(page)
    await page.goto("/")

    await expect(
      page.locator(`a[href*="/jurisdiction/docunit/${documentNumber}/files"]`)
    ).toBeVisible()

    await deleteDocUnit(page, documentNumber)
    await page.goto("/")
    await expect(
      page.locator(`a[href*="/jurisdiction/docunit/${documentNumber}/files"]`)
    ).not.toBeVisible()
  })
})
