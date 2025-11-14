import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  expectHistoryLogRow,
  navigateToCategories,
  navigateToManagementData,
  navigateToPublication,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Publish and withdraw decision",
  {
    tag: ["@RISDEV-6639"],
  },
  () => {
    test.use({
      decisionsToBeCreated: [
        [
          {
            coreData: {
              court: { label: "BGH" },
              decisionDate: "2023-01-01",
              documentType: { label: "Beschluss", jurisShortcut: "Bes" },
            },
            shortTexts: {},
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
          },
          {
            coreData: {
              court: { label: "AG Aachen" },
              decisionDate: "2023-01-01",
              deviatingFileNumbers: ["e2e-test-123"],
              documentType: { label: "Beschluss", jurisShortcut: "Bes" },
            },
            shortTexts: {
              headnote: "headnote",
            },
          },
          {
            coreData: {
              court: { label: "AG Aachen" },
              decisionDate: "2023-01-01",
              deviatingFileNumbers: ["e2e-test-123"],
              documentType: { label: "Beschluss", jurisShortcut: "Bes" },
            },
          },
        ],
        { scope: "test" },
      ],
    })
    test(
      "Entscheidung kann veröffentlicht und zurückgezogen werden",
      {
        tag: ["@RISDEV-8456", "@RISDEV-8460"],
      },
      async ({ page, prefilledDocumentUnit }) => {
        await navigateToPublication(page, prefilledDocumentUnit.documentNumber)

        await test.step("Anzeige einer unveröffentlichten Entscheidung mit allen Checks und ohne Fehler", async () => {
          await expect(
            page.getByRole("heading", { name: "Plausibilitätsprüfung" }),
          ).toBeVisible()
          await expect(
            page.getByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
          ).toBeVisible()
          await expect(
            page.getByRole("heading", { name: "Randnummernprüfung" }),
          ).toBeVisible()
          await expect(
            page.getByText("Die Reihenfolge der Randnummern ist korrekt."),
          ).toBeVisible()
          await expect(
            page.getByRole("heading", { name: "Dublettenprüfung" }),
          ).toBeVisible()
          await expect(
            page.getByText("Es besteht kein Dublettenverdacht."),
          ).toBeVisible()
          // await expect(
          //   page.getByRole("heading", { name: "Rechtschreibprüfung" }),
          // ).toBeVisible()
          // await expect(
          //   page.getByText("Es wurden keine Rechtschreibfehler identifiziert."),
          // ).toBeVisible()
          await expect(page.getByTitle("LDML Vorschau")).toBeVisible()
          await expect(
            page.getByRole("heading", { name: "Aktueller Status Portal" }),
          ).toBeVisible()
          await expect(
            page.getByTestId("portal-publication-status-badge"),
          ).toBeVisible()
          await expect(
            page.getByTestId("portal-publication-status-badge"),
          ).toHaveText("Unveröffentlicht")
          await expect(
            page.getByRole("button", { name: "Veröffentlichen" }),
          ).toBeVisible()
          await expect(
            page.getByRole("button", { name: "Veröffentlichen" }),
          ).toBeEnabled()
        })

        await test.step("Erfolgreiches Veröffentlichen ändert den Status", async () => {
          await page.getByRole("button", { name: "Veröffentlichen" }).click()
          await expect(
            page.getByTestId("portal-publication-status-badge"),
          ).toHaveText("Veröffentlicht")
          await expect(
            page.getByRole("link", {
              name: "Portalseite der Dokumentationseinheit",
            }),
          ).toHaveAttribute(
            "href",
            `https://ris-portal.dev.ds4g.net/case-law/${prefilledDocumentUnit.documentNumber}`,
          )
          await expect(
            page.getByText(
              "Das Hochladen der Stammdaten und der Informationen im Portal-Tab „Details“ dauert ungefähr 5 Minuten.",
            ),
          ).toBeVisible()
          await expect(
            page.getByText("Zuletzt veröffentlicht am:"),
          ).toBeVisible()
          await expect(
            page.getByRole("button", { name: "Zurückziehen" }),
          ).toBeVisible()
        })

        await test.step("Hinweis auf verzögerte Veröffentlichung wird nach Reload ausgeblendet", async () => {
          await page.reload()
          await expect(
            page.getByText(
              "Das Hochladen der Stammdaten und der Informationen im Portal-Tab „Details“ dauert ungefähr 5 Minuten.",
            ),
          ).toBeHidden()
        })

        await test.step("Eine veröffentlichte Dokumentationseinheit kann nicht gelöscht werden", async () => {
          await navigateToManagementData(
            page,
            prefilledDocumentUnit.documentNumber,
          )

          await page
            .getByRole("button", { name: "Dokumentationseinheit löschen" })
            .click()

          await page
            .getByRole("button", { name: "Löschen", exact: true })
            .click()

          await expect(
            page.getByText(
              "Die Dokumentationseinheit konnte nicht gelöscht werden, da Sie im Portal veröffentlicht ist.",
            ),
          ).toBeVisible()

          await navigateToPublication(
            page,
            prefilledDocumentUnit.documentNumber,
          )
        })

        await test.step("Erfolgreiches Zurückziehen ändert den Status", async () => {
          await page.getByRole("button", { name: "Zurückziehen" }).click()
          await expect(
            page.getByTestId("portal-publication-status-badge"),
          ).toHaveText("Zurückgezogen")
          await expect(
            page.getByRole("button", { name: "Zurückziehen" }),
          ).toBeHidden()
          await expect(
            page.getByText(
              "Portalseite der Dokumentationseinheit wurde entfernt",
            ),
          ).toBeVisible()
          await expect(
            page.getByRole("link", {
              name: "Portalseite der Dokumentationseinheit",
            }),
          ).toHaveAttribute(
            "href",
            `https://ris-portal.dev.ds4g.net/case-law/${prefilledDocumentUnit.documentNumber}`,
          )
        })

        await test.step("Veröffentlichen und Zurückziehen wird in der Historie geloggt", async () => {
          await navigateToManagementData(
            page,
            prefilledDocumentUnit.documentNumber,
          )
          await expect(
            page.getByRole("heading", { name: "Historie" }),
          ).toBeVisible()
          await expectHistoryLogRow(
            page,
            0,
            "DS (e2e_tests DigitalService)",
            "Dokeinheit wurde aus dem Portal zurückgezogen",
          )
          await expectHistoryLogRow(
            page,
            1,
            "NeuRIS",
            "Status im Portal geändert: Veröffentlicht → Zurückgezogen",
          )
          await expectHistoryLogRow(
            page,
            2,
            "DS (e2e_tests DigitalService)",
            "Dokeinheit im Portal veröffentlicht",
          )
          await expectHistoryLogRow(
            page,
            3,
            "NeuRIS",
            "Status im Portal geändert: Unveröffentlicht → Veröffentlicht",
          )
        })
      },
    )

    test(
      "Plausibilitätsprüfung",
      {
        tag: ["@RISDEV-8456"],
      },
      async ({ page, documentNumber, prefilledDocumentUnit }) => {
        await navigateToPublication(page, documentNumber)
        await expect(
          page.getByRole("heading", { name: "Plausibilitätsprüfung" }),
        ).toBeVisible()
        await test.step("Zeigt alle benötigten, nicht ausgefüllten Rubriken an", async () => {
          await expect(
            page.getByText(
              "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:",
            ),
          ).toBeVisible()
          await expect(
            page.locator("li:has-text('Aktenzeichen')"),
          ).toBeVisible()
          await expect(page.locator("li:has-text('Gericht')")).toBeVisible()
          await expect(
            page.locator("li:has-text('Entscheidungsdatum')"),
          ).toBeVisible()
          await expect(page.locator("li:has-text('Dokumenttyp')")).toBeVisible()
        })

        await test.step("Anzeige wird nach dem Ausfüllen einer Rubrik geupdated", async () => {
          await page.getByLabel("Rubriken bearbeiten", { exact: true }).click()
          await page.getByLabel("Aktenzeichen").getByRole("textbox").fill("abc")
          await page.keyboard.press("Enter")
          await save(page)
          await page.getByLabel("Veröffentlichen", { exact: true }).click()
          await expect(
            page.getByText(
              "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:",
            ),
          ).toBeVisible()
          await expect(page.locator("li:has-text('Aktenzeichen')")).toBeHidden()
          await expect(page.locator("li:has-text('Gericht')")).toBeVisible()
          await expect(
            page.locator("li:has-text('Entscheidungsdatum')"),
          ).toBeVisible()
          await expect(page.locator("li:has-text('Dokumenttyp')")).toBeVisible()
        })

        await test.step("Zeigt keine Fehler an wenn alle benötigten Rubriken ausgefüllt sind", async () => {
          await navigateToPublication(
            page,
            prefilledDocumentUnit.documentNumber,
          )
          await expect(
            page.getByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
          ).toBeVisible()
          await expect(page.locator("li:has-text('Aktenzeichen')")).toBeHidden()
          await expect(page.locator("li:has-text('Gericht')")).toBeHidden()
          await expect(
            page.locator("li:has-text('Entscheidungsdatum')"),
          ).toBeHidden()
          await expect(page.locator("li:has-text('Dokumenttyp')")).toBeHidden()
        })
      },
    )

    test(
      "Randnummerprüfung",
      {
        tag: ["@RISDEV-8456"],
      },
      async ({ page, decisions }) => {
        const { createdDecisions } = decisions
        const decision = createdDecisions[0]

        await navigateToPublication(page, decision.documentNumber!)

        await test.step("Fehler in der Randnummerprüfung werden angezeigt", async () => {
          await expect(
            page.getByText(
              "Die Reihenfolge der Randnummern ist nicht korrekt.",
            ),
          ).toBeVisible()

          await expect(page.getByText("RubrikTatbestand")).toBeVisible()
          await expect(page.getByText("Erwartete Randnummer 1")).toBeVisible()
          await expect(
            page.getByText("Tatsächliche Randnummer 3"),
          ).toBeVisible()
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

        await test.step("Veröffentlichung ist möglich nachdem Randnummernwarnung bestätigt wurde", async () => {
          await page.getByRole("button", { name: "Veröffentlichen" }).click()

          await expect(
            page.getByText("Prüfung hat Warnungen ergeben"),
          ).toBeVisible()
          await expect(
            page.getByText("Die Randnummern sind nicht korrekt"),
          ).toBeVisible()

          await page.getByLabel("Trotzdem veröffentlichen").click()

          await expect(
            page.getByTestId("portal-publication-status-badge"),
          ).toHaveText("Veröffentlicht")
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
          await page.getByText("LDML Vorschau").click()

          await expect(
            page.getByText(
              '<akn:a class="border-number-link" href="#border-number-link-2">2</akn:a>',
            ),
          ).toBeVisible()
          await expect(
            page.getByText(
              '<akn:a class="border-number-link" href="#border-number-link-1">1</akn:a>',
            ),
          ).toBeVisible()
          await expect(page.getByText("<akn:num>1</akn:num>")).toBeVisible()
          await expect(page.getByText("<akn:num>2</akn:num>")).toBeVisible()
          await expect(page.getByText("<akn:num>3</akn:num>")).toBeVisible()
          await expect(page.getByText("<akn:num>4</akn:num>")).toBeVisible()
        })

        await test.step("Randnummern und Links werden unter Rubriken korrekt angezeigt", async () => {
          await navigateToCategories(page, decision.documentNumber!)

          await expect(page.getByTestId("Tatbestand")).toHaveText("1Text")
          await expect(page.getByTestId("Entscheidungsgründe")).toHaveText(
            "2Text",
          )
          await expect(page.getByTestId("Abweichende Meinung")).toHaveText(
            "3Text",
          )
          await expect(page.getByTestId("Sonstiger Langtext")).toHaveText(
            "4Text",
          )

          // The first border number link was changed from 5 -> 2, the second one left as is.
          await expect(page.getByTestId("Tenor")).toHaveText("2 1")
        })
      },
    )

    test(
      "Dublettenprüfung",
      {
        tag: ["@RISDEV-8456"],
      },
      async ({ page, decisions }) => {
        const { createdDecisions } = decisions
        const decision = createdDecisions[1]

        await navigateToPublication(page, decision.documentNumber)

        await test.step("Dublettenwarnung wird angezeigt", async () => {
          await expect(
            page.getByText("Es besteht Dublettenverdacht."),
          ).toBeVisible()
        })

        await test.step("Veröffentlichung ist möglich nachdem Dublettenwarnung bestätigt wurde", async () => {
          await page.getByRole("button", { name: "Veröffentlichen" }).click()

          await expect(
            page.getByText("Prüfung hat Warnungen ergeben"),
          ).toBeVisible()
          await expect(
            page.getByText(
              "Es besteht Dublettenverdacht.\nWollen Sie das Dokument dennoch übergeben?",
            ),
          ).toBeVisible()

          await page.getByLabel("Trotzdem veröffentlichen").click()

          await expect(
            page.getByTestId("portal-publication-status-badge"),
          ).toHaveText("Veröffentlicht")
        })
      },
    )

    // Returns success but should fail. Unclear why.

    test(
      "Rechtschreibprüfung",
      {
        tag: ["@RISDEV-8456"],
      },
      async ({ page, prefilledDocumentUnit, baseURL }) => {
        // eslint-disable-next-line playwright/no-skipped-test
        test.skip(
          baseURL === "http://127.0.0.1",
          "Skipping this test on local execution, as there is no languagetool running",
        )

        await test.step("Fehlerhaften Text eingeben und speichern", async () => {
          await navigateToCategories(page, prefilledDocumentUnit.documentNumber)
          await page
            .getByRole("button", { name: "Gründe", exact: true })
            .click()
          await page.getByTestId("Gründe").click()
          await page.keyboard.type(`das wort ist flasch geschrieben`)
          await save(page)
        })

        await test.step("Rechtschreibfehler werden auf Veröffentlichungsseite angezeigt", async () => {
          await navigateToPublication(
            page,
            prefilledDocumentUnit.documentNumber,
          )
          const textcheck = page.getByLabel("Rechtschreibprüfung")

          await expect(
            textcheck.getByLabel("Ladestatus"),
            "Text check might take longer then expected",
          ).toBeHidden({
            timeout: 20_000,
          })

          await expect(
            page.getByText("Es wurden Rechtschreibfehler identifiziert:"),
          ).toBeVisible()
        })

        await test.step("Veröffentlichung ist trotz Rechtschreibfehler möglich", async () => {
          await page.getByRole("button", { name: "Veröffentlichen" }).click()

          await expect(
            page.getByTestId("portal-publication-status-badge"),
          ).toHaveText("Veröffentlicht")
        })
      },
    )

    test(
      "LDML Vorschau",
      {
        tag: ["@RISDEV-8456", "@RISDEV-8843"],
      },
      async ({ page, decisions }) => {
        const { createdDecisions } = decisions
        const decision = createdDecisions[1]

        await navigateToPublication(page, decision.documentNumber)

        await test.step("LDML Vorschau Ansicht ist ausklappbar und zeigt LDML an", async () => {
          await expect(page.getByText("LDML Vorschau")).toBeVisible()

          await page.getByText("LDML Vorschau").click()
          await expect(page.getByText("<akn:akomaNtoso")).toBeVisible()
        })

        await test.step("LDML Vorschau Ansicht ist zuklappbar", async () => {
          await page.getByText("LDML Vorschau").click()
          await expect(page.getByText("<akn:akomaNtoso")).toBeHidden()
        })

        await test.step("Fehler beim Laden der LDML Vorschau wenn kein valides LDML erzeugt werden kann", async () => {
          const decisionWithoutJudgmentBody = createdDecisions[2]
          await navigateToPublication(
            page,
            decisionWithoutJudgmentBody.documentNumber,
          )
          await expect(
            page.getByText("Fehler beim Laden der LDML-Vorschau"),
          ).toBeVisible()
          await expect(
            page.getByText(
              "Die LDML-Vorschau konnte nicht geladen werden: Missing judgment body.",
            ),
          ).toBeVisible()
          await expect(
            page.getByRole("button", { name: "Veröffentlichen" }),
          ).toBeDisabled()
        })
      },
    )
  },
)
