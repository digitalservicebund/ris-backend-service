import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import InlineDecision from "@/components/proceedingDecisions/InlineDecision.vue"
import { Court, DocumentType } from "@/domain/documentUnit"
import ProceedingDecision from "@/domain/proceedingDecision"

function renderComponent(options?: {
  court?: Court
  documentType?: DocumentType
  date?: string
  dataSource?: ProceedingDecision["dataSource"]
  documentNumber?: string
}) {
  const props: { decision: ProceedingDecision } = {
    decision: new ProceedingDecision({
      ...{
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
        dataSource: options?.dataSource ?? "PROCEEDING_DECISION",
        documentNumber: options?.documentNumber ?? "testDocumentNumber",
      },
    }),
  }

  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "index",
        component: {},
      },
      {
        path: "/:documentNumber/categories",
        name: "caselaw-documentUnit-:documentNumber-categories",
        component: {},
      },
    ],
  })

  return render(InlineDecision, {
    props,
    global: { plugins: [router] },
  })
}

describe("Decision ListItem", () => {
  it("renders court correctly", async () => {
    renderComponent({
      court: { type: "foo", location: "bar", label: "testLabel" },
    })
    expect(await screen.findByText(/testLabel/)).toBeVisible()
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
    expect(await screen.findByText(/27.03.2022/)).toBeVisible()
  })

  it("renders with link if source is not proceedingDecisions", async () => {
    renderComponent({
      dataSource: "MIGRATION",
      documentNumber: "fooDocumentNumber",
    })
    expect(screen.getByRole("link")).toHaveAttribute(
      "href",
      expect.stringMatching(/fooDocumentNumber/)
    )
  })
})
