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
    await navigateToRubriken(page, documentNumber)
    // using the first field: Aktenzeichen

    await page.locator("id=aktenzeichen").fill("abc")
    await page.locator("button >> text=Speichern").first().click()

    await page.waitForTimeout(500) // give server time to process, otherwise this test gets flaky
    await pageReload(page) // to make sure the data needs to come fresh from the server

    // 1. verify that the change is visible in docunit list on /rechtsprechung
    const aktenzeichenColumn = page
      .locator("tr", {
        has: page.locator(
          `td:nth-child(1) a[href*="/rechtsprechung/${documentNumber}"]`
        ),
      })
      .locator("td:nth-child(3)")
    expect(await aktenzeichenColumn.innerText()).toBe("abc")

    // 2. verify that the change is visible in Rubriken
    await navigateToRubriken(page, documentNumber)
    expect(await page.inputValue("id=aktenzeichen")).toBe("abc")
  })

  test("save formatted text input data change", async () => {
    await navigateToRubriken(page, documentNumber)
    // using the first field: Entscheidungsname

    const boldButton = await page
      .locator("id=entscheidungsname_btns >> div")
      .first()
    await boldButton.click()
    let editorField = await page.locator("id=entscheidungsname_editor >> div")
    await editorField.type("this is bold text")
    await page.locator("button >> text=Speichern").last().click()

    await pageReload(page)

    // verify that the change is visible in Rubriken
    await navigateToRubriken(page, documentNumber)
    editorField = await page.locator("id=entscheidungsname_editor >> div")
    expect(await editorField.innerHTML()).toBe(
      "<p><strong>this is bold text</strong></p>"
    )
  })
})

export const pageReload = async (page: Page) => {
  await page.goto("/")
  await page.reload()
  await page.goto("/")
}
