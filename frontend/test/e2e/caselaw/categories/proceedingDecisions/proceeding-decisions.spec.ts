import { expect } from "@playwright/test"
import {
  checkIfProceedingDecisionCleared,
  fillProceedingDecisionInputs,
  navigateToCategories,
  toggleProceedingDecisionsSection,
} from "~/e2e/caselaw/e2e-utils"
import { testWithDocumentUnit as test } from "~/e2e/caselaw/fixtures"
import { generateString } from "~/test-helper/dataGenerators"

test.describe("Proceeding decisions", () => {
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

  test("add and delete proceeding decision", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleProceedingDecisionsSection(page)

    const fileNumber = generateString()

    await fillProceedingDecisionInputs(page, {
      court: "AG Aalen",
      date: "2004-12-03",
      fileNumber: fileNumber,
      documentType: "AnU",
    })

    await page.getByText("Manuell Hinzufügen").click()

    await checkIfProceedingDecisionCleared(page)

    await expect(
      page.getByText(`AG Aalen, AnU, 03.12.2004, ${fileNumber}`, {
        exact: true,
      })
    ).toBeVisible()

    await page.reload()
    await toggleProceedingDecisionsSection(page)

    await expect(
      page.getByText(`AG Aalen, AnU, 03.12.2004, ${fileNumber}`)
    ).toHaveCount(1)

    // delete proceedingDecision
    await page
      .locator("div", { hasText: "AG Aalen" })
      .getByLabel("Löschen")
      .click()

    await expect(
      page.getByText(`AG Aalen, AnU, 03.12.2004, ${fileNumber}`)
    ).toHaveCount(0)

    page.reload()
    await toggleProceedingDecisionsSection(page)

    await expect(
      page.getByText(`AG Aalen, AnU, 03.12.2004, ${fileNumber}`)
    ).toHaveCount(0)
  })

  test("add same proceeding decision twice", async ({
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

    await checkIfProceedingDecisionCleared(page)

    await expect(
      page.getByText("AG Aalen, AnU, 03.12.2004, 1a2b3c", { exact: true })
    ).toBeVisible()

    await fillProceedingDecisionInputs(page, {
      court: "AG Aalen",
      date: "2004-12-03",
      fileNumber: "1a2b3c",
      documentType: "AnU",
    })

    await page.getByText("Manuell Hinzufügen").click()

    await checkIfProceedingDecisionCleared(page)

    await expect(
      page.getByText("AG Aalen, AnU, 03.12.2004, 1a2b3c")
    ).toHaveCount(2)
  })

  test("add proceeding decision with unknown date", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleProceedingDecisionsSection(page)

    const fileNumber = generateString()

    await test.step("create decision with normal date", async () =>
      await fillProceedingDecisionInputs(page, {
        court: "AG Aalen",
        fileNumber: fileNumber,
        date: "2004-12-03",
        documentType: "AnU",
        dateUnknown: true,
      }))

    await page.getByText("Manuell Hinzufügen").click()

    await checkIfProceedingDecisionCleared(page)

    await expect(
      page.getByText(
        `AG Aalen, AnU, unbekanntes Entscheidungsdatum, ${fileNumber}`,
        {
          exact: true,
        }
      )
    ).toBeVisible()

    await page.reload()
    await toggleProceedingDecisionsSection(page)

    await expect(
      page.getByText(
        `AG Aalen, AnU, unbekanntes Entscheidungsdatum, ${fileNumber}`
      )
    ).toHaveCount(1)
  })
})
