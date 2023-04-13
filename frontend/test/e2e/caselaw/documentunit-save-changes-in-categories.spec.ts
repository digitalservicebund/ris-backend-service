import { expect } from "@playwright/test"
import { navigateToCategories, waitForSaving } from "./e2e-utils"
import { testWithDocumentUnit as test } from "./fixtures"

test.describe("save changes in core data and texts and verify it persists", () => {
  test("test core data change", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Aktenzeichen']").fill("abc")
    await page.locator("[aria-label='ECLI']").fill("abc123")
    await page.keyboard.press("Enter")

    await waitForSaving(page)

    await page.reload()
    expect(await page.inputValue("[aria-label='Aktenzeichen']")).toBe("")
    expect(await page.inputValue("[aria-label='ECLI']")).toBe("abc123")
  })

  test("saved changes also visible in document unit entry list", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Aktenzeichen']").fill("abc")
    await page.locator("[aria-label='ECLI']").fill("abc123")
    await page.keyboard.press("Enter")

    await waitForSaving(page)

    await page.goto("/")
    await expect(
      page.locator(`a[href*="/caselaw/documentunit/${documentNumber}/files"]`)
    ).toBeVisible()
    await page.locator(".table-row", {
      hasText: documentNumber,
    })
    await page.locator(".table-row", {
      hasText: "abc",
    })
  })

  test("nested 'Aktenzeichen' input toggles child input and correctly saves and displays data", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Aktenzeichen']").fill("one")
    await page.keyboard.press("Enter")

    await page.locator("[aria-label='Aktenzeichen']").fill("two")
    await page.keyboard.press("Enter")

    await expect(page.locator("text=one").first()).toBeVisible()
    await expect(page.locator("text=two").first()).toBeVisible()

    await expect(page.locator("text=Abweichendes Aktenzeichen>")).toBeHidden()

    await page
      .locator("[aria-label='Abweichendes Aktenzeichen anzeigen']")
      .click()

    await expect(
      page.locator("text=Abweichendes Aktenzeichen").first()
    ).toBeVisible()

    await page.locator("[aria-label='Abweichendes Aktenzeichen']").fill("three")
    await page.keyboard.press("Enter")

    await waitForSaving(page)

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

  test("nested 'Entscheidungsdatum' input toggles child input and correctly saves and displays data", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Entscheidungsdatum']").fill("2022-02-03")
    expect(
      await page.locator("[aria-label='Entscheidungsdatum']").inputValue()
    ).toBe("2022-02-03")

    await expect(
      page.locator("text=Abweichendes Entscheidungsdatum>")
    ).toBeHidden()

    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum anzeigen']")
      .click()

    await expect(
      page.locator("text=Abweichendes Entscheidungsdatum").first()
    ).toBeVisible()

    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum']")
      .fill("2022-02-02")
    await page.keyboard.press("Enter")
    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum']")
      .fill("2022-02-01")
    await page.keyboard.press("Enter")

    await waitForSaving(page)

    await page.reload()

    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum anzeigen']")
      .click()

    await expect(page.locator(".label-wrapper").nth(0)).toHaveText("02.02.2022")

    await expect(page.locator(".label-wrapper").nth(1)).toHaveText("01.02.2022")

    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum schließen']")
      .click()

    await expect(
      page.locator("text=Abweichendes Entscheidungsdatum").first()
    ).toBeHidden()
  })

  test("nested 'ECLI' input toggles child input and correctly saves and displays data", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='ECLI']").fill("one")
    await expect(page.locator("text=one").first()).toBeVisible()

    await expect(page.locator("text=Abweichender ECLI>")).toBeHidden()

    await page.locator("[aria-label='Abweichender ECLI anzeigen']").click()

    await expect(page.locator("text=Abweichender ECLI").first()).toBeVisible()

    await page.locator("[aria-label='Abweichender ECLI']").fill("two")
    await page.keyboard.press("Enter")
    await page.locator("[aria-label='Abweichender ECLI']").fill("three")
    await page.keyboard.press("Enter")

    await waitForSaving(page)

    await page.reload()

    await page.locator("[aria-label='Abweichender ECLI anzeigen']").click()
    await expect(page.locator("text=two").first()).toBeVisible()
    await expect(page.locator("text=three").first()).toBeVisible()

    await page.locator("[aria-label='Abweichender ECLI schließen']").click()

    await expect(page.locator("text=Abweichender ECLI").first()).toBeHidden()
  })

  test("adding, navigating, deleting multiple chips inputs", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Aktenzeichen']").fill("testone")
    await page.keyboard.press("Enter")

    await page.locator("[aria-label='Aktenzeichen']").fill("testtwo")
    await page.keyboard.press("Enter")

    await page.locator("[aria-label='Aktenzeichen']").fill("testthree")
    await page.keyboard.press("Enter")

    await expect(page.locator("text=testone").first()).toBeVisible()
    await expect(page.locator("text=testtwo").first()).toBeVisible()
    await expect(page.locator("text=testthree").first()).toBeVisible()

    //Navigate back and delete on enter
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

    await waitForSaving(page)

    await page.reload()

    await expect(page.locator("text=testthree").first()).toBeVisible()
  })

  test("text editor fields should have predefined height", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    // small
    const smallEditor = page.locator("[data-testid='Titelzeile']")
    const smallEditorHeight = await smallEditor.evaluate((element) =>
      window.getComputedStyle(element).getPropertyValue("height")
    )
    expect(parseInt(smallEditorHeight)).toBeGreaterThanOrEqual(60)

    //medium
    const mediumEditor = page.locator("[data-testid='Leitsatz']")
    const mediumEditorHeight = await mediumEditor.evaluate((element) =>
      window.getComputedStyle(element).getPropertyValue("height")
    )
    expect(parseInt(mediumEditorHeight)).toBeGreaterThanOrEqual(120)

    //large
    const largeEditor = page.locator("[data-testid='Gründe']")
    const largeEditorHeight = await largeEditor.evaluate((element) =>
      window.getComputedStyle(element).getPropertyValue("height")
    )
    expect(parseInt(largeEditorHeight)).toBeGreaterThanOrEqual(320)
  })

  test("change Spruchkörper two times with autosave after each change", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Spruchkörper']").fill("VG-001")
    await page.keyboard.press("Tab")

    await expect(
      page.locator("text=Zuletzt gespeichert um").nth(0)
    ).toBeVisible({ timeout: 11 * 1000 })
    const firstSaveText = await page
      .locator("text=Zuletzt gespeichert um")
      .nth(0)
      .textContent()

    await page.locator("[aria-label='Spruchkörper']").fill("VG-002")
    await page.keyboard.press("Tab")

    await expect(page.locator(`text=${firstSaveText}`).nth(0)).toBeHidden({
      timeout: 11 * 1000,
    })
    await expect(page.getByText("Zuletzt gespeichert um").nth(0)).toBeVisible({
      timeout: 11 * 1000,
    })

    await page.reload()

    expect(await page.inputValue("[aria-label='Spruchkörper']")).toBe("VG-002")
  })

  test("change Spruchkörper two times with save with button after each change", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Spruchkörper']").fill("VG-001")
    await page.keyboard.press("Tab")

    await waitForSaving(page)

    await page.locator("[aria-label='Spruchkörper']").fill("VG-002")
    await page.keyboard.press("Tab")

    await waitForSaving(page)

    await page.reload()

    expect(await page.inputValue("[aria-label='Spruchkörper']")).toBe("VG-002")
  })
})
