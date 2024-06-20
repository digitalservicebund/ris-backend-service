import { render, screen } from "@testing-library/vue"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import DocumentUnit from "@/domain/documentUnit"

describe("documentUnit InfoPanel", () => {
  it("renders heading if given", async () => {
    render(DocumentUnitInfoPanel, {
      props: {
        heading: "test heading",
        documentUnit: new DocumentUnit("foo", {
          documentNumber: "1234567891234",
          coreData: {
            court: {
              type: "AG",
              location: "Test",
              label: "AG Test",
            },
          },
          texts: {},
          previousDecisions: undefined,
          ensuingDecisions: undefined,
          contentRelatedIndexing: {},
        }),
      },
    })

    screen.getAllByText("test heading")
  })

  it("renders all given property infos in correct order", async () => {
    render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("foo", {
          documentNumber: "1234567891234",
          coreData: {
            decisionDate: "2024-01-01",
            fileNumbers: ["AZ123"],
            court: {
              type: "AG",
              location: "Test",
              label: "AG Test",
            },
          },
          texts: {},
          previousDecisions: undefined,
          ensuingDecisions: undefined,
          contentRelatedIndexing: {},
        }),
      },
    })

    expect(await screen.findByText("Aktenzeichen")).toBeInTheDocument()
    expect(await screen.findByText("AZ123")).toBeInTheDocument()
    expect(await screen.findByText("Entscheidungsdatum")).toBeInTheDocument()
    expect(await screen.findByText("01.01.2024")).toBeInTheDocument()
    expect(await screen.findByText("Gericht")).toBeInTheDocument()
    expect(await screen.findByText("AG Test")).toBeInTheDocument()
  })

  it("renders a placeholder for an undefined property info value", async () => {
    render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("foo", {
          documentNumber: "1234567891234",
          coreData: {
            court: {
              type: "AG",
              location: "Test",
              label: "AG Test",
            },
          },
          texts: {},
          previousDecisions: undefined,
          ensuingDecisions: undefined,
          contentRelatedIndexing: {},
        }),
      },
    })

    const label = screen.getByText("Dokumentationsstelle")
    const [value] = await screen.findAllByText("-")

    expect(value).toBeInTheDocument()
    expect(value.compareDocumentPosition(label)).toBe(
      Node.DOCUMENT_POSITION_PRECEDING,
    )
  })
})
