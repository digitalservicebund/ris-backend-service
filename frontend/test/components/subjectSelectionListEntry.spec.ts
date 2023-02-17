import { fireEvent, render, RenderResult, screen } from "@testing-library/vue"
import SubjectSelectionListEntry from "@/components/SubjectSelectionListEntry.vue"
import { SubjectNode } from "@/domain/SubjectTree"

function renderComponent(subject: Partial<SubjectNode>): RenderResult {
  const props = {
    subject,
  }

  return render(SubjectSelectionListEntry, { props })
}

describe("SubjectSelectionListEntry", () => {
  it("render entry", () => {
    renderComponent({
      subjectFieldNumber: "ST-01-02-03",
      subjectFieldText: "Steuerrecht 1-2-3",
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
      subjectFieldNumber: "ST-01-02-03",
      subjectFieldText: "Steuerrecht 1-2-3",
    })

    await fireEvent.click(screen.getByLabelText("Löschen"))

    expect(emitted()["remove-from-list"]).toBeTruthy()
  })

  it("click on 'Auswahl im Sachgebietsbaum' emit 'select-node", async () => {
    const { emitted } = renderComponent({
      subjectFieldNumber: "ST-01-02-03",
      subjectFieldText: "Steuerrecht 1-2-3",
    })

    await fireEvent.click(screen.getByLabelText("Auswahl im Sachgebietsbaum"))

    expect(emitted()["node-clicked"]).toBeTruthy()
  })
})
