import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { beforeAll } from "vitest"
import PendingProceedingPreview from "@/components/preview/PendingProceedingPreview.vue"
import PendingProceeding from "@/domain/pendingProceeding"
import { SourceValue } from "@/domain/source"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

beforeAll(() => {
  useFeatureToggleServiceMock()
})

function renderComponent() {
  return {
    ...render(PendingProceedingPreview, {
      props: {
        documentNumber: "1234567891234",
      },
      global: {
        plugins: [
          [
            createTestingPinia({
              initialState: {
                session: { user: { internal: true } },
                docunitStore: {
                  documentUnit: new PendingProceeding("foo", {
                    documentNumber: "1234567891234",
                    coreData: {
                      court: {
                        label: "BVerfG",
                        jurisdictionType: "Verfassungsgerichtsbarkeit",
                        regions: ["DEU"],
                      },
                      courtBranchLocation: { value: "Augsburg", id: "1" },
                      leadingDecisionNormReferences: [
                        "NSW WEG $ 14 (BGH-intern)",
                      ],
                      deviatingCourts: ["BGH"],
                      deviatingDocumentNumbers: ["XXRE123456789"],
                      fileNumbers: ["abc-123"],
                      deviatingFileNumbers: ["cde-456"],
                      decisionDate: "2023-12-12",
                      deviatingDecisionDates: ["2022-12-12"],
                      appraisalBody: "1 Senat",
                      documentType: {
                        jurisShortcut: "Bes",
                        label: "Beschluss",
                      },
                      ecli: "ecli123",
                      deviatingEclis: ["eclu123"],
                      procedure: {
                        label: "vorgang-1",
                        documentationUnitCount: 1,
                        createdAt: "2022-12-12",
                      },
                      previousProcedures: ["vorgang-0"],
                      legalEffect: "Ja",
                      yearsOfDispute: [2023],
                      sources: [{ value: SourceValue.Zeitschrift }],
                      isResolved: true,
                      resolutionDate: "2025-06-12",
                    },
                    shortTexts: {
                      headline: "headline",
                      legalIssue: "legalIssue",
                      appellant: "appellant",
                      admissionOfAppeal: "admissioinOfAppeal",
                      resolutionNote: "resolutionNote",
                    },
                  }),
                },
              },
              stubActions: false,
            }),
          ],
        ],
      },
    }),
  }
}

describe("preview", () => {
  test("renders all data", async () => {
    renderComponent()

    expect(await screen.findByText("Gericht")).toBeInTheDocument()
    expect(await screen.findByText("Fehlerhaftes Gericht")).toBeInTheDocument()
    expect(await screen.findByText("Sitz der Außenstelle")).toBeInTheDocument()
    expect(await screen.findByText("Aktenzeichen")).toBeInTheDocument()
    expect(
      await screen.findByText("Abweichendes Aktenzeichen"),
    ).toBeInTheDocument()
    expect(await screen.findByText("Erledigungsmitteilung")).toBeInTheDocument()
    expect(
      screen.queryByText("Abweichendes Entscheidungsdatum"),
    ).not.toBeInTheDocument()
    expect(await screen.findByText("Spruchkörper")).toBeInTheDocument()
    expect(await screen.findByText("Dokumenttyp")).toBeInTheDocument()
    expect(
      await screen.findByText("Abweichende Dokumentnummer"),
    ).toBeInTheDocument()
    expect(await screen.findByText("ECLI")).toBeInTheDocument()
    expect(await screen.findByText("Abweichender ECLI")).toBeInTheDocument()
    expect(await screen.findByText("Vorgang")).toBeInTheDocument()
    expect(await screen.findByText("Vorgangshistorie")).toBeInTheDocument()
    expect(await screen.findByText("Rechtskraft")).toBeInTheDocument()
    expect(await screen.findByText("Gerichtsbarkeit")).toBeInTheDocument()
    expect(await screen.findByText("Region")).toBeInTheDocument()
    expect(await screen.findByText("BGH Nachschlagewerk")).toBeInTheDocument()
    expect(await screen.findByText("Streitjahr")).toBeInTheDocument()
    expect(await screen.findByText("Quelle")).toBeInTheDocument()
    expect(await screen.findByText("Z")).toBeInTheDocument()
    expect(await screen.findByText("Gerichtsbarkeit")).toBeInTheDocument()
    expect(
      await screen.findByText("Verfassungsgerichtsbarkeit"),
    ).toBeInTheDocument()
    expect(await screen.findByText("Region")).toBeInTheDocument()
    expect(await screen.findByText("DEU")).toBeInTheDocument()
    expect(
      await screen.findByText("Erledigung", { exact: true }),
    ).toBeInTheDocument()
    expect(await screen.findByText("Erledigungsmitteilung")).toBeInTheDocument()
    expect(await screen.findByText("Titelzeile")).toBeInTheDocument()
    expect(await screen.findByText("Rechtsfrage")).toBeInTheDocument()
    expect(await screen.findByText("Rechtsmittelführer")).toBeInTheDocument()
    expect(await screen.findByText("Rechtsmittelzulassung")).toBeInTheDocument()
  })
})
