import { expect, JSHandle, Locator, Page, Request } from "@playwright/test"
import { caselawTest as test } from "./fixtures"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import SingleNorm from "@/domain/singleNorm"
import { generateString } from "~/test-helper/dataGenerators"

/* eslint-disable playwright/no-conditional-in-test */

function scrollToID(category?: DocumentUnitCategoriesEnum): string {
  return category ? "#" + category : ""
}

const getAllQueryParamsFromUrl = (page: Page): string => {
  const url = new URL(page.url())
  const params = url.searchParams.toString()
  return params ? `?${params}` : ""
}

export const navigateToSearch = async (
  page: Page,
  { navigationBy }: { navigationBy: "click" | "url" } = { navigationBy: "url" },
) => {
  await test.step("Navigate to 'Suche'", async () => {
    if (navigationBy === "url") {
      await page.goto(`/caselaw/search`, { waitUntil: "domcontentloaded" })
    } else {
      await page.getByTestId("search-navbar-button").click()
    }
    await page.waitForURL("/caselaw/search")

    await expect(
      page.getByTestId("document-unit-search-entry-form"),
    ).toBeVisible({
      timeout: 15000, // for backend warm up
    })
  })
}

export const navigateToInbox = async (
  page: Page,
  { navigationBy }: { navigationBy: "click" | "url" } = { navigationBy: "url" },
) => {
  await test.step("Navigate to 'Eingang'", async () => {
    if (navigationBy === "url") {
      await page.goto(`/caselaw/inbox`, { waitUntil: "domcontentloaded" })
    } else {
      await page.getByTestId("inbox-navbar-button").click()
    }
    await page.waitForURL("/caselaw/inbox")

    await expect(page.getByText("Fremdanlagen")).toBeVisible({
      timeout: 15000, // for backend warm up
    })
  })
}

export const navigateToProcedures = async (
  page: Page,
  searchParam?: string,
) => {
  await test.step("Navigate to 'Vorgänge'", async () => {
    const urlPostFix = searchParam ? `?q=${searchParam}` : ""
    await page.goto(`/caselaw/procedures` + urlPostFix)
    await expect(page.getByLabel("Nach Vorgängen suchen")).toBeVisible()
  })
}

export const navigateToCategories = async (
  page: Page,
  documentNumber: string,
  options?: {
    category?: DocumentUnitCategoriesEnum
    skipAssert?: boolean
    type?: "pending-proceeding" | "documentunit"
  },
) => {
  await test.step("Navigate to 'Rubriken'", async () => {
    const documentType = options?.type ?? "documentunit"
    const queryParams = getAllQueryParamsFromUrl(page)
    const baseUrl =
      `/caselaw/${documentType}/${documentNumber}/categories${queryParams}` +
      scrollToID(options?.category)

    await page.goto(baseUrl)

    if (options?.skipAssert) return

    await expect(
      page.getByRole("heading", { name: documentNumber }),
    ).toBeVisible({
      timeout: 15000, // for backend warm up
    })
  })
}

export const navigateToReferences = async (
  page: Page,
  documentNumber: string,
) => {
  await test.step("Navigate to 'Fundstellen'", async () => {
    const baseUrl = `/caselaw/documentunit/${documentNumber}/references`

    await page.goto(baseUrl)
    await expect(
      page.getByRole("heading", {
        name: "Rechtsprechungsfundstellen",
        exact: true,
      }),
    ).toBeVisible()
  })
}

export const navigateToPeriodicalEvaluation = async (page: Page) => {
  await test.step("Navigate to 'Periodika'", async () => {
    const baseUrl = "/caselaw/periodical-evaluation"

    await page.goto(baseUrl)

    await expect(page.getByTestId("periodical-evaluation-title")).toBeVisible()
  })
}

export const navigateToPeriodicalReferences = async (
  page: Page,
  editionId: string,
) => {
  await test.step("Navigate to 'Periodikumauswertung > Fundstellen'", async () => {
    const baseUrl = `/caselaw/periodical-evaluation/${editionId}/references`
    await getRequest(baseUrl, page)
    await expect(page.getByTestId("references-title")).toBeVisible()
  })
}

