import { Page, expect } from "@playwright/test"

import { openNorm } from "./e2e-utils"
import { getNormBySections, testWithImportedNorm } from "./fixtures"
import {
  MetadataInputSection,
  expectMetadataInputSectionToHaveCorrectDataOnDisplay,
} from "./utilities"

async function expectSectionAppearsAfterScroll(
  page: Page,
  section: MetadataInputSection,
) {
  const locator = page.locator(`a span:text-is('${section.heading}')`)
  await expect(locator).toBeVisible()
  await locator.click()

  if (section.isSingleFieldSection) {
    const firstFieldLabel = section.fields?.[0].label ?? ""
    await expect(
      page.locator(`h2:text-is("${firstFieldLabel}")`),
    ).toBeInViewport()
  } else {
    await expect(
      page.locator(
        `legend:text-is("${section.heading}"), h2:text-is("${section.heading}")`,
      ),
    ).toBeInViewport()
  }
}

testWithImportedNorm(
  "Check display of norm complex",
  async ({ page, normData, guid }) => {
    await openNorm(page, guid)
    await expect(page).toHaveURL(`/norms/norm/${guid}`)
    await expect(
      page.getByText(
        normData.metadataSections?.NORM?.[0]?.OFFICIAL_LONG_TITLE?.[0] ?? "",
      ),
    ).toBeVisible()

    for (const section of Object.values(normData.sections)) {
      await expect(
        page.getByText(section.designation, { exact: true }),
      ).toBeVisible()
      await expect(
        page.getByText(section.header, { exact: true }),
      ).toBeVisible()
      for (const paragraph of Object.values(section.paragraphs)) {
        if (paragraph.marker === undefined) {
          await expect(page.getByText(paragraph.text)).toBeVisible()
        } else {
          await expect(
            page.getByText(paragraph.marker + " " + paragraph.text),
          ).toBeVisible()
        }
      }
    }
  },
)

// eslint-disable-next-line playwright/no-skipped-test
testWithImportedNorm.skip(
  "Check if frame fields are correctly displayed",
  async ({ page, normData, guid }) => {
    await openNorm(page, guid)

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
      if (!section.isNotImported) {
        await expectMetadataInputSectionToHaveCorrectDataOnDisplay(
          page,
          section,
        )
      }
    }
  },
)

testWithImportedNorm(
  "Check if switching frame sections affects sections being inside or outside viewport",
  async ({ page, normData, guid }) => {
    testWithImportedNorm.slow()
    await openNorm(page, guid)

    const locatorFrameButton = page.locator("a:has-text('Rahmen')")
    await expect(locatorFrameButton).toBeVisible()
    await locatorFrameButton.click()
    await expect(page).toHaveURL(`/norms/norm/${guid}/frame`)

    const sections = getNormBySections(normData)
    const sectionsWithHeading = sections.filter((section) => !!section.heading)

    // Manually add sections that are not part of the fixtures
    sectionsWithHeading.push({ heading: "ELI" })

    for (const section of sectionsWithHeading) {
      // Skip section names that are not listed as menu items in the sidebar
      if (
        section.heading === "Abweichendes Inkrafttretedatum" ||
        section.heading === "Abweichendes Außerkrafttretedatum" ||
        section.heading === "Datum des Inkrafttretens" ||
        section.heading === "Datum des Außerkrafttretens" ||
        section.heading === "Grundsätzliches Inkrafttretedatum" ||
        section.heading === "Grundsätzliches Außerkrafttretedatum" ||
        section.heading === "Veröffentlichungsdatum"
      ) {
        continue
      }

      await expectSectionAppearsAfterScroll(page, section)
    }
  },
)
