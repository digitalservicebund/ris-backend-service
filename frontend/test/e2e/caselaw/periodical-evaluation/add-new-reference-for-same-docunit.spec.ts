import { expect } from "@playwright/test"
import {
  navigateToPeriodicalReferences,
  searchForDocUnitWithFileNumberAndDecisionDate,
  fillInput,
  waitForInputValue,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"

/* eslint-disable playwright/no-conditional-in-test */
test.describe(
  "Add new reference for same docunit in periodical evaluation",
  {
    tag: ["@RISDEV-6098"],
  },
  () => {
    test(
      "Add new references for already noted decision",
      {
        tag: ["@RISDEV-6098"],
      },
      async ({ page, edition, prefilledDocumentUnit }) => {
        const fileNumber = prefilledDocumentUnit.coreData.fileNumbers?.[0] ?? ""
        const suffix = edition.suffix || ""
        await navigateToPeriodicalReferences(page, edition.id || "")

        await test.step("Add docunit to periodical evaliation", async () => {
          await searchForDocUnitWithFileNumberAndDecisionDate(
            page,
            fileNumber,
            "31.12.2019",
          )
          // wait for search result to be visible
          const searchResultsContainer = page.getByTestId("search-results")
          await expect(
            searchResultsContainer.getByTestId(
              `decision-summary-${prefilledDocumentUnit.documentNumber}`,
            ),
          ).toBeVisible()

          await fillInput(page, "Zitatstelle *", "5")
          await fillInput(page, "Klammernzusatz", "LT")
          await page.getByLabel("Treffer übernehmen").click()

          // 1 decision summary visible in list summary
          await expect(
            page
              .getByTestId("reference-list-summary")
              .getByTestId(
                `decision-summary-${prefilledDocumentUnit.documentNumber}`,
              ),
          ).toHaveCount(1)

          // the added reference + one added empty entry
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(2)
        })

        await test.step("Click on 'Weitere Fundstelle zu dieser Entscheidung' adds the decision to the existing empty entry", async () => {
          await page
            .getByLabel("Weitere Fundstelle zu dieser Entscheidung")
            .click()
          // 1 decision summary visible in summary list mode
          await expect(
            page
              .getByTestId("reference-list-summary")
              .getByTestId(
                `decision-summary-${prefilledDocumentUnit.documentNumber}`,
              ),
          ).toHaveCount(1)
          // 1 decision summary visible in input mode
          await expect(
            page
              .getByTestId("reference-input-summary")
              .getByTestId(
                `decision-summary-${prefilledDocumentUnit.documentNumber}`,
              ),
          ).toHaveCount(1)

          // the new decision is written into the existing empty entry/ the new entry with decision replaces the existing empty one
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(2)

          await fillInput(page, "Zitatstelle *", "6")
          await fillInput(page, "Klammernzusatz", "L")
          await page.getByLabel("Fundstelle vermerken").click()
          // second reference visible
          await expect(
            page.getByText(`MMG 2024, 6${suffix} (L)`, { exact: true }),
          ).toBeVisible()
        })

        await test.step("If no empty entry in list (e.g. on initial load), click on 'Weitere Fundstelle zu dieser Entscheidung' adds an new item to the list", async () => {
          await page.reload()

          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(2)
          await page
            .getByLabel("Weitere Fundstelle zu dieser Entscheidung")
            .nth(1)
            .click()

          // 2 decision summary visible in summary mode
          await expect(
            page
              .getByTestId("reference-list-summary")
              .getByTestId(
                `decision-summary-${prefilledDocumentUnit.documentNumber}`,
              ),
          ).toHaveCount(2)
          // 1 decision summary visible in input mode
          await expect(
            page
              .getByTestId("reference-input-summary")
              .getByTestId(
                `decision-summary-${prefilledDocumentUnit.documentNumber}`,
              ),
          ).toHaveCount(1)

          // 2 in summary, 1 in edit mode
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(3)
        })

        await test.step("New entry with decision can be switched to literature reference", async () => {
          await page.getByLabel("Literatur Fundstelle").click()

          await fillInput(page, "Zitatstelle *", "7")
          await fillInput(page, "Autor Literaturfundstelle", "Einstein, Albert")
          await fillInput(page, "Dokumenttyp Literaturfundstelle", "Ean")
          await page.getByText("Ean", { exact: true }).click()
          await waitForInputValue(
            page,
            "[aria-label='Dokumenttyp Literaturfundstelle']",
            "Anmerkung",
          )
          await page.getByLabel("Fundstelle vermerken").click()
          await expect(
            page.getByText(`MMG 2024, 7${suffix}, Einstein, Albert (Ean)`),
          ).toBeVisible()

          // 3 decision summary visible, all in summary mode
          await expect(
            page
              .getByTestId("reference-list-summary")
              .getByTestId(
                `decision-summary-${prefilledDocumentUnit.documentNumber}`,
              ),
          ).toHaveCount(3)

          // 2 in summary, 1 in edit mode
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(4)
        })
      },
    )

    test(
      "In a list with several decisions, the correct decision is copied",
      {
        tag: ["@RISDEV-6098"],
      },
      async ({ page, editionWithReferences, prefilledDocumentUnit }) => {
        await navigateToPeriodicalReferences(
          page,
          editionWithReferences.id || "",
        )

        await test.step("Click on first 'Weitere Fundstelle zu dieser Entscheidung', copies the correct decision to the bottom of the list", async () => {
          await page
            .getByLabel("Weitere Fundstelle zu dieser Entscheidung")
            .first()
            .click()

          // the first decision is copied to the entry mode item at the bottom
          await expect(
            page
              .getByTestId("reference-input-summary")
              .getByTestId(
                `decision-summary-${prefilledDocumentUnit.documentNumber}`,
              ),
          ).toHaveCount(1)
        })

        await test.step("Click on the second 'Weitere Fundstelle zu dieser Entscheidung', replaces the decision in edit mode", async () => {
          await page
            .getByLabel("Weitere Fundstelle zu dieser Entscheidung")
            .nth(1)
            .click()

          // the second decision is copied to the entry mode item at the bottom
          await expect(
            page
              .getByTestId("reference-input-summary")
              .getByTestId(`decision-summary-YYTestDoc0001`),
          ).toHaveCount(1)
        })
      },
    )

    test(
      "An entry with a copied decision can be canceled and deleted",
      {
        tag: ["@RISDEV-6098"],
      },
      async ({ page, editionWithReferences, prefilledDocumentUnit }) => {
        await navigateToPeriodicalReferences(
          page,
          editionWithReferences.id || "",
        )

        await test.step("Click on first 'Weitere Fundstelle zu dieser Entscheidung', copies the decision to the bottom of the list", async () => {
          await page
            .getByLabel("Weitere Fundstelle zu dieser Entscheidung")
            .first()
            .click()

          // the first decision is copied to the entry mode item at the bottom
          await expect(
            page
              .getByTestId("reference-input-summary")
              .getByTestId(
                `decision-summary-${prefilledDocumentUnit.documentNumber}`,
              ),
          ).toHaveCount(1)

          // 4 references + 1 new entry in edit mode with copied decision
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(5)
        })

        await test.step("Click on cancel removes the entry again, as long as it was not saved", async () => {
          await page.getByLabel("Abbrechen").click()
          // 4 references all in summary list mode
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(4)
        })

        await test.step("Click on first 'Weitere Fundstelle zu dieser Entscheidung', copies again the decision to the bottom of the list", async () => {
          await page
            .getByLabel("Weitere Fundstelle zu dieser Entscheidung")
            .first()
            .click()

          // the first decision is copied to the entry mode item at the bottom
          await expect(
            page
              .getByTestId("reference-input-summary")
              .getByTestId(
                `decision-summary-${prefilledDocumentUnit.documentNumber}`,
              ),
          ).toHaveCount(1)

          // 4 references + 1 new entry in edit mode with copied decision
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(5)
        })

        await test.step("Click on delete removes the entry again", async () => {
          await page.getByLabel("Eintrag löschen").click()
          // 4 references all in summary list mode
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(4)
        })
      },
    )
  },
)
