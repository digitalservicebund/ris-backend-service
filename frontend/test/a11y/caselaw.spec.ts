import { expect } from "@playwright/test"
import { checkA11y, injectAxe } from "axe-playwright"
import {
  navigateToCategories,
  navigateToFiles,
  navigateToPublication,
  clickSaveButton,
  togglePreviousDecisionsSection,
  fillPreviousDecisionInputs,
  uploadTestfile,
} from "../e2e/caselaw/e2e-utils"
import { testWithDocumentUnit as test } from "../e2e/caselaw/fixtures"

test.describe("a11y of start page (/caselaw)", () => {
  test("documentUnit list", async ({ page }) => {
    await page.goto("/")
    await expect(page.locator("text=Neue Dokumentationseinheit")).toBeVisible()
    await injectAxe(page)
    await checkA11y(page)
  })

  test("delete documentUnit popup", async ({ page, documentNumber }) => {
    await page.goto("/")
    await expect(page.locator("text=Neue Dokumentationseinheit")).toBeVisible()
    await expect(
      page.locator(`a[href*="/caselaw/documentunit/${documentNumber}/files"]`)
    ).toBeVisible()
    await page
      .locator(".table-row", {
        hasText: documentNumber,
      })
      .locator("[aria-label='Dokumentationseinheit löschen']")
      .click()
    await injectAxe(page)
    await checkA11y(page)
  })
})

test.describe("a11y of categories page (/caselaw/documentunit/{documentNumber}/categories)", () => {
  test("first load", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await injectAxe(page)
    await checkA11y(page)
  })

  test("gericht", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await page
      .locator("[aria-label='Gericht'] + button.input-expand-icon")
      .click()
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(
      3925
    )

    await expect(page.locator("text=AG Aachen")).toBeVisible()
    await expect(page.locator("text=AG Aalen")).toBeVisible()
    await page.locator("[aria-label='Gericht']").fill("bayern")
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("bayern")
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(2)
    await injectAxe(page)
    await checkA11y(page)
  })

  test("aktenzeichen", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Aktenzeichen']").fill("testone")
    await page.keyboard.press("Enter")

    await page.locator("[aria-label='Aktenzeichen']").fill("testtwo")
    await page.keyboard.press("Enter")

    await page
      .locator("[aria-label='Abweichendes Aktenzeichen anzeigen']")
      .click()

    await page
      .locator("[aria-label='Abweichendes Aktenzeichen']")
      .fill("testthree")

    await page.keyboard.press("Enter")

    await injectAxe(page)
    await checkA11y(page)
  })

  test("entscheidungsdatum", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Entscheidungsdatum']").fill("2022-02-03")

    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum anzeigen']")
      .click()

    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum']")
      .fill("2022-02-02")
    await page.keyboard.press("Enter")
    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum']")
      .fill("2022-02-01")
    await page.keyboard.press("Enter")

    await injectAxe(page)
    await checkA11y(page)
  })

  test("dokumenttyp", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page
      .locator("[aria-label='Dokumenttyp'] + button.input-expand-icon")
      .click()
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(43)

    // type search string: 3 results for "zwischen"
    await page.locator("[aria-label='Dokumenttyp']").fill("zwischen")
    expect(await page.inputValue("[aria-label='Dokumenttyp']")).toBe("zwischen")
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(3)

    await injectAxe(page)
    await checkA11y(page)
  })

  test("ecli", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='ECLI']").fill("one")

    await page.locator("[aria-label='Abweichender ECLI anzeigen']").click()

    await page.locator("[aria-label='Abweichender ECLI']").fill("two")
    await page.keyboard.press("Enter")
    await page.locator("[aria-label='Abweichender ECLI']").fill("three")
    await page.keyboard.press("Enter")

    await injectAxe(page)
    await checkA11y(page)
  })

  test("rechtskraft", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await page
      .locator("[aria-label='Rechtskraft'] + button.input-expand-icon")
      .click()

    await injectAxe(page)
    await checkA11y(page)
  })

  test("previous decision", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await togglePreviousDecisionsSection(page)
    await fillPreviousDecisionInputs(page, {
      courtType: "Test Court",
      courtLocation: "Test City",
      date: "2004-12-03",
      fileNumber: "1a2b3c",
    })

    await clickSaveButton(page)
    await page.reload()
    await togglePreviousDecisionsSection(page)
    await fillPreviousDecisionInputs(page, {
      date: "2004-12-03",
    })

    await injectAxe(page)
    await checkA11y(page)
  })

  test("text editor", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    const editorField = page.locator("[data-testid='Entscheidungsname'] >> div")
    await editorField.click()
    await editorField.type("this is text")

    await injectAxe(page)
    await checkA11y(page)
  })
})

test.describe("a11y of document page (/caselaw/documentunit/{documentNumber}/files)", () => {
  test("document", async ({ page, documentNumber }) => {
    await navigateToFiles(page, documentNumber)
    await injectAxe(page)
    await checkA11y(page)
  })

  test("upload document", async ({ page, documentNumber }) => {
    await navigateToFiles(page, documentNumber)

    await uploadTestfile(page, "sample.docx")
    await expect(page.locator("text=Hochgeladen am")).toBeVisible()

    await injectAxe(page)
    await checkA11y(page)
  })

  test("delete document", async ({ page, documentNumber }) => {
    await navigateToFiles(page, documentNumber)

    await uploadTestfile(page, "sample.docx")
    await expect(page.locator("text=Hochgeladen am")).toBeVisible()
    await page.locator("text=Datei löschen").click()

    await injectAxe(page)
    await checkA11y(page)
  })

  test("upload non-docx file per file chooser", async ({
    page,
    documentNumber,
  }) => {
    await navigateToFiles(page, documentNumber)
    await uploadTestfile(page, "sample.png")
    await expect(
      page.locator("text=Das ausgewählte Dateiformat ist nicht korrekt.")
    ).toBeVisible()

    await injectAxe(page)
    await checkA11y(page)
  })
})

test.describe("a11y of publication page (/caselaw/documentunit/{documentNumber}/publication)", () => {
  test("publication", async ({ page, documentNumber }) => {
    await navigateToPublication(page, documentNumber)
    await injectAxe(page)
    await checkA11y(page)
  })

  test("publication not possible", async ({ page, documentNumber }) => {
    await navigateToPublication(page, documentNumber)

    await navigateToPublication(page, documentNumber)

    await page
      .locator("[aria-label='Empfängeradresse E-Mail']")
      .fill("wrong.email")
    await page.keyboard.down("Tab")

    await page
      .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
      .click()
    await page
      .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
      .click()
    await expect(page.locator("text=E-Mail-Adresse ungültig")).toBeVisible()

    await injectAxe(page)
    await checkA11y(page)
  })
})
