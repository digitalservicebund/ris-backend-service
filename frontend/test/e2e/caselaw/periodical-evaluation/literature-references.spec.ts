import { expect } from "@playwright/test"
import {
  fillInput,
  navigateToPeriodicalReferences,
  navigateToPreview,
  searchForDocUnitWithFileNumberAndDecisionDate,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"

/* eslint-disable playwright/no-conditional-in-test */

test.describe("Literature references", () => {
  test(
    "Switching between caselaw and literature reference type renders different inputs",
    {
      tag: "@RISDEV-5238",
    },
    async ({ page, edition }) => {
      await test.step("Caselaw reference type is preselected", async () => {
        await navigateToPeriodicalReferences(page, edition.id || "")

        await expect(page.getByLabel("Rechtsprechung Fundstelle")).toBeChecked()

        await expect(page.getByLabel("Literatur Fundstelle")).not.toBeChecked()

        await expect(page.getByLabel("Klammernzusatz")).toBeVisible()

        await expect(
          page.getByLabel("Dokumenttyp Literaturfundstelle"),
        ).toBeHidden()

        await expect(page.getByLabel("Autor Literaturfundstelle")).toBeHidden()
      })

      await test.step("Selecting literature reference type, renders different inputs", async () => {
        await page.getByLabel("Literatur Fundstelle").click()
        await expect(
          page.getByLabel("Rechtsprechung Fundstelle"),
        ).not.toBeChecked()

        await expect(page.getByLabel("Literatur Fundstelle")).toBeChecked()

        await expect(page.getByLabel("Klammernzusatz")).toBeHidden()

        await expect(
          page.getByLabel("Dokumenttyp Literaturfundstelle"),
        ).toBeVisible()

        await expect(page.getByLabel("Autor Literaturfundstelle")).toBeVisible()
      })

      await test.step("Literature document types can be chosen from lookup table values", async () => {
        await page
          .getByLabel("Dokumenttyp Literaturfundstelle", { exact: true })
          .focus()
        // we expect a list of at least 15 values
        await expect(
          page.locator("[aria-label='dropdown-option'] >> nth=15"),
        ).toBeVisible()
      })

      await test.step("Input values are maintained when switching between literature and caselaw references", async () => {
        await fillInput(page, "Zitatstelle *", "2")
        await fillInput(page, "Autor Literaturfundstelle", "Einstein, Albert")

        await page.getByLabel("Rechtsprechung Fundstelle").click()
        await expect(
          page.getByLabel("Zitatstelle *", { exact: true }),
        ).toHaveValue("2")

        await page.getByLabel("Literatur Fundstelle").click()
        await expect(
          page.getByLabel("Zitatstelle *", { exact: true }),
        ).toHaveValue("2")
        await expect(
          page.getByLabel("Autor Literaturfundstelle", { exact: true }),
        ).toHaveValue("Einstein, Albert")
      })
    },
  )

  test(
    "Literature references can be added for decisions to periodical evaluation",
    {
      tag: "@RISDEV-5240 @RISDEV-5237 @RISDEV-5454",
    },
    async ({ page, prefilledDocumentUnit, edition }) => {
      const fileNumber = prefilledDocumentUnit.coreData.fileNumbers?.[0] || ""
      await test.step("Literature references are validated for required inputs", async () => {
        await navigateToPeriodicalReferences(page, edition.id || "")
        await page.getByLabel("Literatur Fundstelle").click()
        await expect(page.getByLabel("Literatur Fundstelle")).toBeChecked()
        await fillInput(page, "Zitatstelle *", `2`)

        await searchForDocUnitWithFileNumberAndDecisionDate(
          page,
          fileNumber,
          "31.12.2019",
        )
        // wait for panel to open
        await expect(page).toHaveURL(/showAttachmentPanel=true/)
        await page.getByLabel("Treffer übernehmen").click()
        // check that both fields display error message
        await expect(
          page.getByText("Pflichtfeld nicht befüllt", { exact: true }),
        ).toHaveCount(2)

        // Switching between radio buttons resets the validation errors
        await page.getByLabel("Rechtsprechung Fundstelle").click()
        await page.getByLabel("Literatur Fundstelle").click()
        await expect(
          page.getByText("Pflichtfeld nicht befüllt", { exact: true }),
        ).toHaveCount(0)
        await page.getByLabel("Seitenpanel schließen").click()
        await expect(page.getByLabel("Seitenpanel schließen")).toBeHidden()
      })

      await test.step("Save literature reference, verify that it is shown in the list", async () => {
        await fillInput(page, "Autor Literaturfundstelle", "Bilen, Ulviye")
        await fillInput(page, "Dokumenttyp Literaturfundstelle", "Ean")
        await page.getByText("Ean", { exact: true }).click()
        await expect(
          page.getByLabel("Dokumenttyp Literaturfundstelle", { exact: true }),
        ).toHaveValue("Anmerkung")

        await searchForDocUnitWithFileNumberAndDecisionDate(
          page,
          fileNumber,
          "31.12.2019",
        )
        await page.getByLabel("Treffer übernehmen").click()
        await expect(
          page.getByText(`MMG 2024, 2${edition.suffix}, Bilen, Ulviye (Ean)`),
        ).toBeVisible()
      })

      await test.step("Radio buttons should not be visible after saving", async () => {
        await page.getByTestId("list-entry-0").click()
        await expect(page.getByLabel("Rechtsprechung Fundstelle")).toBeHidden()

        await expect(page.getByLabel("Literatur Fundstelle")).toBeHidden()
      })
    },
  )

  // Todo: new endpoint for pending proceeding missing for this test case
  // eslint-disable-next-line playwright/no-skipped-test
  test.skip(
    "Literature references can be added for pending proceedings to periodical evaluation",
    {
      tag: "@RISDEV-5240 @RISDEV-5237 @RISDEV-5454",
    },
    async ({ page, prefilledDocumentUnit, edition }) => {
      const fileNumber = prefilledDocumentUnit.coreData.fileNumbers?.[0] || ""
      await test.step("Literature references are validated for required inputs", async () => {
        await navigateToPeriodicalReferences(page, edition.id || "")
        await page.getByLabel("Literatur Fundstelle").click()
        await expect(page.getByLabel("Literatur Fundstelle")).toBeChecked()
        await fillInput(page, "Zitatstelle *", `2`)

        await searchForDocUnitWithFileNumberAndDecisionDate(
          page,
          fileNumber,
          "31.12.2019",
        )
        // wait for panel to open
        await expect(page).toHaveURL(/showAttachmentPanel=true/)
        await page.getByLabel("Treffer übernehmen").click()
        // check that both fields display error message
        await expect(
          page.getByText("Pflichtfeld nicht befüllt", { exact: true }),
        ).toHaveCount(2)

        // Switching between radio buttons resets the validation errors
        await page.getByLabel("Rechtsprechung Fundstelle").click()
        await page.getByLabel("Literatur Fundstelle").click()
        await expect(
          page.getByText("Pflichtfeld nicht befüllt", { exact: true }),
        ).toHaveCount(0)
        await page.getByLabel("Seitenpanel schließen").click()
        await expect(page.getByLabel("Seitenpanel schließen")).toBeHidden()
      })

      await test.step("Save literature reference, verify that it is shown in the list", async () => {
        await fillInput(page, "Autor Literaturfundstelle", "Bilen, Ulviye")
        await fillInput(page, "Dokumenttyp Literaturfundstelle", "Ean")
        await page.getByText("Ean", { exact: true }).click()
        await expect(
          page.getByLabel("Dokumenttyp Literaturfundstelle", { exact: true }),
        ).toHaveValue("Anmerkung")

        await searchForDocUnitWithFileNumberAndDecisionDate(
          page,
          fileNumber,
          "31.12.2019",
        )
        await page.getByLabel("Treffer übernehmen").click()
        await expect(
          page.getByText(`MMG 2024, 2${edition.suffix}, Bilen, Ulviye (Ean)`),
        ).toBeVisible()
      })

      await test.step("Radio buttons should not be visible after saving", async () => {
        await page.getByTestId("list-entry-0").click()
        await expect(page.getByLabel("Rechtsprechung Fundstelle")).toBeHidden()

        await expect(page.getByLabel("Literatur Fundstelle")).toBeHidden()
      })
    },
  )

  test(
    "Literature and caselaw references are chronologically ordered",
    {
      tag: ["@RISDEV-5240", "@RISDEV-5242"],
    },
    async ({
      page,
      prefilledDocumentUnitWithReferences,
      editionWithReferences,
    }) => {
      const fileNumber =
        prefilledDocumentUnitWithReferences.coreData.fileNumbers?.[0] || ""

      await test.step("Add caselaw reference to existing references", async () => {
        await navigateToPeriodicalReferences(
          page,
          editionWithReferences.id || "",
        )
        await expect(
          page.getByText(`MMG 2024, 12-22, Heft 1 (L)`, { exact: true }),
        ).toBeVisible()
        await page.getByLabel("Weitere Angabe", { exact: true }).click()
        await fillInput(page, "Zitatstelle *", "300")
        await fillInput(page, "Klammernzusatz", "ST")

        await searchForDocUnitWithFileNumberAndDecisionDate(
          page,
          fileNumber,
          "31.12.2019",
        )
        await expect(page).toHaveURL(/showAttachmentPanel=true/)
        await page.getByLabel("Treffer übernehmen").click()
        await expect(
          page.getByText(`MMG 2024, 300${editionWithReferences.suffix} (ST)`),
        ).toBeVisible()

        await expect(page).toHaveURL(/showAttachmentPanel=false/)
      })

      await test.step("Add literature reference to existing references", async () => {
        await page.getByLabel("Literatur Fundstelle").click()
        await expect(page.getByLabel("Literatur Fundstelle")).toBeChecked()
        await fillInput(page, "Zitatstelle *", `301-305`)
        await fillInput(page, "Autor Literaturfundstelle", "Bilen, Ulviye")
        await fillInput(page, "Dokumenttyp Literaturfundstelle", "Ean")
        await page.getByText("Ean", { exact: true }).click()
        await expect(
          page.getByLabel("Dokumenttyp Literaturfundstelle", { exact: true }),
        ).toHaveValue("Anmerkung")

        await searchForDocUnitWithFileNumberAndDecisionDate(
          page,
          fileNumber,
          "31.12.2019",
        )
        // wait for panel to open
        await expect(page).toHaveURL(/showAttachmentPanel=true/)

        await page.getByLabel("Treffer übernehmen").click()
        await expect(
          page.getByText(
            `MMG 2024, 301-305${editionWithReferences.suffix}, Bilen, Ulviye (Ean)`,
          ),
        ).toBeVisible()
        await expect(page).toHaveURL(/showAttachmentPanel=false/)
      })

      await test.step("Check correct order in edition", async () => {
        await page.reload()
        await expect(
          page.getByText(
            `MMG 2024, 301-305${editionWithReferences.suffix}, Bilen, Ulviye (Ean)`,
          ),
        ).toBeVisible()
        const correctOrder = [
          "MMG 2024, 12-22, Heft 1 (L)",
          "MMG 2024, 1-11, Heft 1",
          "MMG 2024, 23-25, Heft 1, Picard, Jean-Luc (Ean)",
          "MMG 2024, 26, Heft 1, Janeway, Kathryn (Ean)",
          "MMG 2024, 300, Heft 1 (ST)",
          "MMG 2024, 301-305, Heft 1, Bilen, Ulviye (Ean)",
        ]
        const summaries = await page.getByTestId("citation-summary").all()

        for (let i = 0; i < summaries.length; i++) {
          const text = summaries[i]
          await expect(text).toHaveText(correctOrder[i])
        }
      })

      await test.step("Check correct order in documentation unit", async () => {
        await navigateToPreview(
          page,
          prefilledDocumentUnitWithReferences.documentNumber || "",
        )

        const referencesPreview = page.getByTestId(
          "secondary-references-preview",
        )

        // Make sure the caselaw citations are in the correct order
        expect(await referencesPreview.textContent()).toContain(
          "Sekundäre Fundstellen" +
            "MMG 2024, 1-2, Heft 1 (L)" +
            "MMG 2024, 300, Heft 1 (ST)",
        )

        const literatureReferencesPreview = page.getByTestId(
          "literature-references-preview",
        )

        // Make sure the literature citations are in the correct order
        expect(await literatureReferencesPreview.textContent()).toContain(
          "Literaturfundstellen" +
            "MMG 2024, 3-4, Heft 1, Krümelmonster (Ean)" +
            "MMG 2024, 301-305, Heft 1, Bilen, Ulviye (Ean)",
        )
      })
    },
  )

  test(
    "Editing of existing literature reference",
    {
      tag: ["@RISDEV-5237"],
    },
    async ({ page, editionWithReferences, prefilledDocumentUnit, context }) => {
      const suffix = editionWithReferences.suffix || ""

      await test.step("When editing a literature reference, the citation is a single input containing the joined value of prefix, citation and suffix", async () => {
        const fileNumber = prefilledDocumentUnit.coreData.fileNumbers?.[0] || ""

        await navigateToPeriodicalReferences(
          page,
          editionWithReferences.id || "",
        )
        await expect(
          page.getByText(`MMG 2024, 23-25${suffix}, Picard, Jean-Luc (Ean)`, {
            exact: true,
          }),
        ).toBeVisible()
        await expect(
          page.getByText(`${prefilledDocumentUnit.documentNumber}`, {
            exact: true,
          }),
        ).toHaveCount(2)

        await page.getByTestId("list-entry-2").click()

        await expect(page.getByLabel("Zitatstelle *")).toHaveValue(
          `2024, 23-25${suffix}`,
        )
        await expect(page.getByLabel("Autor Literaturfundstelle")).toHaveValue(
          "Picard, Jean-Luc",
        )

        await expect(page.getByLabel("Zitatstelle Präfix")).toBeHidden()
        await expect(page.getByLabel("Zitatstelle Suffix")).toBeHidden()

        await expect(
          page
            .getByTestId("reference-input-summary")
            .getByText(
              `AG Aachen, 31.12.2019, ${fileNumber}, Anerkenntnisurteil, Unveröffentlicht`,
            ),
        ).toBeVisible()

        await expect(
          page.getByText(`MMG 2024, 23-25${suffix}, Picard, Jean-Luc (Ean)`, {
            exact: true,
          }),
        ).toBeHidden()

        await expect(
          page.getByLabel("Klammernzusatz", { exact: true }),
        ).toBeHidden()
      })

      await test.step("Change existing reference", async () => {
        await fillInput(page, "Zitatstelle *", `2021, 99${suffix}`)
        await fillInput(page, "Autor Literaturfundstelle", "Kirk, James T.")
        await fillInput(page, "Dokumenttyp Literaturfundstelle", "Ean")
        await page.getByText("Ean", { exact: true }).click()
        await expect(
          page.getByLabel("Dokumenttyp Literaturfundstelle", { exact: true }),
        ).toHaveValue("Anmerkung")
        await page.getByLabel("Fundstelle vermerken", { exact: true }).click()

        await expect(
          page.getByText(`MMG 2021, 99${suffix}, Kirk, James T. (Ean)`, {
            exact: true,
          }),
        ).toBeVisible()
        await expect(
          page.getByText(`MMG 2024, 23-25${suffix}, Picard, Jean-Luc (Ebs)`, {
            exact: true,
          }),
        ).toBeHidden()
      })

      await test.step("Changes to the citation are visible in the documentation unit's preview", async () => {
        const previewTab = await context.newPage()

        await navigateToPreview(
          previewTab,
          prefilledDocumentUnit.documentNumber || "",
        )
        await expect(
          previewTab.getByText(`MMG 2021, 99${suffix}, Kirk, James T. (Ean)`, {
            exact: true,
          }),
        ).toBeVisible()
      })
    },
  )
})
