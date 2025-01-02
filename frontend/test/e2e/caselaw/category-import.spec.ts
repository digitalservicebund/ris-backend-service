import { expect, Page } from "@playwright/test"
import { clickCategoryButton, navigateToCategories, save } from "./e2e-utils"
import { caselawTest as test } from "./fixtures"

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

  // Wenn Rubrik im Quelldokument leer ist, wird der Button disabled und in einem grauen Badge angezeigt “Quellrubrik leer“
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
      await manuallyAddKeyword(page, "existingKeyword")

      // ✔ Bei der Übernahme, werden die bestehenden Daten beibehalten und die importierten Rubrikendaten lediglich angehangen.
      await test.step("import into prefilled category", async () => {
        await searchForDocumentUnitToImport(
          page,
          prefilledDocumentUnit.documentNumber,
        )
        await expect(
          page.getByText(prefilledDocumentUnit.coreData.fileNumbers![0]),
        ).toBeVisible()

        await expect(page.getByLabel("Schlagwörter übernehmen")).toBeVisible()
        await page.getByLabel("Schlagwörter übernehmen").click()
        await expect(page.getByText("keyword", { exact: true })).toBeVisible()

        await expect(keywordsContainer.getByTestId("chip")).toHaveCount(2)
      })

      // Eine erfolgreiche Übernahme wird mit einem Erfolgs-Alert bestätigt über den Badge “ ✓ Übernommen“
      await test.step("show success badge", async () => {
        await expect(page.getByText("Übernommen")).toBeVisible()
      })

      // ✔ Bei Übernahme wird die Seite zu der Rubrik gescrollt
      await test.step("scroll to category", async () => {
        await expect(
          page.getByRole("heading", { name: "Schlagwörter" }),
        ).toBeInViewport()
      })

      // ✔ Doppelte Einträge werden innerhalb einer Rubrik bereinigt (Das zuletzt hinzugefügte Duplikat verschwindet)
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

      // TODO: Eine gescheiterte Übernahme wird mit einem Fehler-Alert inline angezeigt (dem Info Modal im ErrorState)
      // würde ich nicht im e2e test abdecken --> unit test
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
      await searchForDocumentUnitToImport(
        page,
        prefilledDocumentUnit.documentNumber,
      )
      await expect(
        page.getByText(prefilledDocumentUnit.coreData.fileNumbers![0]),
      ).toBeVisible()

      await expect(page.getByLabel("Sachgebiete übernehmen")).toBeVisible()
      await page.getByLabel("Sachgebiete übernehmen").click()

      await expect(page.getByText("Übernommen")).toBeVisible()
      await expect(page.getByText("AR-01")).toBeVisible()

      await expect(page.getByTestId("field-of-law-summary")).toHaveCount(1)
      await page.getByLabel("Sachgebiete übernehmen").click()
      // does not import duplicates
      await expect(page.getByTestId("field-of-law-summary")).toHaveCount(1)
    },
  )

  test(
    "import norms",
    { tag: ["@RISDEV-5887"] },
    async ({ page, documentNumber, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, documentNumber)
      await searchForDocumentUnitToImport(
        page,
        prefilledDocumentUnit.documentNumber,
      )
      await expect(
        page.getByText(prefilledDocumentUnit.coreData.fileNumbers![0]),
      ).toBeVisible()

      await expect(page.getByLabel("Normen übernehmen")).toBeVisible()
      await page.getByLabel("Normen übernehmen").click()

      await expect(page.getByText("Übernommen")).toBeVisible()
      await expect(page.getByText("BGB")).toBeVisible()

      const normContainer = page.getByTestId("norms")
      await expect(normContainer.getByLabel("Listen Eintrag")).toHaveCount(2)
      await page.getByLabel("Normen übernehmen").click()
      // does not import duplicates
      await expect(normContainer.getByLabel("Listen Eintrag")).toHaveCount(2)
    },
  )

  test(
    "import active citations",
    { tag: ["@RISDEV-5888"] },
    async ({ page, linkedDocumentNumber, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, linkedDocumentNumber)
      await searchForDocumentUnitToImport(
        page,
        prefilledDocumentUnit.documentNumber,
      )
      await expect(
        page.getByText(prefilledDocumentUnit.coreData.fileNumbers![0]),
      ).toBeVisible()

      await expect(page.getByLabel("Aktivzitierung übernehmen")).toBeVisible()
      await page.getByLabel("Aktivzitierung übernehmen").click()

      await expect(page.getByText("Übernommen")).toBeVisible()
      await expect(
        page.getByText(
          "Abgrenzung, AG Aachen, 01.02.2022, 123, Anerkenntnisurteil",
        ),
      ).toBeVisible()

      const activeCitationsContainer = page.getByTestId("activeCitations")
      await expect(
        activeCitationsContainer.getByLabel("Listen Eintrag"),
      ).toHaveCount(2)
      await page.getByLabel("Aktivzitierung übernehmen").click()
      // does not import duplicates
      await expect(
        activeCitationsContainer.getByLabel("Listen Eintrag"),
      ).toHaveCount(2)
    },
  )

  test(
    "import headline",
    { tag: ["@RISDEV-5888"] },
    async ({ page, linkedDocumentNumber, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, linkedDocumentNumber)
      await searchForDocumentUnitToImport(
        page,
        prefilledDocumentUnit.documentNumber,
      )
      await expect(
        page.getByText(prefilledDocumentUnit.coreData.fileNumbers![0]),
      ).toBeVisible()

      await expect(page.getByLabel("Titelzeile übernehmen")).toBeVisible()
      await page.getByLabel("Titelzeile übernehmen").click()

      await expect(page.getByText("Übernommen")).toBeVisible()
      await expect(page.getByText("testHeadline")).toBeVisible()
    },
  )

  test(
    "import guiding principle",
    { tag: ["@RISDEV-5888"] },
    async ({ page, linkedDocumentNumber, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, linkedDocumentNumber)
      await searchForDocumentUnitToImport(
        page,
        prefilledDocumentUnit.documentNumber,
      )
      await expect(
        page.getByText(prefilledDocumentUnit.coreData.fileNumbers![0]),
      ).toBeVisible()

      await expect(page.getByLabel("Leitsatz übernehmen")).toBeVisible()
      await page.getByLabel("Leitsatz übernehmen").click()

      await expect(page.getByText("Übernommen")).toBeVisible()
      await expect(page.getByText("guidingPrinciple")).toBeVisible()
    },
  )

  test(
    "import headnote",
    { tag: ["@RISDEV-5888"] },
    async ({ page, linkedDocumentNumber, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, linkedDocumentNumber)
      await searchForDocumentUnitToImport(
        page,
        prefilledDocumentUnit.documentNumber,
      )
      await expect(
        page.getByText(prefilledDocumentUnit.coreData.fileNumbers![0]),
      ).toBeVisible()

      await expect(
        page.getByLabel("Orientierungssatz übernehmen"),
      ).toBeVisible()
      await page.getByLabel("Orientierungssatz übernehmen").click()

      await expect(page.getByText("Übernommen")).toBeVisible()
      await expect(page.getByText("testHeadnote")).toBeVisible()
    },
  )

  test(
    "import short texts not possible when target category filled",
    { tag: ["@RISDEV-5888"] },
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
