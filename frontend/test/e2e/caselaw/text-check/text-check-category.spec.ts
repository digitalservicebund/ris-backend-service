import { expect, Locator } from "@playwright/test"
import { clearTextField, navigateToCategories } from "../utils/e2e-utils"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { convertHexToRGB } from "~/test-helper/coloursUtil"

// eslint-disable-next-line playwright/no-skipped-test
test.skip(
  ({ browserName }) => browserName !== "chromium",
  "Skipping firefox flaky test",
)

const textCheckUnderlinesColors = {
  error: "#cd5038",
  ignored: "#66add3",
} as const

async function getMarkId(tag: Locator): Promise<string | null> {
  return await tag.evaluate((el) => el.getAttribute("id"))
}

const textWithErrors = {
  text: "LanguageTool   ist ist Ihr intelligenter Schreibassistent für alle gängigen Browser und Textverarbeitungsprogramme. Schreiben sie in diesem Textfeld oder fügen Sie einen Text ein. Rechtshcreibfehler werden rot markirt, Grammatikfehler werden gelb hervor gehoben und Stilfehler werden, anders wie die anderen Fehler, blau unterstrichen. wussten Sie dass Synonyme per Doppelklick auf ein Wort aufgerufen werden können? Nutzen Sie LanguageTool in allen Lebenslagen, z. B. wenn Sie am Donnerstag, dem 13. Mai 2022, einen Basketballkorb in 10 Fuß Höhe montieren möchten. Testgnorierteswort ist zB. grün markiert",
  incorrectWords: [
    "ist ist", // GERMAN_WORD_REPEAT_RULE
    "Rechtshcreibfehler",
    "markirt",
  ],
  ignoredWords: ["zB", "Testgnorierteswort"],
}

