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

    // close dropdown using the esc key, user input text gets removed and last saved value restored
    await page.keyboard.down("Escape")
    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()
    expect(await page.inputValue("[aria-label='Dokumenttyp']")).toBe("")
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

    // close dropdown using the esc key, user input text gets removed and last saved value restored
    await page.keyboard.down("Escape")
    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("")
  })

  test("test correct esc/tab behaviour in court dropdown", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    expect(await page.inputValue("[aria-label='Gericht']")).toBe("")
    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()

    await page.locator("[aria-label='Gericht']").fill("BVerfG")
    await page.locator("text=BVerfG").click()

    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()
    await expect(page.locator("[aria-label='Gericht']")).toHaveValue("BVerfG")

    await page.locator("[aria-label='Gericht']").fill("BGH")

    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(1)

    await page.keyboard.press("Escape") // reset to last saved value

    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()
    await expect(page.locator("[aria-label='Gericht']")).toHaveValue("BVerfG")

    await page.locator("[aria-label='Gericht']").fill("BGH")
    await page.keyboard.press("Tab") // reset to last saved value

    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()
    await expect(page.locator("[aria-label='Gericht']")).toHaveValue("BVerfG")
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

    expect(await page.inputValue("[aria-label='Rechtskraft']")).toBe("Ja")

    await page
      .locator("[aria-label='Rechtskraft'] + button.input-expand-icon")
      .click()

    await page.locator("text=Nein").click()
    expect(await page.inputValue("[aria-label='Rechtskraft']")).toBe("Nein")

    await page.locator("[aria-label='Stammdaten Speichern Button']").click()
    await waitForSaving(page)

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

  test("invalid 'Entscheidungsdatum' shows error", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Entscheidungsdatum']").fill("2024-02-03")

    await expect(
      page.locator(
        "text=Das Entscheidungsdatum darf nicht in der Zukunft liegen"
      )
    ).toBeVisible()
  })

  test("updated fileNumber should update info panel", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const infoPanel = page.locator("div", { hasText: documentNumber }).nth(-2)
    const fileNumberInfo = infoPanel
      .locator("div", { hasText: "Aktenzeichen" })
      .nth(-2)
    await expect(fileNumberInfo).toHaveText("Aktenzeichen - ")

    await page.locator("[aria-label='Aktenzeichen']").fill("-firstChip")
    await page.keyboard.press("Enter")
    await expect(fileNumberInfo).toHaveText("Aktenzeichen-firstChip")

    await page.locator("[aria-label='Aktenzeichen']").fill("-secondChip")
    await page.keyboard.press("Enter")
    await expect(fileNumberInfo).toHaveText("Aktenzeichen-firstChip")

    // delete first chip
    await page.locator("div", { hasText: "-firstChip" }).nth(-2).click()
    await page.keyboard.press("Enter")
    await expect(fileNumberInfo).toHaveText("Aktenzeichen-secondChip")
  })

  test("updated court should update info panel", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const infoPanel = page.locator("div", { hasText: documentNumber }).nth(-2)
    const courtInfo = infoPanel.locator("div", { hasText: "Gericht" }).first()
    await expect(courtInfo).toHaveText("Gericht - ")

    await page.locator("[aria-label='Gericht']").fill("aalen")
    await page.locator("text=AG Aalen").click()
    expect(await page.inputValue("[aria-label='Gericht']")).toBe("AG Aalen")
    await expect(courtInfo).toContainText("AG Aalen")
  })

  test("updated decion date should update info panel", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const infoPanel = page.locator("div", { hasText: documentNumber }).nth(-2)
    const dateInfo = infoPanel
      .locator("div", { hasText: "Entscheidungsdatum" })
      .first()
    await expect(dateInfo).toHaveText("Entscheidungsdatum - ")

    await page.locator("[aria-label='Entscheidungsdatum']").fill("2022-02-03")
    expect(
      await page.locator("[aria-label='Entscheidungsdatum']").inputValue()
    ).toBe("2022-02-03")

    //when using the .fill() method, we need 3 tabs to leave the field
    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")

    await expect(dateInfo).toContainText("03.02.2022")
  })

  test("backspace delete resets decision date", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Entscheidungsdatum']").fill("2022-02-03")
    expect(
      await page.locator("[aria-label='Entscheidungsdatum']").inputValue()
    ).toBe("2022-02-03")

    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")
    await page.keyboard.press("Tab")

    const infoPanel = page.locator("div", { hasText: documentNumber }).nth(-2)
    await expect(
      infoPanel.locator("div", { hasText: "Entscheidungsdatum" }).first()
    ).toContainText("03.02.2022")

    await page.locator("[aria-label='Entscheidungsdatum']").click()
    await page.keyboard.press("Backspace")

    expect(
      await page.locator("[aria-label='Entscheidungsdatum']").inputValue()
    ).toBe("")

    await expect(
      infoPanel.locator("div", { hasText: "Entscheidungsdatum" }).first()
    ).toHaveText("Entscheidungsdatum -")
  })

  test("backspace delete in deviating decision date", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

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

    await expect(page.locator(".label-wrapper").nth(0)).toHaveText("02.02.2022")
    await expect(page.locator(".label-wrapper").nth(1)).toHaveText("01.02.2022")

    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum']")
      .fill("2022-02-03")

    expect(
      await page.inputValue("[aria-label='Abweichendes Entscheidungsdatum']")
    ).toBe("2022-02-03")

    await page.keyboard.press("Backspace")
    await page.keyboard.press("Backspace")
    expect(
      await page.inputValue("[aria-label='Abweichendes Entscheidungsdatum']")
    ).toBe("")
  })
})
