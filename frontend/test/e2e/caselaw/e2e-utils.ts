import { expect, Page } from "@playwright/test"
import { generateString } from "../../test-helper/dataGenerators"

export const navigateToSearch = async (page: Page) => {
  await page.goto(`/caselaw`)
  await page.waitForURL("/caselaw")

  await expect(page.locator("text=Übersicht Rechtsprechung")).toBeVisible({
    timeout: 15000, // for backend warm up
  })
}

export const navigateToCategories = async (
  page: Page,
  documentNumber: string,
) => {
  await page.goto(`/caselaw/documentunit/${documentNumber}/categories`)
  await expect(page.locator("text=Spruchkörper")).toBeVisible({
    timeout: 15000, // for backend warm up
  })
  await expect(page.getByText(documentNumber)).toBeVisible()
}

export const navigateToFiles = async (page: Page, documentNumber: string) => {
  await page.goto(`/caselaw/documentunit/${documentNumber}/files`)
  await expect(page.locator("h1:has-text('Dokumente')")).toBeVisible({
    timeout: 15000, // for backend warm up
  })
}

export const navigateToPublication = async (
  page: Page,
  documentNumber: string,
) => {
  await page.goto(`/caselaw/documentunit/${documentNumber}/publication`)
  await expect(page.locator("h1:has-text('Veröffentlichen')")).toBeVisible({
    timeout: 15000, // for backend warm up
  })
}

export const publishDocumentationUnit = async (
  page: Page,
  documentNumber: string,
) => {
  await navigateToPublication(page, documentNumber)
  await page
    .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
    .click()
  await expect(page.locator("text=Email wurde versendet")).toBeVisible()

  await expect(page.locator("text=Xml Email Abgabe -")).toBeVisible()
  await expect(page.locator("text=In Veröffentlichung")).toBeVisible()
}

export const uploadTestfile = async (page: Page, filename: string) => {
  const [fileChooser] = await Promise.all([
    page.waitForEvent("filechooser"),
    page.locator("text=oder Datei auswählen").click(),
  ])
  await fileChooser.setFiles("./test/e2e/caselaw/testfiles/" + filename)
  await expect(async () => {
    await expect(page.getByLabel("Ladestatus")).not.toBeAttached()
  }).toPass({ timeout: 15000 })
}

export async function waitForSaving(
  body: () => Promise<void>,
  page: Page,
  options?: { clickSaveButton?: boolean; reload?: boolean },
) {
  if (options?.reload) {
    await page.reload()
  }

  const saveStatus = page.getByText(/Zuletzt .* Uhr/).first()
  let lastSaving: string | undefined = undefined
  if (await saveStatus.isVisible()) {
    lastSaving = /Zuletzt (.*) Uhr/.exec(
      await saveStatus.innerText(),
    )?.[1] as string
  }

  await body()

  if (options?.clickSaveButton) {
    await page.locator("[aria-label='Speichern Button']").click()
  }

  await Promise.all([
    await expect(page.getByText(`Zuletzt`).first()).toBeVisible(),
    lastSaving ??
      (await expect(
        page.getByText(`Zuletzt ${lastSaving} Uhr`).first(),
      ).toBeHidden()),
  ])
}

export async function toggleFieldOfLawSection(page: Page): Promise<void> {
  await page.locator("text=Sachgebiete").click()
}

export async function deleteDocumentUnit(page: Page, documentNumber: string) {
  const response = await page.request.delete(
    `/api/v1/caselaw/documentunits/${documentNumber}`,
  )
  expect(response.ok).toBeTruthy()
}

export async function deleteProcedure(page: Page, uuid: string) {
  const response = await page.request.delete(
    `/api/v1/caselaw/procedure/${uuid}`,
  )
  expect(response.ok()).toBeTruthy()
}

export async function documentUnitExists(
  page: Page,
  documentNumber: string,
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
  expectedValue: string,
) {
  await page.waitForFunction(
    ({ selector, expectedValue }) => {
      const input = document.querySelector(selector) as HTMLInputElement
      return input && input.value === expectedValue
    },
    { selector, expectedValue },
  )
}