test.describe(
  "check text category",
  {
    tag: ["@RISDEV-254"],
  },
  () => {
    test(
      "ignore irrelevant text check categories and rules",
      {
        tag: ["@RISDEV-9169", "@RISDEV-9170"],
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

        await test.step("replace text in headnote (Orientierungssatz) with irrelevant style-related mistakes", async () => {
          await clearTextField(page, headNoteEditorTextArea)

          // Contains examples of Categories we disable
          const textWithErrorsOfDisabledCategories =
            "I bims, LanguageTool. " + // COLLOQUIALISMS: I bims
            "Im täglichen Alltag prüfe ich Texte. " + // REDUNDANCY: täglichen Alltag
            "Dann habe ich Freizeit. Dann esse ich. Dann schlafe ich. " + // REPETITIONS_STYLE: Dann []. Dann []. Dann [].
            "Mir ist es egal, ob du Helpdesk oder Help-Desk schreibst." // STYLE: Helpdesk oder Help-Desk

          await headNoteEditorTextArea.fill(textWithErrorsOfDisabledCategories)
          await expect(headNoteEditorTextArea).toHaveText(
            textWithErrorsOfDisabledCategories,
          )
        })

        await test.step("trigger category text results in no matches for ignored categories", async () => {
          await page
            .getByLabel("Orientierungssatz Button")
            .getByRole("button", { name: "Rechtschreibprüfung" })
            .click()

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toHaveText("Rechtschreibprüfung läuft")

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toBeHidden({ timeout: 10_000 })

          await expect(page.locator(`text-check`)).not.toBeAttached()
        })

        await test.step("replace text in headnote (Orientierungssatz) with mistakes of ignored rules", async () => {
          await clearTextField(page, headNoteEditorTextArea)

          // Contains examples of Rules we disable
          const textWithErrorsOfDisabledRules =
            "der Satz wurde, " + // UPPERCASE_SENTENCE_START: "der"
            "anders als oft behauptet, " + // WIKIPEDIA: "Anders als oft behauptet"
            "nicht von Feuerwehrmännern " + // GENDER_NEUTRALITY / Geschlechtergerechte Sprache: "Erstsemsterin"
            "geschrieben.Noch " + // MISC / Sonstiges: "[Ein Satz].[Noch ein Satz]"
            "hat er mehr als 24Std. " + // TYPOGRAPHY / Typografie: "24Std."
            "oder gar 25 Std.. gedauert. " + // PUNCTUATION / Zeichensetzung: "Std.."
            "Ich freue ich " + // CONFUSED_WORDS / Leicht zu verwechselnde Wörter: "Ich freue ich"
            "seit Geburt an, " + // IDIOMS / Redewendungen: "seit Geburt an"
            "auf die Haus " + // GRAMMAR / Grammatik: "die Haus"
            "nach dem es Berg ab geht. " + // COMPOUNDING / Getrennt- und Zusammenschreibung: "Berg ab"
            "Das tief greifende Problem " + // EMPFOHLENE_RECHTSCHREIBUNG / Empfohlene/Moderne Rechtschreibung: "tief greifende"
            "ist das ich den Film schauen wollte. " + // HILFESTELLUNG_KOMMASETZUNG
            "Aber morgen schien die Sonne." // SEMANTICS / Semantische Unstimmigkeiten: "morgen schien"

          await headNoteEditorTextArea.fill(textWithErrorsOfDisabledRules)
          await expect(headNoteEditorTextArea).toHaveText(
            textWithErrorsOfDisabledRules,
          )
        })

        await test.step("trigger category text button shows results in no matches for ignored rules", async () => {
          await page
            .getByLabel("Orientierungssatz Button")
            .getByRole("button", { name: "Rechtschreibprüfung" })
            .click()

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toHaveText("Rechtschreibprüfung läuft")

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toBeHidden({ timeout: 10_000 })

          await expect(page.locator(`text-check`)).not.toBeAttached()
        })
      },
    )

    // eslint-disable-next-line playwright/no-skipped-test
    test.skip(
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

          const ignoredRGB = convertHexToRGB(textCheckUnderlinesColors.ignored)
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

            await expect(currentTag).toHaveCSS("text-decoration-style", "wavy")
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
          await expect(page.getByTestId("text-check-modal-word")).toBeVisible()
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

          await expect(page.getByTestId("text-check-modal-word")).toBeVisible()
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

          await expect(page.getByTestId("text-check-modal-word")).toBeVisible()

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
            "**/api/v1/caselaw/documentunits/" +
              prefilledDocumentUnit.uuid +
              "/text-check*",
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

          await expect(page.getByText("Rechtschreibprüfung läuft")).toBeHidden()

          await test.step("enter text after text check ended", async () => {
            await reasonEditor.fill("Text added after text check ended.")

            await expect(reasonEditor).toHaveText(
              "Text added after text check ended.",
            )
          })
        },
      )
    })

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
        const textCheckResolvers = [] as {
          promise: Promise<void>
          resolve: () => void
        }[]

        // A counter to track which promise in the array corresponds to the current API call.
        let callCount = 0

        // Helper to create a new lock object and add it to the array.
        const createLock = () => {
          const { promise, resolve } = Promise.withResolvers<void>()
          textCheckResolvers.push({ promise, resolve })
        }

        // Mock the API route for text-check ONCE for the entire test.
        await page.route(
          "**/api/v1/caselaw/documentunits/" +
            prefilledDocumentUnit.uuid +
            "/text-check*",
          async (route) => {
            // Get the lock object corresponding to the current call index.
            const currentLock = textCheckResolvers[callCount]
            const currentCallIndex = callCount
            callCount++ // Increment the counter for the next API call.

            // PAUSE the network response until resolve() is called in the test body.
            await currentLock.promise

            let responseToFulfill

            if (currentCallIndex === 0) {
              responseToFulfill = headnoteResponseInitial
            } else if (currentCallIndex === 1) {
              responseToFulfill = guidingPrincipleResponseInitial
            } else if (currentCallIndex === 2) {
              responseToFulfill = headnoteResponseAfterIgnore
            } else {
              responseToFulfill = {}
            }

            await route.fulfill({
              status: 200,
              contentType: "application/json",
              body: JSON.stringify(responseToFulfill),
            })
          },
        )

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
          createLock()

          await page
            .getByLabel("Orientierungssatz Button")
            .getByRole("button", { name: "Rechtschreibprüfung" })
            .click()

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toHaveText("Rechtschreibprüfung läuft")

          // Release the lock to end the first text check.
          textCheckResolvers[0].resolve()
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
          createLock()

          await page
            .getByLabel("Leitsatz Button")
            .getByRole("button", { name: "Rechtschreibprüfung" })
            .click()

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toHaveText("Rechtschreibprüfung läuft")

          // Release the lock to end the second text check.
          textCheckResolvers[1].resolve()

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
          createLock()

          await page
            .getByLabel("Orientierungssatz Button")
            .getByRole("button", { name: "Rechtschreibprüfung" })
            .click()

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toHaveText("Rechtschreibprüfung läuft")

          textCheckResolvers[2].resolve()

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
            "**/api/v1/caselaw/documentunits/" +
              prefilledDocumentUnit.uuid +
              "/text-check*",
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
            "**/api/v1/caselaw/documentunits/" +
              prefilledDocumentUnit.uuid +
              "/text-check*",
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
