import { expect } from "@playwright/test"
import { generateString } from "../../../../test-helper/dataGenerators"
import {
  deleteDocumentUnit,
  fillProceedingDecisionInputs,
  navigateToCategories,
  toggleProceedingDecisionsSection,
  waitForInputValue,
  waitForSaving,
} from "../../e2e-utils"
import { testWithDocumentUnit as test } from "../../fixtures"

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

    await expect(
      page.getByText("AG Aalen, AnU, 03.12.2004, 1a2b3c")
    ).toHaveCount(2)
  })

  test("search existing proceeding decisions", async ({
    page,
    documentNumber,
  }) => {
    const proceedingDocumentNumber =
      await test.step(`create documentunit as future proceeding decision`, async () => {
        await page.goto("/")
        await page.locator("button >> text=Neue Dokumentationseinheit").click()
        await page.waitForSelector("text=oder Datei auswählen")
        await expect(page).toHaveURL(
          /\/caselaw\/documentunit\/[A-Z0-9]{13}\/files$/
        )

        // Given the earlier expectation we can assume that the regex will match...
        const proceedingDocumentNumber =
          /caselaw\/documentunit\/(.*)\/files/g.exec(page.url())?.[1] as string

        await navigateToCategories(page, proceedingDocumentNumber)

        await waitForSaving(async () => {
          await page.locator("[aria-label='Gericht']").fill("BGH")
          await page.locator("text=BGH").click()
          await waitForInputValue(page, "[aria-label='Gericht']", "BGH")
        }, page)

        await page
          .locator("[aria-label='Entscheidungsdatum']")
          .fill("2020-12-03")
        expect(
          await page.locator("[aria-label='Entscheidungsdatum']").inputValue()
        ).toBe("2020-12-03")

        await waitForSaving(
          async () => {
            await page.locator("[aria-label='Aktenzeichen']").fill("abcde")
            await page.keyboard.press("Enter")
          },
          page,
          { clickSaveButton: true }
        )
        return proceedingDocumentNumber
      })

    await test.step(`find created documentunit in proceeding decisions`, async () => {
      await navigateToCategories(page, documentNumber)
      await page.locator("[aria-label='Entscheidungsdatum']").fill("2024-02-03")

      await navigateToCategories(page, documentNumber)
      await toggleProceedingDecisionsSection(page)
      await fillProceedingDecisionInputs(page, {
        court: "BGH",
        date: "2020-12-03",
        fileNumber: "abcde",
      })

      await page.getByText("Suchen").click()

      await expect(
        page.getByText("BGH, 03.12.2020, abcde, " + proceedingDocumentNumber, {
          exact: false,
        })
      ).toBeVisible()
    })

    await deleteDocumentUnit(page, proceedingDocumentNumber)
  })
})
