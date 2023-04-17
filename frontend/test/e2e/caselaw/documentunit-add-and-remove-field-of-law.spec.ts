import { expect } from "@playwright/test"
import { navigateToCategories, toggleFieldOfLawSection } from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("Add and remove field of to a document unit", () => {
  test("rendering", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await expect(page.getByText("Sachgebiete")).toBeVisible()
    await toggleFieldOfLawSection(page)

    await expect(page.getByRole("heading", { name: "Auswahl" })).toBeVisible()
    await expect(page.getByText("Die Liste ist aktuell leer")).toBeVisible()
    await expect(page.getByText("Normen anzeigen")).toBeVisible()
    await expect(
      page.getByRole("heading", { name: "Sachgebietsbaum" })
    ).toBeVisible()
    await expect(page.getByText("Alle Sachgebiete anzeigen")).toBeVisible()
  })

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
})
