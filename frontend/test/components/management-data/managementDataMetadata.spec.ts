import { render, screen, within } from "@testing-library/vue"
import ManagementDataMetadata from "@/components/management-data/ManagementDataMetadata.vue"
import { Decision } from "@/domain/decision"
import DocumentationOffice from "@/domain/documentationOffice"
import { DocumentationUnit } from "@/domain/documentationUnit"
import { ManagementData } from "@/domain/managementData"
import PendingProceeding from "@/domain/pendingProceeding"
import { Procedure } from "@/domain/procedure"
import Reference from "@/domain/reference"
import { Source, SourceValue } from "@/domain/source"

function mockDecision({
  managementData,
  source,
  procedure,
  creatingDocOffice,
}: {
  managementData?: Partial<ManagementData>
  source?: Source
  procedure?: Procedure
  creatingDocOffice?: DocumentationOffice
} = {}) {
  return new Decision("q834", {
    documentNumber: "original",
    coreData: { source, procedure, creatingDocOffice },
    managementData: {
      duplicateRelations: [],
      borderNumbers: [],
      ...managementData,
    },
  })
}

function mockPendingProceeding({
  managementData,
}: {
  managementData?: Partial<ManagementData>
} = {}) {
  return new PendingProceeding("q834", {
    documentNumber: "original",
    managementData: {
      duplicateRelations: [],
      borderNumbers: [],
      ...managementData,
    },
  })
}

