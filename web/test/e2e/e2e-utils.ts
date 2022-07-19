import { expect, test as base } from "@playwright/test"
import { Page } from "playwright"

export const generateDocUnit = async (page: Page): Promise<string> => {
  await page.goto("/")
  await page.locator("button >> text=Neue Dokumentationseinheit").click()
  await page.waitForSelector("text=Festplatte durchsuchen")

  await expect(page).toHaveURL(/\/jurisdiction\/docunit\/[A-Z0-9]{14}\/files$/)

  const regex = /jurisdiction\/docunit\/(.*)\/files/g
  const match = regex.exec(page.url())
  if (match) {
    return match[1]
  } else {
    throw new Error("Could not get DocumentNumber")
  }
}

export const deleteDocUnit = async (page: Page, documentNumber: string) => {
  await page.goto("/")
  await page
    .locator("tr", {
      hasText: documentNumber,
    })
    .locator("[aria-label='Dokumentationseinheit löschen']")
    .click()
}

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

export const pageReload = async (page: Page) => {
  await page.goto("/")
  await page.reload()
  await page.waitForTimeout(500)
  await page.goto("/")
}

export const uploadTestfile = async (page: Page, filename: string) => {
  const [fileChooser] = await Promise.all([
    page.waitForEvent("filechooser"),
    page.locator("text=Festplatte durchsuchen").click(),
  ])
  await fileChooser.setFiles("./test/e2e/testfiles/" + filename)
}

export const testWithDocUnit = base.extend<{
  page: Page
}>({
  page: async ({ page }, use) => {
    const documentNumber = await generateDocUnit(page)
    await navigateToCategories(page, documentNumber)
    await use(page)
    await deleteDocUnit(page, documentNumber)
  },
})