export async function fillSearchInput(
  page: Page,
  values?: {
    fileNumber?: string
    courtType?: string
    courtLocation?: string
    decisionDate?: string
    decisionDateEnd?: string
    documentNumber?: string
    myDocOfficeOnly?: boolean
    status?: string
  },
) {
  const fillInput = async (ariaLabel: string, value = generateString()) => {
    const input = page.locator(`[aria-label='${ariaLabel}']`)
    await input.fill(value ?? ariaLabel)
    await waitForInputValue(page, `[aria-label='${ariaLabel}']`, value)
  }

  //reset search first
  await navigateToSearch(page)

  if (values?.fileNumber) {
    await fillInput("Aktenzeichen Suche", values?.fileNumber)
  }
  if (values?.courtType) {
    await fillInput("Gerichtstyp Suche", values?.courtType)
  }

  if (values?.courtLocation) {
    await fillInput("Gerichtsort Suche", values?.courtLocation)
  }

  if (values?.decisionDate) {
    await fillInput("Entscheidungsdatum Suche", values?.decisionDate)
  }

  if (values?.decisionDateEnd) {
    await fillInput("Entscheidungsdatum Suche Ende", values?.decisionDateEnd)
  }

  if (values?.documentNumber) {
    await fillInput("Dokumentnummer Suche", values?.documentNumber)
  }

  if (values?.myDocOfficeOnly === true) {
    const myDocOfficeOnlyCheckbox = page.getByLabel(
      "Nur meine Dokstelle Filter",
    )
    if (!(await myDocOfficeOnlyCheckbox.isChecked())) {
      await myDocOfficeOnlyCheckbox.click()
      await expect(myDocOfficeOnlyCheckbox).toBeChecked()
    }
  }

  if (values?.status) {
    const select = page.locator(`select[id="status"]`)
    await select.selectOption(values?.status)
  }

  await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
  await expect(page.getByLabel("Ladestatus")).toBeHidden()
}

export async function toggleNormsSection(page: Page): Promise<void> {
  await page.getByRole("button", { name: "Normen Aufklappen" }).click()
}

export async function fillPreviousDecisionInputs(
  page: Page,
  values?: {
    court?: string
    decisionDate?: string
    fileNumber?: string
    documentType?: string
    dateKnown?: boolean
    deviatingFileNumber?: string
  },
  decisionIndex = 0,
): Promise<void> {
  const fillInput = async (ariaLabel: string, value = generateString()) => {
    const input = page.locator(`[aria-label='${ariaLabel}']`).nth(decisionIndex)
    await input.fill(value ?? ariaLabel)
    await waitForInputValue(page, `[aria-label='${ariaLabel}']`, value)
  }

  if (values?.court) {
    await fillInput("Gericht Vorgehende Entscheidung", values?.court)
    await page.getByText(values.court, { exact: true }).click()
    await waitForInputValue(
      page,
      "[aria-label='Gericht Vorgehende Entscheidung']",
      values.court,
    )
  }
  if (values?.decisionDate) {
    await fillInput(
      "Entscheidungsdatum Vorgehende Entscheidung",
      values?.decisionDate,
    )
  }
  if (values?.fileNumber) {
    await fillInput("Aktenzeichen Vorgehende Entscheidung", values?.fileNumber)
  }
  if (values?.documentType) {
    await fillInput("Dokumenttyp Vorgehende Entscheidung", values?.documentType)
    await page.getByText(values.documentType, { exact: true }).click()
    await waitForInputValue(
      page,
      "[aria-label='Dokumenttyp Vorgehende Entscheidung']",
      values.documentType,
    )
  }

  if (values?.dateKnown === false) {
    const dateUnknownCheckbox = page.getByLabel("Datum unbekannt")
    if (!(await dateUnknownCheckbox.isChecked())) {
      await dateUnknownCheckbox.click()
      await expect(dateUnknownCheckbox).toBeChecked()
    }
  }

  if (values?.deviatingFileNumber) {
    if (
      !(await page
        .getByLabel("Abweichendes Aktenzeichen Vorgehende Entscheidung", {
          exact: true,
        })
        .isVisible())
    ) {
      await page
        .locator(
          "[aria-label='Abweichendes Aktenzeichen Vorgehende Entscheidung anzeigen']",
        )
        .click()
    }
    await fillInput(
      "Abweichendes Aktenzeichen Vorgehende Entscheidung",
      values?.deviatingFileNumber,
    )
  }
}

