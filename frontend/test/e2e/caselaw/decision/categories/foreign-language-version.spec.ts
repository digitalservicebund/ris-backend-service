import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToCategories,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"
import { languages } from "~/e2e/testdata"

test.describe(
  "foreign language version (Fremdsprachige Fassung)",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-8557",
    },
    tag: ["@RISDEV-8557"],
  },
  () => {
    test("Fremdsprachige Fassung", async ({ page, prefilledDocumentUnit }) => {
      await navigateToCategories(page, prefilledDocumentUnit.documentNumber!)

      const otherCategoriesContainer = page.getByLabel("Weitere Rubriken")
      const foreignLanguageVersionButton = otherCategoriesContainer.getByRole(
        "button",
        {
          name: "Fremdsprachige Fassung",
        },
      )
      await test.step("Fremdsprachige Fassung wird in den weiteren Rubriken angezeigt", async () => {
        await expect(foreignLanguageVersionButton).toBeVisible()
        await expect(otherCategoriesContainer.getByText("Sprache")).toBeHidden()
        await expect(otherCategoriesContainer.getByText("Link")).toBeHidden()
      })

      await test.step("Fremdsprachige Fassung hat zwei Inputfelder: Sprache + Link", async () => {
        await foreignLanguageVersionButton.click()
        await expect(
          otherCategoriesContainer.getByText("Sprache"),
        ).toBeVisible()
        await expect(otherCategoriesContainer.getByText("Link")).toBeVisible()
        await expect(
          otherCategoriesContainer.getByTestId(
            "foreign-language-version-language-input",
          ),
        ).toBeVisible()
        await expect(
          otherCategoriesContainer.getByTestId(
            "foreign-language-version-link-input",
          ),
        ).toBeVisible()
      })

      await test.step("'Sprache' zeigt bei Klick in die Combobox alle ISO-639-Sprachcodes", async () => {
        await otherCategoriesContainer
          .getByTestId("foreign-language-version-language-input")
          .click()

        await expect(page.getByTestId("combobox-spinner")).toBeHidden()

        const dropdownOptions = await otherCategoriesContainer
          .getByTestId("foreign-language-version-language-input")
          .getByLabel("dropdown-option")
          .all()

        const actualTexts = await Promise.all(
          dropdownOptions.map((opt) => opt.textContent()),
        )
        expect(actualTexts).toEqual(languages)
      })

      await test.step("'Sprache' zeigt bei Suche zwei gefilterte ISO-639-Sprachcodes", async () => {
        await otherCategoriesContainer
          .getByTestId("foreign-language-version-language")
          .locator("input")
          .fill("Aa")

        await expect(page.getByTestId("combobox-spinner")).toBeHidden()

        const dropdownOptions = await otherCategoriesContainer
          .getByTestId("foreign-language-version-language-input")
          .getByLabel("dropdown-option")
          .all()

        const actualTexts = await Promise.all(
          dropdownOptions.map((opt) => opt.textContent()),
        )

        expect(actualTexts).toEqual(["Afrikaans", "Kalaallisut"])
        await dropdownOptions[0].click()
      })

      await test.step("'Übernehmen'-Button ist nur enabled, wenn Sprache und Link gesetzt sind", async () => {
        const acceptButton = otherCategoriesContainer.getByLabel(
          "Fremdsprachige Fassung speichern",
        )

        await expect(acceptButton).toBeVisible()

        await expect(acceptButton).toBeDisabled()

        await otherCategoriesContainer
          .getByTestId("foreign-language-version-link-input")
          .fill("https://link-to-tranlsation.af")

        await expect(acceptButton).toBeEnabled()
        await acceptButton.click()
        await expect(
          otherCategoriesContainer.getByText(
            "Afrikaans: https://link-to-tranlsation.af",
          ),
        ).toBeVisible()
      })

      await test.step("Mehrfacheinträge sind möglich", async () => {
        const acceptButton = otherCategoriesContainer.getByLabel(
          "Fremdsprachige Fassung speichern",
        )

        await otherCategoriesContainer
          .getByTestId("foreign-language-version-language")
          .locator("input")
          .fill("Fra")

        await expect(page.getByTestId("combobox-spinner")).toBeHidden()

        await otherCategoriesContainer
          .getByTestId("foreign-language-version-language-input")
          .getByLabel("dropdown-option")
          .click()

        await otherCategoriesContainer
          .getByTestId("foreign-language-version-link-input")
          .fill("https://link-to-tranlsation.fr")

        await acceptButton.click()
        await expect(
          otherCategoriesContainer.getByText(
            "Französisch: https://link-to-tranlsation.fr",
          ),
        ).toBeVisible()
        await save(page)
      })

      await test.step("Fremdsprachige Fassung erscheint in der Vorschau", async () => {
        await navigateToPreview(page, prefilledDocumentUnit.documentNumber!)
        await expect(page.getByText("Fremdsprachige Fassung")).toBeVisible()
        await expect(
          page.getByText("Afrikaans: https://link-to-tranlsation.af"),
        ).toBeVisible()
        await expect(
          page.getByText("Französisch: https://link-to-tranlsation.fr"),
        ).toBeVisible()
      })

      await test.step("Der Link ist in der Vorschau klickbar", async () => {
        const firstLink = page.getByText("https://link-to-tranlsation.af")
        const secondLink = page.getByText("https://link-to-tranlsation.fr")
        await expect(firstLink).toBeEnabled()
        await firstLink.click({ trial: true })
        await expect(secondLink).toBeEnabled()
        await secondLink.click({ trial: true })
      })
    })
  },
)
