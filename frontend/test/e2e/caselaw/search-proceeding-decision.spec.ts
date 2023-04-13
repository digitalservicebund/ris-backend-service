import { expect } from "@playwright/test"
import {
  fillProceedingDecisionInputs,
  navigateToCategories,
  toggleProceedingDecisionsSection,
} from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("Search proceeding decisions", () => {
  test("search for existing proceeding decision and add", async ({
    page,
    documentNumber,
    secondaryDocumentUnit,
  }) => {
    await navigateToCategories(page, documentNumber)
    await expect(page.getByText(documentNumber)).toBeVisible()
    await toggleProceedingDecisionsSection(page)

    await fillProceedingDecisionInputs(page, {
      court: secondaryDocumentUnit.coreData.court?.label,
      fileNumber: secondaryDocumentUnit.coreData.fileNumbers?.[0],
      documentType: secondaryDocumentUnit.coreData.documentType?.jurisShortcut,
    })

    await page
      .getByRole("button", { name: "Nach Entscheidungen suchen" })
      .click()

    await expect(page.getByText("Suchergebnis")).toBeVisible()

    await expect(
      page.getByText("AG Aachen, AnU, fooAktenzeichen")
    ).toBeVisible()

    await page
      .locator(".table-row", {
        hasText: "AG Aachen, AnU, fooAktenzeichen",
      })
      .locator("[aria-label='Treffer übernehmen']")
      .click()

    await expect(page.getByText("Bereits hinzugefügt")).toBeVisible()

    await page.getByText("delete_outline").click()
    await expect(page.getByText("Bereits hinzugefügt")).toBeHidden()
  })
})
