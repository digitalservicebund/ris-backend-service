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

const getPreviewMock = vi.spyOn(publishDocumentationUnitService, "getPreview")

describe("DecisionPlausibilityCheck", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
    useFeatureToggleServiceMock()
  })
  afterEach(() => {
    vi.resetAllMocks()
  })

  it("should not show LDML preview if plausibility check fails", async () => {
    await renderComponent({ hasPlausibilityCheckPassed: false })

    // Click simulates updating the plausibility check with mock
    await fireEvent.click(screen.getByText("DecisionPlausibilityCheck"))

    expect(screen.getByText("DecisionPlausibilityCheck")).toBeInTheDocument()
    expect(
      screen.getByText("PublicationActions - publishable: false"),
    ).toBeInTheDocument()
    expect(screen.queryByText("LDML Vorschau")).not.toBeInTheDocument()
    expect(getPreviewMock).not.toHaveBeenCalled()
  })

  it("should render all child components when plausibility check is true", async () => {
    getPreviewMock.mockResolvedValue({
      status: 200,
      data: {
        ldml: "ldml",
        success: true,
      },
    })
    await renderComponent({ hasPlausibilityCheckPassed: true })

    // Click simulates updating the plausibility check with mock
    await fireEvent.click(screen.getByText("DecisionPlausibilityCheck"))

    expect(screen.getByText("DecisionPlausibilityCheck")).toBeInTheDocument()
    expect(
      screen.getByText("PublicationActions - publishable: true"),
    ).toBeInTheDocument()
    expect(screen.getByText("LDML Vorschau")).toBeInTheDocument()
  })
  describe("ldml preview", () => {
    it("should display ldml preview whit plausible data", async () => {
      getPreviewMock.mockResolvedValue({
        status: 200,
        data: {
          ldml: "ldml",
          success: true,
        },
      })
      await renderComponent({ hasPlausibilityCheckPassed: true })

      // Click simulates updating the plausibility check with mock
      await fireEvent.click(screen.getByText("DecisionPlausibilityCheck"))
      // Expand preview
      await fireEvent.click(screen.getByLabelText("Aufklappen"))
      expect(screen.getByTestId("code-snippet")).toBeInTheDocument()
    })

    it("should not display ldml preview with implausible data", async () => {
      await renderComponent({ hasPlausibilityCheckPassed: false })

      expect(screen.queryByText("LDML Vorschau")).not.toBeInTheDocument()
      expect(getPreviewMock).not.toHaveBeenCalled()
    })

    it("should show error when ldml preview cannot be loaded", async () => {
      const description =
        "Die LDML-Vorschau konnte nicht geladen werden: Aktuelle Fehlermeldung."
      getPreviewMock.mockResolvedValue({
        status: 422,
        error: {
          title: "Fehler beim Laden der LDML-Vorschau",
          description: description,
        },
      })
      await renderComponent({ hasPlausibilityCheckPassed: true })

      // Click simulates updating the plausibility check with mock
      await fireEvent.click(screen.getByText("DecisionPlausibilityCheck"))

      expect(screen.getByText(description)).toBeInTheDocument()
    })
  })
})

const DecisionPlausibilityCheck = (hasPlausibilityCheckPassed: boolean) => ({
  template: `<span @click="$emit('update-plausibility-check', ${hasPlausibilityCheckPassed})">DecisionPlausibilityCheck</span>`,
})
const PublicationActions = {
  props: ["isPublishable"],
  template: `<span>PublicationActions - publishable: {{ isPublishable }}</span>`,
}
function mockDocUnitStore() {
  const mockedSessionStore = useDocumentUnitStore()
  mockedSessionStore.documentUnit = new Decision("q834", {
    documentNumber: "original",
  })

  return mockedSessionStore
}
async function renderComponent(
  { hasPlausibilityCheckPassed }: { hasPlausibilityCheckPassed: boolean } = {
    hasPlausibilityCheckPassed: true,
  },
) {
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  const store = mockDocUnitStore()
  await router.push({
    name: "caselaw-documentUnit-documentNumber-publication",
    params: { documentNumber: "KORE123412345" },
  })
  render(DecisionPublication, {
    global: {
      stubs: {
        DecisionPlausibilityCheck: DecisionPlausibilityCheck(
          hasPlausibilityCheckPassed,
        ),
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
