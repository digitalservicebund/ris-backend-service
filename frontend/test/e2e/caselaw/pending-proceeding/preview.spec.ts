import { expect } from "@playwright/test"
import { caselawTest as test } from "../fixtures"
import { navigateToPreview } from "~/e2e/caselaw/e2e-utils"

test.describe("preview pending proceeding", () => {
  test(
    "display preview, check that fields are filled with values from categories",
    {
      annotation: {
        type: "story",
        description:
          "https://digitalservicebund.atlassian.net/browse/RISDEV-6109",
      },
    },
    async ({ page }) => {
      await navigateToPreview(page, "YYTestDoc0017", {
        type: "pending-proceeding",
      })

      const fileNumber = "I R 20000/34"
      await expect(page.getByText("GerichtBFH")).toBeVisible()
      await expect(page.getByText(fileNumber)).toBeVisible()
      await expect(
        page.getByText("Mitteilungsdatum24.02.2025", { exact: true }),
      ).toBeVisible()
      await expect(
        page.getByText(
          "Verfahren ist erledigt durch: Zurücknahme der Klage. Das erstinstanzliche Urteil ist gegenstandslos",
        ),
      ).toBeVisible()
      await expect(
        page.getByText("Anhängiges Verfahren", { exact: true }),
      ).toBeVisible()
      await expect(page.getByText("ErledigungJa")).toBeVisible()
      await expect(page.getByText("Zulassung durch BFH")).toBeVisible()
      await expect(page.getByText("RechtsmittelführerVerwaltung")).toBeVisible()
    },
  )
})
