import { expect } from "@playwright/test"
import { clearTextField, navigateToCategories } from "../utils/e2e-utils"
import { getMarkId, textCheckUnderlinesColors } from "./util"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import { pendingProceedingLabels } from "@/domain/pendingProceeding"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { convertHexToRGB } from "~/test-helper/coloursUtil"

const textWithErrors = {
  text: "LanguageTool   ist ist Ihr intelligenter Schreibassistent für alle gängigen Browser und Textverarbeitungsprogramme. Schreiben sie in diesem Textfeld oder fügen Sie einen Text ein. Rechtshcreibfehler werden rot markirt, Grammatikfehler werden gelb hervor gehoben und Stilfehler werden, anders wie die anderen Fehler, blau unterstrichen. wussten Sie dass Synonyme per Doppelklick auf ein Wort aufgerufen werden können? Nutzen Sie LanguageTool in allen Lebenslagen, z. B. wenn Sie am Donnerstag, dem 13. Mai 2022, einen Basketballkorb in 10 Fuß Höhe montieren möchten. Testgnorierteswort ist zB. grün markiert",
  incorrectWords: [
    "ist ist", // GERMAN_WORD_REPEAT_RULE
    "Rechtshcreibfehler",
    "markirt",
  ],
  ignoredWords: ["zB", "Testgnorierteswort"],
}

