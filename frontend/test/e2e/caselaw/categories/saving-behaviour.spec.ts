import { expect } from "@playwright/test"
import {
  navigateToCategories,
  waitForSaving,
  waitForInputValue,
  fillActiveCitationInputs,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"
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

    await page.locator("[aria-label='Spruchkörper']").fill("VG-001")
    await page.locator("[aria-label='Speichern Button']").click()

    await expect(page.locator("text='Fehler beim Speichern'")).toBeVisible()
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
      { clickSaveButton: true },
    )

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Spruchkörper']").fill("VG-002")
        await waitForInputValue(page, "[aria-label='Spruchkörper']", "VG-002")
      },
      page,
      { clickSaveButton: true, reload: true },
    )

    await page.reload()
    await expect(page.locator("[aria-label='Spruchkörper']")).toHaveValue(
      "VG-002",
    )
  })

  test("change Aktivzitierung to test saving of nested elements", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await waitForSaving(
      async () => {
        await fillActiveCitationInputs(page, {
          documentType: "AnU",
        })
        await page.getByLabel("Aktivzitierung speichern").click()
      },
      page,
      { clickSaveButton: true },
    )

    await page.reload()
    await expect(page.getByText("Anerkenntnisurteil")).toBeVisible()
  })

  test("saved changes also visible in document unit entry list", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const fileNumber = generateString()

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Aktenzeichen']").fill(fileNumber)
        await page.keyboard.press("Enter")
      },
      page,
      { clickSaveButton: true },
    )

    await page.goto("/")
    await page
      .getByLabel("Dokumentnummer oder Aktenzeichen Suche")
      .fill(fileNumber)
    await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
    await page.getByText(documentNumber).isVisible()
    await expect(
      page.locator(".table-row", {
        hasText: documentNumber,
      }),
    ).toBeVisible()
    await expect(
      page.locator(".table-row", {
        hasText: fileNumber,
      }),
    ).toBeVisible()
  })
})
