import { Page, expect } from "@playwright/test"
import {
  AnyField,
  FieldType,
  FieldValueTypeMapping,
  MetadataInputSection,
} from "./types"

type FieldFiller<T> = (page: Page, name: string, value: T) => Promise<void>

type FieldFillerMapping = {
  [Type in FieldType]: FieldFiller<FieldValueTypeMapping[Type]>
}

const fillCheckbox: FieldFiller<boolean> = async (page, name, value) => {
  const selector = `role=checkbox[name="${name}"]`
  await page.setChecked(selector, value)
}

const fillTextInput: FieldFiller<string> = async (page, name, value) => {
  const selector = `input#${name}`
  const locator = page.locator(selector)
  await expect(locator).toBeEditable()
  await locator.fill(value)
}

const fillChipsInput: FieldFiller<string[]> = async (page, name, value) => {
  const input = page.locator(`input#${name}`)
  const wrapper = page.locator("div .input", { has: input })

  // Clear all chips first
  const chips = wrapper.getByLabel("chip")
  const chipCount = await chips.count()

  // Delete backwards to avoid conflicts.
  for (let index = chipCount - 1; index >= 0; index--) {
    await chips.nth(index).getByLabel("Löschen").click()
  }

  for (const subValue of value) {
    await input.fill(subValue)
    await page.keyboard.press("Enter")
  }
}

const fillDropdown: FieldFiller<string> = async (page, name, value) => {
  const selector = `input#${name}`
  const locatorInput = page.locator(selector + "+ button")
  await locatorInput.click()
  const locatorDropdownOptions = page.locator('[aria-label="dropdown-option"]')
  const count = await locatorDropdownOptions.count()

  for (let i = 0; i < count; i++) {
    const locatorOption = locatorDropdownOptions.nth(i)

    if ((await locatorOption.innerText()) === value) {
      await locatorOption.click()
      break
    }
  }
}

const FIELD_FILLERS: FieldFillerMapping = {
  [FieldType.TEXT]: fillTextInput,
  [FieldType.CHECKBOX]: fillCheckbox,
  [FieldType.CHIPS]: fillChipsInput,
  [FieldType.DROPDOWN]: fillDropdown,
}

export function fillInputField<
  Type extends FieldType,
  Value extends FieldValueTypeMapping[Type]
>(page: Page, type: Type, name: string, value: Value): Promise<void> {
  const filler = FIELD_FILLERS[type]
  return filler(page, name, value)
}

export async function fillInputFieldGroup(
  page: Page,
  fields: AnyField[],
  valueIndex?: number
) {
  for (const field of fields) {
    const name = field.type == FieldType.CHECKBOX ? field.label : field.name
    const value =
      valueIndex !== undefined ? field.values?.[valueIndex] : field.value

    if (value !== undefined) {
      await fillInputField(page, field.type, name, value)
    }
  }
}

export async function fillRepeatedMetadataSectionList(
  page: Page,
  section: MetadataInputSection
): Promise<void> {
  const expandable = page.locator(`#${section.id}`)
  await expandable.click()

  // Clear all entries first
  const listEntries = expandable.getByLabel("Listen Eintrag")
  const entryCount = await listEntries.count()

  // Delete backwards to avoid conflicts.
  for (let index = entryCount - 1; index >= 0; index--) {
    await listEntries
      .nth(index)
      .getByRole("button", { name: "Eintrag löschen" })
      .click()
  }

  const numberOfSectionRepetition = Math.max(
    ...(section.fields ?? []).map((field) => field.values?.length ?? 0)
  )

  if (numberOfSectionRepetition > 0) {
    // After deleting all entries, first entry is in edit mode already.
    await fillInputFieldGroup(page, section.fields ?? [], 0)
    await page.keyboard.press("Enter")
  }

  for (let index = 1; index < numberOfSectionRepetition; index++) {
    await expandable.getByRole("button", { name: "Weitere Angabe" }).click()
    await fillInputFieldGroup(page, section.fields ?? [], index)
    await page.keyboard.press("Enter")
  }
}

export async function fillMetadataInputSection(
  page: Page,
  section: MetadataInputSection
): Promise<void> {
  if (section.isRepeatedSection) {
    await fillRepeatedMetadataSectionList(page, section)
  } else if (section.isSingleFieldSection) {
    await fillInputFieldGroup(page, section.fields ?? [])
  } else {
    await fillInputFieldGroup(page, section.fields ?? [])

    for (const subSection of section.sections ?? []) {
      const header = page
        .locator(`legend:text-is("${subSection.heading}")`)
        .first()
      await expect(header).toBeVisible()
      await header.click()

      await fillInputFieldGroup(page, subSection.fields ?? [])
    }
  }
}
