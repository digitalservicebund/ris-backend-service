import { expect } from "@playwright/test"
import { useAxeBuilder } from "~/a11y/caselaw/a11y.utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { navigateToProcedures } from "~/e2e/caselaw/utils/e2e-utils"

test.describe("a11y of procedures page (/caselaw/procedures)", () => {
  test("procedures list", async ({ page }) => {
    await navigateToProcedures(page)

    const accessibilityScanResults = await useAxeBuilder(page).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})
