import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  fillInput,
  navigateToCategories,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe("preview decision", () => {
  test("display preview, check that fields are filled with values from categories", async ({
    page,
    prefilledDocumentUnit,
  }) => {
    await navigateToPreview(page, prefilledDocumentUnit.documentNumber)

    const fileNumber = prefilledDocumentUnit.coreData.fileNumbers![0]
    await expect(page.getByText("AG Aachen", { exact: true })).toBeVisible()
    await expect(page.getByText(fileNumber)).toBeVisible()
    await expect(page.getByText("31.12.2019")).toBeVisible()
    await expect(page.getByText("1. Senat, 2. Kammer")).toBeVisible()
    await expect(
      page.getByText("Anerkenntnisurteil", { exact: true }),
    ).toBeVisible()
    await expect(page.getByText("Keine Angabe")).toBeVisible()
    await expect(page.getByText("RegionNW")).toBeVisible()
    await expect(page.getByText("guidingPrinciple")).toBeVisible()
    await expect(page.getByText("testHeadnote")).toBeVisible()
    await expect(page.getByText("Quelle")).toBeVisible()
    await expect(page.getByText("A", { exact: true })).toBeVisible()
  })

  test("update fields in categories, check that update is reflected in preview", async ({
    page,
    prefilledDocumentUnit,
  }) => {
    await test.step("check that original values are displayed in preview", async () => {
      await navigateToPreview(page, prefilledDocumentUnit.documentNumber)
      await expect(page.getByText("AG Aachen", { exact: true })).toBeVisible()
      await expect(page.getByText("31.12.2019")).toBeVisible()
      await expect(page.getByText("NW", { exact: true })).toBeVisible()
      await expect(page.getByText("guidingPrinciple")).toBeVisible()
    })

    await test.step("navigate to categories and update values", async () => {
      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      await page.getByLabel("Gericht", { exact: true }).fill("BVerfG")
      await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
        "BVerfG",
      )
      await expect(page.getByText("BVerfG")).toBeVisible()
      await page.getByText("BVerfG").click()
      await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
        "BVerfG",
      )

      await fillInput(page, "Entscheidungsdatum", "01.01.2021")

      const inputField = page.getByTestId("Leitsatz")
      await inputField.click()
      await page.keyboard.type("some more text")

      await save(page)
    })

    await test.step("check updated values in preview", async () => {
      await navigateToPreview(page, prefilledDocumentUnit.documentNumber)

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
      await navigateToPreview(page, prefilledDocumentUnit.documentNumber)
      await expect(page.getByText("AG Aachen", { exact: true })).toBeVisible()
      await expect(page.getByText("31.12.2019")).toBeVisible()
      await expect(page.getByText("RegionNW")).toBeVisible()
      await expect(page.getByText("guidingPrinciple")).toBeVisible()
    })

    await test.step("navigate to categories and delete values", async () => {
      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      await page.getByLabel("Entscheidungsdatum", { exact: true }).fill("")

      await page.getByLabel("Aktenzeichen", { exact: true }).click()
      // Navigate back and delete on enter
      await page.keyboard.press("ArrowLeft")
      await page.keyboard.press("Enter")

      // Delete court entry
      await page
        .getByLabel("Auswahl zurücksetzen", { exact: true })
        .first()
        .click()

      await save(page)
    })

    await test.step("check deleted values are not in preview", async () => {
      await navigateToPreview(page, prefilledDocumentUnit.documentNumber)

      await expect(page.getByText("Gericht")).toBeHidden()
      await expect(page.getByText("Entscheidungsdatum")).toBeHidden()
      await expect(page.getByText("Region")).toBeHidden()
      await expect(page.getByText("Aktenzeichen")).toBeHidden()
    })
  })
})
