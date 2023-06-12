import { Page, expect } from "@playwright/test"
import {
  AnyField,
  FieldType,
  FieldValueTypeMapping,
  MetadataInputSection,
} from "./types"

type FieldExpecter<T> = (page: Page, id: string, value: T) => Promise<void>

type FieldExpectMapping = {
  [Type in FieldType]: FieldExpecter<FieldValueTypeMapping[Type]>
}

const expectTextInput: FieldExpecter<string> = async (page, id, value) => {
  const content = await page.locator(`input#${id}`).inputValue()
  expect(content).toBe(value)
}

const expectTextArea: FieldExpecter<string> = async (page, id, value) => {
  const content = await page.locator(`textarea#${id}`).inputValue()
  expect(content).toBe(value)
}

const expectCheckbox: FieldExpecter<boolean> = async (page, id, value) => {
  const checked = await page.locator(`input#${id}`).isChecked()
  expect(checked).toBe(value)
}

const expectRadioButton: FieldExpecter<boolean> = async (page, id, value) => {
  const checked = await page.locator(`input#${id}`).isChecked()
  expect(checked).toBe(value)
}

const expectChipsInput: FieldExpecter<string[]> = async (page, _, value) => {
  for (const subValue of (value ?? []) as string[]) {
    const chip = page.locator(`div.label-wrapper:text-is("${subValue}")`)
    await expect(chip).toBeVisible()
  }
}

const expectDropdown: FieldExpecter<string> = async (page, id, value) => {
  const inputValue = await page.locator(`input#${id}`).inputValue()
  expect(inputValue).toBe(value)
}

const FIELD_EXPECTER: FieldExpectMapping = {
  [FieldType.TEXT]: expectTextInput,
  [FieldType.TEXTAREA]: expectTextArea,
  [FieldType.CHECKBOX]: expectCheckbox,
  [FieldType.RADIO]: expectRadioButton,
  [FieldType.CHIPS]: expectChipsInput,
  [FieldType.DROPDOWN]: expectDropdown,
}

export async function expectInputFieldHasCorrectValue<
  Type extends FieldType,
  Value extends FieldValueTypeMapping[Type]
>(page: Page, type: Type, id: string, value: Value): Promise<void> {
  const expecter = FIELD_EXPECTER[type]
  return expecter(page, id, value)
}

export async function expectInputFieldGroupHasCorrectValues(
  page: Page,
  fields: AnyField[],
  valueIndex?: number
): Promise<void> {
  for (const field of fields ?? []) {
    const value =
      valueIndex !== undefined ? field.values?.[valueIndex] : field.value

    if (value !== undefined) {
      const label = page.locator(`label:has-text("${field.label}")`).first()
      await expect(label).toBeVisible()

      await expectInputFieldHasCorrectValue(page, field.type, field.id, value)
    }
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
  const entryCount = await listEntries.count()
  expect(entryCount).toBe(numberOfSectionRepetition)

  const fields = section.fields ?? []

  async function expectEntry(index: number): Promise<void> {
    await expectInputFieldGroupHasCorrectValues(page, fields, index)
    await page.keyboard.down("Enter") // Stop editing / close inputs again.
  }

  // Single entries are automatically in edit mode.
  if (entryCount == 1) {
    await expectEntry(0)
  } else {
    for (let index = 0; index < numberOfSectionRepetition; index++) {
      const entry = listEntries.nth(index)
      await entry.getByRole("button", { name: "Eintrag bearbeiten" }).click()
      await expectEntry(index)
    }
  }
}

export async function expectExpandableSectionNotRepeatableToHaveCorrectValues(
  page: Page,
  section: MetadataInputSection
): Promise<void> {
  const expandable = page.locator(`#${section.id}`)
  await expect(expandable).toBeVisible()
  await expect(expandable).toContainText(section.heading ?? "")

  await expandable.click()

  for (const field of section.fields ?? []) {
    if (field.values !== undefined && field.values[0] !== undefined) {
      const label = page.locator(`label:has-text("${field.label}")`).first()
      await expect(label).toBeVisible()

      await expectInputFieldHasCorrectValue(
        page,
        field.type,
        field.id,
        field.values[0]
      )
    }
  }
  const finishButton = expandable.getByRole("button", { name: "Fertig" })
  await finishButton.click()
}

export async function expectMetadataInputSectionToHaveCorrectData(
  page: Page,
  section: MetadataInputSection
): Promise<void> {
  if (section.isRepeatedSection) {
    await expectRepeatedSectionListHasCorrectEntries(page, section)
  } else if (section.isSingleFieldSection) {
    await expectInputFieldGroupHasCorrectValues(page, section.fields ?? [])
  } else if (section.isExpandableNotRepeatable) {
    await expectExpandableSectionNotRepeatableToHaveCorrectValues(page, section)
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
