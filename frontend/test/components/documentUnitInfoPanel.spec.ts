import { render, screen } from "@testing-library/vue"
import DocumentUnitInfoPanel from "@/shared/components/DocumentUnitInfoPanel.vue"

describe("documentUnit InfoPanel", () => {
  it("renders heading if given", async () => {
    render(DocumentUnitInfoPanel, {
      props: {
        heading: "test heading",
      },
    })

    screen.getAllByText("test heading")
  })

  it("renders all given property infos in correct order", async () => {
    render(DocumentUnitInfoPanel, {
      props: {
        secondRow: [
          { label: "foo", value: "value-foo" },
          { label: "bar", value: "value-bar" },
        ],
      },
    })

    const fooLabel = await screen.findByText("foo")
    const fooValue = await screen.findByText("value-foo")
    const barLabel = await screen.findByText("bar")
    const barValue = await screen.findByText("value-bar")

    expect(fooLabel).toBeInTheDocument()
    expect(fooValue).toBeInTheDocument()
    expect(barLabel).toBeInTheDocument()
    expect(barValue).toBeInTheDocument()

    expect(fooLabel.compareDocumentPosition(fooValue)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING
    )
    expect(fooValue.compareDocumentPosition(barLabel)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING
    )
    expect(barLabel.compareDocumentPosition(barValue)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING
    )
  })

  it("renders a placeholder for an undefined property info value", async () => {
    render(DocumentUnitInfoPanel, {
      props: {
        secondRow: [{ label: "foo", value: undefined }],
      },
    })

    const label = screen.getByText("foo")
    const value = await screen.findByText("-")

    expect(value).toBeInTheDocument()
    expect(value.compareDocumentPosition(label)).toBe(
      Node.DOCUMENT_POSITION_PRECEDING
    )
  })

  it("renders 'center' as the default alignment", () => {
    render(DocumentUnitInfoPanel)
    const items = screen.getByTestId("document-unit-info-panel-items")
    expect(items).toHaveClass("items-center")
  })

  it("renders 'center' alignment if given", () => {
    render(DocumentUnitInfoPanel, { props: { alignment: "center" } })
    const items = screen.getByTestId("document-unit-info-panel-items")
    expect(items).toHaveClass("items-center")
  })

  it("renders 'baseline' alignment if given", () => {
    render(DocumentUnitInfoPanel, { props: { alignment: "baseline" } })
    const items = screen.getByTestId("document-unit-info-panel-items")
    expect(items).toHaveClass("items-baseline")
  })
})
