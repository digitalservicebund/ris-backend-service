import fs from "fs"
import { Download, expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  createDataTransfer,
  navigateToAttachments,
  uploadTestfile,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe("upload an original document to a doc unit", () => {
  test.beforeEach(async ({ page, documentNumber }) => {
    await navigateToAttachments(page, documentNumber)
  })

  test(
    "upload, download and delete docx file per file chooser",
    { tag: ["@RISDEV-9850"] },
    async ({ page }) => {
      await uploadTestfile(page, "sample.docx")

      const tableView = page.getByRole("cell", {
        name: "Dateiname",
        exact: true,
      })

      const attachmentView = page.locator("#attachment-view")

      await expect(attachmentView).toBeVisible()
      await expect(tableView).toBeVisible()

      await test.step("Downloade die Datei", async () => {
        const downloadButton = page.getByRole("button", {
          name: "sample.docx herunterladen",
        })
        const downloadPromise = page.waitForEvent("download")

        await downloadButton.click()

        // While it is downloading, the download button is disabled and shows a loading spinner
        await expect(downloadButton).toBeDisabled()
        // Wait for loading state to be finished
        await expect(downloadButton).toBeEnabled()

        const downloadEvent = await downloadPromise
        expect(downloadEvent.suggestedFilename()).toBe("sample.docx")
        await compareWithOriginalFile(downloadEvent)
      })

      // delete file
      await page.getByLabel("Datei löschen").click()
      const dialog = page.getByRole("dialog")
      await dialog.getByLabel("Anhang löschen", { exact: true }).click() // confirm
      await expect(page.getByText("Anhang löschen")).toBeHidden()
      await expect(page.getByText("sample.docx")).toBeHidden()

      await page.reload()
      await expect(
        page.getByText("Ziehen Sie Ihre Dateien in diesen Bereich."),
      ).toBeVisible()
      await expect(tableView).toBeHidden()
      await expect(attachmentView).toBeHidden()
    },
  )

  test("upload and delete multiple docx files per file chooser", async ({
    page,
  }) => {
    await test.step("upload files", async () => {
      await uploadTestfile(page, ["sample.docx", "some-formatting.docx"])

      await expect(
        page.getByRole("cell", { name: "sample.docx", exact: true }),
      ).toBeVisible()
      await expect(
        page.getByRole("cell", { name: "some-formatting.docx", exact: true }),
      ).toBeVisible()

      await expect(
        page.getByRole("cell", {
          name: "Dateiname",
          exact: true,
        }),
      ).toBeVisible()
    })

    await test.step("switch files in preview", async () => {
      const previewIdentifier = new RegExp(
        /Die ist ein Test|bold, italic, underlined/,
      )
      await expect(page.getByText(previewIdentifier)).toBeVisible()

      const initialPreviewContent = await page
        .getByText(previewIdentifier)
        .innerText()

      await page.getByLabel("Vorheriges Dokument anzeigen").click()
      await expect(page.getByText(previewIdentifier)).toBeVisible()
      await expect(page.getByText(previewIdentifier)).not.toContainText(
        initialPreviewContent,
      )
    })

    await test.step("delete files", async () => {
      await expect(page.getByLabel("Datei löschen")).toHaveCount(2)

      const deleteFirstDocument = page
        .getByTestId("list-entry-0")
        .getByLabel("Datei löschen")
      await deleteFirstDocument.click()
      const dialog = page.getByRole("dialog")
      await dialog.getByLabel("Anhang löschen", { exact: true }).click() // confirm
      await expect(page.getByLabel("Datei löschen")).toHaveCount(1)

      const deleteSecondDocument = page
        .getByTestId("list-entry-0")
        .getByLabel("Datei löschen")
      await expect(deleteSecondDocument).toBeVisible()
      await deleteSecondDocument.click()
      await dialog.getByLabel("Anhang löschen", { exact: true }).click() // confirm
      await expect(page.getByLabel("Datei löschen")).toBeHidden()

      await page.reload()
      await expect(
        page.getByText("Ziehen Sie Ihre Dateien in diesen Bereich."),
      ).toBeVisible()
      await expect(
        page.getByRole("cell", {
          name: "Dateiname",
          exact: true,
        }),
      ).toBeHidden()
    })
  })

  test("upload non-docx file per file chooser", async ({ page }) => {
    await uploadTestfile(page, "sample.png", { skipAssert: true })
    await expect(
      page.getByText(
        "'sample.png' hat ein falsches Format. Laden Sie eine .docx-Version hoch.",
        { exact: true },
      ),
    ).toBeVisible()
  })

  test("drag over docx file in upload area", async ({ page }) => {
    const docx = await fs.promises.readFile(
      "./test/e2e/caselaw/testfiles/sample.docx",
    )
    const dataTransfer = await createDataTransfer(
      page,
      docx,
      "sample.docx",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    )

    await page.dispatchEvent(".upload-drop-area", "dragover", { dataTransfer })
    await expect(
      page.getByText("Ziehen Sie Ihre Dateien in diesen Bereich."),
    ).toBeVisible()
  })

  test("drop docx file in upload area", async ({ page }) => {
    const docx = await fs.promises.readFile(
      "./test/e2e/caselaw/testfiles/sample.docx",
    )
    const dataTransfer = await createDataTransfer(
      page,
      docx,
      "sample.docx",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    )

    await page.dispatchEvent(".upload-drop-area", "drop", { dataTransfer })
    await expect(page.getByText("sample.docx")).toBeVisible()
  })

  test("drop non-docx file in upload area", async ({ page }) => {
    const png = await fs.promises.readFile(
      "./test/e2e/caselaw/testfiles/sample.png",
    )
    const dataTransfer = await createDataTransfer(
      page,
      png,
      "sample.png",
      "image/png",
    )

    await page.dispatchEvent("#upload-drop-area", "drop", { dataTransfer })
    await expect(
      page.getByText(
        "'sample.png' hat ein falsches Format. Laden Sie eine .docx-Version hoch.",
        { exact: true },
      ),
    ).toBeVisible()
  })
})

async function compareWithOriginalFile(download: Download) {
  const chunks = []
  for await (const chunk of await download.createReadStream()) {
    chunks.push(chunk)
  }
  const downloadedBuffer = Buffer.concat(chunks)

  const originalBuffer = fs.readFileSync(
    "./test/e2e/caselaw/testfiles/sample.docx",
  )

  expect(downloadedBuffer).toEqual(originalBuffer)
}
