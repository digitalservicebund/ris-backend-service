import { expect } from "@playwright/test"

import { fillRepeatedInput, fillTextInput, openNorm } from "./e2e-utils"
import { getNormBySections, testWithImportedNorm } from "./fixtures"
import { newNorm } from "./testdata/norm_edited_fields"

async function fillCheckbox(page, field, value) {
  const selector = `role=checkbox[name="${field.label}"]`
  expect(await page.isChecked(selector)).toBe(field.value ?? false)
  await page.setChecked(selector, value)
}

async function fillDropdown(page, field, value) {
  const selector = `input#${field.name}`
  expect(await page.inputValue(selector)).toBe(field.value ?? "")
  const locatorInput = page.locator(selector + "+ button")
  await locatorInput.click()
  const locatorDropdownOptions = await page.locator(
    '[aria-label="dropdown-option"]'
  )

  const count = await locatorDropdownOptions.count()
  for (let i = 0; i < count; i++) {
    const locatorOption = locatorDropdownOptions.nth(i)
    if ((await locatorOption.innerText()) === value) {
      await expect(locatorOption).toBeVisible()
      await locatorOption.click()
      break
    }
  }
}

async function editFields(page, fields, data) {
  for (const field of fields) {
    await expect(page.locator(`label[for="${field.name}"]`)).toBeVisible()
    if (field.type === "checkbox") {
      await fillCheckbox(page, field, data[field.name])
    }
    if (field.type === "text") {
      await fillTextInput(page, field, data[field.name])
    }
    if (field.type === "dropdown") {
      await fillDropdown(page, field, data[field.name])
    }
    if (field.type === "repeated") {
      await fillRepeatedInput(
        page,
        field,
        data["metadata"].filter(
          (metaDatum) => metaDatum.type === field.selector
        )
      )
    }
  }
}

async function expectUpdatedFields(page, fields, data) {
  for (const field of fields) {
    if (field.type === "checkbox") {
      const checkbox = `role=checkbox[name="${field.label}"]`
      expect(await page.isChecked(checkbox)).toBe(data[field.name])
    }
    if (field.type === "text") {
      const input = `input#${field.name}`
      await expect(page.locator(input)).toBeVisible()
      expect(await page.inputValue(input)).toBe(data[field.name])
    }
    if (field.type === "dropdown") {
      const input = `input#${field.name}`
      await expect(page.locator(input)).toBeVisible()
      expect(await page.inputValue(input)).toBe(data[field.name])
    }
  }
}

testWithImportedNorm.skip(
  "Check if fields can be edited",
  async ({ page, normData, guid }) => {
    await openNorm(page, normData["officialLongTitle"], guid)

    const locatorFrameButton = page.locator("a:has-text('Rahmen')")
    await expect(locatorFrameButton).toBeVisible()
    await locatorFrameButton.click()
    await expect(page).toHaveURL(`/norms/norm/${guid}/frame`)
    const locatorHeadingsButton = page.locator(
      "#headingsAndAbbreviationsUnofficial"
    )
    await expect(locatorHeadingsButton).toBeVisible()
    await locatorHeadingsButton.click()

    const sections = getNormBySections(normData)
    // Update all norm fields
    for (const section of sections) {
      await editFields(page, section.fields ?? [], newNorm)
      for (const subSection of section.sections ?? []) {
        await editFields(page, subSection.fields ?? [], newNorm)
      }
    }

    await page.locator("[aria-label='Rahmendaten Speichern Button']").click()
    await expect(
      page.locator("text=Zuletzt gespeichert um").first()
    ).toBeVisible()
    await page.reload()
    await expect(locatorHeadingsButton).toBeVisible()
    await locatorHeadingsButton.click()

    for (const section of sections) {
      await expectUpdatedFields(page, section.fields ?? [], newNorm)
      for (const subSection of section.sections ?? []) {
        await expectUpdatedFields(page, subSection.fields ?? [], newNorm)
      }
    }
  }
)
