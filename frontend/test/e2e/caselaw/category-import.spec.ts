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
      .fill("")
    await page.getByLabel("Dokumentnummer Eingabefeld").click()
    await page.keyboard.type(documentNumber)
    await expect(
      page.getByRole("button", { name: "Dokumentationseinheit laden" }),
    ).toBeEnabled()
    await page
      .getByRole("button", { name: "Dokumentationseinheit laden" })
      .click()
  }
})
