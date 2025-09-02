import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  handoverDocumentationUnit,
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Eingangsart",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-6383",
    },
  },
  () => {
    test(
      "Anzeigen, Navigieren, Löschen von Eingangsarten",
      { tag: ["@RISDEV-6383"] },
      async ({ page, documentNumber }) => {
        await navigateToCategories(page, documentNumber)

        await test.step("Eingangsart Eingabefeld mit Hinweis wird angezeigt", async () => {
          await expect(page.getByLabel("Eingangsart")).toBeVisible()
          await expect(
            page.getByText(
              "Papier, BLK-DB-Schnittstelle, EUR-LEX-Schnittstelle, E-Mail",
            ),
          ).toBeVisible()
        })

        await test.step("Neu hinzugefügte Eingangsarten sind sichtbar", async () => {
          await page.getByLabel("Eingangsart", { exact: true }).fill("E-Mail")
          await page.keyboard.press("Enter")
          await page.getByLabel("Eingangsart", { exact: true }).fill("Papier")
          await page.keyboard.press("Enter")

          await expect(
            page
              .getByTestId("chips-input-wrapper_inputTypes")
              .getByText("E-Mail"),
          ).toBeVisible()
          await expect(
            page
              .getByTestId("chips-input-wrapper_inputTypes")
              .getByText("Papier"),
          ).toBeVisible()
        })

        await test.step("Navigiere zurück mit Pfeil-links, lösche letzten Eintrag mit enter", async () => {
          await expect(
            page
              .getByTestId("chips-input-wrapper_inputTypes")
              .getByText("Papier"),
          ).toBeVisible()
          await page.keyboard.press("ArrowLeft")
          await page.keyboard.press("Enter")

          await expect(
            page
              .getByTestId("chips-input-wrapper_inputTypes")
              .getByText("Papier"),
          ).toBeHidden()
        })

        await test.step("Navigiere raus und rein, navigiere zurück mit Pfeil-links, lösche chip mit enter", async () => {
          await expect(
            page
              .getByTestId("chips-input-wrapper_inputTypes")
              .getByText("E-Mail"),
          ).toBeVisible()
          await page.keyboard.press("Tab")
          await page.keyboard.press("Tab")
          await page.keyboard.down("Shift")
          await page.keyboard.press("Tab")

          await page.keyboard.press("ArrowLeft")
          await page.keyboard.press("Enter")
          await expect(
            page
              .getByTestId("chips-input-wrapper_inputTypes")
              .getByText("E-Mail"),
          ).toBeHidden()
        })

        await save(page)

        await test.step("Füge die gelöschten Einträge wieder hinzu, sie sind auch nach reload da", async () => {
          await page.getByLabel("Eingangsart", { exact: true }).fill("E-Mail")
          await page.keyboard.press("Enter")

          await page.getByLabel("Eingangsart", { exact: true }).fill("Papier")
          await page.keyboard.press("Enter")
          await save(page)

          await page.reload()
          await expect(
            page
              .getByTestId("chips-input-wrapper_inputTypes")
              .getByText("E-Mail"),
          ).toBeVisible()
          await expect(
            page
              .getByTestId("chips-input-wrapper_inputTypes")
              .getByText("Papier"),
          ).toBeVisible()
        })
      },
    )

    test(
      "Eingangsart ist sichtbar in der Vorschau",
      { tag: ["@RISDEV-6383"] },
      async ({ page, documentNumber }) => {
        await navigateToCategories(page, documentNumber)
        const testData = ["E-Mail", "Papier", "EUR-LEX-Schnittstelle"]

        for (const inputType of testData) {
          await test.step(
            "Eingangsart '" + inputType + "' wird hinzugefügt",
            async () => {
              await page
                .getByLabel("Eingangsart", { exact: true })
                .fill(inputType)
              await page.keyboard.press("Enter")

              await expect(
                page
                  .getByTestId("chips-input-wrapper_inputTypes")
                  .getByText(inputType),
              ).toBeVisible()
            },
          )
        }

        await save(page)
        await test.step("Alle Eingangsarten sind in der Vorschau sichtbar", async () => {
          await navigateToPreview(page, documentNumber)
          await expect(
            page
              .getByTestId("preview")
              .getByText("E-Mail, Papier, EUR-LEX-Schnittstelle", {
                exact: true,
              }),
          ).toBeVisible()
        })

        await test.step("Sind alle Eingangsarten entfernt, ist die Kategorie in der Preview nicht mehr sichtbar", async () => {
          await navigateToCategories(page, documentNumber)

          for (let i = 0; i < testData.length; i++) {
            await page.getByLabel("Löschen").first().click()
          }
          await save(page)
          await navigateToPreview(page, documentNumber)
          await expect(page.getByText("Eingangsart")).toBeHidden()
        })
      },
    )

    test(
      "Eingangsart wird exportiert",
      { tag: ["@RISDEV-6383"] },
      async ({ page, prefilledDocumentUnit }) => {
        await handoverDocumentationUnit(
          page,
          prefilledDocumentUnit.documentNumber ?? "",
        )

        await navigateToCategories(
          page,
          prefilledDocumentUnit.documentNumber ?? "",
        )

        const testData = ["E-Mail", "Papier", "EUR-LEX-Schnittstelle"]

        for (const inputType of testData) {
          await test.step(
            "Eingangsart '" + inputType + "' wird hinzugefügt",
            async () => {
              await page
                .getByLabel("Eingangsart", { exact: true })
                .fill(inputType)
              await page.keyboard.press("Enter")

              await expect(
                page
                  .getByTestId("chips-input-wrapper_inputTypes")
                  .getByText(inputType),
              ).toBeVisible()
            },
          )
        }
        await save(page)

        await test.step("In der Übergabe werden die Einträge angezeigt", async () => {
          await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)
          await expect(page.getByText("XML Vorschau")).toBeVisible()
          await page.getByText("XML Vorschau").click()

          await expect(
            page.getByText("<begriff>E-Mail</begriff>"),
          ).toBeVisible()
          await expect(
            page.getByText("<begriff>Papier</begriff>"),
          ).toBeVisible()
          await expect(
            page.getByText("<begriff>EUR-LEX-Schnittstelle</begriff>"),
          ).toBeVisible()
        })
      },
    )
  },
)
