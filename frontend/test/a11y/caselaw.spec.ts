import AxeBuilder from "@axe-core/playwright"
import { expect } from "@playwright/test"
import { checkA11y, injectAxe } from "axe-playwright"
import {
  navigateToCategories,
  navigateToFiles,
  navigateToPublication,
  toggleProceedingDecisionsSection,
  fillProceedingDecisionInputs,
  uploadTestfile,
  waitForSaving,
} from "../e2e/caselaw/e2e-utils"
import { testWithDocumentUnit as test } from "../e2e/caselaw/fixtures"

test.describe("a11y of start page (/caselaw)", () => {
  test("documentUnit list", async ({ page }) => {
    await page.goto("/")
    await expect(page.locator("text=Neue Dokumentationseinheit")).toBeVisible()
    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
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
    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})

test.describe("a11y of categories page (/caselaw/documentunit/{documentNumber}/categories)", () => {
  test("first load", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    //TODO: we make the assumption, that the field id in the configs always matches the domain model. Input field IDs need to have the same value like the model name, e.g. the court input fields in coreData and proceedingDecision both need to have the ID "court", because the model is named so. This results in a "duplicate-id-aria" violation, we need some decoupling here, so we are able to use different ids in the frontend. Also the ModelComponentRepeater creates components with the same id, we will need some kind of indexing in the ids here. That's why we disable the "duplicate-id-aria" rule for now.
    const accessibilityScanResults = await new AxeBuilder({ page })
      .disableRules(["duplicate-id-aria"])
      .analyze()
    expect(accessibilityScanResults.violations).toEqual([])
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
    const accessibilityScanResults = await new AxeBuilder({ page })
      .disableRules(["duplicate-id-aria"])
      .analyze()
    expect(accessibilityScanResults.violations).toEqual([])
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

    const accessibilityScanResults = await new AxeBuilder({ page })
      .disableRules(["duplicate-id-aria"])
      .analyze()
    expect(accessibilityScanResults.violations).toEqual([])
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

    const accessibilityScanResults = await new AxeBuilder({ page })
      .disableRules(["duplicate-id-aria"])
      .analyze()
    expect(accessibilityScanResults.violations).toEqual([])
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

    const accessibilityScanResults = await new AxeBuilder({ page })
      .disableRules(["duplicate-id-aria"])
      .analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("ecli", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='ECLI']").fill("one")

    await page.locator("[aria-label='Abweichender ECLI anzeigen']").click()

    await page.locator("[aria-label='Abweichender ECLI']").fill("two")
    await page.keyboard.press("Enter")
    await page.locator("[aria-label='Abweichender ECLI']").fill("three")
    await page.keyboard.press("Enter")

    const accessibilityScanResults = await new AxeBuilder({ page })
      .disableRules(["duplicate-id-aria"])
      .analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("rechtskraft", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await page
      .locator("[aria-label='Rechtskraft'] + button.input-expand-icon")
      .click()

    const accessibilityScanResults = await new AxeBuilder({ page })
      .disableRules(["duplicate-id-aria"])
      .analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("proceeding decision", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await toggleProceedingDecisionsSection(page)
    await fillProceedingDecisionInputs(page, {
      court: "AG Aalen",
      date: "2004-12-03",
      fileNumber: "1a2b3c",
    })

    await waitForSaving(page)
    await page.reload()
    await toggleProceedingDecisionsSection(page)
    await fillProceedingDecisionInputs(page, {
      date: "2004-12-03",
    })

    const accessibilityScanResults = await new AxeBuilder({ page })
      .disableRules(["duplicate-id-aria"])
      .analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("text editor", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    const editorField = page.locator("[data-testid='Entscheidungsname'] >> div")
    await editorField.click()
    await editorField.type("this is text")

    const accessibilityScanResults = await new AxeBuilder({ page })
      .disableRules(["duplicate-id-aria"])
      .analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("schlagwörter", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Schlagwörter']").fill("one")
    await page.keyboard.press("Enter")

    await page.locator("[aria-label='Schlagwörter']").fill("two")
    await page.keyboard.press("Enter")

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
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

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("delete document", async ({ page, documentNumber }) => {
    await navigateToFiles(page, documentNumber)

    await uploadTestfile(page, "sample.docx")
    await expect(page.locator("text=Hochgeladen am")).toBeVisible()
    await page.locator("text=Datei löschen").click()

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
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

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})

test.describe("a11y of publication page (/caselaw/documentunit/{documentNumber}/publication)", () => {
  test("publication", async ({ page, documentNumber }) => {
    await navigateToPublication(page, documentNumber)
    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
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

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})
