import { expect } from "@playwright/test"
import { navigateToCategories } from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("autosave on documentation units", () => {
  test("test save button status change", async ({ page, documentNumber }) => {
    navigateToCategories(page, documentNumber)
    await page.locator("[aria-label='Stammdaten Speichern Button']").click()
    await expect(
      page.locator("text=Daten werden gespeichert").nth(0)
    ).toBeVisible()
    await expect(
      page.locator("text=Daten werden gespeichert").nth(1)
    ).toBeVisible()

    await expect(
      page.locator("text=Zuletzt gespeichert um").nth(0)
    ).toBeVisible()
    await expect(
      page.locator("text=Zuletzt gespeichert um").nth(1)
    ).toBeVisible()
  })

  test("test could not update documentUnit", async ({
    page,
    documentNumber,
  }) => {
    await page.route(
      "**/api/v1/caselaw/documentunits/*/docx",
      async (route) => {
        route.fulfill({
          status: 400,
          contentType: "text/plain",
          body: "Not Found!",
        })
      }
    )
    navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Stammdaten Speichern Button']").click()
    await expect(
      page.locator("text='Daten werden gespeichert'").nth(0)
    ).toBeVisible()
    await expect(
      page.locator("text='Daten werden gespeichert'").nth(1)
    ).toBeVisible()

    await expect(
      page.locator("text='Fehler beim Speichern'").nth(0)
    ).toBeVisible()
    await expect(
      page.locator("text='Fehler beim Speichern'").nth(1)
    ).toBeVisible()
  })

  test("test automatic save documentUnit", async ({ page, editorField }) => {
    test.setTimeout(30 * 1000) // autosave is supposed to happen every 10s
    await editorField.click()
    await editorField.type("this is a change")
    await page.keyboard.down("Tab")

    await expect(
      page.locator("text=Daten werden gespeichert").nth(0)
    ).toBeVisible({ timeout: 11 * 1000 }) // autosave is supposed to happen every 10s
    await expect(
      page.locator("text=Daten werden gespeichert").nth(1)
    ).toBeVisible({ timeout: 11 * 1000 }) // autosave is supposed to happen every 10s

    await expect(
      page.locator("text=Zuletzt gespeichert um").nth(0)
    ).toBeVisible()
    await expect(
      page.locator("text=Zuletzt gespeichert um").nth(1)
    ).toBeVisible()
  })
})
