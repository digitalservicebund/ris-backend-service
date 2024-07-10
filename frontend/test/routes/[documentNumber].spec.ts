import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createHead } from "@unhead/vue"
import { createPinia, setActivePinia } from "pinia"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnit from "@/domain/documentUnit"
import categories from "@/routes/caselaw/documentUnit/[documentNumber]/categories.vue"
import DocumentNumber from "@/routes/caselaw/documentUnit/[documentNumber].vue"
import documentUnitService from "@/services/documentUnitService"
import featureToggleService from "@/services/featureToggleService"
import { ServiceResponse } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

function renderComponent() {
  const user = userEvent.setup()
  const head = createHead()
  mockDocumentUnitStore()

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
        component: categories,
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/files",
        name: "caselaw-documentUnit-documentNumber-files",
        component: {
          template: "<div data-testid='files'>Files</div>",
        },
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/publication",
        name: "caselaw-documentUnit-documentNumber-publication",
        component: {
          template: "<div data-testid='publication'>Publication</div>",
        },
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/preview",
        name: "caselaw-documentUnit-documentNumber-preview",
        component: {
          template: "<div data-testid='preview'>Preview</div>",
        },
      },
    ],
  })
  return {
    user,
    router,
    ...render(DocumentNumber, {
      props: {
        documentNumber: "1234567891234",
      },
      global: { plugins: [head, router] },
    }),
  }
}

function mockDocumentUnitStore() {
  const mockedDocumentUnitStore = useDocumentUnitStore()
  const documentUnit = new DocumentUnit("foo", {
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
  })
  mockedDocumentUnitStore.documentUnit = documentUnit

  return mockedDocumentUnitStore
}

describe("Document Number Route", () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.spyOn(documentUnitService, "update").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: { documentationUnitVersion: 1, patch: [], errorPaths: [] },
      }),
    )
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockImplementation(
      () =>
        Promise.resolve({
          status: 200,
          data: new DocumentUnit("foo", {
            documentNumber: "1234567891234",
          }),
        }),
    )
    vi.spyOn(featureToggleService, "isEnabled").mockResolvedValue({
      status: 200,
      data: true,
    })
  })

  describe("Conditional rendering", () => {
    test("should render categories with side panels and header", async () => {
      const { router, container } = renderComponent()

      await router.push({
        path: "/caselaw/documentUnit/1234567891234/categories",
      })

      expect(screen.getByTestId("side-toggle-navigation")).toBeInTheDocument()

      // Header with save button
      expect(screen.getByTestId("document-unit-info-panel")).toBeInTheDocument()
      expect(
        screen.getByRole("button", { name: "Speichern Button" }),
      ).toBeInTheDocument()

      // ExtraContentSidePanel
      expect(
        screen.getByRole("button", { name: "Seitenpanel öffnen" }),
      ).toBeInTheDocument()

      // Main route is rendered
      expect(container.querySelector("#coreData")).toBeInTheDocument()
    })

    test("should render documents with side panels and header", async () => {
      const { router } = renderComponent()

      await router.push({ path: "/caselaw/documentUnit/1234567891234/files" })

      expect(screen.getByTestId("side-toggle-navigation")).toBeInTheDocument()

      // Header with save button
      expect(screen.getByTestId("document-unit-info-panel")).toBeInTheDocument()
      expect(
        screen.queryByRole("button", { name: "Speichern Button" }),
      ).toBeInTheDocument()

      // ExtraContentSidePanel
      expect(
        screen.getByRole("button", { name: "Seitenpanel öffnen" }),
      ).toBeInTheDocument()

      // Main route is rendered
      expect(screen.getByTestId("files")).toBeInTheDocument()
    })

    test("should render publication with only nav side panel and header", async () => {
      const { router } = renderComponent()

      await router.push({
        path: "/caselaw/documentUnit/1234567891234/publication",
      })

      expect(screen.getByTestId("side-toggle-navigation")).toBeInTheDocument()

      // Header without save button
      expect(screen.getByTestId("document-unit-info-panel")).toBeInTheDocument()
      expect(
        screen.queryByRole("button", { name: "Speichern Button" }),
      ).not.toBeInTheDocument()

      // ExtraContentSidePanel
      expect(
        screen.queryByRole("button", { name: "Seitenpanel öffnen" }),
      ).not.toBeInTheDocument()

      // Main route is rendered
      expect(screen.getByTestId("publication")).toBeInTheDocument()
    })

    test("should render preview without side panels and header", async () => {
      const { router } = renderComponent()
      await router.push({ path: "/caselaw/documentUnit/1234567891234/preview" })

      expect(
        screen.queryByTestId("side-toggle-navigation"),
      ).not.toBeInTheDocument()

      // Header with save button
      expect(
        screen.queryByTestId("document-unit-info-panel"),
      ).not.toBeInTheDocument()
      expect(
        screen.queryByRole("button", { name: "Speichern Button" }),
      ).not.toBeInTheDocument()

      // ExtraContentSidePanel
      expect(
        screen.queryByRole("button", { name: "Seitenpanel öffnen" }),
      ).not.toBeInTheDocument()

      // Main route is rendered
      expect(screen.getByTestId("preview")).toBeInTheDocument()
    })

    test("should render error page without document", async () => {
      vi.spyOn(documentUnitService, "getByDocumentNumber").mockImplementation(
        () =>
          Promise.resolve({
            status: 404,
            data: undefined,
            error: { title: "Backend_Error_Title" },
          } as ServiceResponse<DocumentUnit>),
      )

      const { router } = renderComponent()
      await router.push({
        path: "/caselaw/documentUnit/1234567891234/categories",
      })

      // Navigation
      expect(
        screen.queryByTestId("side-toggle-navigation"),
      ).not.toBeInTheDocument()

      // Header with save button
      expect(
        screen.queryByTestId("document-unit-info-panel"),
      ).not.toBeInTheDocument()
      expect(
        screen.queryByRole("button", { name: "Speichern Button" }),
      ).not.toBeInTheDocument()

      // ExtraContentSidePanel
      expect(
        screen.queryByRole("button", { name: "Seitenpanel öffnen" }),
      ).not.toBeInTheDocument()

      // Error page is rendered
      expect(screen.getByText("Backend_Error_Title")).toBeInTheDocument()
    })
  })
})
