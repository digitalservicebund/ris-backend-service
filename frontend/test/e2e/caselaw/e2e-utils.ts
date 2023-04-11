import { expect } from "@playwright/test"
import { Page } from "playwright"
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

export const waitForSaving = async (page: Page): Promise<void> => {
  await expect(
    page.locator("text=Zuletzt gespeichert um >> nth=0")
  ).toBeVisible()
  await expect(
    page.locator("text=Zuletzt gespeichert um >> nth=1")
  ).toBeVisible()
}

export async function clickSaveButton(page: Page): Promise<void> {
  await page.locator("[aria-label='Stammdaten Speichern Button']").click()
  await expect(
    page.locator("text=Zuletzt gespeichert um").first()
  ).toBeVisible()
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
    await page.locator(`text=${values?.court}`).click()
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
