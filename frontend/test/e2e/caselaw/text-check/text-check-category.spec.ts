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
  uncategorized: "#e86a69",
  style: "#9d8eff",
  grammar: "#eeb55c",
  typographical: "#eeb55c",
  ignored: "#26A7F2",
  misspelling: "#e86a69",
} as const

type TextCheckType = keyof typeof textCheckUnderlinesColors

async function getMarkId(tag: Locator): Promise<string | null> {
  return await tag.evaluate((el) => el.getAttribute("id"))
}

function getTextCheckColorRGB(type: string | null): {
  red: number
  green: number
  blue: number
} {
  const typeKey = (type ?? "uncategorized") as TextCheckType
  const hex = textCheckUnderlinesColors[typeKey]
  return convertHexToRGB(hex)
}

const textWithErrors = {
  text: "LanguageTool ist Ihr intelligenter Schreibassistent für alle gängigen Browser und Textverarbeitungsprogramme. Schreiben sie in diesem Textfeld oder fügen Sie einen Text ein. Rechtshcreibfehler werden rot markirt, Grammatikfehler werden gelb hervor gehoben und Stilfehler werden, anders wie die anderen Fehler, blau unterstrichen. wussten Sie dass Synonyme per Doppelklick auf ein Wort aufgerufen werden können? Nutzen Sie LanguageTool in allen Lebenslagen, zB. wenn Sie am Donnerstag, dem 13. Mai 2022, einen Basketballkorb in 10 Fuß Höhe montieren möchten. Testgnorierteswort ist grün markiert",
  incorrectWords: [
    "sie",
    "Rechtshcreibfehler",
    "markirt",
    "hervor gehoben",
    "wie",
    "wussten",
    "Sie dass",
    "zB.",
    "Donnerstag, dem 13",
  ],
  ignoredWords: ["Testgnorierteswort"],
}

test.describe(
  "check text category",
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

        await test.step("text check tags are marked by type with corresponded color", async () => {
          const textCheckTags = page
            .getByTestId("Orientierungssatz")
            .locator("text-check")

          const allTextCheckTags = page.locator(`text-check`)

          for (
            let ignoredTextCheckTag = 0;
            ignoredTextCheckTag < (await allTextCheckTags.count());
            ignoredTextCheckTag++
          ) {
            await expect(textCheckTags.nth(ignoredTextCheckTag)).not.toHaveText(
              "",
            )

            const isTextCheckIgnored = await textCheckTags
              .nth(ignoredTextCheckTag)
              .getAttribute("ignored")

            let expectedRGBUnderlinesColor

            // eslint-disable-next-line playwright/no-conditional-in-test
            if (isTextCheckIgnored == "true") {
              expectedRGBUnderlinesColor = convertHexToRGB(
                textCheckUnderlinesColors.ignored,
              )
            } else {
              const typeAttr = await textCheckTags
                .nth(ignoredTextCheckTag)
                .getAttribute("type")
              // eslint-disable-next-line playwright/no-conditional-in-test
              const type = typeAttr ?? "uncategorized"
              expectedRGBUnderlinesColor = getTextCheckColorRGB(type)
            }

            await expect(textCheckTags.nth(ignoredTextCheckTag)).toHaveCSS(
              "border-bottom",
              "2px solid " +
                `rgb(${expectedRGBUnderlinesColor.red}, ${expectedRGBUnderlinesColor.green}, ${expectedRGBUnderlinesColor.blue})`,
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
          await headNoteEditor.click()

          await headNoteEditor.getByText("zB.").click()

          const textCheckLiteral = "zB."
          await expect(
            page.getByTestId("text-check-modal-word"),
          ).not.toHaveText("zb.")

          await page.getByTestId("suggestion-accept-button").click()
          await expect(page.getByTestId("text-check-modal")).toBeHidden()

          await expect(headNoteEditorTextArea).toHaveText(
            textWithErrors.text.replace(textCheckLiteral, "z. B."),
          )

          await expect(page.locator(`text-check[id='${8}']`)).not.toBeAttached()
        })

        await test.step("click on a selected suggestion, then click on a non-tag closes the text check modal", async () => {
          await page.locator("text-check").nth(0).click()
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
            page.locator(`text-check[id='${textCheckId}']`),
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

          const rgbColors = convertHexToRGB(
            textCheckUnderlinesColors.uncategorized,
          )

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toBeHidden()

          await expect(
            page.locator(`text-check[id='${textCheckId}']`),
          ).toHaveCSS(
            "border-bottom",
            "2px solid " +
              `rgb(${rgbColors.red}, ${rgbColors.green}, ${rgbColors.blue})`,
          )
        })
      },
    )
  },
)
