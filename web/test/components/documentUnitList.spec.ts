import { fireEvent, render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import { DocumentUnitListEntry } from "@/domain/documentUnit"

function renderComponent(
  documentUnitListEntries: DocumentUnitListEntry[] = [
    {
      id: "id",
      uuid: "1",
      documentNumber: "123",
      creationtimestamp: "today",
      fileNumber: "foo",
    },
  ]
) {
  return render(DocumentUnitList, {
    props: {
      documentUnitListEntries: documentUnitListEntries,
    },
    global: {
      plugins: [
        createRouter({
          history: createWebHistory(),
          routes: [
            {
              path: "",
              name: "caselaw-documentUnit-:documentNumber-files",
              component: {},
            },
          ],
        }),
      ],
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
