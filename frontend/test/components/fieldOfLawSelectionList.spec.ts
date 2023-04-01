import { fireEvent, render, RenderResult, screen } from "@testing-library/vue"
import FieldOfLawSelectionList from "@/components/FieldOfLawSelectionList.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"

function renderComponent(
  selectedFieldsOfLaw: Partial<FieldOfLawNode>[]
): RenderResult {
  const props = {
    selectedFieldsOfLaw,
  }

  return render(FieldOfLawSelectionList, { props })
}

describe("FieldOfLawSelectionList", () => {
  it("render a 'Die Liste ist aktuell leer'", () => {
    renderComponent([])

    expect(screen.getByText("Die Liste ist aktuell leer")).toBeInTheDocument()
  })

  it("render one entry", () => {
    renderComponent([
      {
        identifier: "ST-01-02-03",
        text: "Steuerrecht 1-2-3",
      },
    ])

    expect(
      screen.queryByText("Die Liste ist aktuell leer")
    ).not.toBeInTheDocument()
    expect(screen.getByText("ST-01-02-03")).toBeInTheDocument()
    expect(screen.getByText("Steuerrecht 1-2-3")).toBeInTheDocument()
    expect(
      screen.getByLabelText(
        "ST-01-02-03 Steuerrecht 1-2-3 im Sachgebietsbaum anzeigen"
      )
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("ST-01-02-03 Steuerrecht 1-2-3 entfernen")
    ).toBeInTheDocument()
  })

  it("click on 'Löschen' emit 'remove-from-list'", async () => {
    const { emitted } = renderComponent([
      {
        identifier: "ST-01-02-03",
        text: "Steuerrecht 1-2-3",
      },
    ])

    await fireEvent.click(
      screen.getByLabelText("ST-01-02-03 Steuerrecht 1-2-3 entfernen")
    )

    expect(emitted()["remove-from-list"]).toBeTruthy()
  })

  it("click on 'Auswahl im Sachgebietsbaum' emit 'select-node", async () => {
    const { emitted } = renderComponent([
      {
        identifier: "ST-01-02-03",
        text: "Steuerrecht 1-2-3",
      },
    ])

    await fireEvent.click(
      screen.getByLabelText(
        "ST-01-02-03 Steuerrecht 1-2-3 im Sachgebietsbaum anzeigen"
      )
    )

    expect(emitted()["node-clicked"]).toBeTruthy()
  })
})
