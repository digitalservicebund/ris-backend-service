import { Page, expect } from "@playwright/test"
import {
  AnyField,
  FieldType,
  FieldValueTypeMapping,
  FootnoteInputType,
  MetadataInputSection,
} from "./types"
import { FOOTNOTE_LABELS } from "@/components/footnotes/types"
import { MetadatumType } from "@/domain/Norm"

type FieldFiller<T> = (page: Page, id: string, value: T) => Promise<void>

type FieldFillerMapping = {
  [Type in FieldType]: FieldFiller<FieldValueTypeMapping[Type]>
}

const fillTextInput: FieldFiller<string> = async (page, id, value) => {
  const input = page.locator(`input#${id}`)
  await expect(input).toBeEditable()
  await input.fill(value)
}

const fillTextArea: FieldFiller<string> = async (page, id, value) => {
  const input = page.locator(`textarea#${id}`)
  await expect(input).toBeEditable()
  await input.fill(value)
}

const fillTextEditor: FieldFiller<FootnoteInputType[]> = async (
  page,
  id,
  value
) => {
  const input = page.locator(`[data-testid='${id}']`)
  await expect(input).toBeEditable()
  await input.click()
  for (const footnote of value) {
    if (footnote.label != FOOTNOTE_LABELS[MetadatumType.FOOTNOTE_REFERENCE]) {
      await input.type(` #${footnote.label}`)
      await input.press("Enter")
    }
    await input.type(footnote.content)
  }
}

const fillCheckbox: FieldFiller<boolean> = async (page, id, value) => {
  const input = page.locator(`input#${id}`)
  await expect(input).toBeEditable()
  await input.setChecked(value)
}

const fillRadioButton: FieldFiller<boolean> = async (page, id, value) => {
  // You can not "uncheck" a radio button. You must click a different one in the
  // same radio button group.
  if (value) {
    const input = page.locator(`input#${id}`)
    await expect(input).toBeEditable()
    await input.check()
  }
}

const fillChipsInput: FieldFiller<string[]> = async (page, id, value) => {
  const input = page.locator(`input#${id}`)
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

const fillDropdown: FieldFiller<string> = async (page, id, value) => {
  await page.locator(`input#${id} + button`).click()
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
  [FieldType.RADIO]: fillRadioButton,
  [FieldType.CHIPS]: fillChipsInput,
  [FieldType.DROPDOWN]: fillDropdown,
  [FieldType.TEXTAREA]: fillTextArea,
  [FieldType.EDITOR]: fillTextEditor,
}

export function fillInputField<
  Type extends FieldType,
  Value extends FieldValueTypeMapping[Type]
>(page: Page, type: Type, id: string, value: Value): Promise<void> {
  const filler = FIELD_FILLERS[type]
  return filler(page, id, value)
}

export async function fillInputFieldGroup(
  page: Page,
  fields: AnyField[],
  valueIndex?: number
) {
  for (const field of fields) {
    const value =
      valueIndex !== undefined ? field.values?.[valueIndex] : field.value

    if (value !== undefined) {
      await fillInputField(page, field.type, field.id, value)
    }
  }
}

/**
 * Delete all entries of the repeated section, handling all special cases.
 * For more independent integration with other steps, the section gets opened
 * AND closed again.
 */
export async function clearRepeatedMetadataSectionList(
  page: Page,
  section: MetadataInputSection
): Promise<void> {
  const expandable = page.locator(`#${section.id}`)
  await expandable.click()

  const listEntries = expandable.getByLabel("Listen Eintrag")
  const entryCount = await listEntries.count()

  async function deleteEntry(index: number): Promise<void> {
    return listEntries
      .nth(index)
      .getByRole("button", { name: "Eintrag löschen" })
      .click()
  }

  // Single entries are automatically in edit mode.
  if (entryCount == 1) {
    await page.keyboard.press("Enter") // Close edit mode first
    await deleteEntry(0)
  } else {
    // Delete backwards to avoid conflicts.
    for (let index = entryCount - 1; index >= 0; index--) {
      await deleteEntry(index)
    }
  }

  const finishButton = expandable.getByRole("button", { name: "Fertig" })
  await finishButton.click()
}

export async function fillRepeatedMetadataSectionList(
  page: Page,
  section: MetadataInputSection
): Promise<void> {
  if (section.isNotImported !== true) {
    await clearRepeatedMetadataSectionList(page, section)
  }

  const expandable = page.locator(`#${section.id}`)
  await expandable.click()

  const numberOfSectionRepetition = Math.max(
    ...(section.fields ?? []).map((field) => field.values?.length ?? 0)
  )

  if (numberOfSectionRepetition > 0) {
    // First entry is automatically in edit mode.
    await fillInputFieldGroup(page, section.fields ?? [], 0)
    await page.keyboard.press("Enter")
  }

  for (let index = 1; index < numberOfSectionRepetition; index++) {
    await expandable.getByRole("button", { name: "Weitere Angabe" }).click()
    await fillInputFieldGroup(page, section.fields ?? [], index)
    await page.keyboard.press("Enter")
  }

  const finishButton = expandable.getByRole("button", { name: "Fertig" })
  await finishButton.click()
}

export async function fillExpandableSectionNotRepeatable(
  page: Page,
  section: MetadataInputSection
): Promise<void> {
  if (!section.isNotImported) {
    await clearRepeatedMetadataSectionList(page, section)
  }

  const expandable = page.locator(`#${section.id}`)
  await expandable.click()

  await fillInputFieldGroup(page, section.fields ?? [], 0)

  const finishButton = expandable.getByRole("button", { name: "Fertig" })
  await finishButton.click()
}

export async function fillMetadataInputSection(
  page: Page,
  section: MetadataInputSection
): Promise<void> {
  if (section.isRepeatedSection) {
    await fillRepeatedMetadataSectionList(page, section)
  } else if (section.isSingleFieldSection) {
    await fillInputFieldGroup(page, section.fields ?? [])
  } else if (section.isExpandableNotRepeatable) {
    await fillExpandableSectionNotRepeatable(page, section)
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
