import { expect, Page } from "@playwright/test"
import { navigateToCategories } from "../utils/e2e-utils"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  getMarkId,
  ignoredColorStyle,
  textMistakeColor,
} from "~/e2e/caselaw/text-check/util"
import { generateString } from "~/test-helper/dataGenerators"

const wordWithTypo = generateString({ prefix: "etoe" }) // e.g. etoedsfjg

/**
 * Mockt den API-Aufruf für die Rechtschreibprüfung einer Kategorie,
 * klickt den entsprechenden Button und wartet auf das Ergebnis.
 * @param page Die aktuelle Playwright-Seite.
 * @param uuid Die UUID der Dokumenteneinheit.
 * @param category Die Kategorie ('tenor' oder 'reasons').
 */
async function triggerAndMockTextCheck(
  page: Page,
  uuid: string,
  category: string,
) {
  const categoryTestId = category === "tenor" ? "Tenor" : "Gründe"

  // Mock-Response ist für beide gleich (da nur das 'category' Feld in matches variiert)
  const mockResponse = {
    htmlText: `<p>Text mit Fehler: <text-check id="1" type="misspelling" ignored="false">${wordWithTypo}</text-check></p>`,
    matches: [
      {
        id: 1,
        word: wordWithTypo,
        shortMessage: "Rechtschreibfehler",
        category: category,
      },
    ],
  }

  const lock = Promise.withResolvers<void>()
  await page.route(
    `**/api/v1/caselaw/documentunits/${uuid}/text-check?category=${category}`,
    async (route) => {
      await lock.promise
      await route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify(mockResponse),
      })
    },
  )

  const container = page.getByTestId(category)

  await container.click()
  await container.getByRole("button", { name: "Rechtschreibprüfung" }).click()

  await expect(container.getByTestId("text-check-loading-status")).toHaveText(
    "Rechtschreibprüfung läuft",
  )
  lock.resolve()

  await page
    .getByTestId(categoryTestId)
    .locator("text-check")
    .first()
    .waitFor({ state: "visible" })
}

test.describe(
  "docunit ignore",
  {
    tag: ["@RISDEV-9120"],
  },
  () => {
    test.use({
      decisionToBeCreated: {
        longTexts: {
          tenor: `<p>Text mit Fehler: ${wordWithTypo}</p>`,
          reasons: `<p>Text mit Fehler: ${wordWithTypo}</p>`,
        },
      },
    })

    test(
      "text check, ignore in docunit effects all categories",
      {
        tag: ["@RISDEV-9234"],
      },
      async ({ page, decision }) => {
        await test.step("navigate to categories", async () => {
          await navigateToCategories(
            page,
            decision.createdDecision.documentNumber,
            {
              category: DocumentUnitCategoriesEnum.TEXTS,
            },
          )
        })

        await test.step("Trigger text check in tenor and reasons", async () => {
          await triggerAndMockTextCheck(
            page,
            decision.createdDecision.uuid,
            "tenor",
          )
          await triggerAndMockTextCheck(
            page,
            decision.createdDecision.uuid,
            "reasons",
          )
        })

        await test.step("ignoring a word marks match with blue in all categories", async () => {
          const textCheckTag = page
            .getByTestId("Gründe")
            .locator(".tiptap")
            .getByText(wordWithTypo)

          await textCheckTag.click()

          await expect(page.getByTestId("text-check-modal-word")).toBeVisible()
          await page.getByTestId("ignored-word-add-button").click()

          const textCheckId = await getMarkId(textCheckTag)

          const checks = page.locator(`text-check[id='${textCheckId}']`)

          await expect(checks).toHaveCount(2)

          const count = await checks.count()
          for (let i = 0; i < count; i++) {
            await expect(checks.nth(i)).toHaveCSS(
              "border-bottom",
              ignoredColorStyle,
            )
          }
        })

        await test.step("removing a word marks match with red in all categories", async () => {
          const textCheckTag = page
            .getByTestId("Gründe")
            .locator(".tiptap")
            .getByText(wordWithTypo)

          await textCheckTag.click()

          await expect(page.getByTestId("text-check-modal-word")).toBeVisible()

          const removeIgnoredWordButton = page.getByTestId(
            /^(ignored-word-remove-button|ignored-word-global-remove-button)$/,
          )
          await removeIgnoredWordButton.click()

          const textCheckId = await getMarkId(textCheckTag)

          const checks = page.locator(`text-check[id='${textCheckId}']`)

          await expect(checks).toHaveCount(2)

          const count = await checks.count()
          for (let i = 0; i < count; i++) {
            await expect(checks.nth(i)).toHaveCSS(
              "text-decoration-line",
              "underline",
            )
            await expect(checks.nth(i)).toHaveCSS(
              "text-decoration-style",
              "wavy",
            )
            await expect(checks.nth(i)).toHaveCSS(
              "text-decoration-color",
              textMistakeColor,
            )
          }
        })
      },
    )
  },
)
