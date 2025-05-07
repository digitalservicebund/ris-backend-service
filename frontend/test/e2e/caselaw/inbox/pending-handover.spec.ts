import { expect } from "@playwright/test"

import { navigateToInbox } from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

test.describe("pending handover inbox", () => {
  test("renders search entry form", async ({ page }) => {
    await navigateToInbox(page)
    const tab = page.getByTestId("external-handover-tab")
    await expect(tab).toBeVisible()
    await tab.click()

    const tabContent = page.getByTestId("pending-handover-inbox")

    await expect(tabContent.getByLabel("Aktenzeichen Suche")).toBeVisible()
    await expect(tabContent.getByLabel("Gerichtstyp Suche")).toBeVisible()
    await expect(tabContent.getByLabel("Gerichtsort Suche")).toBeVisible()
    await expect(tabContent.getByLabel("Dokumentnummer Suche")).toBeVisible()
    await expect(
      tabContent.getByLabel("Entscheidungsdatum Suche", {
        exact: true,
      }),
    ).toBeVisible()
    await expect(
      tabContent.getByLabel("Entscheidungsdatum Suche Ende", {
        exact: true,
      }),
    ).toBeVisible()
  })

  test("shows all pending handover for docoffice on mounted", async ({
    page,
  }) => {
    await navigateToInbox(page)
    const tab = page.getByTestId("external-handover-tab")
    await expect(tab).toBeVisible()
    await tab.click()

    const tabContent = page.getByTestId("pending-handover-list")

    await expect(tabContent).toBeVisible()

    const row = page.locator("tr", { hasText: "YYTestDoc0019" })
    await expect(
      row.getByRole("button", { name: "Dokumentationseinheit übernehmen" }),
    ).toBeVisible()
  })
})
