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
      dateLabel: "Entscheidungsdatum",
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
        regions: ["DEU"],
      },
      leadingDecisionNormReferences: ["NSW WEG $ 14 (BGH-intern)"],
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
      celexNumber: "celex123",
      procedure: {
        label: "vorgang-1",
        documentationUnitCount: 1,
        createdAt: "2022-12-12",
      },
      previousProcedures: ["vorgang-0"],
      legalEffect: "Ja",
      yearsOfDispute: ["2023"],
      source: { value: SourceValue.Zeitschrift },
      inputTypes: ["Papier", "E-Mail"],
      isResolved: true,
      resolutionDate: "2025-06-12",
    })

    expect(await screen.findByText("Gericht")).toBeInTheDocument()
    expect(await screen.findByText("Fehlerhaftes Gericht")).toBeInTheDocument()
    expect(await screen.findByText("Aktenzeichen")).toBeInTheDocument()
    expect(
      await screen.findByText("Abweichendes Aktenzeichen"),
    ).toBeInTheDocument()
    expect(await screen.findByText("Entscheidungsdatum")).toBeInTheDocument()
    expect(
      await screen.findByText("Abweichendes Entscheidungsdatum"),
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
    expect(await screen.findByText("DEU")).toBeInTheDocument()
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

  test("do not render empty list", async () => {
    renderComponent({
      deviatingCourts: [],
      fileNumbers: [],
      deviatingFileNumbers: [],
      deviatingDecisionDates: [],
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
      ],
    ],
    [
      "Region",
      { court: { label: "foo", region: "BRD" } },
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
      ],
    ],
    [
      "Quelle",
      {
        source: {
          value: SourceValue.Zeitschrift,
        },
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
