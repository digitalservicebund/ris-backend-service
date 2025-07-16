import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Collective Agreements (Tarifvertrag)",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-4578",
    },
    tag: ["@RISDEV-4578"],
  },
  () => {
    test("saving and exporting collective agreement", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      await navigateToCategories(page, prefilledDocumentUnit.documentNumber!)

      await test.step("check button is not displayed without labor court", async () => {
        await expect(
          page.getByRole("button", { name: "Tarifvertrag" }),
        ).toBeHidden()
      })

      await test.step("Select BAG (labor court)", async () => {
        await page.getByLabel("Gericht", { exact: true }).fill("BAG")
        await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
          "BAG",
        )
        await expect(
          page.locator("button").filter({ hasText: /^BAG$/ }),
        ).toBeVisible()
        await page.locator("button").filter({ hasText: /^BAG$/ }).click()
        await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
          "BAG",
        )

        await save(page)
      })

      await test.step("check button is displayed when field is empty", async () => {
        await expect(
          page.getByRole("button", { name: "Tarifvertrag" }),
        ).toBeVisible()
      })
      await page.getByRole("button", { name: "Tarifvertrag" }).click()

      await test.step("enter collective agreement", async () => {
        await page
          .getByTestId("Tarifvertrag_ListInputEdit")
          .fill("Stehende Bühnen")

        await page.getByLabel("Tarifvertrag übernehmen").click()

        await expect(
          page.getByTestId("ListInputDisplay_Tarifvertrag_Stehende Bühnen"),
        ).toHaveText("Stehende Bühnen")

        await save(page)
      })

      await test.step("XML preview should display 'Tarifvertrag' fields in 'paratrubriken'", async () => {
        await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)
        await page.getByText("XML Vorschau").click()
        const xmlPreview = page.getByTitle("XML Vorschau")
        const innerText = await xmlPreview.innerText()

        const regex =
          /<zuordnung>\s*\d*\s*<aspekt>Tarifvertrag<\/aspekt>\s*\d*\s*<begriff>Stehende Bühnen<\/begriff>\s*\d*\s*<\/zuordnung>/

        expect(innerText).toMatch(regex)
      })

      await test.step("Preview should show collective agreement", async () => {
        await navigateToPreview(page, prefilledDocumentUnit.documentNumber!)

        await expect(page.getByTestId("Tarifvertrag")).toHaveText(
          "Stehende Bühnen",
        )
      })
    })
  },
)
