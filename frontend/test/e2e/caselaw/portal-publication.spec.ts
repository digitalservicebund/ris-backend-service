import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  expectHistoryLogRow,
  navigateToManagementData,
  navigateToPublication,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe("Publish to and withdraw from portal", () => {
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
})
