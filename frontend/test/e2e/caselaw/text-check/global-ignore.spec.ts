import { expect, Page } from "@playwright/test"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import {
  navigateToCategories,
  navigateToHandover,
} from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { generateString } from "~/test-helper/dataGenerators"

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

        await test.step("add jDV globally ignored word in headnote (Orientierungssatz)", async () => {
          await headNoteEditor.locator("div").fill("")
          await headNoteEditor.locator("div").fill("VDberbglStPr") // this is ignored by jDV
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
          await headNoteEditor.locator("div").fill("")
          await headNoteEditor
            .locator("div")
            .fill("Text mit Fehler: " + wordWithTypo)
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

        await test.step("make sure the globally ignored word is exported with <noindex> tags", async () => {
          await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
          await expect(page.getByText("XML Vorschau")).toBeVisible()
          await page.getByText("XML Vorschau").click()

          await expect(
            page.getByText("<noindex>" + wordWithTypo + "</noindex>"),
          ).toBeVisible()
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
        await test.step("type same text in headnote (Orientierungssatz)", async () => {
          await secondHeadNoteEditor.locator("div").fill("")
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
          await headNoteEditor.locator("div").click()
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
