import { expect } from "@playwright/test"
import errorMessages from "@/i18n/errors.json"

import {
  navigateToCategories,
  toggleFieldOfLawSection,
  save,
} from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

test.describe("field of law", () => {
  test("rendering", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await expect(page.getByText("Sachgebiete")).toBeVisible()
    await toggleFieldOfLawSection(page)

    await expect(
      page.getByRole("heading", { name: "Ausgewählte Sachgebiete" }),
    ).toBeVisible()
    await expect(page.getByText("Die Liste ist aktuell leer")).toBeVisible()
    await expect(page.getByText("Normen anzeigen")).toBeVisible()
    await expect(
      page.getByRole("heading", { name: "Sachgebietsbaum" }),
    ).toBeVisible()
    await expect(page.getByText("Alle Sachgebiete")).toBeVisible()
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
        name: "Alle Sachgebiete aufklappen",
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
        name: "Alle Sachgebiete aufklappen",
      })
      .click()

    await expect(page.getByText("Strafrecht", { exact: true })).toBeVisible()
    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeHidden()

    await page.getByRole("button", { name: "Strafrecht aufklappen" }).click()

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
        name: "Alle Sachgebiete aufklappen",
      })
      .click()

    await expect(page.getByText("Strafrecht", { exact: true })).toBeVisible()
    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeHidden()

    await page.getByRole("button", { name: "Strafrecht aufklappen" }).click()

    await expect(page.getByText("Strafrecht", { exact: true })).toBeVisible()
    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeVisible()

    await page
      .getByRole("button", {
        name: "Alle Sachgebiete einklappen",
      })
      .click()

    await expect(page.getByText("Strafrecht")).toBeHidden()
    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeHidden()

    await page
      .getByRole("button", {
        name: "Alle Sachgebiete aufklappen",
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
        name: "Alle Sachgebiete aufklappen",
      })
      .click()

    await expect(page.getByText("Strafrecht", { exact: true })).toBeVisible()

    await page.getByRole("button", { name: "Strafrecht aufklappen" }).click()

    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeVisible()

    await page.getByLabel("SR-07 Ordnungswidrigkeitenrecht hinzufügen").click()

    await expect(page.getByText("Die Liste ist aktuell leer")).toBeHidden()
    await expect(
      page.getByLabel(
        "SR-07 Ordnungswidrigkeitenrecht im Sachgebietsbaum anzeigen",
      ),
    ).toBeVisible()
    await expect(
      page.getByLabel("SR-07 Ordnungswidrigkeitenrecht entfernen"),
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
        name: "Alle Sachgebiete aufklappen",
      })
      .click()

    await expect(page.getByText("Strafrecht", { exact: true })).toBeVisible()

    await page.getByRole("button", { name: "Strafrecht aufklappen" }).click()

    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeVisible()

    await page.getByLabel("SR-07 Ordnungswidrigkeitenrecht hinzufügen").click()

    await expect(
      page.getByLabel("SR-07 Ordnungswidrigkeitenrecht entfernen"),
    ).toBeVisible()

    await page.getByLabel("SR-07 Ordnungswidrigkeitenrecht entfernen").click()

    await expect(page.getByText("Die Liste ist aktuell leer")).toBeVisible()
    await expect(
      page.getByLabel("SR-07 Ordnungswidrigkeitenrecht hinzufügen"),
    ).toBeVisible()
  })
  test("opening and closing tree nodes, nodes with no children do not display expand icon", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleFieldOfLawSection(page)

    await page
      .getByRole("button", {
        name: "Alle Sachgebiete aufklappen",
      })
      .click()

    await expect(page.getByLabel("Strafrecht aufklappen")).toBeVisible()

    await page.getByRole("button", { name: "Strafrecht aufklappen" }).click()

    await expect(page.getByLabel("Strafrecht einklappen")).toBeVisible()

    await expect(
      page.getByLabel("Ordnungswidrigkeitenrecht aufklappen"),
    ).toBeVisible()

    await page
      .getByRole("button", { name: "Ordnungswidrigkeitenrecht aufklappen" })
      .click()

    await expect(
      page.getByLabel("Ordnungswidrigkeitenrecht einklappen"),
    ).toBeVisible()

    await page.getByRole("button", { name: "OWi-Verfahren aufklappen" }).click()

    //last layer: nodes with no children do not display expand icon

    await expect(
      page.getByText("Zuständigkeiten", {
        exact: true,
      }),
    ).toBeVisible()

    await expect(page.getByLabel("Zuständigkeiten aufklappen")).toBeHidden()

    //toggling again closes child nodes tree

    await page.getByRole("button", { name: "OWi-Verfahren einklappen" }).click()

    await expect(
      page.getByText("Zuständigkeiten", {
        exact: true,
      }),
    ).toBeHidden()

    await expect(page.getByLabel("OWi-Verfahren aufklappen")).toBeVisible()
  })

  test("open 'Strafrecht' - tree and add 'Ordnungswidrigkeitenrecht', remove it in the selection list", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleFieldOfLawSection(page)

    await page
      .getByRole("button", {
        name: "Alle Sachgebiete aufklappen",
      })
      .click()

    await expect(page.getByText("Strafrecht", { exact: true })).toBeVisible()

    await page.getByRole("button", { name: "Strafrecht aufklappen" }).click()

    await expect(page.getByText("Ordnungswidrigkeitenrecht")).toBeVisible()

    await page.getByLabel("SR-07 Ordnungswidrigkeitenrecht hinzufügen").click()

    await expect(page.getByText("Die Liste ist aktuell leer")).toBeHidden()

    await page.getByLabel("SR-07 Ordnungswidrigkeitenrecht entfernen").click()

    await expect(page.getByText("Die Liste ist aktuell leer")).toBeVisible()
    await expect(
      page.getByLabel("SR-07 Ordnungswidrigkeitenrecht hinzufügen"),
    ).toBeVisible()
  })

  // Search

  test("Search without results", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await toggleFieldOfLawSection(page)

    await page.locator("[aria-label='Sachgebiete Suche']").fill("xyz")
    await page.keyboard.press("Enter")
    await expect(
      page.getByText(errorMessages.SEARCH_RESULTS_NOT_FOUND.title),
    ).toBeVisible()
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

    await expect(page.getByText("Seite 1")).toBeVisible()
    await expect(
      page.getByRole("button", { name: "vorherige Ergebnisse" }),
    ).toBeDisabled()

    await page
      .getByRole("button", {
        name: "nächste Ergebnisse",
      })
      .click()

    await expect(page.getByText("Seite 2")).toBeVisible()
    await expect(
      page.getByRole("button", { name: "vorherige Ergebnisse" }),
    ).toBeEnabled()

    await page
      .getByRole("button", {
        name: "nächste Ergebnisse",
      })
      .click()

    // we expect between 20 and 30 results (3 pages)
    await expect(
      page.getByRole("button", { name: "nächste Ergebnisse" }),
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
      page.getByText("Person, Sache, Willenserklärung, Vertrag, IPR"),
    ).toBeVisible()
  })

  test("Search with paginated results - click on result opens tree in direct path mode to selected result and can be saved", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleFieldOfLawSection(page)

    await page.locator("[aria-label='Sachgebiete Suche']").fill("Grundstück")
    await page.keyboard.press("Enter")

    const searchResult = page.getByLabel(
      "BR-01-06-05 Grundstückskaufvertrag im Sachgebietsbaum anzeigen",
    )
    await expect(searchResult).toBeVisible()
    await searchResult.click()

    const searchResultInTree = page.getByLabel(
      "BR-01-06-05 Grundstückskaufvertrag hinzufügen",
    )
    await expect(searchResultInTree).toBeVisible()
    await searchResultInTree.click()

    await save(page)
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
        "BR-05-01-06 entgangener Gewinn im Sachgebietsbaum anzeigen",
      ),
    ).toBeVisible()

    // if this is visible, it means that the "Normen anzeigen" checkbox got set to true
    await expect(page.getByText("§ 251 BGB").first()).toBeVisible()
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
        "AR-01 Arbeitsvertrag: Abschluss, Klauseln, Arten, Betriebsübergang im Sachgebietsbaum anzeigen",
      ),
    ).toBeVisible()
  })
})
