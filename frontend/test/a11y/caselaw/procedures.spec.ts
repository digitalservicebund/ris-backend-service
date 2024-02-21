import AxeBuilder from "@axe-core/playwright"
import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

test.describe("a11y of procedures page (/caselaw/procedures)", () => {
  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("procedures list", async ({ page }) => {
    await page.goto("caselaw/procedures")
    await expect(page.getByLabel("Nach Vorgängen suchen")).toBeVisible()

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})
