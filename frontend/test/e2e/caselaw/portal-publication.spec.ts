import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  expectHistoryLogRow,
  navigateToCategories,
  navigateToManagementData,
  navigateToPublication,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe("Publish to and withdraw from portal", () => {
  test.use({
    decisionsToBeCreated: [
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
    ],
  })
  test("Dokumentationseinheit kann veröffentlicht und zurückgezogen werden", async ({
    page,
    prefilledDocumentUnit,
  }) => {
    await navigateToPublication(page, prefilledDocumentUnit.documentNumber)

    await test.step("Anzeige einer Unveröffentlichten Dok-Einheit mit allen Checks und ohne Fehler", async () => {
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
        page.getByRole("heading", { name: "Dublettenprüfung" }),
      ).toBeVisible()
      await expect(
        page.getByRole("heading", { name: "Rechtschreibprüfung" }),
      ).toBeVisible()
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
    })

    await test.step("Erfolgreiches Veröffentlichen ändert den Status", async () => {
      await page.getByRole("button", { name: "Veröffentlichen" }).click()
      await expect(
        page.getByTestId("portal-publication-status-badge"),
      ).toHaveText("Veröffentlicht")
      await expect(
        page.getByRole("button", { name: "Zurückziehen" }),
      ).toBeVisible()
    })

    await test.step("Erfolgreiches Zurückziehen ändert den Status", async () => {
      await page.getByRole("button", { name: "Zurückziehen" }).click()
      await expect(
        page.getByTestId("portal-publication-status-badge"),
      ).toHaveText("Zurückgezogen")
      await expect(
        page.getByRole("button", { name: "Zurückziehen" }),
      ).toBeHidden()
    })

    await test.step("Veröffentlichen und Zurückziehen wird in der Historie geloggt", async () => {
      await navigateToManagementData(page, prefilledDocumentUnit.documentNumber)
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
  })
  test("Plausibilitätsprüfung", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
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
      await expect(page.locator("li:has-text('Aktenzeichen')")).toBeVisible()
      await expect(page.locator("li:has-text('Gericht')")).toBeVisible()
      await expect(
        page.locator("li:has-text('Entscheidungsdatum')"),
      ).toBeVisible()
      await expect(page.locator("li:has-text('Dokumenttyp')")).toBeVisible()
    })

    await test.step("Anzeige wird nach dem Ausfüllen einer Rubrik geupdated", async () => {
      await page.getByLabel("Rubriken bearbeiten", { exact: true }).click()
      await page.getByLabel("Aktenzeichen", { exact: true }).fill("abc")
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
      await navigateToPublication(page, prefilledDocumentUnit.documentNumber)
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
  })

  test("Randnummerprüfung", async ({ page, decisions }) => {
    const { createdDecisions } = decisions
    const decision = createdDecisions[0]

    await navigateToPublication(page, decision.documentNumber!)

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

    // await test.step("Neu berechnete Randnummern und Links werden in XML-Vorschau angezeigt", async () => { //todo
    //   await page.getByTitle("XML Vorschau").getByLabel("Aufklappen").click()
    //
    //   const xmlPreviewText = await page
    //     .getByTitle("LDML Vorschau")
    //     .innerText()
    //
    //   const regex =
    //     /<tenor>\s*\d*\s*<body>\s*\d*\s*<div>\s*\d*\s*<rdlink nr="2"\/>\s*\d*\s*<rdlink nr="1"\/>\s*\d*\s*<\/div>\s*\d*\s*<\/body>\s*\d*\s*<\/tenor>\s*\d*\s*<tatbestand>\s*\d*\s*<body>\s*\d*\s*<div>\s*\d*\s*<p>\s*\d*\s*<rd nr="1"\/>Text/
    //
    //   expect(xmlPreviewText).toMatch(regex)
    // })

    await test.step("Randnummern und Links werden unter Rubriken korrekt angezeigt", async () => {
      await navigateToCategories(page, decision.documentNumber!)

      await expect(page.getByTestId("Tatbestand")).toHaveText("1Text")
      await expect(page.getByTestId("Entscheidungsgründe")).toHaveText("2Text")
      await expect(page.getByTestId("Abweichende Meinung")).toHaveText("3Text")
      await expect(page.getByTestId("Sonstiger Langtext")).toHaveText("4Text")

      // The first border number link was changed from 5 -> 2, the second one left as is.
      await expect(page.getByTestId("Tenor")).toHaveText("2 1")
    })
  })
})
