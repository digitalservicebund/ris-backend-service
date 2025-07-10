import { expect, Page } from "@playwright/test"
import {
  fillNormInputs,
  navigateToCategoryImport,
  searchForDocumentUnitToImport,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"
import { longTextLabels, shortTextLabels } from "@/domain/decision"
import SingleNorm from "@/domain/singleNorm"

test.describe("category import of pending proceeding", () => {
  test(
    "Rubrikenimport einer Entscheidung in ein anhängiges Verfahren",
    { tag: ["@RISDEV-7012"] },
    async ({
      page,
      pendingProceeding,
      prefilledPendingProceeding,
      prefilledDocumentUnit,
      documentNumber,
    }) => {
      await navigateToCategoryImport(page, pendingProceeding.documentNumber, {
        type: "pending-proceeding",
      })

      await test.step("Wird nach einer falschen Dokumentnummer gesucht, erscheint ein Fehler", async () => {
        await searchForDocumentUnitToImport(page, "invalidnumber")
        await expect(
          page.getByText("Keine Dokumentationseinheit gefunden."),
        ).toBeVisible()
      })

      await test.step("Wird nach anhängigen Verfahren gesucht, erscheint ein Fehler", async () => {
        await searchForDocumentUnitToImport(
          page,
          prefilledPendingProceeding.documentNumber,
        )
        await expect(
          page.getByText("Import zwischen anhängigen Verfahren nicht möglich."),
        ).toBeVisible()
      })

      await test.step("Wenn die Quellrubriken leer sind wird 'Quellrubrik leer' angezeigt und übernehmen ist nicht möglich", async () => {
        await searchForDocumentUnitToImport(page, documentNumber)
        await expect(page.getByText("Quellrubrik leer")).toHaveCount(3) // we have 3 importable categories
        await expect(
          page.getByRole("button", { name: "Schlagwörter übernehmen" }),
        ).toBeDisabled()
        await expect(
          page.getByRole("button", { name: "Sachgebiete übernehmen" }),
        ).toBeDisabled()
        await expect(
          page.getByRole("button", { name: "Normen übernehmen" }),
        ).toBeDisabled()
      })

      await test.step("Es werden nur die Quell-Rubriken der inhaltlichen Erschließung angezeigt", async () => {
        await expect(page.getByLabel("Schlagwörter übernehmen")).toBeVisible()
        await expect(page.getByLabel("Sachgebiete übernehmen")).toBeVisible()
        await expect(page.getByLabel("Normen übernehmen")).toBeVisible()
        const allOtherLabels = {
          caselawReferences: "Rechtsprechungsfundstellen",
          literatureReferences: "Literaturfundstellen",
          activeCitations: "Aktivzitierung",
          ...shortTextLabels,
          ...longTextLabels,
        }
        for (const label of Object.values(allOtherLabels)) {
          await expect(page.getByLabel(label + " übernehmen")).toBeHidden()
        }
      })

      await test.step("Ist die Quell-Rubrik befüllt, kann sie übernommen werden", async () => {
        await searchForDocumentUnitToImport(
          page,
          prefilledDocumentUnit.documentNumber,
        )
        await expect(
          page.getByRole("button", { name: "Schlagwörter übernehmen" }),
        ).toBeEnabled()
        await expect(
          page.getByRole("button", { name: "Sachgebiete übernehmen" }),
        ).toBeEnabled()
        await expect(
          page.getByRole("button", { name: "Normen übernehmen" }),
        ).toBeEnabled()
      })
    },
  )

  test(
    "Rubrikenimport eines Anhängigen Verfahrens in eine Entscheidung",
    { tag: ["@RISDEV-7012"] },
    async ({
      page,
      documentNumber,
      prefilledPendingProceeding,
      pendingProceeding,
    }) => {
      await navigateToCategoryImport(page, documentNumber)

      await test.step("Wird nach einer falschen Dokumentnummer gesucht, erscheint ein Fehler", async () => {
        await searchForDocumentUnitToImport(page, "invalidnumber")
        await expect(
          page.getByText("Keine Dokumentationseinheit gefunden."),
        ).toBeVisible()
      })

      await test.step("Wenn die Quellrubriken leer sind wird 'Quellrubrik leer' angezeigt und übernehmen ist nicht möglich", async () => {
        await searchForDocumentUnitToImport(
          page,
          pendingProceeding.documentNumber,
        )
        await expect(page.getByText("Quellrubrik leer")).toHaveCount(3) // we have 3 importable categories
        await expect(
          page.getByRole("button", { name: "Schlagwörter übernehmen" }),
        ).toBeDisabled()
        await expect(
          page.getByRole("button", { name: "Sachgebiete übernehmen" }),
        ).toBeDisabled()
        await expect(
          page.getByRole("button", { name: "Normen übernehmen" }),
        ).toBeDisabled()
      })

      await test.step("Es werden nur die Quell-Rubriken der inhaltlichen Erschließung angezeigt", async () => {
        await expect(page.getByLabel("Schlagwörter übernehmen")).toBeVisible()
        await expect(page.getByLabel("Sachgebiete übernehmen")).toBeVisible()
        await expect(page.getByLabel("Normen übernehmen")).toBeVisible()
        const allOtherLabels = {
          caselawReferences: "Rechtsprechungsfundstellen",
          literatureReferences: "Literaturfundstellen",
          activeCitations: "Aktivzitierung",
          ...shortTextLabels,
          ...longTextLabels,
        }
        for (const label of Object.values(allOtherLabels)) {
          await expect(page.getByLabel(label + " übernehmen")).toBeHidden()
        }
      })

      await test.step("Ist die Quell-Rubrik befüllt, kann sie übernommen werden", async () => {
        await searchForDocumentUnitToImport(
          page,
          prefilledPendingProceeding.documentNumber,
        )
        await expect(
          page.getByRole("button", { name: "Schlagwörter übernehmen" }),
        ).toBeEnabled()
        await expect(
          page.getByRole("button", { name: "Sachgebiete übernehmen" }),
        ).toBeEnabled()
        await expect(
          page.getByRole("button", { name: "Normen übernehmen" }),
        ).toBeEnabled()
      })
    },
  )
  // Schlagwörter
  test(
    "Importiere Schlagwörter von einer Entscheidung in ein Anhängiges Verfahren",
    { tag: ["@RISDEV-7012"] },
    async ({ page, pendingProceeding, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, pendingProceeding.documentNumber, {
        type: "pending-proceeding",
      })
      const keywordsContainer = page.getByTestId("keywords")

      await test.step("Schlagwörter werden importiert", async () => {
        await manuallyAddKeyword(page, "existingKeyword")

        await searchForDocumentUnitToImport(
          page,
          prefilledDocumentUnit.documentNumber,
        )

        await expect(page.getByLabel("Schlagwörter übernehmen")).toBeVisible()
        await page.getByLabel("Schlagwörter übernehmen").click()
        await expect(page.getByText("keyword", { exact: true })).toBeVisible()

        await expect(keywordsContainer.getByTestId("chip")).toHaveCount(2)
      })

      await test.step("'Übernommen' wird angezeigt", async () => {
        await expect(page.getByText("Übernommen")).toBeVisible()
      })

      await test.step("Es wird automatisch zu Schlagwörtern gescrollt", async () => {
        await expect(
          page.getByRole("heading", { name: "Schlagwörter" }),
        ).toBeInViewport()
      })

      await test.step("Die Schlagwörter sind ohne Dopplungen und in korrekter Reihenfolge zusammengeführt", async () => {
        await manuallyAddKeyword(page, "newKeyword")

        await page.getByLabel("Schlagwörter übernehmen").click()
        const chipsLocator = page.getByTestId("keywords").getByTestId("chip")
        const chips = await chipsLocator.all()
        await expect(chipsLocator).toHaveCount(3)
        await expect(chips[0]).toHaveText("existingKeyword")
        await expect(chips[1]).toHaveText("keyword") // verify that the previously imported keyword is still the second and not appended at the end
        await expect(chips[2]).toHaveText("newKeyword")
      })
    },
  )

  test(
    "Importiere Schlagwörter von einem Anhängigen Verfahren in eine Entscheidung",
    { tag: ["@RISDEV-7012"] },
    async ({ page, documentNumber, prefilledPendingProceeding }) => {
      await navigateToCategoryImport(page, documentNumber)
      const keywordsContainer = page.getByTestId("keywords")

      await test.step("Schlagwörter werden importiert", async () => {
        await manuallyAddKeyword(page, "existingKeyword")

        await searchForDocumentUnitToImport(
          page,
          prefilledPendingProceeding.documentNumber,
        )

        await expect(page.getByLabel("Schlagwörter übernehmen")).toBeVisible()
        await page.getByLabel("Schlagwörter übernehmen").click()
        await expect(page.getByText("keyword", { exact: true })).toBeVisible()

        await expect(keywordsContainer.getByTestId("chip")).toHaveCount(2)
      })

      await test.step("'Übernommen' wird angezeigt", async () => {
        await expect(page.getByText("Übernommen")).toBeVisible()
      })

      await test.step("Es wird automatisch zu Schlagwörtern gescrollt", async () => {
        await expect(
          page.getByRole("heading", { name: "Schlagwörter" }),
        ).toBeInViewport()
      })

      await test.step("Die Schlagwörter sind ohne Dopplungen und in korrekter Reihenfolge zusammengeführt", async () => {
        await manuallyAddKeyword(page, "newKeyword")

        await page.getByLabel("Schlagwörter übernehmen").click()
        const chipsLocator = page.getByTestId("keywords").getByTestId("chip")
        const chips = await chipsLocator.all()
        await expect(chipsLocator).toHaveCount(3)
        await expect(chips[0]).toHaveText("existingKeyword")
        await expect(chips[1]).toHaveText("keyword") // verify that the previously imported keyword is still the second and not appended at the end
        await expect(chips[2]).toHaveText("newKeyword")
      })
    },
  )

  async function manuallyAddKeyword(page: Page, keyword: string) {
    let button = page
      .getByTestId("category-wrapper-button")
      .getByText(/Schlagwörter/)
    if (await button.isHidden()) {
      button = page.getByLabel("Schlagwörter bearbeiten")
    }
    await button.click()
    await page.getByLabel("Schlagwörter Input").focus()
    await page.keyboard.press("Enter")
    await page.getByLabel("Schlagwörter Input").type(keyword)
    await page.keyboard.press("Enter")
    await page
      .getByTestId("keywords")
      .getByLabel("Schlagwörter übernehmen")
      .click()
  }

  // Sachgebiete
  test(
    "Importiere Sachgebiete von einer Entscheidung in ein Anhängiges Verfahren",
    { tag: ["@RISDEV-7012"] },
    async ({ page, pendingProceeding, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, pendingProceeding.documentNumber, {
        type: "pending-proceeding",
      })
      const fieldsOfLaw = page.getByTestId("field-of-law-summary")

      await test.step("Sachgebiete werden importiert", async () => {
        await searchForDocumentUnitToImport(
          page,
          prefilledDocumentUnit.documentNumber,
        )
        // add a field of law manually
        await page.getByRole("button", { name: "Sachgebiete" }).click()
        await page
          .getByLabel("Direkteingabe-Sachgebietssuche eingeben", {
            exact: true,
          })
          .fill("VR-01-02")
        await expect(page.getByText("Völkergewohnheitsrecht")).toBeVisible()
        await page.getByText("Völkergewohnheitsrecht").click()

        await expect(fieldsOfLaw).toHaveCount(1)

        await expect(page.getByLabel("Sachgebiete übernehmen")).toBeVisible()
        await page.getByLabel("Sachgebiete übernehmen").click()

        const correctOrder = [
          "VR-01-02",
          "Völkergewohnheitsrecht",
          "AR-01",
          "Arbeitsvertrag: Abschluss, Klauseln, Arten, Betriebsübergang",
        ]
        await expect(fieldsOfLaw).toHaveText(correctOrder.join(""))
      })

      await test.step("'Übernommen' wird angezeigt", async () => {
        await expect(page.getByText("Übernommen")).toBeVisible()
      })

      await test.step("Es wird automatisch zu Sachgebieten gescrollt", async () => {
        await expect(
          page.getByRole("heading", { name: "Sachgebiete" }),
        ).toBeInViewport()
      })

      await test.step("Die Sachgebiete sind ohne Dopplungen und in korrekter Reihenfolge zusammengeführt", async () => {
        await page
          .getByLabel("Direkteingabe-Sachgebietssuche eingeben", {
            exact: true,
          })
          .fill("EU-01-01")
        await expect(page.getByText("Aufgaben und Ziele")).toBeVisible()
        await page.getByText("Aufgaben und Ziele").click()

        await page.getByLabel("Sachgebiete übernehmen").click()

        const correctOrder = [
          "VR-01-02",
          "Völkergewohnheitsrecht",
          "AR-01",
          "Arbeitsvertrag: Abschluss, Klauseln, Arten, Betriebsübergang",
          "EU-01-01",
          "Aufgaben und Ziele",
        ]
        await expect(fieldsOfLaw).toHaveText(correctOrder.join(""))
      })
    },
  )

  test(
    "Importiere Sachgebiete von einem Anhängigen Verfahren in eine Entscheidung",
    { tag: ["@RISDEV-7012"] },
    async ({ page, documentNumber, prefilledPendingProceeding }) => {
      await navigateToCategoryImport(page, documentNumber)
      const fieldsOfLaw = page.getByTestId("field-of-law-summary")

      await test.step("Sachgebiete werden importiert", async () => {
        await searchForDocumentUnitToImport(
          page,
          prefilledPendingProceeding.documentNumber,
        )
        // add a field of law manually
        await page.getByRole("button", { name: "Sachgebiete" }).click()
        await page
          .getByLabel("Direkteingabe-Sachgebietssuche eingeben", {
            exact: true,
          })
          .fill("VR-01-02")
        await expect(page.getByText("Völkergewohnheitsrecht")).toBeVisible()
        await page.getByText("Völkergewohnheitsrecht").click()

        await expect(fieldsOfLaw).toHaveCount(1)

        await expect(page.getByLabel("Sachgebiete übernehmen")).toBeVisible()
        await page.getByLabel("Sachgebiete übernehmen").click()

        const correctOrder = [
          "VR-01-02",
          "Völkergewohnheitsrecht",
          "AR-01",
          "Arbeitsvertrag: Abschluss, Klauseln, Arten, Betriebsübergang",
        ]
        await expect(fieldsOfLaw).toHaveText(correctOrder.join(""))
      })

      await test.step("'Übernommen' wird angezeigt", async () => {
        await expect(page.getByText("Übernommen")).toBeVisible()
      })

      await test.step("Es wird automatisch zu Sachgebieten gescrollt", async () => {
        await expect(
          page.getByRole("heading", { name: "Sachgebiete" }),
        ).toBeInViewport()
      })

      await test.step("Die Sachgebiete sind ohne Dopplungen und in korrekter Reihenfolge zusammengeführt", async () => {
        await page
          .getByLabel("Direkteingabe-Sachgebietssuche eingeben", {
            exact: true,
          })
          .fill("EU-01-01")
        await expect(page.getByText("Aufgaben und Ziele")).toBeVisible()
        await page.getByText("Aufgaben und Ziele").click()

        await page.getByLabel("Sachgebiete übernehmen").click()

        const correctOrder = [
          "VR-01-02",
          "Völkergewohnheitsrecht",
          "AR-01",
          "Arbeitsvertrag: Abschluss, Klauseln, Arten, Betriebsübergang",
          "EU-01-01",
          "Aufgaben und Ziele",
        ]
        await expect(fieldsOfLaw).toHaveText(correctOrder.join(""))
      })
    },
  )

  // Normen
  test(
    "Importiere Normen von einer Entscheidung in ein Anhängiges Verfahren",
    { tag: ["@RISDEV-7012"] },
    async ({ page, pendingProceeding, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, pendingProceeding.documentNumber, {
        type: "pending-proceeding",
      })
      const normContainer = page.getByTestId("norms")

      await test.step("Normen werden importiert", async () => {
        // add entry manually
        await fillNormInputs(page, {
          normAbbreviation: "PBefG",
        })
        await normContainer.getByLabel("Norm speichern").click()
        await expect(normContainer.getByText("PBefG")).toBeVisible()

        await searchForDocumentUnitToImport(
          page,
          prefilledDocumentUnit.documentNumber,
        )

        await expect(page.getByLabel("Normen übernehmen")).toBeVisible()
        await page.getByLabel("Normen übernehmen").click()

        await expect(normContainer.getByLabel("Listen Eintrag")).toHaveCount(3) // the last entry is the input field
        await expect(
          normContainer.getByTestId("editable-list-container"),
        ).toHaveText("PBefG BGB RIS-Abkürzung * ")
      })

      await test.step("'Übernommen' wird angezeigt", async () => {
        await expect(page.getByText("Übernommen")).toBeVisible()
      })

      await test.step("Es wird automatisch zu Normen gescrollt", async () => {
        await expect(
          page.getByRole("heading", { name: "Normen" }),
        ).toBeInViewport()
      })

      await test.step("Die Normen sind ohne Dopplungen und in korrekter Reihenfolge zusammengeführt", async () => {
        // add entry manually
        await fillNormInputs(page, {
          normAbbreviation: "KBErrG",
          singleNorms: [{ singleNorm: "§ 8" } as SingleNorm],
        })
        await normContainer.getByLabel("Norm speichern").click()

        await page.getByLabel("Normen übernehmen").click()
        await expect(normContainer.getByLabel("Listen Eintrag")).toHaveCount(4) // the last entry is the input field
        await expect(
          normContainer.getByTestId("editable-list-container"),
        ).toHaveText("PBefG BGB KBErrG, § 8 RIS-Abkürzung *")
      })
    },
  )

  test(
    "Importiere Normen von einem Anhängigen Verfahren in eine Entscheidung",
    { tag: ["@RISDEV-7012"] },
    async ({ page, documentNumber, prefilledPendingProceeding }) => {
      await navigateToCategoryImport(page, documentNumber)
      const normContainer = page.getByTestId("norms")

      await test.step("Normen werden importiert", async () => {
        // add entry manually
        await fillNormInputs(page, {
          normAbbreviation: "PBefG",
        })
        await normContainer.getByLabel("Norm speichern").click()
        await expect(normContainer.getByText("PBefG")).toBeVisible()

        await searchForDocumentUnitToImport(
          page,
          prefilledPendingProceeding.documentNumber,
        )

        await expect(page.getByLabel("Normen übernehmen")).toBeVisible()
        await page.getByLabel("Normen übernehmen").click()

        await expect(normContainer.getByLabel("Listen Eintrag")).toHaveCount(3) // the last entry is the input field
        await expect(
          normContainer.getByTestId("editable-list-container"),
        ).toHaveText("PBefG BGB RIS-Abkürzung * ")
      })

      await test.step("'Übernommen' wird angezeigt", async () => {
        await expect(page.getByText("Übernommen")).toBeVisible()
      })

      await test.step("Es wird automatisch zu Normen gescrollt", async () => {
        await expect(
          page.getByRole("heading", { name: "Normen" }),
        ).toBeInViewport()
      })

      await test.step("Die Normen sind ohne Dopplungen und in korrekter Reihenfolge zusammengeführt", async () => {
        // add entry manually
        await fillNormInputs(page, {
          normAbbreviation: "KBErrG",
          singleNorms: [{ singleNorm: "§ 8" } as SingleNorm],
        })
        await normContainer.getByLabel("Norm speichern").click()

        await page.getByLabel("Normen übernehmen").click()
        await expect(normContainer.getByLabel("Listen Eintrag")).toHaveCount(4) // the last entry is the input field
        await expect(
          normContainer.getByTestId("editable-list-container"),
        ).toHaveText("PBefG BGB KBErrG, § 8 RIS-Abkürzung *")
      })
    },
  )
})
