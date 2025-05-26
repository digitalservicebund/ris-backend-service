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
  "Dok-Einheit anderer Dokumentationsstelle zuweisen (Verwaltungsdaten)",
  { tag: ["@RISDEV-7373"] },
  () => {
    test("Eine Dokumentationseinheit kann einer anderen Dokumentationsstelle zugewiesen werden", async ({
      page,
      pageWithBghUser,
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

      await test.step("Es gibt eine Kategorie 'Zuweisen'", async () => {
        const zuweisenElements = page.getByText("Zuweisen")
        await expect(zuweisenElements).toHaveCount(2)
        await expect(page.locator("#documentationOfficeInput")).toHaveAttribute(
          "placeholder",
          "Dokumentationsstelle auswählen",
        )
      })

      await test.step("Im Dropdown sind alle Dokumentationsstellen, ausgenommen die eigene.", async () => {
        const dropdown = page.getByLabel("Dokumentationsstelle auswählen")
        await dropdown.click()
        const combobox = page.getByTestId("documentation-office-combobox")
        const documentationOffices = [
          "BAG",
          "BFH",
          "BMJ",
          "BPatG",
          "BSG",
          "BVerfG",
          "BVerwG",
          "BZst",
          "CC-RIS",
          "OVGNW",
          "VVBund",
          "juris",
        ]
        for (const office of documentationOffices) {
          await expect(combobox.getByText(office)).toBeVisible()
        }
        await expect(combobox.getByText("DS")).toBeHidden()
      })

      await test.step("Zuweisen ohne Auswahl zeigt Warnung", async () => {
        await page.getByRole("button", { name: "Zuweisen" }).click()

        await expect(
          page.getByText("Wählen Sie eine Dokumentationsstelle aus"),
        ).toBeVisible()
      })

      await test.step("Ziel-Dokumentationsstelle auswählen und bestätigen", async () => {
        page.on("request", (request) => {
          console.log("[Request]", request.method(), request.url())
        })
        const dropdown = page.getByLabel("Dokumentationsstelle auswählen")
        await dropdown.click()
        await page.getByLabel("dropdown-option").getByText("BGH").click()

        await expect(
          page.locator('button[aria-label="dropdown-option"]'),
        ).toBeHidden()
        await page.getByRole("button", { name: "Zuweisen" }).click()
        await page.waitForLoadState()
      })

      await test.step("Nach erfolgreichem Zuweisen wird zur Startseite weitergeleitet", async () => {
        await expect(page).toHaveURL("/caselaw")
      })

      await test.step("Toast für erfolgreiches Zuweisen wird angezeigt", async () => {
        const alert = page.getByRole("alert")
        await expect(alert).toBeVisible()
        await expect(alert).toContainText("Zuweisen erfolgreich")
        await expect(alert).toContainText(
          `Die Dokumentationseinheit ${documentNumber} ist jetzt in der Zuständigkeit der Dokumentationsstelle BGH.`,
        )
      })

      await test.step("Die neue Dokumentationsstelle kann das Dokument editieren", async () => {
        await navigateToCategories(pageWithBghUser, documentNumber)
        await expect(
          page.getByText(
            "Diese Dokumentationseinheit existiert nicht oder Sie haben keine Berechtigung.",
          ),
        ).toBeHidden()
      })
    })
  },
)
