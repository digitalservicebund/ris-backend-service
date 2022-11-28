import { expect } from "@playwright/test"

import { openNorm } from "./e2e-utils"
import { testWithImportedNorm } from "./fixtures"
import normCleanCars from "./testdata/norm_clean_cars.json"

testWithImportedNorm.skip(
  "Check if long title can be edited",
  async ({ page, createdGuid }) => {
    await openNorm(page, normCleanCars.longTitle, createdGuid)

    const locatorFrameButton = page.locator("a:has-text('Rahmen')")
    await expect(locatorFrameButton).toBeVisible()
    await locatorFrameButton.click()

    await expect(page).toHaveURL(`/norms/norm/${createdGuid}/frame`)

    await expect(page.locator("h1:has-text('Überschriften')")).toBeVisible()
    await expect(
      page.locator("label:has-text('Amtliche Langüberschrift')")
    ).toBeVisible()
    const selectorInputLongTitle = "[aria-label='Amtliche Langüberschrift']"

    expect(await page.inputValue(selectorInputLongTitle)).toBe(
      normCleanCars.longTitle
    )
    const locatorInputLongTitle = page.locator(selectorInputLongTitle)
    await expect(locatorInputLongTitle).toBeEditable()
    await locatorInputLongTitle.fill("abc")

    await page.locator("[aria-label='Rahmendaten Speichern Button']").click()
    await expect(
      page.locator("text=Zuletzt gespeichert um").first()
    ).toBeVisible()
    await page.reload()
    expect(await page.inputValue(selectorInputLongTitle)).toBe("abc")
  }
)
