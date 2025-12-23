import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { setActivePinia } from "pinia"
import { createRouter, createWebHistory } from "vue-router"
import { SourceValue } from "./../../../src/domain/source"
import PeriodicalEditionReferenceInput from "@/components/periodical-evaluation/references/PeriodicalEditionReferenceInput.vue"
import { Decision } from "@/domain/decision"
import DocumentationOffice from "@/domain/documentationOffice"
import Reference from "@/domain/reference"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import documentUnitService from "@/services/documentUnitService"
import featureToggleService from "@/services/featureToggleService"
import { onSearchShortcutDirective } from "@/utils/onSearchShortcutDirective"
import routes from "~/test-helper/routes"

// Mock the stores
vi.mock("@/stores/documentUnitStore", () => ({
  useDocumentUnitStore: vi.fn(),
}))

vi.mock("@/stores/extraContentSidePanelStore", () => ({
  useExtraContentSidePanelStore: vi.fn(),
}))

vi.mock("@/stores/useEditionStore", () => ({
  useEditionStore: vi.fn(),
}))

// Mock the useScroll composable globally
const scrollIntoViewportByIdMock = vi.fn()
const openSidePanelAndScrollToSectionMock = vi.fn()

const dsDocOffice: DocumentationOffice = {
  id: "456",
  abbreviation: "DS",
}

const server = setupServer(
  http.get("/api/v1/caselaw/documentationoffices", () =>
    HttpResponse.json([dsDocOffice]),
  ),
  http.get("/api/v1/caselaw/courts", () => {
    return HttpResponse.json([])
  }),
  http.get("/api/v1/caselaw/documenttypes", () => {
    return HttpResponse.json([])
  }),
)

vi.mock("@/composables/useScroll", () => ({
  useScroll: () => ({
    scrollIntoViewportById: scrollIntoViewportByIdMock,
    openSidePanelAndScrollToSection: openSidePanelAndScrollToSectionMock,
  }),
}))

function renderComponent(
  options: {
    modelValue?: Reference
  } = {},
) {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(PeriodicalEditionReferenceInput, {
      props: {
        modelValue: options.modelValue ?? undefined,
        modelValueList: [],
      },
      global: {
        directives: { "ctrl-enter": onSearchShortcutDirective },
        plugins: [
          router,
          [
            createTestingPinia({
              initialState: {
                editionStore: undefined,
              },
              stubActions: false,
            }),
          ],
        ],
      },
    }),
  }
}

describe("Legal periodical edition reference input", () => {
  beforeAll(() => server.listen())
  afterAll(() => server.close())
  beforeEach(() => {
    // Activate Pinia
    vi.restoreAllMocks()
    setActivePinia(createTestingPinia())

    // Mock the searchByRelatedDocumentation method
    vi.spyOn(
      documentUnitService,
      "searchByRelatedDocumentation",
    ).mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {
          content: [
            new RelatedDocumentation({
              uuid: "123",
              court: {
                type: "type1",
                location: "location1",
                label: "label1",
              },
              decisionDate: "2022-02-01",
              documentType: {
                jurisShortcut: "documentTypeShortcut1",
                label: "documentType1",
              },
              fileNumber: "test fileNumber1",
            }),
          ],
          size: 0,
          number: 0,
          numberOfElements: 20,
          first: true,
          last: false,
          empty: false,
        },
      }),
    )

    vi.spyOn(featureToggleService, "isEnabled").mockResolvedValue({
      status: 200,
      data: true,
    })
  })

  it("search is triggered with shortcut", async () => {
    const { user } = renderComponent()

    expect(screen.queryByText(/test fileNumber1/)).not.toBeInTheDocument()
    await user.type(await screen.findByLabelText("Aktenzeichen"), "test")
    await user.keyboard("{Control>}{Enter}")

    expect(screen.getAllByText(/test fileNumber1/).length).toBe(1)
    vi.resetAllMocks()
  })

  test("adding a decision scrolls to reference on validation errors", async () => {
    const { user } = renderComponent()

    await user.click(screen.getByLabelText("Nach Entscheidung suchen"))
    await user.click(screen.getByLabelText("Treffer übernehmen"))

    expect(scrollIntoViewportByIdMock).toHaveBeenCalledWith(
      "periodical-references",
    )

    // onmounted, on search results and on validation error
    expect(scrollIntoViewportByIdMock).toHaveBeenCalledTimes(3)
  })

  test("adding a reference from a newly created documentation unit uses backend reference ID and builds citation", async () => {
    const { user, emitted } = renderComponent({
      modelValue: new Reference({
        citation: "3",
        referenceSupplement: "L",
        legalPeriodical: { uuid: "123", title: "Test Zeitschrift" },
      }),
    })
    await user.type(await screen.findByLabelText("Aktenzeichen"), "test")
    await user.click(screen.getByLabelText("Nach Entscheidung suchen"))
    await user.click(screen.getByLabelText("Dokumentationsstelle auswählen"))
    await user.click(screen.getByText("DS"))
    expect(screen.getByLabelText("Dokumentationsstelle auswählen")).toHaveValue(
      "DS",
    )

    const createNewButton = await screen.findByLabelText(
      "Dokumentationseinheit erstellen",
    )
    expect(createNewButton).toBeInTheDocument()
    vi.spyOn(documentUnitService, "createNew").mockResolvedValue({
      status: 200,
      data: new Decision("foo", {
        documentNumber: "1234567891234",
        coreData: {
          fileNumbers: ["AZ 123"],
          sources: [
            {
              value: SourceValue.Zeitschrift,
              reference: new Reference({
                id: "123",
                citation: "3",
                referenceSupplement: "L",
                legalPeriodical: { uuid: "123", title: "Test Zeitschrift" },
                legalPeriodicalRawValue: "A&G",
                referenceType: "caselaw",
                primaryReference: false,
              }),
            },
          ],
        },
      }),
    })
    window.open = vi.fn()

    await user.click(createNewButton)

    expect(emitted()["update:modelValue"]).toEqual(
      expect.arrayContaining([
        expect.arrayContaining([
          expect.objectContaining({
            id: "123",
            citation: "3",
            referenceSupplement: "L",
            referenceType: "caselaw",
            legalPeriodical: { uuid: "123", title: "Test Zeitschrift" },

            documentationUnit: expect.objectContaining({
              uuid: "foo",
              documentNumber: "1234567891234",
              fileNumber: "AZ 123",
              createdByReference: "123",
            }),
          }),
        ]),
      ]),
    )
    expect(emitted()).toHaveProperty("addEntry")
  })
})
