import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  clickCategoryButton,
  fillCombobox,
  fillInput,
  fillSelect,
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "origin of translation (Herkunft der Übersetzung)",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-8624",
    },
    tag: ["@RISDEV-8624"],
  },
  () => {
    test("Herkunft der Übersetzung", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      const otherCategoriesContainer = page.getByLabel("Weitere Rubriken")
      const originOfTranslationButton = otherCategoriesContainer.getByRole(
        "button",
        {
          name: "Herkunft der Übersetzung",
        },
      )
      const originalLanguageLabel =
        otherCategoriesContainer.getByText("Originalsprache *")
      const translatorsLabel =
        otherCategoriesContainer.getByText("Übersetzer:innen")
      const borderNumbersLabel = otherCategoriesContainer.getByText(
        "Fundstelle: Interne Verlinkung",
      )
      const urlsLabel = otherCategoriesContainer.getByText(
        "Fundstelle: Externe Verlinkung",
      )
      const translationTypeLabel = otherCategoriesContainer.getByText(
        "Übersetzungsart",
        { exact: true },
      )

      const originalLanguageInput = otherCategoriesContainer.getByTestId(
        "origin-of-translation-language-input",
      )
      const translatorsInput = otherCategoriesContainer.getByTestId(
        "origin-of-translation-translators-input",
      )
      const borderNumbersInput = otherCategoriesContainer.getByTestId(
        "origin-of-translation-border-numbers-input",
      )
      const urlsInput = otherCategoriesContainer.getByTestId(
        "origin-of-translation-urls-input",
      )
      const translationTypeInput = otherCategoriesContainer.getByTestId(
        "origin-of-translation-translation-type",
      )

      const acceptButton = otherCategoriesContainer.getByLabel(
        "Herkunft der Übersetzung speichern",
      )

      await test.step("Herkunft der Übersetzung wird als Kategorie-Button angezeigt", async () => {
        await expect(originOfTranslationButton).toBeVisible()
        await expect(originalLanguageLabel).toBeHidden()
        await expect(translatorsLabel).toBeHidden()
        await expect(borderNumbersLabel).toBeHidden()
        await expect(urlsLabel).toBeHidden()
        await expect(translationTypeLabel).toBeHidden()
      })

      await test.step("Klicke auf Kategorie-Button", async () => {
        await originOfTranslationButton.click()
      })

      await test.step("Herkunft der Übersetzung besteht aus fünf Inputfeldern", async () => {
        await expect(originalLanguageLabel).toBeVisible()
        await expect(translatorsLabel).toBeVisible()
        await expect(borderNumbersLabel).toBeVisible()
        await expect(urlsLabel).toBeVisible()
        await expect(translationTypeLabel).toBeVisible()

        await expect(originalLanguageInput).toBeVisible()
        await expect(translatorsInput).toBeVisible()
        await expect(borderNumbersInput).toBeVisible()
        await expect(urlsInput).toBeVisible()
        await expect(translationTypeInput).toBeVisible()
      })

      await test.step("Eintrag mit nicht existierender Randnummer erzeugt Warnung", async () => {
        await fillInput(page, "Fundstelle: Interne Verlinkung", "1")
        await page.keyboard.press("Enter")
        await expect(page.getByText("Randnummer existiert nicht")).toBeVisible()
      })

      await test.step("Füge Randnummer zu Gründe hinzu", async () => {
        await clickCategoryButton("Gründe", page)
        const reasons = page.getByTestId("Gründe")
        await reasons.click()
        await page.keyboard.type(`Some Dummy Text with border number`)
        await page.keyboard.press(`ControlOrMeta+Alt+.`)
        await save(page)
      })

      await test.step("Alle Inputfelder ausfüllen (außer 'Originalsprache')", async () => {
        await translatorsInput.locator("input").fill("Maxi Muster")
        await page.keyboard.press("Enter")

        await borderNumbersInput.locator("input").fill("1")
        await page.keyboard.press("Enter")

        await urlsInput.locator("input").fill("www.link-to-translation.fr")
        await page.keyboard.press("Enter")

        await fillSelect(page, "Übersetzungsart", "Amtlich")
      })

      await test.step("Ohne 'Originalsprache *' kann nicht übernommen werden", async () => {
        await expect(acceptButton).toBeVisible()
        await expect(acceptButton).toBeDisabled()
      })

      await test.step("'Originalsprache *' ausfüllen", async () => {
        await fillCombobox(page, "Originalsprache", "Französisch")
      })

      await test.step("Mit 'Originalsprache *' kann übernommen werden", async () => {
        await expect(acceptButton).toBeVisible()
        await expect(acceptButton).toBeEnabled()

        await acceptButton.click()
        await save(page)
      })

      await test.step("Zusammenfassung wird angezeigt", async () => {
        await expect(
          otherCategoriesContainer.getByText(
            "Französisch, Maxi Muster: 1, www.link-to-translation.fr (Amtlich)",
          ),
        ).toBeVisible()
      })

      await test.step("Herkunft der Übersetzung erscheint in der Vorschau", async () => {
        await navigateToPreview(page, prefilledDocumentUnit.documentNumber)
        await expect(page.getByText("Herkunft der Übersetzung")).toBeVisible()
        await expect(
          page.getByText(
            "Französisch, Maxi Muster: 1, www.link-to-translation.fr (Amtlich)",
          ),
        ).toBeVisible()
      })

      await test.step("Der Link ist in der Vorschau klickbar", async () => {
        const link = page.getByText("www.link-to-translation.fr")
        await expect(link).toBeEnabled()
        await expect(link).toHaveAttribute(
          "href",
          "https://www.link-to-translation.fr",
        )
        await link.click({ trial: true })
      })

      await test.step("Auf Übergabeseite erscheint Warnung, dass die Rubrik nicht übergeben wird", async () => {
        await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
        await expect(
          page.getByText(
            "Folgende Rubriken sind befüllt und können nicht an die jDV exportiert werden",
          ),
        ).toBeVisible()
        await expect(page.getByText("Herkunft der Übersetzung")).toBeVisible()
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
