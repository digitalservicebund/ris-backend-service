import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  navigateToPublication,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "related pending proceedings (Verknüpfung anhängiges Verfahren)",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-8678",
    },
    tag: ["@RISDEV-8678"],
  },
  () => {
    test("Verknüpfung anhängiges Verfahren", async ({
      page,
      pendingProceeding,
      prefilledDocumentUnit,
    }) => {
      await navigateToCategories(page, pendingProceeding.documentNumber, {
        type: "pending-proceeding",
      })
      const coreData = page.getByLabel("Formaldaten")
      await coreData
        .getByRole("textbox", { name: "Gericht", exact: true })
        .fill("AG Aachen")
      await page.getByText("AG Aachen").click()
      await coreData
        .getByRole("textbox", { name: "Entscheidungsdatum" })
        .fill("31.12.2019")
      await save(page)

      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      const otherCategoriesContainer = page.getByLabel("Weitere Rubriken")
      const categoryButton = otherCategoriesContainer.getByRole("button", {
        name: "Verknüpfung anhängiges Verfahren",
      })
      const documentNumberInput = otherCategoriesContainer.getByRole(
        "textbox",
        {
          name: "Dokumentnummer",
        },
      )
      const fileNumberInput = otherCategoriesContainer.getByRole("textbox", {
        name: "Aktenzeichen",
      })

      const searchButton = otherCategoriesContainer.getByLabel(
        "Nach anhängigen Verfahren suchen",
      )

      await test.step("Verknüpfung anhängiges Verfahren wird als Kategorie-Button angezeigt", async () => {
        await expect(categoryButton).toBeVisible()
        await expect(documentNumberInput).toBeHidden()
        await expect(fileNumberInput).toBeHidden()
        await expect(searchButton).toBeHidden()
      })

      await test.step("Klicke auf Kategorie-Button", async () => {
        await categoryButton.click()
      })

      await test.step("Dokumentnummer, Aktenzeichen und Suchbutton sind sichtbar", async () => {
        await expect(categoryButton).toBeHidden()
        await expect(documentNumberInput).toBeVisible()
        await expect(fileNumberInput).toBeVisible()
        await expect(searchButton).toBeVisible()
        await expect(searchButton).toBeEnabled()
      })

      await test.step("Nach Aktenzeichen suchen ergibt einen Treffer", async () => {
        await fileNumberInput.fill(pendingProceeding.coreData.fileNumbers![0])
        await page.keyboard.press("Enter")
        await searchButton.click()
        await expect(page.getByText("1 Ergebnis gefunden")).toBeVisible()
        await expect(
          otherCategoriesContainer.getByText(
            `AG Aachen, 31.12.2019, ${pendingProceeding.coreData.fileNumbers?.[0]}, Anhängiges Verfahren, Unveröffentlicht | ${pendingProceeding.documentNumber}`,
          ),
        ).toBeVisible()
        await fileNumberInput.clear()
      })

      await test.step("Nach Dokumentnummer suchen und Treffer übernehmen", async () => {
        await documentNumberInput.fill(pendingProceeding.documentNumber)
        await page.keyboard.press("Enter")
        await searchButton.click()
        await expect(page.getByText("1 Ergebnis gefunden")).toBeVisible()
        await expect(
          otherCategoriesContainer.getByText(
            `AG Aachen, 31.12.2019, ${pendingProceeding.coreData.fileNumbers?.[0]}, Anhängiges Verfahren, Unveröffentlicht | ${pendingProceeding.documentNumber}`,
          ),
        ).toBeVisible()
        await otherCategoriesContainer.getByLabel("Treffer übernehmen").click()
        await save(page)
      })

      await test.step("Zusammenfassung wird angezeigt", async () => {
        await expect(
          otherCategoriesContainer.getByText(
            `AG Aachen, 31.12.2019, ${pendingProceeding.coreData.fileNumbers?.[0]}, Anhängiges Verfahren | ${pendingProceeding.documentNumber}`,
          ),
        ).toBeVisible()
      })

      await test.step("Erscheint in der Vorschau", async () => {
        await navigateToPreview(page, prefilledDocumentUnit.documentNumber)
        await expect(
          page.getByText("Verknüpfung anhängiges Verfahren"),
        ).toBeVisible()
        await expect(
          page.getByText(
            `AG Aachen, 31.12.2019, ${pendingProceeding.coreData.fileNumbers?.[0]}, Anhängiges Verfahren`,
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
        await expect(
          page.getByText("Verknüpfung anhängiges Verfahren"),
        ).toBeVisible()
        await page
          .getByRole("button", {
            name: "Dokumentationseinheit an jDV übergeben",
          })
          .click()
        await expect(
          page.getByRole("button", { name: "Trotzdem übergeben" }),
        ).toBeVisible()
      })

      await test.step("Auf der Veröffentlichen Seite werden die verknüpften anhängigen Verfahren aufgelistet", async () => {
        await navigateToPublication(page, prefilledDocumentUnit.documentNumber)
        await expect(
          page.getByText(
            "Mit dieser Entscheidung sind folgende anhängige Verfahren verknüpft:",
          ),
        ).toBeVisible()
        await expect(
          page.getByText(
            `${pendingProceeding.coreData.fileNumbers?.[0]}, Anhängiges Verfahren | ${pendingProceeding.documentNumber}`,
          ),
        ).toBeVisible()
      })

      await test.step("Das verknüpfte anhängige Verfahren ist noch nicht erledigt", async () => {
        await navigateToPreview(page, pendingProceeding.documentNumber, {
          type: "pending-proceeding",
        })
        await expect(
          page.getByText("Erledigung", { exact: true }),
        ).toBeVisible()
        await expect(page.getByText("Nein")).toBeVisible()
        await expect(page.getByText("Erledigungsvermerk")).toBeHidden()
      })

      await test.step("Veröffentliche die Entscheidung", async () => {
        await navigateToPublication(page, prefilledDocumentUnit.documentNumber)
        const publishButton = page.getByRole("button", {
          name: "Veröffentlichen",
        })
        await publishButton.click()
      })

      await test.step("Das verknüpfte anhängige Verfahren wurde auf erledigt gesetzt", async () => {
        await navigateToPreview(page, pendingProceeding.documentNumber, {
          type: "pending-proceeding",
        })
        await expect(
          page.getByText("Erledigung", { exact: true }),
        ).toBeVisible()
        await expect(page.getByText("Ja")).toBeVisible()
        await expect(page.getByText("Erledigungsvermerk")).toBeVisible()
        await expect(
          page.getByText(
            "Erledigt durch " + prefilledDocumentUnit.documentNumber,
          ),
        ).toBeVisible()
      })
    })
  },
)
