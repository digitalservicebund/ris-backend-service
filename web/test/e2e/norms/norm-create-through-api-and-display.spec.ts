import { expect } from "@playwright/test"

import { testWithImportedNorm } from "./fixtures"
import normCleanCars from "./testdata/norm_clean_cars.json"

testWithImportedNorm("Check display of norm", async ({ page, createdGuid }) => {
  await page.goto("/norms")
  await expect(page.getByText(normCleanCars.longTitle).first()).toBeVisible()
  const locatorA = page.locator(`a[href*="/norms/norm/${createdGuid}"]`)
  await expect(locatorA).toBeVisible()
  await locatorA.click()
  await expect(locatorA).not.toBeVisible()

  await expect(page).toHaveURL(`/norms/norm/${createdGuid}`)
  await expect(page.getByText(normCleanCars.longTitle)).toBeVisible()
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
})
