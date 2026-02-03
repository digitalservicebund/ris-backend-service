import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  fillInput,
  navigateToPeriodicalReferences,
  searchForDocUnitWithFileNumberAndDecisionDate,
} from "~/e2e/caselaw/utils/e2e-utils"

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
        const fileNumber =
          prefilledDocumentUnit.coreData?.fileNumbers?.[0] ?? ""
        const suffix = edition.suffix || ""
        await navigateToPeriodicalReferences(page, edition.id || "")

        await test.step("Add reference to periodical evaluation", async () => {
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
          const putRequestPromise = page.waitForResponse(
            (response) =>
              response
                .url()
                .includes("api/v1/caselaw/legalperiodicaledition") &&
              response.status() === 200,
          )
          await page.getByLabel("Treffer übernehmen").click()

          await putRequestPromise

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
            page.getByLabel("Listen Eintrag", { exact: true }),
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
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(2)

          await fillInput(page, "Zitatstelle *", "6")
          await fillInput(page, "Klammernzusatz", "L")

          const putRequestPromise = page.waitForResponse(
            (response) =>
              response
                .url()
                .includes("api/v1/caselaw/legalperiodicaledition") &&
              response.status() === 200,
          )
          await page.getByLabel("Fundstelle vermerken").click()

          await putRequestPromise

          // second reference visible
          await expect(
            page.getByText(`MMG 2024, 6${suffix} (L)`, { exact: true }),
          ).toBeVisible()
        })

        await test.step("If no empty entry in list (e.g. on initial load), click on 'Weitere Fundstelle zu dieser Entscheidung' adds an new item to the list", async () => {
          await page.reload()

          await expect(
            page.getByLabel("Listen Eintrag", { exact: true }),
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
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(3)
        })

        await test.step("New entry with decision can be switched to literature reference", async () => {
          await page.getByLabel("Literatur Fundstelle").click()

          await fillInput(page, "Zitatstelle *", "7")
          await fillInput(page, "Autor Literaturfundstelle", "Einstein, Albert")
          await fillInput(page, "Dokumenttyp Literaturfundstelle", "Ean")
          await page.getByText("Ean", { exact: true }).click()
          await expect(
            page.getByLabel("Dokumenttyp Literaturfundstelle", { exact: true }),
          ).toHaveValue("Anmerkung")
          // Listen for the request triggered by "Fundstelle vermerken" and wait for it to finish, so the fixture cleanup does not run into concurrency issues
          const putRequestPromise = page.waitForRequest((request) =>
            request.url().includes("api/v1/caselaw/legalperiodicaledition"),
          )
          await page.getByLabel("Fundstelle vermerken").click()

          await putRequestPromise

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
            page.getByLabel("Listen Eintrag", { exact: true }),
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
        const suffix = editionWithReferences.suffix || ""
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
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(5)
        })

        await test.step("Click on cancel removes the entry again, as long as it was not saved", async () => {
          await page.getByLabel("Abbrechen").click()
          // 4 references all in summary list mode
          await expect(
            page.getByLabel("Listen Eintrag", { exact: true }),
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
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(5)
        })

        await test.step("Fill missing inputs and add as reference", async () => {
          await fillInput(page, "Zitatstelle *", "6")
          await fillInput(page, "Klammernzusatz", "L")
          await page.getByLabel("Fundstelle vermerken").click()
          await expect(
            page.getByText(`MMG 2024, 6${suffix} (L)`, { exact: true }),
          ).toBeVisible()
          // 5 references + 1 new entry in edit mode with copied decision
          await expect(
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(6)
        })

        await test.step("An added reference with copied decision can be edited, like any other reference (after saving)", async () => {
          await page.getByTestId("list-entry-4").click()
          await fillInput(page, "Klammernzusatz", "LT")
          await page.getByLabel("Fundstelle vermerken").click()
          await expect(
            page.getByText(`MMG 2024, 6${suffix} (LT)`, { exact: true }),
          ).toBeVisible()
        })

        await test.step("After saving switching between literature and caselaw is not possible anymore", async () => {
          await page.getByTestId("list-entry-4").click()
          await page
            .getByLabel("Rechtsprechung Fundstelle")
            .waitFor({ state: "detached" })
          await page
            .getByLabel("Literatur Fundstelle")
            .waitFor({ state: "detached" })
          await expect(
            page.getByLabel("Rechtsprechung Fundstelle"),
          ).toBeHidden()
          await expect(page.getByLabel("Literatur Fundstelle")).toBeHidden()
        })

        await test.step("Click on delete removes the entry again", async () => {
          // Listen for the request triggered by "Eintrag löschen" and wait for it to finish, so the fixture cleanup does not run into concurrency issues
          const deleteRequestPromise = page.waitForResponse(
            (response) =>
              response
                .url()
                .includes("api/v1/caselaw/legalperiodicaledition") &&
              response.status() === 200,
          )

          await page.getByLabel("Eintrag löschen").click()

          await deleteRequestPromise

          // 4 references all in summary list mode
          await expect(
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(4)
        })
      },
    )
  },
)
