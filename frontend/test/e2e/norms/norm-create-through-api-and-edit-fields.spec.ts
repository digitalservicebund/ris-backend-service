import { expect } from "@playwright/test"

import { openNorm, saveNormFrame } from "./e2e-utils"
import { getNormBySections, testWithImportedNorm } from "./fixtures"
import { newNorm } from "./testdata/norm_edited_fields"
import {
  fillMetadataInputSection,
  expectMetadataInputSectionToHaveCorrectDataOnEdit,
} from "./utilities"

testWithImportedNorm(
  "Check if fields can be edited",
  async ({ page, guid }) => {
    testWithImportedNorm.slow()
    await openNorm(page, guid)

    const locatorFrameButton = page.locator("a:has-text('Rahmen')")
    await expect(locatorFrameButton).toBeVisible()
    await locatorFrameButton.click()

    const sections = getNormBySections(newNorm)

    for (const section of sections) {
      await fillMetadataInputSection(page, section)
    }

    await saveNormFrame(page)
    await expect(page.locator("text=Zuletzt")).toBeVisible()
    await page.reload()

    for (const section of sections) {
      await expectMetadataInputSectionToHaveCorrectDataOnEdit(page, section)
    }
  },
)
