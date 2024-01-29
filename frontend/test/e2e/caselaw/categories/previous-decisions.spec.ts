import { expect } from "@playwright/test"
import { generateString } from "../../../test-helper/dataGenerators"
import {
  fillPreviousDecisionInputs,
  navigateToCategories,
  waitForSaving,
} from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

test.describe("previous decisions", () => {
  test("renders empty previous decision in edit mode, when none in list", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await expect(
      page.getByRole("heading", { name: "Vorgehende Entscheidung " }),
    ).toBeVisible()
    await expect(
      page.getByLabel("Gericht Vorgehende Entscheidung"),
    ).toBeVisible()
    await expect(
      page.getByLabel("Entscheidungsdatum Vorgehende Entscheidung"),
    ).toBeVisible()
    await expect(
      page.locator("[aria-label='Aktenzeichen Vorgehende Entscheidung']"),
    ).toBeVisible()
    await expect(
      page.getByLabel("Dokumenttyp Vorgehende Entscheidung"),
    ).toBeVisible()
    await expect(page.getByLabel("Datum unbekannt")).toBeVisible()
  })

  test("validates against required fields", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await navigateToCategories(page, documentNumber)

    await fillPreviousDecisionInputs(page, {
      fileNumber: "abc",
    })
    await page.getByLabel("Vorgehende Entscheidung speichern").click()

    await expect(page.getByLabel("Fehlerhafte Eingabe")).toBeVisible()
    await page
      .getByLabel("Vorgehende Entscheidung", { exact: true })
      .getByLabel("Listen Eintrag")
      .click()
    await expect(
      page
        .getByLabel("Vorgehende Entscheidung")
        .getByText("Pflichtfeld nicht befüllt"),
    ).toHaveCount(2)

    await fillPreviousDecisionInputs(page, {
      court: prefilledDocumentUnit.coreData.court?.label,
      fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
      documentType: prefilledDocumentUnit.coreData.documentType?.label,
      decisionDate: "01.01.2020",
    })
    await page.getByLabel("Vorgehende Entscheidung speichern").click()

    await expect(page.getByLabel("Fehlerhafte Eingabe")).toBeHidden()
  })

  test("adding empty previous decision not possible", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await expect(
      page.getByLabel("Vorgehende Entscheidung speichern"),
    ).toBeDisabled()
  })

  test("incomplete date input shows error message and does not persist", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page
      .locator("[aria-label='Entscheidungsdatum Vorgehende Entscheidung']")
      .fill("03")

    await page.keyboard.press("Tab")

    await expect(
      page.locator("[aria-label='Entscheidungsdatum Vorgehende Entscheidung']"),
    ).toHaveValue("03")

    await expect(page.locator("text=Unvollständiges Datum")).toBeVisible()

    await page.reload()

    await expect(
      page.locator("[aria-label='Entscheidungsdatum Vorgehende Entscheidung']"),
    ).toHaveValue("")
  })

  test("no date is displayed or sent if date known is false", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page
      .locator("[aria-label='Datum Unbekannt Vorgehende Entscheidung']")
      .click()

    await expect(
      page.locator("[aria-label='Entscheidungsdatum Vorgehende Entscheidung']"),
    ).toBeHidden()

    await page
      .locator("[aria-label='Datum Unbekannt Vorgehende Entscheidung']")
      .click()

    await page
      .locator("[aria-label='Entscheidungsdatum Vorgehende Entscheidung']")
      .isVisible()
  })

  test("deviating file number can be added and edited for manually added previous decisions", async ({
    page,
    documentNumber,
  }) => {
    const fileNumber = generateString()
    const deviatingFileNumber1 = generateString()
    const deviatingFileNumber2 = generateString()
    await navigateToCategories(page, documentNumber)

    await page
      .locator("[aria-label='Aktenzeichen Vorgehende Entscheidung']")
      .fill(fileNumber)
    // Knödel
    await page
      .locator(
        "[aria-label='Abweichendes Aktenzeichen Vorgehende Entscheidung anzeigen']",
      )
      .click()

    const container = page
      .locator("[aria-label='Vorgehende Entscheidung']")
      .first()
    await expect(
      container.locator("text=Abweichendes Aktenzeichen").first(),
    ).toBeVisible()

    await waitForSaving(
      async () => {
        await page
          .locator(
            "[aria-label='Abweichendes Aktenzeichen Vorgehende Entscheidung']",
          )
          .fill(deviatingFileNumber1)

        await page.getByLabel("Vorgehende Entscheidung speichern").click()
      },
      page,
      { clickSaveButton: true },
    )

    await page
      .getByLabel("Vorgehende Entscheidung", { exact: true })
      .getByLabel("Listen Eintrag")
      .click()
    await page
      .locator(
        "[aria-label='Abweichendes Aktenzeichen Vorgehende Entscheidung anzeigen']",
      )
      .click()
    // TODO why doesn't "await expect(page.getByText(deviatingFileNumber1)).toBeVisible()" work?
    await expect(
      page.locator(
        "[aria-label='Abweichendes Aktenzeichen Vorgehende Entscheidung']",
      ),
    ).toHaveValue(deviatingFileNumber1)

    await waitForSaving(
      async () => {
        await page
          .locator(
            "[aria-label='Abweichendes Aktenzeichen Vorgehende Entscheidung']",
          )
          .fill(deviatingFileNumber2)

        await page.getByLabel("Vorgehende Entscheidung speichern").click()
      },
      page,
      { clickSaveButton: true },
    )

    await page
      .getByLabel("Vorgehende Entscheidung", { exact: true })
      .getByLabel("Listen Eintrag")
      .click()
    await page
      .locator(
        "[aria-label='Abweichendes Aktenzeichen Vorgehende Entscheidung anzeigen']",
      )
      .click()
    // TODO why doesn't "await expect(page.getByText(deviatingFileNumber2)).toBeVisible()" work?
    await expect(
      page.locator(
        "[aria-label='Abweichendes Aktenzeichen Vorgehende Entscheidung']",
      ),
    ).toHaveValue(deviatingFileNumber2)
  })
})
