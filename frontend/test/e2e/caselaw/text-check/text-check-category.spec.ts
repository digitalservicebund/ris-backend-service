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

        await test.step("accept a selected suggestion replaces in text", async () => {
          const textCheckLiteral = "Rechtshcreibfehler"
          await headNoteEditor.getByText(textCheckLiteral).click()

          await page.getByTestId("suggestion-accept-button").click()
          await expect(page.getByTestId("text-check-modal")).toBeHidden()

          await expect(headNoteEditorTextArea).toHaveText(
            textWithErrors.text.replace(textCheckLiteral, "Rechtschreibfehler"),
          )

          await expect(page.locator(`text-check[id='${2}']`)).not.toBeAttached()
        })

        await test.step("click on a selected suggestion, then click on a non-tag closes the text check modal", async () => {
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
              /.*(Von jDV ignoriert|Aus globalem Wörterbuch entfernen|Nicht ignorieren).*/i,
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
  },
)
