import { expect } from "@playwright/test"
import { navigateToCategories, waitForSaving } from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("ensuring the editing experience in categories is as expected", () => {
  test("test legal effect dropdown", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    expect(await page.inputValue("[aria-label='Rechtskraft']")).toBe(
      "Keine Angabe"
    )

    await page
      .locator("[aria-label='Rechtskraft'] + button.input-expand-icon")
      .click()

    await expect(page.locator("text=Ja")).toBeVisible()
    await expect(page.locator("text=Nein")).toBeVisible()
    await expect(page.locator("text=Keine Angabe")).toBeVisible()
  })

  test("test document type dropdown", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    const totalCaselawDocumentTypes = 43

    // on start: closed dropdown, no input text
    expect(await page.inputValue("[aria-label='Dokumenttyp']")).toBe("")
    await expect(page.locator("text=AnU - Anerkenntnisurteil")).toBeHidden()
    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()

    // open dropdown
    await page
      .locator("[aria-label='Dokumenttyp'] + button.input-expand-icon")
      .click()
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(
      totalCaselawDocumentTypes
    )
    await expect(page.locator("text=AnU - Anerkenntnisurteil")).toBeVisible()
    await expect(page.locator("text=AnH - Anhängiges Verfahren")).toBeVisible()

    // type search string: 3 results for "zwischen"
    await page.locator("[aria-label='Dokumenttyp']").fill("zwischen")
    expect(await page.inputValue("[aria-label='Dokumenttyp']")).toBe("zwischen")
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(3)

    // use the clear icon
    await page.locator("[aria-label='Auswahl zurücksetzen']").click()
    expect(await page.inputValue("[aria-label='Dokumenttyp']")).toBe("")
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(
      totalCaselawDocumentTypes
    )

    // close dropdown
    await page
      .locator("[aria-label='Dokumenttyp'] + button.input-expand-icon")
      .click()
    expect(await page.inputValue("[aria-label='Dokumenttyp']")).toBe("")
    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()

    // open dropdown again by typing a search string
    await page.locator("[aria-label='Dokumenttyp']").fill("zwischen")
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(3)

    // close dropdown using the esc key, input text remains
    await page.keyboard.down("Escape")
    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()
    expect(await page.inputValue("[aria-label='Dokumenttyp']")).toBe("zwischen")
  })

  test("test court dropdown", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    const totalCourts = 3925

    // on start: closed dropdown, no input text
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("")
    await expect(page.locator("text=AG Aachen")).toBeHidden()
    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()

    // open dropdown
    await page
      .locator("[aria-label='Gericht'] + button.input-expand-icon")
      .click()
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(
      totalCourts
    )
    await expect(page.locator("text=AG Aachen")).toBeVisible()
    await expect(page.locator("text=AG Aalen")).toBeVisible()

    // type search string: 2 results for "bayern"
    await page.locator("[aria-label='Gericht']").fill("bayern")
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("bayern")
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(2)

    // use the clear icon
    await page.locator("[aria-label='Auswahl zurücksetzen']").click()
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("")
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(
      totalCourts
    )

    // close dropdown
    await page
      .locator("[aria-label='Gericht'] + button.input-expand-icon")
      .click()
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("")
    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()

    // open dropdown again by typing a search string
    await page.locator("[aria-label='Gericht']").fill("bayern")
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(2)
    // first search result displays a revoked string
    await expect(page.locator("text=aufgehoben seit: 1973")).toBeVisible()

    // close dropdown using the esc key, input text remains
    await page.keyboard.down("Escape")
    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("bayern")
  })

  test("test that setting a court sets the region automatically", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Gericht']").fill("aalen")

    // clicking on dropdown item triggers auto save
    await page.locator("text=AG Aalen").click()
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("AG Aalen")

    // saving... and then saved
    await waitForSaving(page)

    await page.reload() // TODO remove reload when region gets updated via response.data
    await expect(page.locator("text=Region")).toBeVisible()

    // region was set by the backend based on state database table
    expect(await page.inputValue("[aria-label='Region']")).toBe(
      "Baden-Württemberg"
    )

    // clear the court
    await page.locator("[aria-label='Auswahl zurücksetzen']").click()
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("")
    // dropdown should not open
    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()

    await waitForSaving(page)
    await page.reload()
    await expect(page.locator("text=Region")).toBeVisible()

    // region was cleared by the backend
    expect(await page.inputValue("[aria-label='Region']")).toBe("")
  })

  // TODO test that arrow keys behave correctly in dropdowns?

  test("test that setting a special court sets legal effect to yes, but it can be changed afterwards", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    expect(await page.inputValue("[aria-label='Rechtskraft']")).toBe(
      "Keine Angabe"
    )

    await page.locator("[aria-label='Gericht']").fill("bgh")
    await page.locator("text=BGH").click()
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("BGH")
    await waitForSaving(page)

    await page.reload() // TODO remove reload when update via response.data works
    expect(await page.inputValue("[aria-label='Rechtskraft']")).toBe("Ja")

    await page
      .locator("[aria-label='Rechtskraft'] + button.input-expand-icon")
      .click()

    await page.locator("text=Nein").click()
    expect(await page.inputValue("[aria-label='Rechtskraft']")).toBe("Nein")

    await page.locator("[aria-label='Stammdaten Speichern Button']").click()
    await waitForSaving(page)
    await page.reload() // TODO remove reload when update via response.data works

    expect(await page.inputValue("[aria-label='Rechtskraft']")).toBe("Nein")
  })

  test("test that setting a non-special court leaves legal effect unchanged", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    expect(await page.inputValue("[aria-label='Rechtskraft']")).toBe(
      "Keine Angabe"
    )

    await page.locator("[aria-label='Gericht']").fill("aachen")
    await page.locator("text=AG Aachen").click()
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("AG Aachen")
    await waitForSaving(page)

    await page.reload() // TODO remove reload when update via response.data works
    expect(await page.inputValue("[aria-label='Rechtskraft']")).toBe(
      "Keine Angabe"
    )
  })
})
