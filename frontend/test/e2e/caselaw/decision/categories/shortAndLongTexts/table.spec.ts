import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  clickCategoryButton,
  navigateToCategories,
  navigateToHandover,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Create table",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-4121",
    },
    tag: ["@RISDEV-4121"],
  },
  () => {
    test("Click table button and check that menu buttons are complete", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      const tableOpeningHTML =
        '<table style="min-width: 25px;"><colgroup><col style="min-width: 25px;"></colgroup>'
      const tableExpectedParagraphText =
        "<p>some text so the table is exportable</p>"
      const tableClosingHTML = "</tbody></table>"
      const tableXMLPreview = `<table class="invisible-table-cell" style="min-width: 25px;">`
      const inputField = page.getByTestId("Gründe")
      const menu = page.getByLabel("Gründe Button Leiste")

      await navigateToCategories(page, prefilledDocumentUnit.documentNumber!)

      await test.step("Click table button in reasons", async () => {
        await clickCategoryButton("Gründe", page)
        await inputField.click()
        await menu.locator(`[aria-label='Tabelle']:not([disabled])`).click()
      })

      await test.step("Check all table menu buttons are visible", async () => {
        await expect(menu.getByLabel("Tabelle", { exact: true })).toBeVisible()
        await expect(menu.getByLabel("Tabelle löschen")).toBeVisible()
        await expect(menu.getByLabel("Zeile darunter einfügen")).toBeVisible()
        await expect(menu.getByLabel("Zeile löschen")).toBeVisible()
        await expect(menu.getByLabel("Spalte rechts einfügen")).toBeVisible()
        await expect(menu.getByLabel("Spalte löschen")).toBeVisible()
      })

      await test.step("Add table, remove two rows and two columns and check the new table is correct", async () => {
        await menu.getByLabel("Tabelle einfügen").click()
        await page.keyboard.type("some text so the table is exportable")
        await page.keyboard.press("ArrowRight")
        await menu.getByLabel("Tabelle", { exact: true }).click()
        await menu.getByLabel("Spalte löschen").click()
        await menu.getByLabel("Tabelle", { exact: true }).click()
        await menu.getByLabel("Spalte löschen").click()
        await page.keyboard.press("ArrowDown")
        await menu.getByLabel("Tabelle", { exact: true }).click()
        await menu.getByLabel("Zeile löschen").click()
        await menu.getByLabel("Tabelle", { exact: true }).click()
        await menu.getByLabel("Zeile löschen").click()
        await menu.getByLabel("Nicht-druckbare Zeichen").click()
        const inputFieldInnerHTML = await inputField.innerHTML()

        const cell = page.locator("th.invisible-table-cell")
        await expect(cell).toBeAttached()
        await expect(cell).toHaveAttribute("colspan", "1")
        await expect(cell).toHaveAttribute("rowspan", "1")
        await expect(cell).toHaveAttribute("style", "")

        expect(inputFieldInnerHTML).toContain(tableOpeningHTML)
        expect(inputFieldInnerHTML).toContain(tableExpectedParagraphText)
        expect(inputFieldInnerHTML).toContain(tableClosingHTML)
      })

      await save(page)

      await test.step("Check table is visible in the XML Vorschau", async () => {
        await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)
        await expect(page.getByText("XML Vorschau")).toBeVisible()
        await page.getByText("XML Vorschau").click()

        await expect(page.getByText(tableXMLPreview)).toBeVisible()
      })
    })
  },
)
