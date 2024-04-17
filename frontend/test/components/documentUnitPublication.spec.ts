import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitPublication from "@/components/DocumentUnitPublication.vue"
import DocumentUnit from "@/domain/documentUnit"
import { PublicationHistoryRecordType } from "@/domain/xmlMail"
import publishService from "@/services/publishService"

function renderComponent() {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "/caselaw/documentUnit/new",
        name: "new",
        component: {},
      },
      {
        path: "/",
        name: "home",
        component: {},
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/categories",
        name: "caselaw-documentUnit-documentNumber-categories",
        component: {},
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/files",
        name: "caselaw-documentUnit-documentNumber-files",
        component: {},
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/publication",
        name: "caselaw-documentUnit-documentNumber-publication",
        component: {},
      },
    ],
  })
  return {
    user,
    ...render(DocumentUnitPublication, {
      props: {
        documentUnit: new DocumentUnit("foo", {
          documentNumber: "1234567891234",
          coreData: {
            fileNumbers: ["123"],
            court: {
              label: "foo",
            },
            decisionDate: "2024-01-01",
            documentType: { jurisShortcut: "", label: "" },
            legalEffect: "Ja",
          },
          texts: {},
          previousDecisions: undefined,
          ensuingDecisions: undefined,
          contentRelatedIndexing: {},
        }),
      },
      global: { plugins: [router] },
    }),
  }
}
describe("Document Unit Publication", () => {
  vi.spyOn(publishService, "getPublicationLog").mockImplementation(() =>
    Promise.resolve({
      status: 200,
      data: [
        {
          type: PublicationHistoryRecordType.PUBLICATION_REPORT,
        },
      ],
    }),
  )
  test("renders successfully", async () => {
    renderComponent()

    expect(
      screen.getByRole("heading", { name: "Überprüfung der Daten ..." }),
    ).toBeInTheDocument()
    expect(
      await screen.findByRole("heading", { name: "Veröffentlichen" }),
    ).toBeInTheDocument()
    expect(
      await screen.findByRole("heading", { name: "Letzte Veröffentlichungen" }),
    ).toBeInTheDocument()
  })

  test("renders error", async () => {
    vi.spyOn(publishService, "getPublicationLog").mockImplementation(() =>
      Promise.resolve({
        status: 300,
        error: {
          title: "Leider ist ein Fehler aufgetreten",
          description: "",
        },
      }),
    )
    renderComponent()

    expect(
      screen.getByRole("heading", { name: "Überprüfung der Daten ..." }),
    ).toBeInTheDocument()
    expect(
      await screen.findByRole("heading", { name: "Veröffentlichen" }),
    ).toBeInTheDocument()
    expect(
      await screen.findByText("Leider ist ein Fehler aufgetreten"),
    ).toBeInTheDocument()
  })

  test("renders publish result", async () => {
    vi.spyOn(publishService, "publishDocument").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {
          type: PublicationHistoryRecordType.PUBLICATION_REPORT,
          statusCode: "200",
        },
      }),
    )
    const { user } = renderComponent()

    expect(
      screen.getByRole("heading", { name: "Überprüfung der Daten ..." }),
    ).toBeInTheDocument()
    expect(
      await screen.findByLabelText("Dokumentationseinheit veröffentlichen"),
    ).toBeInTheDocument()

    await user.click(
      screen.getByLabelText("Dokumentationseinheit veröffentlichen"),
    )

    expect(await screen.findByLabelText("Erfolg der Veröffentlichung"))
      .toBeInTheDocument
  })
})
