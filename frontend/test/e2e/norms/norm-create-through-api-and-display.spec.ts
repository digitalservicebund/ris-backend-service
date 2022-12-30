import { expect } from "@playwright/test"

import { openNorm } from "./e2e-utils"
import { getNormBySections, testWithImportedNorm } from "./fixtures"
import normCleanCars from "./testdata/norm_clean_cars.json"

const sections = getNormBySections(normCleanCars)

async function expectInputFields(page, fields) {
  for (const field of fields) {
    await expect(
      page.locator(`label:text-is("${field.label}")`).first()
    ).toBeVisible()
    if (field.type === "checkbox") {
      expect(await page.isChecked(`role=checkbox[name="${field.label}"]`)).toBe(
        field.value ?? false
      )
    }
    if (field.type === "text") {
      expect(await page.inputValue(`input#${field.name}`)).toBe(
        field.value ?? ""
      )
    }
    // TODO Check the dropdown data
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
  async ({ page, createdGuid }) => {
    await openNorm(page, normCleanCars.officialLongTitle, createdGuid)

    await expect(page).toHaveURL(`/norms/norm/${createdGuid}`)
    await expect(page.getByText(normCleanCars.officialLongTitle)).toBeVisible()
    await expect(page.getByText(normCleanCars.articles[0].marker)).toBeVisible()
    await expect(page.getByText(normCleanCars.articles[0].title)).toBeVisible()
    await expect(
      page.getByText(normCleanCars.articles[0].paragraphs[0].marker)
    ).toBeVisible()
    await expect(
      page.getByText(normCleanCars.articles[0].paragraphs[0].text)
    ).toBeVisible()
    await expect(
      page.getByText(normCleanCars.articles[0].paragraphs[1].marker)
    ).toBeVisible()
    await expect(
      page.getByText(normCleanCars.articles[0].paragraphs[1].text)
    ).toBeVisible()
  }
)

testWithImportedNorm(
  "Check if frame fields are correctly displayed",
  async ({ page, createdGuid }) => {
    await openNorm(page, normCleanCars.officialLongTitle, createdGuid)

    // Outer menu
    await expect(page.locator("a:has-text('Normenkomplex')")).toBeVisible()
    await expect(page.locator("a:has-text('Bestand')")).toBeVisible()
    await expect(page.locator("a:has-text('Abgabe')")).toBeVisible()
    const locatorFrameButton = page.locator("a:has-text('Rahmen')")
    await expect(locatorFrameButton).toBeVisible()

    await locatorFrameButton.click()
    await expect(page).toHaveURL(`/norms/norm/${createdGuid}/frame`)
    const locatorHeadingsButton = page.locator(
      "#headingsAndAbbreviationsUnofficial"
    )
    await expect(locatorHeadingsButton).toBeVisible()
    await locatorHeadingsButton.click()

    for (const section of sections) {
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
      await expectInputFields(page, section.fields ?? [])
      for (const subSection of section.sections ?? []) {
        await expect(
          page.locator(`legend:text-is("${subSection.heading}")`).first()
        ).toBeVisible()
        await expectInputFields(page, subSection.fields ?? [])
      }
    }
  }
)

testWithImportedNorm(
  "Check if switching frame sections affects sections being inside or outside viewport",
  async ({ page, createdGuid }) => {
    await openNorm(page, normCleanCars.officialLongTitle, createdGuid)

    const locatorFrameButton = page.locator("a:has-text('Rahmen')")
    await expect(locatorFrameButton).toBeVisible()
    await locatorFrameButton.click()
    await expect(page).toHaveURL(`/norms/norm/${createdGuid}/frame`)
    await expect(page).toHaveInsideViewport(
      'legend:text-is("Allgemeine Angaben")'
    )

    for (const section of sections) {
      await expectHeadingAppearAfterScroll(page, section.heading)
    }
  }
)
