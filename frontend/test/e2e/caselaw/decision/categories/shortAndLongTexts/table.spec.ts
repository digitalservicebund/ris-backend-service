import { expect, Page } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  clickCategoryButton,
  navigateToCategories,
  navigateToHandover,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe("Editor: table tests", () => {
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
        const tableXMLPreview = `<table style="min-width: 25px;">`
        const inputField = page.getByTestId("Gründe")
        const menu = page.getByLabel("Gründe Button Leiste")

        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await test.step("Click table button in reasons", async () => {
          await clickCategoryButton("Gründe", page)
          await inputField.click()
          await menu.locator(`[aria-label='Tabelle']:not([disabled])`).click()
        })

        await test.step("Check all table menu buttons are visible", async () => {
          await expect(
            menu.getByLabel("Tabelle", { exact: true }),
          ).toBeVisible()
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
          await page.keyboard.press("ArrowUp")
          await menu.getByLabel("Tabelle", { exact: true }).click()
          await menu.getByLabel("Zeile löschen").click()
          await menu.getByLabel("Nicht-druckbare Zeichen").click()
          const inputFieldInnerHTML = await inputField.innerHTML()

          const cell = page.locator("th.invisible-table-cell")
          await expect(cell).toBeAttached()
          await expect(cell).toHaveAttribute("colspan", "1")
          await expect(cell).toHaveAttribute("rowspan", "1")
          await expect(cell).toHaveAttribute("style")

          expect(inputFieldInnerHTML).toContain(tableOpeningHTML)
          expect(inputFieldInnerHTML).toContain(tableExpectedParagraphText)
          expect(inputFieldInnerHTML).toContain(tableClosingHTML)
        })

        await save(page)

        await test.step("Check table is visible in the XML Vorschau", async () => {
          await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
          await expect(page.getByText("XML Vorschau")).toBeVisible()
          await page.getByText("XML Vorschau").click()

          await expect(page.getByText(tableXMLPreview)).toBeVisible()
        })
      })
    },
  )

  test.describe(
    "Click on every table menu button for cursor inside a cell",
    {
      annotation: [
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-6646",
        },
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-9331",
        },
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-10447",
        },
      ],
      tag: ["@RISDEV-6646", "@RISDEV-9331", "@RISDEV-10447"],
    },
    () => {
      test("'Tabellenrahmen' => 'Alle Rahmen' and reset to 'Keine Rahmen'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await test.step("Click 'Alle Rahmen' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Alle Rahmen", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border", "1px solid rgb(0, 0, 0)")
        })

        await test.step("Reset border with 'Keine Rahmen' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Kein Rahmen", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
        })
      })

      test("'Tabellennamen' => 'Rahmen links' and reset to 'Keine Rahmen'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await test.step("Click 'Rahmen links' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Rahmen links", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border-left", "1px solid rgb(0, 0, 0)")
        })

        await test.step("Reset border with 'Keine Rahmen' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Kein Rahmen", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
        })
      })

      test("'Tabellenrahmen' => 'Rahmen rechts' and reset to 'Keine Rahmen'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await test.step("Click 'Rahmen rechts' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Rahmen rechts", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border-right", "1px solid rgb(0, 0, 0)")
        })

        await test.step("Reset border with 'Keine Rahmen' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Kein Rahmen", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
        })
      })

      test("'Tabellenrahmen' => 'Rahmen oben' and reset to 'Keine Rahmen'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await test.step("Click 'Rahmen oben' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Rahmen oben", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border-top", "1px solid rgb(0, 0, 0)")
        })

        await test.step("Reset border with 'Keine Rahmen' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Kein Rahmen", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
        })
      })

      test("'Tabellenrahmen' => 'Rahmen unten' and reset to 'Keine Rahmen'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await test.step("Click 'Rahmen unten' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Rahmen unten", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border-bottom", "1px solid rgb(0, 0, 0)")
        })

        await test.step("Reset border with 'Keine Rahmen' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Kein Rahmen", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
        })
      })

      test("'Vertikale Ausrichtung in Tabellen' => 'Oben ausrichten'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await menu
          .getByLabel("Vertikale Ausrichtung in Tabellen", { exact: true })
          .click()
        await menu.getByLabel("Oben ausrichten", { exact: true }).click()

        await expect(
          page
            .getByTestId("Gründe")
            .getByRole("columnheader", { name: /r1c1/ }),
        ).toHaveCSS("vertical-align", "top")
      })

      test("'Vertikale Ausrichtung in Tabellen' => 'Mittig ausrichten'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await menu
          .getByLabel("Vertikale Ausrichtung in Tabellen", { exact: true })
          .click()
        await menu.getByLabel("Mittig ausrichten", { exact: true }).click()

        await expect(
          page
            .getByTestId("Gründe")
            .getByRole("columnheader", { name: /r1c1/ }),
        ).toHaveCSS("vertical-align", "middle")
      })

      test("'Vertikale Ausrichtung in Tabellen' => 'Unten ausrichten'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await menu
          .getByLabel("Vertikale Ausrichtung in Tabellen", { exact: true })
          .click()
        await menu.getByLabel("Unten ausrichten", { exact: true }).click()

        await expect(
          page
            .getByTestId("Gründe")
            .getByRole("columnheader", { name: /r1c1/ }),
        ).toHaveCSS("vertical-align", "bottom")
      })
    },
  )

  test.describe(
    "Click on every table menu button for (vertical) three selected cell",
    {
      annotation: [
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-6646",
        },
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-9331",
        },
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-10447",
        },
      ],
      tag: ["@RISDEV-6646", "@RISDEV-9331", "@RISDEV-10447"],
    },
    () => {
      test("'Tabellenrahmen' => 'Alle Rahmen' and reset to 'Keine Rahmen'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)
        await selectCellsInFirstColumn(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await test.step("Click 'Alle Rahmen' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Alle Rahmen", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border", "1px solid rgb(0, 0, 0)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r2c1/ }),
          ).toHaveCSS("border", "1px solid rgb(0, 0, 0)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r3c1/ }),
          ).toHaveCSS("border", "1px solid rgb(0, 0, 0)")
        })

        await test.step("Reset border with 'Keine Rahmen' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Kein Rahmen", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r2c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r3c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
        })
      })

      test("'Tabellennamen' => 'Rahmen links' and reset to 'Keine Rahmen'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)
        await selectCellsInFirstColumn(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await test.step("Click 'Rahmen links' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Rahmen links", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border-left", "1px solid rgb(0, 0, 0)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r2c1/ }),
          ).toHaveCSS("border-left", "1px solid rgb(0, 0, 0)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r3c1/ }),
          ).toHaveCSS("border-left", "1px solid rgb(0, 0, 0)")
        })

        await test.step("Reset border with 'Keine Rahmen' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Kein Rahmen", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r2c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r3c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
        })
      })

      test("'Tabellenrahmen' => 'Rahmen rechts' and reset to 'Keine Rahmen'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)
        await selectCellsInFirstColumn(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await test.step("Click 'Rahmen rechts' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Rahmen rechts", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border-right", "1px solid rgb(0, 0, 0)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r2c1/ }),
          ).toHaveCSS("border-right", "1px solid rgb(0, 0, 0)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r3c1/ }),
          ).toHaveCSS("border-right", "1px solid rgb(0, 0, 0)")
        })

        await test.step("Reset border with 'Keine Rahmen' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Kein Rahmen", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r2c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r3c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
        })
      })

      test("'Tabellenrahmen' => 'Rahmen oben' and reset to 'Keine Rahmen'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)
        await selectCellsInFirstColumn(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await test.step("Click 'Rahmen oben' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Rahmen oben", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border-top", "1px solid rgb(0, 0, 0)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r2c1/ }),
          ).toHaveCSS("border-top", "1px solid rgb(0, 0, 0)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r3c1/ }),
          ).toHaveCSS("border-top", "1px solid rgb(0, 0, 0)")
        })

        await test.step("Reset border with 'Keine Rahmen' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Kein Rahmen", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r2c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r3c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
        })
      })

      test("'Tabellenrahmen' => 'Rahmen unten' and reset to 'Keine Rahmen'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)
        await selectCellsInFirstColumn(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await test.step("Click 'Rahmen unten' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Rahmen unten", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border-bottom", "1px solid rgb(0, 0, 0)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r2c1/ }),
          ).toHaveCSS("border-bottom", "1px solid rgb(0, 0, 0)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r3c1/ }),
          ).toHaveCSS("border-bottom", "1px solid rgb(0, 0, 0)")
        })

        await test.step("Reset border with 'Keine Rahmen' in table border menu 'Tabellenrahmen'", async () => {
          await menu.getByLabel("Tabellenrahmen", { exact: true }).click()
          await menu.getByLabel("Kein Rahmen", { exact: true }).click()

          await expect(
            page
              .getByTestId("Gründe")
              .getByRole("columnheader", { name: /r1c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r2c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
          await expect(
            page.getByTestId("Gründe").getByRole("cell", { name: /r3c1/ }),
          ).toHaveCSS("border", "1px dashed rgb(68, 102, 255)")
        })
      })

      test("'Vertikale Ausrichtung in Tabellen' => 'Oben ausrichten'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)
        await selectCellsInFirstColumn(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await menu
          .getByLabel("Vertikale Ausrichtung in Tabellen", { exact: true })
          .click()
        await menu.getByLabel("Oben ausrichten", { exact: true }).click()

        await expect(
          page
            .getByTestId("Gründe")
            .getByRole("columnheader", { name: /r1c1/ }),
        ).toHaveCSS("vertical-align", "top")
        await expect(
          page.getByTestId("Gründe").getByRole("cell", { name: /r2c1/ }),
        ).toHaveCSS("vertical-align", "top")
        await expect(
          page.getByTestId("Gründe").getByRole("cell", { name: /r3c1/ }),
        ).toHaveCSS("vertical-align", "top")
      })

      test("'Vertikale Ausrichtung in Tabellen' => 'Mittig ausrichten'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)
        await selectCellsInFirstColumn(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await menu
          .getByLabel("Vertikale Ausrichtung in Tabellen", { exact: true })
          .click()
        await menu.getByLabel("Mittig ausrichten", { exact: true }).click()

        await expect(
          page
            .getByTestId("Gründe")
            .getByRole("columnheader", { name: /r1c1/ }),
        ).toHaveCSS("vertical-align", "middle")
        await expect(
          page.getByTestId("Gründe").getByRole("cell", { name: /r2c1/ }),
        ).toHaveCSS("vertical-align", "middle")
        await expect(
          page.getByTestId("Gründe").getByRole("cell", { name: /r3c1/ }),
        ).toHaveCSS("vertical-align", "middle")
      })

      test("'Vertikale Ausrichtung in Tabellen' => 'Unten ausrichten'", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)
        await selectCellsInFirstColumn(page)

        const menu = page.getByLabel("Gründe Button Leiste")

        await menu
          .getByLabel("Vertikale Ausrichtung in Tabellen", { exact: true })
          .click()
        await menu.getByLabel("Unten ausrichten", { exact: true }).click()

        await expect(
          page
            .getByTestId("Gründe")
            .getByRole("columnheader", { name: /r1c1/ }),
        ).toHaveCSS("vertical-align", "bottom")
        await expect(
          page.getByTestId("Gründe").getByRole("cell", { name: /r2c1/ }),
        ).toHaveCSS("vertical-align", "bottom")
        await expect(
          page.getByTestId("Gründe").getByRole("cell", { name: /r3c1/ }),
        ).toHaveCSS("vertical-align", "bottom")
      })
    },
  )

  test.describe(
    "Leave the table by arrow key press",
    {
      annotation: [
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-6646",
        },
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-9331",
        },
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-10447",
        },
      ],
      tag: ["@RISDEV-6646", "@RISDEV-9331", "@RISDEV-10447"],
    },
    () => {
      test("Select last cell in the first column, press 'arrow down' key, cell should lose focus", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)

        const lastCellInFirstColumn = page
          .getByTestId("Gründe")
          .getByRole("cell", { name: /r3c1/ })
        await lastCellInFirstColumn.click()
        await lastCellInFirstColumn.press("ArrowDown")

        await expect(lastCellInFirstColumn).not.toBeFocused()
      })

      test("Select last cell in the last column, go to the end of cell text, press 'arrow right' key, cell should lose focus", async ({
        page,
        prefilledDocumentUnit,
      }) => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await generateTableAndPutTextInEveryCell(page)

        const lastCellInLastColumn = page
          .getByTestId("Gründe")
          .getByRole("cell", { name: /r3c3/ })
        await lastCellInLastColumn.click()
        await lastCellInLastColumn.press("End", { delay: 200 })
        await lastCellInLastColumn.press("ArrowRight")

        await expect(lastCellInLastColumn).not.toBeFocused()
      })
    },
  )

  test.describe(
    "Don't lose style information (especially border information) by paste from clipboard",
    {
      annotation: [
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-6646",
        },
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-9333",
        },
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-10447",
        },
      ],
      tag: ["@RISDEV-6646", "@RISDEV-9333", "@RISDEV-10447"],
    },
    () => {
      // eslint-disable-next-line playwright/no-skipped-test
      test.skip(
        ({ browserName }) => browserName !== "chromium",
        "Skipping in engines other than chromium, reason playwright diriven for firefox and safari does not support copy paste type='text/html' from clipboard",
      )

      test("Check style information on pasted table", async ({
        page,
        context,
        prefilledDocumentUnit,
      }) => {
        await context.grantPermissions(["clipboard-write"])

        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await page.evaluate(() => {
          const clipboardItemData = {
            ["text/html"]:
              "<table style='border: 1px solid rgb(0, 0, 0)'><th>test</th></table>",
          }
          const clipboardItem = new ClipboardItem(clipboardItemData)
          return navigator.clipboard.write([clipboardItem])
        })

        await clickCategoryButton("Gründe", page)
        await page.getByTestId("Gründe").click()
        await page
          .getByTestId("Gründe")
          .press("ControlOrMeta+V", { delay: 200 })

        const table = page.getByTestId("Gründe").locator("table")

        await expect(table).toHaveCSS("border", "1px solid rgb(0, 0, 0)")
      })
    },
  )
})

