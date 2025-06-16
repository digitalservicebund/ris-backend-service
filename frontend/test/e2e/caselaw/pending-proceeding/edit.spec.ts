import { expect } from "@playwright/test"
import { caselawTest as test } from "../fixtures"
import { navigateToCategories, save } from "~/e2e/caselaw/e2e-utils"

test.describe("edit pending proceeding", () => {
  test(
    "user can edit and save pending proceeding documents core data",
    { tag: ["@RISDEV-7774"] },
    async ({ page }) => {
      await navigateToCategories(page, "YYTestDoc0018", {
        type: "pending-proceeding",
      })

      // Gericht
      await test.step("court with existing value BFH can be changed to BGH", async () => {
        const court = page.getByLabel("Gericht", { exact: true })
        await expect(court).toHaveValue("BFH")
        await court.fill("BGH")
        await expect(page.getByTestId("combobox-spinner")).toBeHidden()
        await expect(court).toHaveValue("BGH")
        await expect(page.getByText("BGH")).toBeVisible()
        await page.getByText("BGH").click()
        await expect(court).toHaveValue("BGH")
      })

      // Aktenzeichen
      await test.step("existing fileNumber is displayed, fileNumbers can be added", async () => {
        await expect(
          page.getByTestId("chips-input-wrapper_fileNumber").getByText("123"),
        ).toBeVisible()
        const fileNumber = page.getByLabel("Aktenzeichen", { exact: true })
        await fileNumber.fill("abc")
        await page.keyboard.press("Enter")
        await expect(
          page.getByTestId("chips-input-wrapper_fileNumber").getByText("abc"),
        ).toBeVisible()
      })

      //Mitteilungsdatum
      await test.step("decision date can be edited", async () => {
        await expect(page.getByText("Mitteilungsdatum *")).toBeVisible()
        const date = page.getByLabel("Entscheidungsdatum", { exact: true })
        await expect(date).toBeVisible()
        await expect(date).toHaveValue("24.02.2025")
      })

      // Spruchkörper
      await test.step("appraisal body can be edited", async () => {
        const appraisalBody = page.getByLabel("Spruchkörper", { exact: true })
        await appraisalBody.fill("abc")
        await expect(appraisalBody).toHaveValue("abc")
      })

      // Erledigungsmitteilung
      await test.step("'Erledigungsmitteilung' can be edited, automatically sets 'Erledigt' checkbox", async () => {
        const checkbox = page.getByLabel("Erledigt")
        await expect(checkbox).not.toBeChecked()
        const date = page.getByLabel("Erledigungsmitteilung", { exact: true })
        await expect(date).toBeVisible()
        await date.fill("16.06.2025")
        await expect(date).toHaveValue("16.06.2025")
        await expect(checkbox).toBeChecked()
      })

      // Gerichtsbarkeit Finanzgerichtsbarkeit
      await test.step("'Gerichtsbarkeit' is readonly", async () => {
        const jurisdictionType = page.getByLabel("Gerichtsbarkeit", {
          exact: true,
        })
        await expect(jurisdictionType).toHaveValue(
          "Ordentliche Gerichtsbarkeit",
        )
        await expect(jurisdictionType).toHaveAttribute("readonly", "")
      })

      // Gerichtsbarkeit Finanzgerichtsbarkeit
      await test.step("'Region' is readonly", async () => {
        const jurisdictionType = page.getByLabel("Region", {
          exact: true,
        })
        await expect(jurisdictionType).toHaveValue("DEU")
        await expect(jurisdictionType).toHaveAttribute("readonly", "")
      })

      await save(page)
    },
  )
})
