import { expect } from "@playwright/test"
import {
  deleteDocumentUnit,
  fillProceedingDecisionInputs,
  navigateToCategories,
  toggleProceedingDecisionsSection,
  waitForSaving,
} from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("Add and remove proceeding decisions", () => {
  test("rendering", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await expect(
      page.getByRole("heading", { name: "Vorgehende Entscheidungen" })
    ).toBeVisible()

    await toggleProceedingDecisionsSection(page)

    await expect(page.locator("[aria-label='Gericht Rechtszug']")).toBeVisible()
    await expect(
      page.locator("[aria-label='Dokumenttyp Rechtszug']")
    ).toBeVisible()
    await expect(
      page.locator("[aria-label='Aktenzeichen Rechtszug']")
    ).toBeVisible()
    await expect(
      page.locator("[aria-label='Entscheidungsdatum Rechtszug']")
    ).toBeVisible()
  })

  test("add proceeding decision manually and verify it persists", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleProceedingDecisionsSection(page)
    await fillProceedingDecisionInputs(page, {
      court: "AG Aalen",
      date: "2004-12-03",
      fileNumber: "1a2b3c",
      documentType: "AnU",
    })

    await page.getByText("Manuell Hinzufügen").click()
    await expect(
      page.getByText("AG Aalen, AnU, 03.12.2004, 1a2b3c")
    ).toBeVisible()

    await page.reload()
    await toggleProceedingDecisionsSection(page)

    await expect(
      page.getByText("AG Aalen, AnU, 03.12.2004, 1a2b3c")
    ).toBeVisible()
  })

  test("search for proceeding decision", async ({ page, documentNumber }) => {
    //Create new test documentunit
    await page.goto("/")
    await page.locator("button >> text=Neue Dokumentationseinheit").click()
    await page.waitForSelector("text=oder Datei auswählen")
    await expect(page).toHaveURL(
      /\/caselaw\/documentunit\/[A-Z0-9]{13}\/files$/
    )

    const testDocumentNumber = /caselaw\/documentunit\/(.*)\/files/g.exec(
      page.url()
    )?.[1] as string

    //Fill test documentunit
    await navigateToCategories(page, testDocumentNumber)
    await expect(page.getByText(testDocumentNumber)).toBeVisible()

    await page.locator("[aria-label='Gericht']").fill("AG Aachen")
    await page.locator("text=AG Aachen").click()

    await page.locator("[aria-label='Aktenzeichen']").fill("12345678")
    await page.keyboard.press("Enter")

    await page.locator("[aria-label='Dokumenttyp']").fill("AnU")
    await page.locator("text=AnU - Anerkenntnisurteil").click()
    await page.locator("[aria-label='Stammdaten Speichern Button']").click()
    await waitForSaving(page)

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

    const resultItem = page
      .locator(".table-row", {
        hasText: "AG Aachen, AnU, 12345678",
      })
      .first()

    await expect(page.getByText("Suchergebnis")).toBeVisible()

    await expect(resultItem).toBeVisible()

    await resultItem.locator("[aria-label='Treffer übernehmen']").click()

    await deleteDocumentUnit(page, testDocumentNumber)
  })

  test("add multiple proceeding decisions", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleProceedingDecisionsSection(page)
    await fillProceedingDecisionInputs(page, {
      court: "AG Aalen",
      date: "2004-12-03",
      fileNumber: "1a2b3c",
      documentType: "AnU",
    })

    await page.getByText("Manuell Hinzufügen").click()

    await fillProceedingDecisionInputs(page, {
      court: "AG Aalen",
      date: "2004-12-03",
      fileNumber: "1a2b3c",
      documentType: "AnU",
    })

    await page.getByText("Manuell Hinzufügen").click()

    await expect(
      page.getByText("AG Aalen, AnU, 03.12.2004, 1a2b3c")
    ).toHaveCount(2)
  })

  test("remove proceeding decision", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await toggleProceedingDecisionsSection(page)
    await fillProceedingDecisionInputs(page, {
      court: "AG Aalen",
      date: "2004-12-03",
      fileNumber: "1a2b3c",
      documentType: "AnU",
    })

    await page.getByText("Manuell Hinzufügen").click()

    await expect(
      page.getByText("AG Aalen, AnU, 03.12.2004, 1a2b3c")
    ).toHaveCount(1)

    await page
      .locator("li", { hasText: "AG Aalen" })
      .getByLabel("Löschen")
      .click()

    await expect(
      page.getByText("AG Aalen, AnU, 03.12.2004, 1a2b3c")
    ).toHaveCount(0)

    page.reload()
    await toggleProceedingDecisionsSection(page)

    await expect(
      page.getByText("AG Aalen, AnU, 03.12.2004, 1a2b3c")
    ).toHaveCount(0)
  })

  // test("test add proceeding decision with missing required fields not possible", async ({
  //   page,
  //   documentNumber,
  // }) => {
  //   TBD
  // })
})
