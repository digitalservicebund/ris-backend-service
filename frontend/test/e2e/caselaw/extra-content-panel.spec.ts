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
      "auto-opening and display logic",
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
          await navigateToSearch(page)
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
          // Note is displayed in preview tab
          await expect(
            page.locator("div[data-testid='preview']", {
              hasText: "some text",
            }),
          ).toBeVisible()
        })

        await test.step("open document with note and attachment, check that note is displayed in open panel", async () => {
          await navigateToFiles(page, documentNumber)
          await uploadTestfile(page, "sample.docx")
          await expect(page.getByText("Die ist ein Test")).toBeVisible()

          await navigateToSearch(page)

          await navigateToCategories(page, documentNumber)

          await expect(page.getByText("Notiz")).toBeVisible()

          await page.getByLabel("Dokumente anzeigen").click()
          await expect(page.getByText("Die ist ein Test")).toBeVisible()
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

          await navigateToSearch(page)

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

    test("keyboard accessibility", async ({ page, documentNumber }) => {
      await test.step("prepare doc unit with attachments", async () => {
        await navigateToFiles(page, documentNumber)
        await uploadTestfile(page, ["sample.docx", "some-formatting.docx"])
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
    })
  },
)
