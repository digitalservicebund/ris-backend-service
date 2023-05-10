import { render, screen } from "@testing-library/vue"
import DecisionList from "@/components/proceedingDecisions/DecisionList.vue"
import { ProceedingDecision } from "@/domain/proceedingDecision"

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

  it("sorts the decisions by data", async () => {
    renderComponent({
      decisions: [
        new ProceedingDecision({
          ...{
            court: {
              type: "testCourtType",
              location: "testCourtLocation",
              label: "label",
            },
            date: "10-10-2000 12:00:00.000000 +00:00",
          },
        }),
        new ProceedingDecision({
          ...{
            court: {
              type: "testCourtType",
              location: "testCourtLocation",
              label: "label",
            },
            date: "10-10-2100 12:00:00.000000 +00:00",
          },
        }),
        new ProceedingDecision({
          ...{
            court: {
              type: "testCourtType",
              location: "testCourtLocation",
              label: "label",
            },
            date: "10-10-1900 12:00:00.000000 +00:00",
          },
        }),
      ],
    })

    const decision1 = await screen.findByText(/10.10.1900/)
    const decision2 = await screen.findByText(/10.10.2000/)
    const decision3 = await screen.findByText(/10.10.2100/)
    expect(decision1.compareDocumentPosition(decision2)).toBe(2)
    expect(decision1.compareDocumentPosition(decision3)).toBe(2)
    expect(decision2.compareDocumentPosition(decision3)).toBe(2)
  })
})
