import { expect } from "@playwright/test"
import { caselawTest as test } from "../../../fixtures"
import {
  fillInput,
  handoverDocumentationUnit,
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"
import { generateString } from "~/test-helper/dataGenerators"

test.describe("deviating document numbers", () => {
  test(
    "Die Nutzerin kann abweichende Dokumentnummern eintragen, sehen und wieder löschen",
    { tag: ["@RISDEV-652"] },
    async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)

      await test.step("Enter deviating document numbers", async () => {
        const inputField = page.locator("#deviatingDocumentNumbers")
        await inputField.fill("XXRE111111111")
        await page.keyboard.press("Enter")

        await inputField.fill("XXRE222222222")
        await page.keyboard.press("Enter")
      })

      await test.step("Check visibility of deviating document numbers", async () => {
        await expect(page.getByText("XXRE111111111")).toBeVisible()
        await expect(page.getByText("XXRE222222222")).toBeVisible()
      })

      await test.step("Delete deviating document number", async () => {
        await page.locator("#deviatingDocumentNumbers").click()
        await page.keyboard.press("Shift+Tab")
        await page.keyboard.press("Enter")

        await expect(page.getByText("XXRE222222222")).toBeHidden()
      })
    },
  )

  test(
    "Abweichende Dokumentnummer ist in der Vorschau sichtbar",
    { tag: ["@RISDEV-652"] },
    async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)

      await test.step("Enter deviating document numbers", async () => {
        const inputField = page.locator("#deviatingDocumentNumbers")
        await inputField.fill("XXRE111111111")
        await page.keyboard.press("Enter")

        await inputField.fill("XXRE222222222")
        await page.keyboard.press("Enter")

        await inputField.fill("XXRE333333333")
        await page.keyboard.press("Enter")
      })

      await save(page)
      await test.step("Expect all three deviating document numbers to be visible in preview", async () => {
        await navigateToPreview(page, documentNumber)
        await expect(
          page
            .getByTestId("preview")
            .getByText("XXRE111111111, XXRE222222222, XXRE333333333", {
              exact: true,
            }),
        ).toBeVisible()
      })

      await test.step("Remove all deviating document numbers, check that category in preview is not visible anymore", async () => {
        await navigateToCategories(page, documentNumber)

        await page.getByLabel("Eintrag löschen").first().click()
        await page.getByLabel("Eintrag löschen").first().click()
        await page.getByLabel("Eintrag löschen").first().click()

        await navigateToPreview(page, documentNumber)
        await expect(page.getByText("Abweichende Dokumentnummer")).toBeHidden()
      })
    },
  )

  test(
    "Abweichende Dokumentnummer wird an jDV exportiert",
    { tag: ["@RISDEV-652"] },
    async ({ page, prefilledDocumentUnit }) => {
      await handoverDocumentationUnit(
        page,
        prefilledDocumentUnit.documentNumber ?? "",
      )

      await navigateToCategories(
        page,
        prefilledDocumentUnit.documentNumber ?? "",
      )

      await test.step("Enter deviating document numbers", async () => {
        const inputField = page.locator("#deviatingDocumentNumbers")

        await inputField.fill("XXRE111111111")
        await page.keyboard.press("Enter")

        await inputField.fill("XXRE222222222")
        await page.keyboard.press("Enter")
      })

      await save(page)

      await test.step("Navigate to handover, click in 'XML-Vorschau', check they are visible", async () => {
        await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)
        await expect(page.getByText("XML Vorschau")).toBeVisible()
        await page.getByText("XML Vorschau").click()

        await expect(
          page.getByText("<begriff>XXRE111111111</begriff>"),
        ).toBeVisible()
        await expect(
          page.getByText("<begriff>XXRE222222222</begriff>"),
        ).toBeVisible()
      })
    },
  )

  test(
    "Dok-Einheit kann über die Abweichende Dokumentnummer in der Suche gefunden werden",
    { tag: ["@RISDEV-652"] },
    async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)
      const deviatingDocumentNumber = generateString({ length: 13 })

      await test.step("Enter and save deviating document number", async () => {
        const inputField = page.locator("#deviatingDocumentNumbers")
        await inputField.fill(deviatingDocumentNumber)
        await page.keyboard.press("Enter")
        await save(page)

        await expect(page.getByText(deviatingDocumentNumber)).toBeVisible()
      })

      await test.step("Search for deviating document number and check that doc unit is displayed", async () => {
        await page.getByTestId("search-navbar-button").click()
        await fillInput(page, "Dokumentnummer Suche", deviatingDocumentNumber)
        await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
        await expect(page.getByText(documentNumber)).toBeVisible()
      })
    },
  )
})
