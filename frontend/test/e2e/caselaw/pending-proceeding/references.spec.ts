import { expect } from "@playwright/test"
import {
  fillInput,
  navigateToHandover,
  navigateToPreview,
  navigateToReferences,
  save,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"
import { generateString } from "~/test-helper/dataGenerators"

test.describe(
  "references",
  {
    tag: "@RISDEV-7932",
  },
  () => {
    // Todo: new endpoint for pending proceeding missing for this test case
    // eslint-disable-next-line playwright/no-skipped-test
    test.skip(
      "Display, adding, editing and deleting references",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4336",
        },
      },
      async ({ page, documentNumber }) => {
        const citationPrefix = generateString()
        const citation1 = citationPrefix + ", 2-5"
        const citation2 = citationPrefix + ", 10-12"
        const citation3 = citationPrefix + ", 2"

        await test.step("References is a new selectable menu item in a documentation unit", async () => {
          await navigateToReferences(page, documentNumber)
        })

        await test.step("When typing the legal periodical abbreviation (MM), the entry including abbreviation, title and subtitle can be found in the combobox", async () => {
          await fillInput(page, "Periodikum", "MM")
          await expect(
            page.getByText("Magazin des Berliner Mieterverein e.V.", {
              exact: true,
            }),
          ).toBeVisible()
          await page.getByText("MM | Mieter Magazin", { exact: true }).click()
          await expect(
            page.getByLabel("Periodikum", { exact: true }),
          ).toHaveValue("MM")
        })

        await test.step("citation shows citation example", async () => {
          await expect(
            page.getByText("Zitierbeispiel: 2011, Nr 6, 22-23"),
          ).toBeVisible()
        })

        await test.step("citation and supplement can be added", async () => {
          await fillInput(page, "Zitatstelle", citation1)
          await fillInput(page, "Klammernzusatz", "LT")
        })

        await test.step("Reference can be added to editable list", async () => {
          await page.getByLabel("Fundstelle speichern", { exact: true }).click()
          await expect(
            page.getByText("MM " + citation1 + " (LT)"),
          ).toBeVisible()
          await expect(
            page.getByText("sekundär", { exact: true }),
          ).toBeVisible()
        })

        await save(page)

        await test.step("Reference is persisted and shown after reload", async () => {
          await page.reload()
          await expect(
            page.getByText("MM " + citation1 + " (LT)"),
          ).toBeVisible()
          await expect(
            page.getByText("sekundär", { exact: true }),
          ).toBeVisible()
        })

        await test.step("Edit legal periodical in reference, verify that it is updated in the list", async () => {
          await page.getByTestId("list-entry-0").click()
          await fillInput(page, "Periodikum", "GVBl BB")
          await page
            .getByText(
              "GVBl BB | Gesetz- und Verordnungsblatt für das Land Brandenburg",
              { exact: true },
            )
            .click()
          await expect(
            page.getByLabel("Periodikum", { exact: true }),
          ).toHaveValue("GVBl BB")
          await expect(
            page.getByText("Zitierbeispiel: 1991, 676-681"),
          ).toBeVisible()
          await page.getByLabel("Fundstelle speichern", { exact: true }).click()
          await expect(
            page.getByText("GVBl BB " + citation1 + " (LT)"),
          ).toBeVisible()

          await expect(page.getByText("primär", { exact: true })).toBeVisible()
        })

        await test.step("Edit reference supplement in reference, verify that it is updated in the list", async () => {
          await page.getByTestId("list-entry-0").click()
          await fillInput(page, "Klammernzusatz", "S")

          await page.getByLabel("Fundstelle speichern", { exact: true }).click()
          await expect(
            page.getByText("GVBl BB " + citation1 + " (LT)"),
          ).toBeHidden()
          await expect(
            page.getByText("GVBl BB " + citation1 + " (S)"),
          ).toBeVisible()

          await expect(page.getByText("primär", { exact: true })).toBeVisible()
        })

        await test.step("Edit reference, click cancel, verify that it is not updated in the list", async () => {
          await page.getByTestId("list-entry-0").click()
          await fillInput(page, "Klammernzusatz", "LT")

          await page.getByLabel("Abbrechen").click()
          await expect(
            page.getByText("GVBl BB " + citation1 + " (LT)"),
          ).toBeHidden()
          await expect(
            page.getByText("GVBl BB " + citation1 + " (S)"),
          ).toBeVisible()

          await expect(page.getByText("primär", { exact: true })).toBeVisible()
        })

        await test.step("Add second reference, verify that it is shown in the list", async () => {
          await page.getByLabel("Weitere Angabe").click()
          await fillInput(page, "Periodikum", "wdg")
          await page
            .getByText("WdG | Welt der Gesundheitsversorgung", {
              exact: true,
            })
            .click()
          await expect(
            page.getByLabel("Periodikum", { exact: true }),
          ).toHaveValue("WdG")
          await fillInput(page, "Zitatstelle", citation2)
          await fillInput(page, "Klammernzusatz", "ST")
          await page.getByLabel("Fundstelle speichern", { exact: true }).click()
          await expect(
            page.getByText("WdG " + citation2 + " (ST)"),
          ).toBeVisible()
        })
        await save(page)

        await test.step("Add third reference, verify that it is shown in the list", async () => {
          await fillInput(page, "Periodikum", "AllMBl")
          await page
            .getByText("AllMBl | Allgemeines Ministerialblatt", {
              exact: true,
            })
            .click()
          await expect(
            page.getByLabel("Periodikum", { exact: true }),
          ).toHaveValue("AllMBl")
          await fillInput(page, "Zitatstelle", citation3)
          await fillInput(page, "Klammernzusatz", "L")
          await page.getByLabel("Fundstelle speichern", { exact: true }).click()
          await expect(
            page.getByText("AllMBl " + citation3 + " (L)"),
          ).toBeVisible()
        })
        await save(page)
        await test.step("Delete second of 3 reference and verify it disappears, order of remaining items stays the same", async () => {
          await page.getByTestId("list-entry-1").click()
          await page.getByLabel("Eintrag löschen", { exact: true }).click()
          await expect(
            page.getByText("WdG " + citation2 + " (ST)"),
          ).toBeHidden()

          await expect(page.getByLabel("Listen Eintrag").nth(0)).toHaveText(
            "GVBl BB " + citation1 + " (S)primär",
          )
          await expect(page.getByLabel("Listen Eintrag").nth(1)).toHaveText(
            "AllMBl " + citation3 + " (L)primär",
          )
        })

        await test.step("Delete second of 2 references and verify it disappears from the list, first item stays the same", async () => {
          await page.getByTestId("list-entry-1").click()
          await page.getByLabel("Eintrag löschen", { exact: true }).click()
          await expect(
            page.getByText("AllMBl " + citation3 + " (L)"),
          ).toBeHidden()
          await expect(
            page.getByText("GVBl BB " + citation1 + " (S)"),
          ).toBeVisible()
        })
        await test.step("Delete last reference and verify the list is empty", async () => {
          await page.getByTestId("list-entry-0").click()
          await page.getByLabel("Eintrag löschen", { exact: true }).click()
          await expect(
            page.getByText("GVBl BB " + citation1 + " (S)"),
          ).toBeHidden()
          await save(page)

          await page.reload()

          //only reference input list item is shown (input is a list entry in editable list)
          await expect(
            page
              .getByTestId("caselaw-reference-list")
              .getByLabel("Listen Eintrag"),
          ).toHaveCount(1)
        })
      },
    )

    // Todo: new endpoint for pending proceeding missing for this test case
    // eslint-disable-next-line playwright/no-skipped-test
    test.skip(
      "References input is validated against required fields",
      {
        tag: ["@RISDEV-7932"],
      },
      async ({ page, prefilledDocumentUnit }) => {
        const citation = generateString() + ", 10-12"

        await navigateToReferences(
          page,
          prefilledDocumentUnit.documentNumber ?? "",
        )

        await test.step("Add reference with only 'Klammerzusatz' shows error in 'Periodikum' & 'Zitatstelle'", async () => {
          await fillInput(page, "Klammernzusatz", "LT")
          await page.getByLabel("Fundstelle speichern", { exact: true }).click()
          await expect(page.getByText("Pflichtfeld nicht befüllt")).toHaveCount(
            2,
          )
        })
        await test.step("Add 'Periodikum' removes error there, but still shows error for 'Zitatstelle'", async () => {
          await fillInput(page, "Periodikum", "wdg")
          await page
            .getByText("WdG | Welt der Gesundheitsversorgung", {
              exact: true,
            })
            .click()
          await page.getByLabel("Fundstelle speichern", { exact: true }).click()
          await expect(page.getByText("Pflichtfeld nicht befüllt")).toHaveCount(
            1,
          )
        })
        await test.step("Add 'Zitatstelle' removes error, reference can be saved", async () => {
          await fillInput(page, "Zitatstelle", citation)
          await page.getByLabel("Fundstelle speichern", { exact: true }).click()
          await expect(page.getByText("Pflichtfeld nicht befüllt")).toBeHidden()
        })
        await test.step("Add 'Periodikum' with empty Klammernzusatz (referenceSupplement)", async () => {
          await fillInput(page, "Periodikum", "wdg")
          await fillInput(page, "Zitatstelle", citation)
          await fillInput(page, "Klammernzusatz", "")

          await page.getByLabel("Fundstelle speichern", { exact: true }).click()
          await expect(
            page.getByText("Pflichtfeld nicht befüllt"),
            "Should not count empty reference supplement error",
          ).toHaveCount(1)
        })
      },
    )

    // Todo: new endpoint for pending proceeding missing for this test case
    // eslint-disable-next-line playwright/no-skipped-test
    test.skip(
      "Literature references input is validated against required fields",
      {
        tag: ["@RISDEV-7932"],
      },
      async ({ page, prefilledDocumentUnit }) => {
        const citation = generateString() + ", 2"
        await navigateToReferences(
          page,
          prefilledDocumentUnit.documentNumber ?? "",
        )

        await test.step("Literature references are validated for required inputs", async () => {
          await expect(
            page.getByLabel("Literaturfundstelle speichern", { exact: true }),
          ).toBeDisabled()

          await fillInput(page, "Periodikum Literaturfundstelle", "AllMBl")
          await page
            .getByText("AllMBl | Allgemeines Ministerialblatt", {
              exact: true,
            })
            .click()
          await expect(
            page.getByLabel("Periodikum Literaturfundstelle", { exact: true }),
          ).toHaveValue("AllMBl")
          await fillInput(page, "Zitatstelle Literaturfundstelle", citation)

          await page
            .getByLabel("Literaturfundstelle speichern", { exact: true })
            .click()
          // check that both fields display error message
          await expect(
            page.getByText("Pflichtfeld nicht befüllt", { exact: true }),
          ).toHaveCount(2)

          await fillInput(page, "Autor Literaturfundstelle", "Einstein, Albert")

          await expect(
            page.getByText("Pflichtfeld nicht befüllt", { exact: true }),
          ).toHaveCount(1)

          await fillInput(page, "Dokumenttyp Literaturfundstelle", "Ean")
          await page.getByText("Ean", { exact: true }).click()
          await expect(
            page.getByLabel("Dokumenttyp Literaturfundstelle", { exact: true }),
          ).toHaveValue("Anmerkung")

          await expect(
            page.getByText("Pflichtfeld nicht befüllt", { exact: true }),
          ).toBeHidden()
        })
      },
    )

    // Todo: new endpoint for pending proceeding missing for this test case
    // eslint-disable-next-line playwright/no-skipped-test
    test.skip(
      "References are visible in preview",
      {
        tag: ["@RISDEV-7932"],
      },
      async ({ page, prefilledDocumentUnit }) => {
        const documentNumber = prefilledDocumentUnit.documentNumber!
        const citationPrefix = generateString()
        const citation1 = citationPrefix + ", 10-12"
        const citation2 = citationPrefix + ", 01-99"

        await test.step("References are not rendered in preview when empty", async () => {
          await navigateToPreview(page, documentNumber)
          await expect(page.getByText("Fundstellen")).toBeHidden()
        })

        await navigateToReferences(page, documentNumber)

        await test.step("Add primary and secondary references with all data", async () => {
          await fillInput(page, "Periodikum", "wdg")
          await page
            .getByText("WdG | Welt der Gesundheitsversorgung", {
              exact: true,
            })
            .click()
          await fillInput(page, "Zitatstelle", citation1)
          await fillInput(page, "Klammernzusatz", "LT")
          await page.getByLabel("Fundstelle speichern", { exact: true }).click()
          await expect(
            page.getByText("WdG " + citation1 + " (LT)"),
          ).toBeVisible()
          await expect(
            page.getByText("sekundär", {
              exact: true,
            }),
          ).toBeVisible()
          await fillInput(page, "Periodikum", "GVBl BB")
          await page
            .getByText(
              "GVBl BB | Gesetz- und Verordnungsblatt für das Land Brandenburg",
              { exact: true },
            )
            .click()
          await fillInput(page, "Zitatstelle", citation2)
          await fillInput(page, "Klammernzusatz", "L")
          await page.getByLabel("Fundstelle speichern", { exact: true }).click()
          await expect(
            page.getByText("GVBl BB " + citation2 + " (L)"),
          ).toBeVisible()
          await expect(
            page.getByText("primär", {
              exact: true,
            }),
          ).toBeVisible()
        })

        await save(page)

        await test.step("Verify rendering in preview", async () => {
          await navigateToPreview(page, documentNumber)
          await expect(page.getByText("Literaturfundstellen")).toBeHidden()
          await expect(
            page.getByText("Primäre FundstellenGVBl BB " + citation2 + " (L)"),
          ).toBeVisible()
          await expect(
            page.getByText("Sekundäre FundstellenWdG " + citation1 + " (LT)"),
          ).toBeVisible()
        })

        await test.step("Verify references are exported in correct order", async () => {
          await navigateToHandover(page, documentNumber)
          await expect(page.getByText("XML Vorschau")).toBeVisible()
          await page.getByText("XML Vorschau").click()

          const referencesNodes = await page
            .locator('code:has-text("<fundstelle ")')
            .all()
          await expect(referencesNodes[0]).toHaveText(
            '<fundstelle typ="nichtamtlich">',
          )
          await expect(referencesNodes[1]).toHaveText(
            '<fundstelle typ="amtlich">',
          )

          const legalPeriodicalNodes = await page
            .locator('code:has-text("<periodikum>")')
            .all()
          await expect(legalPeriodicalNodes[0]).toHaveText(
            "<periodikum>WdG</periodikum>",
          )
          await expect(legalPeriodicalNodes[1]).toHaveText(
            "<periodikum>GVBl BB</periodikum>",
          )

          const citationNodes = await page
            .locator('code:has-text("<zitstelle>")')
            .all()
          await expect(citationNodes[0]).toHaveText(
            "<zitstelle>" + citation1 + " (LT)</zitstelle>",
          )
          await expect(citationNodes[1]).toHaveText(
            "<zitstelle>" + citation2 + " (L)</zitstelle>",
          )
        })
      },
    )

    // Todo: new endpoint for pending proceeding missing for this test case
    // eslint-disable-next-line playwright/no-skipped-test
    test.skip(
      "Literature references can be saved and are visible in preview",
      {
        tag: ["@RISDEV-7932"],
      },
      async ({ page, documentNumber }) => {
        const citationPrefix = generateString()
        const citation1 = citationPrefix + ", 2"
        const citation2 = citationPrefix + ", 4-6"

        await test.step("References are not rendered in preview when empty", async () => {
          await navigateToPreview(page, documentNumber)
          await expect(page.getByText("Literaturfundstellen")).toBeHidden()
        })

        await test.step("Literature references are located in a dedicated editable list", async () => {
          await navigateToReferences(page, documentNumber)
          await expect(
            page.getByText("Literaturfundstellen", { exact: true }),
          ).toBeVisible()
        })

        await test.step("Add literature reference, verify rendering in editable list", async () => {
          await fillInput(page, "Periodikum Literaturfundstelle", "AllMBl")
          await page
            .getByText("AllMBl | Allgemeines Ministerialblatt", {
              exact: true,
            })
            .click()
          await expect(
            page.getByLabel("Periodikum Literaturfundstelle", { exact: true }),
          ).toHaveValue("AllMBl")
          await fillInput(page, "Zitatstelle Literaturfundstelle", citation1)
          await fillInput(page, "Autor Literaturfundstelle", "Bilen, Ulviye")
          await fillInput(page, "Dokumenttyp Literaturfundstelle", "Ean")
          await page.getByText("Ean", { exact: true }).click()
          await expect(
            page.getByLabel("Dokumenttyp Literaturfundstelle", { exact: true }),
          ).toHaveValue("Anmerkung")

          await page
            .getByLabel("Literaturfundstelle speichern", { exact: true })
            .click()
          await expect(
            page.getByText("AllMBl " + citation1 + ", Bilen, Ulviye (Ean)"),
          ).toBeVisible()
        })

        await test.step("Verify rendering in preview", async () => {
          await page.keyboard.press("v")

          const preview = page.getByTestId("literature-references-preview")

          await expect(preview.getByText("Literaturfundstellen")).toBeVisible()
          await expect(
            preview.getByText("Fundstellen", { exact: true }),
          ).toBeHidden()

          await expect(
            preview.getByText("AllMBl " + citation1 + ", Bilen, Ulviye (Ean)"),
          ).toBeVisible()
        })

        await test.step("Add second literature reference, verify it is added at the bottom", async () => {
          await fillInput(page, "Periodikum Literaturfundstelle", "GVBl BB")
          await page
            .getByText(
              "GVBl BB | Gesetz- und Verordnungsblatt für das Land Brandenburg",
              {
                exact: true,
              },
            )
            .click()
          await expect(
            page.getByLabel("Periodikum Literaturfundstelle", { exact: true }),
          ).toHaveValue("GVBl BB")
          await fillInput(page, "Zitatstelle Literaturfundstelle", citation2)
          await fillInput(page, "Autor Literaturfundstelle", "Kästner, Erich")
          await fillInput(page, "Dokumenttyp Literaturfundstelle", "Ebs")
          await page.getByText("Ebs", { exact: true }).click()
          await expect(
            page.getByLabel("Dokumenttyp Literaturfundstelle", { exact: true }),
          ).toHaveValue("Entscheidungsbesprechung")

          await page
            .getByLabel("Literaturfundstelle speichern", { exact: true })
            .click()
        })

        await test.step("Verify second literature citation it is added at the bottom of editable list", async () => {
          await expect(page.getByLabel("Listen Eintrag").nth(1)).toHaveText(
            "AllMBl " + citation1 + ", Bilen, Ulviye (Ean)primär",
          )
          await expect(page.getByLabel("Listen Eintrag").nth(2)).toHaveText(
            "GVBl BB " + citation2 + ", Kästner, Erich (Ebs)primär",
          )
        })

        await test.step("Verify second literature citation it is added at the bottom of preview", async () => {
          const literatureReferencesPreview = page.getByTestId(
            "literature-references-preview",
          )
          const texts = await literatureReferencesPreview.textContent()

          // Make sure the literature citations are in the correct order
          expect(texts).toContain(
            "AllMBl " +
              citation1 +
              ", Bilen, Ulviye (Ean)" +
              "GVBl BB " +
              citation2 +
              ", Kästner, Erich (Ebs)",
          )
        })
      },
    )
  },
)
