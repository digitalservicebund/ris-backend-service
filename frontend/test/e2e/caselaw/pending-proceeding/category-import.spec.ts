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
    async ({ page, pendingProceeding, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, pendingProceeding.documentNumber, {
        type: "pending-proceeding",
      })

      await manuallyAddKeyword(page, "existingKeyword")
      await manuallyAddNorms(page)

      await test.step("Wird nach einer falschen Dokumentnummer gesucht, erscheint ein Fehler", async () => {
        await searchForDocumentUnitToImport(page, "invalidnumber")
        await expect(
          page.getByText("Keine Dokumentationseinheit gefunden."),
        ).toBeVisible()
      })

      await test.step("Wird nach anhängigen Verfahren gesucht, erscheint ein Fehler", async () => {
        await searchForDocumentUnitToImport(page, "YYTestDoc0017")
        await expect(
          page.getByText("Import zwischen anhängigen Verfahren nicht möglich."),
        ).toBeVisible()
      })

      await test.step("Wenn die Quellrubriken leer sind wird 'Quellrubrik leer' angezeigt und übernehmen ist nicht möglich", async () => {
        await searchForDocumentUnitToImport(page, "YYTestDoc0013")
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

      await importKeywords(page)
      await importFieldsOfLaw(page)
      await importNorms(page)
    },
  )

  /*    test(
    "Rubrikenimport eines anhängigen Verfahrens in eine Entscheidung",
    { tag: ["@RISDEV-7012"] },
    async ({ page, documentNumber, pendingProceeding }) => {
      await prefillPendingProceeding(page, pendingProceeding.documentNumber)
      await navigateToCategoryImport(page, documentNumber)

      await test.step("Wird nach einer falschen Dokumentnummer gesucht, erscheint ein Fehler", async () => {
        await searchForDocumentUnitToImport(page, "invalidnumber")
        await expect(
          page.getByText("Keine Dokumentationseinheit gefunden."),
        ).toBeVisible()
      })

      await test.step("Wenn die Quellrubriken leer sind wird 'Quellrubrik leer' angezeigt und übernehmen ist nicht möglich", async () => {
        await searchForDocumentUnitToImport(page, "YYTestDoc0017")
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
          pendingProceeding.documentNumber,
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

      await importKeywords(page)
      await importFieldsOfLaw(page)
      await importNorms(page)
    },
  )*/

  async function importKeywords(page: Page) {
    const keywordsContainer = page.getByTestId("keywords")
    await test.step("Schlagwörter werden importiert", async () => {
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

    await test.step("Die Schlagwörter sind ohne Dopplungen zusammengeführt", async () => {
      await manuallyAddKeyword(page, "newKeyword")

      await page.getByLabel("Schlagwörter übernehmen").click()
      const chipsLocator = page.getByTestId("keywords").getByTestId("chip")
      const chips = await chipsLocator.all()
      await expect(chipsLocator).toHaveCount(3)
      await expect(chips[0]).toHaveText("existingKeyword")
      await expect(chips[1]).toHaveText("keyword") // verify that the previously imported keyword is still the second and not appended at the end
      await expect(chips[2]).toHaveText("newKeyword")
    })
  }

  async function importFieldsOfLaw(page: Page) {
    const fieldsOfLaw = page.getByTestId("field-of-law-summary")
    await manuallyAddFieldsOfLaw(page)

    await test.step("Sachgebiete werden importiert", async () => {
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
      await expect(page.getByText("Übernommen")).toHaveCount(2)
    })

    await test.step("Es wird automatisch zu Sachgebieten gescrollt", async () => {
      await expect(
        page.getByRole("heading", { name: "Sachgebiete", exact: true }),
      ).toBeInViewport()
    })

    await test.step("Die Sachgebiete sind ohne Dopplungen zusammengeführt", async () => {
      await page
        .getByLabel("Direkteingabe-Sachgebietssuche eingeben", {
          exact: true,
        })
        .fill("EU-01-01")
      await expect(page.getByText("Aufgaben und Ziele")).toBeVisible()
      await page.getByText("Aufgaben und Ziele").click()
      await expect(fieldsOfLaw).toHaveCount(1)

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
  }

  async function importNorms(page: Page) {
    const normContainer = page.getByTestId("norms")
    await test.step("Normen werden importiert", async () => {
      await expect(page.getByLabel("Normen übernehmen")).toBeVisible()
      await page.getByLabel("Normen übernehmen").click()

      await expect(normContainer.getByLabel("Listen Eintrag")).toHaveCount(4) // the last entry is the input field
      await expect(
        normContainer.getByTestId("editable-list-container"),
      ).toHaveText("KBErrG, § 8 PBefG, § 1 BGB RIS-Abkürzung *")
    })

    await test.step("'Übernommen' wird angezeigt", async () => {
      await expect(page.getByText("Übernommen")).toHaveCount(3)
    })

    await test.step("Es wird automatisch zu Normen gescrollt", async () => {
      await expect(
        page.getByRole("heading", { name: "Normen", exact: true }),
      ).toBeInViewport()
    })

    await test.step("Die Normen sind ohne Dopplungen zusammengeführt", async () => {
      await page.getByLabel("Normen übernehmen").click()
      await expect(normContainer.getByLabel("Listen Eintrag")).toHaveCount(3) // the last entry is the input field
      await expect(
        normContainer.getByTestId("editable-list-container"),
      ).toHaveText("KBErrG, § 8 BGB RIS-Abkürzung *")
    })
  }

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
  async function manuallyAddFieldsOfLaw(page: Page) {
    const fieldsOfLaw = page.getByTestId("field-of-law-summary")
    await page.getByRole("button", { name: "Sachgebiete", exact: true }).click()
    await page
      .getByLabel("Direkteingabe-Sachgebietssuche eingeben", {
        exact: true,
      })
      .fill("VR-01-02")
    await expect(page.getByText("Völkergewohnheitsrecht")).toBeVisible()
    await page.getByText("Völkergewohnheitsrecht").click()

    await expect(fieldsOfLaw).toHaveCount(1)
  }

  async function manuallyAddNorms(page: Page) {
    const normContainer = page.getByTestId("norms")
    await fillNormInputs(page, {
      normAbbreviation: "KBErrG",
      singleNorms: [{ singleNorm: "§ 8" } as SingleNorm],
    })
    await normContainer.getByLabel("Norm speichern").click()
    await fillNormInputs(page, {
      normAbbreviation: "PBefG",
      singleNorms: [{ singleNorm: "§ 1" } as SingleNorm],
    })
    await normContainer.getByLabel("Norm speichern").click()
  }
})
