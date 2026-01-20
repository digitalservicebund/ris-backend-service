import { expect } from "@playwright/test"
import dayjs from "dayjs"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  fillActiveCitationInputs,
  handoverDocumentationUnit,
  navigateToCategories,
} from "~/e2e/caselaw/utils/e2e-utils"

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

  test("already linked docunit has tag in search results, but can be linked again", async ({
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
      citationType: "Änderung",
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

    await fillActiveCitationInputs(page, {
      court: prefilledDocumentUnit.coreData.court?.label,
      fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
      documentType: prefilledDocumentUnit.coreData.documentType?.label,
      decisionDate: "31.12.2019",
    })
    await activeCitationContainer.getByLabel("Nach Entscheidung suchen").click()
    await expect(
      activeCitationContainer.getByText("Bereits hinzugefügt"),
    ).toBeVisible()
    await expect(page.getByLabel("Treffer übernehmen")).toBeEnabled()
  })
  // eslint-disable-next-line playwright/consistent-spacing-between-blocks
  ;[
    {
      type: "Parallelentscheidung",
      headnoteAddition: ", welche vollständig dokumentiert ist",
    },
    {
      type: "Teilweise Parallelentscheidung",
      headnoteAddition: "",
    },
  ].forEach(({ type, headnoteAddition }) => {
    test(
      `Generate headnote possible, when citation style ' ${type}'`,
      {
        tag: ["@RISDEV-4829", "@RISDEV-5146", "@RISDEV-5920", "@RISDEV-5722"],
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4829",
        },
      },
      async ({ page, linkedDocumentNumber, prefilledDocumentUnit }) => {
        await handoverDocumentationUnit(
          page,
          prefilledDocumentUnit.documentNumber || "",
        )
        await navigateToCategories(page, linkedDocumentNumber)

        await fillActiveCitationInputs(page, {
          citationType: type,
        })
        await fillActiveCitationInputs(page, {
          court: prefilledDocumentUnit.coreData.court?.label,
          fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
          documentType: prefilledDocumentUnit.coreData.documentType?.label,
          decisionDate: "31.12.2019",
        })
        const activeCitationContainer = page.getByLabel("Aktivzitierung")
        await activeCitationContainer
          .getByLabel("Nach Entscheidung suchen")
          .click()

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

        // RISDEV-5920
        await test.step("generate headnote is possible", async () => {
          const generateButton = page.getByTestId("generate-headnote")
          await expect(generateButton).toBeVisible()
          await generateButton.click()
          await expect(
            page
              .getByTestId("headnote")
              .getByText("Orientierungssatz", { exact: true }),
          ).toBeVisible()
          await expect(page.getByTestId("Orientierungssatz")).toHaveText(
            `${type} zu der Entscheidung (${prefilledDocumentUnit.coreData.documentType?.label}) des ${prefilledDocumentUnit.coreData?.court?.label} vom ${dayjs(prefilledDocumentUnit.coreData.decisionDate).format("DD.MM.YYYY")} - ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}${headnoteAddition}.`,
          )
        })

        // RISDEV-5920
        await test.step("disable button and display tooltip when headnote is already filled", async () => {
          const element = page.getByRole("button", {
            name: "O-Satz generieren",
          })

          await expect(element).toBeDisabled()
          await element.hover()
          await element.dispatchEvent("mouseenter")

          await expect(
            page.getByText("Zielrubrik Orientierungssatz bereits ausgefüllt"),
          ).toBeVisible()
        })

        // RISDEV-5722
        await test.step("import categories is possible", async () => {
          const importButton = page.getByTestId("import-categories")
          await expect(importButton).toBeVisible()
          await expect(importButton).toBeEnabled()
          await importButton.click()
          await expect(page.getByText("Rubriken importieren")).toBeVisible()
          await expect(
            page.getByLabel("Dokumentnummer Eingabefeld"),
          ).toHaveValue(prefilledDocumentUnit.documentNumber)
        })
      },
    )
  })
})
