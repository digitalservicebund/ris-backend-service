import { expect } from "@playwright/test"
import { Decision } from "@/domain/decision"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { updateDocumentationUnit } from "~/e2e/caselaw/utils/documentation-unit-api-util"
import {
  clearTextField,
  fillActiveCitationInputs,
  fillCombobox,
  fillEnsuingDecisionInputs,
  fillPreviousDecisionInputs,
  navigateToCategories,
  navigateToHandover,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe("ensuring the handover of documentunits works as expected", () => {
  test.use({
    decisionsToBeCreated: [
      [
        {
          longTexts: { tenor: "<p>Text mit Fehler</p>" },
        },
        {
          coreData: {
            court: { label: "BGH" },
            decisionDate: "2023-01-01",
            documentType: { label: "Beschluss", jurisShortcut: "Bes" },
          },
          longTexts: {
            tenor:
              '<p>Text mit Bild <img src="data:image/png;base64, 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" alt="Grafik 2BundesAdlerAmtlich"></p>',
          },
        },
      ],
      { scope: "test" },
    ],
  })

  test("handover page shows all possible missing required fields when no fields filled", async ({
    page,
    documentNumber,
  }) => {
    await navigateToHandover(page, documentNumber)

    await expect(page.locator("li:has-text('Aktenzeichen')")).toBeVisible()
    await expect(page.locator("li:has-text('Gericht')")).toBeVisible()
    await expect(
      page.locator("li:has-text('Entscheidungsdatum')"),
    ).toBeVisible()
    await expect(page.locator("li:has-text('Dokumenttyp')")).toBeVisible()
  })

  test("handover page shows missing required fields of previous decisions", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await fillPreviousDecisionInputs(page, {
      court: "AG Aalen",
    })
    await page.getByLabel("Vorgehende Entscheidung speichern").click()
    await save(page)

    await expect(
      page.getByText(`AG Aalen`, {
        exact: true,
      }),
    ).toBeVisible()
    await navigateToHandover(page, documentNumber)

    await expect(
      page.locator("li:has-text('Vorgehende Entscheidungen')"),
    ).toBeVisible()

    await expect(
      page
        .locator("li:has-text('Vorgehende Entscheidungen')")
        .getByText("Entscheidungsdatum"),
    ).toBeVisible()
    await expect(
      page
        .locator("li:has-text('Vorgehende Entscheidungen')")
        .getByText("Aktenzeichen"),
    ).toBeVisible()
  })

  // RISDEV-2183
  test("handover page shows missing required fields of previous decisions with only missing fields in previous decisions", async ({
    page,
    prefilledDocumentUnit,
  }) => {
    await navigateToCategories(page, prefilledDocumentUnit.documentNumber!)

    await fillPreviousDecisionInputs(page, {
      court: "AG Aalen",
    })
    await page.getByLabel("Vorgehende Entscheidung speichern").click()
    await save(page)

    await expect(
      page.getByText(`AG Aalen`, {
        exact: true,
      }),
    ).toBeVisible()

    await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)

    await expect(
      page.locator("li:has-text('Vorgehende Entscheidungen')"),
    ).toBeVisible()

    await expect(
      page
        .locator("li:has-text('Vorgehende Entscheidungen')")
        .getByText("Entscheidungsdatum"),
    ).toBeVisible()
    await expect(
      page
        .locator("li:has-text('Vorgehende Entscheidungen')")
        .getByText("Aktenzeichen"),
    ).toBeVisible()
  })

  test("handover page shows missing required fields of ensuing decisions", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await fillEnsuingDecisionInputs(page, {
      court: "AG Aalen",
    })
    await page.getByLabel("Nachgehende Entscheidung speichern").click()
    await save(page)

    await expect(
      page.getByText(`nachgehend, AG Aalen`, {
        exact: true,
      }),
    ).toBeVisible()
    await navigateToHandover(page, documentNumber)

    await expect(
      page.locator("li:has-text('Nachgehende Entscheidungen')"),
    ).toBeVisible()

    await expect(
      page
        .locator("li:has-text('Nachgehende Entscheidungen')")
        .getByText("Entscheidungsdatum"),
    ).toBeVisible()
    await expect(
      page
        .locator("li:has-text('Nachgehende Entscheidungen')")
        .getByText("Aktenzeichen"),
    ).toBeVisible()
  })

  test("handover page does not show missing required decision date, when ensuing decision is pending", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await fillEnsuingDecisionInputs(page, {
      pending: true,
      court: "AG Aalen",
      fileNumber: "123",
    })
    await page.getByLabel("Nachgehende Entscheidung speichern").click()
    await save(page)
    await expect(
      page.getByText(`anhängig, AG Aalen, Datum unbekannt, 123`, {
        exact: true,
      }),
    ).toBeVisible()
    await navigateToHandover(page, documentNumber)

    await expect(
      page.locator("li:has-text('Nachgehende Entscheidungen')"),
    ).toBeHidden()
  })

  test("handover page shows missing required fields of active citations", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await navigateToCategories(page, documentNumber)

    await fillActiveCitationInputs(page, {
      documentType: prefilledDocumentUnit.coreData.documentType?.label,
    })
    await page.getByLabel("Aktivzitierung speichern").click()
    await save(page)

    await navigateToHandover(page, documentNumber)

    await expect(page.locator("li:has-text('Aktivzitierung')")).toBeVisible()

    await expect(
      page
        .locator("li:has-text('Aktivzitierung')")
        .getByText("Art der Zitierung"),
    ).toBeVisible()
    await expect(
      page.locator("li:has-text('Aktivzitierung')").getByText("Gericht"),
    ).toBeVisible()
    await expect(
      page
        .locator("li:has-text('Aktivzitierung')")
        .getByText("Entscheidungsdatum"),
    ).toBeVisible()
    await expect(
      page.locator("li:has-text('Aktivzitierung')").getByText("Aktenzeichen"),
    ).toBeVisible()
  })

  // Todo: very flaky
  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("handover page updates missing required fields after fields were updated", async ({
    page,
    documentNumber,
  }) => {
    await navigateToHandover(page, documentNumber)
    await expect(page.locator("li:has-text('Aktenzeichen')")).toBeVisible()
    await expect(page.locator("li:has-text('Gericht')")).toBeVisible()
    await expect(
      page.locator("li:has-text('Entscheidungsdatum')"),
    ).toBeVisible()
    await expect(page.locator("li:has-text('Dokumenttyp')")).toBeVisible()
    await page.getByLabel("Rubriken bearbeiten", { exact: true }).click()

    await page.getByLabel("Aktenzeichen", { exact: true }).fill("abc")
    await page.keyboard.press("Enter")
    await save(page)

    await page.getByLabel("Gericht", { exact: true }).fill("aalen")
    await page.getByText("AG Aalen").click()

    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
      "AG Aalen",
    )

    await navigateToHandover(page, documentNumber)
    await expect(page.locator("li:has-text('Aktenzeichen')")).toBeHidden()
    await expect(page.locator("li:has-text('Gericht')")).toBeHidden()
    await expect(
      page.locator("li:has-text('Entscheidungsdatum')"),
    ).toBeVisible()
    await expect(page.locator("li:has-text('Dokumenttyp')")).toBeVisible()
  })

  test("handover not possible if required fields missing", async ({
    page,
    documentNumber,
  }) => {
    await navigateToHandover(page, documentNumber)

    await expect(
      page.getByLabel("Dokumentationseinheit an jDV übergeben"),
    ).toBeDisabled()
  })

  test("handover possible when all required fields filled", async ({
    page,
    prefilledDocumentUnit,
  }) => {
    await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)

    await expect(page.getByText("XML Vorschau")).toBeVisible()

    await page.getByText("XML Vorschau").click()

    await expect(
      page.getByText("        <entsch-datum>2019-12-31</entsch-datum>", {
        exact: true,
      }),
    ).toBeVisible()

    await expect(
      page.getByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
    ).toBeVisible()

    await expect(
      page.locator(
        "text=Diese Dokumentationseinheit wurde bisher nicht an die jDV übergeben",
      ),
    ).toBeVisible()

    await page
      .getByLabel("Dokumentationseinheit an jDV übergeben", { exact: true })
      .click()

    await expect(page.getByText("Email wurde versendet")).toBeVisible()

    await expect(page.getByText("Xml Email Abgabe -")).toBeVisible()
  })

  test(
    "handover does not change publication status",
    {
      annotation: [
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4293",
        },
      ],
    },
    async ({ page, prefilledDocumentUnit }) => {
      await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)
      await expect(page.getByText("unveröffentlicht")).toBeVisible()

      await expect(
        page.getByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).toBeVisible()

      await page
        .getByLabel("Dokumentationseinheit an jDV übergeben", { exact: true })
        .click()

      await expect(page.getByText("Email wurde versendet")).toBeVisible()
      await expect(page.getByText("Xml Email Abgabe -")).toBeVisible()
      await expect(page.getByText("unveröffentlicht")).toBeVisible()
    },
  )

  test(
    "handover displays border number validation errors and cannot resolve them",
    {
      annotation: [
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4975",
        },
      ],
      tag: ["@RISDEV-4975"],
    },
    async ({ page, prefilledDocumentUnit, request }) => {
      await test.step("Befülle Langtexte mit invaliden Randnummern und Verlinkungen", async () => {
        const documentationUnit = {
          ...prefilledDocumentUnit,
          longTexts: {
            tenor: `<border-number-link nr='5'>5</border-number-link>
 <border-number-link nr='1'>1</border-number-link>`,
            caseFacts:
              "<border-number><number>3</number><content>Text</content></border-number>",
            decisionReasons:
              "<border-number><number>5</number><content>Text</content></border-number>",
            otherLongText:
              "<border-number><number>6</number><content>Text</content></border-number>",
            dissentingOpinion:
              "<border-number><number>7</number><content>Text</content></border-number>",
          },
        } as Decision
        await updateDocumentationUnit(page, documentationUnit, request)
      })

      await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)

      await expect(
        page.getByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).toBeVisible()

      await test.step("Fehler in der Randnummerprüfung werden angezeigt", async () => {
        await expect(
          page.getByText("Die Reihenfolge der Randnummern ist nicht korrekt."),
        ).toBeVisible()

        await expect(page.getByText("RubrikTatbestand")).toBeVisible()
        await expect(page.getByText("Erwartete Randnummer 1")).toBeVisible()
        await expect(page.getByText("Tatsächliche Randnummer 3")).toBeVisible()
      })

      await test.step("Fehler in der Randnummer-Verlinkungs-Prüfung werden angezeigt", async () => {
        await expect(
          page
            .getByText(
              "Es gibt ungültige Randnummern-Verweise in folgenden Rubriken:",
            )
            .getByText("Tenor"),
        ).toBeVisible()
      })

      await test.step("Übergabe ist möglich nachdem Randnummernwarnung bestätigt wurde", async () => {
        await page
          .getByLabel("Dokumentationseinheit an jDV übergeben", { exact: true })
          .click()

        await expect(
          page.getByText("Prüfung hat Warnungen ergeben"),
        ).toBeVisible()
        await expect(
          page.getByText("Die Randnummern sind nicht korrekt"),
        ).toBeVisible()

        await page.getByLabel("Trotzdem übergeben").click()

        await expect(page.getByText("Email wurde versendet")).toBeVisible()
        await expect(page.getByText("Xml Email Abgabe -")).toBeVisible()
      })

      await test.step("Randnummern können neu berechnet werden", async () => {
        await page.getByLabel("Randnummern neu berechnen").click()

        await expect(
          page.getByText("Die Randnummern werden neu berechnet"),
        ).toBeVisible()
        await expect(
          page.getByText("Die Reihenfolge der Randnummern ist korrekt."),
          // The loading spinner is shown for 3s (artificial delay)
        ).toBeVisible({ timeout: 5_000 })
      })

      await test.step("Neu berechnete Randnummern und Links werden in XML-Vorschau angezeigt", async () => {
        await page.getByTitle("XML Vorschau").getByLabel("Aufklappen").click()

        const xmlPreviewText = await page.getByTitle("XML Vorschau").innerText()

        const regex =
          /<tenor>\s*\d*\s*<body>\s*\d*\s*<div>\s*\d*\s*<rdlink nr="2"\/>\s*\d*\s*<rdlink nr="1"\/>\s*\d*\s*<\/div>\s*\d*\s*<\/body>\s*\d*\s*<\/tenor>\s*\d*\s*<tatbestand>\s*\d*\s*<body>\s*\d*\s*<div>\s*\d*\s*<p>\s*\d*\s*<rd nr="1"\/>Text/

        expect(xmlPreviewText).toMatch(regex)
      })

      await test.step("Randnummern und Links werden unter Rubriken korrekt angezeigt", async () => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber!)

        await expect(page.getByTestId("Tatbestand")).toHaveText("1Text")
        await expect(page.getByTestId("Entscheidungsgründe")).toHaveText(
          "2Text",
        )
        await expect(page.getByTestId("Abweichende Meinung")).toHaveText(
          "3Text",
        )
        await expect(page.getByTestId("Sonstiger Langtext")).toHaveText("4Text")

        // The first border number link was changed from 5 -> 2, the second one left as is.
        await expect(page.getByTestId("Tenor")).toHaveText("2 1")
      })
    },
  )

  test(
    "(text check) error count should correctly show number of errors",
    {
      tag: ["@RISDEV-254", "@RISDEV-6245", "@RISDEV-9094"],
    },
    async ({ page, decisions }) => {
      const { createdDecisions } = decisions
      const decision = createdDecisions[0]
      await navigateToHandover(page, decision.documentNumber!)
      const handover = page.getByLabel("Rechtschreibprüfung")

      await test.step("Validate document has no errors", async () => {
        await expect(
          handover.getByLabel("Rechtschreibprüfung").getByLabel("Ladestatus"),
          "Text check might take longer then expected",
        ).toBeHidden({
          timeout: 20_000,
        })

        await expect(
          page.getByText("Es wurden keine Rechtschreibfehler identifiziert"),
        ).toBeVisible()
      })

      await test.step("Validate document has text check errors", async () => {
        await navigateToCategories(page, decision.documentNumber!)
        const tenorEditor = page.getByTestId("Tenor")
        await clearTextField(page, tenorEditor)
        await tenorEditor.locator("div").fill("Text mit Feler")
        await navigateToHandover(page, decision.documentNumber!, {
          navigationBy: "click",
        })

        await expect(
          page.getByText("Es wurden Rechtschreibfehler identifiziert"),
        ).toBeVisible()

        await page.getByTestId("total-text-check-errors-container").isVisible()
        await expect(page.getByTestId("total-text-check-errors")).toHaveText(
          "1",
        )
      })

      await test.step("Validate errors are counted", async () => {
        const handover = page.getByLabel("Rechtschreibprüfung")

        await expect(
          handover.getByLabel("Ladestatus"),
          "Text check might take longer then expected",
        ).toBeHidden({
          timeout: 20_000,
        })

        await expect(
          page.getByText("Es wurden Rechtschreibfehler identifiziert:"),
        ).toBeVisible()

        await page.getByTestId("total-text-check-errors-container").isVisible()
        await expect(page.getByTestId("total-text-check-errors")).toHaveText(
          "1",
        )
      })
    },
  )

  test(
    "Warnung: CELEX-Nummer kann nicht an jDV exportiert werden",
    {
      tag: ["@RISDEV-8469"],
    },
    async ({ page, prefilledDocumentUnit }) => {
      await test.step("Befülle CELEX-Nummer", async () => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)
        await fillCombobox(page, "Gericht", "EuG")
        await page.getByLabel("Celex-Nummer", { exact: true }).fill("1234/5678")
        await save(page)
      })

      await test.step("Auf Übergabeseite erscheint Warnung, dass die Rubrik nicht übergeben wird", async () => {
        await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
        await expect(
          page.getByText(
            "Folgende Rubriken sind befüllt und können nicht an die jDV exportiert werden",
          ),
        ).toBeVisible()
        await expect(page.getByText("CELEX-Nummer")).toBeVisible()
        await page
          .getByRole("button", {
            name: "Dokumentationseinheit an jDV übergeben",
          })
          .click()
        await expect(
          page.getByRole("button", { name: "Trotzdem übergeben" }),
        ).toBeVisible()
      })
    },
  )

  test("handover with images", async ({ page, decisions }) => {
    const { createdDecisions } = decisions
    const decision = createdDecisions[1]
    await navigateToHandover(page, decision.documentNumber!)

    await expect(page.getByText("XML Vorschau")).toBeVisible()

    await page.getByText("XML Vorschau").click()

    await expect(page.getByText("<p>Text mit Bild <jurimg alt")).toBeVisible()

    await expect(
      page.getByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
    ).toBeVisible()

    await expect(
      page.locator(
        "text=Diese Dokumentationseinheit wurde bisher nicht an die jDV übergeben",
      ),
    ).toBeVisible()

    await page
      .getByLabel("Dokumentationseinheit an jDV übergeben", { exact: true })
      .click()

    await expect(page.getByText("Email wurde versendet")).toBeVisible()

    await expect(page.getByText("Xml Email Abgabe -")).toBeVisible()
    await expect(
      page.getByText("Anhänge: " + decision.documentNumber.toLowerCase()),
    ).toBeVisible()
  })
})
