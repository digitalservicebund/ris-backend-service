import { expect, Page } from "@playwright/test"
import { navigateToCategories } from "./e2e-utils"
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

  test(
    "import keywords",
    { tag: ["@RISDEV-5720"] },
    async ({ page, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(
        page,
        prefilledDocumentUnit.documentNumber as string,
      )
      await searchForDocumentUnitToImport(page, "YYTestDoc0013")
      await expect(page.getByText("fileNumber5")).toBeVisible()

      await expect(page.getByLabel("Schlagwörter übernehmen")).toBeVisible()
      await page.getByLabel("Schlagwörter übernehmen").click()

      await expect(page.getByText("Übernommen")).toBeVisible()
      await expect(page.getByText("keyword1")).toBeVisible()
    },
  )

  test(
    "import fields of law",
    { tag: ["@RISDEV-5886"] },
    async ({ page, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(
        page,
        prefilledDocumentUnit.documentNumber as string,
      )
      await searchForDocumentUnitToImport(page, "YYTestDoc0013")
      await expect(page.getByText("fileNumber5")).toBeVisible()

      await expect(page.getByLabel("Sachgebiete übernehmen")).toBeVisible()
      await page.getByLabel("Sachgebiete übernehmen").click()

      await expect(page.getByText("Übernommen")).toBeVisible()
      await expect(page.getByText("AR-01")).toBeVisible()
    },
  )

  test(
    "import norms",
    { tag: ["@RISDEV-5887"] },
    async ({ page, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(
        page,
        prefilledDocumentUnit.documentNumber as string,
      )
      await searchForDocumentUnitToImport(page, "YYTestDoc0013")
      await expect(page.getByText("fileNumber5")).toBeVisible()

      await expect(page.getByLabel("Normen übernehmen")).toBeVisible()
      await page.getByLabel("Normen übernehmen").click()

      await expect(page.getByText("Übernommen")).toBeVisible()
      await expect(page.getByText("BGB")).toBeVisible()
    },
  )

  test(
    "import active citations",
    { tag: ["@RISDEV-5888"] },
    async ({ page, prefilledDocumentUnit }) => {
      await navigateToCategoryImport(page, prefilledDocumentUnit.documentNumber)
      await searchForDocumentUnitToImport(page, "YYTestDoc0013")
      await expect(page.getByText("fileNumber5")).toBeVisible()

      await expect(page.getByLabel("Aktivzitierung übernehmen")).toBeVisible()
      await page.getByLabel("Aktivzitierung übernehmen").click()

      await expect(page.getByText("Übernommen")).toBeVisible()
      await expect(page.getByText("Änderung, BVerwG, 09.09.1987")).toBeVisible()
      await expect(
        page.getByRole("button", { name: "YYTestDoc0013" }),
      ).toBeVisible()
    },
  )

  async function navigateToCategoryImport(page: Page, documentNumber: string) {
    await navigateToCategories(page, documentNumber)
    await page.getByLabel("Seitenpanel öffnen").click()
    await page.getByLabel("Rubriken-Import anzeigen").click()

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
