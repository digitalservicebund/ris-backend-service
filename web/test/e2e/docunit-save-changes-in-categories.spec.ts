import { test, expect } from "@playwright/test"
import {
  navigateToCategories,
  generateDocUnit,
  deleteDocUnit,
  pageReload,
} from "./e2e-utils"

test.describe("save changes in core data and texts and verify it persists", () => {
  test("test core data change", async ({ page }) => {
    const documentNumber = await generateDocUnit(page)
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Aktenzeichen']").fill("abc")

    page.once("dialog", async (dialog) => {
      expect(dialog.message()).toBe("Dokumentationseinheit wurde gespeichert")
      await dialog.accept()
    })
    await page.locator("[aria-label='Stammdaten Speichern Button']").click()
    await page.waitForTimeout(500)

    await pageReload(page) // to make sure the data needs to come fresh from the server

    // 1. verify that the change is visible in the docunit list on /jurisdiction
    const aktenzeichenColumn = page
      .locator("tr", {
        has: page.locator(`a >> text=${documentNumber}`),
      })
      .locator("td >> text=abc")

    await expect(aktenzeichenColumn).toBeVisible()
    expect(await aktenzeichenColumn.innerText()).toBe("abc")

    // 2. verify that the change is visible in Rubriken
    await navigateToCategories(page, documentNumber)
    expect(await page.inputValue("[aria-label='Aktenzeichen']")).toBe("abc")

    await deleteDocUnit(page, documentNumber)
  })

  test("test texts data change", async ({ page }) => {
    const documentNumber = await generateDocUnit(page)
    await navigateToCategories(page, documentNumber)

    let editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )

    await editorField.click()

    const boldButton = await page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(2)
    await boldButton.click()

    await editorField.type("this is bold text")
    await page
      .locator("[aria-label='Kurz- und Langtexte Speichern Button']")
      .click()
    await page.waitForTimeout(500)

    await pageReload(page)

    // verify that the change is visible in the Kurz- und Langtexte field
    await navigateToCategories(page, documentNumber)
    editorField = await page.locator(
      "[aria-label='Entscheidungsname Editor Feld'] >> div"
    )
    expect(await editorField.innerHTML()).toBe(
      "<p><strong>this is bold text</strong></p>"
    )

    await deleteDocUnit(page, documentNumber)
  })
})
