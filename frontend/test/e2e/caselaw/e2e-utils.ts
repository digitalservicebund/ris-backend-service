import { expect, Page } from "@playwright/test"
import { generateString } from "../../test-helper/dataGenerators"

export const navigateToCategories = async (
  page: Page,
  documentNumber: string
) => {
  await page.goto(`/caselaw/documentunit/${documentNumber}/categories`)
  await expect(page.locator("text=Spruchkörper")).toBeVisible()
}

export const navigateToFiles = async (page: Page, documentNumber: string) => {
  await page.goto(`/caselaw/documentunit/${documentNumber}/files`)
  await expect(page.locator("h1:has-text('Dokumente')")).toBeVisible()
}

export const navigateToPublication = async (
  page: Page,
  documentNumber: string
) => {
  await page.goto(`/caselaw/documentunit/${documentNumber}/publication`)
  await expect(page.locator("h1:has-text('Veröffentlichen')")).toBeVisible()
}

export const uploadTestfile = async (page: Page, filename: string) => {
  const [fileChooser] = await Promise.all([
    page.waitForEvent("filechooser"),
    page.locator("text=oder Datei auswählen").click(),
  ])
  await fileChooser.setFiles("./test/e2e/caselaw/testfiles/" + filename)
  await expect(page.locator("text=Upload läuft")).toBeHidden()
  await expect(page.locator("text=Dokument wird geladen.")).toBeHidden()
}

export async function waitForSaving(
  body: () => Promise<void>,
  page: Page,
  options?: { clickSaveButton?: boolean; reload?: boolean }
) {
  if (options?.reload) {
    await page.reload()
  }

  const saveStatus = page.getByText(/Zuletzt gespeichert um .* Uhr/).first()
  let lastSaving: string | undefined = undefined
  if (await saveStatus.isVisible()) {
    lastSaving = /Zuletzt gespeichert um (.*) Uhr/.exec(
      await saveStatus.innerText()
    )?.[1] as string
  }

  await body()

  if (options?.clickSaveButton) {
    await page.locator("[aria-label='Stammdaten Speichern Button']").click()
  }

  await Promise.all([
    await expect(
      page.getByText(`Zuletzt gespeichert um`).first()
    ).toBeVisible(),
    lastSaving ??
      (await expect(
        page.getByText(`Zuletzt gespeichert um ${lastSaving} Uhr`).first()
      ).toBeHidden()),
  ])
}

export async function toggleFieldOfLawSection(page: Page): Promise<void> {
  await page.locator("text=Sachgebiete").click()
}

export async function deleteDocumentUnit(page: Page, documentNumber: string) {
  await page.goto("/")
  await expect(
    page.locator(`a[href*="/caselaw/documentunit/${documentNumber}/files"]`)
  ).toBeVisible()
  await page
    .locator(".table-row", {
      hasText: documentNumber,
    })
    .locator("[aria-label='Dokumentationseinheit löschen']")
    .click()
  await page.locator('button:has-text("Löschen")').click()
  await expect(
    page.locator(`a[href*="/caselaw/documentunit/${documentNumber}/files"]`)
  ).toBeHidden()
}

export async function documentUnitExists(
  page: Page,
  documentNumber: string
): Promise<boolean> {
  return (
    await (
      await page.request.get(`/api/v1/caselaw/documentunits/${documentNumber}`)
    ).text()
  ).includes("uuid")
}

export async function waitForInputValue(
  page: Page,
  selector: string,
  expectedValue: string
) {
  await page.waitForFunction(
    ({ selector, expectedValue }) => {
      const input = document.querySelector(selector) as HTMLInputElement
      return input && input.value === expectedValue
    },
    { selector, expectedValue }
  )
}

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

export async function checkIfProceedingDecisionCleared(page: Page) {
  await expect(page.getByLabel("Gericht Rechtszug")).toHaveValue("")
  await expect(page.getByLabel("Entscheidungsdatum Rechtszug")).toHaveValue("")
  await expect(page.getByLabel("Aktenzeichen Rechtszug")).toHaveValue("")
  await expect(page.getByLabel("Dokumenttyp Rechtszug")).toHaveValue("")
}
