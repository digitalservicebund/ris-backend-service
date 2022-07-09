import { test, expect, Page } from "@playwright/test"
import { Browser } from "playwright"

test.describe("generate and delete a doc unit", () => {
  let documentNumber: string
  let page: Page

  test("generate doc unit", async ({ browser }) => {
    page = await getAuthenticatedPage(browser)
    await page.goto("/rechtsprechung")

    documentNumber = await generateDocUnit(page)
  })

  test("delete doc unit", async () => {
    await page.goto("/rechtsprechung")

    await deleteDocUnit(page, documentNumber)

    await page.goto("/rechtsprechung")

    await expect(
      page.locator(`a[href*="/rechtsprechung/${documentNumber}"]`)
    ).not.toBeVisible()
  })
})

export const getAuthenticatedPage = async (browser: Browser) => {
  const context = await browser.newContext({
    httpCredentials: {
      username: process.env.STAGING_USER ?? "",
      password: process.env.STAGING_PASSWORD ?? "",
    },
  })
  return await context.newPage()
}

export const generateDocUnit = async (page: Page) => {
  await page.locator("button >> text=Neue Dokumentationseinheit").click()
  await page.waitForSelector("text=Festplatte durchsuchen")

  const regex = /rechtsprechung\/(.*)\/dokumente/g
  const match = regex.exec(page.url())
  return match[1] || ""
}

export const deleteDocUnit = async (page: Page, documentNumber: string) => {
  const selectDocUnit = page
    .locator("tr", {
      has: page.locator(
        `td:nth-child(1) a[href*="/rechtsprechung/${documentNumber}/dokumente"]`
      ),
    })
    .locator("td:nth-child(5) i")
  await selectDocUnit.waitFor()
  selectDocUnit.click() // an await here would break the test

  await page.waitForTimeout(2000)
}
