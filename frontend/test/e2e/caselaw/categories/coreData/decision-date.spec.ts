import { expect } from "@playwright/test"
import { navigateToCategories, save } from "../../e2e-utils"
import { caselawTest as test } from "../../fixtures"

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

  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("backspace delete in deviating decision date", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum anzeigen']")
      .click()

    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum']")
      .fill("03.02.2022")
    await expect(
      page.getByLabel("Abweichendes Entscheidungsdatum", { exact: true }),
    ).toHaveValue("03.02.2022")

    await page.keyboard.press("Backspace")
    await page.reload()

    await expect(
      page.getByLabel("Entscheidungsdatum", { exact: true }),
    ).toHaveValue("")
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

  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("nested decision date input toggles child input and correctly saves and displays data", async ({
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

    await expect(
      page.getByText("Abweichendes Entscheidungsdatum>"),
    ).toBeHidden()

    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum anzeigen']")
      .click()

    await expect(
      page.getByText("Abweichendes Entscheidungsdatum").first(),
    ).toBeVisible()

    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum']")
      .fill("02.02.2022")
    await page.keyboard.press("Enter")
    await page
      .locator("[aria-label='Abweichendes Entscheidungsdatum']")
      .fill("01.02.2022")
    await page.keyboard.press("Enter")
    await save(page)

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
      page.getByText("Abweichendes Entscheidungsdatum").first(),
    ).toBeHidden()
  })
})
