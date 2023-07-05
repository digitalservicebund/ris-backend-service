import { expect } from "@playwright/test"
import { navigateToCategories } from "./e2e-utils"
import { caselawTest as test } from "./fixtures"

test.describe("info panel", () => {
  test("updated fileNumber should update info panel", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const infoPanel = page.getByText(
      new RegExp(`${documentNumber}.*Aktenzeichen.*`)
    )
    await expect(
      infoPanel.getByText("Aktenzeichen - ", {
        exact: true,
      })
    ).toBeVisible()

    await page.locator("[aria-label='Aktenzeichen']").fill("-firstChip")
    await page.keyboard.press("Enter")
    await expect(infoPanel.getByText("Aktenzeichen-firstChip")).toBeVisible()

    await page.locator("[aria-label='Aktenzeichen']").fill("-secondChip")
    await page.keyboard.press("Enter")
    await expect(infoPanel.getByText("Aktenzeichen-firstChip")).toBeVisible()

    // delete first chip
    await page
      .locator("[data-testid='chip']", { hasText: "-firstChip" })
      .click()
    await page.keyboard.press("Enter")
    await expect(infoPanel.getByText("Aktenzeichen-secondChip")).toBeVisible()
  })

  test("updated court should update info panel", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const infoPanel = page.getByText(new RegExp(`${documentNumber}.*Gericht.*`))
    await expect(
      infoPanel.getByText("Gericht - ", {
        exact: true,
      })
    ).toBeVisible()

    await page.locator("[aria-label='Gericht']").fill("aalen")
    await page.locator("text=AG Aalen").click()
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("AG Aalen")
    await expect(infoPanel.getByText("AG Aalen")).toBeVisible()
  })

  test("updated decision date should update info panel", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const infoPanel = page.getByText(
      new RegExp(`${documentNumber}.*Entscheidungsdatum.*`)
    )
    await expect(
      infoPanel.getByText("Entscheidungsdatum - ", {
        exact: true,
      })
    ).toBeVisible()

    await page.locator("[aria-label='Entscheidungsdatum']").fill("03.02.2022")
    expect(
      await page.locator("[aria-label='Entscheidungsdatum']").inputValue()
    ).toBe("03.02.2022")

    //when using the .fill() method, we need 3 tabs to leave the field
    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")

    await expect(infoPanel.getByText("03.02.2022")).toBeVisible()
  })
})
