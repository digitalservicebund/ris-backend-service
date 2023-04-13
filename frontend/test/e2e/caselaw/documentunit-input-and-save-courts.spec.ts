import { expect } from "@playwright/test"
import { navigateToCategories, waitForSaving } from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("Court and incorrect court", () => {
  test("input value in court field, press enter and reload", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Gericht']").fill("BGH")

    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(1)

    await page.keyboard.press("ArrowDown")
    await page.keyboard.press("Enter")

    await waitForSaving(page)

    await page.reload()

    expect(await page.inputValue("[aria-label='Gericht']")).toBe("BGH")
  })

  test("open incorrect court field, input one, save and reload", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Fehlerhaftes Gericht anzeigen']").click()

    await page
      .locator("[aria-label='Fehlerhaftes Gericht']")
      .fill("incorrectCourt1")
    await page.keyboard.press("Enter")

    await page.locator("[aria-label='Stammdaten Speichern Button']").click()
    await waitForSaving(page)

    await page.reload()

    await page.locator("[aria-label='Fehlerhaftes Gericht anzeigen']").click()

    await expect(page.locator("text=IncorrectCourt1").first()).toBeVisible()
  })

  test("open incorrect court field, input two, save, reload, remove first, save and reload", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Fehlerhaftes Gericht anzeigen']").click()

    await page
      .locator("[aria-label='Fehlerhaftes Gericht']")
      .fill("incorrectCourt1")
    await page.keyboard.press("Enter")
    await page
      .locator("[aria-label='Fehlerhaftes Gericht']")
      .fill("incorrectCourt2")
    await page.keyboard.press("Enter")

    await waitForSaving(page)

    await page.reload()

    await page.locator("[aria-label='Fehlerhaftes Gericht anzeigen']").click()

    await expect(page.locator("text=IncorrectCourt1")).toBeVisible()
    await expect(page.locator("text=IncorrectCourt2")).toBeVisible()

    await page
      .locator(":text('IncorrectCourt1') + div > [aria-label='Löschen']")
      .click()

    await waitForSaving(page)

    await page.reload()

    await page.locator("[aria-label='Fehlerhaftes Gericht anzeigen']").click()

    await expect(page.locator("text=IncorrectCourt1")).toHaveCount(0)
    await expect(page.locator("text=IncorrectCourt2")).toBeVisible()
  })
})
