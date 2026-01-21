import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  fillEnsuingDecisionInputs,
  handoverDocumentationUnit,
  navigateToCategories,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe("ensuing decisions", () => {
  test("renders all fields", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await expect(
      page.getByRole("heading", { name: "Nachgehende Entscheidung " }),
    ).toBeVisible()
    await expect(
      page.getByLabel("Gericht Nachgehende Entscheidung"),
    ).toBeVisible()
    await expect(
      page.getByLabel("Entscheidungsdatum Nachgehende Entscheidung"),
    ).toBeVisible()
    await expect(
      page.getByLabel("Aktenzeichen Nachgehende Entscheidung"),
    ).toBeVisible()
    await expect(
      page.getByLabel("Dokumenttyp Nachgehende Entscheidung"),
    ).toBeVisible()
    await expect(page.getByLabel("Vermerk")).toBeVisible()
    await expect(page.getByLabel("Datum unbekannt")).toBeVisible()
  })

  test("change to 'anhaengig' removes date with value and vice versa", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await navigateToCategories(page, documentNumber)

    await fillEnsuingDecisionInputs(page, {
      court: prefilledDocumentUnit.coreData.court?.label,
      fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
      documentType: prefilledDocumentUnit.coreData.documentType?.label,
      decisionDate: "01.01.2020",
    })

    await page.getByLabel("Nachgehende Entscheidung speichern").click()
    await expect(
      page.getByText(
        `nachgehend, AG Aachen, 01.01.2020, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`,
        {
          exact: true,
        },
      ),
    ).toBeVisible()

    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")

    await expect(
      ensuingDecisionContainer.getByLabel("Listen Eintrag"),
    ).toHaveCount(2)

    await fillEnsuingDecisionInputs(page, {
      court: prefilledDocumentUnit.coreData.court?.label,
      fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
      documentType: prefilledDocumentUnit.coreData.documentType?.label,
      decisionDate: "01.01.2020",
    })

    const pendingCheckbox = page.getByLabel("Anhängige Entscheidung")

    await pendingCheckbox.click()
    await expect(pendingCheckbox).toBeChecked()

    await expect(
      page.getByLabel("Entscheidungsdatum Nachgehende Entscheidung"),
    ).toBeHidden()

    await page.getByLabel("Nachgehende Entscheidung speichern").click()
    await expect(
      page.getByText(
        `anhängig, AG Aachen, Datum unbekannt, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`,
        {
          exact: true,
        },
      ),
    ).toBeVisible()

    await expect(
      ensuingDecisionContainer.getByLabel("Listen Eintrag"),
    ).toHaveCount(3)
  })

  test("only note of linked ensuing decision is editable", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await handoverDocumentationUnit(
      page,
      prefilledDocumentUnit.documentNumber || "",
    )
    await navigateToCategories(page, documentNumber)

    await fillEnsuingDecisionInputs(page, {
      court: prefilledDocumentUnit.coreData.court?.label,
      fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
      documentType: prefilledDocumentUnit.coreData.documentType?.label,
      decisionDate: "31.12.2019",
    })
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    await ensuingDecisionContainer
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
        `nachgehend, AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`,
      ),
    ).toBeVisible()
    await page
      .getByLabel("Nachgehende Entscheidung", { exact: true })
      .getByTestId("list-entry-0")
      .click()
    await expect(
      page.getByLabel("Gericht Nachgehende Entscheidung"),
    ).not.toBeEditable()
    await expect(
      page.getByLabel("Entscheidungsdatum Nachgehende Entscheidung"),
    ).not.toBeEditable()
    await expect(
      page.getByLabel("Aktenzeichen Nachgehende Entscheidung"),
    ).not.toBeEditable()
    await expect(
      page.getByLabel("Dokumenttyp Nachgehende Entscheidung"),
    ).not.toBeEditable()
    await expect(page.getByLabel("Vermerk")).toBeVisible()

    await page.getByLabel("Vermerk").fill("Vermerk")
    await expect(page.getByLabel("Vermerk", { exact: true })).toHaveValue(
      "Vermerk",
    )
    await page.getByLabel("Nachgehende Entscheidung speichern").click()
    await expect(
      page.getByText(
        `nachgehend, AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil, Vermerk`,
      ),
    ).toBeVisible()
  })
})
