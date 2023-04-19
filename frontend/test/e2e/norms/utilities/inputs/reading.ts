import { Page, expect } from "@playwright/test"
import {
  AnyField,
  FieldType,
  FieldValueTypeMapping,
  MetadataInputSection,
} from "./types"

export async function expectInputFieldHasCorrectValue<
  Type extends FieldType,
  Value extends FieldValueTypeMapping[Type]
>(page: Page, type: Type, name: string, value?: Value): Promise<void> {
  switch (type) {
    case FieldType.CHECKBOX:
      expect(await page.isChecked(`role=checkbox[name="${name}"]`)).toBe(
        value ?? false
      )
      break

    case FieldType.TEXT:
      expect(await page.inputValue(`input#${name}`)).toBe(value ?? "")
      break

    case FieldType.CHIPS:
      for (const subValue of (value ?? []) as string[]) {
        const chip = page.locator(`div.label-wrapper:text-is("${subValue}")`)
        await expect(chip).toBeVisible()
      }
      break

    case FieldType.DROPDOWN:
      // TODO
      break
  }
}

export async function expectInputFieldGroupHasCorrectValues(
  page: Page,
  fields: AnyField[],
  valueIndex?: number
): Promise<void> {
  for (const field of fields ?? []) {
    const label = page.locator(`label:text-is("${field.label}")`).first()
    await expect(label).toBeVisible()

    const name = field.type == FieldType.CHECKBOX ? field.label : field.name
    const value =
      valueIndex !== undefined ? field.values?.[valueIndex] : field.value
    await expectInputFieldHasCorrectValue(page, field.type, name, value)
  }
}

export async function expectRepeatedSectionListHasCorrectEntries(
  page: Page,
  section: MetadataInputSection
): Promise<void> {
  const expandable = page.locator(`#${section.id}`)
  await expect(expandable).toBeVisible()
  await expect(expandable).toContainText(section.heading ?? "")
  await expandable.click()
  const numberOfSectionRepetition = Math.max(
    ...(section.fields ?? []).map((field) => field.values?.length ?? 0)
  )
  const listEntries = expandable.getByLabel("Listen Eintrag")
  await expect(listEntries).toHaveCount(numberOfSectionRepetition)

  for (let index = 0; index < numberOfSectionRepetition; index++) {
    const entry = listEntries.nth(index)
    await entry.getByRole("button", { name: "Eintrag bearbeiten" }).click()
    const fields = section.fields ?? []
    await expectInputFieldGroupHasCorrectValues(page, fields, index)
    await page.keyboard.down("Enter") // Stop editing / close inputs again.
  }
}

export async function expectMetadataInputSectionToHaveCorrectData(
  page: Page,
  section: MetadataInputSection
): Promise<void> {
  if (section.isRepeatedSection) {
    await expectRepeatedSectionListHasCorrectEntries(page, section)
  } else if (section.isSingleFieldSection) {
    await expectInputFieldGroupHasCorrectValues(page, section.fields ?? [])
  } else {
    const heading = page.locator(`a span:text-is("${section.heading}")`)
    await expect(heading).toBeVisible()
    const legend = page.locator(
      `h2:text-is("${section.heading}"), legend:text-is("${section.heading}")`
    )
    await expect(legend.first()).toBeVisible()
    await expectInputFieldGroupHasCorrectValues(page, section.fields ?? [])

    for (const subSection of section.sections ?? []) {
      const header = page
        .locator(`legend:text-is("${subSection.heading}")`)
        .first()
      await expect(header).toBeVisible()
      await header.click()
      await expectInputFieldGroupHasCorrectValues(page, subSection.fields ?? [])
    }
  }
}
