import { expect, Page } from "@playwright/test"
import dayjs from "dayjs"
import {
  deleteDocumentUnit,
  fillInput,
  navigateToPeriodicalReferences,
  navigateToPreview,
  navigateToReferences,
  navigateToSearch,
  save,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import { generateString } from "~/test-helper/dataGenerators"

const formattedDate = dayjs().format("DD.MM.YYYY")

async function verifyDocUnitOpensInNewTab(
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
  await expect(newTab.getByTestId("chip-value")).toHaveText(randomFileNumber)
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

async function verifyDocUnitCanBeTakenOver(
  pageWithBghUser: Page,
  documentNumber: string,
  edition: LegalPeriodicalEdition,
) {
  await navigateToSearch(pageWithBghUser)

  await pageWithBghUser.getByLabel("Dokumentnummer Suche").fill(documentNumber)

  const select = pageWithBghUser.locator(`select[id="status"]`)
  await select.selectOption("Fremdanlage")
  await pageWithBghUser
    .getByLabel("Nach Dokumentationseinheiten suchen")
    .click()
  const listEntry = pageWithBghUser.getByRole("row")
  await expect(listEntry).toHaveCount(1)
  await expect(listEntry).toContainText(
    `Fremdanlage aus MMG ${edition.prefix}12${edition.suffix} (DS)`,
  )
  await expect(
    pageWithBghUser.getByLabel("Dokumentationseinheit übernehmen"),
  ).toBeVisible()

  await expect(
    pageWithBghUser.getByText("Dokumentationseinheit bearbeiten"),
  ).toBeHidden()

  await expect(
    pageWithBghUser.getByLabel("Dokumentationseinheit löschen"),
  ).toBeVisible()
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
            page.getByLabel("Zuständige Dokumentationsstelle"),
          ).toHaveValue("BGH")

          // Verwaltungsgericht Aarau is a BVerwG court
          await fillInput(page, "Gericht", "Verwaltungsgericht Aarau")
          await page.getByText("Verwaltungsgericht Aarau").click()
          await page.getByText("Suchen").click()
          await expect(
            page.getByLabel("Zuständige Dokumentationsstelle"),
          ).toHaveValue("BVerwG")
        })

        await test.step("Deleting court, resets responsible docoffice combobox", async () => {
          await page
            .locator("#documentationUnit")
            .getByLabel("Auswahl zurücksetzen")
            .click()
          await page.keyboard.press("Escape")
          await page.getByText("Suchen").click()
          await expect(page.getByLabel("Gericht")).toHaveValue("")
          await expect(
            page.getByLabel("Zuständige Dokumentationsstelle"),
          ).toHaveValue("")
        })

        await test.step("Foreign courts are not assigned to a responsible doc office", async () => {
          await fillInput(page, "Gericht", "Arbeits- und Sozialgericht Wien")
          await page.getByText("Arbeits- und Sozialgericht Wien").click()

          const requestFinishedPromise = page.waitForEvent("requestfinished")
          await page.getByText("Suchen").click()
          await requestFinishedPromise

          await expect(
            page.getByLabel("Zuständige Dokumentationsstelle"),
          ).toHaveValue("")
        })

        await test.step("Documentation Office is a mandatory field for doc unit creation", async () => {
          await expect(
            page.getByText("Übernehmen und weiter bearbeiten"),
          ).toBeDisabled()
        })

        await test.step("DocOffice can be changed manually", async () => {
          await page.getByTestId("documentation-office-combobox").click()

          await expect(
            page.locator("[aria-label='dropdown-option'] >> nth=6"),
          ).toBeVisible()

          await expect(page.getByText("BAG", { exact: true })).toBeVisible()
          await expect(page.getByText("BFH", { exact: true })).toBeVisible()

          await fillInput(page, "Zuständige Dokumentationsstelle", "bv")
          await expect(
            page.getByLabel("Zuständige Dokumentationsstelle"),
          ).toHaveValue("bv")

          await expect(page.getByText("BAG", { exact: true })).toBeHidden()
          await expect(page.getByText("BFH", { exact: true })).toBeHidden()
          await expect(page.getByText("BVerwG", { exact: true })).toBeVisible()
          await expect(
            page.locator("button").filter({ hasText: "BVerfG" }),
          ).toBeVisible()

          await page.locator("button").filter({ hasText: "BVerfG" }).click()

          await expect(
            page.getByLabel("Zuständige Dokumentationsstelle"),
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
            page.getByLabel("Zuständige Dokumentationsstelle"),
          ).toHaveValue("")
        })

        await test.step("Responsible doc office can be updated manually", async () => {
          await fillInput(page, "Zuständige Dokumentationsstelle", "DS")

          await expect(
            page.getByLabel("Zuständige Dokumentationsstelle"),
          ).toHaveValue("DS")
          await page.getByText("DS", { exact: true }).click()
          await expect(
            page.getByText("Übernehmen und weiter bearbeiten"),
          ).toBeEnabled()
        })

        await test.step("Changing other inputs does not reset the doc office | RISDEV-5946", async () => {
          await fillInput(page, "Aktenzeichen", "some new value")
          await expect(
            page.getByLabel("Zuständige Dokumentationsstelle"),
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
          await expect(newTab.getByTestId("chip-value")).toHaveText(
            "some new value",
          )
          await expect(
            newTab.getByLabel("Dokumenttyp", { exact: true }),
          ).toHaveValue("Anerkenntnisurteil")

          await expect(
            newTab.locator("[aria-label='Rechtskraft']"),
          ).toHaveValue("Keine Angabe")
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
          await page.locator("[aria-label='Eintrag löschen']").click()

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
              pageWithBghUser.getByLabel("Zuständige Dokumentationsstelle"),
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
            documentNumber = await verifyDocUnitOpensInNewTab(
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
            const listEntry = newTab.getByRole("row")
            await expect(listEntry).toHaveCount(1)

            await expect(listEntry).toContainText(documentNumber)
            await expect(listEntry).toContainText("Unveröffentlicht")
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
      async ({ page, pageWithBghUser, edition }) => {
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
            page.getByLabel("Zuständige Dokumentationsstelle"),
          ).toHaveValue("BGH")
        })

        const pagePromise = page.context().waitForEvent("page")
        await page.getByText("Übernehmen und weiter bearbeiten").click()
        const newTab = await pagePromise

        await test.step("Created documentation unit for foreign docoffice is editable for creating docoffice", async () => {
          documentNumber = await verifyDocUnitOpensInNewTab(
            newTab,
            randomFileNumber,
          )

          await newTab.keyboard.down("v")

          await expect(newTab.getByText("Sekundäre Fundstellen")).toBeVisible()
          await expect(
            newTab.getByText(
              `${edition.legalPeriodical?.abbreviation} ${edition.prefix}12${edition.suffix} (L)`,
              { exact: true },
            ),
          ).toBeVisible()
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

        await test.step("Created documentation unit is not visible to creating doc office in search with Fremdanlage status", async () => {
          await navigateToSearch(newTab)

          await newTab.getByLabel("Dokumentnummer Suche").fill(documentNumber)

          const select = newTab.locator(`select[id="status"]`)
          await select.selectOption("Fremdanlage")
          await newTab.getByLabel("Nach Dokumentationseinheiten suchen").click()
          const listEntry = newTab.getByRole("row")
          await expect(listEntry).toHaveCount(0)
        })

        await test.step("Created documentation unit is visible with 'Übernehmen' button to foreign doc office in search with Fremdanlage status", async () => {
          await verifyDocUnitCanBeTakenOver(
            pageWithBghUser,
            documentNumber,
            edition,
          )
        })

        await test.step("Created documentation unit's source is automatically set to 'Z'", async () => {
          const pagePromise = pageWithBghUser.context().waitForEvent("page")
          await pageWithBghUser
            .getByLabel("Dokumentationseinheit ansehen")
            .click()
          const newTab = await pagePromise
          await expect(newTab).toHaveURL(
            /\/caselaw\/documentunit\/[A-Z0-9]{13}\/preview$/,
          )
          await expect(newTab.getByText("Quelle")).toBeVisible()
          await expect(newTab.getByText("Z", { exact: true })).toBeVisible()
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

          await navigateToPreview(page, documentNumber, "documentunit", {
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

    test(
      "Takeover and deletion of created documentation unit from foreign docoffice",
      {
        tag: ["@RISDEV-4833"],
      },
      async ({ page, pageWithBghUser, edition }) => {
        await navigateToPeriodicalReferences(page, edition.id ?? "")
        let documentNumber1 = ""
        let documentNumber2 = ""

        await expect(page.getByLabel("Zitatstelle Präfix")).toHaveValue(
          edition.prefix!,
        )
        await expect(page.getByLabel("Zitatstelle Suffix")).toHaveValue(
          edition.suffix!,
        )
        await test.step("Creating docoffice creates a documentunit for owning docoffice and has edit rights", async () => {
          await fillInput(page, "Zitatstelle *", "12")
          await expect(page.getByLabel("Zitatstelle *")).toHaveValue("12")
          await fillInput(page, "Klammernzusatz", "L")
          await searchForDocUnit(
            page,
            "AG Aachen",
            formattedDate,
            generateString(),
            "AnU",
          )

          await expect(
            page.getByLabel("Zuständige Dokumentationsstelle"),
          ).toHaveValue("BGH")
        })

        await test.step("Creating docoffice has access to created docunit", async () => {
          const pagePromise = page.context().waitForEvent("page")
          await page.getByText("Übernehmen und weiter bearbeiten").click()
          const newTab = await pagePromise
          await expect(newTab).toHaveURL(
            /\/caselaw\/documentunit\/[A-Z0-9]{13}\/categories$/,
          )
          documentNumber1 = /caselaw\/documentunit\/(.*)\/categories/g.exec(
            newTab.url(),
          )?.[1] as string
          await expect(page.getByLabel("Listen Eintrag")).toHaveCount(2)
        })

        await test.step("Creating docoffice creates a second documentunit for owning docoffice", async () => {
          await expect(page.getByLabel("Zitatstelle *")).toBeVisible()

          await fillInput(page, "Zitatstelle *", "12")
          await fillInput(page, "Klammernzusatz", "L")

          await searchForDocUnit(
            page,
            "AG Aachen",
            formattedDate,
            generateString(),
            "AnU",
          )

          await expect(
            page.getByLabel("Zuständige Dokumentationsstelle"),
          ).toHaveValue("BGH")
          await page.getByText("Übernehmen", { exact: true }).click()

          const listItems = await page.getByLabel("Listen Eintrag").all()

          //get the document number of the second
          const dataTestId = await listItems[1]
            .locator('[data-testid^="document-number-link-"]')
            .getAttribute("data-testid")

          const documentNumberMatch = dataTestId?.match(
            /document-number-link-(\w+)/,
          )
          // eslint-disable-next-line playwright/no-conditional-in-test
          if (documentNumberMatch) {
            documentNumber2 = documentNumberMatch[1]
          }
          await expect(page.getByLabel("Listen Eintrag")).toHaveCount(3)
        })

        await test.step("Owning docoffice user can preview a created docunit from periodical evaluation", async () => {
          await navigateToSearch(pageWithBghUser)

          await pageWithBghUser
            .getByLabel("Dokumentnummer Suche")
            .fill(documentNumber1)

          const select = pageWithBghUser.locator(`select[id="status"]`)
          await select.selectOption("Fremdanlage")
          await pageWithBghUser
            .getByLabel("Nach Dokumentationseinheiten suchen")
            .click()
          const listEntry = pageWithBghUser.getByRole("row")
          await expect(listEntry).toHaveCount(1)

          await expect(
            pageWithBghUser.getByLabel("Dokumentationseinheit ansehen"),
          ).toBeVisible()
          const pagePromise = pageWithBghUser.context().waitForEvent("page")
          await pageWithBghUser
            .getByLabel("Dokumentationseinheit ansehen")
            .click()

          const newTab = await pagePromise
          await expect(newTab).toHaveURL(
            /\/caselaw\/documentunit\/[A-Z0-9]{13}\/preview$/,
          )
        })

        await test.step("Owning docoffice can accept a created docunit from periodical evaluation, which changes the status to unpublished", async () => {
          await verifyDocUnitCanBeTakenOver(
            pageWithBghUser,
            documentNumber1,
            edition,
          )
          await pageWithBghUser
            .getByLabel("Dokumentationseinheit übernehmen")
            .click()

          await expect(
            pageWithBghUser.getByLabel("Dokumentationseinheit übernehmen"),
          ).toBeHidden()
          await expect(
            pageWithBghUser.getByLabel("Dokumentationseinheit bearbeiten"),
          ).toBeVisible()
          await expect(pageWithBghUser.getByRole("row")).toContainText(
            `Unveröffentlicht`,
          )
        })

        await test.step("Owning docoffice can delete a created docunit from periodical evaluation, which also deletes the reference in the edition", async () => {
          await verifyDocUnitCanBeTakenOver(
            pageWithBghUser,
            documentNumber2,
            edition,
          )
          await pageWithBghUser
            .getByLabel("Dokumentationseinheit löschen")
            .click()

          await pageWithBghUser.locator('button:has-text("Löschen")').click()

          await expect(pageWithBghUser.getByRole("row")).toHaveCount(0)

          await page.reload()
          await expect(page.getByText(documentNumber2)).toBeHidden()
          await expect(page.getByLabel("Listen Eintrag")).toHaveCount(1)
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
        await fillInput(page, "Entscheidungsdatum", date)
      }
      if (documentType) {
        await fillInput(page, "Dokumenttyp", documentType)
        await page.getByText("Anerkenntnisurteil", { exact: true }).click()
      }

      await page.getByText("Suchen").click()
    }
  },
)
