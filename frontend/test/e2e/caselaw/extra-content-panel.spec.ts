import { expect } from "@playwright/test"
import {
  navigateToCategories,
  navigateToFiles,
  navigateToPreview,
  navigateToPublication,
  navigateToSearch,
  uploadTestfile,
} from "./e2e-utils"
import { caselawTest as test } from "./fixtures"

test.describe(
  "test extra content side panel",
  {
    annotation: {
      type: "epic",
      description: "https://digitalservicebund.atlassian.net/browse/RISDEV-86",
    },
  },
  () => {
    test(
      "display note and attachments in side panel",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4173",
        },
      },
      async ({ page, documentNumber }) => {
        await test.step("open panel, check empty states", async () => {
          await navigateToCategories(page, documentNumber)

          await page.getByLabel("Dokumentansicht öffnen").click()
          await expect(page.getByText("Notiz")).toBeVisible()
          await expect(page.getByLabel("Notiz anzeigen")).toBeVisible()
          await page.getByLabel("Dokumente anzeigen").click()
          await expect(
            page.getByText(
              "Wenn Sie eine Datei hochladen, können Sie die Datei hier sehen.",
            ),
          ).toBeVisible()
        })

        await test.step("reload page, check that panel stays open", async () => {
          await page.reload()
          await expect(
            page.getByLabel("Dokumentansicht schließen"),
          ).toBeVisible()
        })

        await test.step("navigate to file upload, check that panel stays open", async () => {
          await navigateToFiles(page, documentNumber)
          await expect(
            page.getByLabel("Dokumentansicht schließen"),
          ).toBeVisible()
        })

        await test.step("upload file, check that is it displayed in the panel", async () => {
          await uploadTestfile(page, "sample.docx")
          await expect(page.locator("#attachment-view")).toBeVisible()
        })

        await test.step("navigate to categories, check that panel is open and displays attachment", async () => {
          await navigateToCategories(page, documentNumber)
          await expect(page.locator("#attachment-view")).toBeVisible()
        })

        await test.step("navigate to publication, check that panel is not displayed", async () => {
          await navigateToPublication(page, documentNumber)
          await expect(
            page.getByLabel("Dokumentansicht schließen"),
          ).toBeHidden()
          await expect(page.getByLabel("Dokumentansicht öffnen")).toBeHidden()
        })

        await test.step("navigate to preview, check that side panels and info panel are not displayed", async () => {
          await navigateToPreview(page, documentNumber)
          await expect(
            page.getByLabel("Dokumentansicht schließen"),
          ).toBeHidden()
          await expect(page.getByLabel("Dokumentansicht öffnen")).toBeHidden()
          await expect(
            page.getByTestId("document-unit-info-panel"),
          ).toBeHidden()
          await expect(page.getByTestId("side-toggle-navigation")).toBeHidden()
        })

        await test.step("navigate to categories, check that panel is open and displays attachment", async () => {
          await navigateToCategories(page, documentNumber)
          await expect(page.locator("#attachment-view")).toBeVisible()
        })
      },
    )
    test(
      "panel auto-opening and display logic",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4173",
        },
      },
      async ({ page }) => {
        const documentNumberWithNote = "YYTestDoc0015"
        const documentNumberWithoutNote = "YYTestDoc0014"
        await test.step("open document with note and no attachment, check that note is displayed in open panel", async () => {
          await navigateToCategories(page, documentNumberWithNote)

          await expect(page.getByText("Notiz")).toBeVisible()

          await page.getByLabel("Dokumente anzeigen").click()
          await expect(
            page.getByText(
              "Wenn Sie eine Datei hochladen, können Sie die Datei hier sehen.",
            ),
          ).toBeVisible()
        })

        await test.step("open document with note and attachment, check that note is displayed in open panel", async () => {
          await navigateToFiles(page, documentNumberWithNote)
          await uploadTestfile(page, "sample.docx")

          await navigateToSearch(page)

          await navigateToCategories(page, documentNumberWithNote)

          await expect(page.getByText("Notiz")).toBeVisible()

          await page.getByLabel("Dokumente anzeigen").click()
          await expect(page.getByText("Die ist ein Test")).toBeVisible()
        })

        await test.step("open document with attachment and no note, check that attachment is displayed in open panel", async () => {
          await navigateToFiles(page, documentNumberWithoutNote)
          await uploadTestfile(page, "sample.docx")

          await navigateToSearch(page)

          await navigateToCategories(page, documentNumberWithoutNote)

          await expect(page.getByText("Die ist ein Test")).toBeVisible()
        })
      },
    )
  },
)
