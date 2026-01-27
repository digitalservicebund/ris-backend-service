import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import PendingProceedingCategories from "@/components/PendingProceedingCategories.vue"
import PendingProceeding from "@/domain/pendingProceeding"
import routes from "~/test-helper/routes"

function renderComponent() {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(PendingProceedingCategories, {
      props: {
        registerTextEditorRef: vi.fn(),
      },
      global: {
        plugins: [
          createTestingPinia({
            initialState: {
              session: { user: { internal: true } },
              docunitStore: {
                documentUnit: new PendingProceeding("foo", {
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

describe("Pending Proceeding Categories", () => {
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
      screen.getByRole("heading", { name: "Kurztexte" }),
    ).toBeInTheDocument()
  })
})
