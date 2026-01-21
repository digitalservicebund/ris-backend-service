import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Dimissal types and grounds (Kündigungsarten / Kündigungsgründe)",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-4577",
    },
    tag: ["@RISDEV-4577"],
  },
  () => {
    test("saving and exporting dismissal attributes", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      await navigateToCategories(page, prefilledDocumentUnit.documentNumber!)

      await test.step("check button is not displayed without labor court", async () => {
        await expect(
          page.getByRole("button", { name: "Kündigung" }),
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
          page.getByRole("button", { name: "Kündigung" }),
        ).toBeVisible()
      })

      await page.getByRole("button", { name: "Kündigung" }).click()

      await test.step("enter dismissal type", async () => {
        await page
          .getByLabel("Kündigungsarten")
          .getByRole("textbox")
          .fill("Betriebsbedingte Kündigung")
        await page.keyboard.press("Enter")
        await expect(
          page.getByLabel("Kündigungsarten").getByRole("listitem"),
        ).toHaveText("Betriebsbedingte Kündigung")
      })

      await test.step("enter dismissal ground", async () => {
        await page
          .getByLabel("Kündigungsgründe")
          .getByRole("textbox")
          .fill("Einführung neuer Technologien")
        await page.keyboard.press("Enter")
        await expect(
          page.getByLabel("Kündigungsgründe").getByRole("listitem"),
        ).toHaveText("Einführung neuer Technologien")
      })

      await save(page)

      await test.step("XML preview should display 'Kündigungs' fields in 'paratrubriken'", async () => {
        await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)
        await page.getByText("XML Vorschau").click()
        const xmlPreview = page.getByTitle("XML Vorschau")
        const innerText = await xmlPreview.innerText()

        const regex =
          /<zuordnung>\s*\d*\s*<aspekt>Kündigungsgrund<\/aspekt>\s*\d*\s*<begriff>Betriebsbedingte Kündigung<\/begriff>\s*\d*\s*<\/zuordnung>\s*\d*\s*<zuordnung>\s*\d*\s*<aspekt>Kündigungsgrund<\/aspekt>\s*\d*\s*<begriff>Einführung neuer Technologien<\/begriff>\s*\d*\s*<\/zuordnung>/
        expect(innerText).toMatch(regex)
      })

      await test.step("Preview should show dismissal fields", async () => {
        await navigateToPreview(page, prefilledDocumentUnit.documentNumber!)

        await expect(page.getByTestId("Kündigungsarten")).toHaveText(
          "Betriebsbedingte Kündigung",
        )
        await expect(page.getByTestId("Kündigungsgründe")).toHaveText(
          "Einführung neuer Technologien",
        )
      })
    })
  },
)
