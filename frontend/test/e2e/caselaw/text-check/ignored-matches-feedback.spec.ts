import { expect } from "@playwright/test"
import { navigateToCategories } from "../utils/e2e-utils"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  defaultText,
  getMarkId,
  ignoredColorStyle,
  textMistakeColor,
  triggerCheckCategory,
} from "~/e2e/caselaw/text-check/util"

// eslint-disable-next-line playwright/no-skipped-test
test.skip(
  ({ browserName }) => browserName !== "chromium",
  "Skipping firefox flaky test",
)

test.describe(
  "ignored matches feedback",
  {
    tag: ["@RISDEV-9120"],
  },

  () => {
    test.use({
      decisionToBeCreated: {
        longTexts: {
          tenor: `<p>${defaultText}</p>`,
          reasons: `<p>${defaultText}</p>`,
        },
      },
    })

    test(
      "text check, ignore effects all categories",
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
          await triggerCheckCategory(page, "Tenor")
          await triggerCheckCategory(page, "Gründe")
        })

        await test.step("ignoring a word marks match with blue in all categories", async () => {
          const textCheckTag = page
            .locator(`text-check[ignored='false']`)
            .first()

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
            .locator(`text-check[ignored='true']`)
            .first()

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
