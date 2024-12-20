import { expect } from "@playwright/test"
import dayjs from "dayjs"
import {
  fillActiveCitationInputs,
  navigateToCategories,
  handoverDocumentationUnit,
} from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

test.describe("active citations", () => {
  test("renders all fields", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await expect(
      page.getByRole("heading", { name: "Aktivzitierung" }),
    ).toBeVisible()
    await expect(page.getByLabel("Art der Zitierung")).toBeVisible()
    await expect(page.getByLabel("Gericht Aktivzitierung")).toBeVisible()
    await expect(
      page.getByLabel("Entscheidungsdatum Aktivzitierung"),
    ).toBeVisible()
    await expect(page.getByLabel("Aktenzeichen Aktivzitierung")).toBeVisible()
    await expect(page.getByLabel("Dokumenttyp Aktivzitierung")).toBeVisible()
  })

  test("only citation style of linked active citation is editable", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await handoverDocumentationUnit(
      page,
      prefilledDocumentUnit.documentNumber || "",
    )
    await navigateToCategories(page, documentNumber)

    await fillActiveCitationInputs(page, {
      court: prefilledDocumentUnit.coreData.court?.label,
      fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
      documentType: prefilledDocumentUnit.coreData.documentType?.label,
      decisionDate: "31.12.2019",
    })
    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    await activeCitationContainer.getByLabel("Nach Entscheidung suchen").click()

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

    await expect(page.getByText("Fehlende Daten")).toBeVisible()

    await page.getByTestId("list-entry-0").click()
    await expect(page.getByLabel("Gericht Aktivzitierung")).not.toBeEditable()
    await expect(
      page.getByLabel("Entscheidungsdatum Aktivzitierung"),
    ).not.toBeEditable()
    await expect(
      page.getByLabel("Aktenzeichen Aktivzitierung"),
    ).not.toBeEditable()
    await expect(
      page.getByLabel("Dokumenttyp Aktivzitierung"),
    ).not.toBeEditable()
    await expect(page.getByLabel("Art der Zitierung")).toBeVisible()

    await fillActiveCitationInputs(page, {
      citationType: "Änderung",
    })
    await page.getByLabel("Aktivzitierung speichern").click()
    await expect(
      page.getByText(
        `Änderung, AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`,
      ),
    ).toBeVisible()

    await expect(page.getByText("Fehlende Daten")).toBeHidden()
  })

  test("Import categories possible, when citation style 'Parallelentscheidung'", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await handoverDocumentationUnit(
      page,
      prefilledDocumentUnit.documentNumber || "",
    )
    await navigateToCategories(page, documentNumber)

    await fillActiveCitationInputs(page, {
      citationType: "Parallelentscheidung",
    })
    await fillActiveCitationInputs(page, {
      court: prefilledDocumentUnit.coreData.court?.label,
      fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
      documentType: prefilledDocumentUnit.coreData.documentType?.label,
      decisionDate: "31.12.2019",
    })
    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    await activeCitationContainer.getByLabel("Nach Entscheidung suchen").click()

    await expect(page.getByText("1 Ergebnis gefunden")).toBeVisible()

    await expect(
      page.getByText(
        `AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`,
      ),
    ).toBeVisible()

    await page.getByLabel("Treffer übernehmen").click()

    await expect(
      page.getByText(
        `Parallelentscheidung, AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`,
      ),
    ).toBeVisible()

    const importButton = page.getByTestId("import-categories")
    await expect(importButton).toBeVisible()
    await importButton.click()
    await expect(page.getByText("Rubriken importieren")).toBeVisible()
    await expect(page.getByLabel("Dokumentnummer Eingabefeld")).toHaveValue(
      prefilledDocumentUnit.documentNumber,
    )
  })

  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("Generate headnote possible, when citation style 'Parallelentscheidung'", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await handoverDocumentationUnit(
      page,
      prefilledDocumentUnit.documentNumber || "",
    )
    await navigateToCategories(page, documentNumber)

    await fillActiveCitationInputs(page, {
      citationType: "Parallelentscheidung",
    })
    await fillActiveCitationInputs(page, {
      court: prefilledDocumentUnit.coreData.court?.label,
      fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
      documentType: prefilledDocumentUnit.coreData.documentType?.label,
      decisionDate: "31.12.2019",
    })
    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    await activeCitationContainer.getByLabel("Nach Entscheidung suchen").click()

    await expect(page.getByText("1 Ergebnis gefunden")).toBeVisible()

    await expect(
      page.getByText(
        `AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`,
      ),
    ).toBeVisible()

    await page.getByLabel("Treffer übernehmen").click()

    await expect(
      page.getByText(
        `Parallelentscheidung, AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`,
      ),
    ).toBeVisible()

    const generateButton = page.getByTestId("generate-headnote")
    await expect(generateButton).toBeVisible()
    await generateButton.click()
    await expect(
      page.getByText("Orientierungssatz", { exact: true }),
    ).toBeVisible()
    await expect(page.getByTestId("Orientierungssatz")).toHaveText(
      `Parallelentscheidung zu der Entscheidung (${prefilledDocumentUnit.coreData.documentType?.label}) des ${prefilledDocumentUnit.coreData?.court?.label} (${prefilledDocumentUnit.coreData?.court?.location}) vom ${dayjs(prefilledDocumentUnit.coreData.decisionDate).format("DD.MM.YYYY")} - ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, welche vollständig dokumentiert ist.`,
    )
    // Clean up: We need to unlink the document units in order to be allowed to delete them in the fixtures
    await page.getByTestId("list-entry-0").click()
    await expect(page.getByLabel("Eintrag löschen")).toBeVisible()
    await page.getByLabel("Eintrag löschen").click()
  })

  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("Generate headnote possible, when citation style 'Teilweise Parallelentscheidung'", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await handoverDocumentationUnit(
      page,
      prefilledDocumentUnit.documentNumber || "",
    )
    await navigateToCategories(page, documentNumber)

    await fillActiveCitationInputs(page, {
      citationType: "Teilweise Parallelentscheidung",
    })
    await fillActiveCitationInputs(page, {
      court: prefilledDocumentUnit.coreData.court?.label,
      fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
      documentType: prefilledDocumentUnit.coreData.documentType?.label,
      decisionDate: "31.12.2019",
    })
    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    await activeCitationContainer.getByLabel("Nach Entscheidung suchen").click()

    await expect(page.getByText("1 Ergebnis gefunden")).toBeVisible()

    await expect(
      page.getByText(
        `AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`,
      ),
    ).toBeVisible()

    await page.getByLabel("Treffer übernehmen").click()

    await expect(
      page.getByText(
        `Parallelentscheidung, AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`,
      ),
    ).toBeVisible()

    const generateButton = page.getByTestId("generate-headnote")
    await expect(generateButton).toBeVisible()
    await generateButton.click()
    await expect(
      page.getByText("Orientierungssatz", { exact: true }),
    ).toBeVisible()
    await expect(page.getByTestId("Orientierungssatz")).toHaveText(
      `Teilweise Parallelentscheidung zu der Entscheidung (${prefilledDocumentUnit.coreData.documentType?.label}) des ${prefilledDocumentUnit.coreData?.court?.label} (${prefilledDocumentUnit.coreData?.court?.location}) vom ${dayjs(prefilledDocumentUnit.coreData.decisionDate).format("DD.MM.YYYY")} - ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}.`,
    )

    // Clean up: We need to unlink the document units in order to be allowed to delete them in the fixtures
    await page.getByTestId("list-entry-0").click()
    await expect(page.getByLabel("Eintrag löschen")).toBeVisible()
    await page.getByLabel("Eintrag löschen").click()
  })
})
