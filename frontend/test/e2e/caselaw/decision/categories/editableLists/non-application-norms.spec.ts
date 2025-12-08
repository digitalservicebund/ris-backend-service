import { expect } from "@playwright/test"
import SingleNorm from "@/domain/singleNorm"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  fillNormInputs,
  navigateToCategories,
  save,
  selectCourt,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Nichtanwendungsgesetz",
  {
    tag: ["@RISDEV-9960"],
  },
  () => {
    test("Nichtanwendungsgesetz", async ({ page, prefilledDocumentUnit }) => {
      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      await test.step("wird nur bei Finanzgerichtsbarkeit angezeigt", async () => {
        await expect(
          page.getByRole("button", { name: "Nichtanwendungsgesetz" }),
        ).toBeHidden()

        await selectCourt(page, "BFH")
        await expect(
          page.getByRole("button", { name: "Nichtanwendungsgesetz" }),
        ).toBeVisible()
      })

      await test.step("Nichtanwendungsgesetz hinzufügen, ändern und löschen", async () => {
        await page
          .getByRole("button", { name: "Nichtanwendungsgesetz" })
          .click()

        const container = page.getByLabel("Nichtanwendungsgesetz")

        // add entry
        await fillNormInputs(page, {
          normAbbreviation: "PBefG",
          singleNorms: [
            { singleNorm: "§ 12", dateOfVersion: "12.12.2022" } as SingleNorm,
          ],
        })

        await container.getByLabel("Norm speichern").click()
        await expect(
          container.getByText("PBefG, § 12, 12.12.2022"),
        ).toBeVisible()

        // edit entry
        await container.getByTestId("list-entry-0").click()
        await fillNormInputs(page, {
          normAbbreviation: "PBefGZustV HE",
        })

        await container.getByLabel("Norm speichern").click()
        await expect(
          container.getByText("PBefGZustV HE, § 12, 12.12.2022"),
        ).toBeVisible()

        // the second list item is a default list entry
        await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)

        // add second entry
        await fillNormInputs(page, {
          normAbbreviation: "LehrBiGDV HE",
        })
        await container.getByLabel("Norm speichern").click()

        // the third list item is a default list entry
        await expect(container.getByLabel("Listen Eintrag")).toHaveCount(3)
        await save(page)
        await page.reload()
        // the default list entry is not shown on reload page
        await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)
        await container.getByLabel("Weitere Angabe").isVisible()

        const listEntries = container.getByLabel("Listen Eintrag")
        await expect(listEntries).toHaveCount(2)

        await container.getByTestId("list-entry-1").click()
        await container.getByLabel("Eintrag löschen").click()
        // the default list entry is not shown on delete item
        await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)
        await container.getByLabel("Weitere Angabe").isVisible()
      })
    })
  },
)
