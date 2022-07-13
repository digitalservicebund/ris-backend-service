import { test, expect, Page } from "@playwright/test"
import { getAuthenticatedPage } from "./e2e-utils"

test.describe("generate a doc unit and delete it again", () => {
  let documentNumber: string
  let page: Page

  test("generate doc unit", async ({ browser }) => {
    page = await getAuthenticatedPage(browser)
    documentNumber = await generateDocUnit(page)
  })

  test("delete doc unit", async () => {
    await page.goto("/")
    await expect(
      page.locator(`a[href*="/rechtsprechung/${documentNumber}"]`)
    ).toBeVisible()
    await deleteDocUnit(page, documentNumber)
    await page.goto("/")
    await expect(
      page.locator(`a[href*="/rechtsprechung/${documentNumber}"]`)
    ).not.toBeVisible()
  })
})

export const generateDocUnit = async (page: Page) => {
  await page.goto("/")

  await page.locator("button >> text=Neue Dokumentationseinheit").click()
  await page.waitForSelector("text=Festplatte durchsuchen")

  const regex = /rechtsprechung\/(.*)\/dokumente/g
  const match = regex.exec(page.url())
  return match[1] || ""
}

export const deleteDocUnit = async (page: Page, documentNumber: string) => {
  await page.goto("/")

  const selectDocUnit = page
    .locator("tr", {
      has: page.locator(
        `td:nth-child(1) a[href*="/rechtsprechung/${documentNumber}"]`
      ),
    })
    .locator("td:nth-child(5) i")
  await selectDocUnit.waitFor()
  selectDocUnit.click() // an await here would break the test

  await page.waitForTimeout(2000)
}
