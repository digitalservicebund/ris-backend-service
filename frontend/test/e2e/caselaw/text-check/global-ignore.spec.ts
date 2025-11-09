import { expect, Page } from "@playwright/test"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  clearTextField,
  navigateToCategories,
} from "~/e2e/caselaw/utils/e2e-utils"
import { generateString } from "~/test-helper/dataGenerators"

async function clickTextCheckButton(page: Page) {
  await page
    .getByLabel("Orientierungssatz Button")
    .getByRole("button", { name: "Rechtschreibprüfung" })
    .click()
}

test.describe(
  "global ignore",
  {
    tag: ["@RISDEV-254"],
  },
  () => {
    test(
      "global dictionary word should be ignored in all document units",
      {
        tag: ["@RISDEV-7464"],
      },
      async ({
        page,
        prefilledDocumentUnit,
        pageWithBghUser,
        prefilledDocumentUnitBgh,
      }) => {
        // Note: the responses for the text-check by category are mocked here, because they are taking too long
        // 1. Response for the initial check with the globally ignored word from jdv
        const jdvIgnoredWordResponse = {
          htmlText:
            '<p><text-check id="1" type="misspelling" ignored="true">VDberbglStPr</text-check></p>',
          matches: [
            {
              id: 1,
              word: "VDberbglStPr",
              category: "headnote",
              ignoredTextCheckWords: [
                { word: "VDberbglStPr", type: "global_jdv" },
              ],
            },
          ],
        }

        const wordWithTypo = generateString({ prefix: "etoe" }) // e.g. etoedsfjg

        // 2. Response for a word with an error (not ignored anywhere)
        const wordWithErrorResponse = {
          htmlText: `<p>Text mit Fehler: <text-check id="1" type="misspelling" ignored="false">${wordWithTypo}</text-check></p>`,
          matches: [
            {
              id: 1,
              word: wordWithTypo,
              shortMessage: "Rechtschreibfehler",
              category: "headnote",
            },
          ],
        }

        // 3. Response for globally ignored word
        const globallyIgnoredWordResponse = {
          htmlText: `<p><text-check id="1" type="misspelling" ignored="true">${wordWithTypo}</text-check></p>`,
          matches: [
            {
              id: 1,
              word: wordWithTypo,
              category: "headnote",
              ignoredTextCheckWords: [{ word: wordWithTypo, type: "global" }],
            },
          ],
        }

        await test.step("open doc unit A in edit mode", async () => {
          await navigateToCategories(
            page,
            prefilledDocumentUnit.documentNumber,
            { category: DocumentUnitCategoriesEnum.TEXTS },
          )
        })

        const headNoteEditor = page.getByTestId("Orientierungssatz")
        const headNoteEditorTextArea = headNoteEditor.locator("div")

        await test.step("add jDV globally ignored word in headnote (Orientierungssatz)", async () => {
          await headNoteEditorTextArea.fill("")
          await headNoteEditorTextArea.fill("VDberbglStPr") // this is ignored by jDV
          await expect(headNoteEditorTextArea).toHaveText("VDberbglStPr")
        })

        await test.step("check text of headnote (Orientierungssatz)", async () => {
          const lock = Promise.withResolvers<void>()
          await page.route(
            `**/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}/text-check?category=headnote`,
            async (route) => {
              await lock.promise
              await route.fulfill({
                status: 200,
                contentType: "application/json",
                body: JSON.stringify(jdvIgnoredWordResponse),
              })
            },
          )

          await clickTextCheckButton(page)
          lock.resolve()
          await headNoteEditor
            .locator("text-check")
            .first()
            .waitFor({ state: "visible" })
        })

        await test.step("jDV globally ignored word can't be removed", async () => {
          await headNoteEditor.locator("text-check").first().click()
          await expect(page.getByTestId("text-check-modal")).toBeVisible()

          await expect(page.getByText("Aus Wörterbuch entfernen")).toBeHidden()
          await expect(
            page.getByText("Zu globalem Wörterbuch hinzufügen"),
          ).toBeHidden()
          await expect(
            page.getByText("Von jDV ignoriert"),
            "You might need to migrate the global dictionary",
          ).toBeVisible()
        })

        await test.step("add text in headnote (Orientierungssatz)", async () => {
          await headNoteEditorTextArea.fill("")
          await headNoteEditorTextArea.fill("Text mit Fehler: " + wordWithTypo)
        })

        await test.step("add word to global ignore", async () => {
          const lock = Promise.withResolvers<void>()
          await page.route(
            `**/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}/text-check?category=headnote`,
            async (route) => {
              await lock.promise
              await route.fulfill({
                status: 200,
                contentType: "application/json",
                body: JSON.stringify(wordWithErrorResponse),
              })
            },
          )

          await clickTextCheckButton(page)
          lock.resolve()

          await headNoteEditor
            .locator("text-check")
            .first()
            .waitFor({ state: "visible" })

          await headNoteEditor.locator("text-check").first().click()
          await expect(page.getByTestId("text-check-modal")).toBeVisible()
          await expect(
            page.getByText("Zum Wörterbuch hinzufügen"),
          ).toBeVisible()

          await page.getByText("Zum Wörterbuch hinzufügen").click()
        })

        await test.step("open doc unit B in edit mode", async () => {
          await navigateToCategories(
            pageWithBghUser,
            prefilledDocumentUnitBgh.documentNumber,
            { category: DocumentUnitCategoriesEnum.TEXTS },
          )
        })

        await expect(pageWithBghUser.getByTestId("headnote")).toBeVisible()

        const secondHeadNoteEditor =
          pageWithBghUser.getByTestId("Orientierungssatz")

        const secondHeadNoteEditorFieldArea =
          secondHeadNoteEditor.locator("div")
        await test.step("type same text in headnote (Orientierungssatz)", async () => {
          await clearTextField(pageWithBghUser, secondHeadNoteEditorFieldArea) // Use pageWithBghUser for clearTextField
          await secondHeadNoteEditor.locator("div").fill(wordWithTypo)
        })

        await test.step("text check should show that word is globally ignored", async () => {
          const lock = Promise.withResolvers<void>()
          await page.route(
            `**/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}/text-check?category=headnote`,
            async (route) => {
              await lock.promise
              await route.fulfill({
                status: 200,
                contentType: "application/json",
                body: JSON.stringify(globallyIgnoredWordResponse),
              })
            },
          )

          await clickTextCheckButton(pageWithBghUser)
          lock.resolve()
          await secondHeadNoteEditor
            .locator("text-check")
            .first()
            .waitFor({ state: "visible" })

          await secondHeadNoteEditor.locator("text-check").first().click()
          await expect(
            pageWithBghUser.getByText("Aus Wörterbuch entfernen"),
          ).toBeVisible()
        })

        await test.step("remove word from global ignore", async () => {
          await pageWithBghUser.getByText("Aus Wörterbuch entfernen").click()
        })

        await test.step("check text again in doc unit A and expect word not to be ignored globally", async () => {
          await navigateToCategories(
            page,
            prefilledDocumentUnit.documentNumber,
            { category: DocumentUnitCategoriesEnum.TEXTS },
          )
          await expect(page.getByTestId("headnote")).toBeVisible()
          await headNoteEditorTextArea.click()

          const lock = Promise.withResolvers<void>()
          await page.route(
            `**/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}/text-check?category=headnote`,
            async (route) => {
              await lock.promise
              await route.fulfill({
                status: 200,
                contentType: "application/json",
                body: JSON.stringify(wordWithErrorResponse),
              })
            },
          )

          await clickTextCheckButton(page)
          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toHaveText("Rechtschreibprüfung läuft")
          lock.resolve()

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toBeHidden({ timeout: 10_000 })

          await headNoteEditor.locator("text-check").first().click()
          await expect(page.getByTestId("text-check-modal")).toBeVisible()
          await expect(
            page.getByText("Zum Wörterbuch hinzufügen"),
          ).toBeVisible()
        })
      },
    )
  },
)
