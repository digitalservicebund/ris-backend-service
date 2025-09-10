import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  expectHistoryLogRow,
  navigateToManagementData,
  navigateToPublication,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Publish and withdraw pending proceeding",
  {
    tag: ["@RISDEV-6639"],
  },
  () => {
    test.use({
      pendingProceedingsToBeCreated: [
        [
          {
            coreData: {
              court: { label: "AG Aachen" },
              decisionDate: "2023-01-01",
              documentType: {
                label: "Anhängiges Verfahren",
                jurisShortcut: "Anh",
              },
            },
            shortTexts: {
              legalIssue: "legalIssue",
            },
          },
          {
            coreData: {
              court: { label: "BVerwG" },
              decisionDate: "2025-05-22",
              documentType: {
                label: "Anhängiges Verfahren",
                jurisShortcut: "Anh",
              },
            },
          },
        ],
        { scope: "test" },
      ],
    })
    test(
      "Anhängiges Verfahren kann veröffentlicht und zurückgezogen werden",
      {
        tag: ["@RISDEV-7896", "@RISDEV-8460"],
      },
      async ({ page, prefilledPendingProceeding }) => {
        await navigateToPublication(
          page,
          prefilledPendingProceeding.documentNumber,
          {
            type: "pending-proceeding",
          },
        )

        await test.step("Anzeige eines unveröffentlichten Verfahrens ohne Fehler", async () => {
          await expect(page.getByTitle("LDML Vorschau")).toBeVisible()
          await expect(
            page.getByRole("heading", {
              name: "Aktueller Status Portal",
            }),
          ).toBeVisible()
          await expect(
            page.getByTestId("portal-publication-status-badge"),
          ).toBeVisible()
          await expect(
            page.getByTestId("portal-publication-status-badge"),
          ).toHaveText("Unveröffentlicht")
          await expect(
            page.getByRole("button", { name: "Veröffentlichen" }),
          ).toBeVisible()
        })

        await test.step("Erfolgreiches Veröffentlichen ändert den Status", async () => {
          await page.getByRole("button", { name: "Veröffentlichen" }).click()
          await expect(
            page.getByTestId("portal-publication-status-badge"),
          ).toHaveText("Veröffentlicht")
          await expect(
            page.getByRole("button", { name: "Zurückziehen" }),
          ).toBeVisible()
        })

        await test.step("Erfolgreiches Zurückziehen ändert den Status", async () => {
          await page.getByRole("button", { name: "Zurückziehen" }).click()
          await expect(
            page.getByTestId("portal-publication-status-badge"),
          ).toHaveText("Zurückgezogen")
          await expect(
            page.getByRole("button", { name: "Zurückziehen" }),
          ).toBeHidden()
        })

        await test.step("Veröffentlichen und Zurückziehen wird in der Historie geloggt", async () => {
          await navigateToManagementData(
            page,
            prefilledPendingProceeding.documentNumber,
          )
          await expect(
            page.getByRole("heading", { name: "Historie" }),
          ).toBeVisible()
          await expectHistoryLogRow(
            page,
            0,
            // "DS (e2e_tests DigitalService)",
            "Dokeinheit wurde aus dem Portal zurückgezogen",
          )
          await expectHistoryLogRow(
            page,
            1,
            // "NeuRIS",
            "Status im Portal geändert: Veröffentlicht → Zurückgezogen",
          )
          await expectHistoryLogRow(
            page,
            2,
            // "DS (e2e_tests DigitalService)",
            "Dokeinheit im Portal veröffentlicht",
          )
          await expectHistoryLogRow(
            page,
            3,
            // "NeuRIS",
            "Status im Portal geändert: Unveröffentlicht → Veröffentlicht",
          )
        })
      },
    )

    test(
      "LDML Vorschau Anhängiges Verfahren",
      {
        tag: ["@RISDEV-7896", "@RISDEV-8843"],
      },
      async ({ pageWithBfhUser, pendingProceedings }) => {
        const { createdPendingProceedings } = pendingProceedings
        const pendingProceeding = createdPendingProceedings[0]

        await navigateToPublication(
          pageWithBfhUser,
          pendingProceeding.documentNumber,
          {
            type: "pending-proceeding",
          },
        )

        await test.step("LDML Vorschau Ansicht ist ausklappbar und zeigt LDML an", async () => {
          await expect(pageWithBfhUser.getByText("LDML Vorschau")).toBeVisible()

          await pageWithBfhUser.getByText("LDML Vorschau").click()
          await expect(
            pageWithBfhUser.getByText("<akn:akomaNtoso"),
          ).toBeVisible()
        })

        await test.step("LDML Vorschau Ansicht ist zuklappbar", async () => {
          await pageWithBfhUser.getByText("LDML Vorschau").click()
          await expect(
            pageWithBfhUser.getByText("<akn:akomaNtoso"),
          ).toBeHidden()
        })

        await test.step("Fehler beim Laden der LDML Vorschau wenn kein valides LDML erzeugt werden kann", async () => {
          const pendingProceedingWithoutJudgmentBody =
            createdPendingProceedings[1]
          await navigateToPublication(
            pageWithBfhUser,
            pendingProceedingWithoutJudgmentBody.documentNumber,
            {
              type: "pending-proceeding",
            },
          )
          await expect(
            pageWithBfhUser.getByText("Fehler beim Laden der LDML-Vorschau"),
          ).toBeVisible()
          await expect(
            pageWithBfhUser.getByText(
              "Die LDML-Vorschau konnte nicht geladen werden: Missing judgment body.",
            ),
          ).toBeVisible()
          await expect(
            pageWithBfhUser.getByRole("button", { name: "Veröffentlichen" }),
          ).toBeDisabled()
        })
      },
    )
  },
)
