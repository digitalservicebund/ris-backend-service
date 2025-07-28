import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import { Decision } from "@/domain/decision"
import { onSearchShortcutDirective } from "@/utils/onSearchShortcutDirective"
import routes from "~/test-helper/routes"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

useFeatureToggleServiceMock()

function renderComponent() {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(DocumentUnitCategories, {
      props: {
        registerTextEditorRef: vi.fn(),
      },
      global: {
        directives: { "ctrl-enter": onSearchShortcutDirective },
        plugins: [
          createTestingPinia({
            initialState: {
              session: { user: { roles: ["Internal"] } },
              docunitStore: {
                documentUnit: new Decision("foo", {
                  documentNumber: "1234567891234",
                }),
              },
            },
          }),
          [router],
        ],
      },
    }),
  }
}

describe("Document Unit Categories", () => {
  test("renders all categories", async () => {
    renderComponent()

    expect(screen.getByRole("heading", { name: "Formaldaten" })).toBeVisible()

    expect(
      screen.getByRole("heading", { name: "Rechtszug" }),
    ).toBeInTheDocument()

    expect(
      screen.getByRole("heading", { name: "Inhaltliche Erschließung" }),
    ).toBeInTheDocument()

    expect(
      screen.getByRole("heading", { name: "Kurz- & Langtexte" }),
    ).toBeInTheDocument()
  })
})
