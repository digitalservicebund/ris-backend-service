import { expect } from "@playwright/test"
import {
  fillProceedingDecisionInputs,
  navigateToCategories,
  toggleProceedingDecisionsSection,
} from "~/e2e/caselaw/e2e-utils"
import { testWithDocumentUnit as test } from "~/e2e/caselaw/fixtures"

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

    const result = page.locator(".table-row", {
      hasText: `AG Aachen, AnU, ${secondaryDocumentUnit.coreData.fileNumbers?.[0]}`,
    })
    await expect(result).toBeVisible()
    await result.locator("[aria-label='Treffer übernehmen']").click()

    await expect(page.getByText("Bereits hinzugefügt")).toBeVisible()

    await page.getByText("delete_outline").click()
    await expect(page.getByText("Bereits hinzugefügt")).toBeHidden()
  })
})
