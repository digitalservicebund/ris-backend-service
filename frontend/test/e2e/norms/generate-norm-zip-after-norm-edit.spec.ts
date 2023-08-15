import { expect } from "@playwright/test"

import {
  getDownloadedFileContent,
  getMetaDataFileAsString,
  openNorm,
  saveNormFrame,
} from "./e2e-utils"
import { testWithImportedNorm } from "./fixtures"
import { FieldType, fillInputField } from "./utilities"

testWithImportedNorm(
  "Check if norm zip can be generated properly after an edit",
  async ({ page, normData, guid }) => {
    await openNorm(page, guid)
    const fileName = normData["jurisZipFileName"]
    const locatorFrameButton = page.locator("a:has-text('Rahmen')")
    await expect(locatorFrameButton).toBeVisible()
    await locatorFrameButton.click()

    const newValue = "fake-Juris-Abkürzung"
    await fillInputField(page, FieldType.TEXT, "NORM/risAbbreviation", newValue)

    await saveNormFrame(page)
    await expect(page.locator("text=Zuletzt").first()).toBeVisible()

    const locatorExportMenu = page.locator("a:has-text('Export')")
    await expect(locatorExportMenu).toBeVisible()
    await locatorExportMenu.click()
    await expect(page).toHaveURL(`/norms/norm/${guid}/export`)
    const locatorNewGeneration = page.locator(
      "a:has-text('Neue Zip-Datei generieren')",
    )
    await expect(locatorNewGeneration).toBeVisible()
    await locatorNewGeneration.click()

    const downloadFileContent = await getDownloadedFileContent(page, fileName)

    const metadataFileDownloaded = await getMetaDataFileAsString(
      downloadFileContent,
    )

    expect(metadataFileDownloaded.includes(newValue)).toBeTruthy()
  },
)
