import { fireEvent, render, RenderResult, screen } from "@testing-library/vue"
import FieldOfLawSelectionListEntry from "@/components/FieldOfLawSelectionListEntry.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"

function renderComponent(subject: Partial<FieldOfLawNode>): RenderResult {
  const props = {
    subject,
  }

  return render(FieldOfLawSelectionListEntry, { props })
}

describe("SubjectSelectionListEntry", () => {
  it("render entry", () => {
    renderComponent({
      identifier: "ST-01-02-03",
      text: "Steuerrecht 1-2-3",
    })

    expect(screen.getByText("ST-01-02-03")).toBeInTheDocument()
    expect(screen.getByText("Steuerrecht 1-2-3")).toBeInTheDocument()
    expect(
      screen.getByLabelText("Auswahl im Sachgebietsbaum")
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Löschen")).toBeInTheDocument()
  })

  it("click on 'Löschen' emit 'remove-from-list'", async () => {
    const { emitted } = renderComponent({
      identifier: "ST-01-02-03",
      text: "Steuerrecht 1-2-3",
    })

    await fireEvent.click(screen.getByLabelText("Löschen"))

    expect(emitted()["remove-from-list"]).toBeTruthy()
  })

  it("click on 'Auswahl im Sachgebietsbaum' emit 'select-node", async () => {
    const { emitted } = renderComponent({
      identifier: "ST-01-02-03",
      text: "Steuerrecht 1-2-3",
    })

    await fireEvent.click(screen.getByLabelText("Auswahl im Sachgebietsbaum"))

    expect(emitted()["node-clicked"]).toBeTruthy()
  })
})
