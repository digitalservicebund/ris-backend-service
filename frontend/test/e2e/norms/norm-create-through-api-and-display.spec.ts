import { Page, expect } from "@playwright/test"

import { openNorm } from "./e2e-utils"
import { getNormBySections, testWithImportedNorm } from "./fixtures"
import {
  MetadataInputSection,
  expectMetadataInputSectionToHaveCorrectData,
} from "./utilities"

async function expectSectionAppearsAfterScroll(
  page: Page,
  section: MetadataInputSection
) {
  const locator = page.locator(`a span:text-is('${section.heading}')`)
  await expect(locator).toBeVisible()
  await locator.click()

  if (section.isSingleFieldSection) {
    const firstFieldLabel = section.fields?.[0].label ?? ""
    await expect(
      page.locator(`label:text-is("${firstFieldLabel}")`)
    ).toBeInViewport()
  } else {
    await expect(
      page
        .locator(
          `legend:text-is("${section.heading}"), h2:text-is("${section.heading}")`
        )
        .first()
    ).toBeInViewport()
  }
}

testWithImportedNorm(
  "Check display of norm complex",
  async ({ page, normData, guid }) => {
    await openNorm(page, normData.officialLongTitle, guid)

    await expect(page).toHaveURL(`/norms/norm/${guid}`)
    await expect(page.getByText(normData.officialLongTitle)).toBeVisible()

    for (const article of Object.values(normData.articles)) {
      await expect(
        page.getByText(article.marker, { exact: true })
      ).toBeVisible()
      await expect(page.getByText(article.title, { exact: true })).toBeVisible()
      for (const paragraph of Object.values(article.paragraphs)) {
        if (paragraph.marker === undefined) {
          await expect(page.getByText(paragraph.text)).toBeVisible()
        } else {
          await expect(
            page.getByText(paragraph.marker + " " + paragraph.text)
          ).toBeVisible()
        }
      }
    }
  }
)

testWithImportedNorm(
  "Check if frame fields are correctly displayed",
  async ({ page, normData, guid }) => {
    await openNorm(page, normData.officialLongTitle, guid)

    // Outer menu
    await expect(page.locator("a:has-text('Normenkomplex')")).toBeVisible()
    await expect(page.locator("a:has-text('Bestand')")).toBeVisible()
    await expect(page.locator("a:has-text('Export')")).toBeVisible()
    const locatorFrameButton = page.locator("a:has-text('Rahmen')")
    await expect(locatorFrameButton).toBeVisible()

    await locatorFrameButton.click()
    await expect(page).toHaveURL(`/norms/norm/${guid}/frame`)

    const sections = getNormBySections(normData)

    for (const section of sections) {
      await expectMetadataInputSectionToHaveCorrectData(page, section)
    }
  }
)

testWithImportedNorm(
  "Check if switching frame sections affects sections being inside or outside viewport",
  async ({ page, normData, guid }) => {
    testWithImportedNorm.slow()
    await openNorm(page, normData.officialLongTitle, guid)

    const locatorFrameButton = page.locator("a:has-text('Rahmen')")
    await expect(locatorFrameButton).toBeVisible()
    await locatorFrameButton.click()
    await expect(page).toHaveURL(`/norms/norm/${guid}/frame`)

    const sections = getNormBySections(normData)
    const sectionsWithHeading = sections.filter((section) => !!section.heading)

    for (const section of sectionsWithHeading) {
      await expectSectionAppearsAfterScroll(page, section)
    }
  }
)
