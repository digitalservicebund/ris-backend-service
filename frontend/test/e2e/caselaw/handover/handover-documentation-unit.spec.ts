import { expect } from "@playwright/test"
import {
  fillActiveCitationInputs,
  fillEnsuingDecisionInputs,
  fillPreviousDecisionInputs,
  navigateToCategories,
  navigateToHandover,
  save,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"
import { Decision } from "@/domain/decision"
import {
  addIgnoreWordToDocumentationUnit,
  updateDocumentationUnit,
} from "~/e2e/caselaw/utils/documentation-unit-api-util"

test.describe("ensuring the handover of documentunits works as expected", () => {
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

  // eslint-disable-next-line playwright/no-skipped-test
  test.skip(
    "handover wraps <noindex> on ignored words in preview and publication",
    {
      tag: ["@RISDEV-254", "@RISDEV-6205", "@RISDEV-7394"],
    },
    async ({ page, prefilledDocumentUnit, request }) => {
      const expectedNoindexCount = 9
      await test.step("Befülle Langtexte und Kurztexte mit texts", async () => {
        await addIgnoreWordToDocumentationUnit(
          page,
          prefilledDocumentUnit.uuid,
          "text",
        )

        const text =
          "<p>das wort text soll mit noindex umgeschlossen werden</p>"

        const documentationUnit = {
          ...prefilledDocumentUnit,
          shortTexts: {
            headline: text,
            guidingPrinciple: text,
            headnote: text,
            otherHeadnote: text,
          },
          longTexts: {
            tenor: text,
            caseFacts: text,
            decisionReasons: text,
            otherLongText: text,
            dissentingOpinion: text,
          },
        } as Decision
        await updateDocumentationUnit(page, documentationUnit, request)
      })

      await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)

      await test.step("XML Vorschau zeigt die noindex tags", async () => {
        await expect(page.getByText("XML Vorschau")).toBeVisible()

        await page.getByText("XML Vorschau").click()

        await expect(
          page.getByText(
            "<p>das wort <noindex>text</noindex> soll mit noindex umgeschlossen werden</p>",
            {
              exact: true,
            },
          ),
        ).toHaveCount(expectedNoindexCount)
      })

      await test.step("Übergabe an der jDV beinhaltet noindex", async () => {
        await page
          .getByLabel("Dokumentationseinheit an jDV übergeben", { exact: true })
          .click()

        await expect(page.getByText("Email wurde versendet")).toBeVisible()
        await expect(page.getByText("Xml Email Abgabe -")).toBeVisible()

        // workaround: formatting works on page referseh
        await page.reload()

        await expect(
          page
            .getByTestId("xml-handover-code-snippet-preview")
            .getByText(
              "<p>das wort <noindex>text</noindex> soll mit noindex umgeschlossen werden</p>",
              {
                exact: true,
              },
            ),
        ).toHaveCount(expectedNoindexCount)
      })
    },
  )

  test(
    "handover shows text check mistakes",
    {
      tag: ["@RISDEV-254", "@RISDEV-6245"],
    },
    async ({ page, prefilledDocumentUnit, request }) => {
      await test.step("Befülle Langtexte und Kurztexte mit texts", async () => {
        const text = "<p>das wort hat einen Flerher</p>"

        const documentationUnit = {
          ...prefilledDocumentUnit,
          shortTexts: {
            headline: undefined,
            guidingPrinciple: undefined,
            headnote: undefined,
            otherHeadnote: text,
          },
          longTexts: {
            tenor: text,
            caseFacts: undefined,
            decisionReasons: undefined,
            otherLongText: undefined,
            dissentingOpinion: undefined,
          },
        } as Decision
        await updateDocumentationUnit(page, documentationUnit, request)
      })

      await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)

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
      })
    },
  )
})