describe("ManagementDataMetadata", () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it("should show all fields with – for a doc unit without data", async () => {
    const docUnit = mockDecision()
    renderMetadata(docUnit)

    const createdAt = screen.getByTestId("management-data-created-at")
    expect(within(createdAt).getByText("Angelegt am")).toBeInTheDocument()
    expect(within(createdAt).getByText("–")).toBeInTheDocument()

    const createdBy = screen.getByTestId("management-data-created-by")
    expect(within(createdBy).getByText("Von")).toBeInTheDocument()
    expect(within(createdBy).getByText("–")).toBeInTheDocument()

    const source = screen.getByTestId("management-data-source")
    expect(within(source).getByText("Quelle")).toBeInTheDocument()
    expect(within(source).getByText("–")).toBeInTheDocument()

    const lastUpdatedAt = screen.getByTestId("management-data-last-updated-at")
    expect(
      within(lastUpdatedAt).getByText("Zuletzt bearbeitet am"),
    ).toBeInTheDocument()
    expect(within(lastUpdatedAt).getByText("–")).toBeInTheDocument()

    const lastUpdatedBy = screen.getByTestId("management-data-last-updated-by")
    expect(within(lastUpdatedBy).getByText("Von")).toBeInTheDocument()
    expect(within(lastUpdatedBy).getByText("–")).toBeInTheDocument()

    const procedure = screen.getByTestId("management-data-procedure")
    expect(within(procedure).getByText("Vorgang")).toBeInTheDocument()
    expect(within(procedure).getByText("–")).toBeInTheDocument()

    const firstPublishedAt = screen.getByTestId(
      "management-data-first-published-at",
    )
    expect(
      within(firstPublishedAt).getByText("Erstveröffentlichung am"),
    ).toBeInTheDocument()
    expect(within(firstPublishedAt).getByText("–")).toBeInTheDocument()
  })

  it("should show date time for firstPublishedAtDateTime", async () => {
    const docUnit = mockDecision({
      managementData: { firstPublishedAtDateTime: "2025-07-31T08:00:00Z" },
    })
    renderMetadata(docUnit)

    const lastUpdated = screen.getByTestId("management-data-first-published-at")
    expect(
      within(lastUpdated).getByText("Erstveröffentlichung am"),
    ).toBeInTheDocument()
    expect(
      within(lastUpdated).getByText("31.07.2025 um 10:00 Uhr"),
    ).toBeInTheDocument()
  })

  it("should not show procedure for pending proceeding", async () => {
    const docUnit = mockPendingProceeding()
    renderMetadata(docUnit)

    expect(
      screen.queryByTestId("management-data-procedure"),
    ).not.toBeInTheDocument()
  })

  it("should show procedure if present", async () => {
    const docUnit = mockDecision({
      procedure: {
        label: "Mein Vorgang",
        createdAt: "1234",
        documentationUnitCount: 1,
      },
    })
    renderMetadata(docUnit)

    const lastUpdated = screen.getByTestId("management-data-procedure")
    expect(within(lastUpdated).getByText("Vorgang")).toBeInTheDocument()
    expect(within(lastUpdated).getByText("Mein Vorgang")).toBeInTheDocument()
  })

  describe("Angelegt von/am", () => {
    it("should show date time for createdAt", async () => {
      const docUnit = mockDecision({
        managementData: { createdAtDateTime: "2023-10-01T12:11:43Z" },
      })
      renderMetadata(docUnit)

      const createdAt = screen.getByTestId("management-data-created-at")
      expect(within(createdAt).getByText("Angelegt am")).toBeInTheDocument()
      expect(
        within(createdAt).getByText("01.10.2023 um 14:11 Uhr"),
      ).toBeInTheDocument()
    })

    it("should show name and doc office for createdBy", async () => {
      const docUnit = mockDecision({
        managementData: {
          createdByDocOffice: "BGH",
          createdByName: "Ada Lovelace",
        },
      })
      renderMetadata(docUnit)

      const createdAt = screen.getByTestId("management-data-created-by")
      expect(within(createdAt).getByText("Von")).toBeInTheDocument()
      expect(
        within(createdAt).getByText("BGH (Ada Lovelace)"),
      ).toBeInTheDocument()
    })

    it("should show only doc office for createdBy", async () => {
      const docUnit = mockDecision({
        managementData: {
          createdByDocOffice: "BGH",
        },
      })
      renderMetadata(docUnit)

      const createdAt = screen.getByTestId("management-data-created-by")
      expect(within(createdAt).getByText("Von")).toBeInTheDocument()
      expect(within(createdAt).getByText("BGH")).toBeInTheDocument()
    })

    it("should show only name for createdBy", async () => {
      const docUnit = mockDecision({
        managementData: {
          createdByName: "NeuRIS",
        },
      })
      renderMetadata(docUnit)

      const createdAt = screen.getByTestId("management-data-created-by")
      expect(within(createdAt).getByText("Von")).toBeInTheDocument()
      expect(within(createdAt).getByText("NeuRIS")).toBeInTheDocument()
    })
  })

  describe("Zuletzt bearbeitet von/am", () => {
    it("should show date time for lastUpdatedAt", async () => {
      const docUnit = mockDecision({
        managementData: { lastUpdatedAtDateTime: "2025-01-31T23:55:59Z" },
      })
      renderMetadata(docUnit)

      const lastUpdated = screen.getByTestId("management-data-last-updated-at")
      expect(
        within(lastUpdated).getByText("Zuletzt bearbeitet am"),
      ).toBeInTheDocument()
      expect(
        within(lastUpdated).getByText("01.02.2025 um 00:55 Uhr"),
      ).toBeInTheDocument()
    })

    it("should show name and doc office for lastUpdated", async () => {
      const docUnit = mockDecision({
        managementData: {
          lastUpdatedByDocOffice: "BGH",
          lastUpdatedByName: "Ada Lovelace",
        },
      })
      renderMetadata(docUnit)

      const lastUpdated = screen.getByTestId("management-data-last-updated-by")
      expect(within(lastUpdated).getByText("Von")).toBeInTheDocument()
      expect(
        within(lastUpdated).getByText("BGH (Ada Lovelace)"),
      ).toBeInTheDocument()
    })

    it("should show only doc office for lastUpdated", async () => {
      const docUnit = mockDecision({
        managementData: {
          lastUpdatedByDocOffice: "BGH",
        },
      })
      renderMetadata(docUnit)

      const lastUpdated = screen.getByTestId("management-data-last-updated-by")
      expect(within(lastUpdated).getByText("Von")).toBeInTheDocument()
      expect(within(lastUpdated).getByText("BGH")).toBeInTheDocument()
    })

    it("should show only name for lastUpdated", async () => {
      const docUnit = mockDecision({
        managementData: {
          lastUpdatedByName: "NeuRIS",
        },
      })
      renderMetadata(docUnit)

      const lastUpdated = screen.getByTestId("management-data-last-updated-by")
      expect(within(lastUpdated).getByText("Von")).toBeInTheDocument()
      expect(within(lastUpdated).getByText("NeuRIS")).toBeInTheDocument()
    })
  })

  describe("Quelle", () => {
    it("should show source without reference", async () => {
      const docUnit = mockDecision({
        source: { value: SourceValue.Email },
      })
      renderMetadata(docUnit)

      const lastUpdated = screen.getByTestId("management-data-source")
      expect(within(lastUpdated).getByText("Quelle")).toBeInTheDocument()
      expect(within(lastUpdated).getByText("E")).toBeInTheDocument()
    })

    it("should show source with reference without creating doc office", async () => {
      const docUnit = mockDecision({
        source: {
          value: SourceValue.Zeitschrift,
          reference: new Reference({
            legalPeriodical: { abbreviation: "OLF" },
            citation: "2024, 01",
            referenceSupplement: "L",
            author: "Marie Merke",
            documentType: { label: "janz doll", jurisShortcut: "jd" },
          }),
        },
      })
      renderMetadata(docUnit)

      const lastUpdated = screen.getByTestId("management-data-source")
      expect(within(lastUpdated).getByText("Quelle")).toBeInTheDocument()
      expect(
        within(lastUpdated).getByText(
          "Z aus OLF 2024, 01, (L) Marie Merke (jd)",
        ),
      ).toBeInTheDocument()
    })

    it("should show source with reference with creating doc office", async () => {
      const docUnit = mockDecision({
        source: {
          value: SourceValue.Zeitschrift,
          reference: new Reference({
            legalPeriodical: { abbreviation: "R&R" },
            citation: "2024, 01",
          }),
        },
        creatingDocOffice: { abbreviation: "BGH", id: "123" },
      })
      renderMetadata(docUnit)

      const lastUpdated = screen.getByTestId("management-data-source")
      expect(within(lastUpdated).getByText("Quelle")).toBeInTheDocument()
      expect(
        within(lastUpdated).getByText("Z aus R&R 2024, 01 (BGH)"),
      ).toBeInTheDocument()
    })
  })

  function renderMetadata(documentUnit: DocumentationUnit) {
    render(ManagementDataMetadata, { props: { documentUnit } })
  }
})
