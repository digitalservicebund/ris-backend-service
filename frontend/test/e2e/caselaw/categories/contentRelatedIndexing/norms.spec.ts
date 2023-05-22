import { expect } from "@playwright/test"
import {
  navigateToCategories,
  waitForInputValue,
} from "~/e2e/caselaw/e2e-utils"
import { testWithDocumentUnit as test } from "~/e2e/caselaw/fixtures"

test.describe("norm", () => {
  test("rendering", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await expect(page.getByRole("heading", { name: "Normen" })).toBeVisible()

    await expect(page.getByLabel("RIS-Abkürzung")).toBeVisible()
    await expect(page.getByLabel("Einzelnorm")).toBeVisible()
    await expect(page.getByLabel("Fassungsdatum")).toBeVisible()
    await expect(page.getByLabel("Jahr")).toBeVisible()
  })

  test("direct norm input", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='RIS-Abkürzung']").fill("Bay")
    await expect(page.getByText("BayWaldNatPV BY")).toBeVisible()
    await page.getByText("BayWaldNatPV BY").click()
    await waitForInputValue(
      page,
      "[aria-label='RIS-Abkürzung']",
      "BayWaldNatPV BY"
    )
  })
})