export const navigateToPeriodicalHandover = async (
  page: Page,
  editionId: string,
) => {
  await test.step("Navigate to 'Periodikumauswertung > Übergabe an jDV'", async () => {
    const baseUrl = `/caselaw/periodical-evaluation/${editionId}/handover`

    await page.goto(baseUrl)
    await expect(page.getByTestId("handover-title")).toBeVisible()
  })
}
export const navigateToPreview = async (
  page: Page,
  documentNumber: string,
  options?: {
    skipAssert?: boolean
    type?: "pending-proceeding" | "documentunit"
  },
) => {
  await test.step("Navigate to 'Vorschau'", async () => {
    const queryParams = getAllQueryParamsFromUrl(page)
    const documentType = options?.type ?? "documentunit"
    const baseUrl = `/caselaw/${documentType}/${documentNumber}/preview${queryParams}`

    await page.goto(baseUrl)

    if (options?.skipAssert) return
    await expect(page.getByTestId("preview")).toBeVisible({
      timeout: 15000, // for backend warm-up
    })
    await expect(page.getByText(documentNumber)).toBeVisible()
  })
}

export const navigateToAttachments = async (
  page: Page,
  documentNumber: string,
  options?: {
    skipAssert?: boolean
  },
) => {
  await test.step("Navigate to 'Dokumente'", async () => {
    const queryParams = getAllQueryParamsFromUrl(page)
    await page.goto(
      `/caselaw/documentunit/${documentNumber}/attachments${queryParams}`,
    )
    if (options?.skipAssert) return

    await expect(page.getByTestId("document-unit-attachments")).toBeVisible({
      timeout: 15000, // for backend warm up
    })
  })
}

export const navigateToManagementData = async (
  page: Page,
  documentNumber: string,
  options?: {
    type?: "pending-proceeding" | "documentunit"
  },
) => {
  await test.step("Navigate to 'Verwaltungsdaten'", async () => {
    const documentType = options?.type ?? "documentunit"
    const baseUrl = `/caselaw/${documentType}/${documentNumber}/managementdata`
    await getRequest(baseUrl, page)
    await expect(page.getByTestId("title").first()).toHaveText(
      "Verwaltungsdaten",
    )
  })
}

export const navigateToHandover = async (
  page: Page,
  documentNumber: string,
  options?: {
    skipAssert?: boolean
  },
) => {
  await test.step("Navigate to 'Übergabe an jDV'", async () => {
    await page.goto(`/caselaw/documentunit/${documentNumber}/handover`)
    if (options?.skipAssert) return
    await expect(page.locator("h1:has-text('Übergabe an jDV')")).toBeVisible({
      timeout: 15000, // for backend warm up
    })
  })
}

export const navigateToSettings = async (page: Page) => {
  await test.step("Navigate to Einstellungen'", async () => {
    await page.goto(`/settings`)
    await expect(page.locator("h3:has-text('Einstellungen')")).toBeVisible()
    await expect(page.getByText("API Key", { exact: true })).toBeVisible()
  })
}

export const handoverDocumentationUnit = async (
  page: Page,
  documentNumber: string,
) => {
  await test.step(`Übergebe Dokumentationseinheit ${documentNumber}`, async () => {
    await navigateToHandover(page, documentNumber)
    await expect(page.getByText("XML Vorschau")).toBeVisible()
    await page.getByLabel("Dokumentationseinheit an jDV übergeben").click()
    await expect(page.getByText("Email wurde versendet")).toBeVisible()

    await expect(page.getByText("Xml Email Abgabe -")).toBeVisible()
  })
}

export const uploadTestfile = async (
  page: Page,
  filename: string | string[],
  options?: {
    skipAssert?: boolean
  },
) => {
  const [fileChooser] = await Promise.all([
    page.waitForEvent("filechooser"),
    page.getByText("Oder hier auswählen").click(),
  ])
  const fileNames = Array.isArray(filename) ? filename : [filename]
  await fileChooser.setFiles(
    fileNames.map((file) => "./test/e2e/caselaw/testfiles/" + file),
  )
  await fileChooser.setFiles(
    fileNames.map((file) => "./test/e2e/caselaw/testfiles/" + file),
  )
  await expect(async () => {
    await expect(page.getByLabel("Ladestatus")).not.toBeAttached()
  }).toPass({ timeout: 15000 })

  // Assert upload block
  if (options?.skipAssert) return
  await expect(page.getByText("Hochgeladen am")).toBeVisible()

  for (const file of fileNames) {
    const lastFileName = page.getByRole("cell", { name: file }).last()
    await expect(lastFileName).toBeVisible()
  }
}

