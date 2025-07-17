import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  fillPreviousDecisionInputs,
  handoverDocumentationUnit,
  navigateToCategories,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"
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
      .getByLabel("Datum Unbekannt Vorgehende Entscheidung", { exact: true })
      .click()

    await expect(
      page.getByLabel("Entscheidungsdatum Vorgehende Entscheidung", {
        exact: true,
      }),
    ).toBeHidden()

    await page
      .getByLabel("Datum Unbekannt Vorgehende Entscheidung", { exact: true })
      .click()

    await page
      .getByLabel("Entscheidungsdatum Vorgehende Entscheidung", { exact: true })
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
      .getByLabel("Aktenzeichen Vorgehende Entscheidung", { exact: true })
      .fill(fileNumber)

    await page
      .getByLabel(
        "Abweichendes Aktenzeichen Vorgehende Entscheidung anzeigen",
        { exact: true },
      )
      .click()

    const container = page
      .getByLabel("Vorgehende Entscheidung", { exact: true })
      .first()
    await expect(
      container.getByText("Abweichendes Aktenzeichen Vorinstanz").first(),
    ).toBeVisible()

    await page
      .getByLabel("Abweichendes Aktenzeichen Vorgehende Entscheidung", {
        exact: true,
      })
      .fill(deviatingFileNumber1)

    await page.getByLabel("Vorgehende Entscheidung speichern").click()
    await save(page)

    await page
      .getByLabel("Vorgehende Entscheidung", { exact: true })
      .getByTestId("list-entry-0")
      .click()
    // If deviating data is available, it is automatically expanded
    await expect(
      page.getByLabel("Abweichendes Aktenzeichen Vorgehende Entscheidung", {
        exact: true,
      }),
    ).toHaveValue(deviatingFileNumber1)

    await page
      .getByLabel("Abweichendes Aktenzeichen Vorgehende Entscheidung", {
        exact: true,
      })
      .fill(deviatingFileNumber2)

    await page.getByLabel("Vorgehende Entscheidung speichern").click()
    await save(page)

    await page
      .getByLabel("Vorgehende Entscheidung", { exact: true })
      .getByTestId("list-entry-0")
      .click()
    // If deviating data is available, it is automatically expanded
    await expect(
      page.getByLabel("Abweichendes Aktenzeichen Vorgehende Entscheidung", {
        exact: true,
      }),
    ).toHaveValue(deviatingFileNumber2)
  })

  test("only deviating file number of linked previous decision is editable", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await handoverDocumentationUnit(
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

    await expect(
      page.getByText(
        `AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`,
      ),
    ).toBeVisible()

    await expect(
      page.getByTestId(
        `decision-summary-${prefilledDocumentUnit.documentNumber}`,
      ),
    ).toBeVisible()

    await page.getByLabel("Treffer übernehmen").click()

    await expect(
      page.getByText(
        `AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`,
      ),
    ).toBeVisible()

    await page.getByTestId("list-entry-0").click()
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

    await page
      .getByLabel("Abweichendes Aktenzeichen Vorgehende Entscheidung", {
        exact: true,
      })
      .fill(deviatingFileNumber1)

    await page.getByLabel("Vorgehende Entscheidung speichern").click()
    await save(page)
    await expect(
      page.getByText(
        `AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, ${deviatingFileNumber1}, Anerkenntnisurteil`,
      ),
    ).toBeVisible()

    // Clean up:
    // We need to unlink the document units in order to be allowed to delete them in the fixtures
    await previousDecisionContainer.getByTestId("list-entry-0").click()

    await previousDecisionContainer.getByLabel("Eintrag löschen").click()

    await save(page)
  })
})
