import { expect } from "@playwright/test"
import {
  deleteDocumentUnit,
  documentUnitExists,
  fillProceedingDecisionInputs,
  navigateToCategories,
  toggleProceedingDecisionsSection,
} from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("Search proceeding decisions", () => {
  let testDocumentNumber: string

  //Create new documentunit as proceeding decision
  test.beforeEach(async ({ page }) => {
    await page.goto("/")
    await page.locator("button >> text=Neue Dokumentationseinheit").click()
    await page.waitForSelector("text=oder Datei auswählen")
    await expect(page).toHaveURL(
      /\/caselaw\/documentunit\/[A-Z0-9]{13}\/files$/
    )

    testDocumentNumber = /caselaw\/documentunit\/(.*)\/files/g.exec(
      page.url()
    )?.[1] as string

    //Fill test documentunit
    await navigateToCategories(page, testDocumentNumber)
    await expect(page.getByText(testDocumentNumber)).toBeVisible()

    await page.locator("[aria-label='Gericht']").fill("AG Aachen")
    await page.locator("text=AG Aachen").click()
    await page.locator("[aria-label='Stammdaten Speichern Button']").click()

    await page.locator("[aria-label='Aktenzeichen']").fill("12345678")
    await page.keyboard.press("Enter")
    await page
      .locator("[aria-label='Stammdaten Speichern Button']")
      .click({ timeout: 100000 })

    await page.locator("[aria-label='Dokumenttyp']").fill("AnU")
    await page.locator("text=AnU - Anerkenntnisurteil").click()
    await page.locator("[aria-label='Stammdaten Speichern Button']").click()
    await expect(
      page.locator("text=Zuletzt gespeichert um").first()
    ).toBeVisible()

    await expect(page.getByText("12345678").first()).toBeVisible()

    //Check if documentunit is available
    await page.goto("/")
    await expect(
      page.locator(
        `a[href*="/caselaw/documentunit/${testDocumentNumber}/files"]`
      )
    ).toBeVisible()
    await expect(
      page.locator(".table-row", {
        hasText: "12345678",
      })
    ).toBeVisible()
  })

  //Remove proceeding documentunit
  test.afterEach(async ({ page }) => {
    if (await documentUnitExists(page, testDocumentNumber))
      await deleteDocumentUnit(page, testDocumentNumber)
  })

  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("search for proceeding decision", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await expect(page.getByText(documentNumber)).toBeVisible()
    await toggleProceedingDecisionsSection(page)

    await fillProceedingDecisionInputs(page, {
      court: "AG Aachen",
      fileNumber: "12345678",
      documentType: "AnU",
    })

    await page
      .getByRole("button", { name: "Nach Entscheidungen suchen" })
      .click()

    await expect(page.getByText("Suchergebnis")).toBeVisible()

    await page
      .locator(".table-row", {
        hasText: "AG Aachen, AnU, 12345678",
      })
      .locator("[aria-label='Treffer übernehmen']")
      .click()

    await expect(page.getByText("Bereits hinzugefügt")).toBeVisible()

    await page.getByText("delete_outline").click()
    await expect(page.getByText("Bereits hinzugefügt")).toBeHidden()
  })
})
