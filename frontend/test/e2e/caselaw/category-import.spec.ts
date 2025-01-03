import { expect, Page } from "@playwright/test"
import {
  clickCategoryButton,
  fillActiveCitationInputs,
  fillNormInputs,
  navigateToCategories,
  save,
} from "./e2e-utils"
import { caselawTest as test } from "./fixtures"
import SingleNorm from "@/domain/singleNorm"

test.describe("category import", () => {
  test(
    "display category import",
    { tag: ["@RISDEV-5719"] },
    async ({ page, prefilledDocumentUnit }) => {
      await test.step("displays category import with disabled button", async () => {
        await navigateToCategoryImport(
          page,
          prefilledDocumentUnit.documentNumber as string,
        )
        await expect(
          page.getByRole("button", { name: "Dokumentationseinheit laden" }),
        ).toBeVisible()
        await expect(
          page.getByRole("button", { name: "Dokumentationseinheit laden" }),
        ).toBeDisabled()
      })

      await test.step("search for non-existent document unit displays error", async () => {
        await searchForDocumentUnitToImport(page, "invalidnumber")
        await expect(
          page.getByText("Keine Dokumentationseinheit gefunden."),
        ).toBeVisible()
      })

      await test.step("search for document unit displays core data", async () => {
        await searchForDocumentUnitToImport(page, "YYTestDoc0013")
        await expect(page.getByText("fileNumber5")).toBeVisible()
      })
    },
  )

  test(
    "disable import for empty categories",
    {
      tag: [
        "@RISDEV-5720",
        "@RISDEV-5886",
        "@RISDEV-5887",
        "@RISDEV-5888",
        "@RISDEV-5721",
      ],
    },
    async ({ page, documentNumber }) => {
      await navigateToCategoryImport(page, documentNumber)
      await searchForDocumentUnitToImport(page, documentNumber)
      await expect(page.getByText("Quellrubrik leer")).toHaveCount(7) // we have 7 importable categories
    },
  )

  test(
    "import keywords",
    { tag: ["@RISDEV-5720"] },
    async ({ page, documentNumber, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, documentNumber)
      const keywordsContainer = page.getByTestId("keywords")

      await test.step("import into prefilled category", async () => {
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

      await test.step("show success badge", async () => {
        await expect(page.getByText("Übernommen")).toBeVisible()
      })

      await test.step("scroll to category", async () => {
        await expect(
          page.getByRole("heading", { name: "Schlagwörter" }),
        ).toBeInViewport()
      })

      await test.step("do not import duplicates and keep first keyword", async () => {
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

  test(
    "import fields of law",
    { tag: ["@RISDEV-5886"] },
    async ({ page, documentNumber, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, documentNumber)
      const fieldsOfLaw = page.getByTestId("field-of-law-summary")

      await test.step("import into prefilled category", async () => {
        await searchForDocumentUnitToImport(
          page,
          prefilledDocumentUnit.documentNumber,
        )
        // add a field of law manually
        await page.getByRole("button", { name: "Sachgebiete" }).click()
        await page
          .locator("[aria-label='Direkteingabe-Sachgebietssuche eingeben']")
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

      await test.step("show success badge", async () => {
        await expect(page.getByText("Übernommen")).toBeVisible()
      })

      await test.step("scroll to category", async () => {
        await expect(
          page.getByRole("heading", { name: "Sachgebiete" }),
        ).toBeInViewport()
      })

      await test.step("do not import duplicates and keep first field of law", async () => {
        await page
          .locator("[aria-label='Direkteingabe-Sachgebietssuche eingeben']")
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
    "import norms",
    { tag: ["@RISDEV-5887"] },
    async ({ page, documentNumber, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, documentNumber)
      const normContainer = page.getByTestId("norms")

      await test.step("import into prefilled category", async () => {
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

        const correctOrder = ["PBefG", "BGB"]
        await expect(
          normContainer.getByTestId("editable-list-container"),
        ).toHaveText(correctOrder.join("") + "RIS-Abkürzung *")
      })

      await test.step("show success badge", async () => {
        await expect(page.getByText("Übernommen")).toBeVisible()
      })

      await test.step("scroll to category", async () => {
        await expect(
          page.getByRole("heading", { name: "Normen" }),
        ).toBeInViewport()
      })

      await test.step("do not import duplicates and keep first field of law", async () => {
        // add entry manually
        await fillNormInputs(page, {
          normAbbreviation: "KBErrG",
          singleNorms: [{ singleNorm: "§ 8" } as SingleNorm],
        })
        await normContainer.getByLabel("Norm speichern").click()

        await page.getByLabel("Normen übernehmen").click()
        await expect(normContainer.getByLabel("Listen Eintrag")).toHaveCount(4) // the last entry is the input field
        const correctOrder = ["PBefG", "BGB", "KBErrG, § 8"]
        await expect(
          normContainer.getByTestId("editable-list-container"),
        ).toHaveText(correctOrder.join("") + "RIS-Abkürzung *")
      })
    },
  )

  test(
    "import active citations",
    { tag: ["@RISDEV-5888"] },
    async ({ page, linkedDocumentNumber, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, linkedDocumentNumber)
      const activeCitationsContainer = page.getByTestId("activeCitations")

      await test.step("import into prefilled category", async () => {
        // Add active citation to YYTestDoc0014 manually
        await fillActiveCitationInputs(page, {
          court: "AG Aachen",
          decisionDate: "01.01.1989",
          citationType: "Abweichung",
        })
        await activeCitationsContainer
          .getByLabel("Nach Entscheidung suchen")
          .click()
        await page.getByLabel("Treffer übernehmen").click()

        await searchForDocumentUnitToImport(
          page,
          prefilledDocumentUnit.documentNumber,
        )

        await expect(page.getByLabel("Aktivzitierung übernehmen")).toBeVisible()
        await page.getByLabel("Aktivzitierung übernehmen").click()

        await expect(
          page.getByText(
            "Abgrenzung, AG Aachen, 01.02.2022, 123, Anerkenntnisurteil",
          ),
        ).toBeVisible()

        await expect(
          activeCitationsContainer.getByLabel("Listen Eintrag"),
        ).toHaveCount(3) // the last entry is the creation form

        await expect(
          activeCitationsContainer.getByLabel("Listen Eintrag").nth(0),
        ).toHaveText(
          "Abweichung, AG Aachen, 01.01.1989, Beschluss |YYTestDoc0014",
        )
        await expect(
          activeCitationsContainer.getByLabel("Listen Eintrag").nth(1),
        ).toHaveText(
          `Abgrenzung, AG Aachen, 01.02.2022, 123, Anerkenntnisurteil |YYTestDoc0013`,
        )
      })

      await test.step("show success badge", async () => {
        await expect(page.getByText("Übernommen")).toBeVisible()
      })

      await test.step("scroll to category", async () => {
        await expect(
          page.getByRole("heading", { name: "Aktivzitierung" }),
        ).toBeInViewport()
      })

      await test.step("do not import duplicates and keep first field of law", async () => {
        // Add active citation to YYTestDoc0003 manually
        await fillActiveCitationInputs(page, {
          court: "BGH",
          decisionDate: "10.10.1964",
          citationType: "Verbunden",
        })
        await activeCitationsContainer
          .getByLabel("Nach Entscheidung suchen")
          .click()
        await page.getByLabel("Treffer übernehmen").click()

        await page.getByLabel("Aktivzitierung übernehmen").click()
        await expect(
          activeCitationsContainer.getByLabel("Listen Eintrag"),
        ).toHaveCount(4) // the last entry is the creation form

        await expect(
          activeCitationsContainer.getByLabel("Listen Eintrag").nth(0),
        ).toHaveText(
          "Abweichung, AG Aachen, 01.01.1989, Beschluss |YYTestDoc0014",
        )
        await expect(
          activeCitationsContainer.getByLabel("Listen Eintrag").nth(1),
        ).toHaveText(
          `Abgrenzung, AG Aachen, 01.02.2022, 123, Anerkenntnisurteil |YYTestDoc0013`,
        )
        await expect(
          activeCitationsContainer.getByLabel("Listen Eintrag").nth(2),
        ).toHaveText(/Verbunden, BGH, 10.10.1964.*/)
      })
    },
  )

  test(
    "import headline",
    { tag: ["@RISDEV-5888"] },
    async ({ page, linkedDocumentNumber, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, linkedDocumentNumber)

      await test.step("import into empty category", async () => {
        await searchForDocumentUnitToImport(
          page,
          prefilledDocumentUnit.documentNumber,
        )

        await expect(page.getByLabel("Titelzeile übernehmen")).toBeVisible()
        await page.getByLabel("Titelzeile übernehmen").click()
        await expect(page.getByText("testHeadline")).toBeVisible()
      })

      await test.step("show success badge", async () => {
        await expect(page.getByText("Übernommen")).toBeVisible()
      })

      await test.step("scroll to category", async () => {
        await expect(
          page.getByLabel("Kurztexte").getByText("Titelzeile", { exact: true }),
        ).toBeInViewport()
      })
    },
  )

  // Short text categories

  test(
    "import guiding principle",
    { tag: ["@RISDEV-5721"] },
    async ({ page, linkedDocumentNumber, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, linkedDocumentNumber)

      await test.step("import into empty category", async () => {
        await searchForDocumentUnitToImport(
          page,
          prefilledDocumentUnit.documentNumber,
        )

        await expect(page.getByLabel("Leitsatz übernehmen")).toBeVisible()
        await page.getByLabel("Leitsatz übernehmen").click()

        await expect(page.getByText("guidingPrinciple")).toBeVisible()
      })

      await test.step("show success badge", async () => {
        await expect(page.getByText("Übernommen")).toBeVisible()
      })

      await test.step("scroll to category", async () => {
        await expect(
          page.getByLabel("Kurztexte").getByText("Leitsatz", { exact: true }),
        ).toBeInViewport()
      })
    },
  )

  test(
    "import headnote",
    { tag: ["@RISDEV-5721"] },
    async ({ page, linkedDocumentNumber, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, linkedDocumentNumber)

      await test.step("import into empty category", async () => {
        await searchForDocumentUnitToImport(
          page,
          prefilledDocumentUnit.documentNumber,
        )
        await expect(
          page.getByLabel("Orientierungssatz übernehmen"),
        ).toBeVisible()
        await page.getByLabel("Orientierungssatz übernehmen").click()

        await expect(page.getByText("testHeadnote")).toBeVisible()
      })

      await test.step("show success badge", async () => {
        await expect(page.getByText("Übernommen")).toBeVisible()
      })

      await test.step("scroll to category", async () => {
        await expect(
          page
            .getByLabel("Kurztexte")
            .getByText("Orientierungssatz", { exact: true }),
        ).toBeInViewport()
      })
    },
  )

  test(
    "import short texts not possible when target category filled",
    { tag: ["@RISDEV-5721"] },
    async ({ page, documentNumber, prefilledDocumentUnit }) => {
      await navigateToCategories(page, documentNumber)
      await clickCategoryButton("Leitsatz", page)
      const guidingPrincipleInput = page.locator("[data-testid='Leitsatz']")
      await guidingPrincipleInput.click()
      await page.keyboard.type(`Test`)
      await save(page)

      await navigateToCategoryImport(page, documentNumber)
      await searchForDocumentUnitToImport(
        page,
        prefilledDocumentUnit.documentNumber,
      )
      await expect(page.getByText("Zielrubrik ausgefüllt")).toBeVisible()
      await guidingPrincipleInput.click()
      await page.keyboard.press(`ControlOrMeta+A`)
      await page.keyboard.press(`ControlOrMeta+Backspace`)
      await expect(page.getByText("Zielrubrik ausgefüllt")).toBeHidden()
    },
  )

  async function navigateToCategoryImport(page: Page, documentNumber: string) {
    await navigateToCategories(page, documentNumber)
    await page.getByLabel("Seitenpanel öffnen").click()
    await page
      .getByTestId("category-import-button")
      .getByLabel("Rubriken-Import anzeigen")
      .click()

    await expect(page.getByText("Rubriken importieren")).toBeVisible()
    await expect(page.getByLabel("Dokumentnummer Eingabefeld")).toBeVisible()
  }

  async function searchForDocumentUnitToImport(
    page: Page,
    documentNumber: string,
  ) {
    await page
      .getByRole("textbox", { name: "Dokumentnummer Eingabefeld" })
      .fill(documentNumber)

    await expect(
      page.getByRole("button", { name: "Dokumentationseinheit laden" }),
    ).toBeEnabled()
    await page
      .getByRole("button", { name: "Dokumentationseinheit laden" })
      .click()
  }
})
