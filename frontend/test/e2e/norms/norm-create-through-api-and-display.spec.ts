import { expect } from "@playwright/test"

import { openNorm } from "./e2e-utils"
import { getNormBySections, testWithImportedNorm } from "./fixtures"

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
    if (field.type === "repeated") {
      for (const value of field.value) {
        await expect(
          await page.locator(`div.label-wrapper:text-is("${value}")`)
        ).toBeVisible()
      }
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
  async ({ page, normData, guid }) => {
    await openNorm(page, normData["officialLongTitle"], guid)

    await expect(page).toHaveURL(`/norms/norm/${guid}`)
    await expect(page.getByText(normData["officialLongTitle"])).toBeVisible()

    for (const article of Object.values(normData["articles"])) {
      await expect(
        page.getByText(article["marker"], { exact: true })
      ).toBeVisible()
      await expect(
        page.getByText(article["title"], { exact: true })
      ).toBeVisible()
      for (const paragraph of Object.values(article["paragraphs"])) {
        if (paragraph["marker"] === undefined) {
          await expect(page.getByText(paragraph["text"])).toBeVisible()
        } else {
          await expect(
            page.getByText(paragraph["marker"] + " " + paragraph["text"])
          ).toBeVisible()
        }
      }
    }
  }
)

testWithImportedNorm.skip(
  "Check if frame fields are correctly displayed",
  async ({ page, normData, guid }) => {
    await openNorm(page, normData["officialLongTitle"], guid)

    // Outer menu
    await expect(page.locator("a:has-text('Normenkomplex')")).toBeVisible()
    await expect(page.locator("a:has-text('Bestand')")).toBeVisible()
    await expect(page.locator("a:has-text('Export')")).toBeVisible()
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

testWithImportedNorm.skip(
  "Check if switching frame sections affects sections being inside or outside viewport",
  async ({ page, normData, guid }) => {
    await openNorm(page, normData["officialLongTitle"], guid)

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
