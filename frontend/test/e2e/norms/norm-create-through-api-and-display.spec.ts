import { expect, Page } from "@playwright/test"

import { openNorm } from "./e2e-utils"
import {
  FieldType,
  getNormBySections,
  NormSection,
  testWithImportedNorm,
} from "./fixtures"

async function expectInputFields(
  page: Page,
  section: NormSection,
  valueIndex?: number
) {
  for (const field of section.fields ?? []) {
    await expect(
      page.locator(`label:text-is("${field.label}")`).first()
    ).toBeVisible()

    const value =
      valueIndex !== undefined ? field.values?.[valueIndex] : field.value

    switch (field.type) {
      case FieldType.CHECKBOX:
        expect(
          await page.isChecked(`role=checkbox[name="${field.label}"]`)
        ).toBe(value ?? false)
        break

      case FieldType.TEXT:
        expect(await page.inputValue(`input#${field.name}`)).toBe(value ?? "")
        break

      case FieldType.CHIPS:
        for (const partialValue of value as unknown[]) {
          await expect(
            await page.locator(`div.label-wrapper:text-is("${partialValue}")`)
          ).toBeVisible()
        }
        break

      case FieldType.DROPDOWN:
        // TODO
        break
    }
  }
}

async function expectRepeatedSectionInputFields(
  page: Page,
  section: NormSection
) {
  const expandable = page.locator(`#${section.id}`)
  await expect(expandable).toBeVisible()
  await expect(expandable).toContainText(section.heading)
  await expandable.click()
  const numberOfSectionRepetition = Math.max(
    ...(section.fields ?? []).map((field) => field.values?.length ?? 0)
  )
  const listEntries = expandable.getByLabel("Listen Eintrag")
  await expect(listEntries).toHaveCount(numberOfSectionRepetition)

  for (let index = 0; index < numberOfSectionRepetition; index++) {
    const entry = listEntries.nth(index)
    await entry.getByRole("button", { name: "Eintrag bearbeiten" }).click()
    await expectInputFields(page, section, index)
    await page.keyboard.down("Enter") // Stop editing / close inputs again.
  }
}

async function expectHeadingAppearAfterScroll(page, heading) {
  const locator = page.locator(`a span:text-is('${heading}')`)
  await expect(locator).toBeVisible()
  await locator.click()
  await expect(page).toHaveInsideViewport(
    `legend:text-is("${heading}"), h2:text-is("${heading}")`
  )
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
      if (section.isRepeatedSection) {
        await expectRepeatedSectionInputFields(page, section)
      } else {
        await expect(
          page.locator(`a span:text-is("${section.heading}")`)
        ).toBeVisible()
        await expect(
          page
            .locator(
              `h2:text-is("${section.heading}"), legend:text-is("${section.heading}")`
            )
            .first()
        ).toBeVisible()

        expectInputFields(page, section)

        for (const subSection of section.sections ?? []) {
          const header = page
            .locator(`legend:text-is("${subSection.heading}")`)
            .first()
          await expect(header).toBeVisible()
          await header.click()
          await expectInputFields(page, subSection)
        }
      }
    }
  }
)

testWithImportedNorm(
  "Check if switching frame sections affects sections being inside or outside viewport",
  async ({ page, normData, guid }) => {
    await openNorm(page, normData.officialLongTitle, guid)

    const locatorFrameButton = page.locator("a:has-text('Rahmen')")
    await expect(locatorFrameButton).toBeVisible()
    await locatorFrameButton.click()
    await expect(page).toHaveURL(`/norms/norm/${guid}/frame`)
    await expect(page).toHaveInsideViewport(
      'legend:text-is("Allgemeine Angaben")'
    )

    const sections = getNormBySections(normData)

    for (const section of sections) {
      await expectHeadingAppearAfterScroll(page, section.heading)
    }
  }
)