test.describe("check text category", () => {
  test.describe(
    "for decisions",
    {
      tag: ["@RISDEV-254"],
    },
    () => {
      test(
        "clicking on text check button, save document and returns matches",
        {
          tag: ["@RISDEV-6205", "@RISDEV-6154", "@RISDEV-7397"],
        },
        async ({ page, prefilledDocumentUnit }) => {
          const headNoteEditor = page.getByTestId("Orientierungssatz")
          const headNoteEditorTextArea = headNoteEditor.locator("div")

          await test.step("navigate to headnote (Orientierungssatz) in categories", async () => {
            await navigateToCategories(
              page,
              prefilledDocumentUnit.documentNumber,
              { category: DocumentUnitCategoriesEnum.TEXTS },
            )
          })

          await test.step("replace text in headnote (Orientierungssatz)", async () => {
            await clearTextField(page, headNoteEditorTextArea)

            await headNoteEditorTextArea.fill(textWithErrors.text)
            await expect(headNoteEditorTextArea).toHaveText(textWithErrors.text)
          })

          await test.step("trigger category text button shows loading status and highlights matches", async () => {
            await page
              .getByLabel("Orientierungssatz Button")
              .getByRole("button", { name: "Rechtschreibprüfung" })
              .click()

            await expect(
              page.getByTestId("text-check-loading-status"),
            ).toHaveText("Rechtschreibprüfung läuft")

            await headNoteEditor
              .locator("text-check")
              .first()
              .waitFor({ state: "visible" })
          })

          // make sure consecutive whitespaces are not removed (RISDEV-9343)
          await test.step("returned text is unchanged", async () => {
            await expect(headNoteEditorTextArea).toHaveText(
              /LanguageTool {3}ist .*/,
            )
          })

          await test.step("text check tags are marked by type with corresponded color", async () => {
            const ignoredTags = page
              .getByTestId("Orientierungssatz")
              .locator('text-check[ignored="true"]')

            const errorTags = page
              .getByTestId("Orientierungssatz")
              .locator('text-check:not([ignored="true"])')

            const ignoredRGB = convertHexToRGB(
              textCheckUnderlinesColors.ignored,
            )
            for (let i = 0; i < (await ignoredTags.count()); i++) {
              const currentTag = ignoredTags.nth(i)
              await expect(currentTag).not.toHaveText("")
              await expect(currentTag).toHaveCSS(
                "border-bottom",
                `2px solid rgb(${ignoredRGB.red}, ${ignoredRGB.green}, ${ignoredRGB.blue})`,
              )
            }

            // 3. Test all STANDARD ERROR words (wavy red line)
            // Calculate the single error color once before the loop
            const errorRGB = convertHexToRGB(textCheckUnderlinesColors.error)
            for (let i = 0; i < (await errorTags.count()); i++) {
              const currentTag = errorTags.nth(i)
              await expect(currentTag).not.toHaveText("")

              await expect(currentTag).toHaveCSS(
                "text-decoration-style",
                "wavy",
              )
              await expect(currentTag).toHaveCSS(
                "text-decoration-color",
                `rgb(${errorRGB.red}, ${errorRGB.green}, ${errorRGB.blue})`,
              )
            }
          })

          await test.step("clicking on text check tags opens corresponded modal", async () => {
            for (
              let textCheckIndex = 0;
              textCheckIndex < textWithErrors.incorrectWords.length;
              textCheckIndex++
            ) {
              const allTextCheckById = page.locator(
                `text-check[id='${textCheckIndex + 1}']`,
              )
              const totalTextCheckTags = await allTextCheckById.count()

              for (let index = 0; index < totalTextCheckTags; index++) {
                const textCheckTag = allTextCheckById.nth(index)
                await textCheckTag.click()
                await expect(page.getByTestId("text-check-modal")).toBeVisible()
                await expect(
                  page.getByTestId("text-check-modal-word"),
                ).toContainText(textWithErrors.incorrectWords[textCheckIndex])
              }
            }
          })

          await test.step("click on a text check tags, then click on a non-tag closes the text check modal", async () => {
            await page.locator("text-check").first().click()
            await expect(
              page.getByTestId("text-check-modal-word"),
            ).toBeVisible()
            await headNoteEditor.getByText("LanguageTool").click()
            await expect(page.getByTestId("text-check-modal-word")).toBeHidden()
          })

          await test.step("clicking on an ignored words shows ignore reason", async () => {
            const allTextCheckById = page.locator(`text-check[type='ignored']`)
            const totalTextCheckTags = await allTextCheckById.count()

            for (let index = 0; index < totalTextCheckTags; index++) {
              const textCheckTag = allTextCheckById.nth(index)
              await textCheckTag.click()
              await expect(page.getByTestId("text-check-modal")).toBeVisible()

              const locator = page.getByTestId("ignored-word-handler")
              await expect(locator).toHaveText(
                /.*(Von jDV ignoriert|Aus Wörterbuch entfernen|Nicht ignorieren).*/i,
              )
            }
          })

          await test.step("ignoring a word highlights it in blue after text re-check", async () => {
            const textCheckTag = page
              .locator(`text-check[ignored='false']`)
              .first()

            const textCheckId = await getMarkId(textCheckTag)

            await textCheckTag.click()

            await expect(
              page.getByTestId("text-check-modal-word"),
            ).toBeVisible()
            await page.getByTestId("ignored-word-add-button").click()

            const rgbColors = convertHexToRGB(textCheckUnderlinesColors.ignored)
            await expect(
              page.getByTestId("text-check-loading-status"),
            ).toBeHidden()

            await expect(
              page.locator(`text-check[id='${textCheckId}']`).nth(0),
            ).toHaveCSS(
              "border-bottom",
              "2px solid " +
                `rgb(${rgbColors.red}, ${rgbColors.green}, ${rgbColors.blue})`,
            )
          })

          await test.step("removing ignored word highlights it in red after text re-check", async () => {
            const textCheckTag = page
              .locator(`text-check[ignored='true']`)
              .first()

            const textCheckId = await getMarkId(textCheckTag)

            await textCheckTag.click()

            await expect(
              page.getByTestId("text-check-modal-word"),
            ).toBeVisible()

            const removeIgnoredWordButton = page.getByTestId(
              /^(ignored-word-remove-button|ignored-word-global-remove-button)$/,
            )
            await removeIgnoredWordButton.click()

            const rgbColors = convertHexToRGB(textCheckUnderlinesColors.error)

            await expect(
              page.getByTestId("text-check-loading-status"),
            ).toBeHidden()

            await expect(
              page.locator(`text-check[id='${textCheckId}']`).nth(0),
            ).toHaveCSS("text-decoration-style", "wavy")

            await expect(
              page.locator(`text-check[id='${textCheckId}']`).nth(0),
            ).toHaveCSS(
              "text-decoration-color",
              `rgb(${rgbColors.red}, ${rgbColors.green}, ${rgbColors.blue})`,
            )
          })
        },
      )

      const results = [
        { status: "successful", result: { status: 200, body: "" } },
        { status: "failed", result: { status: 500, body: "" } },
      ]

      results.forEach(({ status, result }) => {
        test(
          "disable editor and set back the old status after the " +
            status +
            " call",
          {
            tag: ["@RISDEV-9481"],
          },
          async ({ page, prefilledDocumentUnit }) => {
            const { promise: lock, resolve: releaseLock } =
              Promise.withResolvers<void>()

            await page.route(
              `**/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}/text-check*`,
              async (route) => {
                await lock
                await route.fulfill(result)
              },
            )

            await test.step("navigate to reason (Gründe) in categories", async () => {
              await navigateToCategories(
                page,
                prefilledDocumentUnit.documentNumber,
                { category: DocumentUnitCategoriesEnum.TEXTS },
              )
            })

            await test.step("open reason editor", async () => {
              await page
                .getByRole("button", { name: "Gründe", exact: true })
                .click()
            })

            const reasonEditor = page.getByTestId("Gründe").locator("div")

            await test.step("fill text into reason editor", async () => {
              await reasonEditor.fill("This is text before running text check.")

              await expect(reasonEditor).toHaveText(
                "This is text before running text check.",
              )
            })

            await test.step("trigger text check", async () => {
              await page
                .getByLabel("Gründe Button")
                .getByRole("button", { name: "Rechtschreibprüfung" })
                .click()
            })

            await test.step("check editor is not editable", async () => {
              await expect(reasonEditor).toHaveAttribute(
                "contenteditable",
                "false",
              )
            })

            await test.step("end text check", async () => {
              releaseLock()
            })

            await expect(
              page.getByText("Rechtschreibprüfung läuft"),
            ).toBeHidden()

            await test.step("enter text after text check ended", async () => {
              await reasonEditor.fill("Text added after text check ended.")

              await expect(reasonEditor).toHaveText(
                "Text added after text check ended.",
              )
            })
          },
        )
      })
    },
  )

  test.describe(
    "for pending proceedings",
    {
      tag: ["@RISDEV-9022"],
    },
    () => {
      const allowedTextCheckCategories = [
        {
          name: "resolutionNote",
          germanWord: pendingProceedingLabels.resolutionNote,
        },
        {
          name: "headline",
          germanWord: pendingProceedingLabels.headline,
        },
        {
          name: "legalIssue",
          germanWord: pendingProceedingLabels.legalIssue + " *",
        },
      ]

      allowedTextCheckCategories.forEach(({ name, germanWord }) => {
        test(
          `${name}: clicking on text check button, save document and returns matches`,
          {
            tag: ["@RISDEV-10128"],
          },
          async ({ page, prefilledPendingProceeding }) => {
            const editor = page.locator(`#${name}`).getByTestId(germanWord)
            const textArea = editor.locator("div")

            await test.step(`navigate to ${name} (${germanWord}) in categories`, async () => {
              await navigateToCategories(
                page,
                prefilledPendingProceeding.documentNumber,
                {
                  category: DocumentUnitCategoriesEnum.TEXTS,
                  type: "pending-proceeding",
                },
              )
            })

            await test.step(`replace text in ${name} (${germanWord})`, async () => {
              await clearTextField(page, textArea)

              await textArea.fill(textWithErrors.text)
              await expect(textArea).toHaveText(textWithErrors.text)
            })

            await test.step("trigger category text button shows loading status and highlights matches", async () => {
              await page
                .getByLabel(`${germanWord} Button`)
                .getByRole("button", { name: "Rechtschreibprüfung" })
                .click()

              await expect(
                page.getByTestId("text-check-loading-status"),
              ).toHaveText("Rechtschreibprüfung läuft")

              await editor
                .locator("text-check")
                .first()
                .waitFor({ state: "visible" })
            })

            // make sure consecutive whitespaces are not removed (RISDEV-9343)
            await test.step("returned text is unchanged", async () => {
              await expect(textArea).toHaveText(/LanguageTool {3}ist .*/)
            })

            await test.step("text check tags are marked by type with corresponded color", async () => {
              const ignoredTags = page
                .getByTestId(germanWord)
                .locator('text-check[ignored="true"]')

              const errorTags = page
                .getByTestId(germanWord)
                .locator('text-check:not([ignored="true"])')

              const ignoredRGB = convertHexToRGB(
                textCheckUnderlinesColors.ignored,
              )
              for (let i = 0; i < (await ignoredTags.count()); i++) {
                const currentTag = ignoredTags.nth(i)
                await expect(currentTag).not.toHaveText("")
                await expect(currentTag).toHaveCSS(
                  "border-bottom",
                  `2px solid rgb(${ignoredRGB.red}, ${ignoredRGB.green}, ${ignoredRGB.blue})`,
                )
              }

              // 3. Test all STANDARD ERROR words (wavy red line)
              // Calculate the single error color once before the loop
              const errorRGB = convertHexToRGB(textCheckUnderlinesColors.error)
              for (let i = 0; i < (await errorTags.count()); i++) {
                const currentTag = errorTags.nth(i)
                await expect(currentTag).not.toHaveText("")

                await expect(currentTag).toHaveCSS(
                  "text-decoration-style",
                  "wavy",
                )
                await expect(currentTag).toHaveCSS(
                  "text-decoration-color",
                  `rgb(${errorRGB.red}, ${errorRGB.green}, ${errorRGB.blue})`,
                )
              }
            })

            await test.step("clicking on text check tags opens corresponded modal", async () => {
              for (
                let textCheckIndex = 0;
                textCheckIndex < textWithErrors.incorrectWords.length;
                textCheckIndex++
              ) {
                const allTextCheckById = textArea.locator(
                  `text-check[id='${textCheckIndex + 1}']`,
                )
                const totalTextCheckTags = await allTextCheckById.count()

                for (let index = 0; index < totalTextCheckTags; index++) {
                  const textCheckTag = allTextCheckById.nth(index)
                  await textCheckTag.click()
                  await expect(
                    page.getByTestId("text-check-modal"),
                  ).toBeVisible()
                  await expect(
                    page.getByTestId("text-check-modal-word"),
                  ).toContainText(textWithErrors.incorrectWords[textCheckIndex])
                }
              }
            })

            await test.step("click on a text check tags, then click on a non-tag closes the text check modal", async () => {
              await textArea.locator("text-check").first().click()
              await expect(
                page.getByTestId("text-check-modal-word"),
              ).toBeVisible()
              await editor.getByText("LanguageTool").click()
              await expect(
                page.getByTestId("text-check-modal-word"),
              ).toBeHidden()
            })

            await test.step("clicking on an ignored words shows ignore reason", async () => {
              const allTextCheckById = page.locator(
                `text-check[type='ignored']`,
              )
              const totalTextCheckTags = await allTextCheckById.count()

              for (let index = 0; index < totalTextCheckTags; index++) {
                const textCheckTag = allTextCheckById.nth(index)
                await textCheckTag.click()
                await expect(page.getByTestId("text-check-modal")).toBeVisible()

                const locator = page.getByTestId("ignored-word-handler")
                await expect(locator).toHaveText(
                  /.*(Von jDV ignoriert|Aus Wörterbuch entfernen|Nicht ignorieren).*/i,
                )
              }
            })

            await test.step("ignoring a word highlights it in blue after text re-check", async () => {
              const textCheckTag = textArea
                .locator(`text-check[ignored='false']`)
                .first()

              const textCheckId = await getMarkId(textCheckTag)

              await textCheckTag.click()

              await expect(
                page.getByTestId("text-check-modal-word"),
              ).toBeVisible()
              await page.getByTestId("ignored-word-add-button").click()

              const rgbColors = convertHexToRGB(
                textCheckUnderlinesColors.ignored,
              )
              await expect(
                page.getByTestId("text-check-loading-status"),
              ).toBeHidden()

              await expect(
                textArea.locator(`text-check[id='${textCheckId}']`).nth(0),
              ).toHaveCSS(
                "border-bottom",
                "2px solid " +
                  `rgb(${rgbColors.red}, ${rgbColors.green}, ${rgbColors.blue})`,
              )
            })

            await test.step("removing ignored word highlights it in red after text re-check", async () => {
              const textCheckTag = textArea
                .locator(`text-check[ignored='true']`)
                .first()

              const textCheckId = await getMarkId(textCheckTag)

              await textCheckTag.click()

              await expect(
                page.getByTestId("text-check-modal-word"),
              ).toBeVisible()

              const removeIgnoredWordButton = page.getByTestId(
                /^(ignored-word-remove-button|ignored-word-global-remove-button)$/,
              )
              await removeIgnoredWordButton.click()

              const rgbColors = convertHexToRGB(textCheckUnderlinesColors.error)

              await expect(
                page.getByTestId("text-check-loading-status"),
              ).toBeHidden()

              await expect(
                textArea.locator(`text-check[id='${textCheckId}']`).first(),
              ).toHaveCSS("text-decoration-style", "wavy")

              await expect(
                textArea.locator(`text-check[id='${textCheckId}']`).first(),
              ).toHaveCSS(
                "text-decoration-color",
                `rgb(${rgbColors.red}, ${rgbColors.green}, ${rgbColors.blue})`,
              )
            })
          },
        )
      })
    },
  )
})
