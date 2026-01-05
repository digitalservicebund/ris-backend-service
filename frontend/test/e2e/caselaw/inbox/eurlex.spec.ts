import { expect } from "@playwright/test"
import {
  addEurlexDecisions,
  cleanUpEurlexDecisions,
} from "../utils/documentation-unit-api-util"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToInbox,
  navigateToProcedures,
} from "~/e2e/caselaw/utils/e2e-utils"
import { generateString } from "~/test-helper/dataGenerators"

test.describe("eurlex", () => {
  test(
    "Eurlex Entscheidungen können in NeuRIS übernommen werden",
    { tag: ["@RISDEV-7376", "@RISDEV-7578", "@RISDEV-6383"] },
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
        await page.waitForResponse(
          (response) =>
            response.url().includes("/api/v1/caselaw/eurlex") &&
            response.request().method() === "GET" &&
            response.status() === 200,
        )
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
        ).toBeHidden() //this entry does not have an HTML link

        const rowWithCelexNumber3 = rows.filter({ hasText: celexNumber3 })
        await expect(rowWithCelexNumber3).toHaveCount(1)
        await expect(rowWithCelexNumber3).toContainText(celexNumber3)

        await expect(rowWithCelexNumber3).toContainText("EuGH") // Gerichtstyp
        await expect(rowWithCelexNumber3).toContainText("-") // Gerichtsort
        await expect(rowWithCelexNumber3).toContainText("06.10.2021") // Datum
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

      await test.step("Eine Entscheidung kann einer anderen Dok-Stelle zugewiesen werden", async () => {
        const rows = page.locator("tr")
        const row3 = rows.filter({ hasText: celexNumber3 })

        // all eurlex test entries are visible
        await expect(page.getByText(celexNumber1)).toBeVisible()
        await expect(page.getByText(celexNumber2)).toBeVisible()
        await expect(page.getByText(celexNumber3)).toBeVisible()

        await row3.getByRole("checkbox").click()

        await page.locator("#documentationOfficeSelector").click()
        await page.getByText("BGH").click()
        await page.getByLabel("Dokumentationsstelle zuweisen").click()

        await expect(
          page.getByText(
            "Die Dokumentationseinheit wurde der Dokumentationsstelle BGH zugewiesen.",
          ),
        ).toBeVisible()

        // the assigned entry is not visible anymore
        await expect(page.getByText(celexNumber3)).toBeHidden()
        await expect(page.getByText(celexNumber1)).toBeVisible()
        await expect(page.getByText(celexNumber2)).toBeVisible()
      })

      await test.step("Alle verbleibenden Entscheidungen können an meine Dok-Stelle zugewiesen werden", async () => {
        await page.getByTestId("eurlex-tab").click()
        // Wait for the URL tab param to update
        await page.waitForFunction(() => {
          return window.location.href.includes("eur-lex")
        })

        //all remaining entries are visible
        await expect(page.getByText(celexNumber1)).toBeVisible()
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

        await page.getByRole("button", { name: "Entfernen" }).click()
        await page.getByText("DS", { exact: true }).click()
        await page.getByLabel("Dokumentationsstelle zuweisen").click()

        await expect(
          page.getByText(
            "Die Dokumentationseinheiten wurden der Dokumentationsstelle DS zugewiesen.",
          ),
        ).toBeVisible()

        // the assigned entries are not visible anymore
        await expect(page.getByText(celexNumber1)).toBeHidden()
        await expect(page.getByText(celexNumber2)).toBeHidden()

        await page.getByTestId("eu-tab").click()

        // Wait for the URL tab param to update
        await page.waitForFunction(() => {
          return window.location.href.includes("eu-rechtsprechung")
        })

        await expect(page.getByText("Ergebnisse anzeigen")).toBeEnabled()

        await expect(
          page.getByRole("cell", { name: "C-878/24" }).first(),
        ).toBeVisible()

        await expect(
          page.getByRole("cell", { name: "C-538/23" }).first(),
        ).toBeVisible()
      })

      await test.step("Formaldaten, Eingangsart und Textrubriken wurden automatisch befüllt", async () => {
        //look at judgment and check all data is there
        const pagePromise = page.context().waitForEvent("page")
        const rows = page.locator("tr")
        const row = rows.filter({ hasText: "C-878/24" }).first()
        const previewButton = row.getByRole("button", {
          name: "Dokumentationseinheit ansehen",
        })

        await previewButton.click()

        const newTab = await pagePromise

        await expect(newTab).toHaveURL(
          /\/caselaw\/documentunit\/[A-Za-z0-9]{13}\/preview$/,
        )

        await expect(newTab.getByText("GerichtEuGH")).toBeVisible()
        await expect(newTab.getByText("AktenzeichenC-878/24")).toBeVisible()
        await expect(
          newTab.getByText("Entscheidungsdatum09.04.2025"),
        ).toBeVisible()
        await expect(newTab.getByText("DokumenttypBeschluss")).toBeVisible()
        await expect(newTab.getByText("ECLIECLI:EU:C:2025:256")).toBeVisible()
        await expect(newTab.getByText("RechtskraftKeine Angabe")).toBeVisible()
        await expect(newTab.getByText("QuelleL")).toBeVisible()
        await expect(
          newTab.getByText("EingangsartEUR-LEX-Schnittstelle"),
        ).toBeVisible()
        await expect(
          newTab.getByText("GerichtsbarkeitBesondere Gerichtsbarkeit"),
        ).toBeVisible()
        await expect(newTab.getByText("RegionEU")).toBeVisible()
        await expect(newTab.getByText("CELEX-Nummer62024CO0878")).toBeVisible()
        await expect(
          newTab.getByText("TenorAus diesen Gründen hat"),
        ).toBeVisible()
        await expect(newTab.getByText("GründeBeschluss")).toBeVisible()
      })

      await test.step("Originaltext wird im Seitenpanel angezeigt", async () => {
        const pagePromise = page.context().waitForEvent("page")
        const rows = page.locator("tr")
        const row = rows.filter({ hasText: "C-538/23" }).first()
        const editButton = row.getByRole("button", {
          name: "Dokumentationseinheit bearbeiten",
        })

        await editButton.click()

        const newTab = await pagePromise

        await expect(newTab).toHaveURL(
          /\/caselaw\/documentunit\/[A-Za-z0-9]{13}\/categories$/,
        )

        await expect(newTab.locator("#attachment-view")).toBeVisible()
        await expect(
          newTab.getByText("Urteil des Gerichtshofs (Zweite Kammer)"),
        ).toBeVisible()
      })

      await test.step("EU-Rechtsprechungsdokument kann aus Eingang in Vorgang verschoben werden", async () => {
        const rows = page.locator("tr")
        const procedureName = generateString({ length: 10 })
        await page
          .getByRole("textbox", { name: "Vorgang auswählen" })
          .fill(procedureName)
        await page.getByText(`${procedureName} neu erstellen`).click()

        const row = rows.filter({ hasText: "C-538/23" }).first()
        await row.getByLabel("Zeile abgewählt").click()

        await expect(row.getByLabel("Zeile ausgewählt")).toBeChecked()

        await page
          .getByRole("button", { name: "Zu Vorgang hinzufügen" })
          .click()

        await expect(page.getByText("Hinzufügen erfolgreich")).toBeVisible()
        await expect(row).toBeHidden()

        // document can be found in procedures
        await navigateToProcedures(page, procedureName)
        const listItems = page.getByLabel("Vorgang Listenelement")
        await expect(listItems).toHaveCount(1)
        await listItems.click()
        await expect(page.getByText("C-538/23")).toBeVisible()
      })
    },
  )

  test.afterEach("Clean up test data", async ({ page }) => {
    await cleanUpEurlexDecisions(page)
  })
})
