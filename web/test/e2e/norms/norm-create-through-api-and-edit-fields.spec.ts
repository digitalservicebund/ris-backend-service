import { expect } from "@playwright/test"

import { openNorm } from "./e2e-utils"
import { getNormBySections, testWithImportedNorm } from "./fixtures"
import normCleanCars from "./testdata/norm_clean_cars.json"
import newNorm from "./testdata/norm_edited_fields.json"

const sections = getNormBySections(normCleanCars)

async function fillCheckbox(page, field, value) {
  const selector = `role=checkbox[name="${field.label}"]`
  expect(await page.isChecked(selector)).toBe(field.value ?? false)
  await page.setChecked(selector, value)
}

async function fillTextInput(page, field, value) {
  const selector = `input#${field.name}`
  expect(await page.inputValue(selector)).toBe(field.value ?? "")
  const locator = page.locator(selector)
  await expect(locator).toBeEditable()
  await locator.fill(value)
}

async function editFields(page, fields, data) {
  for (const field of fields) {
    await expect(page.locator(`label[for="${field.name}"]`)).toBeVisible()
    if (field.isCheckbox) {
      await fillCheckbox(page, field, data[field.name])
    } else {
      await fillTextInput(page, field, data[field.name])
    }
  }
}

async function expectUpdatedFields(page, fields, data) {
  for (const field of fields) {
    if (field.isCheckbox) {
      const checkbox = `role=checkbox[name="${field.label}"]`
      expect(await page.isChecked(checkbox)).toBe(data[field.name])
    } else {
      const input = `input#${field.name}`
      await expect(page.locator(input)).toBeVisible()
      expect(await page.inputValue(input)).toBe(data[field.name])
    }
  }
}

testWithImportedNorm(
  "Check if fields can be edited",
  async ({ page, createdGuid }) => {
    await openNorm(page, normCleanCars.officialLongTitle, createdGuid)

    const locatorFrameButton = page.locator("a:has-text('Rahmen')")
    await expect(locatorFrameButton).toBeVisible()
    await locatorFrameButton.click()
    await expect(page).toHaveURL(`/norms/norm/${createdGuid}/frame`)
    const locatorHeadingsButton = page.locator(
      "#headingsAndAbbreviationsUnofficial"
    )
    await expect(locatorHeadingsButton).toBeVisible()
    await locatorHeadingsButton.click()

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
      if (section.heading === "Überschriften und Abkürzungen") {
        break
      }
      await expectUpdatedFields(page, section.fields ?? [], newNorm)
      for (const subSection of section.sections ?? []) {
        await expectUpdatedFields(page, subSection.fields ?? [], newNorm)
      }
    }
  }
)
