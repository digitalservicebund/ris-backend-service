import { expect } from "@playwright/test"
import { navigateToCategories } from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

test.describe("norm", () => {
  test("renders all fields", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await expect(page.getByRole("heading", { name: "Normen" })).toBeVisible()
    await expect(page.getByLabel("RIS-Abkürzung")).toBeVisible()
    await expect(page.getByLabel("Einzelnorm")).toBeVisible()
    await expect(page.getByLabel("Fassungsdatum")).toBeVisible()
    await expect(page.getByLabel("Jahr")).toBeVisible()
  })
})
