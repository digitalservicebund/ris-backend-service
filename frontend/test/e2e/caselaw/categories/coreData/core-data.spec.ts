import { expect } from "@playwright/test"
import { navigateToCategories, waitForSaving } from "../../e2e-utils"
import { testWithDocumentUnit as test } from "../../fixtures"

test.describe("core data", () => {
  test("test core data change", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Aktenzeichen']").fill("abc")
        await page.keyboard.press("Enter")
        await page.locator("[aria-label='ECLI']").fill("abc123")
        await page.keyboard.press("Enter")
      },
      page,
      { clickSaveButton: true }
    )

    await page.reload()
    expect(await page.inputValue("[aria-label='Aktenzeichen']")).toBe("")
    expect(await page.inputValue("[aria-label='ECLI']")).toBe("abc123")
  })

  test("nested 'ECLI' input toggles child input and correctly saves and displays data", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='ECLI']").fill("one")
        await expect(page.locator("text=one").first()).toBeVisible()

        await expect(page.locator("text=Abweichender ECLI>")).toBeHidden()

        await page.locator("[aria-label='Abweichender ECLI anzeigen']").click()

        await expect(
          page.locator("text=Abweichender ECLI").first()
        ).toBeVisible()

        await page.locator("[aria-label='Abweichender ECLI']").fill("two")
        await page.keyboard.press("Enter")
        await page.locator("[aria-label='Abweichender ECLI']").fill("three")
        await page.keyboard.press("Enter")
      },
      page,
      { clickSaveButton: true }
    )

    await page.reload()

    await page.locator("[aria-label='Abweichender ECLI anzeigen']").click()
    await expect(page.locator("text=two").first()).toBeVisible()
    await expect(page.locator("text=three").first()).toBeVisible()

    await page.locator("[aria-label='Abweichender ECLI schließen']").click()
    await expect(page.locator("text=Abweichender ECLI").first()).toBeHidden()
  })

  test("nested fileNumbers input toggles child input and correctly saves and displays data", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Aktenzeichen']").fill("one")
        await page.keyboard.press("Enter")

        await page.locator("[aria-label='Aktenzeichen']").fill("two")
        await page.keyboard.press("Enter")

        await expect(page.locator("text=one").first()).toBeVisible()
        await expect(page.locator("text=two").first()).toBeVisible()

        await expect(
          page.locator("text=Abweichendes Aktenzeichen>")
        ).toBeHidden()

        await page
          .locator("[aria-label='Abweichendes Aktenzeichen anzeigen']")
          .click()

        await expect(
          page.locator("text=Abweichendes Aktenzeichen").first()
        ).toBeVisible()

        await page
          .locator("[aria-label='Abweichendes Aktenzeichen']")
          .fill("three")
        await page.keyboard.press("Enter")
      },
      page,
      { clickSaveButton: true }
    )

    await page.reload()

    await page
      .locator("[aria-label='Abweichendes Aktenzeichen anzeigen']")
      .click()
    await expect(page.locator("text=three").first()).toBeVisible()
    await page
      .locator("[aria-label='Abweichendes Aktenzeichen schließen']")
      .click()
    await expect(
      page.locator("text=Abweichendes Aktenzeichen").first()
    ).toBeHidden()
  })

  test("adding, navigating, deleting multiple fileNumbers", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Aktenzeichen']").fill("testone")
        await page.keyboard.press("Enter")

        await page.locator("[aria-label='Aktenzeichen']").fill("testtwo")
        await page.keyboard.press("Enter")

        await page.locator("[aria-label='Aktenzeichen']").fill("testthree")
        await page.keyboard.press("Enter")

        await expect(page.locator("text=testone").first()).toBeVisible()
        await expect(page.locator("text=testtwo").first()).toBeVisible()
        await expect(page.locator("text=testthree").first()).toBeVisible()

        // Navigate back and delete on enter
        await page.keyboard.press("ArrowLeft")
        await page.keyboard.press("ArrowLeft")
        await page.keyboard.press("Enter")

        await expect(page.locator("text=testtwo").first()).toBeHidden()

        // Tab out and in
        await page.keyboard.press("Tab")
        await page.keyboard.press("Tab")

        await page.keyboard.down("Shift")
        await page.keyboard.press("Tab")
        await page.keyboard.up("Shift")

        await page.keyboard.down("Shift")
        await page.keyboard.press("Tab")
        await page.keyboard.up("Shift")

        await page.keyboard.press("ArrowLeft")

        //Navigate back and delete on backspace
        await page.keyboard.press("Enter")

        await expect(page.locator("text=testone").first()).toBeHidden()
      },
      page,
      { clickSaveButton: true }
    )

    await page.reload()
    await expect(page.locator("text=testthree").first()).toBeVisible()
  })

  test("test legal effect dropdown", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    expect(await page.inputValue("[aria-label='Rechtskraft']")).toBe(
      "Keine Angabe"
    )

    await page
      .locator("[aria-label='Rechtskraft'] + button.input-expand-icon")
      .click()

    await expect(page.getByText("Ja", { exact: true })).toBeVisible()
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
    await expect(page.locator("text=Anerkenntnisurteil")).toBeVisible()
    await expect(page.locator("text=Anhängiges Verfahren")).toBeVisible()

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

  test("ensure new docUnit has correct documentationOffice for DS user", async ({
    page,
  }) => {
    await test.step("create new docUnit with logged in user", async () => {
      page.goto("/caselaw")
      await page.getByText("Neue Dokumentationseinheit").click()

      await expect(
        page.getByText("Aktuell ist keine Datei hinterlegt")
      ).toBeVisible()

      await page.getByText("Rubriken").click()
      await expect(
        page.getByText("DOKUMENTATIONSSTELLEDigitalService")
      ).toBeVisible()
    })
  })
})

test.describe(() => {
  test.use({
    storageState: "test/e2e/shared/.auth/user_bgh.json",
  })

  test("ensure new docUnit has correct documentationOffice for BGH user", async ({
    page,
  }) => {
    await test.step("create new docUnit with logged in user", async () => {
      page.goto("/caselaw")
      await page.getByText("Neue Dokumentationseinheit").click()

      await expect(
        page.getByText("Aktuell ist keine Datei hinterlegt")
      ).toBeVisible()

      await page.getByText("Rubriken").click()
      await expect(page.getByText("DOKUMENTATIONSSTELLEBGH")).toBeVisible()
    })
  })
})
