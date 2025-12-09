import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  fillCombobox,
  fillSelect,
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "object value (Gegenstandswert)",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-8810",
    },
    tag: ["@RISDEV-8810"],
  },
  () => {
    test("Gegenstandswert", async ({ page, prefilledDocumentUnit }) => {
      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      const otherCategoriesContainer = page.getByLabel("Weitere Rubriken")
      const objectValueButton = otherCategoriesContainer.getByRole("button", {
        name: "Gegenstandswert",
      })
      const amountLabel = otherCategoriesContainer.getByText("Betrag *")
      const currencyLabel = otherCategoriesContainer.getByText("Währung *")
      const proceedingLabel = otherCategoriesContainer.getByText("Verfahren", {
        exact: true,
      })

      const amountInput = otherCategoriesContainer.getByLabel("Betrag")
      const currencyInput = otherCategoriesContainer.getByLabel("Währung")
      const proceedingInput = otherCategoriesContainer.getByLabel("Verfahren", {
        exact: true,
      })

      const acceptButton = otherCategoriesContainer.getByLabel(
        "Gegenstandswert speichern",
      )

      await test.step("Gegenstandswert wird als Kategorie-Button angezeigt", async () => {
        await expect(objectValueButton).toBeVisible()
        await expect(amountLabel).toBeHidden()
        await expect(currencyLabel).toBeHidden()
        await expect(proceedingLabel).toBeHidden()
      })

      await test.step("Klicke auf Kategorie-Button", async () => {
        await objectValueButton.click()
      })

      await test.step("Gegenstandswert besteht aus drei Inputfeldern", async () => {
        await expect(amountInput).toBeVisible()
        await expect(currencyInput).toBeVisible()
        await expect(proceedingInput).toBeVisible()
      })

      await test.step("Nur 'Verfahren' ausfüllen", async () => {
        await fillSelect(page, "Verfahren", "Verfassungsbeschwerde")
      })

      await test.step("Ohne Pflichtfelder kann nicht übernommen werden", async () => {
        await expect(acceptButton).toBeVisible()
        await expect(acceptButton).toBeDisabled()
      })

      await test.step("'Betrag *' und 'Währung *' ausfüllen", async () => {
        const input = page.getByLabel("Betrag", { exact: true })
        await input.fill("10000")
        await page.keyboard.press("Enter")
        await expect(page.getByLabel("Betrag", { exact: true })).toHaveValue(
          "10.000",
        )
        await fillCombobox(page, "Währung", "Euro (EUR)")
      })

      await test.step("Mit Pflichtfeldern kann übernommen werden", async () => {
        await expect(acceptButton).toBeVisible()
        await expect(acceptButton).toBeEnabled()

        await acceptButton.click()
        await save(page)
      })

      await test.step("Zusammenfassung wird angezeigt", async () => {
        await expect(
          otherCategoriesContainer.getByText(
            "10.000 Euro (EUR), Verfassungsbeschwerde",
          ),
        ).toBeVisible()
      })

      await test.step("Gegenstandswert erscheint in der Vorschau", async () => {
        await navigateToPreview(page, prefilledDocumentUnit.documentNumber)
        await expect(page.getByText("Gegenstandswert")).toBeVisible()
        await expect(
          page.getByText("10.000 Euro (EUR), Verfassungsbeschwerde"),
        ).toBeVisible()
      })

      await test.step("Auf Übergabeseite erscheint Warnung, dass die Rubrik nicht übergeben wird", async () => {
        await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
        await expect(
          page.getByText(
            "Folgende Rubriken sind befüllt und können nicht an die jDV exportiert werden",
          ),
        ).toBeVisible()
        await expect(page.getByText("Gegenstandswert")).toBeVisible()
        await page
          .getByRole("button", {
            name: "Dokumentationseinheit an jDV übergeben",
          })
          .click()
        await expect(
          page.getByRole("button", { name: "Trotzdem übergeben" }),
        ).toBeVisible()
      })
    })
  },
)
