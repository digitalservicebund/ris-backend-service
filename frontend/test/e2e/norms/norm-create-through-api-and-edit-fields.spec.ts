import { expect } from "@playwright/test"

import { openNorm } from "./e2e-utils"
import { getNormBySections, testWithImportedNorm } from "./fixtures"
import { newNorm } from "./testdata/norm_edited_fields"
import {
  fillMetadataInputSection,
  expectMetadataInputSectionToHaveCorrectData,
} from "./utilities"

testWithImportedNorm.skip(
  "Check if fields can be edited",
  async ({ page, normData, guid }) => {
    testWithImportedNorm.slow()
    await openNorm(page, normData["officialLongTitle"], guid)

    const locatorFrameButton = page.locator("a:has-text('Rahmen')")
    await expect(locatorFrameButton).toBeVisible()
    await locatorFrameButton.click()

    const sections = getNormBySections(newNorm)

    for (const section of sections) {
      await fillMetadataInputSection(page, section)
    }

    await page.locator("[aria-label='Rahmendaten Speichern Button']").click()
    await expect(
      page.locator("text=Zuletzt gespeichert um").first()
    ).toBeVisible()
    await page.reload()

    for (const section of sections) {
      await expectMetadataInputSectionToHaveCorrectData(page, section)
    }
  }
)
