import { expect } from "@playwright/test"

import { openNorm } from "./e2e-utils"
import { testWithImportedNorm } from "./fixtures"

testWithImportedNorm(
  "Check if norm can be exported",
  async ({ page, normData, guid }) => {
    await openNorm(page, normData["officialLongTitle"], guid)

    const locatorExportMenu = page.locator("a:has-text('Export')")
    await expect(locatorExportMenu).toBeVisible()
    await locatorExportMenu.click()
    await expect(page).toHaveURL(`/norms/norm/${guid}/export`)

    const locatorExportButton = page.locator(
      'a:has-text("Zip Datei speichern")'
    )
    await expect(locatorExportButton).toBeVisible()
    await expect(locatorExportButton).toHaveAttribute(
      "download",
      normData["jurisZipFileName"]
    )

    const [download] = await Promise.all([
      // Start waiting for the download
      page.waitForEvent("download"),
      // Perform the action that initiates download
      page.locator('a:has-text("Zip Datei speichern")').click(),
    ])

    expect(download.suggestedFilename()).toBe(normData["jurisZipFileName"])
  }
)
