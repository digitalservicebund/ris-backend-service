import { expect } from "@playwright/test"
import { navigateToCategories } from "./e2e-utils"
import { caselawTest as test } from "./fixtures"

test.describe("info panel", () => {
  test("updated fileNumber should update info panel", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Aktenzeichen']").fill("-firstChip")
    await page.keyboard.press("Enter")

    await expect(
      page
        .getByTestId("document-unit-info-panel-items")
        .getByText("-firstChip"),
    ).toBeVisible()

    await page.locator("[aria-label='Aktenzeichen']").fill("-secondChip")
    await page.keyboard.press("Enter")
    await expect(
      page
        .getByTestId("document-unit-info-panel-items")
        .getByText("-firstChip"),
    ).toBeVisible()

    // delete first chip
    await page
      .locator("[data-testid='chip']", { hasText: "-firstChip" })
      .click()
    await page.keyboard.press("Enter")
    await expect(
      page
        .getByTestId("document-unit-info-panel-items")
        .getByText("-secondChip"),
    ).toBeVisible()
  })

  test("updated court should update info panel", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Gericht']").fill("aalen")
    await page.getByText("AG Aalen").click()
    await expect(page.locator("[aria-label='Gericht']")).toHaveValue("AG Aalen")
    await expect(
      page.getByTestId("document-unit-info-panel-items").getByText("AG Aalen"),
    ).toBeVisible()
  })

  test("updated decision date should update info panel", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Entscheidungsdatum']").fill("03.02.2022")
    await expect(page.locator("[aria-label='Entscheidungsdatum']")).toHaveValue(
      "03.02.2022",
    )

    //when using the .fill() method, we need 4 tabs to leave the field
    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")

    await expect(
      page
        .getByTestId("document-unit-info-panel-items")
        .getByText("03.02.2022"),
    ).toBeVisible()
  })
})
