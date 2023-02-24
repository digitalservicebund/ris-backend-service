import { fireEvent, render, RenderResult, screen } from "@testing-library/vue"
import FieldOfLawSelectionList from "@/components/FieldOfLawSelectionList.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLawTree"

function renderComponent(
  selectedSubjects: Partial<FieldOfLawNode>[]
): RenderResult {
  const props = {
    selectedSubjects,
  }

  return render(FieldOfLawSelectionList, { props })
}

describe("SubjectSelectionList", () => {
  it("check headline", () => {
    renderComponent([])

    expect(screen.getByText("Auswahl")).toBeInTheDocument()
  })

  it("render a 'Die Liste ist aktuell leer'", () => {
    renderComponent([])

    expect(screen.getByText("Die Liste ist aktuell leer")).toBeInTheDocument()
  })

  it("render one entry", () => {
    renderComponent([
      {
        identifier: "ST-01-02-03",
        subjectFieldText: "Steuerrecht 1-2-3",
      },
    ])

    expect(
      screen.queryByText("Die Liste ist aktuell leer")
    ).not.toBeInTheDocument()
    expect(screen.getByText("ST-01-02-03")).toBeInTheDocument()
    expect(screen.getByText("Steuerrecht 1-2-3")).toBeInTheDocument()
    expect(
      screen.getByLabelText("Auswahl im Sachgebietsbaum")
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Löschen")).toBeInTheDocument()
  })

  it("click on 'Löschen' emit 'remove-from-list'", async () => {
    const { emitted } = renderComponent([
      {
        identifier: "ST-01-02-03",
        subjectFieldText: "Steuerrecht 1-2-3",
      },
    ])

    await fireEvent.click(screen.getByLabelText("Löschen"))

    expect(emitted()["remove-from-list"]).toBeTruthy()
  })

  it("click on 'Auswahl im Sachgebietsbaum' emit 'select-node", async () => {
    const { emitted } = renderComponent([
      {
        identifier: "ST-01-02-03",
        subjectFieldText: "Steuerrecht 1-2-3",
      },
    ])

    await fireEvent.click(screen.getByLabelText("Auswahl im Sachgebietsbaum"))

    expect(emitted()["node-clicked"]).toBeTruthy()
  })
})
