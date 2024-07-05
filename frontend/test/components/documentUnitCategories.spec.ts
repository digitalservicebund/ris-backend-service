import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import featureToggleService from "@/services/featureToggleService"

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
        showNavigationPanel: true,
        documentUnit: new DocumentUnit("foo", {
          documentNumber: "1234567891234",
          coreData: {},
          texts: {},
          previousDecisions: undefined,
          ensuingDecisions: undefined,
          contentRelatedIndexing: {},
        }),
      },
      global: {
        plugins: [
          [router],

          [
            createTestingPinia({
              initialState: {
                docunitStore: {
                  documentUnit: new DocumentUnit("foo", {
                    documentNumber: "1234567891234",
                    coreData: {},
                    texts: {},
                    previousDecisions: undefined,
                    ensuingDecisions: undefined,
                    contentRelatedIndexing: {},
                  }),
                },
              },
            }),
          ],
        ],
      },
    }),
  }
}
describe("Document Unit Categories", () => {
  vi.spyOn(documentUnitService, "update").mockImplementation(() =>
    Promise.resolve({
      status: 200,
      data: {
        patch: "",
        errorPaths: [],
        documentationUnitVersion: 0,
        errors: [],
      },
    }),
  )

  // Enable feature flag "neuris.note"
  vi.spyOn(featureToggleService, "isEnabled").mockResolvedValue({
    status: 200,
    data: true,
  })

  test("renders correctly", async () => {
    renderComponent()

    expect(
      screen.getByRole("heading", { name: "Stammdaten" }),
    ).toBeInTheDocument()

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

  // Todo repair this test
  // test("updates core data", async () => {
  //   const court: Court = {
  //     type: "AG",
  //     location: "Test",
  //     label: "AG Test",
  //   }

  //   const dropdownCourtItems: ComboboxItem[] = [
  //     {
  //       label: court.label,
  //       value: court,
  //       additionalInformation: court.revoked,
  //     },
  //   ]

  //   vi.spyOn(comboboxItemService, "getCourts").mockImplementation(() =>
  //     Promise.resolve({ status: 200, data: dropdownCourtItems }),
  //   )
  //   const { user } = renderComponent()

  //   const coreDataCourt = within(
  //     screen.getByLabelText("Stammdaten", { selector: "div" }),
  //   ).getByLabelText("Gericht")
  //   await user.type(coreDataCourt, "AG")

  //   const dropdownItems = screen.getAllByLabelText("dropdown-option")
  //   expect(dropdownItems[0]).toHaveTextContent("AG Test")
  //   await user.click(dropdownItems[0])

  //   expect(screen.getByText(/AG Test/)).toBeVisible()
  // })
})
