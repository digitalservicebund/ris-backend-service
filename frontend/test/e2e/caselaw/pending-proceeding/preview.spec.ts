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
        page.getByText("DokumenttypAnhängiges Verfahren", { exact: true }),
      ).toBeVisible()
      await expect(page.getByText("ErledigungJa")).toBeVisible()
      await expect(
        page.getByText("Erledigungsmitteilung06.06.2025", { exact: true }),
      ).toBeVisible()
      await expect(
        page.getByText("GerichtsbarkeitFinanzgerichtsbarkeit", { exact: true }),
      ).toBeVisible()
      await expect(page.getByText("RegionDEU", { exact: true })).toBeVisible()
      await expect(
        page.getByText(
          "RechtsfrageGewerbesteuerpflicht des Bäderbetriebs einer Gemeinde als Betrieb gewerblicher Art",
        ),
      ).toBeVisible()
      await expect(
        page.getByText("RechtsmittelzulassungZulassung durch BFH"),
      ).toBeVisible()
      await expect(page.getByText("RechtsmittelführerVerwaltung")).toBeVisible()
      await expect(
        page.getByText(
          "ErledigungsvermerkVerfahren ist erledigt durch: Zurücknahme der Klage. Das erstinstanzliche Urteil ist gegenstandslos",
        ),
      ).toBeVisible()
    },
  )
})
