import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "country of origin (Herkunftsland)",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-9884",
    },
    tag: ["@RISDEV-9884"],
  },
  () => {
    test("Herkunftsland", async ({ page, prefilledDocumentUnit }) => {
      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      const otherCategoriesContainer = page.getByLabel("Weitere Rubriken")
      const countryOfOriginButton = otherCategoriesContainer.getByRole(
        "button",
        {
          name: "Herkunftsland",
        },
      )
      const countryInput = otherCategoriesContainer.getByRole("combobox", {
        name: "Landbezeichnung",
      })
      const fieldOfLawInput = otherCategoriesContainer.getByRole("combobox", {
        name: "Rechtlicher Rahmen",
      })

      const acceptButton = otherCategoriesContainer.getByLabel(
        "Herkunftsland speichern",
      )

      await test.step("Herkunftsland wird als Kategorie-Button angezeigt", async () => {
        await expect(countryOfOriginButton).toBeVisible()
        await expect(countryInput).toBeHidden()
        await expect(fieldOfLawInput).toBeHidden()
      })

      await test.step("Klicke auf Kategorie-Button", async () => {
        await countryOfOriginButton.click()
      })

      await test.step("Rechtlichen Rahmen ausfüllen", async () => {
        await fieldOfLawInput.fill("AR-01-01-0")
        await page.getByRole("option", { name: "AR-01-01-01" }).click()
      })

      await test.step("Ohne Landbezeichnung kann nicht übernommen werden", async () => {
        await expect(acceptButton).toBeVisible()
        await expect(acceptButton).toBeDisabled()
      })

      await test.step("Landbezeichnung ausfüllen", async () => {
        await countryInput.fill("DEU")
        await page.getByRole("option", { name: "RE-07-DEU" }).click()
      })

      await test.step("Mit Landbezeichnung kann übernommen werden", async () => {
        await expect(acceptButton).toBeVisible()
        await expect(acceptButton).toBeEnabled()

        await acceptButton.click()
        await save(page)
      })

      await test.step("Zusammenfassung wird angezeigt", async () => {
        await expect(
          otherCategoriesContainer.getByText(
            "RE-07-DEU Deutschland, AR-01-01-01 ",
          ),
        ).toBeVisible()
      })

      await test.step("Erscheint in der Vorschau", async () => {
        await navigateToPreview(page, prefilledDocumentUnit.documentNumber)
        await expect(page.getByText("Herkunftsland")).toBeVisible()
        await expect(
          page.getByText(
            "RE-07-DEU Deutschland, AR-01-01-01 Verschulden bei Vertragsschluss (culpa in contrahendo)",
          ),
        ).toBeVisible()
      })

      await test.step("Auf Übergabeseite erscheint Warnung, dass die Rubrik nicht übergeben wird", async () => {
        await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
        await expect(
          page.getByText(
            "Folgende Rubriken sind befüllt und können nicht an die jDV exportiert werden",
          ),
        ).toBeVisible()
        await expect(page.getByText("Herkunftsland")).toBeVisible()
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

    test("Altwerte werden angezeigt und gelöscht", async ({
      page,
      prefilledDocumentUnitWithLegacyCountryOfOrigin,
    }) => {
      await navigateToCategories(
        page,
        prefilledDocumentUnitWithLegacyCountryOfOrigin.documentNumber,
      )

      const otherCategoriesContainer = page.getByLabel("Weitere Rubriken")
      const countryInput = otherCategoriesContainer.getByRole("combobox", {
        name: "Landbezeichnung",
      })

      const acceptButton = otherCategoriesContainer.getByLabel(
        "Herkunftsland speichern",
      )

      await test.step("Zusammenfassung wird angezeigt", async () => {
        await expect(
          otherCategoriesContainer.getByText("Altwert legacy value"),
        ).toBeVisible()
      })

      await test.step("Altwert erscheint in der Vorschau", async () => {
        await navigateToPreview(
          page,
          prefilledDocumentUnitWithLegacyCountryOfOrigin.documentNumber,
        )
        await expect(page.getByText("Herkunftsland")).toBeVisible()
        await expect(page.getByText("legacy value")).toBeVisible()
      })

      await test.step("Landbezeichnung ausfüllen", async () => {
        await navigateToCategories(
          page,
          prefilledDocumentUnitWithLegacyCountryOfOrigin.documentNumber,
        )

        await otherCategoriesContainer
          .getByRole("button", { name: "Eintrag bearbeiten" })
          .click()
        await expect(countryInput).toBeVisible()

        await countryInput.fill("DEU")
        await page.getByRole("option", { name: "RE-07-DEU" }).click()
        await expect(countryInput).toHaveValue("RE-07-DEU")

        await acceptButton.click()
        await save(page)
      })

      await test.step("Zusammenfassung wird angezeigt, altwert ist nichtmehr sichtbar", async () => {
        await expect(
          otherCategoriesContainer.getByText("RE-07-DEU Deutschland"),
        ).toBeVisible()
        await expect(page.getByText("Altwert")).toBeHidden()
      })
    })
  },
)
