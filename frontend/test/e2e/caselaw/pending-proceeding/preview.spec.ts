import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { navigateToPreview } from "~/e2e/caselaw/utils/e2e-utils"

test.describe("preview pending proceeding", () => {
  test.use({
    pendingProceedingsToBeCreated: [
      [
        {
          coreData: {
            fileNumbers: ["I R 20000/34"],
            court: {
              label: "BFH",
            },
            isResolved: true,
            decisionDate: "2025-02-24",
            resolutionDate: "2025-06-06",
            deviatingDocumentNumbers: ["STRE123456789"],
          },
          shortTexts: {
            headline: "Anhängiges Verfahren am BFH ",
            resolutionNote:
              "Verfahren ist erledigt durch: Zurücknahme der Klage. Das erstinstanzliche Urteil ist gegenstandslos",
            legalIssue:
              "Gewerbesteuerpflicht des Bäderbetriebs einer Gemeinde als Betrieb gewerblicher Art",
            admissionOfAppeal: "Zulassung durch BFH",
            appellant: "Verwaltung",
          },
        },
      ],
      { scope: "test" },
    ],
  })
  test(
    "display preview, check that fields are filled with values from categories",
    {
      annotation: {
        type: "story",
        description:
          "https://digitalservicebund.atlassian.net/browse/RISDEV-6109",
      },
    },
    async ({ pageWithBfhUser, pendingProceedings }) => {
      const { createdPendingProceedings } = pendingProceedings
      await navigateToPreview(
        pageWithBfhUser,
        createdPendingProceedings[0].documentNumber,
        {
          type: "pending-proceeding",
        },
      )

      const fileNumber = "I R 20000/34"
      await expect(pageWithBfhUser.getByText("GerichtBFH")).toBeVisible()
      await expect(pageWithBfhUser.getByText(fileNumber)).toBeVisible()
      await expect(
        pageWithBfhUser.getByText("Mitteilungsdatum24.02.2025", {
          exact: true,
        }),
      ).toBeVisible()
      await expect(
        pageWithBfhUser.getByText("DokumenttypAnhängiges Verfahren", {
          exact: true,
        }),
      ).toBeVisible()
      await expect(
        pageWithBfhUser.getByText("Abweichende DokumentnummerSTRE123456789", {
          exact: true,
        }),
      ).toBeVisible()
      await expect(pageWithBfhUser.getByText("ErledigungJa")).toBeVisible()
      await expect(
        pageWithBfhUser.getByText("Erledigungsmitteilung06.06.2025", {
          exact: true,
        }),
      ).toBeVisible()
      await expect(
        pageWithBfhUser.getByText("GerichtsbarkeitFinanzgerichtsbarkeit", {
          exact: true,
        }),
      ).toBeVisible()
      await expect(
        pageWithBfhUser.getByText("RegionDEU, BY", { exact: true }),
      ).toBeVisible()
      await expect(
        pageWithBfhUser.getByText(
          "RechtsfrageGewerbesteuerpflicht des Bäderbetriebs einer Gemeinde als Betrieb gewerblicher Art",
        ),
      ).toBeVisible()
      await expect(
        pageWithBfhUser.getByText("RechtsmittelzulassungZulassung durch BFH"),
      ).toBeVisible()
      await expect(
        pageWithBfhUser.getByText("RechtsmittelführerVerwaltung"),
      ).toBeVisible()
      await expect(
        pageWithBfhUser.getByText(
          "ErledigungsvermerkVerfahren ist erledigt durch: Zurücknahme der Klage. Das erstinstanzliche Urteil ist gegenstandslos",
        ),
      ).toBeVisible()
    },
  )
})
