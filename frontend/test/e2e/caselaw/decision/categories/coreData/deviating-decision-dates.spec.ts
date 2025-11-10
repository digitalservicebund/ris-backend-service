import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  handoverDocumentationUnit,
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe("deviating decision dates", () => {
  test("display, adding, navigating, deleting multiple deviating decision dates", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await test.step("Add two deviating decision dates, check they are visible", async () => {
      await page.getByLabel("Abweichendes Entscheidungsdatum anzeigen").click()
      await page.locator("#deviatingDecisionDates").fill("01.02.2020")
      await page.keyboard.press("Enter")
      await page.locator("#deviatingDecisionDates").fill("01.02.2021")
      await page.keyboard.press("Enter")

      await expect(page.getByText("01.02.2020")).toBeVisible()
      await expect(page.getByText("01.02.2021")).toBeVisible()
    })

    await test.step("Navigate by keyboard to last chips, delete by pressing enter", async () => {
      await page.keyboard.press("Shift+Tab")
      await page.keyboard.press("Enter")

      await expect(page.getByText("01.02.2021")).toBeHidden()
    })
    await save(page)

    await test.step("Check if deviating decision dates are persisted in reload", async () => {
      await page.reload()
      // If deviating data is available, it is automatically expanded
      await expect(page.getByText("01.02.2020")).toBeVisible()
    })
  })

  test("deviating decision dates in preview", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await test.step("Add two deviating decision dates, check they are visible", async () => {
      await page.getByLabel("Abweichendes Entscheidungsdatum anzeigen").click()
      await page.locator("#deviatingDecisionDates").fill("01.02.2020")
      await page.keyboard.press("Enter")
      await page.locator("#deviatingDecisionDates").fill("01.02.2021")
      await page.keyboard.press("Enter")

      await expect(page.getByText("01.02.2020")).toBeVisible()
      await expect(page.getByText("01.02.2021")).toBeVisible()
    })

    await save(page)

    await test.step("Navigate to preview, check they are visible", async () => {
      await navigateToPreview(page, documentNumber)
      await expect(page.getByText("01.02.2020")).toBeVisible()
      await expect(page.getByText("01.02.2021")).toBeVisible()
    })
  })

  test("deviating decision dates are exported", async ({
    page,
    prefilledDocumentUnit,
  }) => {
    await handoverDocumentationUnit(
      page,
      prefilledDocumentUnit.documentNumber || "",
    )

    await navigateToCategories(page, prefilledDocumentUnit.documentNumber || "")

    await test.step("Add two deviating decision dates, check they are visible", async () => {
      await page.getByLabel("Abweichendes Entscheidungsdatum anzeigen").click()
      await page.locator("#deviatingDecisionDates").fill("01.02.2020")
      await page.keyboard.press("Enter")
      await page.locator("#deviatingDecisionDates").fill("01.02.2021")
      await page.keyboard.press("Enter")

      await expect(page.getByText("01.02.2020")).toBeVisible()
      await expect(page.getByText("01.02.2021")).toBeVisible()
    })

    await save(page)

    await test.step("Navigate to handover, click in 'XML-Vorschau', check they are visible", async () => {
      await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)

      await expect(page.getByText("XML Vorschau")).toBeVisible()

      await page.getByText("XML Vorschau").click()

      await expect(
        page.getByText("<begriff>2021-02-01</begriff>"),
      ).toBeVisible()
      await expect(
        page.getByText("<begriff>2020-02-01</begriff>"),
      ).toBeVisible()
    })
  })

  test("validating deviating decision dates input", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await test.step("Add two identical deviating decision dates not possible, shows error", async () => {
      await page.getByLabel("Abweichendes Entscheidungsdatum anzeigen").click()
      await page.locator("#deviatingDecisionDates").fill("01.02.2020")
      await page.keyboard.press("Enter")
      await page.locator("#deviatingDecisionDates").fill("01.02.2020")
      await page.keyboard.press("Enter")
      await expect(page.getByText("01.02.2020 bereits vorhanden")).toBeVisible()
    })

    await test.step("Add invalid deviating decision dates not possible, former error replaced by new one", async () => {
      await page.locator("#deviatingDecisionDates").fill("29.02.2021")
      await page.keyboard.press("Enter")
      await expect(page.getByText("01.02.2020 bereits vorhanden")).toBeHidden()
      await expect(page.getByText("Kein valides Datum")).toBeVisible()
    })

    await test.step("Add deviating decision dates in future not possible, former error replaced by new one", async () => {
      await page.locator("#deviatingDecisionDates").fill("01.02.2040")
      await page.keyboard.press("Enter")
      await expect(page.getByText("Kein valides Datum")).toBeHidden()
      await expect(
        page.getByText(
          "Abweichendes Entscheidungsdatum darf nicht in der Zukunft liegen",
        ),
      ).toBeVisible()
    })

    // eslint-disable-next-line playwright/no-skipped-test
    await test.step.skip(
      // does not work with ris-ui component
      "On blur validates input, input is not saved with error",
      async () => {
        await page.locator("#deviatingDecisionDates").fill("01.02")
        await page.keyboard.press("Tab")
        await expect(
          page.getByText(
            "Abweichendes Entscheidungsdatum darf nicht in der Zukunft liegen",
          ),
        ).toBeHidden()
        await expect(page.getByText("Kein valides Datum")).toBeVisible()
      },
    )
    await save(page)

    await test.step("On reload, check if no invalid dates were saved", async () => {
      await page.reload()
      // If deviating data is available, it is automatically expanded
      await expect(page.getByText("01.02.2020")).toBeVisible()
    })
  })
})
