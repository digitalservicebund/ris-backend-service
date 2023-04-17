import { expect } from "@playwright/test"
import { navigateToPublication, waitForSaving } from "../e2e-utils"
import { testWithDocumentUnit as test } from "../fixtures"

test.describe("ensuring the publishing of documentunits works as expected", () => {
  test("publication page shows all possible missing required fields when no fields filled", async ({
    page,
    documentNumber,
  }) => {
    await navigateToPublication(page, documentNumber)

    await expect(page.locator("li:has-text('Aktenzeichen')")).toBeVisible()
    await expect(page.locator("li:has-text('Gericht')")).toBeVisible()
    await expect(
      page.locator("li:has-text('Entscheidungsdatum')")
    ).toBeVisible()
    await expect(page.locator("li:has-text('Dokumenttyp')")).toBeVisible()
  })

  test("publication page updates missing required fields after fields were updated", async ({
    page,
    documentNumber,
  }) => {
    await navigateToPublication(page, documentNumber)

    await expect(page.locator("li:has-text('Aktenzeichen')")).toBeVisible()
    await expect(page.locator("li:has-text('Gericht')")).toBeVisible()
    await expect(
      page.locator("li:has-text('Entscheidungsdatum')")
    ).toBeVisible()
    await expect(page.locator("li:has-text('Dokumenttyp')")).toBeVisible()
    await page.locator("[aria-label='Rubriken bearbeiten']").click()

    await page.locator("[aria-label='Aktenzeichen']").fill("abc")
    await page.keyboard.press("Enter")

    await page.locator("[aria-label='Gericht']").fill("aalen")
    await page.locator("text=AG Aalen").click() // triggers autosave

    await waitForSaving(page)
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("AG Aalen")

    await navigateToPublication(page, documentNumber)
    await expect(page.locator("li:has-text('Aktenzeichen')")).toBeHidden()
    await expect(page.locator("li:has-text('Gericht')")).toBeHidden()
    await expect(
      page.locator("li:has-text('Entscheidungsdatum')")
    ).toBeVisible()
    await expect(page.locator("li:has-text('Dokumenttyp')")).toBeVisible()
  })

  test("publication not possible if required fields missing", async ({
    page,
    documentNumber,
  }) => {
    await navigateToPublication(page, documentNumber)

    await page
      .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
      .click()
    await expect(
      page.locator("text=Es sind noch nicht alle Pflichtfelder befüllt.")
    ).toBeVisible()
  })

  test("publication not possible with empty email", async ({
    page,
    documentNumber,
  }) => {
    await navigateToPublication(page, documentNumber)

    await page.locator("[aria-label='Empfängeradresse E-Mail']").fill("")
    await page.keyboard.down("Tab")

    await page
      .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
      .click()
    await page
      .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
      .click()
    await expect(page.locator("text=E-Mail-Adresse ungültig")).toBeVisible()
  })

  test("publication not possible with invalid email", async ({
    page,
    documentNumber,
  }) => {
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
  })

  test("publication possible when all required fields filled", async ({
    page,
    documentNumber,
  }) => {
    await navigateToPublication(page, documentNumber)
    await page.locator("[aria-label='Rubriken bearbeiten']").click()

    await page.locator("[aria-label='Aktenzeichen']").fill("abc")
    await page.keyboard.press("Enter")
    await expect(page.getByText("abc").first()).toBeVisible()
    await waitForSaving(page)

    await page.locator("[aria-label='Entscheidungsdatum']").fill("2022-02-03")
    expect(
      await page.locator("[aria-label='Entscheidungsdatum']").inputValue()
    ).toBe("2022-02-03")
    await page.keyboard.press("Tab")
    await waitForSaving(page)

    await page.locator("[aria-label='Gericht']").fill("vgh mannheim")
    await page.locator("text=VGH Mannheim").click()
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("VGH Mannheim")
    await waitForSaving(page)

    await page.locator("[aria-label='Dokumenttyp']").fill("AnU")
    await page.locator("text=AnU - Anerkenntnisurteil").click()
    await waitForSaving(page)

    await page
      .locator("[aria-label='Rechtskraft'] + button.input-expand-icon")
      .click()

    await page.locator("text=Ja").click()
    await waitForSaving(page)

    await expect(
      page.locator("text=Zuletzt gespeichert um").first()
    ).toBeVisible()
    await navigateToPublication(page, documentNumber)

    await expect(
      page.locator("text=Alle Pflichtfelder sind korrekt ausgefüllt")
    ).toBeVisible()

    await expect(
      page.locator(
        "text=Diese Dokumentationseinheit wurde bisher nicht veröffentlicht"
      )
    ).toBeVisible()

    await page
      .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
      .click()

    await expect(page.locator("text=Email wurde versendet")).toBeVisible()

    await expect(page.locator("text=Letzte Veröffentlichung am")).toBeVisible()
  })
})
