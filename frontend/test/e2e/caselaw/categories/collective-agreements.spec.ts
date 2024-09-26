import { expect } from "@playwright/test"
import {
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"

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

      await test.step("check button is displayed when field is empty", async () => {
        await expect(
          page.getByRole("button", { name: "Tarifvertrag" }),
        ).toBeVisible()
      })
      await page.getByRole("button", { name: "Tarifvertrag" }).click()

      await test.step("enter collective agreement", async () => {
        await page.getByLabel("Tarifvertrag").fill("Stehende Bühnen")
        await page.keyboard.press("Enter")
        await expect(
          page
            .getByTestId("chips-input_collectiveAgreements")
            .getByTestId("chip-value"),
        ).toHaveText("Stehende Bühnen")

        await save(page)
      })

      await test.step("XML preview should display 'Tarifvertrag' fields in 'paratrubriken'", async () => {
        await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)
        await page.getByText("XML Vorschau").click()
        const xmlPreview = page.getByTitle("XML Vorschau")
        const innerText = await xmlPreview.innerText()
        expect(innerText).toBeDefined()
        expect(innerText).toContain(
          "<paratrubriken>\n" +
            "14\n" +
            "        <zuordnung>\n" +
            "15\n" +
            "            <aspekt>Tarifvertrag</aspekt>\n" +
            "16\n" +
            "            <begriff>Stehende Bühnen</begriff>\n" +
            "17\n" +
            "        </zuordnung>\n",
        )
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
