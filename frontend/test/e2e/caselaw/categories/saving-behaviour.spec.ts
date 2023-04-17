import { expect } from "@playwright/test"
import { navigateToCategories, waitForSaving } from "../e2e-utils"
import { testWithDocumentUnit as test } from "../fixtures"

test.describe("saving behaviour", () => {
  test("test could not update documentUnit", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.route("**/*", async (route) => {
      await route.abort("internetdisconnected")
    })

    await page.locator("[aria-label='Stammdaten Speichern Button']").click()

    await expect(
      page.locator("text='Fehler beim Speichern'").nth(0)
    ).toBeVisible()
    await expect(
      page.locator("text='Fehler beim Speichern'").nth(1)
    ).toBeVisible()
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
    ).toBeVisible({
      timeout: 11 * 1000,
    })
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
    await expect(
      page.locator(".table-row", {
        hasText: documentNumber,
      })
    ).toBeVisible()
    await expect(
      page.locator(".table-row", {
        hasText: "abc",
      })
    ).toBeVisible()
  })
})
