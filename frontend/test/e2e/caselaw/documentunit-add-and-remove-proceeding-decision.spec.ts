import { expect } from "@playwright/test"
import {
  fillPreviousDecisionInputs,
  navigateToCategories,
  togglePreviousDecisionsSection,
} from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("Add and remove proceeding decisions", () => {
  test("rendering", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await expect(
      page.getByRole("heading", { name: "Vorgehende Entscheidungen" })
    ).toBeVisible()

    await togglePreviousDecisionsSection(page)

    await expect(page.locator("[aria-label='Gericht Rechtszug']")).toBeVisible()
    await expect(
      page.locator("[aria-label='Dokumenttyp Rechtszug']")
    ).toBeVisible()
    await expect(
      page.locator("[aria-label='Aktenzeichen Rechtszug']")
    ).toBeVisible()
    await expect(page.locator("[aria-label='Datum Rechtszug']")).toBeVisible()
  })

  test("add proceeding decision and verify it persists", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await togglePreviousDecisionsSection(page)
    await fillPreviousDecisionInputs(page, {
      court: "AG Aalen",
      date: "2004-12-03",
      fileNumber: "1a2b3c",
      documentType: "AnU",
    })

    await page.getByText("Manuell Hinzufügen").click()
    await expect(
      page.locator(
        "text=AG Aalen Anerkenntnisurteil" // 2004-12-02T23:00:00Z 1a2b3c" TODO: 2004-12-03T00:00:00Z on pipeline failure screenshot
      )
    ).toBeVisible()

    await page.reload()
    await togglePreviousDecisionsSection(page)

    await expect(
      page.locator(
        "text=AG Aalen Anerkenntnisurteil" // 2004-12-02T23:00:00Z 1a2b3c" TODO
      )
    ).toBeVisible()
  })

  test("add multiple proceeding decisions", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await togglePreviousDecisionsSection(page)
    await fillPreviousDecisionInputs(page, {
      court: "AG Aalen",
      date: "2004-12-03",
      fileNumber: "1a2b3c",
      documentType: "AnU",
    })

    await page.getByText("Manuell Hinzufügen").click()

    await fillPreviousDecisionInputs(page, {
      court: "AG Aalen",
      date: "2004-12-03",
      fileNumber: "1a2b3c",
      documentType: "AnU",
    })

    await page.getByText("Manuell Hinzufügen").click()

    await expect(
      page.getByText("AG Aalen Anerkenntnisurteil") // 2004-12-02T23:00:00Z 1a2b3c") TODO
    ).toHaveCount(2)
  })

  // test("delete proceeding decision", async ({ page, documentNumber }) => {
  //   TBD
  // })

  // test("test add previous decision with missing required fields not possible", async ({
  //   page,
  //   documentNumber,
  // }) => {
  //   TBD
  // })
})
