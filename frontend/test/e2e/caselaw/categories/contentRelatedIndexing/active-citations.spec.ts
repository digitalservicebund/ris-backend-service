import { expect } from "@playwright/test"
import {
  fillActiveCitationInputs,
  navigateToCategories,
} from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

test.describe("active citations", () => {
  test("renders empty active citation in edit mode, when no activeCitations in list", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await expect(
      page.getByRole("heading", { name: "Aktivzitierung" }),
    ).toBeVisible()
    await expect(page.getByLabel("Art der Zitierung")).toBeVisible()
    await expect(page.getByLabel("Gericht der Aktivzitierung")).toBeVisible()
    await expect(
      page.getByLabel("Entscheidungsdatum der Aktivzitierung"),
    ).toBeVisible()
    await expect(page.getByLabel("Aktenzeichen Aktivzitierung")).toBeVisible()
    await expect(
      page.getByLabel("Dokumenttyp der Aktivzitierung"),
    ).toBeVisible()
  })

  test("validates against required fields", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await navigateToCategories(page, documentNumber)

    await fillActiveCitationInputs(page, {
      fileNumber: "abc",
    })
    await page.getByLabel("Aktivzitierung speichern").click()

    await expect(page.getByLabel("Fehlerhafte Eingabe")).toBeVisible()
    await page
      .getByLabel("Aktivzitierung", { exact: true })
      .getByLabel("Listen Eintrag")
      .click()
    await expect(
      page.getByLabel("Aktivzitierung").getByText("Pflichtfeld nicht befüllt"),
    ).toHaveCount(3)

    await fillActiveCitationInputs(page, {
      citationType: "Änderung",
      court: prefilledDocumentUnit.coreData.court?.label,
      fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
      documentType: prefilledDocumentUnit.coreData.documentType?.label,
      decisionDate: "31.12.2019",
    })
    await page.getByLabel("Aktivzitierung speichern").click()

    await expect(page.getByLabel("Fehlerhafte Eingabe")).toBeHidden()
  })

  test("incomplete date input shows error message and does not persist", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page
      .locator("[aria-label='Entscheidungsdatum der Aktivzitierung']")
      .fill("03")

    await page.keyboard.press("Tab")

    await expect(
      page.locator("[aria-label='Entscheidungsdatum der Aktivzitierung']"),
    ).toHaveValue("03")

    await expect(page.locator("text=Unvollständiges Datum")).toBeVisible()

    await page.reload()

    await expect(
      page.locator("[aria-label='Entscheidungsdatum der Aktivzitierung']"),
    ).toHaveValue("")
  })
})
