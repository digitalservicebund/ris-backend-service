import { expect, Page } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Einkunftsart",
  {
    tag: ["@RISDEV-8712"],
  },
  () => {
    test("Einkunftsart", async ({ page, prefilledDocumentUnit }) => {
      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      await test.step("wird nur bei Finanzgerichtsbarkeit angezeigt", async () => {
        await expect(
          page.getByRole("button", { name: "Einkunftsart" }),
        ).toBeHidden()

        await selectCourt(page, "BFH")
        await expect(
          page.getByRole("button", { name: "Einkunftsart" }),
        ).toBeVisible()
      })

      await test.step("Einkunftsart kann mit beiden Teileinträgen hinzugefügt werden", async () => {
        await page.getByRole("button", { name: "Einkunftsart" }).click()
        await page.getByRole("combobox", { name: "Bitte auswählen" }).click()
        await page.getByText("Land- und Forstwirtschaft").click()
        await page
          .getByRole("textbox", { name: "Begrifflichkeit" })
          .fill("Förster")

        await page
          .getByRole("button", { name: "Einkunftsart speichern" })
          .click()

        await expect(
          page.getByText("Land- und Forstwirtschaft, Förster"),
        ).toBeVisible()
      })

      await test.step("Einkunftsart kann nur mit Einkunftsart hinzugefügt werden", async () => {
        await page.getByRole("combobox", { name: "Bitte auswählen" }).click()
        await page.getByText("Gewerbebetrieb").click()

        await page
          .getByRole("button", { name: "Einkunftsart speichern" })
          .click()

        await expect(page.getByText("Gewerbebetrieb")).toBeVisible()
      })

      await test.step("Einkunftsart kann nur mit Begrifflichkeit hinzugefügt werden", async () => {
        await page
          .getByRole("textbox", { name: "Begrifflichkeit" })
          .fill("Programmiererin")

        await page
          .getByRole("button", { name: "Einkunftsart speichern" })
          .click()

        await expect(page.getByText("Programmiererin")).toBeVisible()
      })

      await save(page)

      await test.step("Einträge werden nach Neuladen der Seite weiterhin angezeigt", async () => {
        await page.reload()
        await expect(
          page.getByText("Land- und Forstwirtschaft, Förster"),
        ).toBeVisible()
        await expect(page.getByText("Gewerbebetrieb")).toBeVisible()
        await expect(page.getByText("Programmiererin")).toBeVisible()
      })

      await test.step("Einträge werden bei Gerichtswechel weiterhin angezeigt", async () => {
        await selectCourt(page, "BGH")
        await expect(
          page.getByText("Land- und Forstwirtschaft, Förster"),
        ).toBeVisible()
      })

      await test.step("Einträge werden in der Vorschau angezeigt", async () => {
        await navigateToPreview(page, prefilledDocumentUnit.documentNumber)
        await expect(
          page.getByText("Land- und Forstwirtschaft, Förster"),
        ).toBeVisible()
        await expect(page.getByText("Gewerbebetrieb")).toBeVisible()
        await expect(page.getByText("Programmiererin")).toBeVisible()
      })

      await test.step("Warnung auf der jDV Übergabe Seite wird angezeigt", async () => {
        await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
        await expect(
          page.getByText(
            "Folgende Rubriken sind befüllt und können nicht an die jDV exportiert werden",
          ),
        ).toBeVisible()
        await expect(page.getByText("Einkunftsart")).toBeVisible()
      })
    })

    async function selectCourt(page: Page, courtName: string) {
      await page.getByLabel("Gericht", { exact: true }).fill(courtName)
      await expect(page.getByTestId("combobox-spinner")).toBeHidden()
      await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
        courtName,
      )
      await expect(page.getByText(courtName, { exact: true })).toBeVisible()
      await page.getByText(courtName, { exact: true }).click()
      await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
        courtName,
      )

      await save(page)
    }
  },
)