const generateTableAndPutTextInEveryCell = async (page: Page) => {
  await clickCategoryButton("Gründe", page)
  await page.getByTestId("Gründe").click()

  const menu = page.getByLabel("Gründe Button Leiste")
  await menu.getByLabel("Tabelle", { exact: true }).click()
  await menu.getByLabel("Tabelle einfügen").click()

  // first row
  await page
    .getByTestId("Gründe")
    .getByRole("columnheader")
    .nth(0)
    .fill("table cell r1c1")
  await page
    .getByTestId("Gründe")
    .getByRole("columnheader")
    .nth(1)
    .fill("table cell r1c2")
  await page
    .getByTestId("Gründe")
    .getByRole("columnheader")
    .nth(2)
    .fill("table cell r1c3")

  // second row
  await page
    .getByTestId("Gründe")
    .getByRole("cell")
    .nth(0)
    .fill("table cell r2c1")
  await page
    .getByTestId("Gründe")
    .getByRole("cell")
    .nth(1)
    .fill("table cell r2c2")
  await page
    .getByTestId("Gründe")
    .getByRole("cell")
    .nth(2)
    .fill("table cell r2c3")

  // third row
  await page
    .getByTestId("Gründe")
    .getByRole("cell")
    .nth(3)
    .fill("table cell r3c1")
  await page
    .getByTestId("Gründe")
    .getByRole("cell")
    .nth(4)
    .fill("table cell r3c2")
  await page
    .getByTestId("Gründe")
    .getByRole("cell")
    .nth(5)
    .fill("table cell r3c3")

  // goto first row and first cell
  await page
    .getByTestId("Gründe")
    .getByRole("columnheader", { name: /r1c1/ })
    .click()
}
const selectCellsInFirstColumn = async (page: Page) => {
  await page
    .getByTestId("Gründe")
    .getByRole("columnheader", { name: /r1c1/ })
    .click()
  await page.keyboard.down("Shift")
  await page.getByTestId("Gründe").getByRole("cell", { name: /r2c1/ }).click()
  await page.getByTestId("Gründe").getByRole("cell", { name: /r3c1/ }).click()
  await page.keyboard.up("Shift")
}
