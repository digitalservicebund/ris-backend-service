import { expect } from "@playwright/test"
import { Page } from "playwright"

export const navigateToCategories = async (
  page: Page,
  documentNumber: string
) => {
  await page.goto("/")
  await page
    .locator(`a[href*="/jurisdiction/docunit/${documentNumber}/files"]`)
    .click()

  await page
    .locator(
      `a[href*="/jurisdiction/docunit/${documentNumber}/categories"] >> text=Rubriken`
    )
    .click()
}

export const uploadTestfile = async (page: Page, filename: string) => {
  const [fileChooser] = await Promise.all([
    page.waitForEvent("filechooser"),
    page.locator("text=Festplatte durchsuchen").click(),
  ])
  await fileChooser.setFiles("./test/e2e/testfiles/" + filename)
  expect(page.locator("text=Upload läuft")).not.toBeVisible()
  expect(page.locator("text=Dokument wird geladen.")).not.toBeVisible()
}
