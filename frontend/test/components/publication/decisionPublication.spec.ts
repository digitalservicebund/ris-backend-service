import { createTestingPinia } from "@pinia/testing"
import { fireEvent, render, screen } from "@testing-library/vue"
import { setActivePinia, Store } from "pinia"
import { Ref } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import DecisionPublication from "@/components/publication/DecisionPublication.vue"
import { Decision } from "@/domain/decision"
import publishDocumentationUnitService from "@/services/publishDocumentationUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"
import routes from "~pages"

describe("DecisionPlausibilityCheck", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
    useFeatureToggleServiceMock()
    vi.spyOn(publishDocumentationUnitService, "getPreview").mockResolvedValue({
      status: 200,
      data: {
        ldml: "ldml",
        success: true,
      },
    })
  })
  it("should not show LDML preview if plausibility check fails", async () => {
    await renderComponent({ isPlausibilityCheckValid: false })

    // Click simulates updating the plausibility check with mock
    await fireEvent.click(screen.getByText("DecisionPlausibilityCheck"))

    expect(screen.getByText("DecisionPlausibilityCheck")).toBeInTheDocument()
    expect(
      screen.getByText("PublicationActions - publishable: false"),
    ).toBeInTheDocument()
    expect(screen.queryByText("LDML Vorschau")).not.toBeInTheDocument()
  })

  it("should render all child components when plausibility check is true", async () => {
    await renderComponent({ isPlausibilityCheckValid: true })

    // Click simulates updating the plausibility check with mock
    await fireEvent.click(screen.getByText("DecisionPlausibilityCheck"))

    expect(screen.getByText("DecisionPlausibilityCheck")).toBeInTheDocument()
    expect(
      screen.getByText("PublicationActions - publishable: true"),
    ).toBeInTheDocument()
    expect(screen.getByText("LDML Vorschau")).toBeInTheDocument()
  })
})

const DecisionPlausibilityCheck = (isPlausibilityCheckValid: boolean) => ({
  template: `<span @click="$emit('update-plausibility-check', ${isPlausibilityCheckValid})">DecisionPlausibilityCheck</span>`,
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
  { isPlausibilityCheckValid }: { isPlausibilityCheckValid: boolean } = {
    isPlausibilityCheckValid: true,
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
          isPlausibilityCheckValid,
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
