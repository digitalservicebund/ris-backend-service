import { expect } from "@playwright/test"
import { navigateToCategories } from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("ensuring the editing experience in categories is as expected", () => {
  test("test legal effect dropdown", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await expect(page.locator("text=Keine Angabe")).toBeHidden()

    await page
      .locator("[aria-label='Rechtskraft'] + [aria-label='Dropdown öffnen']")
      .click()

    await expect(page.locator("text=Ja")).toBeVisible()
    await expect(page.locator("text=Nein")).toBeVisible()
    await expect(page.locator("text=Keine Angabe")).toBeVisible()
  })

  // TODO
  // test("test document type dropdown", async ({ page, documentNumber }) => {})
  // test("test court dropdown", async ({ page, documentNumber }) => {})
  // test("test that setting a court sets the region automatically", async ({ page, documentNumber }) => {})
})
