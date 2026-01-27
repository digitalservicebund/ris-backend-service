import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import CorrectionSummary from "@/components/CorrectionSummary.vue"
import Correction from "@/domain/correction"
import PendingProceeding from "@/domain/pendingProceeding"

function renderComponent(correction: Correction) {
  return render(CorrectionSummary, {
    props: {
      data: correction,
    },
    global: {
      plugins: [
        [
          createTestingPinia({
            initialState: {
              session: { user: { internal: true } },
              docunitStore: {
                documentUnit: new PendingProceeding("foo", {
                  documentNumber: "1234567891234",
                }),
              },
            },
          }),
        ],
      ],
    },
  })
}

describe("Correction summary", () => {
  it("renders summary correctly for just type", async () => {
    renderComponent(
      new Correction({
        type: "Unrichtigkeiten",
      }),
    )
    expect(screen.getByText("Unrichtigkeiten")).toBeVisible()
  })

  it("renders summary correctly for type, description & border numbers", async () => {
    const view = renderComponent(
      new Correction({
        type: "Unrichtigkeiten",
        description: "Hauffen -> Haufen",
        borderNumbers: [1, 3],
      }),
    )
    expect(view.container).toHaveTextContent(
      "Unrichtigkeiten , Hauffen -> Haufen|13",
    )
  })

  it("renders summary correctly for all properties", async () => {
    const view = renderComponent(
      new Correction({
        type: "Unrichtigkeiten",
        date: "2023-12-24",
        description: "Hauffen -> Haufen",
        borderNumbers: [1, 3],
      }),
    )
    expect(view.container).toHaveTextContent(
      "Unrichtigkeiten , Hauffen -> Haufen, 24.12.2023|13",
    )
  })

  it("shows error badge for errors", async () => {
    renderComponent(
      new Correction({
        date: "2023-12-24",
        description: "Hauffen -> Haufen",
        borderNumbers: [1, 3],
      }),
    )
    expect(screen.getByText("Fehlende Daten")).toBeVisible()
  })
})
