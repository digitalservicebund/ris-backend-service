import { test, expect } from "@playwright/test"
import {
  generateDocUnit,
  deleteDocUnit,
  getAuthenticatedPage,
} from "./e2e-utils"

test.describe("create a doc unit and delete it again", () => {
  test("generate doc unit", async ({ browser }) => {
    const page = await getAuthenticatedPage(browser)
    const documentNumber = await generateDocUnit(page)
    await deleteDocUnit(page, documentNumber)
  })

  test("delete doc unit", async ({ browser }) => {
    const page = await getAuthenticatedPage(browser)
    const documentNumber = await generateDocUnit(page)
    await page.goto("/")

    await expect(
      page.locator(`a[href*="/jurisdiction/docunit/${documentNumber}/files"]`)
    ).toBeVisible()

    await deleteDocUnit(page, documentNumber)
    await expect(
      page.locator(`a[href*="/jurisdiction/docunit/${documentNumber}/files"]`)
    ).not.toBeVisible()
  })
})
