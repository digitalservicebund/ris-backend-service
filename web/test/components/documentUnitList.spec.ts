import { fireEvent, render, screen } from "@testing-library/vue"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import DocumentUnit from "@/domain/documentUnit"

describe("documentUnit list", () => {
  test("renders fallback if no documentUnits found", async () => {
    render(DocumentUnitList, {
      props: {
        documentUnits: [],
      },
    })

    await screen.findByText("Keine Dokumentationseinheiten gefunden")
  })

  test("renders documentUnits", async () => {
    const documentUnit = new DocumentUnit("1", {
      coreData: {
        fileNumber: "foo",
      },
    })

    render(DocumentUnitList, {
      props: {
        documentUnits: [documentUnit],
      },
    })

    await screen.findByText("foo")
    expect(
      screen.queryByText("Keine Dokumentationseinheiten gefunden")
    ).not.toBeInTheDocument()
  })

  test("delete emits event", async () => {
    const documentUnit = new DocumentUnit("1", {
      coreData: {
        fileNumber: "foo",
      },
    })

    const { emitted } = render(DocumentUnitList, {
      props: {
        documentUnits: [documentUnit],
      },
    })

    await fireEvent.click(
      screen.getByLabelText("Dokumentationseinheit löschen")
    )
    const confirmButton = screen.getByRole("button", { name: "Löschen" })
    expect(confirmButton).toBeInTheDocument()
    await fireEvent.click(confirmButton)
    expect(emitted().deleteDocumentUnit).toBeTruthy()
  })
})
