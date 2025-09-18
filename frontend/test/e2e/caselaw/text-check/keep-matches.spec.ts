import { expect } from "@playwright/test"
import { navigateToCategories } from "../utils/e2e-utils"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

test.describe(
  "matches store",
  {
    tag: ["@RISDEV-254"],
  },
  () => {
    test.use({
      decisionToBeCreated: {
        shortTexts: { headnote: "Text mit Felelr" },
      },
    })

    test(
      "clicking on text check button, save document and returns matches",
      {
        tag: ["@RISDEV-9234"],
      },
      async ({ page, decision }) => {
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
          await expect(
            page.getByLabel("Orientierungssatz Button"),
          ).toBeVisible()

          await page
            .getByLabel("Orientierungssatz Button")
            .getByRole("button", { name: "Rechtschreibprüfung" })
            .click()

          await expect(
            page.getByTestId("text-check-loading-status"),
          ).toHaveText("Rechtschreibprüfung läuft")

          await page
            .getByTestId("Orientierungssatz")
            .locator("text-check")
            .first()
            .waitFor({ state: "visible" })
        })

        await test.step("match modal should still appear after navigation", async () => {
          await page
            .getByTestId("caselaw-documentUnit-documentNumber-handover")
            .click()

          await expect(
            page.getByRole("heading", { name: "Rechtschreibprüfung" }),
          ).toBeVisible()

          await page
            .getByTestId("side-toggle-navigation")
            .getByRole("link", { name: "Rubriken" })
            .click()

          await expect(
            page.getByTestId("Orientierungssatz").locator("text-check").first(),
          ).toBeVisible()

          await page.locator("text-check").first().click()
          await expect(page.getByTestId("text-check-modal-word")).toBeVisible()
        })
      },
    )
  },
)
