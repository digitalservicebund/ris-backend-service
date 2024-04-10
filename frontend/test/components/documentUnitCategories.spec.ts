import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import DocumentUnit from "@/domain/documentUnit"

function renderComponent() {
  // eslint-disable-next-line testing-library/await-async-events
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
    ...render(DocumentUnitCategories, {
      props: {
        documentUnit: new DocumentUnit("foo", {
          documentNumber: "1234567891234",
          coreData: {},
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
describe("Document Unit Categories", () => {
  test("renders correctly", async () => {
    renderComponent()

    screen.getByText("Stammdaten")
    screen.getByText("Rechtszug")
    screen.getByText("Inhaltliche Erschließung")
    screen.getByText("Kurz- & Langtexte")
  })

  test("updates core data", async () => {})
})
