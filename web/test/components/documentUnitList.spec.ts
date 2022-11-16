import { fireEvent, render, screen } from "@testing-library/vue"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import DocumentUnit from "@/domain/documentUnit"

function renderComponent(
  documentUnits: DocumentUnit[] = [
    new DocumentUnit("1", {
      coreData: {
        fileNumber: "foo",
      },
    }),
  ]
) {
  return render(DocumentUnitList, {
    props: {
      documentUnits: documentUnits,
    },
  })
}

describe("documentUnit list", () => {
  test("renders fallback if no documentUnits found", async () => {
    renderComponent([])

    await screen.findByText("Keine Dokumentationseinheiten gefunden")
  })

  test("renders documentUnits", async () => {
    renderComponent()

    await screen.findByText("foo")
    expect(
      screen.queryByText("Keine Dokumentationseinheiten gefunden")
    ).not.toBeInTheDocument()
  })

  test("delete emits event", async () => {
    const { emitted } = renderComponent()

    await fireEvent.click(
      screen.getByLabelText("Dokumentationseinheit löschen")
    )
    const confirmButton = screen.getByRole("button", { name: "Löschen" })
    expect(confirmButton).toBeInTheDocument()
    await fireEvent.click(confirmButton)
    expect(emitted().deleteDocumentUnit).toBeTruthy()
  })
})
