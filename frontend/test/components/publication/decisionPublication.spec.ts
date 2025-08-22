import { createTestingPinia } from "@pinia/testing"
import { fireEvent, render, screen } from "@testing-library/vue"
import { setActivePinia, Store } from "pinia"
import { afterEach, expect } from "vitest"
import { Ref } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import DecisionPublication from "@/components/publication/DecisionPublication.vue"
import { Decision } from "@/domain/decision"
import publishDocumentationUnitService from "@/services/publishDocumentationUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"
import routes from "~pages"

const previewMock = vi.spyOn(publishDocumentationUnitService, "getPreview")

describe("DecisionPlausibilityCheck", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
    useFeatureToggleServiceMock()
    previewMock.mockResolvedValue({
      status: 200,
      data: {
        ldml: "ldml",
        success: true,
      },
    })
  })
  afterEach(() => {
    vi.resetAllMocks()
  })

  it("should render all child components when plausibility check is true", async () => {
    await renderComponent(validDocument)

    expect(
      screen.getByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
    ).toBeInTheDocument()
    expect(
      screen.getByText("PublicationActions - publishable: true"),
    ).toBeInTheDocument()
    expect(await screen.findByText("LDML Vorschau")).toBeInTheDocument()
    expect(previewMock).toHaveBeenCalled()
  })

  describe("ldml preview", () => {
    it("should display ldml preview whit plausible data", async () => {
      await renderComponent(validDocument)

      expect(await screen.findByText("LDML Vorschau")).toBeInTheDocument()

      // Expand preview
      await fireEvent.click(screen.getByLabelText("Aufklappen"))
      expect(await screen.findByTestId("code-snippet")).toBeInTheDocument()
    })

    it("should not display ldml preview with implausible data", async () => {
      await renderComponent(invalidDocument)

      expect(screen.queryByText("LDML Vorschau")).not.toBeInTheDocument()
      expect(previewMock).not.toHaveBeenCalled()
    })

    it("should show error when ldml preview cannot be loaded", async () => {
      const description =
        "Die LDML-Vorschau konnte nicht geladen werden: Aktuelle Fehlermeldung."
      previewMock.mockResolvedValue({
        status: 422,
        error: {
          title: "Fehler beim Laden der LDML-Vorschau",
          description: description,
        },
      })
      await renderComponent(validDocument)

      expect(await screen.findByText(description)).toBeInTheDocument()
    })
  })
})

const PublicationActions = {
  props: ["isPublishable"],
  template: `<span>PublicationActions - publishable: {{ isPublishable }}</span>`,
}
const invalidDocument = new Decision("q834", {
  documentNumber: "original",
})
const validDocument = new Decision("q834", {
  documentNumber: "original",
  coreData: {
    fileNumbers: ["IZ 1234"],
    court: { label: "BGH" },
    decisionDate: "2024-01-01",
    documentType: { label: "Urteil", jurisShortcut: "U" },
    legalEffect: "unbestimmt",
  },
})
function mockDocUnitStore(document: Decision) {
  const mockedSessionStore = useDocumentUnitStore()
  mockedSessionStore.documentUnit = document
  return mockedSessionStore
}
async function renderComponent(document: Decision) {
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  const store = mockDocUnitStore(document)
  await router.push({
    name: "caselaw-documentUnit-documentNumber-publication",
    params: { documentNumber: "KORE123412345" },
  })
  render(DecisionPublication, {
    global: {
      stubs: {
        PublicationActions,
      },
      plugins: [router],
    },
  })
  return store as Store<
    "docunitStore",
    {
      documentUnit: Ref<Decision>
    }
  >
}
