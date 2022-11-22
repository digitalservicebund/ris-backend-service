import { fireEvent, render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import { DocumentUnitListEntry } from "@/domain/documentUnitListEntry"

function renderComponent(
  options?: Partial<DocumentUnitListEntry> | DocumentUnitListEntry[]
) {
  const documentUnitListEntries: DocumentUnitListEntry[] =
    options instanceof Array
      ? options
      : [
          {
            id: "id",
            uuid: "1",
            documentNumber: "123",
            creationtimestamp: "2022-02-10",
            filename: "",
            fileNumber: "",
            ...options,
          },
        ]
  return render(DocumentUnitList, {
    props: {
      documentUnitListEntries,
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
            {
              path: "",
              name: "caselaw-documentUnit-:documentNumber-categories",
              component: {},
            },
          ],
        }),
      ],
    },
  })
}

describe("documentUnit list", () => {
  test("renders fallback if no documentUnitsListEntries found", async () => {
    renderComponent([])

    await screen.findByText("Keine Dokumentationseinheiten gefunden")
  })

  test("renders documentUnits", async () => {
    renderComponent({ documentNumber: "foo" })

    await screen.findByText("123")
    await screen.findByText("10.02.2022")
    expect(
      screen.queryByText("Keine Dokumentationseinheiten gefunden")
    ).not.toBeInTheDocument()
  })

  test("renders documentUnits with file number and file name", async () => {
    renderComponent([
      {
        id: "id1",
        uuid: "1",
        documentNumber: "123",
        creationtimestamp: "2022-02-10",
        fileNumber: "foo",
        filename: "test.docx",
      },
    ])
    await screen.findByText("123")
    await screen.findByText("10.02.2022")
    await screen.findByText("foo")
    await screen.findByText("test.docx")
    expect(
      screen.queryByText("Keine Dokumentationseinheiten gefunden")
    ).not.toBeInTheDocument()
  })

  test("renders placeholder for missing file number and file name", async () => {
    renderComponent([
      {
        id: "id2",
        uuid: "2",
        documentNumber: "123",
        creationtimestamp: "2022-02-10",
      },
    ])

    const items = await screen.findAllByText(/-/)
    expect(items).toHaveLength(2)
    expect(
      screen.queryByText("Keine Dokumentationseinheiten gefunden")
    ).not.toBeInTheDocument()
  })

  test("delete emits event", async () => {
    const { emitted } = renderComponent({ id: "123" })

    await fireEvent.click(
      screen.getByLabelText("Dokumentationseinheit löschen")
    )
    const confirmButton = screen.getByRole("button", { name: "Löschen" })
    expect(confirmButton).toBeInTheDocument()
    await fireEvent.click(confirmButton)
    expect(emitted().deleteDocumentUnit).toBeTruthy()
  })
})
