import { userEvent } from "@testing-library/user-event"
import { render, screen, within } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import { ComboboxItem } from "@/components/input/types"
import DocumentUnit, { Court } from "@/domain/documentUnit"
import comboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import featureToggleService from "@/services/featureToggleService"

function renderComponent() {
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
        validationErrors: [],
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
      }),
    }),
  )

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

  test("updates core data", async () => {
    const court: Court = {
      type: "AG",
      location: "Test",
      label: "AG Test",
    }

    const dropdownCourtItems: ComboboxItem[] = [
      {
        label: court.label,
        value: court,
        additionalInformation: court.revoked,
      },
    ]

    vi.spyOn(comboboxItemService, "getCourts").mockImplementation(() =>
      Promise.resolve({ status: 200, data: dropdownCourtItems }),
    )
    const { user, emitted } = renderComponent()

    const coreDataCourt = within(
      screen.getByLabelText("Stammdaten", { selector: "div" }),
    ).getByLabelText("Gericht")
    await user.type(coreDataCourt, "AG")

    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems[0]).toHaveTextContent("AG Test")
    await user.click(dropdownItems[0])

    // Event is emitted for saving the DocUnit with updated properties
    const [updatedDocUnit] = emitted().documentUnitUpdatedLocally[0] as [
      DocumentUnit,
    ]
    expect(updatedDocUnit.coreData.court).toEqual({
      label: "AG Test",
      location: "Test",
      type: "AG",
    })
  })
})
