import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { navigateToCategories } from "~/e2e/caselaw/utils/e2e-utils"

test.describe("editable chips", { tag: ["@RISDEV-8150"] }, () => {
  test("chips can be edited", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page
      .getByLabel("Aktenzeichen")
      .getByRole("textbox")
      .fill("some value")
    await page.keyboard.press("Enter")

    await expect(
      page.getByTestId("file-numbers").getByRole("listitem"),
    ).toHaveText("some value")

    await page
      .getByLabel("Aktenzeichen")
      .getByRole("listitem")
      .getByLabel("Eintrag bearbeiten")
      .dblclick()

    await page.locator("#fileNumberInput-chip-0").fill("edited")
    await page.keyboard.press("Enter")

    await expect(
      page.getByTestId("file-numbers").getByRole("listitem"),
    ).toHaveText("edited")
  })
})
