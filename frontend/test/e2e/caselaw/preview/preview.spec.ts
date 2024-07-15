import { expect } from "@playwright/test"
import {
  fillInput,
  navigateToCategories,
  navigateToPreview,
  waitForInputValue,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"

test.describe("preview", () => {
  test("display preview, check that fields are filled with values from categories", async ({
    page,
    prefilledDocumentUnit,
  }) => {
    await navigateToPreview(
      page,
      prefilledDocumentUnit.documentNumber as string,
    )

    const fileNumber = prefilledDocumentUnit.coreData.fileNumbers![0]
    await expect(page.getByText("AG Aachen")).toBeVisible()
    await expect(page.getByText(fileNumber)).toBeVisible()
    await expect(page.getByText("31.12.2019")).toBeVisible()
    await expect(page.getByText("1. Senat, 2. Kammer")).toBeVisible()
    await expect(page.getByText("Anerkenntnisurteil")).toBeVisible()
    await expect(page.getByText("Keine Angabe")).toBeVisible()
    await expect(page.getByText("RegionNW")).toBeVisible()
    await expect(page.getByText("guidingPrinciple")).toBeVisible()
    await expect(page.getByText("testHeadnote")).toBeVisible()
  })

  test("update fields in categories, check that update is reflected in preview", async ({
    page,
    prefilledDocumentUnit,
  }) => {
    await test.step("check that original values are displayed in preview", async () => {
      await navigateToPreview(
        page,
        prefilledDocumentUnit.documentNumber as string,
      )
      await expect(page.getByText("AG Aachen")).toBeVisible()
      await expect(page.getByText("31.12.2019")).toBeVisible()
      await expect(page.getByText("NW")).toBeVisible()
      await expect(page.getByText("guidingPrinciple")).toBeVisible()
    })

    await test.step("navigate to categories and update values", async () => {
      await navigateToCategories(
        page,
        prefilledDocumentUnit.documentNumber as string,
      )

      await page.locator("[aria-label='Gericht']").fill("BVerfG")
      await waitForInputValue(page, "[aria-label='Gericht']", "BVerfG")
      await expect(page.getByText("BVerfG")).toBeVisible()
      await page.getByText("BVerfG").click()
      await waitForInputValue(page, "[aria-label='Gericht']", "BVerfG")

      await fillInput(page, "Entscheidungsdatum", "01.01.2021")

      const inputField = page.locator("[data-testid='Leitsatz']")
      await inputField.click()
      await page.keyboard.type("some more text")

      await page.getByLabel("Speichern Button").click()
      await page.waitForEvent("requestfinished", { timeout: 5_000 })
    })

    await test.step("check updated values in preview", async () => {
      await navigateToPreview(
        page,
        prefilledDocumentUnit.documentNumber as string,
      )

      await expect(page.getByText("BVerfG")).toBeVisible()
      await expect(page.getByText("01.01.2021")).toBeVisible()
      await expect(page.getByText("DEU")).toBeVisible()
      await expect(page.getByText("some more text")).toBeVisible()
    })
  })

  test("delete fields in categories, check that deletion is reflected in preview", async ({
    page,
    prefilledDocumentUnit,
  }) => {
    await test.step("check that original values are displayed in preview", async () => {
      await navigateToPreview(
        page,
        prefilledDocumentUnit.documentNumber as string,
      )
      await expect(page.getByText("AG Aachen")).toBeVisible()
      await expect(page.getByText("31.12.2019")).toBeVisible()
      await expect(page.getByText("RegionNW")).toBeVisible()
      await expect(page.getByText("guidingPrinciple")).toBeVisible()
    })

    await test.step("navigate to categories and delete values", async () => {
      await navigateToCategories(
        page,
        prefilledDocumentUnit.documentNumber as string,
      )

      await fillInput(page, "Entscheidungsdatum", "")

      await page.locator("[aria-label='Aktenzeichen']").click()
      // Navigate back and delete on enter
      await page.keyboard.press("ArrowLeft")
      await page.keyboard.press("Enter")

      // Delete court entry
      await page.locator("[aria-label='Auswahl zurücksetzen']").first().click()

      await page.getByLabel("Speichern Button").click()
      await page.waitForEvent("requestfinished", { timeout: 5_000 })
    })

    await test.step("check deleted values are not in preview", async () => {
      await navigateToPreview(
        page,
        prefilledDocumentUnit.documentNumber as string,
      )

      await expect(page.getByText("Gericht")).toBeHidden()
      await expect(page.getByText("Entscheidungsdatum")).toBeHidden()
      await expect(page.getByText("Region")).toBeHidden()
      await expect(page.getByText("Aktenzeichen")).toBeHidden()
    })
  })
})
