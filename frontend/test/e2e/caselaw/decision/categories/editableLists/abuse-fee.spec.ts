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
  "abuse fee (Missbrauchsgebühr)",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-9959",
    },
    tag: ["@RISDEV-9959"],
  },
  () => {
    test("Missbrauchsgebühr", async ({ page, prefilledDocumentUnit }) => {
      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      const otherCategoriesContainer = page.getByLabel("Weitere Rubriken")
      const abuseFeeButton = otherCategoriesContainer.getByRole("button", {
        name: "Missbrauchsgebühr",
      })
      const amountLabel = otherCategoriesContainer.getByText("Betrag *")
      const currencyLabel = otherCategoriesContainer.getByText("Währung *")
      const addresseeLabel = otherCategoriesContainer.getByText("Adressat", {
        exact: true,
      })

      const amountInput = otherCategoriesContainer.getByLabel("Betrag")
      const currencyInput = otherCategoriesContainer.getByLabel("Währung")
      const addresseeInput = otherCategoriesContainer.getByLabel("Adressat")

      const acceptButton = otherCategoriesContainer.getByLabel(
        "Missbrauchsgebühr speichern",
      )

      await test.step("Missbrauchsgebühr wird als Kategorie-Button angezeigt", async () => {
        await expect(abuseFeeButton).toBeVisible()
        await expect(amountLabel).toBeHidden()
        await expect(currencyLabel).toBeHidden()
        await expect(addresseeLabel).toBeHidden()
      })

      await test.step("Klicke auf Kategorie-Button", async () => {
        await abuseFeeButton.click()
      })

      await test.step("Missbrauchsgebühr besteht aus drei Inputfeldern", async () => {
        await expect(amountInput).toBeVisible()
        await expect(currencyInput).toBeVisible()
        await expect(addresseeInput).toBeVisible()
      })

      await test.step("Nur 'Adressat' ausfüllen", async () => {
        await fillSelect(page, "Adressat", "Bevollmächtigter")
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
            "10.000 Euro (EUR), Bevollmächtigter",
          ),
        ).toBeVisible()
      })

      await test.step("Missbrauchsgebühr erscheint in der Vorschau", async () => {
        await navigateToPreview(page, prefilledDocumentUnit.documentNumber)
        await expect(page.getByText("Missbrauchsgebühr")).toBeVisible()
        await expect(
          page.getByText("10.000 Euro (EUR), Bevollmächtigter"),
        ).toBeVisible()
      })

      await test.step("Auf Übergabeseite erscheint Warnung, dass die Rubrik nicht übergeben wird", async () => {
        await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
        await expect(
          page.getByText(
            "Folgende Rubriken sind befüllt und können nicht an die jDV exportiert werden",
          ),
        ).toBeVisible()
        await expect(page.getByText("Missbrauchsgebühr")).toBeVisible()
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
