import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitAttachments from "@/components/DocumentUnitAttachments.vue"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"

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
    ...render(DocumentUnitAttachments, {
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
  vi.spyOn(documentUnitService, "update").mockImplementation(() =>
    Promise.resolve({
      status: 200,
      data: new DocumentUnit("foo", {
        documentNumber: "1234567891234",

        texts: {},
        previousDecisions: undefined,
        ensuingDecisions: undefined,
        contentRelatedIndexing: {},
      }),
    }),
  )
  test("renders correctly without attachments", async () => {
    renderComponent()

    expect(screen.getByTestId("title")).toBeVisible()
    expect(screen.getByTestId("title")).toBeVisible()
  })
})