export async function save(page: Page) {
  const saveRequest = page.waitForRequest("**/api/v1/caselaw/documentunits/*", {
    timeout: 5_000,
  })
  await page.getByLabel("Speichern Button", { exact: true }).click()
  await saveRequest
  await expect(page.getByText(`Zuletzt`).first()).toBeVisible()
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
  //reset search first
  await navigateToSearch(page)

  if (values?.fileNumber) {
    await fillInput(page, "Aktenzeichen Suche", values?.fileNumber)
  }
  if (values?.courtType) {
    await fillInput(page, "Gerichtstyp Suche", values?.courtType)
  }

  if (values?.courtLocation) {
    await fillInput(page, "Gerichtsort Suche", values?.courtLocation)
  }

  if (values?.decisionDate) {
    await fillInput(page, "Entscheidungsdatum Suche", values?.decisionDate)
  }

  if (values?.decisionDateEnd) {
    await fillInput(
      page,
      "Entscheidungsdatum Suche Ende",
      values?.decisionDateEnd,
    )
  }

  if (values?.documentNumber) {
    await fillInput(page, "Dokumentnummer Suche", values?.documentNumber)
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
    await page.getByLabel("Status Suche").click()
    await page
      .getByRole("option", {
        name: values?.status,
        exact: true,
      })
      .click()
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
    const input = page.getByLabel(ariaLabel, { exact: true }).nth(decisionIndex)
    await input.fill(value ?? ariaLabel)
    await expect(page.getByLabel(ariaLabel, { exact: true })).toHaveValue(value)
  }

  if (values?.court) {
    await fillInput("Gericht Vorgehende Entscheidung", values?.court)
    await page.getByText(values.court, { exact: true }).click()
    await expect(
      page.getByLabel("Gericht Vorgehende Entscheidung", { exact: true }),
    ).toHaveValue(values.court)
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
    await expect(
      page.getByLabel("Dokumenttyp Vorgehende Entscheidung", { exact: true }),
    ).toHaveValue(values.documentType)
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
        .getByLabel(
          "Abweichendes Aktenzeichen Vorgehende Entscheidung anzeigen",
          { exact: true },
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
    const input = page.getByLabel(ariaLabel, { exact: true }).nth(decisionIndex)
    await input.fill(value ?? ariaLabel)
    await expect(page.getByLabel(ariaLabel, { exact: true })).toHaveValue(value)
  }

  if (values?.court) {
    await fillInput("Gericht Nachgehende Entscheidung", values?.court)
    await page.getByText(values.court, { exact: true }).click()
    await expect(
      page.getByLabel("Gericht Nachgehende Entscheidung", { exact: true }),
    ).toHaveValue(values.court)
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
    await expect(
      page.getByLabel("Dokumenttyp Nachgehende Entscheidung", { exact: true }),
    ).toHaveValue(values.documentType)
  }
  if (values?.pending) {
    const pendingCheckbox = page.getByLabel("Anhängige Entscheidung")
    if (!(await pendingCheckbox.isChecked())) {
      await pendingCheckbox.click()
      await expect(pendingCheckbox).toBeChecked()
    }
  }
}

export async function fillInput(
  page: Page,
  ariaLabel: string,
  value = generateString(),
) {
  const input = page.getByLabel(ariaLabel, { exact: true })
  await input.fill(value ?? ariaLabel)
  await expect(page.getByLabel(ariaLabel, { exact: true })).toHaveValue(value)
}

export async function fillCombobox(
  page: Page,
  ariaLabel: string,
  value: string,
) {
  await fillInput(page, ariaLabel, value)
  await expect(page.getByTestId("combobox-spinner")).toBeHidden()
  await page.getByText(value, { exact: true }).click()
}

export async function clearInput(page: Page, ariaLabel: string) {
  const input = page.getByLabel(ariaLabel, { exact: true })
  await input.clear()
}

export async function clearTextField(page: Page, locator: Locator) {
  await locator.click({ clickCount: 3 })
  await page.keyboard.press("Backspace")
}

export async function fillNormInputs(
  page: Page,
  values?: {
    normAbbreviation?: string
    singleNorms?: SingleNorm[]
  },
): Promise<void> {
  if (values?.normAbbreviation) {
    await fillCombobox(page, "RIS-Abkürzung", values.normAbbreviation)
  }
  if (values?.singleNorms) {
    for (let index = 0; index < values.singleNorms.length; index++) {
      const entry = values.singleNorms[index]
      if (entry.singleNorm) {
        const input = page.getByLabel("Einzelnorm der Norm").nth(index)
        await input.fill(entry.singleNorm)
        await expect(
          page.getByLabel("Einzelnorm der Norm", { exact: true }).nth(index),
        ).toHaveValue(entry.singleNorm.trim())
      }

      if (entry.dateOfVersion) {
        const input = page.getByLabel("Fassungsdatum der Norm").nth(index)
        await input.fill(entry.dateOfVersion)
        await expect(
          page.getByLabel("Fassungsdatum der Norm", { exact: true }).nth(index),
        ).toHaveValue(entry.dateOfVersion)
      }
      if (entry.dateOfRelevance) {
        const input = page.getByLabel("Jahr der Norm").nth(index)
        await input.fill(entry.dateOfRelevance)
        await expect(
          page.getByLabel("Jahr der Norm", { exact: true }).nth(index),
        ).toHaveValue(entry.dateOfRelevance)
      }
    }
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
  if (values?.citationType) {
    await fillInput(page, "Art der Zitierung", values?.citationType)
    await page.getByText(values.citationType, { exact: true }).click()
    await expect(
      page.getByLabel("Art der Zitierung", { exact: true }),
    ).toHaveValue(values.citationType)
  }

  if (values?.court) {
    await fillInput(page, "Gericht Aktivzitierung", values?.court)
    await page.getByText(values.court, { exact: true }).click()
    await expect(
      page.getByLabel("Gericht Aktivzitierung", { exact: true }),
    ).toHaveValue(values.court)
  }
  if (values?.decisionDate) {
    await fillInput(
      page,
      "Entscheidungsdatum Aktivzitierung",
      values?.decisionDate,
    )
  }
  if (values?.fileNumber) {
    await fillInput(page, "Aktenzeichen Aktivzitierung", values?.fileNumber)
  }
  if (values?.documentType) {
    await fillInput(page, "Dokumenttyp Aktivzitierung", values?.documentType)
    await page.getByText(values.documentType, { exact: true }).click()
    await expect(
      page.getByLabel("Dokumenttyp Aktivzitierung", { exact: true }),
    ).toHaveValue(values.documentType)
  }
}

export async function copyPasteTextFromAttachmentIntoEditor(
  page: Page,
  attachmentLocator: Locator,
  editor: Locator,
): Promise<void> {
  await attachmentLocator.selectText()

  // copy from side panel to clipboard
  await page.keyboard.press("ControlOrMeta+C")

  // paste from clipboard into input field
  await editor.click()

  await page.keyboard.press("ControlOrMeta+V")

  await page
    .locator(`[aria-label='Nicht-druckbare Zeichen']:not([disabled])`)
    .click()
}

export async function getRequest(url: string, page: Page): Promise<Request> {
  const requestFinishedPromise = page.waitForEvent("requestfinished")
  // waitUntil, because timeout NS_ERROR_FAILURE is a common issue in Firefox
  await page.goto(url, { waitUntil: "domcontentloaded" })

  return await requestFinishedPromise
}

export async function clickCategoryButton(testId: string, page: Page) {
  await test.step(`click '${testId}' button to open category`, async () => {
    const button = page.getByRole("button", { name: testId, exact: true })

    if (await button.isVisible()) {
      await button.click()
    }
    await expect(page.getByTestId(testId)).toBeVisible()
  })
}

export async function assignProcedureToDocUnit(
  page: Page,
  documentNumber: string,
  /** Needed to be able to delete all procedures with prefix afterward */
  prefix: string,
) {
  let procedureName = ""
  await test.step("Internal user assigns new procedure to doc unit", async () => {
    await navigateToCategories(page, documentNumber)
    procedureName = generateString({ length: 10, prefix: prefix })
    await page.getByLabel("Vorgang", { exact: true }).fill(procedureName)
    await page
      .getByText(`${procedureName} neu erstellen`)
      .click({ timeout: 5_000 })
    await save(page)
  })
  return procedureName
}

export async function searchForDocUnitWithFileNumberAndDecisionDate(
  page: Page,
  fileNumber: string,
  date: string,
) {
  await fillInput(page, "Gericht", "AG Aachen")
  await page.getByText("AG Aachen", { exact: true }).click()
  await fillInput(page, "Aktenzeichen", fileNumber)
  await fillInput(page, "Entscheidungsdatum", date)
  await fillInput(page, "Dokumenttyp", "AnU")

  await page
    .locator("button")
    .filter({ hasText: "AnerkenntnisurteilAnU" })
    .click()

  await page.getByText("Suchen").click()
}

/**
 * Navigate through the top extra content side panel bar
 * Will throw an error if tried x times
 * @param page
 * @param locator
 * @param type
 * @param maxAttemptCount
 */
export async function extraContentMenuKeyboardNavigator(
  page: Page,
  locator: Locator,
  type: "Tab" | "Shift+Tab",
  maxAttemptCount = 7,
) {
  let attemptCount = 0
  let previousDocumentButtonIsFocused = false
  while (!previousDocumentButtonIsFocused) {
    await page.keyboard.press(type)

    try {
      await expect(locator).toBeFocused({ timeout: 500 })
      attemptCount++
      previousDocumentButtonIsFocused = true
    } catch {
      attemptCount++
      if (attemptCount > maxAttemptCount) {
        throw new Error(
          `Exceeded maximum allowed attempts (${maxAttemptCount})`,
        )
      }
    }
  }
}

export async function createDataTransfer(
  page: Page,
  fileContent: Buffer,
  fileName: string,
  fileType: string,
): Promise<JSHandle<DataTransfer>> {
  return page.evaluateHandle(
    async ({ buffer, fileName, fileType }) => {
      const blob = await fetch(buffer).then((value) => value.blob())
      const file = new File([blob], fileName, { type: fileType })
      const data = new DataTransfer()
      data.items.add(file)
      return data
    },
    {
      buffer: `data:application/octet-stream;base64,${fileContent.toString(
        "base64",
      )}`,
      fileName,
      fileType,
    },
  )
}

export async function assignUserGroupToProcedure(
  page: Page,
  procedureName: string,
) {
  await test.step("Internal user assigns a user group to the given procedure", async () => {
    await navigateToProcedures(page, procedureName)

    const assignRequest = page.waitForRequest(
      "**/api/v1/caselaw/procedure/*/assign/*",
      { timeout: 5_000 },
    )

    await page
      .getByLabel("Vorgang Listenelement")
      .getByLabel("dropdown input")
      .click()
    await page
      .getByRole("option", {
        name: "Extern",
        exact: true,
      })
      .click()
    await assignRequest
    await page.reload()

    await expect(
      page.getByLabel("Vorgang Listenelement").getByLabel("dropdown input"),
      // The id of the user group "Extern"
    ).toHaveText(/.+/, { timeout: 5_000 })
  })
}

export async function unassignUserGroupFromProcedure(
  page: Page,
  procedureName: string,
) {
  await test.step("Internal user unassigns a user group from the given procedure", async () => {
    await navigateToProcedures(page, procedureName)

    const unassignRequest = page.waitForRequest(
      "**/api/v1/caselaw/procedure/*/unassign",
      { timeout: 5_000 },
    )

    await page
      .getByLabel("Vorgang Listenelement")
      .getByLabel("dropdown input")
      .click()

    await page
      .getByRole("option", {
        name: "Nicht zugewiesen",
        exact: true,
      })
      .click({ timeout: 5000 })

    await unassignRequest
    await page.reload()

    await expect(
      page.getByLabel("Vorgang Listenelement").getByLabel("dropdown input"),
      // The unassigned option has an empty value
    ).toHaveText("Nicht zugewiesen", { timeout: 5_000 })
  })
}

export async function createPendingHandoverDecisionForBGH(
  page: Page,
  edition: LegalPeriodicalEdition,
  citation: string,
  court: string,
  date: string,
  fileNumber: string,
  doctype: string,
) {
  await navigateToPeriodicalReferences(page, edition.id ?? "")
  const addReferenceButton = page.getByLabel("Weitere Angabe")

  if (await addReferenceButton.isVisible()) {
    await addReferenceButton.click()
  }

  await fillInput(page, "Zitatstelle *", citation)
  await fillInput(page, "Klammernzusatz", "L")
  await searchForDocUnit(page, court, date, fileNumber, doctype)

  await expect(page.getByText("Übernehmen und weiter bearbeiten")).toBeVisible()

  await expect(page.getByLabel("Dokumentationsstelle auswählen")).toHaveValue(
    "BGH",
  )

  const pagePromise = page.context().waitForEvent("page")
  await page.getByText("Übernehmen und weiter bearbeiten").click()
  const newTab = await pagePromise
  await expect(newTab).toHaveURL(
    /\/caselaw\/documentunit\/[A-Z0-9]{13}\/categories$/,
  )
  const documentNumber = /caselaw\/documentunit\/(.*)\/categories/g.exec(
    newTab.url(),
  )?.[1] as string
  return documentNumber
}

export async function searchForDocUnit(
  page: Page,
  court?: string,
  date?: string,
  fileNumber?: string,
  documentType?: string,
) {
  if (fileNumber) {
    await fillInput(page, "Aktenzeichen", fileNumber)
  }
  if (court) {
    await fillCombobox(page, "Gericht", court)
  }
  if (date) {
    await fillInput(page, "Entscheidungsdatum", date)
  }
  if (documentType) {
    await fillCombobox(page, "Dokumenttyp", documentType)
  }

  await page.getByText("Suchen").click()
}

export async function expectHistoryCount(page: Page, count: number) {
  await expect(
    page.getByTestId("document-unit-history-log").getByRole("row"),
    // header counts as row
  ).toHaveCount(count + 1)
}

export async function expectHistoryLogRow(
  page: Page,
  index: number,
  createdBy: string,
  description: string,
) {
  const historyRow = page
    .getByTestId("document-unit-history-log")
    .getByRole("row")
    // Header has index 0
    .nth(index + 1)
  const createdAtCell = historyRow.getByRole("cell").nth(0)
  const createdByCell = historyRow.getByRole("cell").nth(1)
  const descriptionCell = historyRow.getByRole("cell").nth(2)
  await expect(createdAtCell).toHaveText(
    /^\d{2}\.\d{2}\.\d{4} um \d{2}:\d{2} Uhr$/,
  )
  await expect(createdByCell).toHaveText(createdBy)
  await expect(descriptionCell).toHaveText(description)
}

export async function navigateToCategoryImport(
  page: Page,
  documentNumber: string,
  options?: {
    category?: DocumentUnitCategoriesEnum
    skipAssert?: boolean
    type?: "pending-proceeding" | "documentunit"
  },
) {
  await navigateToCategories(page, documentNumber, options)
  await page.getByLabel("Seitenpanel öffnen").click()
  await page.getByTestId("category-import-button").click()

  await expect(page.getByText("Rubriken importieren")).toBeVisible()
  await expect(page.getByLabel("Dokumentnummer Eingabefeld")).toBeVisible()
}

export async function searchForDocumentUnitToImport(
  page: Page,
  documentNumber: string,
) {
  await page
    .getByRole("textbox", { name: "Dokumentnummer Eingabefeld" })
    .fill(documentNumber)

  await expect(
    page.getByRole("button", { name: "Dokumentationseinheit laden" }),
  ).toBeEnabled()
  await page
    .getByRole("button", { name: "Dokumentationseinheit laden" })
    .click()
}
