import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitHandover from "@/components/DocumentUnitHandover.vue"
import DocumentUnit from "@/domain/documentUnit"
import { EventRecordType } from "@/domain/eventRecord"
import documentUnitService from "@/services/documentUnitService"
import handoverService from "@/services/handoverService"

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
        path: "/caselaw/documentUnit/:documentNumber/handover",
        name: "caselaw-documentUnit-documentNumber-handover",
        component: {},
      },
    ],
  })
  return {
    user,
    ...render(DocumentUnitHandover, {
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
describe("Document Unit Handover", () => {
  vi.spyOn(handoverService, "getEventLog").mockImplementation(() =>
    Promise.resolve({
      status: 200,
      data: [
        {
          type: EventRecordType.HANDOVER_REPORT,
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
      await screen.findByRole("heading", { name: "Übergabe an jDV" }),
    ).toBeInTheDocument()
    expect(
      await screen.findByRole("heading", { name: "Letzte Ereignisse" }),
    ).toBeInTheDocument()
  })

  test("renders error", async () => {
    vi.spyOn(handoverService, "getEventLog").mockImplementation(() =>
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
      await screen.findByRole("heading", { name: "Übergabe an jDV" }),
    ).toBeInTheDocument()
    expect(
      await screen.findByText("Leider ist ein Fehler aufgetreten"),
    ).toBeInTheDocument()
  })

  test.skip("renders handover result", async () => {
    vi.spyOn(handoverService, "handoverDocument").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {
          type: EventRecordType.HANDOVER_REPORT,
          success: true,
        },
      }),
    )
    vi.spyOn(handoverService, "getPreview").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {
          xml: "<xml>all good</xml>",
          success: true,
        },
      }),
    )
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockImplementation(
      () =>
        Promise.resolve({
          status: 200,
          data: new DocumentUnit("foo", {
            documentNumber: "1234567891234",
            coreData: {
              court: {
                type: "AG",
                location: "Test",
                label: "AG Test",
              },
            },
            texts: {},
            previousDecisions: undefined,
            ensuingDecisions: undefined,
            contentRelatedIndexing: {},
          }),
        }),
    )
    const { user } = renderComponent()

    expect(
      screen.getByRole("heading", { name: "Überprüfung der Daten ..." }),
    ).toBeInTheDocument()
    expect(
      await screen.findByLabelText("Dokumentationseinheit an jDV übergeben"),
    ).toBeInTheDocument()

    await user.click(
      screen.getByLabelText("Dokumentationseinheit an jDV übergeben"),
    )

    expect(await screen.findByLabelText("Erfolg der jDV Übergabe"))
      .toBeInTheDocument
  })
})
