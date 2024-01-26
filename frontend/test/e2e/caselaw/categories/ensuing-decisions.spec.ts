import { expect } from "@playwright/test"
import {
  fillEnsuingDecisionInputs,
  navigateToCategories,
  waitForInputValue,
  publishDocumentationUnit,
} from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

test.describe("ensuing decisions", () => {
  test("renders empty ensuing decision in edit mode, when none in list", async ({
    page,
    documentNumber,
  }) => {
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
    ).toHaveCount(1)

    await page.getByLabel("Weitere Angabe").click()
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
    ).toHaveCount(2)
  })

  // TODO move to small-search.spec.ts?
  test("only note of linked ensuing decision is editable", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await publishDocumentationUnit(
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

    await expect(page.getByText("Seite 1")).toBeVisible()

    const result = page.getByText(
      `AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil, ${prefilledDocumentUnit.documentNumber}`,
    )

    await expect(result).toBeVisible()
    await page.getByLabel("Treffer übernehmen").click()

    //make sure to have citation style in list
    await expect(
      page.getByText(
        `nachgehend, AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil, ${prefilledDocumentUnit.documentNumber}`,
      ),
    ).toBeVisible()
    await expect(page.getByLabel("Eintrag löschen")).toBeVisible()

    //can not be edited
    await expect(page.getByLabel("Eintrag bearbeiten")).toBeVisible()

    await page.getByLabel("Eintrag bearbeiten").click()
    await expect(
      page.getByLabel("Gericht Nachgehende Entscheidung"),
    ).toBeHidden()
    await expect(
      page.getByLabel("Entscheidungsdatum Nachgehende Entscheidung"),
    ).toBeHidden()
    await expect(
      page.getByLabel("Aktenzeichen Nachgehende Entscheidung"),
    ).toBeHidden()
    await expect(
      page.getByLabel("Dokumenttyp Nachgehende Entscheidung"),
    ).toBeHidden()
    await expect(page.getByLabel("Vermerk")).toBeVisible()

    await page.getByLabel("Vermerk").fill("Vermerk")
    await waitForInputValue(page, `[aria-label='Vermerk']`, "Vermerk")
    await page.getByLabel("Nachgehende Entscheidung speichern").click()
    await expect(
      page.getByText(
        `nachgehend, AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil, Vermerk, ${prefilledDocumentUnit.documentNumber}`,
      ),
    ).toBeVisible()
  })

  test("validates against required fields", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await navigateToCategories(page, documentNumber)

    await fillEnsuingDecisionInputs(page, {
      fileNumber: "abc",
    })
    await page.getByLabel("Nachgehende Entscheidung speichern").click()

    await expect(page.getByLabel("Fehlerhafte Eingabe")).toBeVisible()
    await page.getByLabel("Eintrag bearbeiten").click()
    await expect(
      page
        .getByLabel("Nachgehende Entscheidung")
        .getByText("Pflichtfeld nicht befüllt"),
    ).toHaveCount(2)

    await fillEnsuingDecisionInputs(page, {
      court: prefilledDocumentUnit.coreData.court?.label,
      fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
      documentType: prefilledDocumentUnit.coreData.documentType?.label,
      decisionDate: "01.01.2020",
    })
    await page.getByLabel("Nachgehende Entscheidung speichern").click()

    await expect(page.getByLabel("Fehlerhafte Eingabe")).toBeHidden()
  })

  test("adding empty ensuing decision not possible", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await expect(
      page.getByLabel("Nachgehende Entscheidung speichern"),
    ).toBeDisabled()
  })

  test("incomplete date input shows error message and does not persist", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page
      .locator("[aria-label='Entscheidungsdatum Nachgehende Entscheidung']")
      .fill("03")

    await page.keyboard.press("Tab")

    await expect(
      page.locator(
        "[aria-label='Entscheidungsdatum Nachgehende Entscheidung']",
      ),
    ).toHaveValue("03")

    await expect(page.locator("text=Unvollständiges Datum")).toBeVisible()

    await page.reload()

    await expect(
      page.locator(
        "[aria-label='Entscheidungsdatum Nachgehende Entscheidung']",
      ),
    ).toHaveValue("")
  })
})
