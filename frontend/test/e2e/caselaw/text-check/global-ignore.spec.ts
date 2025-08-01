import { expect, Page } from "@playwright/test"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  clearTextField,
  navigateToCategories,
  navigateToHandover,
} from "~/e2e/caselaw/utils/e2e-utils"
import { generateString } from "~/test-helper/dataGenerators"

// eslint-disable-next-line playwright/no-skipped-test
test.skip(
  ({ baseURL }) => baseURL === "http://127.0.0.1",
  "Skipping this test on local execution, as there is no languagetool running",
)

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
          await page
            .getByLabel("Orientierungssatz Button")
            .getByRole("button", { name: "Rechtschreibprüfung" })
            .click()
        })

        await test.step("jDV globally ignored word can't be removed", async () => {
          await headNoteEditor
            .locator("text-check")
            .first()
            .waitFor({ state: "visible" })
          await headNoteEditor.locator("text-check").first().click()
          await expect(page.getByTestId("text-check-modal")).toBeVisible()

          await expect(
            page.getByText("Aus globalem Wörterbuch entfernen"),
          ).toBeHidden()
          await expect(
            page.getByText("Zu globalem Wörterbuch hinzufügen"),
          ).toBeHidden()
          await expect(
            page.getByText("Von jDV ignoriert"),
            "You might need to migrate the global dictionary",
          ).toBeVisible()
        })

        const wordWithTypo = generateString({ prefix: "etoe" }) // e.g. etoedsfjg

        await test.step("add text in headnote (Orientierungssatz)", async () => {
          await headNoteEditorTextArea.fill("")
          await headNoteEditorTextArea.fill("Text mit Fehler: " + wordWithTypo)
        })

        await test.step("add word to global ignore", async () => {
          await checkTextOfHeadnote(page)
          await headNoteEditor
            .locator("text-check")
            .first()
            .waitFor({ state: "visible" })
          await headNoteEditor.locator("text-check").first().click()
          await expect(page.getByTestId("text-check-modal")).toBeVisible()

          await page.getByText("Zum globalen Wörterbuch hinzufügen").click()
        })

        // eslint-disable-next-line playwright/no-skipped-test
        await test.step.skip(
          "make sure the globally ignored word is exported with <noindex> tags",
          async () => {
            await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
            await expect(page.getByText("XML Vorschau")).toBeVisible()
            await page.getByText("XML Vorschau").click()

            await expect(
              page.getByText("<noindex>" + wordWithTypo + "</noindex>"),
            ).toBeVisible()
          },
        )

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
          await clearTextField(page, secondHeadNoteEditorFieldArea)
          await secondHeadNoteEditor.locator("div").fill(wordWithTypo)
        })

        await test.step("text check should show that word is globally ignored", async () => {
          await checkTextOfHeadnote(pageWithBghUser)
          await secondHeadNoteEditor
            .locator("text-check")
            .first()
            .waitFor({ state: "visible" })
          await secondHeadNoteEditor.locator("text-check").first().click()
          await expect(
            pageWithBghUser.getByText("Aus globalem Wörterbuch entfernen"),
          ).toBeVisible()
        })

        await test.step("remove word from global ignore", async () => {
          await pageWithBghUser
            .getByText("Aus globalem Wörterbuch entfernen")
            .click()
        })

        await test.step("check text again in doc unit A and expect word not to be ignored globally", async () => {
          await navigateToCategories(
            page,
            prefilledDocumentUnit.documentNumber,
            { category: DocumentUnitCategoriesEnum.TEXTS },
          )
          await expect(page.getByTestId("headnote")).toBeVisible()
          await headNoteEditorTextArea.click()
          await checkTextOfHeadnote(page)
          await headNoteEditor
            .locator("text-check")
            .first()
            .waitFor({ state: "visible" })
          await headNoteEditor.locator("text-check").first().click()
          await expect(page.getByTestId("text-check-modal")).toBeVisible()
          await expect(
            page.getByText("Zum globalen Wörterbuch hinzufügen"),
          ).toBeVisible()
        })
      },
    )
  },
)

async function checkTextOfHeadnote(page: Page) {
  await page
    .getByLabel("Orientierungssatz Button")
    .getByRole("button", { name: "Rechtschreibprüfung" })
    .click()
  await expect(page.getByText("Rechtschreibprüfung läuft")).toBeVisible()
  await expect(page.getByText("Rechtschreibprüfung läuft")).not.toBeVisible({
    timeout: 15000,
  })
}
