import { test, Page, expect } from "@playwright/test"
import { deleteDocUnit, generateDocUnit } from "./docunit-lifecycle.spec"
import { navigateToRubriken } from "./docunit-store-changes.spec"
import { getAuthenticatedPage } from "./e2e-utils"

test.describe("test the different layout options", () => {
  // SETUP

  let documentNumber: string
  let page: Page

  test.beforeAll(async ({ browser }) => {
    page = await getAuthenticatedPage(browser)

    documentNumber = await generateDocUnit(page)
  })

  test.afterAll(async () => await deleteDocUnit(page, documentNumber))

  // TESTS

  test("ensure default layout", async () => {
    await page.goto("/")
    await navigateToRubriken(page, documentNumber)

    // menu open
    await expect(await page.locator("id=sidebar-close-button")).toBeVisible()
    // odoc panel closed
    await expect(await page.locator("id=odoc-open-element")).toBeVisible()
  })

  test("open odoc panel without odoc attached and close sidebar menu", async () => {
    // open odoc panel
    await page.locator("id=odoc-open-element").click()
    await expect(await page.locator("id=odoc-panel-element")).toBeVisible()
    let urlParams = page.url().split("?")[1]
    expect(urlParams).toEqual("showOdocPanel=true&showSidebar=true")

    await page.waitForSelector(
      "text=Es wurde noch kein Originaldokument hochgeladen."
    )

    // close sidebar menu
    await page.locator("id=sidebar-close-button").click()
    await expect(await page.locator("id=sidebar-open-button")).toBeVisible()
    urlParams = page.url().split("?")[1]
    expect(urlParams).toEqual("showOdocPanel=true&showSidebar=false")
  })
})
