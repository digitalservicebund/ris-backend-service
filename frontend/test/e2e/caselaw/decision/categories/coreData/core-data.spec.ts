import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { deleteDocumentUnit } from "~/e2e/caselaw/utils/documentation-unit-api-util"
import { navigateToCategories, save } from "~/e2e/caselaw/utils/e2e-utils"

test.describe("core data", () => {
  test("core data change", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    const fileNumberInput = page.getByLabel("Aktenzeichen").getByRole("textbox")

    await fileNumberInput.fill("abc")
    await page.keyboard.press("Enter")
    await page.getByLabel("ECLI", { exact: true }).fill("abc123")
    await page.keyboard.press("Enter")

    await save(page)

    await page.reload()
    await expect(
      page.getByLabel("Aktenzeichen").getByRole("listitem"),
    ).toHaveText("abc")
    await expect(page.getByLabel("ECLI", { exact: true })).toHaveValue("abc123")
  })

  test("nested 'ECLI' input toggles child input and correctly saves and displays data", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.getByLabel("ECLI", { exact: true }).type("one")
    await expect(page.getByText("one").first()).toBeVisible()

    await expect(page.getByText("Abweichender ECLI>")).toBeHidden()

    await page.getByLabel("Abweichender ECLI anzeigen", { exact: true }).click()

    await expect(page.getByText("Abweichender ECLI").first()).toBeVisible()

    const deviatingInput = page
      .getByLabel("Abweichender ECLI")
      .getByRole("textbox")
    await deviatingInput.fill("two")
    await page.keyboard.press("Enter")
    await deviatingInput.fill("three")
    await page.keyboard.press("Enter")

    await save(page)

    await page.reload()

    // If deviating data is available, it is automatically expanded
    await expect(page.getByText("two").first()).toBeVisible()
    await expect(page.getByText("three").first()).toBeVisible()

    await page
      .getByLabel("Abweichender ECLI schließen", { exact: true })
      .click()
    await expect(page.getByText("Abweichender ECLI").first()).toBeHidden()
  })

  test("nested fileNumbers input toggles child input and correctly saves and displays data", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const fileNumberInput = page.getByLabel("Aktenzeichen").getByRole("textbox")
    await fileNumberInput.fill("one")
    await page.keyboard.press("Enter")

    await fileNumberInput.fill("two")
    await page.keyboard.press("Enter")

    await expect(page.getByText("one").first()).toBeVisible()
    await expect(page.getByText("two").first()).toBeVisible()

    await expect(page.getByText("Abweichendes Aktenzeichen>")).toBeHidden()

    await page
      .getByLabel("Abweichendes Aktenzeichen anzeigen", { exact: true })
      .click()

    await expect(
      page.getByText("Abweichendes Aktenzeichen").first(),
    ).toBeVisible()

    await page
      .getByLabel("Abweichendes Aktenzeichen")
      .getByRole("textbox")
      .fill("three")
    await page.keyboard.press("Enter")

    await save(page)
    await page.reload()

    // If deviating data is available, it is automatically expanded
    await expect(page.getByText("three").first()).toBeVisible()
    await page
      .getByLabel("Abweichendes Aktenzeichen schließen", { exact: true })
      .click()
    await expect(
      page.getByText("Abweichendes Aktenzeichen").first(),
    ).toBeHidden()
  })

  test("adding, navigating, deleting multiple fileNumbers", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const fileNumberInput = page.getByLabel("Aktenzeichen").getByRole("textbox")

    await fileNumberInput.fill("testone")
    await page.keyboard.press("Enter")

    await fileNumberInput.fill("testtwo")
    await page.keyboard.press("Enter")

    await fileNumberInput.fill("testthree")
    await page.keyboard.press("Enter")

    await expect(page.getByText("testone").first()).toBeVisible()
    await expect(page.getByText("testtwo").first()).toBeVisible()
    await expect(page.getByText("testthree").first()).toBeVisible()

    // Navigate back and delete on enter
    await page.keyboard.press("Shift+Tab")
    await page.keyboard.press("Shift+Tab")
    await page.keyboard.press("Shift+Tab")
    await page.keyboard.press("Enter")

    await expect(page.getByText("testtwo").first()).toBeHidden()

    // Tab out and in
    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")

    await page.keyboard.press("Shift+Tab")
    await page.keyboard.press("Shift+Tab")

    //Edit entry
    await page.keyboard.press("Shift+Tab")
    await page.keyboard.press("Enter")
    await page.keyboard.type("test")
    await page.keyboard.press("Enter")

    await expect(page.getByText("testthreetest").first()).toBeVisible()
    await save(page)

    await page.reload()
    await expect(page.getByText("testone").first()).toBeVisible()
    await expect(page.getByText("testthreetest").first()).toBeVisible()
  })

  test("legal effect dropdown", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    const dropdown = page.getByRole("combobox", { name: "Rechtskraft" })
    await expect(dropdown).toHaveText("Keine Angabe")

    await dropdown.click()

    await expect(page.getByRole("option", { name: "Ja" })).toHaveCount(1)
    await expect(page.getByRole("option", { name: "Nein" })).toHaveCount(1)
    await expect(
      page.getByRole("option", { name: "Keine Angabe" }),
    ).toHaveCount(1)
  })

  test("document type dropdown", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    // on start: closed dropdown, no input text
    await expect(page.getByLabel("Dokumenttyp", { exact: true })).toHaveValue(
      "",
    )
    await expect(page.getByText("AnU - Anerkenntnisurteil")).toBeHidden()
    await expect(
      page.getByLabel("dropdown-option", { exact: true }),
    ).toBeHidden()

    // open dropdown
    await page
      .locator("#coreData div")
      .filter({ hasText: "Spruchkörper Dokumenttyp" })
      .getByLabel("Dropdown öffnen")
      .click()
    await expect(
      page.getByLabel("dropdown-option", { exact: true }),
    ).not.toHaveCount(0)
    await expect(page.getByText("Anerkenntnisurteil")).toBeVisible()
    await expect(page.getByText("EuGH-Vorlage")).toBeVisible()

    // type search string: 3 results for "zwischen"
    await page.getByLabel("Dokumenttyp", { exact: true }).fill("zwischen")
    await expect(page.getByLabel("Dokumenttyp", { exact: true })).toHaveValue(
      "zwischen",
    )
    await expect(
      page.getByLabel("dropdown-option", { exact: true }),
    ).toHaveCount(3)

    // use the clear icon
    await page.getByLabel("Auswahl zurücksetzen", { exact: true }).click()
    await expect(page.getByLabel("Dokumenttyp", { exact: true })).toHaveValue(
      "",
    )
    await expect(
      page.getByLabel("dropdown-option", { exact: true }),
    ).not.toHaveCount(0)
    await expect(page.getByText("Anerkenntnisurteil")).toBeVisible()

    // close dropdown
    await page.getByLabel("Dropdown schließen").click()
    await expect(page.getByLabel("Dokumenttyp", { exact: true })).toHaveValue(
      "",
    )
    await expect(
      page.getByLabel("dropdown-option", { exact: true }),
    ).toBeHidden()

    // open dropdown again by focussing
    await page.getByLabel("Dokumenttyp", { exact: true }).focus()

    // close dropdown using the esc key, user input text gets removed and last saved value restored
    await page.keyboard.down("Escape")
    await expect(
      page.getByLabel("dropdown-option", { exact: true }),
    ).toBeHidden()
    await expect(page.getByLabel("Dokumenttyp", { exact: true })).toHaveValue(
      "",
    )
  })

  // skipped as we don't show the Dokstelle anymore as of RISDEV-4177
  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("ensure new docUnit has correct documentationOffice for DS user", async ({
    page,
  }) => {
    await test.step("create new docUnit with logged in user", async () => {
      await page.goto("/caselaw")
      const tableView = page.getByRole("cell", {
        name: "Dateiname",
        exact: true,
      })

      await page
        .getByRole("button", {
          name: "Neue Entscheidung",
          exact: true,
        })
        .click()

      await expect(tableView).toBeHidden()

      await page.getByText("Rubriken").click()
      await expect(page.getByText("DOKUMENTATIONSSTELLEDS")).toBeVisible()

      const documentNumber = page
        .url()
        .match(/documentunit\/([A-Z0-9]{13})\/files/)![1]

      await deleteDocumentUnit(page, documentNumber)
    })
  })

  // skipped as we don't show the Dokstelle anymore as of RISDEV-4177
  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("ensure new docUnit has correct documentationOffice for BGH user", async ({
    pageWithBghUser,
  }) => {
    await test.step("create new docUnit with logged in user", async () => {
      await pageWithBghUser.goto("/caselaw")
      await pageWithBghUser
        .getByRole("button", {
          name: "Neue Entscheidung",
          exact: true,
        })
        .click()

      const tableView = pageWithBghUser.getByRole("cell", {
        name: "Dateiname",
        exact: true,
      })

      await expect(tableView).toBeHidden()

      await pageWithBghUser.getByText("Rubriken").click()
      await expect(
        pageWithBghUser.getByText("DOKUMENTATIONSSTELLEBGH"),
      ).toBeVisible()

      const documentNumber = pageWithBghUser
        .url()
        .match(/documentunit\/([A-Z0-9]{13})\/files/)![1]

      await deleteDocumentUnit(pageWithBghUser, documentNumber)
    })
  })

  test("ensure that leadingDecisionNormReferences are visible, savable, and deletable for the BGH and not for others", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const nswInput = page.getByLabel("BGH Nachschlagewerk").getByRole("textbox")
    const CITATION = "1968, 249-252 (ST)"
    const nswChipTag = page.getByText(CITATION)
    await expect(nswInput).toBeHidden()

    await page.getByLabel("Gericht", { exact: true }).fill("BGH")
    await page.getByText("BGH").click()
    await expect(nswInput).toBeVisible()
    await nswInput.fill(CITATION)
    await page.keyboard.press("Enter")

    await save(page)
    await expect(
      nswChipTag,
      "NSW Fundstelle is not visible for BGH",
    ).toBeVisible()

    await page.getByLabel(CITATION).getByLabel("Eintrag Löschen").click()
    await save(page)

    await expect(nswChipTag, "Citation was not deleted").toBeHidden()

    await page.getByLabel("Gericht", { exact: true }).fill("AG Aalen")
    await page.getByText("AG Aalen").click()
    await expect(
      nswInput,
      "NSW Fundstelle is visible for other courts",
    ).toBeHidden()
  })

  test(
    "source dropdown",
    {
      tag: ["@RISDEV-6381", "@RISDEV-7139"],
    },
    async ({ page, documentNumber }) => {
      await test.step("source can be selected via dropdown", async () => {
        await navigateToCategories(page, documentNumber)

        const dropdown = page.getByTestId("source-input")
        await expect(dropdown).toHaveText("Bitte auswählen")

        await dropdown.click()

        const expectedOptions = [
          { label: "unaufgefordert eingesandtes Original (O)" },
          { label: "angefordertes Original (A)" },
          { label: "Zeitschriftenveröffentlichung (Z)" },
          { label: "ohne Vorlage des Originals E-Mail (E)" },
          {
            label:
              "Ländergerichte, EuG- und EuGH-Entscheidungen über jDV-Verfahren (L)",
          },
          { label: "Sonstige (S)" },
        ]
        for (const option of expectedOptions) {
          await expect(
            page.getByRole("option", { name: option.label }),
          ).toHaveCount(1)
        }

        await page
          .getByRole("option", {
            name: "Zeitschriftenveröffentlichung (Z)",
          })
          .click()
        await expect(dropdown).toHaveText("Zeitschriftenveröffentlichung (Z)")
      })

      await test.step("a second source can be selected", async () => {
        const dropdown = page.getByTestId("source-input")
        await page
          .getByRole("option", {
            name: "unaufgefordert eingesandtes Original (O)",
          })
          .click()
        await expect(dropdown).toHaveText(
          "Zeitschriftenveröffentlichung (Z)unaufgefordert eingesandtes Original (O)",
        )
      })

      // Todo: display legacy values if not in dropdown options
      // await test.step("source dropdown with legacy data as display value, can not be reselected", async () => {
      //   await navigateToCategories(page, "YYTestDoc0001")

      //   const dropdown = page.getByRole("combobox", { name: "Quelle Input" })
      //   await expect(dropdown).toHaveText("legacy value")

      //   await page
      //     .getByRole("option", {
      //       name: "Zeitschriftenveröffentlichung (Z)",
      //     })
      //     .click()

      //   await expect(dropdown).toHaveText("Zeitschriftenveröffentlichung (Z)")
      // })
    },
  )

  test(
    "core data is hidden for external user",
    {
      annotation: {
        type: "story",
        description:
          "https://digitalservicebund.atlassian.net/browse/RISDEV-4523",
      },
      tag: ["@RISDEV-4523"],
    },
    async ({ pageWithExternalUser, documentNumber }) => {
      await test.step("Navigiere zu Rubriken als external Nutzer", async () => {
        await navigateToCategories(pageWithExternalUser, documentNumber)
      })

      await test.step("Gericht und Fehlerhaftes Gericht sind nicht sichtbar", async () => {
        const court = pageWithExternalUser.getByLabel("Gericht", {
          exact: true,
        })
        const deviatingCourt =
          pageWithExternalUser.getByTestId("deviating-courts")
        await expect(court).toBeHidden()
        await expect(deviatingCourt).toBeHidden()
      })

      await test.step("Aktenzeichen und abweichendes Aktenzeichen sind nicht sichtbar", async () => {
        const fileNumber = pageWithExternalUser.getByTestId("file-numbers")
        const deviatingFileNumber = pageWithExternalUser.getByTestId(
          "deviating-file-numbers",
        )
        await expect(fileNumber).toBeHidden()
        await expect(deviatingFileNumber).toBeHidden()
      })

      await test.step("Entscheidungsdatum und abweichendes Entscheidungsdatum sind nicht sichtbar", async () => {
        const decisionDate = pageWithExternalUser.getByLabel(
          "Entscheidungsdatum",
          { exact: true },
        )
        const deviatingDecisionDate = pageWithExternalUser.getByTestId(
          "deviating-decision-dates",
        )
        await expect(decisionDate).toBeHidden()
        await expect(deviatingDecisionDate).toBeHidden()
      })

      await test.step("Spruchkörper ist nicht sichtbar", async () => {
        const appraisalBody = pageWithExternalUser.getByLabel("Spruchkörper", {
          exact: true,
        })
        await expect(appraisalBody).toBeHidden()
      })

      await test.step("Dokumenttyp ist nicht sichtbar", async () => {
        const documentType = pageWithExternalUser.getByLabel("Dokumenttyp", {
          exact: true,
        })
        await expect(documentType).toBeHidden()
      })

      await test.step("Abweichende Dokumentnummer ist nicht sichtbar", async () => {
        const deviatingDocumentNumber = pageWithExternalUser.getByLabel(
          "Abweichende Dokumentnummer",
          {
            exact: true,
          },
        )
        await expect(deviatingDocumentNumber).toBeHidden()
      })

      await test.step("Celex Nummer ist nicht sichtbar", async () => {
        const celexNumber = pageWithExternalUser.getByLabel("CELEX-Nummer", {
          exact: true,
        })
        await expect(celexNumber).toBeHidden()
      })

      await test.step("ECLI und abweichender ECLI sind nicht sichtbar", async () => {
        const ecli = pageWithExternalUser.getByLabel("ECLI", { exact: true })
        const deviatingEcli =
          pageWithExternalUser.getByTestId("deviating-eclis")
        await expect(ecli).toBeHidden()
        await expect(deviatingEcli).toBeHidden()
      })

      await test.step("Vorgang ist nicht sichtbar", async () => {
        const procedure = pageWithExternalUser.getByLabel("Vorgang", {
          exact: true,
        })
        const previousProcedures = pageWithExternalUser.getByTestId(
          "previous-procedures",
        )
        await expect(procedure).toBeHidden()
        await expect(previousProcedures).toBeHidden()
      })

      await test.step("Rechtskraft ist nicht sichtbar", async () => {
        const legalEffect = pageWithExternalUser.getByLabel("Rechtskraft", {
          exact: true,
        })
        await expect(legalEffect).toBeHidden()
      })

      await test.step("Streitjahr ist nicht sichtbar", async () => {
        const yearsOfDispute =
          pageWithExternalUser.getByTestId("year-of-dispute")
        await expect(yearsOfDispute).toBeHidden()
      })

      await test.step("Quelle ist nicht sichtbar", async () => {
        const source = pageWithExternalUser.getByTestId("source-input")
        await expect(source).toBeHidden()
      })

      await test.step("Eingangsart ist nicht sichtbar", async () => {
        const inputTypes = pageWithExternalUser.getByTestId("input-types")
        await expect(inputTypes).toBeHidden()
      })

      await test.step("Gerichtsbarkeit ist nicht sichtbar", async () => {
        const jurisdictionType =
          pageWithExternalUser.getByTestId("jurisdiction-type")
        await expect(jurisdictionType).toBeHidden()
      })

      await test.step("Region ist nicht sichtbar", async () => {
        const region = pageWithExternalUser.getByLabel("Region", {
          exact: true,
        })
        await expect(region).toBeHidden()
      })
    },
  )

  test(
    "core data is editable for internal user",
    {
      annotation: {
        type: "story",
        description:
          "https://digitalservicebund.atlassian.net/browse/RISDEV-4523",
      },
      tag: ["@RISDEV-4523"],
    },
    async ({ page, documentNumber }) => {
      await test.step("Navigiere zu Rubriken als interner Nutzer", async () => {
        await navigateToCategories(page, documentNumber)
      })

      await test.step("Gericht und Fehlerhaftes Gericht sind bearbeitbar", async () => {
        const court = page.getByLabel("Gericht", { exact: true })
        const deviatingCourt = page.locator("#deviatingCourts")
        await expect(court).toBeEditable()
        await expect(deviatingCourt).toBeEditable()
      })

      await test.step("Aktenzeichen und abweichendes Aktenzeichen sind bearbeitbar", async () => {
        const fileNumber = page.locator("#fileNumberInput")
        const deviatingFileNumber = page.locator("#deviatingFileNumbers")
        await expect(fileNumber).toBeEditable()
        await expect(deviatingFileNumber).toBeEditable()
      })

      await test.step("Entscheidungsdatum und abweichendes Entscheidungsdatum sind bearbeitbar", async () => {
        const decisionDate = page.getByLabel("Entscheidungsdatum", {
          exact: true,
        })
        const deviatingDecisionDate = page.locator("#deviatingDecisionDates")
        const oralHearingDate = page.locator("#oralHearingDates")
        await expect(decisionDate).toBeEditable()
        await expect(deviatingDecisionDate).toBeEditable()
        await expect(oralHearingDate).toBeEditable()
      })

      await test.step("Spruchkörper ist bearbeitbar", async () => {
        const appraisalBody = page.getByLabel("Spruchkörper", { exact: true })
        await expect(appraisalBody).toBeEditable()
      })

      await test.step("Dokumenttyp ist bearbeitbar", async () => {
        const documentType = page.getByLabel("Dokumenttyp", { exact: true })
        await expect(documentType).toBeEditable()
      })

      await test.step("Abweichende Dokumentnummer ist bearbeitbar", async () => {
        const deviatingDocumentNumber = page.locator(
          "#deviatingDocumentNumbers",
        )
        await expect(deviatingDocumentNumber).toBeEditable()
      })

      await test.step("Celex Nummer ist bearbeitbar", async () => {
        const court = page.getByLabel("Gericht", { exact: true })
        await expect(court).toHaveValue("")
        await court.fill("EuGH")
        await expect(page.getByTestId("combobox-spinner")).toBeHidden()
        await expect(court).toHaveValue("EuGH")
        await page.getByText("EuGH", { exact: true }).click()
        await expect(court).toHaveValue("EuGH")
        const celexNumber = page.getByLabel("Celex-Nummer", { exact: true })
        await expect(celexNumber).toBeEditable()
      })

      await test.step("ECLI und abweichender ECLI sind bearbeitbar", async () => {
        const ecli = page.getByLabel("ECLI", { exact: true })
        const deviatingEcli = page.locator("#deviatingEclis")
        await expect(ecli).toBeEditable()
        await expect(deviatingEcli).toBeEditable()
      })

      await test.step("Vorgang ist bearbeitbar", async () => {
        const procedure = page.getByLabel("Vorgang", { exact: true })
        const previousProcedures = page.locator("#previousProcedures")
        await expect(procedure).toBeEditable()
        await expect(previousProcedures).toBeEditable()
      })

      await test.step("Rechtskraft ist bearbeitbar", async () => {
        const legalEffect = page.getByLabel("Rechtskraft", { exact: true })
        await expect(legalEffect).toBeEnabled()
      })

      await test.step("Streitjahr ist bearbeitbar", async () => {
        const yearsOfDispute = page.locator("#yearsOfDispute")
        await expect(yearsOfDispute).toBeEditable()
      })

      await test.step("Eingangsart ist bearbeitbar", async () => {
        const inputTypes = page.locator("#inputTypes")
        await expect(inputTypes).toBeEditable()
      })

      await test.step("Region ist readonly", async () => {
        const region = page.getByLabel("Region", { exact: true })
        await expect(region).not.toBeEditable()
      })
    },
  )

  test(
    "updates jurisdiction type on court selection",
    {
      tag: ["@RISDEV-6379"],
    },
    async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)
      const jurisidictionType = page.getByLabel("Gerichtsbarkeit", {
        exact: true,
      })

      await test.step("Gerichtsbarkeit ist initial nicht ausgefüllt", async () => {
        await expect(jurisidictionType).toHaveValue("")
      })

      await test.step("Auswahl eines Gerichts mit Gerichtsbarkeit - Gerichtsbarkeit wird angezeigt", async () => {
        await page.getByLabel("Gericht", { exact: true }).fill("AG Aalen")
        await page.getByText("AG Aalen").click()
        await expect(jurisidictionType).toHaveValue(
          "Ordentliche Gerichtsbarkeit",
        )
      })

      await test.step("Auswahl eines Gerichts ohne Gerichtsbarkeit - Gerichtsbarkeit wird nicht angezeigt", async () => {
        await page.getByLabel("Gericht", { exact: true }).fill("GStA")
        await page.getByText("GStA Berlin").click()
        await expect(jurisidictionType).toHaveValue("")
      })
    },
  )

  test(
    "can only edit celex number if court is eugh or eug",
    {
      tag: ["@RISDEV-8469"],
    },
    async ({ page, documentNumber }) => {
      await test.step("Navigiere zu Rubriken als interner Nutzer", async () => {
        await navigateToCategories(page, documentNumber)
      })

      await test.step("set court to EuGH", async () => {
        const court = page.getByLabel("Gericht", { exact: true })
        await expect(court).toHaveValue("")
        await court.fill("EuGH")
        await expect(page.getByTestId("combobox-spinner")).toBeHidden()
        await expect(court).toHaveValue("EuGH")
        await page.getByText("EuGH", { exact: true }).click()
        await expect(court).toHaveValue("EuGH")
      })

      await test.step("check celex number is editable", async () => {
        const celexNumber = page.getByLabel("Celex-Nummer", { exact: true })
        await expect(celexNumber).toBeEditable()
        await expect(celexNumber).not.toHaveAttribute("readonly")
        await celexNumber.fill("abc")
        await expect(celexNumber).toHaveValue("abc")
      })

      await test.step("set court to BGH", async () => {
        const court = page.getByLabel("Gericht", { exact: true })
        await court.fill("BGH")
        await expect(page.getByTestId("combobox-spinner")).toBeHidden()
        await expect(court).toHaveValue("BGH")
        await page.getByText("BGH", { exact: true }).click()
        await expect(court).toHaveValue("BGH")
      })

      await test.step("check celex number is not editable", async () => {
        const celexNumber = page.getByLabel("Celex-Nummer", { exact: true })
        await expect(celexNumber).toBeVisible()
        await expect(celexNumber).toHaveValue("abc")
        await expect(celexNumber).not.toBeEditable()
        await expect(celexNumber).toHaveAttribute("readonly")
      })

      await test.step("set court to EuG", async () => {
        const court = page.getByLabel("Gericht", { exact: true })
        await court.fill("EuG")
        await expect(page.getByTestId("combobox-spinner")).toBeHidden()
        await expect(court).toHaveValue("EuG")
        await page.getByText("EuG", { exact: true }).click()
        await expect(court).toHaveValue("EuG")
      })

      await test.step("check celex number is editable", async () => {
        const celexNumber = page.getByLabel("Celex-Nummer", { exact: true })
        await expect(celexNumber).toBeEditable()
        await expect(celexNumber).not.toHaveAttribute("readonly")
      })
    },
  )
})
