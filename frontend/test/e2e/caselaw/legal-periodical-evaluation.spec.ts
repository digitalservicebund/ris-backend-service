import { expect, Page } from "@playwright/test"
import dayjs from "dayjs"
import {
  fillInput,
  navigateToPeriodicalEvaluation,
  navigateToPeriodicalReferences,
  navigateToPreview,
  waitForInputValue,
} from "./e2e-utils"
import { caselawTest as test } from "./fixtures"
import { generateString } from "~/test-helper/dataGenerators"

const formattedDate = dayjs().format("DD.MM.YYYY")

/* eslint-disable playwright/no-conditional-expect */
/* eslint-disable playwright/no-conditional-in-test */

test.describe(
  "Periodical evaluation",
  {
    tag: "@RISDEV-4264",
    annotation: {
      type: "epic",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-4264",
    },
  },
  () => {
    test(
      "Periodicals overview with a list of editions",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4497",
        },
      },
      async ({ page, edition }) => {
        await test.step("References is a new selectable menu item in the top navigation", async () => {
          await navigateToPeriodicalEvaluation(page)
        })

        await test.step("Initially, an empty table with a hint is shown", async () => {
          await expect(
            page.getByText(
              "Wählen Sie ein Periodikum um die Ausgaben anzuzeigen.",
              {
                exact: true,
              },
            ),
          ).toBeVisible()
          await expect(page.locator(".table > td")).toHaveCount(1) // only header
          await expect(
            page.getByText(
              "Ausgabe" +
                "Periodikum" +
                "Anzahl der Fundstellen" +
                "Hinzugefügt",
            ),
          ).toBeVisible()
          await expect(page.locator(".table > tr")).toHaveCount(0)
        })

        await test.step("The dropdown indicates the periodical type (amtlich/nichtamtlich)", async () => {
          await fillInput(page, "Periodikum", "ABl AHK")
          await expect(
            page.getByText("ABl AHK | ABl AHK" + "amtlich", {
              exact: true,
            }),
          ).toBeVisible()
        })

        await test.step("A periodical can be selected using a combo box.", async () => {
          await fillInput(page, "Periodikum", "MMG")
          const periodical = page.getByText(
            "MMG | Medizin Mensch Gesellschaft" + "nicht amtlich",
            {
              exact: true,
            },
          )
          await expect(periodical).toBeVisible()
          await periodical.click()
          await waitForInputValue(page, "[aria-label='Periodikum']", "MMG")
        })

        await test.step("An existing periodical edition appears in the results", async () => {
          await expect(page.locator(".table > tr >> nth=0")).toBeVisible()

          await expect(
            page.getByText((edition.name || "") + "MMG" + "0" + formattedDate),
          ).toBeVisible()
        })

        await test.step("The table is cleared when filter is deleted", async () => {
          await page.locator("[aria-label='Auswahl zurücksetzen']").click()
          await expect(
            page.getByText(
              "Wählen Sie ein Periodikum um die Ausgaben anzuzeigen.",
              {
                exact: true,
              },
            ),
          ).toBeVisible()
          await expect(page.locator(".table > tr")).toHaveCount(0)
        })

        await test.step("The table is cleared when a periodical without edtions is selected", async () => {
          await fillInput(page, "Periodikum", "ZAU")
          await page
            .getByText("ZAU | Zeitschrift für angewandte Umweltforschung", {
              exact: true,
            })
            .click()
          await waitForInputValue(page, "[aria-label='Periodikum']", "ZAU")
          await expect(
            page.getByText("Keine Suchergebnisse gefunden", { exact: true }),
          ).toBeVisible()
          await expect(page.locator(".table > tr")).toHaveCount(0)
        })

        await test.step("By clicking on an edition, the detail view is opened.", async () => {
          await fillInput(page, "Periodikum", "MMG")
          await page
            .getByText("MMG | Medizin Mensch Gesellschaft", { exact: true })
            .click()
          await expect(page.locator(".table > tr >> nth=0")).toBeVisible()
          const pagePromise = page.context().waitForEvent("page")
          const line = page.getByText(
            (edition.name || "") + "MMG" + "0" + formattedDate,
          )
          await line.locator("a").click()
          const newTab = await pagePromise
          await expect(newTab).toHaveURL(
            `/caselaw/periodical-evaluation/${edition.id}/references`,
          )
        })
      },
    )

    test(
      "New periodical evaluation",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4499",
        },
      },

      async ({ page }) => {
        await navigateToPeriodicalEvaluation(page)

        await test.step("A legal periodical can be selected", async () => {
          await fillInput(page, "Periodikum", "wdg")
          await page
            .getByText("WdG | Welt der Gesundheitsversorgung", {
              exact: true,
            })
            .click()
        })

        await test.step("A new evaluation is started using the “Neue Periodikumsauswertung button.", async () => {
          const newLegalPeriodicalEvaluation = page.getByLabel(
            "Neue Periodikumsauswertung",
          )
          await expect(newLegalPeriodicalEvaluation).toBeVisible()
          await newLegalPeriodicalEvaluation.click()

          // Wait until the page is fully loaded
          await page.waitForLoadState("load")
          await page.waitForURL(
            /\/caselaw\/periodical-evaluation\/[0-9a-fA-F\-]{36}\/edition/,
          )
        })

        await test.step("The inputs are correctly validated (name have to be chosen)", async () => {
          await expect(page.getByText("Pflichtfeld nicht befüllt")).toBeHidden()
          await page.getByLabel("Fortfahren").click()
          await expect(
            page.locator(`text="Pflichtfeld nicht befüllt"`),
          ).toHaveCount(1)
        })

        const name = generateString()

        await test.step("Prefix, suffix and name can be set", async () => {
          await fillInput(page, "Präfix", "präfix")
          await fillInput(page, "Suffix", "suffix")
          await fillInput(page, "Name der Ausgabe", name)
          await expect(page.getByLabel("Präfix")).toHaveValue("präfix")
          await expect(page.getByLabel("Suffix")).toHaveValue("suffix")
          await expect(page.getByLabel("Name der Ausgabe")).toHaveValue(name)
        })

        try {
          await test.step("'Fortfahren' saved the edition and replaces url with new edition id", async () => {
            await page.getByLabel("Fortfahren").click()

            await page.waitForURL(
              /\/caselaw\/periodical-evaluation\/[0-9a-fA-F\-]{36}\/references/,
              { timeout: 5_000 },
            )

            await expect(
              page.getByText("Periodikumsauswertung | WdG " + name, {
                exact: true,
              }),
            ).toBeVisible()
          })

          await test.step("The edition can be deleted", async () => {
            await navigateToPeriodicalEvaluation(page)
            await fillInput(page, "Periodikum", "wdg")
            await page
              .getByText("WdG | Welt der Gesundheitsversorgung", {
                exact: true,
              })
              .click()
            await waitForInputValue(page, "[aria-label='Periodikum']", "WdG")
            await expect(page.locator(".table > tr >> nth=0")).toBeVisible()
            const line = page.getByText(name + "WdG0" + formattedDate)

            await line.locator("[aria-label='Ausgabe löschen']").click()
            await expect(
              page.getByText(name + "WdG0" + formattedDate),
            ).toBeHidden()
            await page.reload()
            await expect(
              page.getByTestId("periodical-evaluation-title"),
            ).toBeVisible()
            await expect(
              page.getByText(name + "WdG0" + formattedDate),
            ).toBeHidden()
          })

          // make sure the edition is deleted also if the test fails
        } finally {
          await navigateToPeriodicalEvaluation(page)
          await fillInput(page, "Periodikum", "wdg")
          await page
            .getByText("WdG | Welt der Gesundheitsversorgung", { exact: true })
            .click()
          if (await page.locator(".table > tr >> nth=0").isVisible()) {
            const line = page.getByText(name + "WdG0" + formattedDate)
            await line.locator("[aria-label='Ausgabe löschen']").click()
          }
        }
      },
    )

    test(
      "Periodical edition reference editing",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4560",
        },
      },
      async ({
        context,
        page,
        edition,
        prefilledDocumentUnit,
        secondPrefilledDocumentUnit,
      }) => {
        const fileNumber = prefilledDocumentUnit.coreData.fileNumbers?.[0] || ""
        const secondFileNumber =
          secondPrefilledDocumentUnit.coreData.fileNumbers?.[0] || ""
        const suffix = edition.suffix || ""

        await navigateToPeriodicalReferences(page, edition.id || "")

        await test.step("Citation shows selected prefix and suffix", async () => {
          await expect(page.getByLabel("Zitatstelle Präfix")).toHaveValue(
            "2024, ",
          )
          await expect(page.getByLabel("Zitatstelle Suffix")).toHaveValue(
            suffix,
          )
        })

        await test.step("Prefix, suffix, Name and Legal Periodical can't be edited", async () => {
          await expect(page.locator("[aria-label='Periodikum']")).toBeHidden()

          await expect(
            page.locator("[aria-label='Zitatstelle Präfix']"),
          ).not.toBeEditable()
          await expect(
            page.locator("[aria-label='Zitatstelle Suffix']"),
          ).not.toBeEditable()
        })

        await test.step("should open and close document preview in side panel", async () => {
          await searchForDocUnitWithFileNumber(page, fileNumber, "31.12.2019")
          await openSidePanelPreview(page, fileNumber)
          await expect(page.getByLabel("Seitenpanel öffnen")).toBeHidden()
          await expect(
            page.locator("[aria-label='Vorschau anzeigen']"),
          ).toBeVisible()
          await page.getByLabel("Seitenpanel schließen").click()
          await expect(page).toHaveURL(/showAttachmentPanel=false/)
          await page.reload()
        })

        await test.step("Citation input is validated when input is left", async () => {})

        await test.step("A docunit can be added as reference by entering citation and search fields", async () => {
          await searchForDocUnitWithFileNumber(page, fileNumber, "31.12.2019")
          await expect(
            page.getByText(
              `AG Aachen, 31.12.2019, ${fileNumber}, Anerkenntnisurteil, Unveröffentlicht`,
            ),
          ).toBeVisible()
          await expect(page.getByText("Bereits hinzugefügt")).toBeHidden()
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(1)

          await fillInput(page, "Zitatstelle *", "5")
          await fillInput(page, "Klammernzusatz", "LT")
          await page.getByLabel("Treffer übernehmen").click()
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(2)
        })

        await test.step("A docunit can be added to an edition multiple times", async () => {
          await searchForDocUnitWithFileNumber(page, fileNumber, "31.12.2019")
          await expect(
            page.getByRole("link", {
              name: `AG Aachen, 31.12.2019, ${fileNumber}, Anerkenntnisurteil, Unveröffentlicht`,
            }),
          ).toBeVisible()
          await expect(page.getByText("Bereits hinzugefügt")).toBeVisible()
          await fillInput(page, "Zitatstelle *", "99")
          await fillInput(page, "Klammernzusatz", "LT")
          await page.getByLabel("Treffer übernehmen").click()
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(3)
        })

        await test.step("A reference is added to the editable list after being added", async () => {
          const decisionElement = page.getByText(
            `AG Aachen, 31.12.2019, ${fileNumber}, Anerkenntnisurteil`,
          )
          await expect(decisionElement).toHaveCount(2)

          // Assert that both elements are visible
          await expect(decisionElement.nth(0)).toBeVisible()
          await expect(decisionElement.nth(1)).toBeVisible()

          await expect(
            page.getByText(`MMG 2024, 5${suffix} (LT)`, { exact: true }),
          ).toBeVisible()
        })

        await test.step("Other docUnits can be added to an edition", async () => {
          await searchForDocUnitWithFileNumber(
            page,
            secondFileNumber,
            "01.01.2020",
          )
          await expect(
            page.getByText(
              `AG Aachen, 01.01.2020, ${secondFileNumber}, Anerkenntnisurteil, Unveröffentlicht`,
            ),
          ).toBeVisible()

          await fillInput(page, "Zitatstelle *", "104")
          await fillInput(page, "Klammernzusatz", "LT")

          await page.getByLabel("Treffer übernehmen").click()
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(4)

          await expect(
            page.getByText(`MMG 2024, 104${suffix} (LT)`, { exact: true }),
          ).toBeVisible()
        })

        await test.step("The form is cleared after adding a reference ", async () => {
          await expect(page.getByLabel("Zitatstelle *")).toBeEmpty()
          await expect(page.getByLabel("Klammernzusatz")).toBeEmpty()
          await expect(page.getByLabel("Gericht")).toBeEmpty()
          await expect(page.getByLabel("Aktenzeichen")).toBeEmpty()
          await expect(page.getByLabel("Entscheidungsdatum")).toBeEmpty()
          await expect(page.getByLabel("Dokumenttyp")).toBeEmpty()
        })

        // open documentation unit preview in new tab
        const previewTab = await context.newPage()
        await navigateToPreview(
          previewTab,
          prefilledDocumentUnit.documentNumber || "",
        )

        // open documentation unit preview in new tab
        const secondPreviewTab = await context.newPage()
        await navigateToPreview(
          secondPreviewTab,
          secondPrefilledDocumentUnit.documentNumber || "",
        )

        await test.step("An added citation is visible in the documentation unit's preview ", async () => {
          await expect(
            previewTab.getByText(`MMG 2024, 5${suffix} (LT)`, { exact: true }),
          ).toHaveCount(1)
          await expect(
            previewTab.getByText(`MMG 2024, 99${suffix} (LT)`, { exact: true }),
          ).toHaveCount(1)
          await expect(
            secondPreviewTab.getByText(`MMG 2024, 104${suffix} (LT)`, {
              exact: true,
            }),
          ).toHaveCount(1)
        })

        await test.step("When editing a reference, the citation is a single input containing the joined value of prefix, citation and suffix", async () => {
          await page.getByTestId("list-entry-0").click()
          await expect(page.getByLabel("Zitatstelle *")).toHaveValue(
            `2024, 5${suffix}`,
          )
          await expect(page.getByLabel("Klammernzusatz")).toHaveValue("LT")

          await expect(page.getByLabel("Zitatstelle Präfix")).toBeHidden()
          await expect(page.getByLabel("Zitatstelle Suffix")).toBeHidden()

          await expect(page.getByLabel("Gericht")).toHaveValue("AG Aachen")
          await expect(
            page.locator("[aria-label='Gericht']"),
          ).not.toBeEditable()

          await expect(page.getByLabel("Aktenzeichen")).toHaveValue(fileNumber)
          await expect(
            page.locator("[aria-label='Aktenzeichen']"),
          ).not.toBeEditable()

          await expect(page.getByLabel("Entscheidungsdatum")).toHaveValue(
            "31.12.2019",
          )
          await expect(
            page.locator("[aria-label='Entscheidungsdatum']"),
          ).not.toBeEditable()

          await expect(page.getByLabel("Dokumenttyp")).toHaveValue(
            "Anerkenntnisurteil",
          )
          await expect(
            page.locator("[aria-label='Dokumenttyp']"),
          ).not.toBeEditable()

          // validate citation in edit mode (not allowed to be empty)
          await fillInput(page, "Zitatstelle *", "")
          await page.getByLabel("Fundstelle vermerken", { exact: true }).click()
          await expect(
            page.getByText("Pflichtfeld nicht befüllt"),
          ).toBeVisible()

          await fillInput(page, "Zitatstelle *", `2021, 2${suffix}`)
          await fillInput(page, "Klammernzusatz", "L")
          await page.getByLabel("Fundstelle vermerken", { exact: true }).click()

          await expect(
            page.getByText(`MMG 2021, 2${suffix} (L)`, { exact: true }),
          ).toBeVisible()
        })

        await test.step("Changes to the citation are visible in the documentation unit's preview ", async () => {
          await previewTab.reload()
          await expect(
            previewTab.getByText(`MMG 2021, 2${suffix} (L)`, { exact: true }),
          ).toHaveCount(1, { timeout: 10_000 })
          await expect(
            previewTab.getByText(`MMG 2024, 99${suffix} (LT)`, { exact: true }),
          ).toHaveCount(1)
        })

        await test.step("Unchanged citation is unchanged in preview ", async () => {
          await secondPreviewTab.reload()
          await expect(
            secondPreviewTab.getByText(`MMG 2024, 104${suffix} (LT)`, {
              exact: true,
            }),
          ).toHaveCount(1)
        })

        await test.step("The edition can't be deleted as long as it has references", async () => {
          await navigateToPeriodicalEvaluation(page)

          await fillInput(page, "Periodikum", "MMG")
          await page
            .getByText("MMG | Medizin Mensch Gesellschaft", { exact: true })
            .click()

          const line = page.getByText(
            (edition.name || "") + "MMG" + "3" + formattedDate,
          )

          await expect(line).toBeVisible()
          // delete button should not be clickable
          await expect(
            line.locator("[aria-label='Ausgabe löschen']"),
          ).toBeHidden()

          await expect(
            line
              .locator("[aria-label='Ausgabe kann nicht gelöscht werden']")
              .first(),
          ).toBeVisible()
        })

        await test.step("A reference can be deleted", async () => {
          await navigateToPeriodicalReferences(page, edition.id || "")

          while (await page.getByTestId("list-entry-0").isVisible()) {
            await page.getByTestId("list-entry-0").click()
            const saveRequest = page.waitForResponse(
              "**/api/v1/caselaw/legalperiodicaledition",
              { timeout: 5_000 },
            )
            await page.locator("[aria-label='Eintrag löschen']").click()
            await saveRequest
          }

          await page.getByTestId("list-entry-0").isHidden()
          await expect(
            page.locator("[aria-label='Eintrag löschen']"),
          ).toBeHidden()
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(1)
          await expect(
            page.getByText(`MMG 2021, 2${suffix} (L)`, { exact: true }),
          ).toBeHidden()

          await page.reload()
          await page.getByTestId("list-entry-0").isHidden()
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(1)
          await expect(
            page.getByText(`MMG 2021, 2${suffix} (L)`, { exact: true }),
          ).toBeHidden()
        })

        await test.step("Deleted citations disappear from the documentation unit's preview ", async () => {
          await previewTab.reload()
          await expect(previewTab.getByText(fileNumber)).toBeVisible()
          await expect(
            previewTab.getByText(`MMG 2021, 2${suffix} (L)`, { exact: true }),
          ).toBeHidden()
          await expect(
            previewTab.getByText("Fundstellen", { exact: true }),
          ).toBeHidden()

          await secondPreviewTab.reload()
          await expect(
            secondPreviewTab.getByText(secondFileNumber),
          ).toBeVisible()
          await expect(
            previewTab.getByText(`MMG 2021, 2${suffix} (L)`, { exact: true }),
          ).toBeHidden()
          await expect(
            previewTab.getByText(`MMG 2024, 99${suffix} (LT)`, { exact: true }),
          ).toBeHidden()
        })
      },
    )

    // We can't test this yet, because we can't create published docUnit via NeuRIS UI
    // eslint-disable-next-line playwright/no-skipped-test
    test.skip(
      "Adding references for other docoffices' documentation units",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4673",
        },
      },
      async ({ context, page, edition, prefilledDocumentUnitBgh }) => {
        const fileNumber =
          prefilledDocumentUnitBgh.coreData.fileNumbers?.[0] || ""

        await test.step("A docunit of another docoffice can be added as reference", async () => {
          await navigateToPeriodicalReferences(page, edition.id || "")

          await fillInput(page, "Zitatstelle *", "12")
          await fillInput(page, "Klammernzusatz", "L")

          await searchForDocUnitWithFileNumber(page, fileNumber, "31.12.2019")
          await expect(
            page.getByText(
              `AG Aachen, 31.12.2019, ${fileNumber}, Anerkenntnisurteil, Veröffentlicht`,
            ),
          ).toBeVisible()
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(1)
          await page.getByLabel("Treffer übernehmen").click()
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(2)
        })

        // open documentation unit preview in new tab
        const previewTab = await context.newPage()
        await navigateToPreview(
          previewTab,
          prefilledDocumentUnitBgh.documentNumber || "",
        )

        await test.step("An added citation is visible in the other docoffice's documentation unit's preview ", async () => {
          await expect(
            previewTab.getByText(`MMG 2024, 12${edition.suffix} (L)`, {
              exact: true,
            }),
          ).toBeVisible()
        })

        await test.step("Changes to the citation are visible in the other docoffice's documentation unit's preview ", async () => {
          await page.getByTestId("list-entry-0").click()
          await expect(page.getByLabel("Zitatstelle *")).toHaveValue(
            "2022, 11, Heft 3",
          )
          await expect(page.getByLabel("Klammernzusatz")).toHaveValue("LT")
          await page.getByLabel("Fundstelle vermerken").click()

          await previewTab.reload()

          await expect(
            previewTab.getByText("MMG 2022, 11, Heft 3 (LT)", { exact: true }),
          ).toBeVisible()
        })

        await test.step("Ensure reference cant be saved with empty Klammernzusatz (referenceSupplement)", async () => {
          await page.getByTestId("list-entry-0").click()
          await expect(page.getByLabel("Zitatstelle *")).toHaveValue(
            "2022, 11, Heft 3",
          )
          await fillInput(page, "Klammernzusatz", "")
          await page.getByLabel("Fundstelle vermerken").click()
          await expect(
            page.getByText("Pflichtfeld nicht befüllt"),
            "Empty Klammernzusatz is not allowed",
          ).toHaveCount(1)
        })

        await test.step("Deleted citations disappear from the other docoffice's documentation unit's preview ", async () => {
          await page.getByTestId("list-entry-0").click()
          await page.locator("[aria-label='Eintrag löschen']").click()
          await previewTab.reload()
          await expect(
            previewTab.getByText("MMG 2022, 11, Heft 3 (LT)", { exact: true }),
          ).toBeHidden()
          await expect(previewTab.getByText("Fundstellen")).toBeHidden()
        })
      },
    )

    async function openSidePanelPreview(page: Page, fileNumber: string) {
      await page.getByTestId(`document-number-link-${fileNumber}`).click()
      await expect(page).toHaveURL(/showAttachmentPanel=true/)
      await page.getByLabel("Vorschau anzeigen").click()
    }

    async function searchForDocUnitWithFileNumber(
      page: Page,
      fileNumber: string,
      date: string,
    ) {
      await fillInput(page, "Gericht", "AG Aachen")
      await page.getByText("AG Aachen", { exact: true }).click()
      await fillInput(page, "Aktenzeichen", fileNumber)
      await fillInput(page, "Entscheidungsdatum", date)
      await fillInput(page, "Dokumenttyp", "AnU")
      await page.getByText("Anerkenntnisurteil", { exact: true }).click()

      await page.getByText("Suchen").click()
    }
  },
)
