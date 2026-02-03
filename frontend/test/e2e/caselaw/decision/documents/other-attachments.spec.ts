import fs from "fs/promises"
import { Download, expect } from "@playwright/test"
import errorMessages from "@/i18n/errors.json" with { type: "json" }
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  expectHistoryCount,
  expectHistoryLogRow,
  navigateToCategories,
  navigateToManagementData,
  uploadTestfile,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe("Weitere Anhänge", { tag: ["@RISDEV-8920"] }, () => {
  test.use({
    testFilesOptions: [
      [
        { fileName: "too_large.bin", sizeInMB: 101 },
        { fileName: "max_size.mp4", sizeInMB: 99.9 },
        { fileName: "no_extension", sizeInMB: 10 },
      ],
      { scope: "test" },
    ],
  })

  test(
    "Im Seitenpanel kann eine Datei hochgeladen, heruntergeladen und gelöscht werden.",
    { tag: ["@RISDEV-9850"] },
    async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)

      await test.step("Öffne Weitere Anhänge im Seitenpanel mit Taste (a)", async () => {
        await page.getByRole("button", { name: "Seitenpanel öffnen" }).click()
        await page.keyboard.press("a")
      })

      // We store the icon displayed with empty files because it should change after uploading files.
      const sidePanelTabButtonIcon = await page
        .getByRole("button", { name: "Anhänge anzeigen" })
        .locator("svg")
        .innerHTML()

      await uploadTestfile(page, "sample.docx")

      await test.step("Datei ist in Liste der Anhänge enthalten", async () => {
        const attachmentTable = page.getByRole("cell", {
          name: "Dateiname",
          exact: true,
        })
        await expect(attachmentTable).toBeVisible()

        const fileName = page
          .getByTestId("attachment-list")
          .getByText("sample.docx")
        await expect(fileName).toBeVisible()
      })

      await test.step("Neues Anhänge-Icon für Seitenpanel, wenn Dateien vorliegen", async () => {
        const newIcon = await page
          .getByRole("button", { name: "Anhänge anzeigen" })
          .locator("svg")
          .innerHTML()
        expect(newIcon).not.toEqual(sidePanelTabButtonIcon)
      })

      await test.step("Downloade die Datei und prüfe Inhalt ist identisch", async () => {
        const downloadButton = page.getByRole("button", {
          name: "sample.docx herunterladen",
        })
        const downloadPromise = page.waitForEvent("download")

        await downloadButton.click()

        // While it is downloading, the download button is disabled and shows a loading spinner
        await expect(downloadButton).toBeDisabled()
        // Wait for the loading state to be finished
        await expect(downloadButton).toBeEnabled()

        const downloadEvent = await downloadPromise
        expect(downloadEvent.suggestedFilename()).toBe("sample.docx")
        await compareWithOriginalFile(downloadEvent)
      })

      await test.step("Lösche die Datei nach Dialog-Bestätigung und prüfe Entfernung", async () => {
        await page.getByLabel("Datei löschen").click()
        // Confirm the dialog
        const dialog = page.getByRole("dialog")
        await dialog.getByLabel("Anhang löschen", { exact: true }).click()
        await expect(page.getByText("Anhang löschen")).toBeHidden()
        await expect(page.getByText("sample.docx")).toBeHidden()
      })

      await test.step("Auch nach Neu-Laden sind keine Dateien vorhanden", async () => {
        await page.reload()
        await page.getByRole("button", { name: "Anhänge anzeigen" }).click()

        await expect(
          page.getByText("Ziehen Sie Ihre Dateien in diesen Bereich."),
        ).toBeVisible()

        const tableView = page.getByRole("cell", {
          name: "Dateiname",
          exact: true,
        })
        await expect(tableView).toBeHidden()

        // With empty files, the initial icon should be shown again.
        const newIcon = await page
          .getByRole("button", { name: "Anhänge anzeigen" })
          .locator("svg")
          .innerHTML()
        expect(newIcon).toEqual(sidePanelTabButtonIcon)
      })

      await test.step("In der Historie gibt es ein Hochladen und ein Löschen Historien-Event", async () => {
        await navigateToManagementData(page, documentNumber, {
          navigationBy: "click",
        })
        await expectHistoryCount(page, 5)
        await expectHistoryLogRow(
          page,
          0,
          "DS (e2e_tests DigitalService)",
          'Anhang "sample.docx" gelöscht',
        )
        await expectHistoryLogRow(
          page,
          1,
          "DS (e2e_tests DigitalService)",
          'Anhang "sample.docx" hinzugefügt',
        )
      })
    },
  )

  test(
    "Eine Datei über 100 MB wird abgelehnt",
    { tag: ["@RISDEV-9850"] },
    async ({ page, documentNumber, testFiles }) => {
      await navigateToCategories(page, documentNumber)

      await test.step("Öffne Weitere Anhänge im Seitenpanel", async () => {
        await page.getByRole("button", { name: "Seitenpanel öffnen" }).click()
        await page.getByRole("button", { name: "Anhänge anzeigen" }).click()
      })

      const tooLargeFile = testFiles[0]

      await uploadTestfile(page, tooLargeFile.path, {
        skipAssert: true,
        basePath: "",
      })

      await test.step("Ein Fehler wird angezeigt", async () => {
        await expect(page.getByRole("alert")).toHaveText(
          "Es ist ein Fehler aufgetreten'too_large.bin' " +
            errorMessages.OTHER_FILE_TOO_LARGE_CASELAW.title +
            " " +
            errorMessages.OTHER_FILE_TOO_LARGE_CASELAW.description,
        )
      })

      await test.step("Es wird keine Anhangsliste angezeigt, da nichts hochgeladen wurde", async () => {
        const attachmentTable = page.getByRole("cell", {
          name: "Dateiname",
          exact: true,
        })
        await expect(attachmentTable).toBeHidden()
      })
    },
  )

  test(
    "Zwei Dateien mit 100 MB und 10 MB können hochgeladen werden",
    { tag: ["@RISDEV-9850"] },
    async ({ page, documentNumber, testFiles }) => {
      await navigateToCategories(page, documentNumber)

      await test.step("Öffne Weitere Anhänge im Seitenpanel", async () => {
        await page.getByRole("button", { name: "Seitenpanel öffnen" }).click()
        await page.getByRole("button", { name: "Anhänge anzeigen" }).click()
      })

      const largeFile = testFiles[1]
      const fileWithoutExtension = testFiles[2]

      await uploadTestfile(page, [largeFile.path, fileWithoutExtension.path], {
        skipAssert: true,
        basePath: "",
      })

      await test.step("Es wird kein Fehler angezeigt", async () => {
        await expect(page.getByRole("alert")).toBeHidden()
      })

      await test.step("Hochgeladene Dateien sind in Anhangliste", async () => {
        const attachmentTable = page.getByRole("cell", {
          name: "Dateiname",
          exact: true,
        })
        await expect(attachmentTable).toBeVisible()

        const fileName1 = page
          .getByTestId("attachment-list")
          .getByText("max_size.mp4")
        await expect(fileName1).toBeVisible()

        const fileName2 = page
          .getByTestId("attachment-list")
          .getByText("no_extension")
        await expect(fileName2).toBeVisible()
      })
    },
  )
})

async function compareWithOriginalFile(download: Download) {
  const chunks = []
  for await (const chunk of await download.createReadStream()) {
    chunks.push(chunk)
  }
  const downloadedBuffer = Buffer.concat(chunks)

  const originalBuffer = await fs.readFile(
    "./test/e2e/caselaw/testfiles/sample.docx",
  )

  expect(downloadedBuffer).toEqual(originalBuffer)
}
