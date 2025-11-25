import { render, screen } from "@testing-library/vue"
import { previewLayoutInjectionKey } from "@/components/preview/constants"
import PreviewCoreData from "@/components/preview/PreviewCoreData.vue"
import { CoreData } from "@/domain/coreData"
import { Kind } from "@/domain/documentationUnitKind"
import { SourceValue } from "@/domain/source"

function renderComponent(coreData: CoreData, kind?: Kind) {
  return render(PreviewCoreData, {
    props: {
      coreData: coreData,
      kind: kind ?? Kind.DECISION,
    },
    global: {
      provide: {
        [previewLayoutInjectionKey as symbol]: "wide",
      },
    },
  })
}

describe("preview core data", () => {
  test("renders all core data fields", async () => {
    renderComponent({
      court: {
        label: "BVerfG",
        jurisdictionType: "Verfassungsgerichtsbarkeit",
        regions: ["DEU", "BY"],
      },
      leadingDecisionNormReferences: ["NSW WEG $ 14 (BGH-intern)"],
      deviatingCourts: ["BGH"],
      courtBranchLocation: "Augsburg",
      deviatingDocumentNumbers: ["XXRE123456789"],
      fileNumbers: ["abc-123"],
      deviatingFileNumbers: ["cde-456"],
      decisionDate: "2023-12-12",
      deviatingDecisionDates: ["2022-12-12"],
      oralHearingDates: ["2011-11-11", "2022-12-12"],
      appraisalBody: "1 Senat",
      documentType: {
        jurisShortcut: "Bes",
        label: "Beschluss",
      },
      ecli: "ecli123",
      deviatingEclis: ["eclu123"],
      celexNumber: "celex123",
      procedure: {
        label: "vorgang-1",
        documentationUnitCount: 1,
        createdAt: "2022-12-12",
      },
      previousProcedures: ["vorgang-0"],
      legalEffect: "Ja",
      yearsOfDispute: ["2023"],
      sources: [{ value: SourceValue.Zeitschrift }],
      inputTypes: ["Papier", "E-Mail"],
      isResolved: true,
      resolutionDate: "2025-06-12",
    })

    expect(await screen.findByText("Gericht")).toBeInTheDocument()
    expect(await screen.findByText("Fehlerhaftes Gericht")).toBeInTheDocument()
    expect(await screen.findByText("Sitz der Außenstelle")).toBeInTheDocument()
    expect(await screen.findByText("Aktenzeichen")).toBeInTheDocument()
    expect(
      await screen.findByText("Abweichendes Aktenzeichen"),
    ).toBeInTheDocument()
    expect(await screen.findByText("Entscheidungsdatum")).toBeInTheDocument()
    expect(
      await screen.findByText("Abweichendes Entscheidungsdatum"),
    ).toBeInTheDocument()
    expect(
      await screen.findByText("Datum der mündlichen Verhandlung"),
    ).toBeInTheDocument()
    expect(await screen.findByText("Spruchkörper")).toBeInTheDocument()
    expect(await screen.findByText("Dokumenttyp")).toBeInTheDocument()
    expect(
      await screen.findByText("Abweichende Dokumentnummer"),
    ).toBeInTheDocument()
    expect(await screen.findByText("CELEX-Nummer")).toBeInTheDocument()
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
    expect(await screen.findByText("Eingangsart")).toBeInTheDocument()
    expect(await screen.findByText("Papier, E-Mail")).toBeInTheDocument()
    expect(await screen.findByText("Gerichtsbarkeit")).toBeInTheDocument()
    expect(
      await screen.findByText("Verfassungsgerichtsbarkeit"),
    ).toBeInTheDocument()
    expect(await screen.findByText("Region")).toBeInTheDocument()
    expect(await screen.findByText("DEU, BY")).toBeInTheDocument()
    expect(
      screen.queryByText("Erledigung", { exact: true }),
    ).not.toBeInTheDocument()
    expect(await screen.findByText("Erledigungsmitteilung")).toBeInTheDocument()
  })

  test("renders 'Erledigung' for pending Proceeding", async () => {
    renderComponent(
      {
        court: {
          label: "BVerfG",
        },
        isResolved: true,
        resolutionDate: "2025-06-12",
      },
      Kind.PENDING_PROCEEDING,
    )

    expect(
      await screen.findByText("Erledigung", { exact: true }),
    ).toBeInTheDocument()
    expect(await screen.findByText("Erledigungsmitteilung")).toBeInTheDocument()
  })

  test("renders 'Datum der Auslieferung an Verkündung statt'", async () => {
    renderComponent({
      hasDeliveryDate: true,
      decisionDate: "2025-06-12",
    })

    expect(
      await screen.findByText("Datum der Zustellung an Verkündungs statt", {
        exact: true,
      }),
    ).toBeInTheDocument()
    expect(await screen.findByText("12.06.2025")).toBeInTheDocument()
  })

  test("do not render empty list", async () => {
    renderComponent({
      deviatingCourts: [],
      fileNumbers: [],
      deviatingFileNumbers: [],
      deviatingDecisionDates: [],
      oralHearingDates: [],
      deviatingEclis: [],
      deviatingDocumentNumbers: [],
      previousProcedures: [],
      leadingDecisionNormReferences: [],
      yearsOfDispute: [],
      inputTypes: [],
    })

    expect(screen.queryByText("Fehlerhaftes Gericht")).not.toBeInTheDocument()
    expect(screen.queryByText("Aktenzeichen")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Abweichendes Aktenzeichen"),
    ).not.toBeInTheDocument()
    expect(
      screen.queryByText("Abweichendes Entscheidungsdatum"),
    ).not.toBeInTheDocument()
    expect(
      screen.queryByText("Datum der mündlichen Verhandlung"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("Abweichender ECLI")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Abweichende Dokumentnummer"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("Vorgangshistorie")).not.toBeInTheDocument()
    expect(screen.queryByText("BGH Nachschlagewerk")).not.toBeInTheDocument()
    expect(screen.queryByText("Streitjahr")).not.toBeInTheDocument()
    expect(screen.queryByText("Eingangsart")).not.toBeInTheDocument()
  })

  it.each([
    [
      "Gericht",
      { court: { label: "BVerfG" } },
      [
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Fehlerhaftes Gericht",
      { deviatingCourts: ["AG Foo"] },
      [
        "Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Aktenzeichen",
      { fileNumbers: ["abc123"] },
      [
        "Gericht",
        "Fehlerhaftes Gericht",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Abweichendes Aktenzeichen",
      { deviatingFileNumbers: ["def456"] },
      [
        "Gericht",
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Entscheidungsdatum",
      { decisionDate: "2024-12-12" },
      [
        "Gericht",
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Abweichendes Entscheidungsdatum",
      { deviatingDecisionDates: ["2014-12-12"] },
      [
        "Gericht",
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Datum der mündlichen Verhandlung",
      { oralHearingDates: ["2014-12-12"] },
      [
        "Gericht",
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Spruchkörper",
      { appraisalBody: "Senat" },
      [
        "Gericht",
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Dokumenttyp",
      { documentType: { jurisShortcut: "Bes", label: "Beschluss" } },
      [
        "Gericht",
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Abweichende Dokumentnummer",
      { deviatingDocumentNumbers: ["XXRE123456789"] },
      [
        "Gericht",
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "CELEX-Nummer",
      { celexNumber: "celex123" },
      [
        "Gericht",
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "ECLI",
      { ecli: "ECLI:123:456:ABC" },
      [
        "Gericht",
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Abweichender ECLI",
      { deviatingEclis: ["ECLI:123:456:ABC"] },
      [
        "Gericht",
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Vorgang",
      {
        procedure: {
          label: "vorgang2",
          documentationUnitCount: 1,
          createdAt: "2023-12-24",
        },
      },
      [
        "Gericht",
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Vorgangshistorie",
      { previousProcedures: ["vorgang1"] },
      [
        "Gericht",
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Rechtskraft",
      { legalEffect: "Ja" },
      [
        "Gericht",
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Region",
      { court: { label: "foo", regions: ["BRD"] } },
      [
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Gerichtsbarkeit",
      { court: { label: "foo", jurisdictionType: "foo" } },
      [
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "BGH Nachschlagewerk",
      { leadingDecisionNormReferences: ["NSW WEG $ 14 (BGH-intern)"] },
      [
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Quelle",
      {
        sources: [
          {
            value: SourceValue.Zeitschrift,
          },
        ],
      },
      [
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Eingangsart",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Eingangsart",
      {
        inputTypes: ["Papier", "E-Mail"],
      },
      [
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Sitz der Außenstelle",
      ],
    ],
    [
      "Sitz der Außenstelle",
      {
        courtBranchLocation: "Augsburg",
      },
      [
        "Fehlerhaftes Gericht",
        "Aktenzeichen",
        "Abweichendes Aktenzeichen",
        "Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        "Spruchkörper",
        "Dokumenttyp",
        "Abweichende Dokumentnummer",
        "CELEX-Nummer",
        "ECLI",
        "Abweichender ECLI",
        "Vorgang",
        "Vorgangshistorie",
        "Rechtskraft",
        "Region",
        "BGH Nachschlagewerk",
        "Gerichtsbarkeit",
        "Quelle",
        "Eingangsart",
      ],
    ],
  ])(
    `renders only %s`,
    async (expected: string, coreData: CoreData, notExpected: string[]) => {
      renderComponent(coreData)
      expect(await screen.findByText(expected)).toBeInTheDocument()
      notExpected.forEach((it) => {
        expect(screen.queryByText(it)).not.toBeInTheDocument()
      })
    },
  )
})
