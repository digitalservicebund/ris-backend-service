import { expect, Page } from "@playwright/test"
import dayjs from "dayjs"
import {
  assignUserGroupToProcedure,
  clickCategoryButton,
  navigateToAttachments,
  navigateToCategories,
  navigateToHandover,
  navigateToInbox,
  navigateToManagementData,
  createPendingHandoverDecisionForBGH,
  save,
  uploadTestfile,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"
import { deleteAllProcedures } from "~/e2e/caselaw/utils/documentation-unit-api-util"
import { generateString } from "~/test-helper/dataGenerators"

/* eslint-disable playwright/expect-expect */
test.describe("Historie in Verwaltungsdaten", { tag: ["@RISDEV-7248"] }, () => {
  const testPrefix = `e2e_${generateString({ length: 10 })}`
  test("Es wird geloggt, wenn eine Dokeinheit angelegt wird", async ({
    page,
    documentNumber,
  }) => {
    await test.step("Die Historie hat ein 'Dokeinheit angelegt'-Event", async () => {
      await navigateToManagementData(page, documentNumber)
      await expectHistoryCount(page, 1)
      await expectHistoryLogRow(
        page,
        0,
        "DS (e2e_tests DigitalService)",
        `Dokeinheit angelegt`,
      )
    })
  })

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
      await save(page)
      await navigateToManagementData(page, documentNumber)
      await expectHistoryCount(page, 2)
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
      await save(page)
      await navigateToManagementData(page, documentNumber)
      await expectHistoryCount(page, 2)
      await expectHistoryLogRow(
        page,
        0,
        "DS (e2e_tests DigitalService)",
        `Dokeinheit bearbeitet`,
      )
    })
  })

  test("Es wird geloggt, wenn eine Dokeinheit an die jDV übergeben wird", async ({
    page,
    prefilledDocumentUnit,
  }) => {
    const documentNumber = prefilledDocumentUnit.documentNumber

    await test.step("Übergebe Dokument an die jDV", async () => {
      await navigateToHandover(page, documentNumber)
      await page
        .getByRole("button", { name: "Dokumentationseinheit an jDV übergeben" })
        .click()
    })

    await test.step("Nach einer Übergabe wird ein Historien-Log erstellt", async () => {
      await navigateToManagementData(page, documentNumber)
      await expectHistoryCount(page, 3)
      await expectHistoryLogRow(
        page,
        0,
        "DS (e2e_tests DigitalService)",
        `Dokeinheit an jDV übergeben`,
      )
    })
  })

  test("Es wird jede Vorgangsänderung geloggt", async ({
    page,
    pageWithExternalUser,
    documentNumber,
  }) => {
    let newProcedure: string

    await test.step("Setze den Vorgang", async () => {
      await navigateToCategories(page, documentNumber)
      newProcedure = testPrefix + generateString({ length: 10 })
      await page.getByLabel("Vorgang", { exact: true }).fill(newProcedure)
      await page.getByText(`${newProcedure} neu erstellen`).click()
      await save(page)
    })

    await test.step("In der Historie sind zwei Historien-Events", async () => {
      await navigateToManagementData(page, documentNumber)
      await expectHistoryCount(page, 3)
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

    await test.step("Dok-Einheit externer Person zuweisen", async () => {
      await assignUserGroupToProcedure(page, newProcedure)
    })

    await test.step("Externe Person ändert Entscheidungsname", async () => {
      await navigateToCategories(pageWithExternalUser, documentNumber)
      await clickCategoryButton("Entscheidungsname", pageWithExternalUser)
      await pageWithExternalUser
        .getByLabel("Entscheidungsname")
        .fill("ein Name")
      await save(pageWithExternalUser)
    })

    await test.step("Bearbeitungsevent der Externen Person ist zusätzlich sichtbar", async () => {
      await navigateToManagementData(page, documentNumber)
      await expectHistoryCount(page, 4)
      await expectHistoryLogRow(
        page,
        0,
        "DS (E2emila Extern)",
        `Dokeinheit bearbeitet`,
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
      await expectHistoryCount(page, 3)
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
      await expectHistoryCount(page, 4)
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
    const fileNumber = generateString()
    let documentNumber: string

    await test.step("Fremdanlage für BGH anlegen", async () => {
      documentNumber = await createPendingHandoverDecisionForBGH(
        page,
        edition,
        "12",
        "AG Aachen",
        dayjs("2025-01-01").format("DD.MM.YYYY"),
        fileNumber,
        "AnU",
      )
    })

    await test.step("Nach Aktenzeichen der Fremdanlage suchen", async () => {
      await navigateToInbox(pageWithBghUser)
      const fileNumberInput = pageWithBghUser.getByLabel("Aktenzeichen Suche")
      await fileNumberInput.fill(fileNumber)
      await pageWithBghUser
        .getByTestId("pending-handover-inbox")
        .getByLabel("Nach Dokumentationseinheiten suchen")
        .click()
      // 2 results + 1 header
      await expect(pageWithBghUser.getByRole("row")).toHaveCount(1 + 1)
    })

    await test.step("Fremdanlage als BGH-User annehmen", async () => {
      const rows = pageWithBghUser.locator("tr")
      const doc1Row = rows.filter({ hasText: documentNumber })

      const takeOverButton = doc1Row.getByRole("button", {
        name: "Dokumentationseinheit übernehmen",
      })
      await takeOverButton.click()
    })

    await test.step("BGH-User sieht nur BGH Namen in Verwaltungsdaten", async () => {
      await navigateToManagementData(pageWithBghUser, documentNumber)
      await expectHistoryCount(pageWithBghUser, 3)
      await expectHistoryLogRow(
        pageWithBghUser,
        1,
        "DS",
        `Fremdanalage angelegt für BGH`,
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

  test.afterAll(async ({ browser }) => {
    await deleteAllProcedures(browser, testPrefix)
  })
})

async function expectHistoryCount(page: Page, count: number) {
  await expect(
    page.getByTestId("document-unit-history-log").getByRole("row"),
    // header counts as row
  ).toHaveCount(count + 1)
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
