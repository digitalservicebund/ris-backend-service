import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createHead } from "@unhead/vue/client"
import { createRouter, createWebHistory } from "vue-router"
import PendingProceeding from "@/domain/pendingProceeding"
import categories from "@/routes/caselaw/pending-proceeding/[documentNumber]/categories.vue"
import DocumentNumber from "@/routes/caselaw/pending-proceeding/[documentNumber].vue"
import documentUnitService from "@/services/documentUnitService"
import featureToggleService from "@/services/featureToggleService"
import { ServiceResponse } from "@/services/httpClient"

function renderComponent() {
  const user = userEvent.setup()
  const head = createHead()
  vi.mock("primevue/usetoast", () => ({
    useToast: () => ({ add: vi.fn() }),
  }))
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "/",
        name: "home",
        component: {},
      },
      {
        path: "/caselaw/pendingProceeding/:documentNumber/categories",
        name: "caselaw-pending-proceeding-documentNumber-categories",
        component: categories,
      },
      {
        path: "/caselaw/pendingProceeding/:documentNumber/preview",
        name: "caselaw-pending-proceeding-documentNumber-preview",
        component: {
          template: "<div data-testid='preview'>Preview</div>",
        },
      },
      {
        path: "/caselaw/pendingProceeding/:documentNumber/references",
        name: "caselaw-pending-proceeding-documentNumber-references",
        component: {
          template:
            "<div data-testid='references'>Fundstellen<input aria-label=\"Fundstellen\"/></div>",
        },
      },
      {
        path: "/caselaw/pendingProceeding/:documentNumber/managementdata",
        name: "caselaw-pending-proceeding-documentNumber-managementdata",
        component: {
          template:
            "<div data-testid='managementdata'>Verwaltungsdaten<input aria-label=\"Verwaltungsdaten\"/></div>",
        },
      },
      {
        path: "/caselaw/pendingProceeding/:documentNumber/publication",
        name: "caselaw-pending-proceeding-documentNumber-publication",
        component: {
          template:
            "<div data-testid='publication'>Veröffentlichen<input aria-label=\"Veröffentlichen\"/></div>",
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
      global: {
        plugins: [
          head,
          router,
          [
            createTestingPinia({
              initialState: {
                session: { user: { internal: true } },
                docunitStore: {
                  documentUnit: new PendingProceeding("foo", {
                    documentNumber: "1234567891234",
                    coreData: {
                      court: {
                        type: "AG",
                        location: "Test",
                        label: "AG Test",
                      },
                    },
                  }),
                },
              },
              stubActions: false,
            }),
          ],
        ],
      },
    }),
  }
}

describe("Document Number Route", () => {
  beforeEach(() => {
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
          data: new PendingProceeding("foo", {
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
        path: "/caselaw/pendingProceeding/1234567891234/categories",
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

    test("should render references with side panels and header", async () => {
      const { router } = renderComponent()

      await router.push({
        path: "/caselaw/pendingProceeding/1234567891234/references",
      })

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

      // route is rendered
      expect(screen.getByTestId("references")).toBeInTheDocument()
    })

    test("should render management data side panels and header", async () => {
      const { router } = renderComponent()

      await router.push({
        path: "/caselaw/pendingProceeding/1234567891234/managementdata",
      })

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

      // route is rendered
      expect(screen.getByTestId("managementdata")).toBeInTheDocument()
    })

    test("should render preview without side panel and header", async () => {
      const { router } = renderComponent()
      await router.push({
        path: "/caselaw/pendingProceeding/1234567891234/preview",
      })

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
          } as ServiceResponse<PendingProceeding>),
      )

      const { router } = renderComponent()
      await router.push({
        path: "/caselaw/pendingProceeding/1234567891234/categories",
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

  describe("Shortcuts", () => {
    it('detects "v" keypress and opens preview', async () => {
      const { user, router } = renderComponent()
      await router.push({
        path: "/caselaw/pendingProceeding/1234567891234/categories?showAttachmentPanel=false",
      })
      await user.keyboard("r") // is needed as preview is displayed as default for pending proceedings

      expect(screen.queryByTestId("preview")).not.toBeInTheDocument()
      await user.keyboard("v")
      expect(screen.getByTestId("preview")).toBeInTheDocument()
    })

    it('detects "r" keypress and opens category import', async () => {
      const { user, router } = renderComponent()
      await router.push({
        path: "/caselaw/pendingProceeding/1234567891234/categories?showAttachmentPanel=false",
      })

      expect(screen.queryByTestId("category-import")).not.toBeInTheDocument()
      await user.keyboard("r")
      expect(screen.getByTestId("category-import")).toBeInTheDocument()
    })

    it('detects ">" keypress and opens both panels', async () => {
      const { user, router } = renderComponent()
      await router.push({
        path: "/caselaw/pendingProceeding/1234567891234/categories?showAttachmentPanel=false",
      })

      expect(
        screen.queryByLabelText("Seitenpanel schließen"),
      ).not.toBeInTheDocument()
      await user.keyboard("<")
      expect(screen.getByLabelText("Seitenpanel schließen")).toBeVisible()
      await user.keyboard("<")
      expect(
        screen.queryByLabelText("Seitenpanel schließen"),
      ).not.toBeInTheDocument()
    })
  })
})
