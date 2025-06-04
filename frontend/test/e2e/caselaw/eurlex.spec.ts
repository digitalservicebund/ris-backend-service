import { expect } from "@playwright/test"
import { navigateToInbox } from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  addEurlexDecisions,
  cleanUpEurlexDecisions,
} from "~/e2e/caselaw/utils/documentation-unit-api-util"

test.describe("eurlex", () => {
  test(
    "Eurlex Entscheidungen können im EURLEX Postfach gesehen werden",
    { tag: ["@RISDEV-7376", "@RISDEV-7578"] },
    async ({ page }) => {
      const celexNumber1 = "62024CO0878"
      const celexNumber2 = "62023CJ0538"
      const celexNumber3 = "62019CV0001(02)"

      await addEurlexDecisions(page)

      const tab = page.getByTestId("eurlex-tab")
      await navigateToInbox(page)
      await expect(tab).toBeVisible()
      await tab.click()

      await test.step("Entscheidungen werden mit Metadaten angezeigt", async () => {
        const rows = page.locator("tr")
        // all eurlex test entries are visible
        await expect(page.getByText(celexNumber1)).toBeVisible()
        await expect(page.getByText(celexNumber2)).toBeVisible()
        await expect(page.getByText(celexNumber3)).toBeVisible()

        const rowWithCelexNumber1 = rows.filter({ hasText: celexNumber1 })
        await expect(rowWithCelexNumber1).toHaveCount(1)
        await expect(rowWithCelexNumber1).toContainText(celexNumber1)

        await expect(rowWithCelexNumber1).toContainText("EuG") // Gerichtstyp
        await expect(rowWithCelexNumber1).toContainText("-") // Gerichtsort
        await expect(rowWithCelexNumber1).toContainText("09.04.2025") // Datum
        await expect(rowWithCelexNumber1).toContainText("C-878/24") // Aktenzeichen
        await expect(
          rowWithCelexNumber1.getByLabel("Öffne Vorschau"),
        ).toBeVisible()

        const rowWithCelexNumber2 = rows.filter({ hasText: celexNumber2 })
        await expect(rowWithCelexNumber2).toHaveCount(1)
        await expect(rowWithCelexNumber2).toContainText(celexNumber2)
        await expect(rowWithCelexNumber2).toContainText("EuG") // Gerichtstyp
        await expect(rowWithCelexNumber2).toContainText("-") // Gerichtsort
        await expect(rowWithCelexNumber2).toContainText("22.05.2025") // Datum
        await expect(rowWithCelexNumber2).toContainText("C-538/23") // Aktenzeichen
        await expect(
          rowWithCelexNumber2.getByLabel("Öffne Vorschau"),
        ).toBeHidden()

        const rowWithCelexNumber3 = rows.filter({ hasText: celexNumber3 })
        await expect(rowWithCelexNumber3).toHaveCount(1)
        await expect(rowWithCelexNumber3).toContainText(celexNumber3)

        await expect(rowWithCelexNumber3).toContainText("EuGH") // Gerichtstyp
        await expect(rowWithCelexNumber3).toContainText("-") // Gerichtsort
        await expect(rowWithCelexNumber3).toContainText("06.10.2021") // Datum
        //    await expect(rowWithCelexNumber3).toContainText("Avis 1/19") // Aktenzeichen
        await expect(
          rowWithCelexNumber3.getByLabel("Öffne Vorschau"),
        ).toBeVisible()
      })

      await test.step("Entscheidungen können gefiltert werden", async () => {
        const courtType = page.getByLabel("CELEX-Nummer Suche")
        await courtType.fill(celexNumber1)
        await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
      })

      await test.step("Tab wechseln setzt die Suche zurück", async () => {
        await page.getByTestId("eu-tab").click()

        // Wait for the URL tab param to update
        await page.waitForFunction(() => {
          return window.location.href.includes("eu-rechtsprechung")
        })
        await page.getByTestId("eurlex-tab").click()
        await page.waitForFunction(() => {
          return window.location.href.includes("eur-lex")
        })

        await expect(page.getByLabel("CELEX-Nummer Suche")).toHaveText("")
      })

      await test.step("Eine Entscheidung kann an eine andere Dok-Stelle zugewiesen werden", async () => {
        const rows = page.locator("tr")
        const row1 = rows.filter({ hasText: celexNumber1 })

        // all eurlex test entries are visible
        await expect(page.getByText(celexNumber1)).toBeVisible()
        await expect(page.getByText(celexNumber2)).toBeVisible()

        await row1.getByRole("checkbox").click()

        await page.locator("#documentationOfficeSelector").click()
        await page.getByText("BGH").click()
        await page.getByLabel("Dokumentationsstelle zuweisen").click()

        await expect(
          page.getByText(
            "Die Dokumentationseinheit wurde der Dokumentationsstelle BGH zugewiesen.",
          ),
        ).toBeVisible()

        // the assigned entry is not visible anymore
        await expect(page.getByText(celexNumber1)).toBeHidden()
        await expect(page.getByText(celexNumber2)).toBeVisible()
        await expect(page.getByText(celexNumber3)).toBeVisible()
      })

      await test.step("Alle verbleibenden Entscheidungen können an meine Dok-Stelle zugewiesen werden", async () => {
        await page.getByTestId("eurlex-tab").click()
        // Wait for the URL tab param to update
        await page.waitForFunction(() => {
          return window.location.href.includes("eur-lex")
        })
        await expect(page.getByText(celexNumber2)).toBeVisible()

        await page
          .getByRole("checkbox", {
            name: "Alle Elemente abgewählt",
          })
          .click()

        await expect(
          page.getByRole("checkbox", {
            name: "Alle Elemente ausgewählt",
          }),
        ).toBeChecked()

        await page.getByRole("button", { name: "Auswahl zurücksetzen" }).click()
        await page.getByText("DS", { exact: true }).click()
        await page.getByLabel("Dokumentationsstelle zuweisen").click()

        await expect(
          page.getByText(
            "Die Dokumentationseinheiten wurden der Dokumentationsstelle DS zugewiesen.",
          ),
        ).toBeVisible()

        // the assigned entry is not visible anymore
        await expect(page.getByText(celexNumber2)).toBeHidden()
        await expect(page.getByText(celexNumber3)).toBeHidden()

        await page.getByTestId("eu-tab").click()

        // Wait for the URL tab param to update
        await page.waitForFunction(() => {
          return window.location.href.includes("eu-rechtsprechung")
        })

        await expect(page.getByText("Ergebnisse anzeigen")).toBeEnabled()

        await expect(
          page.getByRole("cell", { name: "C-538/23" }).first(),
        ).toBeVisible()
      })

      await cleanUpEurlexDecisions(page)
    },
  )
})
