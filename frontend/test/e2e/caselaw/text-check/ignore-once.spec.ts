import { expect } from "@playwright/test"
import { getMarkId, textCheckUnderlinesColors } from "./util"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  clearTextField,
  navigateToCategories,
} from "~/e2e/caselaw/utils/e2e-utils"
import { convertHexToRGB } from "~/test-helper/coloursUtil"

test.describe(
  "global ignore",
  {
    tag: ["@RISDEV-9171"],
  },
  () => {
    test(
      "ignore word once",
      {
        tag: ["@RISDEV-9171"],
      },
      async ({ page, prefilledDocumentUnit }) => {
        const headnoteResponseInitial = {
          htmlText:
            '<p>Das ist ein <text-check id="1" type="misspelling" ignored="false">felher</text-check>. Das ist ein zweiter <text-check id="2" type="misspelling" ignored="false">felher</text-check>.</p>',
          matches: [
            {
              id: 1,
              word: "felher",
              message: "Möglicher Tippfehler gefunden.",
              shortMessage: "Rechtschreibfehler",
              category: "headnote",
              isIgnoredOnce: false,
            },
            {
              id: 2,
              word: "felher",
              shortMessage: "Rechtschreibfehler",
              message: "Möglicher Tippfehler gefunden.",
              category: "headnote",
              isIgnoredOnce: false,
            },
          ],
        }

        const guidingPrincipleResponseInitial = {
          htmlText:
            '<p>Das ist ein <text-check id="1" type="misspelling" ignored="false">felher</text-check>. Das ist ein zweiter <text-check id="2" type="misspelling" ignored="false">felher</text-check>.</p>',
          matches: [
            {
              id: 1,
              word: "felher",
              shortMessage: "Rechtschreibfehler",
              message: "Möglicher Tippfehler gefunden.",
              category: "guidingPrinciple",
              isIgnoredOnce: false,
            },
            {
              id: 2,
              word: "felher",
              shortMessage: "Rechtschreibfehler",
              message: "Möglicher Tippfehler gefunden.",
              category: "guidingPrinciple",
              isIgnoredOnce: false,
            },
          ],
        }

        const headnoteResponseAfterIgnore = {
          htmlText:
            '<p>Das ist ein <ignore-once><text-check id="1" type="misspelling" ignored="true">felher</text-check></ignore-once>. Das ist ein zweiter <text-check id="2" type="misspelling" ignored="false">felher</text-check>.</p>',
          matches: [
            {
              id: 1,
              word: "felher",
              shortMessage: "Rechtschreibfehler",
              message: "Möglicher Tippfehler gefunden.",
              category: "headnote",
              isIgnoredOnce: true,
            },
            {
              id: 2,
              word: "felher",
              shortMessage: "Rechtschreibfehler",
              message: "Möglicher Tippfehler gefunden.",
              category: "headnote",
              isIgnoredOnce: false,
            },
          ],
        }

        const headNoteEditor = page.getByTestId("Orientierungssatz")
        const headNoteEditorTextArea = headNoteEditor.locator("div")

        const guidingPrinciple = page.getByTestId("Leitsatz")
        const guidingPrincipleTextArea = guidingPrinciple.locator("div")
        const textWithErrors = {
          text: "Das ist ein felher. Das ist ein zweiter felher.",
          incorrectWords: ["felher"],
          ignoredWords: [],
        }

        await test.step("add text to headnote (Orientierungssatz)", async () => {
          await navigateToCategories(
            page,
            prefilledDocumentUnit.documentNumber,
            { category: DocumentUnitCategoriesEnum.TEXTS },
          )

          await clearTextField(page, headNoteEditorTextArea)
          await headNoteEditorTextArea.fill(textWithErrors.text)
          await expect(headNoteEditorTextArea).toHaveText(textWithErrors.text)
        })

        await test.step("trigger text check", async () => {
          // Prepare the lock for the first API call.
          const lock = Promise.withResolvers<void>()

          await page.route(
            `**/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}/text-check?category=headnote`,
            async (route) => {
              await lock.promise
              await route.fulfill({
                status: 200,
                contentType: "application/json",
                body: JSON.stringify(headnoteResponseInitial),
              })
            },
          )

          await page
            .getByLabel("Orientierungssatz Button")
            .getByRole("button", { name: "Rechtschreibprüfung" })
            .click()

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toHaveText("Rechtschreibprüfung läuft")

          // Release the lock to end the first text check.
          lock.resolve()
          await headNoteEditor
            .locator("text-check")
            .first()
            .waitFor({ state: "visible" })

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toBeHidden()
        })

        await test.step("ignoring a word once highlights it in blue", async () => {
          const textCheckTag = page
            .locator(`text-check[ignored='false']`)
            .first()

          const textCheckId = await getMarkId(textCheckTag)
          await textCheckTag.click()

          const modal = page.getByTestId("text-check-modal-word")

          await expect(modal).toBeVisible()
          await expect(modal.getByText("felher")).toBeVisible()
          await expect(page.getByText("Rechtschreibfehler")).toBeVisible()
          await expect(page.getByText("Hier ignorieren")).toBeVisible()
          await expect(
            page.getByText("In Dokumentationseinheit ignorieren"),
          ).toBeVisible()
          await expect(
            page.getByText("Zum Wörterbuch hinzufügen"),
          ).toBeVisible()
          await page.getByLabel("Hier ignorieren").click()

          await expect(page.locator(`ignore-once`)).toHaveCount(1)

          const ignoredRgbColors = convertHexToRGB(
            textCheckUnderlinesColors.ignored,
          )

          await expect(
            page.locator(`text-check[id='${textCheckId}']`).nth(0),
          ).toHaveCSS(
            "border-bottom",
            "2px solid " +
              `rgb(${ignoredRgbColors.red}, ${ignoredRgbColors.green}, ${ignoredRgbColors.blue})`,
          )
        })

        await test.step("the same word in the same category is still shown as error", async () => {
          const textCheckTag = page
            .locator(`text-check[ignored='false']`)
            .first()

          const textCheckId = await getMarkId(textCheckTag)

          const errorRGB = convertHexToRGB(textCheckUnderlinesColors.error)

          const currentTag = page.locator(`text-check[id='${textCheckId}']`)

          await expect(currentTag).toHaveCSS("text-decoration-style", "wavy")
          await expect(currentTag).toHaveCSS(
            "text-decoration-color",
            `rgb(${errorRGB.red}, ${errorRGB.green}, ${errorRGB.blue})`,
          )
        })

        await test.step("the same word in other category is still shown as error", async () => {
          await navigateToCategories(
            page,
            prefilledDocumentUnit.documentNumber,
            { category: DocumentUnitCategoriesEnum.TEXTS },
          )
          await clearTextField(page, guidingPrincipleTextArea)
          await guidingPrincipleTextArea.fill(textWithErrors.text)
          await expect(guidingPrincipleTextArea).toHaveText(textWithErrors.text)

          // Prepare the lock for the second API call.
          const lock = Promise.withResolvers<void>()

          await page.route(
            `**/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}/text-check?category=guidingPrinciple`,
            async (route) => {
              await lock.promise
              await route.fulfill({
                status: 200,
                contentType: "application/json",
                body: JSON.stringify(guidingPrincipleResponseInitial),
              })
            },
          )

          await page
            .getByLabel("Leitsatz Button")
            .getByRole("button", { name: "Rechtschreibprüfung" })
            .click()

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toHaveText("Rechtschreibprüfung läuft")

          // Release the lock to end the second text check.
          lock.resolve()

          await guidingPrinciple
            .locator("text-check")
            .first()
            .waitFor({ state: "visible" })

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toBeHidden()

          const textCheckTag = page
            .locator(`text-check[ignored='false']`)
            .first()

          const textCheckId = await getMarkId(textCheckTag)

          const errorRGB = convertHexToRGB(textCheckUnderlinesColors.error)

          const currentTag = guidingPrinciple.locator(
            `text-check[id='${textCheckId}']`,
          )

          await expect(currentTag).toHaveCSS("text-decoration-style", "wavy")
          await expect(currentTag).toHaveCSS(
            "text-decoration-color",
            `rgb(${errorRGB.red}, ${errorRGB.green}, ${errorRGB.blue})`,
          )
        })

        await test.step("running the text check again still shows once ignored word as ignored (not as error)", async () => {
          await headNoteEditorTextArea.click()
          await headNoteEditorTextArea.fill(" ")
          await page.keyboard.press("Backspace")
          // Prepare the lock for the third API call.
          const lock = Promise.withResolvers<void>()

          await page.route(
            `**/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}/text-check?category=headnote`,
            async (route) => {
              await lock.promise
              await route.fulfill({
                status: 200,
                contentType: "application/json",
                body: JSON.stringify(headnoteResponseAfterIgnore),
              })
            },
          )

          await page
            .getByLabel("Orientierungssatz Button")
            .getByRole("button", { name: "Rechtschreibprüfung" })
            .click()

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toHaveText("Rechtschreibprüfung läuft")

          lock.resolve()

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toBeHidden()

          await expect(page.locator(`text-check[ignored='true']`)).toHaveCount(
            1,
          )

          await expect(page.locator(`ignore-once`)).toHaveCount(1)

          const textCheckTag = page
            .locator(`text-check[ignored='true']`)
            .first()

          const textCheckId = await getMarkId(textCheckTag)

          const ignoredColors = convertHexToRGB(
            textCheckUnderlinesColors.ignored,
          )

          await expect(
            headNoteEditorTextArea
              .locator(`text-check[id='${textCheckId}']`)
              .nth(0),
          ).toHaveCSS(
            "border-bottom",
            "2px solid " +
              `rgb(${ignoredColors.red}, ${ignoredColors.green}, ${ignoredColors.blue})`,
          )
        })

        await test.step("clicking again on once ignored word shows correct modal options and can be unignored here and shows it as error again", async () => {
          const textCheckTag = page
            .locator(`text-check[ignored='true']`)
            .first()

          const textCheckId = await getMarkId(textCheckTag)
          await textCheckTag.click()

          const modal = page.getByTestId("text-check-modal-word")
          await expect(modal).toBeVisible()
          await expect(modal.getByText("felher")).toBeVisible()
          await expect(
            page.getByText("an dieser Stelle ignoriert"),
          ).toBeVisible()
          await expect(page.getByLabel("Hier nicht ignorieren")).toBeVisible()
          await expect(
            page.getByText("In Dokumentationseinheit ignorieren"),
          ).toBeVisible()
          await expect(
            page.getByText("Zum Wörterbuch hinzufügen"),
          ).toBeVisible()

          await page.getByLabel("Hier nicht ignorieren").click()

          await expect(page.locator(`ignore-once`)).toHaveCount(0)

          const errorRGB = convertHexToRGB(textCheckUnderlinesColors.error)

          const currentTag = headNoteEditor.locator(
            `text-check[id='${textCheckId}']`,
          )

          await expect(currentTag).toHaveCSS("text-decoration-style", "wavy")
          await expect(currentTag).toHaveCSS(
            "text-decoration-color",
            `rgb(${errorRGB.red}, ${errorRGB.green}, ${errorRGB.blue})`,
          )
        })
      },
    )

    test(
      "ignore word once, multi-occurrence",
      {
        tag: ["@RISDEV-9171"],
      },
      async ({ page, prefilledDocumentUnit }) => {
        const word = "errrror"
        const textWithErrors = {
          text: `Das ist ein ${word}. Das ist ein zweiter ${word}.`,
        }

        const initialResponse = {
          htmlText: `<p>Das ist ein <text-check id="1" type="misspelling" ignored="false">${word}</text-check>. Das ist ein zweiter <text-check id="2" type="misspelling" ignored="false">${word}</text-check>.</p>`,
          matches: [
            // Assuming Match IDs 1 and 2 correspond to the first and second 'errrror'
            {
              id: 1,
              word: word,
              shortMessage: "Rechtschreibfehler",
              category: "headnote",
              isIgnoredOnce: false,
            },
            {
              id: 2,
              word: word,
              shortMessage: "Rechtschreibfehler",
              category: "headnote",
              isIgnoredOnce: false,
            },
          ],
        }

        const headNoteEditor = page.getByTestId("Orientierungssatz")
        const headNoteEditorTextArea = headNoteEditor.locator("div")
        const errorRGB = convertHexToRGB(textCheckUnderlinesColors.error)
        const ignoredRgbColors = convertHexToRGB(
          textCheckUnderlinesColors.ignored,
        )

        await test.step("add text and run initial check", async () => {
          await navigateToCategories(
            page,
            prefilledDocumentUnit.documentNumber,
            {
              category: DocumentUnitCategoriesEnum.TEXTS,
            },
          )
          await clearTextField(page, headNoteEditorTextArea)
          await headNoteEditorTextArea.fill(textWithErrors.text)

          const lock = Promise.withResolvers<void>()
          await page.route(
            `**/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}/text-check?category=headnote`,
            async (route) => {
              await lock.promise
              await route.fulfill({
                status: 200,
                contentType: "application/json",
                body: JSON.stringify(initialResponse),
              })
            },
          )

          await page
            .getByLabel("Orientierungssatz Button")
            .getByRole("button", { name: "Rechtschreibprüfung" })
            .click()
          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toHaveText("Rechtschreibprüfung läuft")
          lock.resolve()
          await expect(page.locator(`text-check[ignored='false']`)).toHaveCount(
            2,
          )
        })

        await test.step("ignore the first occurrence once", async () => {
          const firstTag = headNoteEditor.locator('text-check[id="1"]')
          await firstTag.click()

          await page.getByLabel("Hier ignorieren").click()

          await expect(page.locator("ignore-once")).toHaveCount(1)
          await expect(firstTag).toHaveCSS(
            "border-bottom",
            "2px solid " +
              `rgb(${ignoredRgbColors.red}, ${ignoredRgbColors.green}, ${ignoredRgbColors.blue})`,
          )
        })

        await test.step("verify second word remains an error", async () => {
          const secondTag = headNoteEditor.locator('text-check[id="2"]')

          await expect(page.locator(`text-check[ignored='false']`)).toHaveCount(
            1,
          )
          await expect(page.locator("ignore-once")).toHaveCount(1)

          await expect(secondTag).toHaveCSS("text-decoration-style", "wavy")
          await expect(secondTag).toHaveCSS(
            "text-decoration-color",
            `rgb(${errorRGB.red}, ${errorRGB.green}, ${errorRGB.blue})`,
          )
        })
      },
    )

    test(
      "ignore word once, then clicking on ignore in docunit",
      {
        tag: ["@RISDEV-9171"],
      },
      async ({ page, prefilledDocumentUnit }) => {
        const headNoteEditor = page.getByTestId("Orientierungssatz")
        const headNoteEditorTextArea = headNoteEditor.locator("div")

        await test.step("add text to headnote (Orientierungssatz)", async () => {
          await navigateToCategories(
            page,
            prefilledDocumentUnit.documentNumber,
            { category: DocumentUnitCategoriesEnum.TEXTS },
          )

          await clearTextField(page, headNoteEditorTextArea)
          await headNoteEditorTextArea.fill(
            "Das ist ein felher. Das ist ein zweiter felher.",
          )
          await expect(headNoteEditorTextArea).toHaveText(
            "Das ist ein felher. Das ist ein zweiter felher.",
          )
        })

        await test.step("trigger text check", async () => {
          const { promise, resolve } = Promise.withResolvers<void>()

          await page.route(
            `**/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}/text-check?category=headnote`,
            async (route) => {
              await promise
              await route.fulfill({
                status: 200,
                contentType: "application/json",
                body: JSON.stringify({
                  htmlText:
                    '<p>Das ist ein <text-check id="1" type="misspelling" ignored="false">felher</text-check>. Das ist ein zweiter <text-check id="2" type="misspelling" ignored="false">felher</text-check>.</p>',
                  matches: [
                    {
                      id: 1,
                      word: "felher",
                      message: "Möglicher Tippfehler gefunden.",
                      shortMessage: "Rechtschreibfehler",
                      category: "headnote",
                      isIgnoredOnce: false,
                    },
                    {
                      id: 2,
                      word: "felher",
                      shortMessage: "Rechtschreibfehler",
                      message: "Möglicher Tippfehler gefunden.",
                      category: "headnote",
                      isIgnoredOnce: false,
                    },
                  ],
                }),
              })
            },
          )

          await page
            .getByLabel("Orientierungssatz Button")
            .getByRole("button", { name: "Rechtschreibprüfung" })
            .click()

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toHaveText("Rechtschreibprüfung läuft")

          resolve()
          await headNoteEditor
            .locator("text-check")
            .first()
            .waitFor({ state: "visible" })

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toBeHidden()
        })

        await test.step("ignoring a word once highlights it in blue", async () => {
          const textCheckTag = page
            .locator(`text-check[ignored='false']`)
            .first()

          const textCheckId = await getMarkId(textCheckTag)
          await textCheckTag.click()

          const modal = page.getByTestId("text-check-modal-word")

          await expect(modal).toBeVisible()
          await expect(modal.getByText("felher")).toBeVisible()
          await expect(page.getByText("Rechtschreibfehler")).toBeVisible()
          await expect(page.getByText("Hier ignorieren")).toBeVisible()
          await expect(
            page.getByText("In Dokumentationseinheit ignorieren"),
          ).toBeVisible()
          await expect(
            page.getByText("Zum Wörterbuch hinzufügen"),
          ).toBeVisible()
          await page.getByLabel("Hier ignorieren").click()

          await expect(page.locator(`ignore-once`)).toHaveCount(1)

          const ignoredRgbColors = convertHexToRGB(
            textCheckUnderlinesColors.ignored,
          )

          await expect(
            page.locator(`text-check[id='${textCheckId}']`).nth(0),
          ).toHaveCSS(
            "border-bottom",
            "2px solid " +
              `rgb(${ignoredRgbColors.red}, ${ignoredRgbColors.green}, ${ignoredRgbColors.blue})`,
          )
        })

        await test.step("the same word in the same category is still shown as error", async () => {
          const textCheckTag = page
            .locator(`text-check[ignored='false']`)
            .first()

          const textCheckId = await getMarkId(textCheckTag)

          const errorRGB = convertHexToRGB(textCheckUnderlinesColors.error)

          const currentTag = page.locator(`text-check[id='${textCheckId}']`)

          await expect(currentTag).toHaveCSS("text-decoration-style", "wavy")
          await expect(currentTag).toHaveCSS(
            "text-decoration-color",
            `rgb(${errorRGB.red}, ${errorRGB.green}, ${errorRGB.blue})`,
          )
        })

        await test.step("clicking on ignore in docunit, also marks all other similar words in docunit as ignored", async () => {
          await page.locator(`text-check[ignored='true']`).first().click()

          const modal = page.getByTestId("text-check-modal-word")
          await expect(modal).toBeVisible()

          await expect(page.getByLabel("Hier nicht ignorieren")).toBeVisible()

          await page.getByText("In Dokumentationseinheit ignorieren").click()

          await expect(page.locator(`ignore-once`)).toHaveCount(0)

          await expect(modal).toBeHidden()

          await expect(page.locator(`text-check[ignored='true']`)).toHaveCount(
            2,
          )

          const ignoredTags = page
            .getByTestId("Orientierungssatz")
            .locator('text-check[ignored="true"]')

          const ignoredRGB = convertHexToRGB(textCheckUnderlinesColors.ignored)
          for (let i = 0; i < (await ignoredTags.count()); i++) {
            const currentTag = ignoredTags.nth(i)
            await expect(currentTag).not.toHaveText("")
            await expect(currentTag).toHaveCSS(
              "border-bottom",
              `2px solid rgb(${ignoredRGB.red}, ${ignoredRGB.green}, ${ignoredRGB.blue})`,
            )
          }
        })

        await test.step("clicking again on docunit ignored word shows no ignore once option anymore", async () => {
          await page.locator(`text-check[id="2"]`).first().click()

          const modal = page.getByTestId("text-check-modal")
          await expect(modal).toBeVisible()

          await expect(page.getByText("Hier ignorieren")).toBeHidden()
          await expect(page.getByText("Hier nicht ignorieren")).toBeHidden()
          await expect(
            page.getByText("in dieser Dokumentationseinheit ignoriert"),
          ).toBeVisible()
          await expect(
            page.getByText("Nicht in Dokumentationseinheit ignorieren"),
          ).toBeVisible()
          await expect(
            page.getByText("Zum Wörterbuch hinzufügen"),
          ).toBeVisible()
        })
      },
    )

    test(
      "ignore word once, then clicking on ignore globally",
      {
        tag: ["@RISDEV-9171"],
      },
      async ({ page, prefilledDocumentUnit }) => {
        const headNoteEditor = page.getByTestId("Orientierungssatz")
        const headNoteEditorTextArea = headNoteEditor.locator("div")

        await test.step("add text to headnote (Orientierungssatz)", async () => {
          await navigateToCategories(
            page,
            prefilledDocumentUnit.documentNumber,
            { category: DocumentUnitCategoriesEnum.TEXTS },
          )

          await clearTextField(page, headNoteEditorTextArea)
          await headNoteEditorTextArea.fill(
            "Das ist ein felher. Das ist ein zweiter felher.",
          )
          await expect(headNoteEditorTextArea).toHaveText(
            "Das ist ein felher. Das ist ein zweiter felher.",
          )
        })

        await test.step("trigger text check", async () => {
          const { promise, resolve } = Promise.withResolvers<void>()

          await page.route(
            `**/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}/text-check?category=headnote`,
            async (route) => {
              await promise
              await route.fulfill({
                status: 200,
                contentType: "application/json",
                body: JSON.stringify({
                  htmlText:
                    '<p>Das ist ein <text-check id="1" type="misspelling" ignored="false">felher</text-check>. Das ist ein zweiter <text-check id="2" type="misspelling" ignored="false">felher</text-check>.</p>',
                  matches: [
                    {
                      id: 1,
                      word: "felher",
                      message: "Möglicher Tippfehler gefunden.",
                      shortMessage: "Rechtschreibfehler",
                      category: "headnote",
                      isIgnoredOnce: false,
                    },
                    {
                      id: 2,
                      word: "felher",
                      shortMessage: "Rechtschreibfehler",
                      message: "Möglicher Tippfehler gefunden.",
                      category: "headnote",
                      isIgnoredOnce: false,
                    },
                  ],
                }),
              })
            },
          )

          await page
            .getByLabel("Orientierungssatz Button")
            .getByRole("button", { name: "Rechtschreibprüfung" })
            .click()

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toHaveText("Rechtschreibprüfung läuft")

          resolve()
          await headNoteEditor
            .locator("text-check")
            .first()
            .waitFor({ state: "visible" })

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toBeHidden()
        })

        await test.step("ignoring a word once highlights it in blue", async () => {
          const textCheckTag = page
            .locator(`text-check[ignored='false']`)
            .first()

          const textCheckId = await getMarkId(textCheckTag)
          await textCheckTag.click()

          const modal = page.getByTestId("text-check-modal-word")

          await expect(modal).toBeVisible()
          await expect(modal.getByText("felher")).toBeVisible()
          await expect(page.getByText("Rechtschreibfehler")).toBeVisible()
          await expect(page.getByText("Hier ignorieren")).toBeVisible()
          await expect(
            page.getByText("In Dokumentationseinheit ignorieren"),
          ).toBeVisible()
          await expect(
            page.getByText("Zum Wörterbuch hinzufügen"),
          ).toBeVisible()
          await page.getByLabel("Hier ignorieren").click()

          const ignoredRgbColors = convertHexToRGB(
            textCheckUnderlinesColors.ignored,
          )

          await expect(
            page.locator(`text-check[id='${textCheckId}']`).nth(0),
          ).toHaveCSS(
            "border-bottom",
            "2px solid " +
              `rgb(${ignoredRgbColors.red}, ${ignoredRgbColors.green}, ${ignoredRgbColors.blue})`,
          )
        })

        await test.step("the same word in the same category is still shown as error", async () => {
          const textCheckTag = page
            .locator(`text-check[ignored='false']`)
            .first()

          const textCheckId = await getMarkId(textCheckTag)

          const errorRGB = convertHexToRGB(textCheckUnderlinesColors.error)

          const currentTag = page.locator(`text-check[id='${textCheckId}']`)

          await expect(currentTag).toHaveCSS("text-decoration-style", "wavy")
          await expect(currentTag).toHaveCSS(
            "text-decoration-color",
            `rgb(${errorRGB.red}, ${errorRGB.green}, ${errorRGB.blue})`,
          )
        })

        await test.step("clicking on ignore globally, also marks all other similar words as ignored", async () => {
          await expect(page.locator(`ignore-once`)).toHaveCount(1)
          await expect(page.locator(`text-check[ignored='true']`)).toHaveCount(
            1,
          )

          await page.locator(`text-check[ignored='true']`).first().click()

          const modal = page.getByTestId("text-check-modal-word")
          await expect(modal).toBeVisible()

          await expect(page.getByLabel("Hier nicht ignorieren")).toBeVisible()

          await page.getByText("Zum Wörterbuch hinzufügen").click()

          await expect(modal).toBeHidden()

          await expect(page.locator(`text-check[ignored='true']`)).toHaveCount(
            2,
          )

          await expect(page.locator(`ignore-once`)).toHaveCount(0)

          const ignoredTags = page
            .getByTestId("Orientierungssatz")
            .locator('text-check[ignored="true"]')

          const ignoredRGB = convertHexToRGB(textCheckUnderlinesColors.ignored)
          for (let i = 0; i < (await ignoredTags.count()); i++) {
            const currentTag = ignoredTags.nth(i)
            await expect(currentTag).not.toHaveText("")
            await expect(currentTag).toHaveCSS(
              "border-bottom",
              `2px solid rgb(${ignoredRGB.red}, ${ignoredRGB.green}, ${ignoredRGB.blue})`,
            )
          }
        })

        await test.step("clicking again on docunit ignored word shows no ignore once option anymore", async () => {
          await page.locator(`text-check[id="2"]`).first().click()

          const modal = page.getByTestId("text-check-modal")
          await expect(modal).toBeVisible()

          await expect(page.getByText("Hier ignorieren")).toBeHidden()
          await expect(page.getByText("Hier nicht ignorieren")).toBeHidden()
          await expect(
            page.getByText("im Wörterbuch / für alle Dokstellen ignoriert"),
          ).toBeVisible()
          await expect(page.getByText("Aus Wörterbuch entfernen")).toBeVisible()
        })
      },
    )
  },
)
