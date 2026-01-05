import { expect, Page } from "@playwright/test"
import dayjs from "dayjs"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { deleteDocumentUnit } from "~/e2e/caselaw/utils/documentation-unit-api-util"
import {
  fillInput,
  navigateToPeriodicalReferences,
  navigateToPreview,
  navigateToReferences,
  navigateToSearch,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"
import { generateString } from "~/test-helper/dataGenerators"

const formattedDate = dayjs().format("DD.MM.YYYY")

async function verifyDocUnitOpensInEditMode(
  newTab: Page,
  randomFileNumber: string,
) {
  await expect(newTab).toHaveURL(
    /\/caselaw\/documentunit\/[A-Z0-9]{13}\/categories$/,
  )
  const documentNumber = /caselaw\/documentunit\/(.*)\/categories/g.exec(
    newTab.url(),
  )?.[1] as string
  await expect(newTab.getByLabel("Gericht", { exact: true })).toHaveValue(
    "AG Aachen",
  )
  await expect(
    newTab.getByLabel("Entscheidungsdatum", { exact: true }),
  ).toHaveValue(formattedDate)
  await expect(
    newTab.getByLabel("Aktenzeichen").getByRole("listitem"),
  ).toHaveText(randomFileNumber)
  await expect(newTab.getByLabel("Dokumenttyp", { exact: true })).toHaveValue(
    "Anerkenntnisurteil",
  )

  // Can be edited and saved after creation
  await newTab
    .getByLabel("Entscheidungsdatum", { exact: true })
    .fill("01.01.2021")
  await save(newTab)
  return documentNumber
}

test.describe(
  "Creation of new documentation units from periodical evaluation",
  {
    tag: ["@RISDEV-4562"],
    annotation: {
      type: "epic",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-4562",
    },
  },
  () => {
    test(
      "Documentation office is automatically selected based on court",
      {
        tag: ["@RISDEV-4831"],
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4829",
        },
      },
      async ({ page, edition }) => {
        await navigateToPeriodicalReferences(page, edition.id ?? "")

        await test.step("After searching, a documentation unit can be created", async () => {
          await expect(
            page.getByText("Dokumentationsstelle zuweisen *"),
          ).toBeHidden()
          await fillInput(page, "Gericht", "BGH")
          await page.getByText("BGH").click()
          await page.getByText("Suchen").click()
          await expect(
            page.getByText("Dokumentationsstelle zuweisen *"),
          ).toBeVisible()
        })

        await fillInput(page, "Zitatstelle *", "12")
        await fillInput(page, "Klammernzusatz", "L")

        await test.step("DocOffice is automatically selected based on court", async () => {
          // AG Aachen is a BGH court
          await fillInput(page, "Gericht", "AG Aachen")
          await page.getByText("AG Aachen").click()
          await page.getByLabel("Nach Entscheidung suchen").click()
          await expect(
            page.getByLabel("Dokumentationsstelle auswählen"),
          ).toHaveValue("BGH")

          // Verwaltungsgericht Aarau is a BVerwG court
          await fillInput(page, "Gericht", "Verwaltungsgericht Aarau")
          await page.getByText("Verwaltungsgericht Aarau").click()
          await page.getByText("Suchen").click()
          await expect(
            page.getByLabel("Dokumentationsstelle auswählen"),
          ).toHaveValue("BVerwG")
        })

        await test.step("Deleting court, resets responsible docoffice combobox", async () => {
          await page
            .locator("#documentationUnit")
            .getByLabel("Entfernen")
            .click()
          await page.keyboard.press("Escape")

          const requestPromise = page.waitForRequest((request) =>
            request
              .url()
              .includes(
                "api/v1/caselaw/documentunits/search-linkable-documentation-units",
              ),
          )
          await page.getByText("Suchen").click()
          await requestPromise

          await expect(page.getByLabel("Gericht")).toHaveValue("")
          await expect(
            page.getByLabel("Dokumentationsstelle auswählen"),
          ).toHaveValue("")
        })

        await test.step("Foreign courts are not assigned to a responsible doc office", async () => {
          await fillInput(page, "Gericht", "Arbeits- und Sozialgericht Wien")
          await page.getByText("Arbeits- und Sozialgericht Wien").click()

          const requestFinishedPromise = page.waitForEvent("requestfinished")
          await page.getByText("Suchen").click()
          await requestFinishedPromise

          await expect(
            page.getByLabel("Dokumentationsstelle auswählen"),
          ).toHaveValue("")
        })

        await test.step("DocOffice can be changed manually", async () => {
          await page.getByTestId("documentation-office-combobox").click()

          await expect(page.getByRole("option").nth(6)).toBeVisible()

          await expect(page.getByText("BAG", { exact: true })).toBeVisible()
          await expect(page.getByText("BFH", { exact: true })).toBeVisible()

          await fillInput(page, "Dokumentationsstelle auswählen", "bv")
          await expect(
            page.getByLabel("Dokumentationsstelle auswählen"),
          ).toHaveValue("bv")

          await expect(page.getByText("BAG", { exact: true })).toBeHidden()
          await expect(page.getByText("BFH", { exact: true })).toBeHidden()
          await expect(page.getByText("BVerwG", { exact: true })).toBeVisible()
          await expect(
            page.locator("button").filter({ hasText: "BVerfG" }),
          ).toBeVisible()

          await page.locator("button").filter({ hasText: "BVerfG" }).click()

          await expect(
            page.getByLabel("Dokumentationsstelle auswählen"),
          ).toHaveValue("BVerfG")

          await expect(
            page.getByText("Übernehmen und weiter bearbeiten"),
          ).toBeEnabled()
        })
      },
    )
    test(
      "Docoffice is empty and has to be set manually when new decision has empty court",
      {
        tag: ["@RISDEV-4999", "@RISDEV-4853", "@RISDEV-5898"],
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4999, https://digitalservicebund.atlassian.net/browse/RISDEV-4853, https://digitalservicebund.atlassian.net/browse/RISDEV-5898",
        },
      },
      async ({ page, edition }) => {
        await navigateToPeriodicalReferences(page, edition.id ?? "")
        const randomFileNumber = generateString()
        let documentNumber = ""

        await test.step("After searching, the responsible docoffice is evaluated and a documentation unit can be created", async () => {
          await searchForDocUnit(
            page,
            undefined,
            formattedDate,
            randomFileNumber,
            "AnU",
          )

          await expect(
            page.getByText("Übernehmen und weiter bearbeiten"),
          ).toBeVisible()

          await expect(
            page.getByLabel("Dokumentationsstelle auswählen"),
          ).toHaveValue("")
        })

        await test.step("Responsible doc office can be updated manually", async () => {
          await fillInput(page, "Dokumentationsstelle auswählen", "DS")

          await expect(
            page.getByLabel("Dokumentationsstelle auswählen"),
          ).toHaveValue("DS")
          await page.getByText("DS", { exact: true }).click()
          await expect(
            page.getByText("Übernehmen und weiter bearbeiten"),
          ).toBeEnabled()
        })

        await test.step("Changing other inputs does not reset the doc office | RISDEV-5946", async () => {
          await fillInput(page, "Aktenzeichen", "some new value")
          await expect(
            page.getByLabel("Dokumentationsstelle auswählen"),
          ).toHaveValue("DS")
        })

        await fillInput(page, "Zitatstelle *", "12")
        await fillInput(page, "Klammernzusatz", "L")

        await test.step("Create docunit, Rechtskraft is set to unknown, as no court was given", async () => {
          const pagePromise = page.context().waitForEvent("page")
          await page.getByText("Übernehmen und weiter bearbeiten").click()
          const newTab = await pagePromise
          await expect(newTab).toHaveURL(
            /\/caselaw\/documentunit\/[A-Z0-9]{13}\/categories$/,
          )
          documentNumber = /caselaw\/documentunit\/(.*)\/categories/g.exec(
            newTab.url(),
          )?.[1] as string

          await expect(
            newTab.getByLabel("Gericht", { exact: true }),
          ).toHaveValue("")
          await expect(
            newTab.getByLabel("Entscheidungsdatum", { exact: true }),
          ).toHaveValue(formattedDate)
          await expect(
            newTab.getByLabel("Aktenzeichen").getByRole("listitem"),
          ).toHaveText("some new value")
          await expect(
            newTab.getByLabel("Dokumenttyp", { exact: true }),
          ).toHaveValue("Anerkenntnisurteil")

          await expect(
            newTab.getByLabel("Rechtskraft", { exact: true }),
          ).toHaveText("Keine Angabe")
        })

        // this test has nothing to do with the other test steps
        // it belongs to RISDEV-5898 and was added here to optimize test execution
        await test.step("Source references can be deleted", async () => {
          await navigateToReferences(page, documentNumber)

          const referenceSummary = `${edition.legalPeriodical?.abbreviation} ${edition.prefix}12${edition.suffix} (L)`

          await expect(
            page.getByText(referenceSummary, {
              exact: true,
            }),
          ).toBeVisible()

          await page.getByTestId("list-entry-0").first().click()
          await page.getByLabel("Eintrag löschen", { exact: true }).click()

          await expect(
            page.getByText(referenceSummary, {
              exact: true,
            }),
          ).toBeHidden()

          await save(page)
          await page.reload()

          //only reference input list item is shown (input is a list entry in editable list)
          await expect(
            page
              .getByTestId("caselaw-reference-list")
              .getByLabel("Listen Eintrag"),
          ).toHaveCount(1)

          await navigateToPeriodicalReferences(page, edition.id ?? "")

          await expect(page.getByLabel("Listen Eintrag")).toHaveCount(1)
          await expect(page.getByText(documentNumber)).toBeHidden()
        })

        await deleteDocumentUnit(page, documentNumber)
      },
    )
    ;[
      {
        type: "Rechtsprechung",
        previewLabel: "Sekundäre Fundstellen",
        mandatoryFields: 2,
      },
      {
        type: "Literatur",
        previewLabel: "Literaturfundstellen",
        mandatoryFields: 3,
      },
    ].forEach(({ type, previewLabel, mandatoryFields }) => {
      test(
        `Allow creation from periodical evaluation of type ${type} for own docoffice`,
        {
          tag: ["@RISDEV-4829", "@RISDEV-5146"],
          annotation: {
            type: "story",
            description:
              "https://digitalservicebund.atlassian.net/browse/RISDEV-4829",
          },
        },
        async ({ pageWithBghUser, edition }) => {
          await navigateToPeriodicalReferences(
            pageWithBghUser,
            edition.id ?? "",
          )
          const randomFileNumber = generateString()
          let documentNumber = ""

          await pageWithBghUser
            .getByLabel(type + " Fundstelle")
            .setChecked(true)

          await test.step("After searching, the responsible docoffice is evaluated and a documentation unit can be created", async () => {
            await searchForDocUnit(
              pageWithBghUser,
              "AG Aachen",
              formattedDate,
              randomFileNumber,
              "AnU",
            )

            await expect(
              pageWithBghUser.getByText("Übernehmen und weiter bearbeiten"),
            ).toBeVisible()

            await expect(
              pageWithBghUser.getByLabel("Dokumentationsstelle auswählen"),
            ).toHaveValue("BGH")
          })

          await test.step("Validation of required fields before creation new documentation unit from search parameters", async () => {
            await pageWithBghUser
              .getByText("Übernehmen und weiter bearbeiten")
              .click()

            await expect(
              pageWithBghUser.getByLabel("Listen Eintrag"),
            ).toHaveCount(1)

            await expect(
              pageWithBghUser.getByText("Pflichtfeld nicht befüllt"),
            ).toHaveCount(mandatoryFields)

            await fillInput(pageWithBghUser, "Zitatstelle *", "12")

            await expect(
              pageWithBghUser.getByText("Pflichtfeld nicht befüllt"),
            ).toHaveCount(mandatoryFields - 1)

            await pageWithBghUser
              .getByText("Übernehmen und weiter bearbeiten")
              .click()

            await expect(
              pageWithBghUser.getByLabel("Listen Eintrag"),
            ).toHaveCount(1)

            // eslint-disable-next-line playwright/no-conditional-in-test
            if (type === "Rechtsprechung") {
              await fillInput(pageWithBghUser, "Klammernzusatz", "L")
            } else {
              await fillInput(
                pageWithBghUser,
                "Autor Literaturfundstelle",
                "Bob",
              )
              await fillInput(
                pageWithBghUser,
                "Dokumenttyp Literaturfundstelle",
                "Ean",
              )
              await pageWithBghUser.getByText("Ean", { exact: true }).click()
              // eslint-disable-next-line playwright/no-conditional-expect
              await expect(
                pageWithBghUser.getByLabel("Dokumenttyp Literaturfundstelle"),
              ).toHaveValue("Anmerkung")
            }

            await expect(
              pageWithBghUser.getByText("Pflichtfeld nicht befüllt"),
            ).toBeHidden()
          })

          const pagePromise = pageWithBghUser.context().waitForEvent("page")
          await pageWithBghUser
            .getByText("Übernehmen und weiter bearbeiten")
            .click()
          const newTab = await pagePromise

          await test.step("Created documentation unit opens up with in new tab with correct data and reference assigned and is editable", async () => {
            documentNumber = await verifyDocUnitOpensInEditMode(
              newTab,
              randomFileNumber,
            )
          })

          let referenceSummary = `${edition.legalPeriodical?.abbreviation} ${edition.prefix}12${edition.suffix} (L)`
          // eslint-disable-next-line playwright/no-conditional-in-test
          if (type === "Literatur") {
            referenceSummary = `${edition.legalPeriodical?.abbreviation} ${edition.prefix}12${edition.suffix}, Bob (Ean)`
          }

          await test.step("Reference is added to new documentation unit (Fundstelle Tab and preview)", async () => {
            await newTab.keyboard.down("v")

            await expect(newTab.getByText(previewLabel)).toBeVisible()
            await expect(
              newTab.getByText(referenceSummary, { exact: true }),
            ).toBeVisible()

            await newTab.getByLabel("Fundstellen").click()
            await expect(
              newTab
                .getByTestId("preview")
                .getByText(previewLabel, { exact: true }),
            ).toBeVisible()
            await expect(
              newTab
                .getByLabel("Listen Eintrag")
                .getByText(referenceSummary, { exact: true }),
            ).toBeVisible()
          })

          await test.step("The new documentation unit is added to the list of references", async () => {
            await expect(
              pageWithBghUser.getByLabel("Listen Eintrag"),
            ).toHaveCount(2)

            await expect(
              pageWithBghUser.getByText(
                `AG Aachen, ${formattedDate}, ${randomFileNumber}, Anerkenntnisurteil, Unveröffentlicht`,
              ),
            ).toBeVisible()

            await expect(
              pageWithBghUser.getByText(referenceSummary, { exact: true }),
            ).toBeVisible()
          })

          await test.step("The new documentation unit is visible in search with unpublished status", async () => {
            await navigateToSearch(newTab)

            await newTab.getByLabel("Dokumentnummer Suche").fill(documentNumber)
            await newTab
              .getByLabel("Nach Dokumentationseinheiten suchen")
              .click()
            const allRows = newTab.getByRole("row")
            const resultRow = allRows.filter({ hasText: documentNumber })
            await expect(resultRow).toBeVisible()
            await expect(resultRow).toContainText("Unveröffentlicht")
          })

          await test.step("The new documentation unit can be deleted with when deleting reference", async () => {
            await pageWithBghUser.getByTestId("list-entry-0").click()

            await pageWithBghUser.getByText("Eintrag löschen").click()
            await pageWithBghUser
              .locator('button:has-text("Dokumentationseinheit löschen")')
              .click()

            await expect(
              pageWithBghUser.locator(
                'button:has-text("Dokumentationseinheit löschen")',
              ),
            ).toBeHidden()

            await expect(
              pageWithBghUser.getByText(
                `AG Aachen, ${formattedDate}, ${randomFileNumber}, Anerkenntnisurteil, Unveröffentlicht`,
              ),
            ).toBeHidden()

            await expect(
              pageWithBghUser.getByText(referenceSummary, { exact: true }),
            ).toBeHidden()

            await newTab.reload()
            const listEntry = newTab.getByRole("row")
            await expect(listEntry).toHaveCount(0)
          })
        },
      )
    })

    test(
      "Validation of required fields of Rechtsprechungsfundstellen before creation",
      {
        tag: ["@RISDEV-4829"],
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4829",
        },
      },
      async ({ page, edition }) => {
        await navigateToPeriodicalReferences(page, edition.id ?? "")
        await searchForDocUnit(
          page,
          "AG Aachen",
          formattedDate,
          generateString(),
          "AnU",
        )
        await page.getByText("Übernehmen und weiter bearbeiten").click()

        await expect(page.getByLabel("Listen Eintrag")).toHaveCount(1)

        await expect(page.getByText("Pflichtfeld nicht befüllt")).toHaveCount(2)

        await fillInput(page, "Zitatstelle *", "12")
        await expect(page.getByText("Pflichtfeld nicht befüllt")).toHaveCount(1)
        await page.getByText("Übernehmen und weiter bearbeiten").click()

        await expect(page.getByLabel("Listen Eintrag")).toHaveCount(1)

        await fillInput(page, "Klammernzusatz", "L")

        await expect(page.getByText("Pflichtfeld nicht befüllt")).toBeHidden()
      },
    )

    test(
      "Allow creation from periodical evaluation for foreign docoffice",
      {
        tag: ["@RISDEV-4832", "@RISDEV-4980, @RISDEV-6381"],
      },
      async ({ page, edition }) => {
        await navigateToPeriodicalReferences(page, edition.id ?? "")
        const randomFileNumber = generateString()
        let documentNumber = ""

        await test.step("After searching, the responsible docoffice is evaluated and a documentation unit can be created", async () => {
          await fillInput(page, "Zitatstelle *", "12")
          await expect(page.getByLabel("Zitatstelle *")).toHaveValue("12")
          await fillInput(page, "Klammernzusatz", "L")
          await searchForDocUnit(
            page,
            "AG Aachen",
            formattedDate,
            randomFileNumber,
            "AnU",
          )

          await expect(
            page.getByText("Übernehmen und weiter bearbeiten"),
          ).toBeVisible()

          await expect(
            page.getByLabel("Dokumentationsstelle auswählen"),
          ).toHaveValue("BGH")
        })

        const pagePromise = page.context().waitForEvent("page")
        await page.getByText("Übernehmen und weiter bearbeiten").click()
        const newTab = await pagePromise

        await test.step("Created documentation unit for foreign docoffice is editable for creating docoffice", async () => {
          documentNumber = await verifyDocUnitOpensInEditMode(
            newTab,
            randomFileNumber,
          )
        })

        await test.step("Created documentation unit's has reference assigned", async () => {
          await newTab.keyboard.down("v")

          await expect(newTab.getByText("Sekundäre Fundstellen")).toBeVisible()
          await expect(
            newTab.getByText(
              `${edition.legalPeriodical?.abbreviation} ${edition.prefix}12${edition.suffix} (L)`,
              { exact: true },
            ),
          ).toBeVisible()
        })

        await test.step("Created documentation unit's source is automatically set to 'Z'", async () => {
          await expect(
            newTab.getByTestId("preview").getByText("Quelle"),
          ).toBeVisible()
          await expect(newTab.getByText("Z", { exact: true })).toBeVisible()
        })

        await test.step("The new documentation unit is added to the list of references", async () => {
          await expect(page.getByLabel("Listen Eintrag")).toHaveCount(2)

          await expect(
            page.getByText(
              `AG Aachen, ${formattedDate}, ${randomFileNumber}, Anerkenntnisurteil, Fremdanlage`,
            ),
          ).toBeVisible()

          await expect(
            page.getByText(
              `${edition.legalPeriodical?.abbreviation} ${edition.prefix}12${edition.suffix} (L)`,
              { exact: true },
            ),
          ).toBeVisible()
        })

        await test.step("Created DocUnit is deleted when reference is deleted", async () => {
          await page.getByTestId("list-entry-0").click()

          await page
            .getByText("Fundstelle und Dokumentationseinheit löschen")
            .click()

          await expect(
            page.getByText(
              `AG Aachen, ${formattedDate}, ${randomFileNumber}, Anerkenntnisurteil, Unveröffentlicht`,
            ),
          ).toBeHidden()

          await expect(
            page.getByText(
              `${edition.legalPeriodical?.abbreviation} ${edition.prefix}12${edition.suffix} (L)`,
              { exact: true },
            ),
          ).toBeHidden()

          const saveRequest = page.waitForResponse(
            "**/api/v1/caselaw/legalperiodicaledition",
            { timeout: 5_000 },
          )
          await saveRequest

          await navigateToPreview(page, documentNumber, {
            skipAssert: true,
          })

          await expect(
            page.getByText(
              "Diese Dokumentationseinheit existiert nicht oder Sie haben keine Berechtigung.",
            ),
          ).toBeVisible()
        })
      },
    )

    async function searchForDocUnit(
      page: Page,
      court?: string,
      date?: string,
      fileNumber?: string,
      documentType?: string,
    ) {
      if (fileNumber) {
        await fillInput(page, "Aktenzeichen", fileNumber)
      }
      if (court) {
        await fillInput(page, "Gericht", court)
        await page.getByText(court, { exact: true }).click()
      }
      if (date) {
        await fillInput(page, "Datum", date)
      }
      if (documentType) {
        await fillInput(page, "Dokumenttyp", documentType)
        await page.getByText("Anerkenntnisurteil", { exact: true }).click()
      }

      await page.getByText("Suchen").click()
    }
  },
)
