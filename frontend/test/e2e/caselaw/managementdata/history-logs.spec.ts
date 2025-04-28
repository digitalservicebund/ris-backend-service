import { expect, Page } from "@playwright/test"
import dayjs from "dayjs"
import {
  fillInput,
  navigateToAttachments,
  navigateToCategories,
  navigateToManagementData,
  navigateToPeriodicalReferences,
  navigateToSearch,
  save,
  uploadTestfile,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"
import { generateString } from "~/test-helper/dataGenerators"

const formattedDate = dayjs().format("DD.MM.YYYY")

/* eslint-disable playwright/expect-expect */
test.describe("Historie in Verwaltungsdaten", { tag: ["@RISDEV-7248"] }, () => {
  test("Es wird geloggt, wenn eine Userin etwas an den Rubriken geändert hat", async ({
    page,
    documentNumber,
  }) => {
    await test.step("Historie einer neu erstellten Dokeinheit ist leer", async () => {
      await navigateToManagementData(page, documentNumber)
      page.getByTestId("document-unit-history-log").getByText("Keine Daten")
    })

    await test.step("Nach einer Bearbeitung wird ein Historien-Log erstellt", async () => {
      await navigateToCategories(page, documentNumber)
      await page.getByLabel("ECLI", { exact: true }).fill("ECLI-12345")
      await navigateToManagementData(page, documentNumber)
      await expectHistoryCount(page, 1)
      await expectHistoryLogRow(
        page,
        0,
        "DS (e2e_tests DigitalService)",
        `Dokeinheit bearbeitet`,
      )
    })

    await test.step("Nach einer erneuten Bearbeitung durch den selben User wird der bestehende Log aktualisiert", async () => {
      await navigateToCategories(page, documentNumber)
      await page.getByLabel("ECLI", { exact: true }).fill("ECLI-anderer-Wert")
      await navigateToManagementData(page, documentNumber)
      await expectHistoryCount(page, 1)
      await expectHistoryLogRow(
        page,
        0,
        "DS (e2e_tests DigitalService)",
        `Dokeinheit bearbeitet`,
      )
    })
  })

  test("Es wird jede Vorgangsänderung geloggt", async ({
    page,
    documentNumber,
  }) => {
    let newProcedure: string

    await test.step("Setze den Vorgang", async () => {
      await navigateToCategories(page, documentNumber)
      const testPrefix = generateString({ length: 10 })
      newProcedure = testPrefix + generateString({ length: 10 })
      await page.getByLabel("Vorgang", { exact: true }).fill(newProcedure)
      await page.getByText(`${newProcedure} neu erstellen`).click()
    })

    await test.step("In der Historie sind zwei Historien-Events", async () => {
      await navigateToManagementData(page, documentNumber)
      await expectHistoryCount(page, 2)
      await expectHistoryLogRow(
        page,
        0,
        "DS (e2e_tests DigitalService)",
        `Dokeinheit bearbeitet`,
      )
      await expectHistoryLogRow(
        page,
        1,
        "DS (e2e_tests DigitalService)",
        `Vorgang gesetzt: ${newProcedure}`,
      )
    })
  })

  test("Es wird das Hochladen/Löschen eines Word-Dokuments geloggt", async ({
    page,
    documentNumber,
  }) => {
    await test.step("Lade Word-Dokument hoch", async () => {
      await navigateToAttachments(page, documentNumber)
      await uploadTestfile(page, "sample.docx")
    })

    await test.step("In der Historie gibt es ein Hochladen und ein Bearbeitet Historien-Event", async () => {
      await navigateToManagementData(page, documentNumber)
      await expectHistoryCount(page, 2)
      await expectHistoryLogRow(
        page,
        0,
        "DS (e2e_tests DigitalService)",
        `Dokeinheit bearbeitet`,
      )
      await expectHistoryLogRow(
        page,
        1,
        "DS (e2e_tests DigitalService)",
        `Word-Dokument hinzugefügt`,
      )
    })

    await test.step("Lösche Word-Dokument", async () => {
      await navigateToAttachments(page, documentNumber)
      await page.getByTestId("list-entry-0").getByLabel("Datei löschen").click()
      // Dialog bestätigen
      await page.getByLabel("Löschen", { exact: true }).click()
    })

    await test.step("In der Historie gibt es zusätzlich ein Dokument gelöscht Historien-Event", async () => {
      await navigateToManagementData(page, documentNumber)
      await expectHistoryCount(page, 3)
      await expectHistoryLogRow(
        page,
        0,
        "DS (e2e_tests DigitalService)",
        `Word-Dokument gelöscht`,
      )
    })
  })

  test("Angaben zum Bearbeiter sind nur innerhalb einer Dokstelle sichtbar", async ({
    page,
    pageWithBghUser,
    edition,
  }) => {
    const randomFileNumber = generateString()
    let documentNumber: string

    await test.step("Fremdanlage für BGH anlegen", async () => {
      await navigateToPeriodicalReferences(page, edition.id ?? "")
      await fillInput(page, "Zitatstelle *", "12")
      await expect(page.getByLabel("Zitatstelle *")).toHaveValue("12")
      await fillInput(page, "Klammernzusatz", "L")
      await searchForDocUnit(
        page,
        "AG Aachen",
        formattedDate,
        randomFileNumber,
        "AnU",
      )

      await expect(
        page.getByText("Übernehmen und weiter bearbeiten"),
      ).toBeVisible()

      await expect(
        page.getByLabel("Zuständige Dokumentationsstelle"),
      ).toHaveValue("BGH")

      const pagePromise = page.context().waitForEvent("page")
      await page.getByText("Übernehmen und weiter bearbeiten").click()
      const newTab = await pagePromise
      documentNumber = await verifyDocUnitOpensInNewTab(
        newTab,
        randomFileNumber,
      )
    })

    await test.step("Fremdanlage als BGH-User annehmen", async () => {
      await navigateToSearch(pageWithBghUser)
      await pageWithBghUser
        .getByRole("textbox", { name: "Dokumentnummer Suche" })
        .fill(documentNumber)
      await pageWithBghUser
        .getByRole("button", { name: "Nach Dokumentationseinheiten" })
        .click()
      await pageWithBghUser
        .getByTestId(`listEntry_${documentNumber}`)
        .getByRole("button", { name: "Dokumentationseinheit übernehmen" })
        .click()
    })

    await test.step("BGH-User sieht nur BGH Namen in Verwaltungsdaten", async () => {
      await navigateToManagementData(pageWithBghUser, documentNumber)
      await expectHistoryCount(pageWithBghUser, 2)
      await expectHistoryLogRow(
        pageWithBghUser,
        1,
        "DS",
        `Dokeinheit bearbeitet`,
      )
    })

    await test.step("Es wird jede Statusänderung geloggt", async () => {
      await expectHistoryLogRow(
        pageWithBghUser,
        0,
        "BGH (BGH testUser)",
        `Status geändert: Fremdanlage → Unveröffentlicht`,
      )
    })
  })
})

async function expectHistoryCount(page: Page, count: number) {
  await expect(
    page.getByTestId("document-unit-history-log").getByRole("row"),
    // header counts as row
  ).toHaveCount(count + 1)
}

async function searchForDocUnit(
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
    await fillInput(page, "Gericht", court)
    await page.getByText(court, { exact: true }).click()
  }
  if (date) {
    await fillInput(page, "Entscheidungsdatum", date)
  }
  if (documentType) {
    await fillInput(page, "Dokumenttyp", documentType)
    await page.getByText("Anerkenntnisurteil", { exact: true }).click()
  }

  await page.getByText("Suchen").click()
}

async function verifyDocUnitOpensInNewTab(
  newTab: Page,
  randomFileNumber: string,
) {
  await expect(newTab).toHaveURL(
    /\/caselaw\/documentunit\/[A-Z0-9]{13}\/categories$/,
  )
  const documentNumber = /caselaw\/documentunit\/(.*)\/categories/g.exec(
    newTab.url(),
  )?.[1] as string
  await expect(newTab.getByLabel("Gericht", { exact: true })).toHaveValue(
    "AG Aachen",
  )
  await expect(
    newTab.getByLabel("Entscheidungsdatum", { exact: true }),
  ).toHaveValue(formattedDate)
  await expect(newTab.getByTestId("chip-value")).toHaveText(randomFileNumber)
  await expect(newTab.getByLabel("Dokumenttyp", { exact: true })).toHaveValue(
    "Anerkenntnisurteil",
  )

  // Can be edited and saved after creation
  await newTab
    .getByLabel("Entscheidungsdatum", { exact: true })
    .fill("01.01.2021")
  await save(newTab)
  return documentNumber
}

async function expectHistoryLogRow(
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
