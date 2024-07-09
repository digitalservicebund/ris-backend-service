import { expect } from "@playwright/test"
import {
  fillInput,
  navigateToCategories,
  navigateToFiles,
  navigateToPreview,
  navigateToPublication,
  navigateToSearch,
  uploadTestfile,
} from "./e2e-utils"
import { caselawTest as test } from "./fixtures"

test.describe(
  "extra content side panel",
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

          await page.getByLabel("Seitenpanel öffnen").click()
          await expect(page).toHaveURL(/showAttachmentPanel=true/)
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
          await expect(page.getByLabel("Seitenpanel schließen")).toBeVisible()
        })

        await test.step("navigate to file upload, check that panel stays open", async () => {
          await navigateToFiles(page, documentNumber)
          await expect(page.getByLabel("Seitenpanel schließen")).toBeVisible()
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
          await expect(page.getByLabel("Seitenpanel schließen")).toBeHidden()
          await expect(page.getByLabel("Seitenpanel öffnen")).toBeHidden()
        })

        await test.step("navigate to preview, check that side panels and info panel are not displayed", async () => {
          await navigateToPreview(page, documentNumber)
          await expect(page.getByLabel("Seitenpanel schließen")).toBeHidden()
          await expect(page.getByLabel("Seitenpanel öffnen")).toBeHidden()
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
      "add, edit, delete note, and default opening and display logic",
      {
        annotation: [
          {
            type: "story",
            description:
              "https://digitalservicebund.atlassian.net/browse/RISDEV-4173",
          },
          {
            type: "story",
            description:
              "https://digitalservicebund.atlassian.net/browse/RISDEV-4174",
          },
        ],
      },
      async ({ page, documentNumber }) => {
        await test.step("prepare document with note", async () => {
          await navigateToCategories(page, documentNumber)
          await page.getByLabel("Seitenpanel öffnen").click()
          await fillInput(page, "Notiz Eingabefeld", "some text")
          await page.getByLabel("Speichern Button").click()
          await page.waitForEvent("requestfinished")
          await navigateToSearch(page, { navigationBy: "click" })
        })

        await test.step("open document with note and no attachment, check that note is displayed in open panel", async () => {
          await navigateToCategories(page, documentNumber)

          await expect(page.getByText("Notiz")).toBeVisible()
          await expect(page.getByLabel("Notiz Eingabefeld")).toHaveValue(
            "some text",
          )

          await page.getByLabel("Dokumente anzeigen").click()
          await expect(
            page.getByText(
              "Wenn Sie eine Datei hochladen, können Sie die Datei hier sehen.",
            ),
          ).toBeVisible()

          await page.getByLabel("Vorschau anzeigen").click()
          // Note is displayed in preview tab, label is above value
          await expect(
            page.locator(
              "div[data-testid='preview'] div:text('some text'):below(div:text('Notiz'))",
            ),
          ).toBeVisible()
        })

        await test.step("open document with note and attachment, check that note is displayed in open panel", async () => {
          await navigateToFiles(page, documentNumber)
          await uploadTestfile(page, "sample.docx")
          await expect(page.getByText("Die ist ein Test")).toBeVisible()

          await page.waitForEvent("requestfinished")
          await navigateToSearch(page, { navigationBy: "click" })

          await navigateToCategories(page, documentNumber)

          await expect(page.getByText("Notiz")).toBeVisible()

          await page.getByLabel("Dokumente anzeigen").click()
          await expect(page.getByText("Die ist ein Test")).toBeVisible()
        })

        await test.step("edit and save note with long text and scroll within note", async () => {
          await page.getByLabel("Notiz anzeigen").click()
          const longNoteText = `RISDEV-4230

Der III. Zivilsenat des Bundesgerichtshofs hat im schriftlichen Verfahren nach § 128 Abs. 2 ZPO, in dem Schriftsätze bis zum 7. Januar 2022 eingereicht werden konnten, durch den Vorsitzenden Richter Dr. Herrmann, den Richter Dr. Remmert, die Richterinnen Dr. Arend und Dr. Böttcher sowie den Richter Dr. Kessen

für Recht erkannt:

Auf die Revision des Klägers wird das Urteil des Oberlandesgerichts Nürnberg - 3. Zivilsenat und Kartellsenat - vom 29. Dezember 2020 teilweise aufgehoben und wie folgt neu gefasst:

Auf die Berufung des Klägers wird das Urteil des Landgerichts Nürnberg-Fürth - 11. Zivilkammer - vom 20. Mai 2020 unter Zurückweisung der weitergehenden Berufung teilweise abgeändert und insgesamt wie folgt neu gefasst:

Die Beklagte wird verurteilt,

den nachfolgend wiedergegebenen, am 16. Januar 2018 gelöschten Beitrag des Klägers wieder freizuschalten ("Post 1"):

den nachfolgend wiedergegebenen, am 22. Februar 2018 gelöschten Beitrag des Klägers wieder freizuschalten ("Post 2"):

den nachfolgend wiedergegebenen, am 8. Juni 2018 gelöschten Beitrag des Klägers wieder freizuschalten ("Post 4"):

 "Betreibe einfach kommando krav maga (Stufe 2!) dann können musels ruhig antraben"

den nachfolgend wiedergegebenen, am 26. Juni 2018 gelöschten Beitrag des Klägers wieder freizuschalten ("Post 5"):

es zu unterlassen, den Kläger für das Einstellen des unter Ziffer 1 genannten Bildes oder Textes auf www.f.

         .com erneut zu sperren oder den Beitrag zu löschen. Für den Fall der Zuwiderhandlung wird der Beklagten Ordnungsgeld von bis zu 250.000 €, ersatzweise Ordnungshaft, oder Ordnungshaft bis zu sechs Monaten angedroht, wobei die Ordnungshaft an ihren Vorstandsmitgliedern zu vollziehen ist.`
          await fillInput(page, "Notiz Eingabefeld", longNoteText)
          await page.getByLabel("Speichern Button").click()
          await page.waitForEvent("requestfinished")
          await expect(page.getByLabel("Notiz Eingabefeld")).toHaveValue(
            longNoteText,
          )

          //TODO: re-enable after fixing the test

          // const scrollTopBeforeScrolling = await page
          //   .locator("#notesInput")
          //   .evaluate((el) => el.scrollTop)
          // await page.locator("#notesInput").click()
          // await page.keyboard.press("Home")
          //
          // await page.waitForFunction(
          //   () => window.document.getElementById("notesInput")?.scrollTop === 0,
          //   undefined,
          //   { timeout: 1_000 },
          // )
          //
          // const scrollTopAfterScrolling = await page
          //   .locator("#notesInput")
          //   .evaluate((el) => el.scrollTop)
          // expect(scrollTopBeforeScrolling).toBeGreaterThan(0)
          // expect(scrollTopAfterScrolling).toBe(0)

          await page.reload()
          await expect(page.getByLabel("Notiz Eingabefeld")).toHaveValue(
            longNoteText,
          )
        })

        await test.step("open document with attachment and no note, check that attachment is displayed in open panel", async () => {
          await navigateToCategories(page, documentNumber)
          await page.getByLabel("Notiz anzeigen").click()
          await fillInput(page, "Notiz Eingabefeld", "")
          await page.getByLabel("Speichern Button").click()
          await page.waitForEvent("requestfinished")
          await navigateToFiles(page, documentNumber)
          await uploadTestfile(page, "sample.docx")
          await expect(page.getByText("Die ist ein Test")).toBeVisible()

          await page.waitForEvent("requestfinished")
          await navigateToSearch(page, { navigationBy: "click" })

          await navigateToCategories(page, documentNumber)

          await expect(page.getByText("Die ist ein Test")).toBeVisible()
          await page.getByLabel("Notiz anzeigen").click()
          await expect(page.getByLabel("Notiz Eingabefeld")).toHaveValue("")
        })
      },
    )

    test(
      "export note",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4175",
        },
      },
      // DS does not export note field, that's why we need BGH user here
      async ({ pageWithBghUser, prefilledDocumentUnitBgh }) => {
        const documentNumber = prefilledDocumentUnitBgh.documentNumber!
        await test.step("Confirm note is exported in XML on publish page", async () => {
          await navigateToPublication(pageWithBghUser, documentNumber)
          await expect(
            pageWithBghUser.getByText("XML Vorschau der Veröffentlichung"),
          ).toBeVisible()

          await pageWithBghUser
            .getByText("XML Vorschau der Veröffentlichung")
            .click()

          await expect(
            pageWithBghUser.locator("span", {
              hasText: "<notiz>example note</notiz>",
            }),
          ).toBeVisible()
        })

        await test.step("Delete note from doc unit", async () => {
          await navigateToCategories(pageWithBghUser, documentNumber)
          await fillInput(pageWithBghUser, "Notiz Eingabefeld", "")
          await pageWithBghUser.getByLabel("Speichern Button").click()
          await pageWithBghUser.waitForEvent("requestfinished")
        })

        await test.step("Confirm note is not exported in XML on publish page", async () => {
          await navigateToPublication(pageWithBghUser, documentNumber)
          await expect(
            pageWithBghUser.getByText("XML Vorschau der Veröffentlichung"),
          ).toBeVisible()

          await pageWithBghUser
            .getByText("XML Vorschau der Veröffentlichung")
            .click()

          await expect(
            pageWithBghUser.locator("span", { hasText: "<notiz>" }),
          ).toBeHidden()
        })
      },
    )

    test(
      "keyboard accessibility",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4254",
        },
      },
      async ({ page, documentNumber }) => {
        await test.step("prepare doc unit with attachments", async () => {
          await navigateToFiles(page, documentNumber)
          await uploadTestfile(page, "sample.docx")
          await expect(page.getByText("Die ist ein Test")).toBeVisible()
          await uploadTestfile(page, "some-formatting.docx")
          await expect(page.getByText("Subheadline")).toBeVisible()
        })

        await test.step("test opening and closing panel with keyboard", async () => {
          await page.getByTestId("Rubriken").click()
          await expect(
            page.getByRole("heading", { name: "Stammdaten" }),
          ).toBeVisible()
          await page
            .getByRole("button", { name: "Seitenpanel schließen" })
            .click()
          await expect(
            page.getByRole("button", { name: "Seitenpanel öffnen" }),
          ).toBeFocused()
          await page.keyboard.press("Enter")
          await expect(
            page.getByRole("button", { name: "Seitenpanel schließen" }),
          ).toBeFocused()
        })

        await test.step("test content selection with keyboard", async () => {
          await page.keyboard.press("Tab")
          await expect(
            page.getByRole("button", { name: "Notiz anzeigen" }),
          ).toBeFocused()
          await page.keyboard.press("Enter")
          await expect(page.getByText("Notiz")).toBeVisible()
          await page.keyboard.press("Tab")
          await expect(
            page.getByRole("button", { name: "Dokumente anzeigen" }),
          ).toBeFocused()
          await page.keyboard.press("Enter")
          await expect(page.getByText("some-formatting.docx")).toBeVisible()
        })

        await test.step("test document selection with keyboard", async () => {
          // skip preview button
          await page.keyboard.press("Tab")
          await page.keyboard.press("Tab")
          await expect(
            page.getByRole("button", { name: "Vorheriges Dokument anzeigen" }),
          ).toBeFocused()
          await page.keyboard.press("Tab")
          await expect(
            page.getByRole("button", { name: "Nächstes Dokument anzeigen" }),
          ).toBeFocused()
          await expect(page.getByText("some-formatting.docx")).toBeVisible()
          await page.keyboard.press("Enter")
          await expect(page.getByText("sample.docx")).toBeVisible()
          await page.keyboard.press("Enter")
          await expect(page.getByText("some-formatting.docx")).toBeVisible()
          await page.keyboard.press("Shift+Tab")
          await expect(
            page.getByRole("button", { name: "Vorheriges Dokument anzeigen" }),
          ).toBeFocused()
          await page.keyboard.press("Enter")
          await expect(page.getByText("sample.docx")).toBeVisible()
          await page.keyboard.press("Enter")
          await expect(page.getByText("some-formatting.docx")).toBeVisible()
        })

        await test.step("select preview with keyboard", async () => {
          await page.keyboard.press("Shift+Tab")
          await expect(
            page.getByRole("button", { name: "Vorschau anzeigen" }),
          ).toBeFocused()
          await page.keyboard.press("Enter")
          await expect(
            page.locator("p", { hasText: "Vorschau erstellt am" }),
          ).toBeVisible()
        })

        await test.step("open preview in new tab with keyboard", async () => {
          await page.keyboard.press("Tab")
          await expect(
            page.getByRole("link", { name: "Vorschau in neuem Tab öffnen" }),
          ).toBeFocused()

          const newTabPromise = page.context().waitForEvent("page")
          await page.keyboard.press("Enter")
          const newTab = await newTabPromise
          expect(newTab.url()).toContain("/preview")
          await newTab.close()
        })
      },
    )
  },
)
