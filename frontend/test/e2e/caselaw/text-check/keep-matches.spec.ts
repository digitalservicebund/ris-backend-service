import { expect } from "@playwright/test"
import { navigateToCategories } from "../utils/e2e-utils"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

const ERROR_WORD = "Felelr"
test.describe(
  "text check store",
  {
    tag: ["@RISDEV-9022"],
  },
  () => {
    test.use({
      decisionToBeCreated: {
        shortTexts: { headnote: `<p>Text mit ${ERROR_WORD}</p>` },
      },
    })
    test(
      "text check, preserves matches after navigation",
      {
        tag: ["@RISDEV-9234"],
      },
      async ({ page, decision }) => {
        const mockResponse = {
          htmlText: `<p>Text mit <text-check id="1" type="misspelling" ignored="false">${ERROR_WORD}</text-check></p>`,
          matches: [
            {
              id: 1,
              word: ERROR_WORD,
              category: "headnote",
            },
          ],
        }
        await test.step("navigate to headnote (Orientierungssatz) in categories", async () => {
          await navigateToCategories(
            page,
            decision.createdDecision.documentNumber,
            {
              category: DocumentUnitCategoriesEnum.TEXTS,
            },
          )
        })

        await test.step("Trigger text check in headnote (Orientierungssatz)", async () => {
          const headNoteEditor = page.getByTestId("Orientierungssatz")
          const lock = Promise.withResolvers<void>()

          await page.route(
            `**/api/v1/caselaw/documentunits/${decision.createdDecision.uuid}/text-check?category=headnote`,
            async (route) => {
              await lock.promise
              await route.fulfill({
                status: 200,
                contentType: "application/json",
                body: JSON.stringify(mockResponse),
              })
            },
          )

          await headNoteEditor.locator("div").click()

          await page
            .getByLabel("Orientierungssatz Button")
            .getByRole("button", { name: "Rechtschreibprüfung" })
            .click()

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toHaveText("Rechtschreibprüfung läuft")

          lock.resolve()

          await headNoteEditor
            .locator("text-check")
            .first()
            .waitFor({ state: "visible" })

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toBeHidden()
        })

        await test.step("match should still appear after navigation", async () => {
          await page.getByRole("link", { name: "Dokumente" }).click()

          await expect(
            page.getByRole("heading", { name: "Dokumente" }),
          ).toBeVisible()

          await page
            .getByRole("link", { name: "Rubriken", exact: true })
            .click()

          await expect(
            page.getByTestId("Orientierungssatz").locator("text-check").first(),
          ).toBeVisible()
        })
      },
    )
  },
)
