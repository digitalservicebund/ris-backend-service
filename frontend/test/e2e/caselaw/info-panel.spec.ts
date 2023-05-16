import { expect } from "@playwright/test"
import { navigateToCategories } from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("info panel", () => {
  test("updated fileNumber should update info panel", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const infoPanel = page.locator("div", { hasText: documentNumber }).nth(-2)
    const fileNumberInfo = infoPanel
      .locator("div", { hasText: "Aktenzeichen" })
      .nth(-2)
    await expect(fileNumberInfo).toHaveText("Aktenzeichen - ")

    await page.locator("[aria-label='Aktenzeichen']").fill("-firstChip")
    await page.keyboard.press("Enter")
    await expect(fileNumberInfo).toHaveText("Aktenzeichen-firstChip")

    await page.locator("[aria-label='Aktenzeichen']").fill("-secondChip")
    await page.keyboard.press("Enter")
    await expect(fileNumberInfo).toHaveText("Aktenzeichen-firstChip")

    // delete first chip
    await page.locator("div", { hasText: "-firstChip" }).nth(-2).click()
    await page.keyboard.press("Enter")
    await expect(fileNumberInfo).toHaveText("Aktenzeichen-secondChip")
  })

  test("updated court should update info panel", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const infoPanel = page.locator("div", { hasText: documentNumber }).nth(-2)
    const courtInfo = infoPanel.locator("div", { hasText: "Gericht" }).first()
    await expect(courtInfo).toHaveText("Gericht - ")

    await page.locator("[aria-label='Gericht']").fill("aalen")
    await page.locator("text=AG Aalen").click()
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("AG Aalen")
    await expect(courtInfo).toContainText("AG Aalen")
  })

  test("updated decion date should update info panel", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const infoPanel = page.locator("div", { hasText: documentNumber }).nth(-2)
    const dateInfo = infoPanel
      .locator("div", { hasText: "Entscheidungsdatum" })
      .first()
    await expect(dateInfo).toHaveText("Entscheidungsdatum - ")

    await page.locator("[aria-label='Entscheidungsdatum']").fill("03.02.2022")
    expect(
      await page.locator("[aria-label='Entscheidungsdatum']").inputValue()
    ).toBe("03.02.2022")

    //when using the .fill() method, we need 3 tabs to leave the field
    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")

    await expect(dateInfo).toContainText("03.02.2022")
  })
})
