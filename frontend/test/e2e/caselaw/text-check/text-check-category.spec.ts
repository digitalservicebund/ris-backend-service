import { expect } from "@playwright/test"
import { navigateToCategories } from "../e2e-utils"
import { caselawTest as test } from "../fixtures"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import { convertHexToRGB } from "~/test-helper/coloursUtil"

const textWithErrors =
  "LanguageTool ist Ihr intelligenter Schreibassistent für alle gängigen Browser und Textverarbeitungsprogramme. Schreiben sie in diesem Textfeld oder fügen Sie einen Text ein. Rechtschreibfehler werden rot markirt, Grammatikfehler werden gelb hervorgehoben und Stilfehler werden, anders wie die anderen Fehler, blau unterstrichen. Wussten Sie, dass Synonyme per Doppelklick auf ein Wort aufgerufen werden können? Nutzen Sie LanguageTool in allen Lebenslagen, zB. wenn Sie am Freitag, dem 13. Mai 2022, einen Basketballkorb in 10 Fuß Höhe montieren möchten."

test.describe(
  "check text category",
  {
    annotation: {
      description: "https://digitalservicebund.atlassian.net/browse/RISDEV-254",
      type: "epic",
    },
  },
  () => {
    test(
      "clicking on text check save document and returns matches",
      {
        annotation: {
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-6154",
          type: "story",
        },
      },
      async ({ page, prefilledDocumentUnit }) => {
        await test.step("navigate to other headnote (Orientierungssatz) in categories", async () => {
          await navigateToCategories(
            page,
            prefilledDocumentUnit.documentNumber,
            { category: DocumentUnitCategoriesEnum.TEXTS },
          )
        })

        await test.step("replace text in otherHeadNote (Orientierungssatz)", async () => {
          const otherHeadNoteEditor = page.getByTestId("Orientierungssatz")
          await otherHeadNoteEditor.locator("div").fill("")

          await otherHeadNoteEditor.locator("div").fill(textWithErrors)
          await expect(otherHeadNoteEditor.locator("div")).toHaveText(
            textWithErrors,
          )
        })

        await test.step("trigger category text check underlines matches with corresponded color", async () => {
          const expectedTextCheckCount = 6
          await page
            .getByLabel("Orientierungssatz Button")
            .getByRole("button", { name: "Rechtschreibprüfung" })
            .click()

          const textCheckTags = page.locator("text-check")
          await expect(textCheckTags).toHaveCount(expectedTextCheckCount)

          for (let i = 0; i < expectedTextCheckCount; i++) {
            await expect(textCheckTags.nth(i)).not.toHaveText("")

            const type =
              // eslint-disable-next-line playwright/no-conditional-in-test
              (await textCheckTags.nth(i).getAttribute("type")) ??
              "uncategorized"

            const uncategorized = "#e86a69"
            const expectedBorder =
              // eslint-disable-next-line playwright/no-conditional-in-test
              {
                uncategorized: uncategorized,
                style: "#9d8eff",
                grammar: "#eeb55c",
                typographical: "#eeb55c",
              }[type] || uncategorized

            const rgbColors = convertHexToRGB(expectedBorder)

            await expect(textCheckTags.nth(i)).toHaveCSS(
              "border-bottom",
              "2px solid " +
                `rgb(${rgbColors.red}, ${rgbColors.green}, ${rgbColors.blue})`,
            )
          }
        })

        await test.step("clicking on text check tags opens corresponded modal", async () => {
          const expectedTextCheckCount = 6

          const textCheckTags = page.locator("text-check")
          await expect(textCheckTags).toHaveCount(expectedTextCheckCount)

          for (
            let textCheckIndex = 0;
            textCheckIndex < expectedTextCheckCount;
            textCheckIndex++
          ) {
            const textCheckTag = textCheckTags.nth(textCheckIndex)

            const textContent = await textCheckTag.evaluate(
              (el) => el.textContent,
            )

            await textCheckTags.nth(textCheckIndex).click()
            await expect(page.getByTestId("text-check-modal")).toBeVisible()
            await expect(
              page.getByTestId("text-check-modal-word"),
            ).toContainText(textContent!)
            await expect(
              page.getByTestId("text-check-modal-word"),
            ).not.toHaveText("")
          }
        })

        await test.step("accept a selected suggestion replaces it in text", async () => {
          const otherHeadNoteEditor = page.getByTestId("Orientierungssatz")
          await otherHeadNoteEditor.click()

          await otherHeadNoteEditor.getByText("zB.").click()

          const textCheckLiteral = "zB."
          await expect(
            page.getByTestId("text-check-modal-word"),
          ).not.toHaveText("zb.")

          await page.getByTestId("suggestion-accept-button").click()
          await expect(page.getByTestId("text-check-modal")).toBeHidden()

          await expect(otherHeadNoteEditor.locator("div")).toHaveText(
            textWithErrors.replace(textCheckLiteral, "z. B."),
          )
        })

        await test.step("click on a selected suggestion, then click on a non-tag closes the text check modal.", async () => {
          await page.locator("text-check").nth(0).click()
          await expect(page.getByTestId("text-check-modal-word")).toBeVisible()
          await page.getByText("Lebenslagen,").click()
          await expect(page.getByTestId("text-check-modal-word")).toBeHidden()
        })
      },
    )
  },
)