export async function fillEnsuingDecisionInputs(
  page: Page,
  values?: {
    pending?: boolean
    court?: string
    decisionDate?: string
    fileNumber?: string
    documentType?: string
    note?: string
  },
  decisionIndex = 0,
): Promise<void> {
  const fillInput = async (ariaLabel: string, value = generateString()) => {
    const input = page.locator(`[aria-label='${ariaLabel}']`).nth(decisionIndex)
    await input.fill(value ?? ariaLabel)
    await waitForInputValue(page, `[aria-label='${ariaLabel}']`, value)
  }

  if (values?.court) {
    await fillInput("Gericht Nachgehende Entscheidung", values?.court)
    await page.getByText(values.court, { exact: true }).click()
    await waitForInputValue(
      page,
      "[aria-label='Gericht Nachgehende Entscheidung']",
      values.court,
    )
  }
  if (values?.decisionDate) {
    await fillInput(
      "Entscheidungsdatum Nachgehende Entscheidung",
      values?.decisionDate,
    )
  }
  if (values?.fileNumber) {
    await fillInput("Aktenzeichen Nachgehende Entscheidung", values?.fileNumber)
  }
  if (values?.documentType) {
    await fillInput(
      "Dokumenttyp Nachgehende Entscheidung",
      values?.documentType,
    )
    await page.getByText(values.documentType, { exact: true }).click()
    await waitForInputValue(
      page,
      "[aria-label='Dokumenttyp Nachgehende Entscheidung']",
      values.documentType,
    )
  }
  if (values?.pending) {
    const pendingCheckbox = page.getByLabel("Anhängige Entscheidung")
    if (!(await pendingCheckbox.isChecked())) {
      await pendingCheckbox.click()
      await expect(pendingCheckbox).toBeChecked()
    }
  }
}

export async function fillNormInputs(
  page: Page,
  values?: {
    normAbbreviation?: string
    singleNorm?: string
    dateOfVersion?: string
    dateOfRelevance?: string
  },
): Promise<void> {
  const fillInput = async (ariaLabel: string, value = generateString()) => {
    const input = page.locator(`[aria-label='${ariaLabel}']`)
    await input.fill(value ?? ariaLabel)
    await waitForInputValue(page, `[aria-label='${ariaLabel}']`, value)
  }

  if (values?.normAbbreviation) {
    await fillInput("RIS-Abkürzung der Norm", values?.normAbbreviation)
    await page.getByRole("button", { name: "dropdown-option" }).click()
    await waitForInputValue(
      page,
      "[aria-label='RIS-Abkürzung der Norm']",
      values.normAbbreviation,
    )
  }

  if (values?.singleNorm) {
    await fillInput("Einzelnorm der Norm", values?.singleNorm)
  }
  if (values?.dateOfVersion) {
    await fillInput("Fassungsdatum der Norm", values?.dateOfVersion)
  }
  if (values?.dateOfRelevance) {
    await fillInput("Jahr der Norm", values?.dateOfRelevance)
  }
}

export async function fillActiveCitationInputs(
  page: Page,
  values?: {
    citationType?: string
    court?: string
    decisionDate?: string
    fileNumber?: string
    documentType?: string
  },
): Promise<void> {
  const fillInput = async (ariaLabel: string, value = generateString()) => {
    const input = page.locator(`[aria-label='${ariaLabel}']`)
    await input.fill(value ?? ariaLabel)
    await waitForInputValue(page, `[aria-label='${ariaLabel}']`, value)
  }

  if (values?.citationType) {
    await fillInput("Art der Zitierung", values?.citationType)
    await page.getByText(values.citationType, { exact: true }).click()
    await waitForInputValue(
      page,
      "[aria-label='Art der Zitierung']",
      values.citationType,
    )
  }

  if (values?.court) {
    await fillInput("Gericht Aktivzitierung", values?.court)
    await page.getByText(values.court, { exact: true }).click()
    await waitForInputValue(
      page,
      "[aria-label='Gericht Aktivzitierung']",
      values.court,
    )
  }
  if (values?.decisionDate) {
    await fillInput("Entscheidungsdatum Aktivzitierung", values?.decisionDate)
  }
  if (values?.fileNumber) {
    await fillInput("Aktenzeichen Aktivzitierung", values?.fileNumber)
  }
  if (values?.documentType) {
    await fillInput("Dokumenttyp Aktivzitierung", values?.documentType)
    await page.getByText(values.documentType, { exact: true }).click()
    await waitForInputValue(
      page,
      "[aria-label='Dokumenttyp Aktivzitierung']",
      values.documentType,
    )
  }
}

export async function checkIfPreviousDecisionCleared(page: Page) {
  ;[
    "Gericht Vorgehende Entscheidung",
    "Entscheidungsdatum Vorgehende Entscheidung",
    "Aktenzeichen Vorgehende Entscheidung",
    "Dokumenttyp Vorgehende Entscheidung",
  ].forEach((ariaLabel) =>
    waitForInputValue(page, `[aria-label='${ariaLabel}']`, ""),
  )
}
