import { expect } from "@playwright/test"
import {
  navigateToCategories,
  toggleFieldOfLawSection,
} from "~/e2e/caselaw/e2e-utils"
import { testWithDocumentUnit as test } from "~/e2e/caselaw/fixtures"

test.describe("field of law", () => {
  test("rendering", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await expect(page.getByText("Sachgebiete")).toBeVisible()
    await toggleFieldOfLawSection(page)

    await expect(
      page.getByRole("heading", { name: "Ausgewählte Sachgebiete" })
    ).toBeVisible()
    await expect(page.getByText("Die Liste ist aktuell leer")).toBeVisible()
    await expect(page.getByText("Normen anzeigen")).toBeVisible()
    await expect(
      page.getByRole("heading", { name: "Sachgebietsbaum" })
    ).toBeVisible()
    await expect(page.getByText("Alle Sachgebiete anzeigen")).toBeVisible()
    await expect(page.getByRole("heading", { name: "Suche" })).toBeVisible()
    await expect(page.getByText("Direkteingabe Sachgebiet")).toBeVisible()
  })

  // Tree and selection list

  test("click on root element in 'fields of law'-tree open level one", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleFieldOfLawSection(page)

    await page
      .getByRole("button", {
        name: "root Alle Sachgebiete anzeigen aufklappen",
      })
      .click()

    await expect(page.getByText("Strafrecht", { exact: true })).toBeVisible()
    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeHidden()
  })

  test("click on root element in 'fields of law'-tree and on level one on 'Strafrecht'", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleFieldOfLawSection(page)

    await page
      .getByRole("button", {
        name: "root Alle Sachgebiete anzeigen aufklappen",
      })
      .click()

    await expect(page.getByText("Strafrecht", { exact: true })).toBeVisible()
    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeHidden()

    await page.getByRole("button", { name: "SR Strafrecht aufklappen" }).click()

    await expect(page.getByText("Strafrecht", { exact: true })).toBeVisible()
    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeVisible()
  })

  test("click on root element in 'fields of law'-tree and on level one on 'Strafrecht, close and reopen'", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleFieldOfLawSection(page)

    await page
      .getByRole("button", {
        name: "root Alle Sachgebiete anzeigen aufklappen",
      })
      .click()

    await expect(page.getByText("Strafrecht", { exact: true })).toBeVisible()
    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeHidden()

    await page.getByRole("button", { name: "SR Strafrecht aufklappen" }).click()

    await expect(page.getByText("Strafrecht", { exact: true })).toBeVisible()
    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeVisible()

    await page
      .getByRole("button", {
        name: "root Alle Sachgebiete anzeigen aufklappen",
      })
      .click()

    await expect(page.getByText("Strafrecht")).toBeHidden()
    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeHidden()

    await page
      .getByRole("button", {
        name: "root Alle Sachgebiete anzeigen aufklappen",
      })
      .click()

    await expect(page.getByText("Strafrecht", { exact: true })).toBeVisible()
    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeVisible()
  })

  test("open 'Strafrecht' - tree and add 'Ordnungswidrigkeitenrecht'", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleFieldOfLawSection(page)

    await page
      .getByRole("button", {
        name: "root Alle Sachgebiete anzeigen aufklappen",
      })
      .click()

    await expect(page.getByText("Strafrecht", { exact: true })).toBeVisible()

    await page.getByRole("button", { name: "SR Strafrecht aufklappen" }).click()

    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeVisible()

    await page
      .getByRole("button", {
        name: "SR-07 Ordnungswidrigkeitenrecht hinzufügen",
      })
      .click()

    await expect(page.getByText("Die Liste ist aktuell leer")).toBeHidden()
    await expect(
      page.getByLabel(
        "SR-07 Ordnungswidrigkeitenrecht im Sachgebietsbaum anzeigen"
      )
    ).toBeVisible()
    await expect(
      page
        .getByRole("button", {
          name: "SR-07 Ordnungswidrigkeitenrecht entfernen",
        })
        .filter({ hasText: "delete_outline" })
    ).toBeVisible()
    await expect(
      page
        .getByRole("button", {
          name: "SR-07 Ordnungswidrigkeitenrecht entfernen",
        })
        .filter({ hasText: "done" })
    ).toBeVisible()
  })

  test("open 'Strafrecht' - tree and add 'Ordnungswidrigkeitenrecht', remove it in the tree", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleFieldOfLawSection(page)

    await page
      .getByRole("button", {
        name: "root Alle Sachgebiete anzeigen aufklappen",
      })
      .click()

    await expect(page.getByText("Strafrecht", { exact: true })).toBeVisible()

    await page.getByRole("button", { name: "SR Strafrecht aufklappen" }).click()

    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeVisible()

    await page
      .getByRole("button", {
        name: "SR-07 Ordnungswidrigkeitenrecht hinzufügen",
      })
      .click()

    await expect(
      page
        .getByRole("button", {
          name: "SR-07 Ordnungswidrigkeitenrecht entfernen",
        })
        .filter({ hasText: "done" })
    ).toBeVisible()

    await page
      .getByRole("button", {
        name: "SR-07 Ordnungswidrigkeitenrecht entfernen",
      })
      .filter({ hasText: "done" })
      .click()

    await expect(page.getByText("Die Liste ist aktuell leer")).toBeVisible()
    await expect(
      page.getByRole("button", {
        name: "SR-07 Ordnungswidrigkeitenrecht hinzufügen",
      })
    ).toBeVisible()
  })

  test("open 'Strafrecht' - tree and add 'Ordnungswidrigkeitenrecht', remove it in the selection list", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleFieldOfLawSection(page)

    await page
      .getByRole("button", {
        name: "root Alle Sachgebiete anzeigen aufklappen",
      })
      .click()

    await expect(page.getByText("Strafrecht", { exact: true })).toBeVisible()

    await page.getByRole("button", { name: "SR Strafrecht aufklappen" }).click()

    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeVisible()

    await page
      .getByRole("button", {
        name: "SR-07 Ordnungswidrigkeitenrecht hinzufügen",
      })
      .click()

    await expect(page.getByText("Die Liste ist aktuell leer")).toBeHidden()

    await page
      .getByRole("button", {
        name: "SR-07 Ordnungswidrigkeitenrecht entfernen",
      })
      .filter({ hasText: "delete_outline" })
      .click()

    await expect(page.getByText("Die Liste ist aktuell leer")).toBeVisible()
    await expect(
      page.getByRole("button", {
        name: "SR-07 Ordnungswidrigkeitenrecht hinzufügen",
      })
    ).toBeVisible()
  })

  // Search

  test("Search without results", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await toggleFieldOfLawSection(page)

    await page.locator("[aria-label='Sachgebiete Suche']").fill("xyz")
    await page.keyboard.press("Enter")
    await expect(page.getByText("Total 0 Items")).toBeVisible()
  })

  test("Search with paginated results - test the pagination navigation", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleFieldOfLawSection(page)

    await page.locator("[aria-label='Sachgebiete Suche']").fill("Grundstück")

    await page
      .getByRole("button", {
        name: "Sachgebietssuche ausführen",
      })
      .click()

    await expect(page.getByText("1 von 3")).toBeVisible()
    await expect(
      page.getByRole("button", { name: "vorherige Ergebnisse" })
    ).toBeDisabled()

    await page
      .getByRole("button", {
        name: "nächste Ergebnisse",
      })
      .click()

    await expect(page.getByText("2 von 3")).toBeVisible()
    await expect(
      page.getByRole("button", { name: "vorherige Ergebnisse" })
    ).toBeEnabled()

    await page
      .getByRole("button", {
        name: "nächste Ergebnisse",
      })
      .click()

    await expect(page.getByText("3 von 3")).toBeVisible()
    await expect(
      page.getByRole("button", { name: "nächste Ergebnisse" })
    ).toBeDisabled()
  })

  test("Search with paginated results - first result to open in tree", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleFieldOfLawSection(page)

    await page.locator("[aria-label='Sachgebiete Suche']").fill("Grundstück")
    await page.keyboard.press("Enter")

    // if these two are visible, it must mean that the tree opened automatically with the first result
    await expect(page.getByText("Bürgerliches Recht")).toBeVisible()
    await expect(
      page.getByText("Fallgruppen der Leistungskondiktion")
    ).toBeVisible()
  })

  test("Search with both norm string and stext string - sets show norm checkbox to true", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleFieldOfLawSection(page)

    await page
      .locator("[aria-label='Sachgebiete Suche']")
      .fill('norm:"§ 252 BGB" Gewinn')
    await page.keyboard.press("Enter")

    await expect(
      page.getByLabel(
        "BR-05-01-06 entgangener Gewinn im Sachgebietsbaum anzeigen"
      )
    ).toBeVisible()

    // if this is visible, it means that the "Normen anzeigen" checkbox got set to true
    await expect(page.getByText("§ 251 BGB")).toBeVisible()
  })

  // Direct input

  test("Direct input - search and choose item", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleFieldOfLawSection(page)

    await page
      .locator("[aria-label='Direkteingabe-Sachgebietssuche eingeben']")
      .fill("AR")

    // if this is visible, it means that the dropdown opened with the search results
    await expect(page.getByText("Abschluss")).toBeVisible()

    await page.getByText("Abschluss").click()

    // it was added to the selection list
    await expect(
      page.getByLabel(
        "AR-01 Arbeitsvertrag: Abschluss, Klauseln, Arten, Betriebsübergang im Sachgebietsbaum anzeigen"
      )
    ).toBeVisible()
  })
})
