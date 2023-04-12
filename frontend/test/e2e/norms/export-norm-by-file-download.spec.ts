import { expect } from "@playwright/test"

import {
  getDownloadedFileContent,
  loadJurisTestFile,
  openNorm,
} from "./e2e-utils"
import { testWithImportedNorm } from "./fixtures"

testWithImportedNorm.skip(
  "Check if norm can be exported",
  async ({ page, normData, guid, request }) => {
    await openNorm(page, normData["officialLongTitle"], guid)

    const fileName = normData["jurisZipFileName"]

    const locatorExportMenu = page.locator("a:has-text('Export')")
    await expect(locatorExportMenu).toBeVisible()
    await locatorExportMenu.click()
    await expect(page).toHaveURL(`/norms/norm/${guid}/export`)

    const locatorExportButton = page.locator(
      'a:has-text("Zip Datei speichern")'
    )
    await expect(locatorExportButton).toBeVisible()
    await expect(locatorExportButton).toHaveAttribute("download", fileName)

    const { fileContent } = await loadJurisTestFile(request, fileName)

    expect(
      Buffer.compare(
        fileContent,
        await getDownloadedFileContent(page, fileName)
      )
    ).toBe(0)
  }
)
