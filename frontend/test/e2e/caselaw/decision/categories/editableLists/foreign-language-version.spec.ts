import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToCategories,
  navigateToHandover,
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
      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

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

        // Locally, our db collation is different -> different sorting
        // eslint-disable-next-line playwright/no-conditional-in-test
        if (process.env.E2E_BASE_URL) {
          // eslint-disable-next-line playwright/no-conditional-expect
          expect(actualTexts).toEqual(languages)
        } else {
          // eslint-disable-next-line playwright/no-conditional-expect
          expect(actualTexts.slice().sort()).toEqual(languages.slice().sort())
        }
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
          .fill("https://link-to-translation.af")

        await expect(acceptButton).toBeEnabled()
        await acceptButton.click()
        await expect(
          otherCategoriesContainer.getByText(
            "Afrikaans: https://link-to-translation.af",
          ),
        ).toBeVisible()
        const link = page.getByText("https://link-to-translation.af")
        // Link is clickable in the editable list summary
        await link.click({ trial: true })
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
          .fill("www.link-to-translation.fr")

        await acceptButton.click()
        await expect(
          otherCategoriesContainer.getByText(
            "Französisch: www.link-to-translation.fr",
          ),
        ).toBeVisible()
        await save(page)
      })

      await test.step("Fremdsprachige Fassung erscheint in der Vorschau", async () => {
        await navigateToPreview(page, prefilledDocumentUnit.documentNumber)
        await expect(page.getByText("Fremdsprachige Fassung")).toBeVisible()
        await expect(
          page.getByText("Afrikaans: https://link-to-translation.af"),
        ).toBeVisible()
        await expect(
          page.getByText("Französisch: www.link-to-translation.fr"),
        ).toBeVisible()
      })

      await test.step("Der Link ist in der Vorschau klickbar", async () => {
        const firstLink = page.getByText("https://link-to-translation.af")
        const secondLink = page.getByText("www.link-to-translation.fr")
        await expect(firstLink).toBeEnabled()
        await expect(firstLink).toHaveAttribute(
          "href",
          "https://link-to-translation.af",
        )
        await firstLink.click({ trial: true })
        await expect(secondLink).toBeEnabled()
        await expect(secondLink).toHaveAttribute(
          "href",
          "https://www.link-to-translation.fr",
        )
        await secondLink.click({ trial: true })
      })

      await test.step("Auf Übergabeseite erscheint Warnung, dass die Rubrik nicht übergeben wird", async () => {
        await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
        await expect(
          page.getByText(
            "Folgende Rubriken sind befüllt und können nicht an die jDV exportiert werden",
          ),
        ).toBeVisible()
        await expect(page.getByText("Fremdsprachige Fassung")).toBeVisible()
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
