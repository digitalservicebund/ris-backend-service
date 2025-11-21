import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  clickCategoryButton,
  fillInput,
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Definition",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-6688",
    },
    tag: ["@RISDEV-6688"],
  },
  () => {
    test("Definition hinzufügen und exportieren", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      const documentNumber = prefilledDocumentUnit.documentNumber
      await navigateToCategories(page, documentNumber)

      await test.step("Klicke auf Kategorie-Button Definition", async () => {
        await page.getByRole("button", { name: "Definition" }).click()
      })

      await test.step("Definierter Begriff ausfüllen", async () => {
        await fillInput(page, "Definierter Begriff", "indirekte Steuern")
      })

      const saveDefinitionButton = page.getByRole("button", {
        name: "Definition speichern",
      })
      await test.step("Definition kann ohne Randnummer gespeichert werden", async () => {
        await saveDefinitionButton.click()
        await save(page)
        await expect(
          page.getByTestId("Definitionen").getByTestId("list-entry-0"),
        ).toBeVisible()
        await expect(page.getByTestId("Definitionen")).toHaveText(
          /indirekte Steuern/,
        )
      })

      await test.step("Eintrag mit nicht existierender Randnummer kann nicht hinzugefügt werden", async () => {
        await fillInput(page, "Definierter Begriff", "Sachgesamtheit")
        await fillInput(page, "Definition des Begriffs", "1")
        await saveDefinitionButton.focus()
        await expect(page.getByText("Randnummer existiert nicht")).toBeVisible()
        await expect(saveDefinitionButton).toBeDisabled()
      })

      await test.step("Füge Randnummer zu Gründe hinzu", async () => {
        await clickCategoryButton("Gründe", page)
        const reasons = page.getByTestId("Gründe")
        await reasons.click()
        await page.keyboard.type(`Text`)
        await page.keyboard.press(`ControlOrMeta+Alt+.`)
        await save(page)
      })

      await test.step("Eintrag mit existierender Randnummer kann hinzugefügt werden", async () => {
        await page.getByLabel("Definition des Begriffs").focus()
        await saveDefinitionButton.focus()
        await expect(page.getByText("Randnummer existiert nicht")).toBeHidden()
        await expect(saveDefinitionButton).toBeEnabled()
        await saveDefinitionButton.click()
        await expect(
          page.getByTestId("Definitionen").getByTestId("list-entry-1"),
        ).toBeVisible()
        await expect(page.getByTestId("Definitionen")).toHaveText(
          /Sachgesamtheit \| 1/,
        )
        await save(page)
      })

      await test.step("Definition kann auf Übergabeseite exportiert werden", async () => {
        await navigateToHandover(page, documentNumber)
        await page.getByText("XML Vorschau").click()
        const xmlPreview = page.getByTitle("XML Vorschau")
        const innerText = await xmlPreview.innerText()

        const regexWithoutBorderNumber =
          /<zuordnung>\s*\d*\s*<aspekt>Definition<\/aspekt>\s*\d*\s*<begriff>indirekte Steuern<\/begriff>\s*\d*\s*<\/zuordnung>/
        expect(innerText).toMatch(regexWithoutBorderNumber)
        const regexWithBorderNumber =
          /<zuordnung>\s*\d*\s*<aspekt>Definition<\/aspekt>\s*\d*\s*<begriff>Sachgesamtheit\|Randnummer=1<\/begriff>\s*\d*\s*<\/zuordnung>/
        expect(innerText).toMatch(regexWithBorderNumber)
      })

      await test.step("Bei gelöschter Randnummer wird der Eintrag als fehlerhaft markiert", async () => {
        await navigateToCategories(page, documentNumber)
        await page.getByTestId("Gründe").click()
        await page.keyboard.press(`ControlOrMeta+Alt+-`)
        await save(page)
        await expect(
          page
            .getByTestId("Definitionen")
            .locator("span", { hasText: "1", hasNotText: "|" }),
        ).toHaveClass(
          'text-red-900 bg-red-200 before:content-["⚠Rd_"] pr-2 pl-2',
        )
      })
    })

    test("Definition editieren und löschen", async ({
      page,
      prefilledDocumentUnitWithTexts,
    }) => {
      const documentNumber = prefilledDocumentUnitWithTexts.documentNumber
      await navigateToCategories(page, documentNumber)

      await test.step("Existierende Definitionen sind nach dem Laden sichtbar", async () => {
        await expect(
          page.getByTestId("Definitionen").getByTestId("list-entry-0"),
        ).toBeVisible()
        await expect(page.getByTestId("Definitionen")).toHaveText(
          /Test Definition \| 2/,
        )
        await expect(
          page.getByTestId("Definitionen").getByTestId("list-entry-1"),
        ).toBeVisible()
        await expect(page.getByTestId("Definitionen")).toHaveText(
          /Test Definition2/,
        )
      })

      const deleteDefinitionButton = page
        .getByTestId("Definitionen")
        .getByRole("button", {
          name: "Eintrag löschen",
        })
      await test.step("Der erste Eintrag kann gelöscht werden", async () => {
        await page
          .getByTestId("Definitionen")
          .getByTestId("list-entry-0")
          .click()
        await deleteDefinitionButton.click()
        await expect(page.getByTestId("Definitionen")).not.toHaveText(
          /Test Definition \| 2/,
        )
      })

      const saveDefinitionButton = page.getByRole("button", {
        name: "Definition speichern",
      })
      await test.step("Der zweite Eintrag kann editiert werden", async () => {
        await page
          .getByTestId("Definitionen")
          .getByTestId("list-entry-0")
          .click()
        await fillInput(page, "Definierter Begriff", "indirekte Steuern")
        await saveDefinitionButton.click()
        await expect(page.getByTestId("Definitionen")).not.toHaveText(
          /Test Definition2/,
        )
        await expect(page.getByTestId("Definitionen")).toHaveText(
          /indirekte Steuern/,
        )
      })
    })

    test("Definition wird in Vorschau angezeigt", async ({
      page,
      prefilledDocumentUnitWithTexts,
    }) => {
      const documentNumber = prefilledDocumentUnitWithTexts.documentNumber
      await navigateToPreview(page, documentNumber)
      await test.step("Definition erscheint in der Vorschau", async () => {
        await expect(
          page.getByText("Definition", { exact: true }),
        ).toBeVisible()
        await expect(
          page.getByText("Test Definition | 2", { exact: true }),
        ).toBeVisible()
        await expect(
          page.getByText("Test Definition2", { exact: true }),
        ).toBeVisible()
      })
    })
  },
)
