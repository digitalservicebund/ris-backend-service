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
        "https://digitalservicebund.atlassian.net/browse/RISDEV-6687",
    },
    tag: ["@RISDEV-4578", "@RISDEV-6687"],
  },
  () => {
    test("saving and exporting collective agreement", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      await navigateToCategories(page, prefilledDocumentUnit.documentNumber!)

      await test.step("check button is displayed when field is empty", async () => {
        await expect(
          page.getByRole("button", { name: "Tarifvertrag" }),
        ).toBeVisible()
      })
      await page.getByRole("button", { name: "Tarifvertrag" }).click()

      await test.step("enter collective agreement", async () => {
        const section = page.getByLabel("Tarifvertrag", { exact: true })
        await section
          .getByRole("textbox", { name: "Bezeichnung des Tarifvertrags" })
          .fill("Stehende Bühnen")
        await section.getByRole("textbox", { name: "Datum" }).fill("12.2001")
        await section.getByRole("textbox", { name: "Tarifnorm" }).fill("§ 23")
        await section.getByRole("textbox", { name: "Branche" }).fill("Bü")
        await section.getByText("Bühne, Theater, Orchester").click()

        await section
          .getByRole("button", { name: "Tarifvertrag speichern" })
          .click()

        await expect(
          section.getByText(
            "Stehende Bühnen, 12.2001, § 23 (Bühne, Theater, Orchester)",
          ),
        ).toBeVisible()

        await save(page)
      })

      await test.step("XML preview should display 'Tarifvertrag' fields in 'paratrubriken'", async () => {
        await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)
        await page.getByText("XML Vorschau").click()
        const xmlPreview = page.getByTitle("XML Vorschau")
        const innerText = await xmlPreview.innerText()

        const regex =
          /<zuordnung>\s*\d*\s*<aspekt>Tarifvertrag<\/aspekt>\s*\d*\s*<begriff>bezeichnung=Stehende Bühnen|datum=12.2001|tarifnorm=§ 23|branche=Bühne, Theater, Orchester<\/begriff>\s*\d*\s*<\/zuordnung>/

        expect(innerText).toMatch(regex)
      })

      await test.step("Preview should show collective agreement", async () => {
        await navigateToPreview(page, prefilledDocumentUnit.documentNumber!)

        await expect(page.getByTestId("Tarifvertrag")).toHaveText(
          "Stehende Bühnen, 12.2001, § 23 (Bühne, Theater, Orchester)",
        )
      })
    })
  },
)
