import { render, screen } from "@testing-library/vue"
import DecisionList from "@/components/proceedingDecisions/DecisionList.vue"
import { ProceedingDecision } from "@/domain/documentUnit"

function renderComponent(options?: { decisions: ProceedingDecision[] }) {
  const props: { decisions: ProceedingDecision[] } = {
    decisions: options?.decisions ?? [
      {
        court: {
          type: "testCourtType",
          location: "testCourtLocation",
          label: "label1",
        },
      },
    ],
  }

  return render(DecisionList, {
    props,
    global: {
      stubs: { routerLink: { template: "<a><slot/></a>" } },
    },
  })
}

describe("ProceedingDecision List", () => {
  it("renders correct amount of items", async () => {
    renderComponent({
      decisions: [
        {
          court: {
            type: "testCourtType1",
            location: "testCourtLocation1",
            label: "label1",
          },
        },
        {
          court: {
            type: "testCourtType2",
            location: "testCourtLocation2",
            label: "label2",
          },
        },
      ],
    })
    expect((await screen.findAllByLabelText("Löschen")).length).toEqual(2)
  })
})
