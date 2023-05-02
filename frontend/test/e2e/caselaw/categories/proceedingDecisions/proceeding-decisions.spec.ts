import { expect, Page } from "@playwright/test"
import { generateString } from "../../../../test-helper/dataGenerators"
import { navigateToCategories } from "../../e2e-utils"
import { testWithDocumentUnit as test } from "../../fixtures"

test.describe("Proceeding decisions", () => {
  test("rendering", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await expect(
      page.getByRole("heading", { name: "Vorgehende Entscheidungen" })
    ).toBeVisible()

    await toggleProceedingDecisionsSection(page)

    await expect(page.locator("[aria-label='Gericht Rechtszug']")).toBeVisible()
    await expect(
      page.locator("[aria-label='Dokumenttyp Rechtszug']")
    ).toBeVisible()
    await expect(
      page.locator("[aria-label='Aktenzeichen Rechtszug']")
    ).toBeVisible()
    await expect(
      page.locator("[aria-label='Entscheidungsdatum Rechtszug']")
    ).toBeVisible()
  })

  test("add and delete proceeding decision", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleProceedingDecisionsSection(page)

    const fileNumber = generateString()

    await fillProceedingDecisionInputs(page, {
      court: "AG Aalen",
      date: "2004-12-03",
      fileNumber: fileNumber,
      documentType: "AnU",
    })

    await page.getByText("Manuell Hinzufügen").click()

    await expect(
      page.getByText(`AG Aalen, AnU, 03.12.2004, ${fileNumber}`)
    ).toBeVisible()

    await page.reload()
    await toggleProceedingDecisionsSection(page)

    await expect(
      page.getByText(`AG Aalen, AnU, 03.12.2004, ${fileNumber}`)
    ).toHaveCount(1)

    // delete proceedingDecision
    await page
      .locator("div", { hasText: "AG Aalen" })
      .getByLabel("Löschen")
      .click()

    await expect(
      page.getByText(`AG Aalen, AnU, 03.12.2004, ${fileNumber}`)
    ).toHaveCount(0)

    page.reload()
    await toggleProceedingDecisionsSection(page)

    await expect(
      page.getByText(`AG Aalen, AnU, 03.12.2004, ${fileNumber}`)
    ).toHaveCount(0)
  })

  test("add same proceeding decision twice", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleProceedingDecisionsSection(page)
    await fillProceedingDecisionInputs(page, {
      court: "AG Aalen",
      date: "2004-12-03",
      fileNumber: "1a2b3c",
      documentType: "AnU",
    })

    await page.getByText("Manuell Hinzufügen").click()

    await expect(
      page.getByText("AG Aalen, AnU, 03.12.2004, 1a2b3c")
    ).toBeVisible()

    await fillProceedingDecisionInputs(page, {
      court: "AG Aalen",
      date: "2004-12-03",
      fileNumber: "1a2b3c",
      documentType: "AnU",
    })

    await page.getByText("Manuell Hinzufügen").click()

    await expect(
      page.getByText("AG Aalen, AnU, 03.12.2004, 1a2b3c")
    ).toHaveCount(2)
  })
})

export async function toggleProceedingDecisionsSection(
  page: Page
): Promise<void> {
  await page.locator("text=Vorgehende Entscheidungen").click()
}

export async function fillProceedingDecisionInputs(
  page: Page,
  values?: {
    court?: string
    date?: string
    fileNumber?: string
    documentType?: string
  },
  decisionIndex = 0
): Promise<void> {
  const fillInput = async (ariaLabel: string, value?: string) => {
    await page
      .locator(`[aria-label='${ariaLabel}']`)
      .nth(decisionIndex)
      .fill(value ?? generateString())
  }

  if (values?.court) {
    await fillInput("Gericht Rechtszug", values?.court)
    await page.getByText(values.court, { exact: true }).click()

    await expect(async () => {
      const inputValue = await page.getByLabel("Gericht Rechtszug").inputValue()
      expect(inputValue).toBe(values.court)
    }).toPass({ timeout: 5000 })
  }
  if (values?.date) {
    await fillInput("Entscheidungsdatum Rechtszug", values?.date)
  }
  if (values?.fileNumber) {
    await fillInput("Aktenzeichen Rechtszug", values?.fileNumber)
  }
  if (values?.documentType) {
    await fillInput("Dokumenttyp Rechtszug", values?.documentType)
    await page.locator("[aria-label='dropdown-option']").first().click()
  }
}
