import { expect } from "@playwright/test"
import { navigateToCategories } from "../utils/e2e-utils"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

test.describe(
  "text check service run",
  {
    tag: ["@RISDEV-9481"],
  },
  () => {
    ;[
      { status: "successful", result: { status: 200, body: "" } },
      { status: "failed", result: { status: 500, body: "" } },
    ].forEach(({ status, result }) => {
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
  },
)
