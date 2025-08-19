import { createTestingPinia } from "@pinia/testing"
import { fireEvent, render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import { createRouter, createWebHistory } from "vue-router"
import DecisionPublication from "@/components/publication/DecisionPublication.vue"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"
import routes from "~pages"

describe("DecisionPlausibilityCheck", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
    useFeatureToggleServiceMock()
  })
  it("should not show XML preview if plausibility check fails", async () => {
    await renderComponent({ hasPlausibilityCheckPassed: false })

    // Click simulates updating the plausibility check with mock
    await fireEvent.click(screen.getByText("DecisionPlausibilityCheck"))

    expect(screen.getByText("DecisionPlausibilityCheck")).toBeInTheDocument()
    expect(
      screen.getByText("PublicationActions - publishable: false"),
    ).toBeInTheDocument()
    expect(screen.queryByText("XML Vorschau")).not.toBeInTheDocument()
  })

  it("should render all child components when plausibility check is true", async () => {
    await renderComponent({ hasPlausibilityCheckPassed: true })

    // Click simulates updating the plausibility check with mock
    await fireEvent.click(screen.getByText("DecisionPlausibilityCheck"))

    expect(screen.getByText("DecisionPlausibilityCheck")).toBeInTheDocument()
    expect(
      screen.getByText("PublicationActions - publishable: true"),
    ).toBeInTheDocument()
    expect(screen.getByText("XML Vorschau")).toBeInTheDocument()
  })
})

const DecisionPlausibilityCheck = (hasPlausibilityCheckPassed: boolean) => ({
  template: `<span @click="$emit('update-plausibility-check', ${hasPlausibilityCheckPassed})">DecisionPlausibilityCheck</span>`,
})
const PublicationActions = {
  props: ["isPublishable"],
  template: `<span>PublicationActions - publishable: {{ isPublishable }}</span>`,
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
  await router.push({
    name: "caselaw-documentUnit-documentNumber-publication",
    params: { documentNumber: "KORE123412345" },
  })
  return {
    router,
    ...render(DecisionPublication, {
      global: {
        stubs: {
          DecisionPlausibilityCheck: DecisionPlausibilityCheck(
            hasPlausibilityCheckPassed,
          ),
          PublicationActions,
        },
        plugins: [router],
      },
    }),
  }
}
