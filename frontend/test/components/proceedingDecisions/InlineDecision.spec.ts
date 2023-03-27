import { render, screen } from "@testing-library/vue"
import InlineDecision from "@/components/proceedingDecisions/InlineDecision.vue"
import { Court, DocumentType, ProceedingDecision } from "@/domain/documentUnit"

function renderComponent(options?: {
  court?: Court
  documentType?: DocumentType
  date?: string
}) {
  const props: { decision: ProceedingDecision } = {
    decision: {
      court: options?.court ?? {
        type: "testCourtType",
        location: "testCourtLocation",
        label: "label1",
      },
      documentType: options?.documentType ?? {
        label: "testDocumentType",
        jurisShortcut: "testDocumentTypeShortcut",
      },
      date: options?.date ?? "2004-12-02 12:00:00.000000 +00:00",
    },
  }

  return render(InlineDecision, { props })
}

describe("Decision ListItem", () => {
  it("renders court correctly", async () => {
    renderComponent({
      court: { type: "foo", location: "bar", label: "testLabel" },
    })
    expect(await screen.findByText(/foo bar/)).toBeVisible()
  })

  it("renders documentType shortcut", async () => {
    renderComponent({
      documentType: {
        label: "fooLabel",
        jurisShortcut: "barShortcut",
      },
    })
    expect(await screen.findByText(/barShortcut/)).toBeVisible()
  })

  it("renders date correctly", async () => {
    renderComponent({ date: "2022-03-27" })
    expect(await screen.findByText(/27.03.2022/))
  })
})
