import { expect } from "@playwright/test"
import { useAxeBuilder } from "~/a11y/caselaw/a11y.utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

test.describe("a11y of start page (/caselaw)", () => {
  test("documentUnit list", async ({ page }) => {
    await page.goto("/")
    await expect(
      page.getByRole("button", {
        name: "Neue Entscheidung",
        exact: true,
      }),
    ).toBeVisible()
    const accessibilityScanResults = await useAxeBuilder(page).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})
