import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  fillInput,
  navigateToPreview,
  navigateToReferences,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"
import { generateString } from "~/test-helper/dataGenerators"

test.describe(
  "references",
  {
    tag: "@RISDEV-7932",
  },
  () => {
    test("Display, adding, editing and deleting references", async ({
      page,
      pendingProceeding,
    }) => {
      const citationPrefix = generateString()
      const citation1 = citationPrefix + ", 2-5"
      const citation2 = citationPrefix + ", 10-12"
      const citation3 = citationPrefix + ", 2"

      await test.step("References is a new selectable menu item in a documentation unit", async () => {
        await navigateToReferences(page, pendingProceeding.documentNumber, {
          type: "pending-proceeding",
        })
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
          page
            .getByTestId("caselaw-reference-list")
            .getByText("MM " + citation1 + " (LT)"),
        ).toBeVisible()
        await expect(page.getByText("sekundär", { exact: true })).toBeVisible()
      })

      await save(page)

      await test.step("Reference is persisted and shown after reload", async () => {
        await page.reload()
        await expect(
          page
            .getByTestId("caselaw-reference-list")
            .getByText("MM " + citation1 + " (LT)"),
        ).toBeVisible()
        await expect(page.getByText("sekundär", { exact: true })).toBeVisible()
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
          page
            .getByTestId("caselaw-reference-list")
            .getByText("GVBl BB " + citation1 + " (LT)"),
        ).toBeVisible()

        await expect(page.getByText("primär", { exact: true })).toBeVisible()
      })

      await test.step("Edit reference supplement in reference, verify that it is updated in the list", async () => {
        await page.getByTestId("list-entry-0").click()
        await fillInput(page, "Klammernzusatz", "S")

        await page.getByLabel("Fundstelle speichern", { exact: true }).click()
        await expect(
          page
            .getByTestId("caselaw-reference-list")
            .getByText("GVBl BB " + citation1 + " (LT)"),
        ).toBeHidden()
        await expect(
          page
            .getByTestId("caselaw-reference-list")
            .getByText("GVBl BB " + citation1 + " (S)"),
        ).toBeVisible()

        await expect(page.getByText("primär", { exact: true })).toBeVisible()
      })

      await test.step("Edit reference, click cancel, verify that it is not updated in the list", async () => {
        await page.getByTestId("list-entry-0").click()
        await fillInput(page, "Klammernzusatz", "LT")

        await page.getByLabel("Abbrechen").click()
        await expect(
          page
            .getByTestId("caselaw-reference-list")
            .getByText("GVBl BB " + citation1 + " (LT)"),
        ).toBeHidden()
        await expect(
          page
            .getByTestId("caselaw-reference-list")
            .getByText("GVBl BB " + citation1 + " (S)"),
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
          page
            .getByTestId("caselaw-reference-list")
            .getByText("WdG " + citation2 + " (ST)"),
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
          page
            .getByTestId("caselaw-reference-list")
            .getByText("AllMBl " + citation3 + " (L)"),
        ).toBeVisible()
      })

      await save(page)

      await test.step("Delete second of 3 reference and verify it disappears, order of remaining items stays the same", async () => {
        await page.getByTestId("list-entry-1").click()
        await page.getByLabel("Eintrag löschen", { exact: true }).click()
        await expect(page.getByText("WdG " + citation2 + " (ST)")).toBeHidden()

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
          page
            .getByTestId("caselaw-reference-list")
            .getByText("AllMBl " + citation3 + " (L)"),
        ).toBeHidden()
        await expect(
          page
            .getByTestId("caselaw-reference-list")
            .getByText("GVBl BB " + citation1 + " (S)"),
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
    })

    test("References input is validated against required fields", async ({
      page,
      pendingProceeding,
    }) => {
      const citation = generateString() + ", 10-12"

      await navigateToReferences(page, pendingProceeding.documentNumber ?? "", {
        type: "pending-proceeding",
      })

      await test.step("Add reference with only 'Klammerzusatz' shows error in 'Periodikum' & 'Zitatstelle'", async () => {
        await fillInput(page, "Klammernzusatz", "LT")
        await page.getByLabel("Fundstelle speichern", { exact: true }).click()
        await expect(page.getByText("Pflichtfeld nicht befüllt")).toHaveCount(2)
      })

      await test.step("Add 'Periodikum' removes error there, but still shows error for 'Zitatstelle'", async () => {
        await fillInput(page, "Periodikum", "wdg")
        await page
          .getByText("WdG | Welt der Gesundheitsversorgung", {
            exact: true,
          })
          .click()
        await page.getByLabel("Fundstelle speichern", { exact: true }).click()
        await expect(page.getByText("Pflichtfeld nicht befüllt")).toHaveCount(1)
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
    })

    test("References are visible in preview", async ({
      page,
      pendingProceeding,
    }) => {
      const documentNumber = pendingProceeding.documentNumber
      const citationPrefix = generateString()
      const citation1 = citationPrefix + ", 10-12"
      const citation2 = citationPrefix + ", 01-99"

      await test.step("References are not rendered in preview when empty", async () => {
        await navigateToPreview(page, documentNumber, {
          type: "pending-proceeding",
        })
        await expect(page.getByText("Fundstellen")).toBeHidden()
      })

      await test.step("Add primary and secondary references with all data", async () => {
        await navigateToReferences(page, documentNumber, {
          type: "pending-proceeding",
        })
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
          page
            .getByTestId("caselaw-reference-list")
            .getByText("WdG " + citation1 + " (LT)"),
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
          page
            .getByTestId("caselaw-reference-list")
            .getByText("GVBl BB " + citation2 + " (L)"),
        ).toBeVisible()
        await expect(
          page.getByText("primär", {
            exact: true,
          }),
        ).toBeVisible()
      })

      await save(page)

      await test.step("Verify rendering in preview", async () => {
        await navigateToPreview(page, pendingProceeding.documentNumber, {
          type: "pending-proceeding",
        })
        await expect(page.getByText("Literaturfundstellen")).toBeHidden()
        await expect(
          page.getByText("Primäre FundstellenGVBl BB " + citation2 + " (L)"),
        ).toBeVisible()
        await expect(
          page.getByText("Sekundäre FundstellenWdG " + citation1 + " (LT)"),
        ).toBeVisible()
      })
    })
  },
)
