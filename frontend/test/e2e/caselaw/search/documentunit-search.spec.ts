import { expect } from "@playwright/test"
import dayjs from "dayjs"
import { generateString } from "../../../test-helper/dataGenerators"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

test.describe("search", () => {
  test("renders search entry form", async ({ page }) => {
    await page.goto("/")
    await expect(
      page.getByRole("button", {
        name: "Neue Dokumentationseinheit",
        exact: true,
      }),
    ).toBeVisible()

    await expect(
      page.getByRole("heading", { name: "Übersicht Rechtsprechung" }),
    ).toBeVisible()

    await expect(
      page.getByLabel("Dokumentnummer oder Aktenzeichen Suche"),
    ).toBeVisible()
    await expect(page.getByLabel("Gerichtstyp Suche")).toBeVisible()
    await expect(page.getByLabel("Gerichtsort Suche")).toBeVisible()
    await expect(
      page.getByLabel("Entscheidungsdatum Suche", {
        exact: true,
      }),
    ).toBeVisible()
    await expect(
      page.getByLabel("Entscheidungsdatum Suche Ende", {
        exact: true,
      }),
    ).toBeVisible()
    await expect(page.getByLabel("Status Suche")).toBeVisible()
    await expect(page.getByLabel("Nur meine Dokstelle Filter")).toBeVisible()

    await expect(
      page.getByText(
        "Starten Sie die Suche oder erstellen Sie eine neue Dokumentationseinheit.",
      ),
    ).toBeVisible()
  })

  // Suchzustände
  test("renders search results and updates states correctly", async ({
    page,
    documentNumber,
  }) => {
    await page.goto("/")

    //initial state
    await expect(
      page.getByText(
        "Starten Sie die Suche oder erstellen Sie eine neue Dokumentationseinheit.",
      ),
    ).toBeVisible()
    await expect(
      page.getByRole("button", {
        name: "Neue Dokumentationseinheit erstellen",
      }),
    ).toBeVisible()

    //loading
    await page.getByLabel("Nur meine Dokstelle Filter").click()
    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()

    await expect(page.getByLabel("Ladestatus")).toBeVisible()

    //results
    await page
      .getByLabel("Dokumentnummer oder Aktenzeichen Suche")
      .fill(documentNumber)
    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
    //TODO: remove the timeout when search performance get better
    await expect(
      page.locator(".table-row", {
        hasText: documentNumber,
      }),
    ).toBeVisible({ timeout: 30000 })

    //no results
    await page
      .getByLabel("Dokumentnummer oder Aktenzeichen Suche")
      .fill("wrong document number")
    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
    //TODO: remove the timeout when search performance get better
    await expect(
      page.locator(".table-row", {
        hasText: documentNumber,
      }),
    ).toBeHidden({ timeout: 30000 })

    //error
    await page.getByLabel("Nur meine Dokstelle Filter").click()
    await page.route("**/*", async (route) => {
      await route.abort("internetdisconnected")
    })
    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()

    await expect(page.getByLabel("Infomodal")).toBeVisible()
    await expect(
      page.getByText("Die Suchergebnisse konnten nicht geladen werden."),
    ).toBeVisible()
  })

  test("starting search with all kinds of errors or no search parameters not possible", async ({
    page,
  }) => {
    await page.goto("/")
    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
    await expect(
      page.getByText("Geben Sie mindestens ein Suchkriterium ein"),
    ).toBeVisible()

    await page
      .getByLabel("Entscheidungsdatum Suche", { exact: true })
      .fill("28.02.2022")
    await page.getByLabel("Entscheidungsdatum Suche Ende").fill("29.02.2023")
    await expect(page.getByText("Kein valides Datum")).toBeVisible()
    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
    await expect(page.getByText("Fehler in Suchkriterien")).toBeVisible()

    await page.getByLabel("Entscheidungsdatum Suche Ende").fill("28.02.2023")
    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
    await expect(page.getByText("Fehler in Suchkriterien")).toBeHidden()
    await expect(page.getByLabel("Ladestatus")).toBeVisible()
  })

  // Datumskomponente Zeitraum
  test("search for exact dates", async ({
    page,
    prefilledDocumentUnit,
    secondPrefilledDocumentUnit,
  }) => {
    //1st date input provided: display results matching exactly this date.
    await page.goto("/")

    await page
      .getByLabel("Entscheidungsdatum Suche", { exact: true })
      .fill(
        dayjs(prefilledDocumentUnit.coreData.decisionDate).format("DD.MM.YYYY"),
      )
    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()

    await expect(page.getByLabel("Ladestatus")).toBeHidden({ timeout: 30000 })
    expect(
      await page
        .locator(".table-row", {
          hasText: dayjs(prefilledDocumentUnit.coreData.decisionDate).format(
            "DD.MM.YYYY",
          ),
        })
        .count(),
    ).toBeGreaterThanOrEqual(1)

    expect(
      await page
        .locator(".table-row", {
          hasText: dayjs(
            secondPrefilledDocumentUnit.coreData.decisionDate,
          ).format("DD.MM.YYYY"),
        })
        .count(),
    ).toBe(0)

    await page
      .getByLabel("Entscheidungsdatum Suche", { exact: true })
      .fill(
        dayjs(secondPrefilledDocumentUnit.coreData.decisionDate).format(
          "DD.MM.YYYY",
        ),
      )
    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
    await expect(page.getByLabel("Ladestatus")).toBeHidden({ timeout: 30000 })

    expect(
      await page
        .locator(".table-row", {
          hasText: dayjs(
            secondPrefilledDocumentUnit.coreData.decisionDate,
          ).format("DD.MM.YYYY"),
        })
        .count(),
    ).toBeGreaterThanOrEqual(1)

    expect(
      await page
        .locator(".table-row", {
          hasText: dayjs(prefilledDocumentUnit.coreData.decisionDate).format(
            "DD.MM.YYYY",
          ),
        })
        .count(),
    ).toBe(0)
  })

  test("search results between two dates", async ({
    page,
    prefilledDocumentUnit,
    secondPrefilledDocumentUnit,
  }) => {
    //Both inputs provided: display results matching the date range.
    await page.goto("/")

    await page
      .getByLabel("Entscheidungsdatum Suche", { exact: true })
      .fill(
        dayjs(prefilledDocumentUnit.coreData.decisionDate).format("DD.MM.YYYY"),
      )

    await page
      .getByLabel("Entscheidungsdatum Suche Ende", { exact: true })
      .fill(
        dayjs(secondPrefilledDocumentUnit.coreData.decisionDate).format(
          "DD.MM.YYYY",
        ),
      )
    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
    await expect(page.getByLabel("Ladestatus")).toBeHidden({ timeout: 30000 })

    //TODO: add this line again when sorting by asc desc date in possible
    // expect(
    //   await page
    //     .locator(".table-row", {
    //       hasText: dayjs(prefilledDocumentUnit.coreData.decisionDate).format(
    //         "DD.MM.YYYY",
    //       ),
    //     })
    //     .count(),
    // ).toBeGreaterThanOrEqual(1)
    expect(
      await page
        .locator(".table-row", {
          hasText: dayjs(
            secondPrefilledDocumentUnit.coreData.decisionDate,
          ).format("DD.MM.YYYY"),
        })
        .count(),
    ).toBeGreaterThanOrEqual(1)

    //Same date entered: Show results for the exact date
    await page
      .getByLabel("Entscheidungsdatum Suche Ende", { exact: true })
      .fill(
        dayjs(prefilledDocumentUnit.coreData.decisionDate).format("DD.MM.YYYY"),
      )

    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
    await expect(page.getByLabel("Ladestatus")).toBeHidden({ timeout: 30000 })

    expect(
      await page
        .locator(".table-row", {
          hasText: dayjs(prefilledDocumentUnit.coreData.decisionDate).format(
            "DD.MM.YYYY",
          ),
        })
        .count(),
    ).toBeGreaterThanOrEqual(1)
    expect(
      await page
        .locator(".table-row", {
          hasText: dayjs(
            secondPrefilledDocumentUnit.coreData.decisionDate,
          ).format("DD.MM.YYYY"),
        })
        .count(),
    ).toBe(0)
  })

  test("displaying errors on focus and blur", async ({
    page,
    prefilledDocumentUnit,
    secondPrefilledDocumentUnit,
  }) => {
    await page.goto("/")

    await page
      .getByLabel("Entscheidungsdatum Suche Ende")
      .fill(
        dayjs(prefilledDocumentUnit.coreData.decisionDate).format("DD.MM.YYYY"),
      )

    await page.getByLabel("Entscheidungsdatum Suche Ende").blur()
    await expect(page.getByText("Startdatum fehlt")).toBeVisible()

    await page.getByLabel("Entscheidungsdatum Suche", { exact: true }).focus()
    await expect(page.getByText("Startdatum fehlt")).toBeHidden()

    //on blur without changes error reappears
    await page.getByLabel("Entscheidungsdatum Suche", { exact: true }).blur()
    await expect(page.getByText("Startdatum fehlt")).toBeVisible()

    await page
      .getByLabel("Entscheidungsdatum Suche", { exact: true })
      .fill(
        dayjs(secondPrefilledDocumentUnit.coreData.decisionDate).format(
          "DD.MM.YYYY",
        ),
      )

    await expect(page.getByText("Startdatum fehlt")).toBeHidden()
    await expect(
      page.getByText("Enddatum darf nich vor Startdatum liegen"),
    ).toBeVisible()

    await page.getByLabel("Entscheidungsdatum Suche", { exact: true }).clear()
    await expect(page.getByText("Startdatum fehlt")).toBeVisible()
    await expect(
      page.getByText("Enddatum darf nich vor Startdatum liegen"),
    ).toBeHidden()
  })

  test("updating of date input errors and interdependent errors", async ({
    page,
  }) => {
    await page.goto("/")

    const firstDate = page.getByLabel("Entscheidungsdatum Suche", {
      exact: true,
    })
    const secondDate = page.getByLabel("Entscheidungsdatum Suche Ende")
    const firstDateInput = page.getByTestId("decisionDateInput")
    const secondDateInput = page.getByTestId("decisionDateEndInput")

    await firstDate.fill("29.02.2022")
    await expect(firstDateInput.getByText("Kein valides Datum")).toBeVisible()
    await secondDate.fill("29.02.2022")
    await expect(secondDateInput.getByText("Kein valides Datum")).toBeVisible()

    await secondDate.clear()
    await secondDate.fill("28.02.2022")

    // no valid error "wins" against missing start date error
    await expect(firstDateInput.getByText("Kein valides Datum")).toBeVisible()
    await firstDate.clear()
    await expect(page.getByText("Startdatum fehlt")).toBeVisible()
    await firstDate.fill("28.02.2023")
    await expect(
      secondDateInput.getByText("Enddatum darf nich vor Startdatum liegen"),
    ).toBeVisible()

    // no valid error "wins" against range error
    await secondDate.clear()
    await secondDate.fill("29.02.2022")
    await expect(secondDateInput.getByText("Kein valides Datum")).toBeVisible()
  })

  test("search for status", async ({ pageWithBghUser }) => {
    await pageWithBghUser.goto("/")

    const docofficeOnly = pageWithBghUser.getByLabel(
      "Nur meine Dokstelle Filter",
    )
    await docofficeOnly.click()

    await pageWithBghUser
      .getByLabel("Nach Dokumentationseinheiten suchen")
      .click()

    //TODO: remove the timeout when search performance get better
    await expect(pageWithBghUser.getByLabel("Ladestatus")).toBeHidden({
      timeout: 30000,
    })

    expect(
      await pageWithBghUser.getByText("unveröffentlicht").count(),
    ).toBeGreaterThanOrEqual(1)

    const select = pageWithBghUser.locator(`select[id="status"]`)
    await select.selectOption("Veröffentlicht")

    await pageWithBghUser
      .getByLabel("Nach Dokumentationseinheiten suchen")
      .click()

    //TODO: remove the timeout when search performance get better
    await expect(pageWithBghUser.getByLabel("Ladestatus")).toBeHidden({
      timeout: 30000,
    })

    expect(
      await pageWithBghUser.getByText("veröffentlicht").count(),
    ).toBeGreaterThanOrEqual(1)

    await expect(pageWithBghUser.getByText("unveröffentlicht")).toBeHidden()
  })

  // Filtern auf Fehler
  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("filter for documentunits with errors only", async ({
    pageWithBghUser,
  }) => {
    await pageWithBghUser.goto("/")

    const docofficeOnly = pageWithBghUser.getByLabel(
      "Nur meine Dokstelle Filter",
    )
    const errorsOnly = pageWithBghUser.getByLabel(
      "Nur fehlerhafte Dokumentationseinheiten",
    )

    const select = pageWithBghUser.locator(`select[id="status"]`)
    await select.selectOption("Veröffentlicht")
    await pageWithBghUser
      .getByLabel("Nach Dokumentationseinheiten suchen")
      .click()

    //TODO: remove the timeout when search performance get better
    await expect(pageWithBghUser.getByLabel("Ladestatus")).toBeHidden({
      timeout: 30000,
    })

    expect(
      await pageWithBghUser.getByText("XXRE").count(),
    ).toBeGreaterThanOrEqual(1)

    await select.selectOption("Alle")
    await docofficeOnly.click()

    await pageWithBghUser
      .getByLabel("Nach Dokumentationseinheiten suchen")
      .click()

    //TODO: remove the timeout when search performance get better
    await expect(pageWithBghUser.getByLabel("Ladestatus")).toBeHidden({
      timeout: 30000,
    })
    expect(
      await pageWithBghUser.getByText("KORE").count(),
    ).toBeGreaterThanOrEqual(1)

    await expect(pageWithBghUser.getByText("XXRE")).toBeHidden({
      timeout: 30000,
    })

    await errorsOnly.click()
    await pageWithBghUser
      .getByLabel("Nach Dokumentationseinheiten suchen")
      .click()

    //TODO: remove the timeout when search performance get better
    await expect(pageWithBghUser.getByLabel("Ladestatus")).toBeHidden({
      timeout: 30000,
    })
    expect(
      await pageWithBghUser.getByText("Nicht veröffentlicht (Fehler)").count(),
    ).toBeGreaterThanOrEqual(1)

    await docofficeOnly.click()
    await expect(errorsOnly).toBeHidden()

    await pageWithBghUser
      .getByLabel("Nach Dokumentationseinheiten suchen")
      .click()

    //TODO: remove the timeout when search performance get better
    await expect(pageWithBghUser.getByLabel("Ladestatus")).toBeHidden({
      timeout: 30000,
    })

    expect(
      await pageWithBghUser.getByText("unveröffentlicht").count(),
    ).toBeGreaterThanOrEqual(1)

    //unclick my dokstelle should also reset errors only filter
    await docofficeOnly.click()
    await expect(errorsOnly).not.toBeChecked()
  })

  // Suche zurücksetzen
  test("resetting the search", async ({ page }) => {
    // on input button is visible
    await page.goto("/")
    const resetSearch = page.getByLabel("Suche zurücksetzen")
    const searchTerm = generateString()
    await expect(resetSearch).toBeHidden()
    await page
      .getByLabel("Dokumentnummer oder Aktenzeichen Suche")
      .fill(searchTerm)
    await expect(resetSearch).toBeVisible()
    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()

    //TODO: remove the timeout when search performance get better
    await expect(page.getByLabel("Ladestatus")).toBeHidden({
      timeout: 30000,
    })
    await expect(page.getByText("Keine Ergebnisse")).toBeVisible()
    await resetSearch.click()
    await expect(page.getByText("123")).toBeHidden()
    await expect(page.getByText("Keine Ergebnisse")).toBeHidden()
    await expect(
      page.getByText(
        "Starten Sie die Suche oder erstellen Sie eine neue Dokumentationseinheit.",
      ),
    ).toBeVisible()
    await expect(resetSearch).toBeHidden()
    await page.getByLabel("Dokumentnummer oder Aktenzeichen Suche").fill("123")
    await expect(resetSearch).toBeVisible()
    await page.getByLabel("Dokumentnummer oder Aktenzeichen Suche").clear()
    await expect(resetSearch).toBeHidden()
  })
})
