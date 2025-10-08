import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { navigateToCategories } from "~/e2e/caselaw/utils/e2e-utils"

test.describe("decision date", () => {
  test("invalid decision date shows error", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page
      .getByLabel("Entscheidungsdatum", { exact: true })
      .fill("03.02.2099")

    await expect(
      page.getByText("Das Datum darf nicht in der Zukunft liegen"),
    ).toBeVisible()
  })

  test("backspace deletes decision date character by character", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page
      .getByLabel("Entscheidungsdatum", { exact: true })
      .fill("03.02.2022")
    await expect(
      page.getByLabel("Entscheidungsdatum", { exact: true }),
    ).toHaveValue("03.02.2022")

    const infoPanel = page.getByText(
      new RegExp(`${documentNumber}.*Entscheidungsdatum.*`),
    )
    await expect(infoPanel.getByText("03.02.2022")).toBeVisible()

    await page.getByLabel("Entscheidungsdatum", { exact: true }).click()
    await page.keyboard.press("Backspace")

    await expect(
      page.getByLabel("Entscheidungsdatum", { exact: true }),
    ).toHaveValue("03.02.202_")
  })

  test("incomplete input shows error onblur", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.getByLabel("Entscheidungsdatum", { exact: true }).fill("03")

    await page.keyboard.press("Tab")

    await expect(
      page.getByLabel("Entscheidungsdatum", { exact: true }),
    ).toHaveValue("03.__.____")

    await expect(page.getByText("Unvollständiges Datum")).toBeVisible()

    await page.reload()

    await expect(
      page.getByLabel("Entscheidungsdatum", { exact: true }),
    ).toHaveValue("")
  })
})
