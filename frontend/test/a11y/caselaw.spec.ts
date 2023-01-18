import { expect } from "@playwright/test"
import { checkA11y, injectAxe } from "axe-playwright"
import {
  navigateToCategories,
  navigateToFiles,
  navigateToPublication,
} from "../e2e/caselaw/e2e-utils"
import { testWithDocumentUnit as test } from "../e2e/caselaw/fixtures"

test.describe("a11y of start page (/caselaw)", () => {
  test("documentUnit list", async ({ page }) => {
    await page.goto("/")
    await expect(page.locator("text=Neue Dokumentationseinheit")).toBeVisible()
    await injectAxe(page)
    await checkA11y(page)
  })
})

test.describe("a11y of categories page (/caselaw/documentunit/{documentNumber}/categories)", () => {
  test("categories", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await injectAxe(page)
    await checkA11y(page)
  })
})

test.describe("a11y of document page (/caselaw/documentunit/{documentNumber}/files)", () => {
  test("document", async ({ page, documentNumber }) => {
    await navigateToFiles(page, documentNumber)
    await injectAxe(page)
    await checkA11y(page)
  })
})

test.describe("a11y of publication page (/caselaw/documentunit/{documentNumber}/publication)", () => {
  test("publication", async ({ page, documentNumber }) => {
    await navigateToPublication(page, documentNumber)
    await injectAxe(page)
    await checkA11y(page)
  })
})
