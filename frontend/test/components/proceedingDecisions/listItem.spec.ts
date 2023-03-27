import { render, screen } from "@testing-library/vue"
import ListItem from "@/components/proceedingDecisions/ListItem.vue"
import { Court, ProceedingDecision } from "@/domain/documentUnit"

function renderComponent(options?: { court?: Court }) {
  const props: { decision: ProceedingDecision } = {
    decision: {
      court: options?.court ?? {
        type: "testCourtType",
        location: "testCourtLocation",
        label: "label1",
      },
    },
  }

  return render(ListItem, { props })
}

describe("Decision ListItem", () => {
  it("renders court correctly", async () => {
    renderComponent({
      court: { type: "foo", location: "bar", label: "testLabel" },
    })
    expect(await screen.findByText("foo bar")).toBeVisible()
  })
})
