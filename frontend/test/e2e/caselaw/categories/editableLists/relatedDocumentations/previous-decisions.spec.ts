import { expect } from "@playwright/test"
import {
  navigateToCategories,
  waitForSaving,
  publishDocumentationUnit,
  fillPreviousDecisionInputs,
} from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { generateString } from "~/test-helper/dataGenerators"

test.describe("previous decisions", () => {
  test("renders all fields", async ({ page, documentNumber }) => {
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
      page.getByLabel("Aktenzeichen Vorgehende Entscheidung", { exact: true }),
    ).toBeVisible()
    await expect(
      page.getByLabel(
        "Abweichendes Aktenzeichen Vorgehende Entscheidung anzeigen",
      ),
    ).toBeVisible()
    await expect(
      page.getByLabel("Dokumenttyp Vorgehende Entscheidung"),
    ).toBeVisible()
    await expect(page.getByLabel("Datum unbekannt")).toBeVisible()
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

    await page
      .locator(
        "[aria-label='Abweichendes Aktenzeichen Vorgehende Entscheidung anzeigen']",
      )
      .click()

    const container = page
      .locator("[aria-label='Vorgehende Entscheidung']")
      .first()
    await expect(
      container.locator("text=Abweichendes Aktenzeichen Vorinstanz").first(),
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
      .first()
      .click()
    await page
      .locator(
        "[aria-label='Abweichendes Aktenzeichen Vorgehende Entscheidung anzeigen']",
      )
      .click()
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
      .first()
      .click()
    // if 'Abweichendes Aktenzeichen' input filled, the nested input is expanded
    await page
      .locator(
        "[aria-label='Abweichendes Aktenzeichen Vorgehende Entscheidung anzeigen']",
      )
      .click()
    await expect(
      page.locator(
        "[aria-label='Abweichendes Aktenzeichen Vorgehende Entscheidung']",
      ),
    ).toHaveValue(deviatingFileNumber2)
  })

  test("only deviating file number of linked previous decision is editable", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await publishDocumentationUnit(
      page,
      prefilledDocumentUnit.documentNumber || "",
    )
    await navigateToCategories(page, documentNumber)

    await fillPreviousDecisionInputs(page, {
      court: prefilledDocumentUnit.coreData.court?.label,
      fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
      documentType: prefilledDocumentUnit.coreData.documentType?.label,
      decisionDate: "31.12.2019",
    })
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    await previousDecisionContainer
      .getByLabel("Nach Entscheidung suchen")
      .click()

    await expect(page.getByText("1 Ergebnis gefunden")).toBeVisible()

    const result = page.getByText(
      `AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil, ${prefilledDocumentUnit.documentNumber}`,
    )

    await expect(result).toBeVisible()
    await page.getByLabel("Treffer übernehmen").click()

    await expect(
      page.getByText(
        `AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`,
      ),
    ).toBeVisible()

    await page
      .getByText(
        `AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`,
      )
      .click()
    await expect(
      page.getByLabel("Gericht Vorgehende Entscheidung"),
    ).not.toBeEditable()
    await expect(
      page.getByLabel("Entscheidungsdatum Vorgehende Entscheidung"),
    ).not.toBeEditable()
    await expect(
      page.getByLabel("Aktenzeichen Vorgehende Entscheidung", { exact: true }),
    ).not.toBeEditable()
    await expect(
      page.getByLabel("Dokumenttyp Vorgehende Entscheidung"),
    ).not.toBeEditable()
    await expect(page.getByLabel("Datum unbekannt")).not.toBeEditable()

    const deviatingFileNumber1 = generateString()
    await waitForSaving(
      async () => {
        await page
          .getByLabel("Abweichendes Aktenzeichen Vorgehende Entscheidung", {
            exact: true,
          })
          .fill(deviatingFileNumber1)

        await page.getByLabel("Vorgehende Entscheidung speichern").click()
      },
      page,
      { clickSaveButton: true },
    )
    await expect(
      page.getByText(
        `AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, ${deviatingFileNumber1}, Anerkenntnisurteil`,
      ),
    ).toBeVisible()

    // Clean up:
    // We need to unlink the document units in order to be allowed to delete them in the fixtures
    await previousDecisionContainer.getByLabel("Listen Eintrag").first().click()
    await previousDecisionContainer.getByLabel("Eintrag löschen").click()

    await page.getByText("Speichern").click()
    await page.waitForEvent("requestfinished")
  })
})
