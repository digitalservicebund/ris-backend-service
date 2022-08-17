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

export const isInViewport = async (page: Page, selector: string) => {
  await page.locator(selector).boundingBox() // it contains x, y, width, and height only
  const isVisible = await page.evaluate((selector) => {
    let isVisible = false
    const element = document.querySelector(selector)
    if (element) {
      const rect = element.getBoundingClientRect()
      if (rect.top >= 0 && rect.left >= 0) {
        const vw = Math.max(
          document.documentElement.clientWidth || 0,
          window.innerWidth || 0
        )
        const vh = Math.max(
          document.documentElement.clientHeight || 0,
          window.innerHeight || 0
        )
        if (rect.right <= vw && rect.bottom <= vh) {
          isVisible = true
        }
      }
    }
    return isVisible
  }, selector)
  return isVisible
}
