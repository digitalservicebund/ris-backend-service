import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  clickCategoryButton,
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Berichtigung",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-8622",
    },
    tag: ["@RISDEV-8622"],
  },
  () => {
    test("Berichtigung", async ({ page, prefilledDocumentUnit }) => {
      const documentNumber = prefilledDocumentUnit.documentNumber
      await navigateToCategories(page, documentNumber)

      const categoryButton = page.getByRole("button", {
        name: "Berichtigung",
        exact: true,
      })

      await test.step("Berichtigung ist sichtbar", async () => {
        await expect(categoryButton).toBeVisible()
      })

      await test.step(`Berichtigung lässt sich öffnen`, async () => {
        await categoryButton.click()
        await expect(page.getByText("Art der Eintragung")).toBeVisible()
      })

      await test.step("Eintrag mit nicht existierender Randnummer erzeugt Warnung", async () => {
        await page
          .getByLabel("Randnummern der Änderung")
          .getByRole("textbox")
          .fill("1")
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

      await test.step("Berichtigung kann ausgefüllt werden", async () => {
        await page
          .getByRole("combobox", {
            name: "Art der Eintragung",
          })
          .click()
        await page
          .getByRole("option", { name: "Schreibfehlerberichtigung" })
          .click()
        await page
          .getByRole("textbox", {
            name: "Art der Änderung",
          })
          .fill("Hauffen -> Haufen")
        await page
          .getByRole("textbox", {
            name: "Datum der Änderung",
          })
          .fill("24.12.2024")
        const borderNumberInput = page.getByRole("group", {
          name: "Randnummern der Änderung",
        })
        await expect(borderNumberInput).toBeVisible()
        await borderNumberInput.getByRole("textbox").fill("1")
        await page.keyboard.press("Enter")

        const textField = page.getByTestId("correctionContent-editor")
        await textField.click()
        await page.keyboard.type("Ersetzen von 'Hauffen' mit 'Haufen'")
        const innerText = await textField.innerText()
        expect(innerText).toContain("Ersetzen von 'Hauffen' mit 'Haufen'")
      })

      await test.step("Klick auf Übernehmen übernimmt den Eintrag", async () => {
        await page
          .getByRole("button", { name: "Berichtigung speichern" })
          .click()
        await expect(page.getByText("Schreibfehlerberichtigung")).toHaveText(
          " Schreibfehlerberichtigung , Hauffen -> Haufen, 24.12.2024|1",
        )
      })

      await test.step("Speichern der Änderungen", async () => {
        await save(page)
      })

      await test.step("Berichtigungen erscheint in der Vorschau", async () => {
        await navigateToPreview(page, documentNumber)
        await expect(
          page.getByText("Berichtigung", { exact: true }),
        ).toBeVisible()

        await expect(
          page.getByText(
            " Schreibfehlerberichtigung , Hauffen -> Haufen, 24.12.2024|1",
          ),
        ).toBeVisible()
        await expect(
          page.getByText("Ersetzen von 'Hauffen' mit 'Haufen'"),
        ).toBeVisible()
      })

      await test.step("Auf Übergabeseite erscheint Warnung, dass Berichtigung nicht übergeben wird", async () => {
        await navigateToHandover(page, documentNumber)
        await expect(
          page.getByText(
            "Folgende Rubriken sind befüllt und können nicht an die jDV exportiert werden",
          ),
        ).toBeVisible()
        await expect(page.getByText("Berichtigung")).toBeVisible()
      })
    })
  },
)
