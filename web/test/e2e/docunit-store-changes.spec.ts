import { test, Page, expect } from "@playwright/test"
import { deleteDocUnit, generateDocUnit } from "./docunit-lifecycle.spec"
import { getAuthenticatedPage } from "./e2e-utils"

test.describe("save changes and verify it persists", () => {
  // SETUP

  let documentNumber: string
  let page: Page

  test.beforeAll(async ({ browser }) => {
    page = await getAuthenticatedPage(browser)

    documentNumber = await generateDocUnit(page)
  })

  test.afterAll(async () => await deleteDocUnit(page, documentNumber))

  // TESTS

  test("save core data change", async () => {
    await page.goto("/")
    await page
      .locator(`a[href*="/rechtsprechung/${documentNumber}/dokumente"]`)
      .click()
    await page
      .locator(`a[href*="/rechtsprechung/${documentNumber}/rubriken"]`)
      .first()
      .click()

    await page.locator("id=aktenzeichen").fill("abc")
    await page.locator("button >> text=Speichern").first().click()

    await page.goto("/")
    await page.reload() // to make sure the data needs to come fresh from the server
    await page.goto("/")

    // verify change is visible in docunit list on /rechtsprechung
    const aktenzeichenColumn = page
      .locator("tr", {
        has: page.locator(
          `td:nth-child(1) a[href*="/rechtsprechung/${documentNumber}"]`
        ),
      })
      .locator("td:nth-child(3)")

    expect(await aktenzeichenColumn.innerText()).toBe("abc")
  })
})
