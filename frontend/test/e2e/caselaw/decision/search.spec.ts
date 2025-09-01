import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { deleteDocumentUnit } from "~/e2e/caselaw/utils/documentation-unit-api-util"
import {
  fillInput,
  navigateToSearch,
  checkResultListContent,
  openSearchWithFileNumberPrefix,
  triggerSearch,
  fillSearchInput,
  navigateToCategories,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"
import { noteContent } from "~/e2e/testdata"
import { generateString } from "~/test-helper/dataGenerators"

/* eslint-disable playwright/expect-expect */

test.describe("Große Suche nach Entscheidungen", () => {
  let documentNumberToBeDeleted: string | undefined
  test.use({
    decisionsToBeCreated: [
      // Reverse sorting: date DESC, docNumber DESC
      [
        { coreData: { court: { label: "BFH" } } },
        { coreData: { court: { label: "AG Aachen" } } },
        { coreData: { decisionDate: "2023-01-01" } },
        { coreData: { decisionDate: "2023-01-02" } },
        { coreData: { decisionDate: "2023-01-31" } },
      ],
      { scope: "test" },
    ],
  })

  test("Keine Ergebnisse", async ({ page }) => {
    await navigateToSearch(page)
    const loadingIndicator = page.locator('div[data-pc-section="mask"]')

    await test.step("Initialer Zustand", async () => {
      await expect(
        page.getByText(
          "Starten Sie die Suche oder erstellen Sie eine neue Dokumentationseinheit.",
        ),
      ).toBeVisible()
      await expect(
        page.getByRole("button", {
          name: "Neue Entscheidung erstellen",
        }),
      ).toBeVisible()
    })

    await test.step("Suche auslösen und Ladezustand prüfen", async () => {
      await fillInput(page, "Dokumentnummer Suche", "non existing")
      await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
      await expect(loadingIndicator).toBeVisible()
    })

    await test.step("Keine Ergebnisse nach Suche", async () => {
      await expect(
        page.getByText("Keine Suchergebnisse gefunden"),
      ).toBeVisible()
      await expect(page.getByText("Neue Entscheidung erstellen")).toBeVisible()
      await expect(loadingIndicator).toBeHidden()
    })
  })

  test("Fehleranzeige bei fehlgeschlagener Suche", async ({ page }) => {
    await navigateToSearch(page)

    await page.getByLabel("Nur meine Dokstelle Filter").click()
    await page.route("**/*", async (route) => {
      await route.fulfill({
        status: 404,
        contentType: "text/plain",
        body: "Not Found!",
      })

      await expect(
        page.getByText("Die Suchergebnisse konnten nicht geladen werden."),
      ).toBeVisible()
    })
    await page.unroute("**/*")
  })

  test("Eine Suche kann nicht mit leeren oder fehlerhaften Inputs gestartet werden", async ({
    page,
  }) => {
    await navigateToSearch(page)
    await triggerSearch(page)
    await expect(
      page.getByText("Geben Sie mindestens ein Suchkriterium ein"),
    ).toBeVisible()

    await page
      .getByLabel("Entscheidungsdatum Suche", { exact: true })
      .fill("24.12.2022")
    await page.getByLabel("Entscheidungsdatum Suche Ende").fill("29.02.2023")
    await expect(page.getByText("Kein valides Datum")).toBeVisible()
    await triggerSearch(page)
    await expect(page.getByText("Fehler in Suchkriterien")).toBeVisible()

    await page.getByLabel("Entscheidungsdatum Suche Ende").fill("25.12.2022")
    await triggerSearch(page)
    await expect(page.getByText("Fehler in Suchkriterien")).toBeHidden()
  })

  test("Suche nach Aktenzeichen", async ({ page, decisions }) => {
    const { fileNumberPrefix, createdDecisions } = decisions
    await openSearchWithFileNumberPrefix(fileNumberPrefix, page)
    await triggerSearch(page)
    await checkResultListContent(createdDecisions, page)
  })

  test("Suche nach Gerichtstyp", async ({ page, decisions }) => {
    const { fileNumberPrefix, createdDecisions } = decisions
    await openSearchWithFileNumberPrefix(fileNumberPrefix, page)
    await test.step("Wähle Gerichtstyp 'BFH' in Suche", async () => {
      await fillInput(page, "Gerichtstyp Suche", "BFH")
    })
    await triggerSearch(page)
    const docUnitSearchResults = createdDecisions.filter(
      (p) => p.coreData.court?.label === "BFH",
    )
    await checkResultListContent(docUnitSearchResults, page)
  })

  test("Suche nach Gerichtsort", async ({ page, decisions }) => {
    const { fileNumberPrefix, createdDecisions } = decisions
    await openSearchWithFileNumberPrefix(fileNumberPrefix, page)
    await test.step("Wähle Gerichtsort 'Aachen' in Suche", async () => {
      await fillInput(page, "Gerichtsort Suche", "Aachen")
    })
    await triggerSearch(page)
    const docUnitSearchResults = createdDecisions.filter(
      (p) => p.coreData.court?.label === "AG Aachen",
    )
    await checkResultListContent(docUnitSearchResults, page)
  })

  test("Suche nach Dokumentnummer", async ({ page, decisions }) => {
    const { fileNumberPrefix, createdDecisions } = decisions
    await openSearchWithFileNumberPrefix(fileNumberPrefix, page)
    const docNumber = createdDecisions[4].documentNumber
    await test.step(`Wähle Dokumentnummer '${docNumber}' in Suche`, async () => {
      await fillInput(page, "Dokumentnummer Suche", docNumber)
    })
    await triggerSearch(page)
    const docUnitSearchResults = createdDecisions.filter(
      (p) => p.documentNumber === docNumber,
    )
    await checkResultListContent(docUnitSearchResults, page)
  })

  test("Suche nach Status", async ({ page }) => {
    await navigateToSearch(page)
    await fillInput(page, "Dokumentnummer Suche", "YYTestDoc001")
    await test.step(`Wähle Status 'Veröffentlicht' in Suche`, async () => {
      await page.getByLabel("Status Suche").click()
      await page
        .getByRole("option", { name: "Veröffentlicht", exact: true })
        .click()
    })
    await triggerSearch(page)
    await test.step(`Prüfe, dass 5 Ergebnisse gefunden wurde`, async () => {
      await expect(page.getByText("5 Ergebnisse gefunden")).toBeVisible()
      //Veröffentlicht Status kann nur übers seeding script generiert werden
      await expect(page.getByText("YYTestDoc0013")).toBeVisible()
    })
    await test.step(`Setze Status-Filter zurück`, async () => {
      await page.getByLabel("Status Suche").click()
      await page
        .getByRole("option", { name: "Nicht ausgewählt", exact: true })
        .click()
    })
    await triggerSearch(page)
    await test.step(`Prüfe, dass 7 Ergebnisse gefunden wurden`, async () => {
      await expect(page.getByText("7 Ergebnisse gefunden")).toBeVisible()
    })
  })

  test("Suche nach Entscheidungsdatum", async ({ page, decisions }) => {
    const { fileNumberPrefix, createdDecisions } = decisions
    await openSearchWithFileNumberPrefix(fileNumberPrefix, page)
    await test.step("Wähle Entscheidungsdatum '02.01.2023' in Suche", async () => {
      await fillInput(page, "Entscheidungsdatum Suche", "02.01.2023")
    })
    await triggerSearch(page)
    const docUnitSearchResultsSpecificDate = createdDecisions.filter(
      (p) => p.coreData.decisionDate === "2023-01-02",
    )
    await checkResultListContent(docUnitSearchResultsSpecificDate, page)

    await test.step("Wähle Entsheidungsdatum Ende '31.01.2023' in Suche", async () => {
      await fillInput(page, "Entscheidungsdatum Suche Ende", "31.01.2023")
    })
    await triggerSearch(page)
    const docUnitSearchResultsDateRange = createdDecisions.filter(
      (p) =>
        p.coreData.decisionDate && p.coreData.decisionDate !== "2023-01-01",
    )
    await checkResultListContent(docUnitSearchResultsDateRange, page)
  })

  test(
    "Suche nach Prozessschritt",
    {
      tag: "@RISDEV-8718",
    },
    async ({ page, decisions }) => {
      const { createdDecisions } = decisions
      await openSearchWithFileNumberPrefix("e2e-", page)
      await page.getByLabel("Nur meine Dokstelle Filter").click()

      // all new doc units are created with step Ersterfassung
      await test.step(`Wähle Schritt 'Ersterfassung'`, async () => {
        await page.getByLabel("Prozessschritt").click()
        await page
          .getByRole("option", { name: "Ersterfassung", exact: true })
          .click()
      })
      await triggerSearch(page)

      await test.step(`Prüfe, dass Ergebnisse gefunden wurde`, async () => {
        await expect(page.getByText("Ergebnisse gefunden")).toBeVisible()
        await expect(
          page.getByText(createdDecisions[0].documentNumber),
        ).toBeVisible()
      })

      await test.step(`Wähle Schritt 'Fertig'`, async () => {
        await page.getByLabel("Prozessschritt").click()
        await page.getByRole("option", { name: "Fertig", exact: true }).click()
      })
      await triggerSearch(page)

      await test.step(`Prüfe, dass keine Ergebnisse gefunden wurde`, async () => {
        await expect(
          page.getByText("Keine Suchergebnisse gefunden"),
        ).toBeVisible()
      })
      await test.step(`Setze Schritt-Filter zurück`, async () => {
        await page.getByLabel("Prozessschritt").click()
        await page
          .getByRole("option", { name: "Nicht ausgewählt", exact: true })
          .click()
      })
      await triggerSearch(page)

      await test.step(`Prüfe, dass alle Ergebnisse gefunden wurden`, async () => {
        await expect(page.getByText("Ergebnisse gefunden")).toBeVisible()
        await expect(
          page.getByText(createdDecisions[0].documentNumber),
        ).toBeVisible()
      })

      await test.step(`Weise einen neuen Prozesschritt ohne Person`, async () => {
        await navigateToCategories(page, createdDecisions[0].documentNumber)

        await page
          .getByRole("button", { name: "Dokumentationseinheit weitergeben" })
          .click()
        const dialog = page.getByRole("dialog")
        await expect(dialog).toBeVisible()
        await expect(
          dialog.getByText("Dokumentationseinheit weitergeben"),
        ).toBeVisible()
        await expect(dialog.getByText("Neuer Schritt")).toBeVisible()
        await expect(dialog.getByText("Fachdokumentation")).toBeVisible()
        await expect(dialog.getByText("Neue Person")).toBeVisible()
        await dialog.getByLabel("Dropdown öffnen").click()

        await dialog
          .getByRole("button", {
            name: "Weitergeben",
          })
          .click()

        await expect(dialog).toBeHidden()
      })

      await test.step(`Prüfe, dass Ergebnisse die zugewiesene Dokumentationseinheit nicht mehr enthalten`, async () => {
        await openSearchWithFileNumberPrefix("e2e-", page)
        await page.getByLabel("Nur meine Dokstelle Filter").click()
        await page.getByLabel("Nur mir zugewiesen").click()
        await expect(page.getByLabel("Nur mir zugewiesen")).toBeChecked()
        await triggerSearch(page)
        await expect(page.getByText("Ergebnisse gefunden")).toBeVisible()
        await expect(
          page.getByText(createdDecisions[0].documentNumber),
        ).toBeHidden()
      })
    },
  )

  test(
    "Suche nach 'Nur mir zugewiesen' und 'Niemandem zugewiesen'",
    {
      tag: "@RISDEV-8718",
    },
    async ({ page, decisions }) => {
      const { createdDecisions } = decisions
      await openSearchWithFileNumberPrefix("e2e-", page)
      await page.getByLabel("Nur meine Dokstelle Filter").click()

      // all new doc units are created with my user assigned
      await test.step("Wähle 'Nur mir zugewiesen'", async () => {
        await page.getByLabel("Nur mir zugewiesen").click()
        await expect(page.getByLabel("Nur mir zugewiesen")).toBeChecked()
        await triggerSearch(page)
      })

      await test.step(`Prüfe, dass alle Ergebnisse gefunden wurde`, async () => {
        await expect(
          page.getByText(createdDecisions.length + " Ergebnisse gefunden"),
        ).toBeVisible()
        await expect(
          page.getByText(createdDecisions[0].documentNumber),
        ).toBeVisible()
      })

      await test.step(`Weise einen neuen Prozesschritt zu, entferne Person`, async () => {
        await navigateToCategories(page, createdDecisions[0].documentNumber)

        await page
          .getByRole("button", { name: "Dokumentationseinheit weitergeben" })
          .click()
        const dialog = page.getByRole("dialog")
        await expect(dialog).toBeVisible()
        await expect(
          dialog.getByText("Dokumentationseinheit weitergeben"),
        ).toBeVisible()
        await expect(dialog.getByText("Neuer Schritt")).toBeVisible()
        await expect(dialog.getByText("Fachdokumentation")).toBeVisible()
        await expect(dialog.getByText("Neue Person")).toBeVisible()
        await dialog.getByLabel("Dropdown öffnen").click()

        await dialog
          .getByRole("button", {
            name: "Weitergeben",
          })
          .click()

        await expect(dialog).toBeHidden()
      })

      await test.step(`Prüfe, dass ein Ergebnis weniger gefunden wurde`, async () => {
        await openSearchWithFileNumberPrefix("e2e-", page)
        await page.getByLabel("Nur meine Dokstelle Filter").click()
        await page.getByLabel("Nur mir zugewiesen").click()
        await expect(page.getByLabel("Nur mir zugewiesen")).toBeChecked()
        await triggerSearch(page)
        await expect(
          page.getByText(createdDecisions.length - 1 + " Ergebnisse gefunden"),
        ).toBeVisible()
        await expect(
          page.getByText(createdDecisions[0].documentNumber),
        ).toBeHidden()
      })

      await test.step("Wähle 'Niemandem zugewiesen', resetted automatisch 'Nur mir zugewiesen'", async () => {
        await page.getByLabel("Niemandem zugewiesen").click()
        await expect(page.getByLabel("Niemandem zugewiesen")).toBeChecked()
        await expect(page.getByLabel("Nur mir zugewiesen")).not.toBeChecked()
        await triggerSearch(page)
      })

      await test.step("Erwarte Dokeinheit mit eben entfernter Person als Suchresultat", async () => {
        await page.getByLabel("Niemandem zugewiesen").click()
        await expect(page.getByLabel("Niemandem zugewiesen")).toBeChecked()
        await expect(page.getByLabel("Nur mir zugewiesen")).not.toBeChecked()
        await triggerSearch(page)
        await expect(page.getByText("1 Ergebnis gefunden")).toBeVisible()
        await expect(
          page.getByText(createdDecisions[0].documentNumber),
        ).toBeVisible()
      })

      await test.step("Wähle 'Nur mir zugewiesen'', resetted automatisch 'Niemandem zugewiesen'", async () => {
        await page.getByLabel("Nur mir zugewiesen").click()
        await expect(page.getByLabel("Nur mir zugewiesen")).toBeChecked()
        await expect(page.getByLabel("Niemandem zugewiesen")).not.toBeChecked()
      })
    },
  )

  test("Datumsvalidierung", async ({ page }) => {
    await navigateToSearch(page)
    await test.step("Wähle Entscheidungsdatum Ende '02.01.2023' in Suche", async () => {
      await fillInput(page, "Entscheidungsdatum Suche Ende", "02.01.2022")
      await page.getByLabel("Entscheidungsdatum Suche Ende").blur()
      await expect(page.getByText("Startdatum fehlt")).toBeVisible()
    })

    await test.step("Fokussieren von Entscheidungsdatum Input lässt Fehlermeldung verschwinden", async () => {
      await page.getByLabel("Entscheidungsdatum Suche", { exact: true }).focus()
      await expect(page.getByText("Startdatum fehlt")).toBeHidden()
    })

    await test.step("Verlasse von Entscheidungsdatum Input ohne Input lässt Fehlermeldung wieder erscheinen", async () => {
      await page.getByLabel("Entscheidungsdatum Suche", { exact: true }).blur()
      await expect(page.getByText("Startdatum fehlt")).toBeVisible()
    })

    await test.step("Enddatum darf nicht vor Startdatum liegen", async () => {
      await fillInput(page, "Entscheidungsdatum Suche", "02.01.2023")
      await expect(page.getByText("Startdatum fehlt")).toBeHidden()
      await expect(
        page.getByText("Enddatum darf nicht vor Startdatum liegen"),
      ).toBeVisible()
    })

    await test.step("Löschen von Startdatum lässt erste Fehlermeldung wieder erscheinen", async () => {
      await page.getByLabel("Entscheidungsdatum Suche", { exact: true }).clear()
      await page.getByLabel("Entscheidungsdatum Suche", { exact: true }).blur()
      await expect(page.getByText("Startdatum fehlt")).toBeVisible()
      await expect(
        page.getByText("Enddatum darf nicht vor Startdatum liegen"),
      ).toBeHidden()
    })

    await test.step("Löschen von Enddatum lässt erste Fehlermeldung wieder verschwinden", async () => {
      await page
        .getByLabel("Entscheidungsdatum Suche Ende", { exact: true })
        .clear()
      await page
        .getByLabel("Entscheidungsdatum Suche Ende", { exact: true })
        .blur()
      await expect(page.getByText("Startdatum fehlt")).toBeHidden()
    })

    await test.step("Datum wird validiert", async () => {
      const firstDate = page.getByLabel("Entscheidungsdatum Suche", {
        exact: true,
      })
      const secondDate = page.getByLabel("Entscheidungsdatum Suche Ende")
      const firstDateInput = page.getByTestId("decision-date-input")
      const secondDateInput = page.getByTestId("decision-date-end-input")

      await firstDate.fill("29.02.2022")
      await expect(firstDateInput.getByText("Kein valides Datum")).toBeVisible()
      await secondDate.fill("29.02.2022")
      await expect(
        secondDateInput.getByText("Kein valides Datum"),
      ).toBeVisible()
    })
  })

  test("Suche zurücksetzen", async ({ page }) => {
    // on input button is visible
    await navigateToSearch(page)
    const resetSearch = page.getByLabel("Suche zurücksetzen")
    const searchTerm = generateString()
    await expect(resetSearch).toBeHidden()
    await page.getByLabel("Aktenzeichen Suche").fill(searchTerm)
    await expect(resetSearch).toBeVisible()
    await triggerSearch(page)

    await expect(page.getByText("Keine Suchergebnisse gefunden")).toBeVisible()
    await resetSearch.click()
    await expect(page.getByText(searchTerm)).toBeHidden()
    await expect(page.getByText("Keine Suchergebnisse gefunden")).toBeHidden()
    await expect(
      page.getByText(
        "Starten Sie die Suche oder erstellen Sie eine neue Dokumentationseinheit.",
      ),
    ).toBeVisible()
  })

  test("URL mit Parametern wird richtig aktualisiert", async ({ page }) => {
    await navigateToSearch(page)

    await fillInput(page, "Entscheidungsdatum Suche", "02.01.2022")

    await triggerSearch(page)

    await expect(page).toHaveURL(/decisionDate=2022-01-02/)
    await page.reload()
    await expect(page).toHaveURL(/decisionDate=2022-01-02/)

    await fillInput(page, "Entscheidungsdatum Suche Ende", "02.01.2023")
    await triggerSearch(page)
    await expect(page).toHaveURL(
      /decisionDate=2022-01-02&decisionDateEnd=2023-01-02/,
    )

    await page.goBack()
    await expect(page).not.toHaveURL(
      /decisionDate=2022-01-02&decisionDateEnd=2023-01-02/,
    )
    await page.reload()
    await expect(page).toHaveURL(/decisionDate=2022-01-02/)
  })

  test("Suche nach fehlerhaften Dokumentationseinheiten", async ({ page }) => {
    await navigateToSearch(page)

    await page.getByLabel("Dokumentnummer Suche").fill("YYTestDoc")

    const docofficeOnly = page.getByLabel("Nur meine Dokstelle Filter")
    await docofficeOnly.click()
    const errorsOnly = page.getByLabel(
      "Nur fehlerhafte Dokumentationseinheiten",
    )
    await errorsOnly.click()
    await triggerSearch(page)

    //3 + table header
    await expect.poll(async () => page.getByRole("row").count()).toBe(4)

    await docofficeOnly.click()
    await expect(errorsOnly).toBeHidden()
  })

  // Todo: replace prefilledDocumentUnit, use dynamic 'decision' fixture instead and fill in place
  test("Spruchkörper wird in Suchergebnissen angezeigt", async ({
    page,
    prefilledDocumentUnit,
  }) => {
    await navigateToSearch(page)

    await fillSearchInput(page, {
      documentNumber: prefilledDocumentUnit.documentNumber,
    })
    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()

    await expect
      .poll(async () =>
        page
          .getByRole("row")
          .getByText(prefilledDocumentUnit.coreData.appraisalBody!)
          .count(),
      )
      .toBeGreaterThanOrEqual(1)
  })

  // Todo: replace prefilledDocumentUnit, use dynamic 'decision' fixture instead and fill in place
  test("exisiting headnote/ guiding principle are indicated as icon", async ({
    page,
    prefilledDocumentUnit,
  }) => {
    await navigateToSearch(page)

    await fillSearchInput(page, {
      documentNumber: prefilledDocumentUnit.documentNumber,
    })
    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
    await expect(page.getByTestId("headnote-principle-icon")).toBeVisible()
  })

  // Todo: replace prefilledDocumentUnit, use dynamic 'decision' fixture instead and fill in place

  test(
    "Existierende Notiz wird in Suchergebnissen angezeigt",
    {
      tag: "@RISDEV-4176",
    },
    async ({ page, prefilledDocumentUnit }) => {
      await test.step("Notiz ausfüllen", async () => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await page.getByRole("button", { name: "Seitenpanel öffnen" }).click()

        await fillInput(page, "Notiz Eingabefeld", noteContent)
        await save(page)
      })

      await test.step("Suchergebnis zeigt an, dass Notiz vorhanden ist", async () => {
        await navigateToSearch(page)

        await fillSearchInput(page, {
          documentNumber: prefilledDocumentUnit.documentNumber,
        })
        await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
        const trimmedNote = noteContent.slice(0, 50) + "..."
        await expect(page.getByLabel(trimmedNote)).toBeVisible()
      })

      await test.step("Lösche Notiz", async () => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber!)

        await fillInput(page, "Notiz Eingabefeld", "")
        await save(page)
      })

      await test.step("Suchergebnis zeigt an, dass keine Notiz vorhanden ist", async () => {
        await navigateToSearch(page)

        await fillSearchInput(page, {
          documentNumber: prefilledDocumentUnit.documentNumber,
        })
        await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
        await expect(page.getByLabel("Keine Notiz vorhanden")).toBeVisible()
      })
    },
  )

  test("Entscheidung neu erstellen und löschen", async ({ page }) => {
    await navigateToSearch(page)
    await test.step("Klicke auf 'Neue Entscheidung'", async () => {
      await page
        .getByRole("button", { name: "Neue Entscheidung" })
        .first()
        .click()
    })
    await test.step("Öffnet Rubriken von neuer Entscheidung", async () => {
      await expect(page).toHaveURL(
        /\/caselaw\/documentunit\/[A-Z0-9]{13}\/attachments$/,
      )
      documentNumberToBeDeleted =
        /caselaw\/documentunit\/(.*)\/attachments/g.exec(
          page.url(),
        )?.[1] as string
      await expect(
        page.getByRole("heading", {
          name: documentNumberToBeDeleted,
        }),
      ).toBeVisible()
    })
    await test.step("Suche nach neuer Dokumentnummer", async () => {
      await navigateToSearch(page)
      await fillInput(page, "Dokumentnummer Suche", documentNumberToBeDeleted)
      await triggerSearch(page)
    })
    await test.step(`Prüfe, dass 1 Ergebnis gefunden wurde`, async () => {
      await expect(page.getByText("1 Ergebnis gefunden")).toBeVisible()
      await expect(page.getByText(documentNumberToBeDeleted!)).toBeVisible()
    })
    await test.step("Lösche Entscheidung", async () => {
      await deleteDocumentUnit(page, documentNumberToBeDeleted!)
    })
    await test.step("Suche nach neuer Dokumentnummer ergibt kein Ergebnis", async () => {
      await navigateToSearch(page)
      await fillInput(page, "Dokumentnummer Suche", documentNumberToBeDeleted)
      await triggerSearch(page)
      await expect(
        page.getByText("Keine Suchergebnisse gefunden"),
      ).toBeVisible()
      documentNumberToBeDeleted = undefined
    })
  })

  test("Neuanlage aus Suche", async ({ page }) => {
    const fileNumber = generateString()
    await navigateToSearch(page)
    await test.step("Suche nach Gericht, Datum und Aktenzeichen", async () => {
      await fillInput(page, "Aktenzeichen Suche", fileNumber)
      await fillInput(page, "Gerichtstyp Suche", "BFH")
      await fillInput(page, "Entscheidungsdatum Suche", "05.07.2022")
      await triggerSearch(page)
    })
    await test.step("Ohne Ergebnisse kann Neuanlage aus Suchparametern erfolgen", async () => {
      await expect(
        page.getByText("folgenden Formaldaten übernehmen"),
      ).toBeVisible()
      await expect(
        page.getByText(`${fileNumber}, BFH, 05.07.2022`),
      ).toBeVisible()
    })
    await test.step("Klicke 'Übernehmen und fortfahren'", async () => {
      await page
        .getByRole("button", { name: "Übernehmen und fortfahren" })
        .click()
    })
    await test.step("Öffnet Rubriken von neuer Entscheidung", async () => {
      await expect(page).toHaveURL(
        /\/caselaw\/documentunit\/[A-Z0-9]{13}\/categories$/,
      )
      documentNumberToBeDeleted =
        /caselaw\/documentunit\/(.*)\/categories/g.exec(
          page.url(),
        )?.[1] as string
      await expect(
        page.getByRole("heading", {
          name: documentNumberToBeDeleted,
        }),
      ).toBeVisible()
    })
    await test.step("Rubriken sind bereits mit Suchparametern befüllt", async () => {
      await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
        "BFH",
      )
      await expect(
        page.getByRole("textbox", {
          name: "Entscheidungsdatum",
          exact: true,
        }),
      ).toHaveValue("05.07.2022")
      await expect(
        page
          .getByTestId("chips-input-wrapper_fileNumber")
          .getByTestId("chip-value"),
      ).toHaveText(fileNumber)
    })
  })
})
