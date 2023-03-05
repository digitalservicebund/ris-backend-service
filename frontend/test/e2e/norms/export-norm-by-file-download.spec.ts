import fs from "fs"
import { expect } from "@playwright/test"

import { loadJurisTestFile, openNorm } from "./e2e-utils"
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

    const [download] = await Promise.all([
      // Start waiting for the download
      page.waitForEvent("download"),
      // Perform the action that initiates download
      page.locator('a:has-text("Zip Datei speichern")').click(),
    ])

    // Test file name and size
    expect(download.suggestedFilename()).toBe(fileName)
    expect(
      (await fs.promises.stat((await download.path()) as string)).size
    ).toBeGreaterThan(0)

    // Test file content
    const { fileContent } = await loadJurisTestFile(request, fileName)
    const readable = await download.createReadStream()
    const chunks = []
    for await (const chunk of readable) {
      chunks.push(chunk)
    }

    expect(Buffer.compare(fileContent, Buffer.concat(chunks))).toBe(0)
  }
)
