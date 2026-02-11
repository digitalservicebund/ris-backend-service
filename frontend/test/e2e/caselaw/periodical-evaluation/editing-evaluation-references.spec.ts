import { expect, Page } from "@playwright/test"
import dayjs from "dayjs"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  fillInput,
  navigateToPeriodicalEvaluation,
  navigateToPeriodicalReferences,
  navigateToPreview,
  searchForDocUnitWithFileNumberAndDecisionDate,
} from "~/e2e/caselaw/utils/e2e-utils"

/* eslint-disable playwright/no-conditional-in-test */

test.describe(
  "Editing and deleting references in periodical evaluation",
  {
    tag: "@RISDEV-4560",
  },
  () => {
    test("Should render correct UI according to edition data", async ({
      page,
      edition,
    }) => {
      const suffix = edition.suffix || ""

      await navigateToPeriodicalReferences(page, edition.id || "")

      await test.step("Citation shows selected prefix and suffix", async () => {
        await expect(page.getByLabel("Zitatstelle Präfix")).toHaveValue(
          "2024, ",
        )
        await expect(page.getByLabel("Zitatstelle Suffix")).toHaveValue(suffix)
      })

      await test.step("Prefix, suffix, Name and Legal Periodical can't be edited", async () => {
        await expect(
          page.getByLabel("Periodikum", { exact: true }),
        ).toBeHidden()

        await expect(
          page.getByLabel("Zitatstelle Präfix", { exact: true }),
        ).not.toBeEditable()
        await expect(
          page.getByLabel("Zitatstelle Suffix", { exact: true }),
        ).not.toBeEditable()
      })
    })

    test(
      "Add decision caselaw references to edition",
      {
        tag: "@RISDEV-4560",
      },
      async ({
        page,
        edition,
        prefilledDocumentUnit,
        secondPrefilledDocumentUnit,
      }) => {
        const suffix = edition.suffix || ""
        const fileNumber = prefilledDocumentUnit.coreData.fileNumbers?.[0] ?? ""
        const secondFileNumber =
          secondPrefilledDocumentUnit.coreData.fileNumbers?.[0] ?? ""

        await navigateToPeriodicalReferences(page, edition.id || "")

        await test.step("A docunit can be added as reference by entering citation and search fields", async () => {
          await searchForDocUnitWithFileNumberAndDecisionDate(
            page,
            fileNumber,
            "31.12.2019",
          )
          // wait search result to be visible
          const searchResultsContainer = page.getByTestId("search-results")
          await expect(
            searchResultsContainer.getByTestId(
              `decision-summary-${prefilledDocumentUnit.documentNumber}`,
            ),
          ).toBeVisible()
          // wait for panel to open
          await expect(page).toHaveURL(/showAttachmentPanel=true/)
          await expect(
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(1)

          await fillInput(page, "Zitatstelle *", "5")
          await fillInput(page, "Klammernzusatz", "LT")
          await page.getByLabel("Treffer übernehmen").click()

          // 1 decision summary visible
          const editableListContainer = page.getByTestId(
            "editable-list-container",
          )
          await expect(
            editableListContainer.getByTestId(
              `decision-summary-${prefilledDocumentUnit.documentNumber}`,
            ),
          ).toHaveCount(1)
          // 1 reference summary visible
          await expect(
            page.getByText(`MMG 2024, 5${suffix} (LT)`, { exact: true }),
          ).toHaveCount(1)

          await expect(
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(2)

          await expect(page).toHaveURL(/showAttachmentPanel=false/)
        })

        await test.step("A docunit can be added to an edition multiple times", async () => {
          await searchForDocUnitWithFileNumberAndDecisionDate(
            page,
            fileNumber,
            "31.12.2019",
          )

          // wait search result to be visible
          const searchResultsContainer = page.getByTestId("search-results")
          await expect(
            searchResultsContainer.getByTestId(
              `decision-summary-${prefilledDocumentUnit.documentNumber}`,
            ),
          ).toBeVisible()

          // wait for panel to open
          await expect(page).toHaveURL(/showAttachmentPanel=true/)

          await expect(page.getByText("Bereits hinzugefügt")).toBeVisible()
          await fillInput(page, "Zitatstelle *", "99")
          await fillInput(page, "Klammernzusatz", "LT")
          await page.getByLabel("Treffer übernehmen").click()

          // second decision summary visible
          const editableListContainer = page.getByTestId(
            "editable-list-container",
          )
          await expect(
            editableListContainer.getByTestId(
              `decision-summary-${prefilledDocumentUnit.documentNumber}`,
            ),
          ).toHaveCount(2)

          // reference summary visible
          await expect(
            page.getByText(`MMG 2024, 99${suffix} (LT)`, { exact: true }),
          ).toBeVisible()

          await expect(
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(3)

          await expect(page).toHaveURL(/showAttachmentPanel=false/)
        })

        await test.step("Other docUnits can be added to an edition", async () => {
          await searchForDocUnitWithFileNumberAndDecisionDate(
            page,
            secondFileNumber,
            "01.01.2020",
          )

          // wait search result to be visible
          const searchResultsContainer = page.getByTestId("search-results")
          await expect(
            searchResultsContainer.getByTestId(
              `decision-summary-${secondPrefilledDocumentUnit.documentNumber}`,
            ),
          ).toBeVisible()

          // wait for panel to open
          await expect(page).toHaveURL(/showAttachmentPanel=true/)

          await fillInput(page, "Zitatstelle *", "104")
          await fillInput(page, "Klammernzusatz", "LT")

          await page.getByLabel("Treffer übernehmen").click()

          // decision summary visible
          const editableListContainer = page.getByTestId(
            "editable-list-container",
          )
          await expect(
            editableListContainer.getByTestId(
              `decision-summary-${secondPrefilledDocumentUnit.documentNumber}`,
            ),
          ).toHaveCount(1)
          // 1 reference summary visible
          await expect(
            page.getByText(`MMG 2024, 104${suffix} (LT)`, { exact: true }),
          ).toBeVisible()

          await expect(
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(4)
        })

        await test.step("The form is cleared after adding a reference", async () => {
          await expect(page.getByLabel("Zitatstelle *")).toBeEmpty()
          await expect(page.getByLabel("Klammernzusatz")).toBeEmpty()
          await expect(page.getByLabel("Gericht")).toBeEmpty()
          await expect(page.getByLabel("Aktenzeichen")).toBeEmpty()
          await expect(page.getByLabel("Datum")).toBeEmpty()
          await expect(page.getByLabel("Dokumenttyp")).toBeEmpty()
        })
      },
    )

    test(
      "Add pending proceeding caselaw references to edition",
      {
        tag: "@RISDEV-7932",
      },
      async ({ page, edition, pendingProceeding }) => {
        const suffix = edition.suffix || ""
        const fileNumber = pendingProceeding.coreData.fileNumbers?.[0] ?? ""

        await navigateToPeriodicalReferences(page, edition.id || "")

        await test.step("A pending proceeding docunit can be added as reference, search results can be filtered for pending proceeding doctypex", async () => {
          await fillInput(page, "Dokumenttyp", "Anh")
          await fillInput(page, "Aktenzeichen", fileNumber)
          await page
            .locator("button")
            .filter({ hasText: "Anhängiges Verfahren" })
            .click()

          await page.getByText("Suchen").click()

          // wait search result to be visible
          const searchResultsContainer = page.getByTestId("search-results")
          await expect(
            searchResultsContainer.getByTestId(
              `decision-summary-${pendingProceeding.documentNumber}`,
            ),
          ).toBeVisible()
          // wait for panel to open
          await expect(page).toHaveURL(/showAttachmentPanel=true/)
          await expect(
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(1)

          await fillInput(page, "Zitatstelle *", "5")
          await fillInput(page, "Klammernzusatz", "LT")
          await page.getByLabel("Treffer übernehmen").click()

          // 1 decision summary visible
          const editableListContainer = page.getByTestId(
            "editable-list-container",
          )
          await expect(
            editableListContainer.getByTestId(
              `decision-summary-${pendingProceeding.documentNumber}`,
            ),
          ).toHaveCount(1)
          // 1 reference summary visible
          await expect(
            page.getByText(`MMG 2024, 5${suffix} (LT)`, { exact: true }),
          ).toHaveCount(1)

          await expect(
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(2)

          await expect(page).toHaveURL(/showAttachmentPanel=false/)
        })

        await test.step("The form is cleared after adding a reference", async () => {
          await expect(page.getByLabel("Zitatstelle *")).toBeEmpty()
          await expect(page.getByLabel("Klammernzusatz")).toBeEmpty()
          await expect(page.getByLabel("Gericht")).toBeEmpty()
          await expect(page.getByLabel("Aktenzeichen")).toBeEmpty()
          await expect(page.getByLabel("Datum")).toBeEmpty()
          await expect(page.getByLabel("Dokumenttyp")).toBeEmpty()
        })
      },
    )

    test("Preview of references", async ({
      context,
      page,
      edition,
      prefilledDocumentUnit,
    }) => {
      await navigateToPeriodicalReferences(page, edition.id || "")
      const fileNumber = prefilledDocumentUnit.coreData.fileNumbers?.[0] ?? ""
      const suffix = edition.suffix || ""

      await test.step("should not open preview if more then one search result", async () => {
        await page.getByText("Suchen").click()
        await expect(page.getByLabel("Ladestatus")).toBeHidden()
        await expect(page.getByLabel("Seitenpanel schließen")).toBeHidden()
      })

      await test.step("should open preview if one search result", async () => {
        await searchForDocUnitWithFileNumberAndDecisionDate(
          page,
          fileNumber,
          "31.12.2019",
        )
        await expect(page.getByText("1 Ergebnis gefunden")).toBeVisible()
        await expect(page).toHaveURL(/showAttachmentPanel=true/)
      })

      await test.step("open editing from side panel", async () => {
        const newTabPromise = page.context().waitForEvent("page")
        await openDocumentationUnitEditModeTabThroughSidePanel(page)
        const newTab = await newTabPromise
        expect(newTab.url()).toContain("/categories")
        await newTab.close()
      })

      await test.step("Add documentunit", async () => {
        await fillInput(page, "Zitatstelle *", "5")
        await fillInput(page, "Klammernzusatz", "LT")
        await page.getByLabel("Treffer übernehmen").click()

        // 1 decision summary visible
        const editableListContainer = page.getByTestId(
          "editable-list-container",
        )
        await expect(
          editableListContainer.getByTestId(
            `decision-summary-${prefilledDocumentUnit.documentNumber}`,
          ),
        ).toHaveCount(1)
        // 1 reference summary visible
        await expect(
          page.getByText(`MMG 2024, 5${suffix} (LT)`, { exact: true }),
        ).toHaveCount(1)

        await expect(
          page.getByLabel("Listen Eintrag", { exact: true }),
        ).toHaveCount(2)
      })

      await test.step("An added citation is visible in the documentation unit's preview", async () => {
        const previewTab = await context.newPage()
        await navigateToPreview(
          previewTab,
          prefilledDocumentUnit.documentNumber || "",
        )
        await expect(
          previewTab.getByText(`MMG 2024, 5${suffix} (LT)`, { exact: true }),
        ).toHaveCount(1)
      })
    })

    test("Changes to an edition are synced to a second tab", async ({
      context,
      edition,
      page,
      prefilledDocumentUnit,
    }) => {
      const suffix = edition.suffix || ""
      await navigateToPeriodicalReferences(page, edition.id || "")
      const secondPage = await context.newPage()

      await test.step("Open up edition on second page", async () => {
        await navigateToPeriodicalReferences(secondPage, edition.id)
        await expect(secondPage.getByLabel("Listen Eintrag")).toHaveCount(1)
      })

      await test.step("Add documentunit on first page", async () => {
        await searchForDocUnitWithFileNumberAndDecisionDate(
          page,
          prefilledDocumentUnit.coreData.fileNumbers?.[0] ?? "",
          "31.12.2019",
        )

        await expect(page).toHaveURL(/showAttachmentPanel=true/)

        await fillInput(page, "Zitatstelle *", "5")
        await fillInput(page, "Klammernzusatz", "LT")
        await page.getByLabel("Treffer übernehmen").click()

        // 1 decision summary visible
        const editableListContainer = page.getByTestId(
          "editable-list-container",
        )
        await expect(
          editableListContainer.getByTestId(
            `decision-summary-${prefilledDocumentUnit.documentNumber}`,
          ),
        ).toHaveCount(1)
        // 1 reference summary visible
        await expect(
          page.getByText(`MMG 2024, 5${suffix} (LT)`, { exact: true }),
        ).toHaveCount(1)

        await expect(
          page.getByLabel("Listen Eintrag", { exact: true }),
        ).toHaveCount(2)
      })

      await test.step("Expect changed to be visible after 10 seconds", async () => {
        const editionIntervalFetchResponse = secondPage.waitForResponse(
          `**/api/v1/caselaw/legalperiodicaledition/${edition.id}`,
          { timeout: 10_000 }, // auto fetch takes place every 10 seconds
        )

        await editionIntervalFetchResponse
        await expect(secondPage.getByLabel("Listen Eintrag")).toHaveCount(2)
      })
    })

    test("Editing of existing reference", async ({
      page,
      editionWithReferences,
      prefilledDocumentUnit,
      context,
    }) => {
      const suffix = editionWithReferences.suffix || ""

      await test.step("When editing a reference, the citation is a single input containing the joined value of prefix, citation and suffix", async () => {
        const fileNumber = prefilledDocumentUnit.coreData.fileNumbers?.[0] || ""

        await navigateToPeriodicalReferences(
          page,
          editionWithReferences.id || "",
        )
        await expect(
          page.getByText(`MMG 2024, 12-22${suffix} (L)`, { exact: true }),
        ).toBeVisible()
        await expect(
          page.getByText(`${prefilledDocumentUnit.documentNumber}`, {
            exact: true,
          }),
        ).toHaveCount(2)

        await page.getByTestId("list-entry-0").click()

        await expect(page.getByLabel("Zitatstelle *")).toHaveValue(
          `2024, 12-22${suffix}`,
        )
        await expect(page.getByLabel("Klammernzusatz")).toHaveValue("L")

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
          page.getByText(`MMG 2024, 12-22${suffix} (L)`, {
            exact: true,
          }),
        ).toBeHidden()

        await expect(
          page.getByLabel("Dokumenttyp", { exact: true }),
        ).toBeHidden()
      })

      await test.step("Change existing reference", async () => {
        await fillInput(page, "Zitatstelle *", `2021, 2${suffix}`)
        await fillInput(page, "Klammernzusatz", "LT")
        await page.getByLabel("Fundstelle vermerken", { exact: true }).click()

        await expect(
          page.getByText(`MMG 2021, 2${suffix} (LT)`, { exact: true }),
        ).toBeVisible()
        await expect(
          page.getByText(`MMG 2024, 12-22${suffix} (L)`, { exact: true }),
        ).toBeHidden()
      })

      await test.step("Changes to the citation are visible in the documentation unit's preview", async () => {
        const previewTab = await context.newPage()

        await navigateToPreview(
          previewTab,
          prefilledDocumentUnit.documentNumber || "",
        )
        await expect(
          previewTab.getByText(
            `MMG 2021, 2${editionWithReferences.suffix} (LT)`,
            {
              exact: true,
            },
          ),
        ).toBeVisible()
      })
    })

    test("should scroll to guiding principle, if present", async ({
      page,
      prefilledDocumentUnit,
      edition,
    }) => {
      const fileNumber = prefilledDocumentUnit.coreData.fileNumbers?.[0] || ""

      await navigateToPeriodicalReferences(page, edition.id || "")

      await searchForDocUnitWithFileNumberAndDecisionDate(
        page,
        fileNumber,
        "31.12.2019",
      )
      // wait for panel to open
      await expect(page).toHaveURL(/showAttachmentPanel=true/)

      await expect(page.getByText("Leitsatz")).toBeInViewport()
      await expect(
        page.getByText(prefilledDocumentUnit.shortTexts.guidingPrinciple!),
      ).toBeInViewport()
    })

    test("should scroll to tenor, if no guiding principle present", async ({
      page,
      prefilledDocumentUnitWithTexts,
      edition,
    }) => {
      await navigateToPeriodicalReferences(page, edition.id || "")

      await searchForDocUnitWithFileNumberAndDecisionDate(
        page,
        prefilledDocumentUnitWithTexts.coreData.fileNumbers?.[0] || "",
        "31.12.2019",
      )
      // wait for panel to open
      await expect(page).toHaveURL(/showAttachmentPanel=true/)

      await expect(page.getByText("Tenor", { exact: true })).toBeInViewport()
      await expect(
        page.getByText(prefilledDocumentUnitWithTexts.longTexts.tenor!),
      ).toBeInViewport()
    })

    test(
      "Page number resets when new search started",
      { tag: "@RISDEV-5434" },
      async ({ page, edition }) => {
        await test.step("Page number resets when new search started", async () => {
          await navigateToPeriodicalReferences(page, edition.id)

          await page.getByText("Suchen").click()
          await page.getByLabel("nächste Ergebnisse").click()
          await expect(page.getByText("Seite 2")).toBeVisible()
          await page.getByText("Suchen").click()

          await expect(page.getByText("Seite 2")).toBeHidden()
          await expect(page.getByText("Seite 1")).toBeVisible()
        })
      },
    )

    test(
      "Deleting references in periodical evaluation",
      {
        tag: ["@RISDEV-5146", "@RISDEV-5237"],
      },
      async ({
        context,
        page,
        editionWithReferences,
        prefilledDocumentUnit,
      }) => {
        await test.step("A reference can be deleted", async () => {
          await navigateToPeriodicalReferences(
            page,
            editionWithReferences.id || "",
          )
          await expect(
            page.getByText(`MMG 2024, 12-22, Heft 1 (L)`, { exact: true }),
          ).toBeVisible()
          const count = page.getByLabel("Listen Eintrag")
          await expect(count).toHaveCount(4)

          await page.getByTestId("list-entry-0").click()

          await page.getByText("Eintrag löschen").click()

          await expect(
            page.getByLabel("Eintrag löschen", { exact: true }),
          ).toBeHidden()
          await expect(
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(3)
          await expect(
            page.getByText(`MMG 2024, 12-22, Heft 1 (L)`, { exact: true }),
          ).toBeHidden()
        })

        await test.step("On reload deleted references are not visible", async () => {
          await page.reload()
          await expect(
            page.getByText(`MMG 2024, 12-22, Heft 1 (L)`, { exact: true }),
          ).toBeHidden()
          await expect(
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(3)
        })

        await test.step("Literature reference can be deleted", async () => {
          await expect(
            page.getByText(`MMG 2024, 23-25, Heft 1, Picard, Jean-Luc (Ean)`, {
              exact: true,
            }),
          ).toBeVisible()
          const count = page.getByLabel("Listen Eintrag")
          await expect(count).toHaveCount(3)

          await page.getByTestId("list-entry-1").click()

          await page.getByText("Eintrag löschen").click()

          await expect(
            page.getByLabel("Eintrag löschen", { exact: true }),
          ).toBeHidden()
          await expect(
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(2)
          await expect(
            page.getByText(`MMG 2024, 23-25, Heft 1, Picard, Jean-Luc (Ean)`, {
              exact: true,
            }),
          ).toBeHidden()
        })

        await test.step("On reload deleted references are not visible", async () => {
          await page.reload()
          await expect(
            page.getByText(`MMG 2024, 23-25, Heft 1, Picard, Jean-Luc (Ean)`, {
              exact: true,
            }),
          ).toBeHidden()
          await expect(
            page.getByLabel("Listen Eintrag", { exact: true }),
          ).toHaveCount(2)
        })

        await test.step("Deleted references disappear from the documentation unit's preview", async () => {
          // open documentation unit preview in new tab

          const previewTab = await context.newPage()
          await navigateToPreview(
            previewTab,
            prefilledDocumentUnit.documentNumber || "",
          )
          await expect(
            page.getByText(`MMG 2024, 12-22, Heft 1 (L)`, { exact: true }),
          ).toBeHidden()
          await expect(
            page.getByText(`MMG 2024, 23-25, Heft 1, Picard, Jean-Luc (Ean)`, {
              exact: true,
            }),
          ).toBeHidden()
          await expect(
            previewTab.getByText("Fundstellen", { exact: true }),
          ).toBeHidden()
          await expect(
            previewTab.getByText("Literaturfundstellen", { exact: true }),
          ).toBeHidden()
        })
      },
    )

    test("Adding references for other docoffices' documentation units", async ({
      context,
      page,
      edition,
      foreignDocumentationUnit,
    }) => {
      await test.step("Search for documentation unit of foreign doc office", async () => {
        await navigateToPeriodicalReferences(page, edition.id || "")
        const court = foreignDocumentationUnit.court?.label || ""
        const fileNumber = foreignDocumentationUnit.fileNumber || ""
        const date = foreignDocumentationUnit.decisionDate
          ? dayjs(foreignDocumentationUnit.decisionDate).format("DD.MM.YYYY")
          : ""

        await fillInput(page, "Zitatstelle *", "12")
        await fillInput(page, "Klammernzusatz", "L")
        await fillInput(page, "Gericht", court)
        await page.getByText(court, { exact: true }).click()
        await fillInput(page, "Aktenzeichen", fileNumber)
        await fillInput(page, "Datum", date)

        await page.getByText("Suchen").click()

        await expect(
          page.getByText(
            `BAG, 02.01.1963, fileNumber1, Änderungsnorm, Veröffentlicht`,
          ),
        ).toBeVisible()
      })

      await test.step("An added reference is visible in the other docoffice's documentation unit's preview", async () => {
        await expect(
          page.getByLabel("Listen Eintrag", { exact: true }),
        ).toHaveCount(1)
        await page.getByLabel("Treffer übernehmen").click()
        await expect(
          page.getByLabel("Listen Eintrag", { exact: true }),
        ).toHaveCount(2)

        const previewTab = await context.newPage()
        await navigateToPreview(
          previewTab,
          foreignDocumentationUnit.documentNumber || "",
        )
        await expect(
          previewTab.getByText(`MMG 2024, 12${edition.suffix} (L)`, {
            exact: true,
          }),
        ).toBeVisible()
      })
    })

    // eslint-disable-next-line playwright/no-skipped-test
    test.skip(
      "External user cannot edit or delete periodical editions",
      { tag: ["@RISDEV-4724", "@RISDEV-4519"] },
      async ({ pageWithExternalUser, edition }) => {
        await navigateToPeriodicalEvaluation(pageWithExternalUser)

        await test.step("A periodical can be selected using a combo box.", async () => {
          await fillInput(pageWithExternalUser, "Periodikum", "MMG")
          const periodical = pageWithExternalUser.getByText(
            "MMG | Medizin Mensch Gesellschaft" + "nicht amtlich",
            {
              exact: true,
            },
          )
          await expect(periodical).toBeVisible()
          await periodical.click()
          await expect(
            pageWithExternalUser.getByLabel("Periodikum", { exact: true }),
          ).toHaveValue("MMG")
        })

        await test.step("User can view but not edit or delete the editions", async () => {
          // Define the specific row first
          const editionRow = pageWithExternalUser.getByRole("row", {
            name: new RegExp(edition.name || ""),
          })

          await expect(
            editionRow.getByLabel("Ausgabe bearbeiten"),
          ).toBeDisabled()

          await expect(editionRow.getByLabel("Ausgabe löschen")).toBeDisabled()
        })
      },
    )

    test(
      "Scrolling behaviour in long lists",
      { tag: "@RISDEV-6030" },
      async ({ page, editionWithManyReferences }) => {
        await test.step("Click on 'Weitere Angabe' on top of references list, scrolls new entry at the bottom into viewport", async () => {
          await navigateToPeriodicalReferences(
            page,
            editionWithManyReferences.id || "",
          )
          await expect(page.getByLabel("Listen Eintrag")).toHaveCount(5)
          await page.getByLabel("Weitere Angabe Top").click()
          await expect(page.getByLabel("Listen Eintrag")).toHaveCount(6)
          await expect(
            page.getByRole("heading", { name: "Fundstelle hinzufügen" }),
          ).toBeInViewport()
        })

        await test.step("Click on 'Weitere Angabe' at the bottom also scrolls new entry into viewport", async () => {
          await page.reload()
          await navigateToPeriodicalReferences(
            page,
            editionWithManyReferences.id || "",
          )
          await expect(page.getByLabel("Listen Eintrag")).toHaveCount(5)
          await page.getByLabel("Weitere Angabe", { exact: true }).click()
          await expect(page.getByLabel("Listen Eintrag")).toHaveCount(6)
          await expect(
            page.getByLabel("Nach Entscheidung suchen"),
          ).toBeInViewport()
        })

        await test.step("Click on 'Suche' scrolls search results into viewport", async () => {
          await page.getByLabel("Nach Entscheidung suchen").click()
          await expect(
            page.getByText("Passende Suchergebnisse:"),
          ).toBeInViewport()
        })
      },
    )

    async function openDocumentationUnitEditModeTabThroughSidePanel(
      page: Page,
    ) {
      await expect(
        page,
        "Opened content side panel is required to proceed",
      ).toHaveURL(/showAttachmentPanel=true/)

      await page
        .getByRole("link", {
          name: "Dokumentationseinheit in einem neuen Tab bearbeiten",
        })
        .click()
    }
  },
)
