import { expect } from "@playwright/test"
import {
  navigateToCategories,
  waitForSaving,
  waitForInputValue,
} from "../e2e-utils"
import { testWithDocumentUnit as test } from "../fixtures"
import { generateString } from "~/test-helper/dataGenerators"

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

  test("change Spruchkörper two times, saving after each change", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Spruchkörper']").fill("VG-001")
        await waitForInputValue(page, "[aria-label='Spruchkörper']", "VG-001")
      },
      page,
      { clickSaveButton: true }
    )

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Spruchkörper']").fill("VG-002")
        await waitForInputValue(page, "[aria-label='Spruchkörper']", "VG-002")
      },
      page,
      { clickSaveButton: true, reload: true }
    )

    await page.reload()
    expect(await page.inputValue("[aria-label='Spruchkörper']")).toBe("VG-002")
  })

  test("saved changes also visible in document unit entry list", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const fileNumber = generateString()
    const ecli = generateString()

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Aktenzeichen']").fill(fileNumber)
        await page.keyboard.press("Enter")
        await page.locator("[aria-label='ECLI']").fill(ecli)
        await page.keyboard.press("Enter")
      },
      page,
      { clickSaveButton: true }
    )

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
        hasText: fileNumber,
      })
    ).toBeVisible()
  })
})
