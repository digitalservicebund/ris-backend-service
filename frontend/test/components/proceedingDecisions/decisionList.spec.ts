import { render, screen } from "@testing-library/vue"
import DecisionList from "@/components/proceedingDecisions/DecisionList.vue"
import ProceedingDecision from "@/domain/proceedingDecision"

function renderComponent(options?: { decisions: ProceedingDecision[] }) {
  const props: { decisions: ProceedingDecision[] } = {
    decisions: options?.decisions ?? [
      new ProceedingDecision({
        ...{
          court: {
            type: "testCourtType",
            location: "testCourtLocation",
            label: "label",
          },
        },
      }),
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
        new ProceedingDecision({
          ...{
            court: {
              type: "testCourtType1",
              location: "testCourtLocation1",
              label: "label1",
            },
          },
        }),
        new ProceedingDecision({
          ...{
            court: {
              type: "testCourtType2",
              location: "testCourtLocation2",
              label: "label2",
            },
          },
        }),
      ],
    })
    expect((await screen.findAllByLabelText("Löschen")).length).toEqual(2)
  })
})
