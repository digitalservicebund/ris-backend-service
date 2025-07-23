import { expect } from "@playwright/test"
import { caselawTest as test } from "../../../fixtures"
import {
  handoverDocumentationUnit,
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe("deviating document numbers", () => {
  test(
    "Die Nutzerin kann abweichende Dokumentnummern eintragen, sehen und wieder löschen",
    { tag: ["@RISDEV-652"] },
    async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)

      await test.step("Enter deviating document numbers", async () => {
        const inputField = page.getByLabel("Abweichende Dokumentnummer", {
          exact: true,
        })
        await inputField.fill("XXRE111111111")
        await page.keyboard.press("Enter")

        await inputField.fill("XXRE222222222")
        await page.keyboard.press("Enter")
      })

      await test.step("Check visibility of deviating document numbers", async () => {
        const chipsLocator = page.getByTestId("chip")
        const chips = await chipsLocator.all()
        await expect(chipsLocator).toHaveCount(2)
        await expect(chips[0].getByTestId("chip-value")).toHaveText(
          "XXRE111111111",
        )
        await expect(chips[1].getByTestId("chip-value")).toHaveText(
          "XXRE222222222",
        )
      })

      await test.step("Delete deviating document number", async () => {
        await expect(page.getByText("XXRE222222222")).toBeVisible()
        await page.keyboard.press("ArrowLeft")
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
        const inputField = page.getByLabel("Abweichende Dokumentnummer", {
          exact: true,
        })
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

        await page.getByLabel("Löschen").first().click()
        await page.getByLabel("Löschen").first().click()
        await page.getByLabel("Löschen").first().click()

        await save(page)
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
        const inputField = page.getByLabel("Abweichende Dokumentnummer", {
          exact: true,
        })

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
})
