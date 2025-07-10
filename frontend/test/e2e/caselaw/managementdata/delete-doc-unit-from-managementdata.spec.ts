import { expect } from "@playwright/test"
import {
  navigateToCategories,
  navigateToManagementData,
  navigateToSearch,
  save,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"
import { generateString } from "~/test-helper/dataGenerators"

test.describe(
  "Dok-Einheit löschen (Verwaltungsdaten)",
  { tag: ["@RISDEV-88", "@RISDEV-5885"] },
  () => {
    test("Eine Dokumentationseinheit kann über die Verwaltungsdaten gelöscht werden", async ({
      page,
    }) => {
      let documentNumber: string
      const fileNumber = "e2e_" + generateString()
      await navigateToSearch(page)
      await test.step("Erstelle neue Dokumentationseinheit", async () => {
        await page
          .getByRole("button", {
            name: "Neue Dokumentationseinheit",
            exact: true,
          })
          .click()

        await expect(page).toHaveURL(
          /\/caselaw\/documentunit\/[A-Z0-9]{13}\/attachments$/,
        )

        documentNumber = /caselaw\/documentunit\/(.*)\/attachments/g.exec(
          page.url(),
        )?.[1] as string
      })

      // We add a file number to be able to identify the document. If multiple tests run in parallel, the docnumber might be recycled for a new doc unit and makes it seems as it was not deleted.
      await navigateToCategories(page, documentNumber!)
      await page.getByTestId("chips-input_fileNumber").fill(fileNumber)
      await save(page)

      await navigateToManagementData(page, documentNumber!)
      await test.step("Lösche die Dokumentationseinheit", async () => {
        await page
          .getByRole("button", { name: "Dokumentationseinheit löschen" })
          .click()

        await page.getByRole("button", { name: "Löschen", exact: true }).click()
      })

      await test.step("Nach Löschen wird zur Startseite weitergeleitet", async () => {
        await expect(page).toHaveURL("/caselaw/search")
      })

      await test.step("Dokumentationseinheit existiert nicht mehr", async () => {
        await page.goto(
          `/caselaw/search/decisions?documentNumber=${documentNumber}&fileNumber=${fileNumber}`,
        )

        await page.getByText("Ergebnisse anzeigen").click()

        await expect(
          page.getByText("Keine Suchergebnisse gefunden"),
        ).toBeVisible()
      })
    })
  },
)
