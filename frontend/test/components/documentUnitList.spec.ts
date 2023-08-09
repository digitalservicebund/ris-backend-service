import { fireEvent, render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import { PublicationState } from "@/domain/documentUnit"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"

function renderComponent(
  options?: Partial<DocumentUnitListEntry> | DocumentUnitListEntry[],
) {
  const documentUnitListEntries: DocumentUnitListEntry[] =
    options instanceof Array
      ? options
      : [
          {
            id: "id",
            uuid: "1",
            documentNumber: "123",
            decisionDate: "2022-02-10",
            fileName: "",
            fileNumber: "",
            documentationOffice: { label: "testOffice" },
            documentType: { label: "Testlabel", jurisShortcut: "Test" },
            court: { type: "typeA", location: "locB", label: "typeA locB" },
            status: {
              publicationStatus: PublicationState.PUBLISHED,
              withError: false,
            },
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
              path: "/caselaw/documentUnit/:documentNumber/files",
              name: "caselaw-documentUnit-documentNumber-files",
              component: {},
            },
            {
              path: "/caselaw/documentUnit/:documentNumber/categories",
              name: "caselaw-documentUnit-documentNumber-categories",
              component: {},
            },
            {
              path: "/",
              name: "caselaw",
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
    renderComponent()

    await screen.findByText("123")
    await screen.findByText("10.02.2022")
    expect(
      screen.queryByText("Keine Dokumentationseinheiten gefunden"),
    ).not.toBeInTheDocument()
  })

  test("renders documentUnits with file number", async () => {
    renderComponent({
      fileNumber: "foo",
    })

    await screen.findByText("123")
    await screen.findByText("10.02.2022")
    await screen.findByText("foo")
    expect(
      screen.queryByText("Keine Dokumentationseinheiten gefunden"),
    ).not.toBeInTheDocument()
  })

  test("renders documentUnits with document type", async () => {
    renderComponent({
      documentType: { label: "Test123", jurisShortcut: "Test" },
    })

    await screen.findByText("123")
    await screen.findByText("10.02.2022")
    await screen.findByText("Test")
    expect(
      screen.queryByText("Keine Dokumentationseinheiten gefunden"),
    ).not.toBeInTheDocument()
  })

  test("renders documentUnits with court", async () => {
    renderComponent({
      court: { type: "typeA", location: "locB", label: "typeA locB" },
    })

    await screen.findByText("typeA")
    await screen.findByText("locB")
  })

  test("renders icon when file attached", async () => {
    renderComponent({
      fileName: "test.docx",
    })

    await screen.findByText("123")
    await screen.findByText("10.02.2022")

    expect(screen.getByText("attach_file")).toBeInTheDocument()
  })

  test("renders publication status", async () => {
    renderComponent()

    await screen.findByText("veröffentlicht")
  })

  test("delete emits event", async () => {
    const { emitted } = renderComponent({ id: "123" })

    await fireEvent.click(
      screen.getByLabelText("Dokumentationseinheit löschen"),
    )
    const confirmButton = screen.getByRole("button", { name: "Löschen" })
    expect(confirmButton).toBeInTheDocument()
    await fireEvent.click(confirmButton)
    expect(emitted().deleteDocumentUnit).toBeTruthy()
  })
})
