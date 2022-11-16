import { expect } from "@playwright/test"
import { Page } from "playwright"

export const openNorm = async (page: Page, longTitle: string, guid: string) => {
  await page.goto("/norms")
  await expect(page.getByText(longTitle).first()).toBeVisible()
  const locatorA = page.locator(`a[href*="/norms/norm/${guid}"]`)
  await expect(locatorA).toBeVisible()
  await locatorA.click()
}
