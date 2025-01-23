import { expect } from "@playwright/test"
import {
  navigateToCategories,
  navigateToManagementData,
  navigateToSearch,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"

test.describe(
  "Dok-Einheit löschen (Verwaltungsdaten)",
  { tag: ["@RISDEV-88", "@RISDEV-5885"] },
  () => {
    test("Eine Dokumentationseinheit kann über die Verwaltungsdaten gelöscht werden", async ({
      page,
    }) => {
      let documentNumber: string
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

      await navigateToManagementData(page, documentNumber!)
      await test.step("Lösche die Dokumentationseinheit", async () => {
        await page
          .getByRole("button", { name: "Dokumentationseinheit löschen" })
          .click()

        await page.getByRole("button", { name: "Löschen", exact: true }).click()
      })

      await test.step("Nach Löschen wird zur Startseite weitergeleitet", async () => {
        await expect(page).toHaveURL("/caselaw")
      })

      await test.step("Dokumentationseinheit existiert nicht mehr", async () => {
        await navigateToCategories(page, documentNumber!, { skipAssert: true })

        await expect(
          page.getByText(
            "Diese Dokumentationseinheit existiert nicht oder Sie haben keine Berechtigung.",
          ),
        ).toBeVisible()
      })
    })
  },
)
