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

  test("test input during save not lost", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Spruchkörper']").fill("VG-001")
    await waitForInputValue(page, "[aria-label='Spruchkörper']", "VG-001")
    await page.locator("[aria-label='Speichern Button']").click()

    await page.locator("[aria-label='Spruchkörper']").fill("VG-001, VG-002")
    await waitForInputValue(
      page,
      "[aria-label='Spruchkörper']",
      "VG-001, VG-002",
    )

    await expect(page.getByText(/Zuletzt .* Uhr/)).toBeVisible()

    await expect(page.locator("[aria-label='Spruchkörper']")).toHaveValue(
      "VG-001, VG-002",
      { timeout: 500 },
    )
  })

  test("test input not lost on page unload (autosave not triggered yet)", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Spruchkörper']").fill("VG-001")
    await waitForInputValue(page, "[aria-label='Spruchkörper']", "VG-001")

    await expect(page.getByText(/Zuletzt .* Uhr/)).toBeHidden()
    await page.locator("a:has-text('Veröffentlichen')").click()
    await expect(page.locator("h1:has-text('Veröffentlichen')")).toBeVisible({
      timeout: 15000,
    })
    await navigateToCategories(page, documentNumber)

    await expect(page.locator("[aria-label='Spruchkörper']")).toHaveValue(
      "VG-001",
      { timeout: 500 },
    )
  })

  test("test removing last chip element not possible", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Aktenzeichen']").type(generateString())
        await page.keyboard.press("Enter")

        const labels = [
          "Abweichendes Aktenzeichen",
          "Abweichendes Entscheidungsdatum",
          "Abweichender ECLI",
          "Fehlerhaftes Gericht",
        ]

        for (const label of labels) {
          await page.locator(`[aria-label='${label} anzeigen']`).click()
          await page.locator(`[aria-label='${label}']`).type("22.11.2023")
          await page.keyboard.press("Enter")
        }
      },
      page,
      { clickSaveButton: true },
    )

    await waitForSaving(
      async () => {
        while (
          (await page.locator("[data-testid='chip'] > button").count()) > 0
        ) {
          await page.locator("[data-testid='chip'] > button").first().click()
        }
      },
      page,
      { clickSaveButton: true },
    )

    await page.waitForTimeout(500) // eslint-disable-line playwright/no-wait-for-timeout

    await expect(page.locator("[data-testid='chip']")).toHaveCount(0)
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
    //TODO: remove the timeout when search performance get better
    await expect(
      page.locator(".table-row", {
        hasText: documentNumber,
      }),
    ).toBeVisible({ timeout: 30000 })
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
