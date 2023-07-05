import AxeBuilder from "@axe-core/playwright"
import { expect } from "@playwright/test"
import {
  navigateToCategories,
  waitForSaving,
  fillProceedingDecisionInputs,
  toggleProceedingDecisionsSection,
} from "../../e2e/caselaw/e2e-utils"
import { caselawTest as test } from "../../e2e/caselaw/fixtures"

test.describe("a11y of categories page (/caselaw/documentunit/{documentNumber}/categories)", () => {
  test("first load", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
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
    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("aktenzeichen", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Aktenzeichen']").type("testone")
    await page.keyboard.press("Enter")

    await page.locator("[aria-label='Aktenzeichen']").type("testtwo")
    await page.keyboard.press("Enter")

    await page
      .locator("[aria-label='Abweichendes Aktenzeichen anzeigen']")
      .click()

    await page
      .locator("[aria-label='Abweichendes Aktenzeichen']")
      .type("testthree")

    await page.keyboard.press("Enter")

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("entscheidungsdatum", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Entscheidungsdatum']").fill("03.02.2022")

    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum anzeigen']")
      .click()

    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum']")
      .fill("02.02.2022")
    await page.keyboard.press("Enter")
    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum']")
      .fill("01.02.2022")
    await page.keyboard.press("Enter")

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
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

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test("ecli", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='ECLI']").type("one")

    await page.locator("[aria-label='Abweichender ECLI anzeigen']").click()

    await page.locator("[aria-label='Abweichender ECLI']").type("two")
    await page.keyboard.press("Enter")
    await page.locator("[aria-label='Abweichender ECLI']").type("three")
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

    await waitForSaving(
      async () => {
        await fillProceedingDecisionInputs(page, {
          court: "AG Aalen",
          decisionDate: "03.12.2004",
          fileNumber: "1a2b3c",
        })
      },
      page,
      { clickSaveButton: true }
    )
    await page.reload()
    await toggleProceedingDecisionsSection(page)
    await fillProceedingDecisionInputs(page, {
      decisionDate: "03.12.2004",
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
