import { expect, test } from "@playwright/test"
import { checkA11y, injectAxe } from "axe-playwright"
import { testWithImportedNorm } from "../e2e/norms/fixtures"

// eslint-disable-next-line playwright/no-skipped-test
test.describe("a11y of norms list page (/norms)", () => {
  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("norms list", async ({ page }) => {
    await page.goto("/norms")
    await expect(page.locator("text=Dokumentationseinheiten")).toBeVisible()
    await injectAxe(page)
    await checkA11y(page)
  })
})

test.describe("a11y of a norm complex (/norms/norm/{guid})", () => {
  // eslint-disable-next-line playwright/no-skipped-test
  testWithImportedNorm.skip("norm complex", async ({ page, createdGuid }) => {
    await page.goto(`/norms/norm/${createdGuid}`)
    await expect(
      page.locator("text=Gesetz über die Beschaffung sauberer Straßenfahrzeuge")
    ).toBeVisible()
    await injectAxe(page)
    await checkA11y(page)
  })
})

test.describe("a11y of a norm frame data (/norms/norm/{guid}/frame)", () => {
  // eslint-disable-next-line playwright/no-skipped-test
  testWithImportedNorm.skip(
    "norm frame data",
    async ({ page, createdGuid }) => {
      await page.goto(`/norms/norm/${createdGuid}/frame`)
      await expect(
        page.locator("text=Dokumentation des Rahmenelements")
      ).toBeVisible()
      await injectAxe(page)
      await checkA11y(page)
    }
  )
})
