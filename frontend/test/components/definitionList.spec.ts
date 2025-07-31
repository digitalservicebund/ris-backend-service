import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DefinitionList from "@/components/DefinitionList.vue"
import { Decision } from "@/domain/decision"
import Definition from "@/domain/definition"
import routes from "~/test-helper/routes"

function renderComponent(definitions?: Definition[]) {
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })

  return {
    ...render(DefinitionList, {
      props: { label: "Defintion" },
      global: {
        plugins: [
          [
            createTestingPinia({
              initialState: {
                docunitStore: {
                  documentUnit: new Decision("foo", {
                    contentRelatedIndexing: { definitions },
                    managementData: {
                      borderNumbers: ["1", "2"],
                      duplicateRelations: [],
                    },
                  }),
                },
              },
              stubActions: false,
            }),
          ],
          [router],
        ],
      },
    }),
  }
}

describe("DefinitionList", () => {
  beforeEach(() => {
    window.HTMLElement.prototype.scrollIntoView = vi.fn()
  })
  test("Without data directly renders inputs with disabled save button", async () => {
    renderComponent()
    expect(
      screen.getByRole("textbox", { name: /definierter begriff/i }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole("spinbutton", { name: /definition des begriffs/i }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole("button", { name: /definition speichern/i }),
    ).toBeDisabled()
  })

  test("Definierter Begriff is required", async () => {
    renderComponent()
    await userEvent.type(
      screen.getByRole("spinbutton", { name: /definition des begriffs/i }),
      "1",
    )
    await userEvent.click(
      screen.getByRole("button", { name: /definition speichern/i }),
    )

    expect(screen.getByText("Pflichtfeld nicht befüllt")).toBeInTheDocument()
    expect(
      screen.getByRole("button", { name: /definition speichern/i }),
    ).toBeDisabled()
  })

  test("Randnummer only allows number input", async () => {
    renderComponent()
    await userEvent.type(
      screen.getByRole("spinbutton", { name: /definition des begriffs/i }),
      "abc",
    )
    await userEvent.click(
      screen.getByRole("button", { name: /definition speichern/i }),
    )

    expect(
      screen.getByRole("spinbutton", { name: /definition des begriffs/i }),
    ).toHaveValue("")
  })

  test("Only valid Randnummer is allowed", async () => {
    renderComponent()
    await userEvent.type(
      screen.getByRole("spinbutton", { name: /definition des begriffs/i }),
      "3",
    )
    await userEvent.click(
      screen.getByRole("button", { name: /definition speichern/i }),
    )

    expect(screen.getByText("Randnummer existiert nicht")).toBeInTheDocument()
  })

  test("Valid Randnummer can be saved", async () => {
    renderComponent()

    await userEvent.type(
      screen.getByRole("textbox", { name: /definierter begriff/i }),
      "abc",
    )
    await userEvent.type(
      screen.getByRole("spinbutton", { name: /definition des begriffs/i }),
      "2",
    )
    await userEvent.click(
      screen.getByRole("button", { name: /definition speichern/i }),
    )

    expect(screen.getAllByLabelText("Listen Eintrag")[0]).toHaveTextContent(
      "abc | 2",
    )
  })

  test("Randnummer is optional", async () => {
    renderComponent()
    await userEvent.type(
      screen.getByRole("textbox", { name: /definierter begriff/i }),
      "abc",
    )
    await userEvent.click(
      screen.getByRole("button", { name: /definition speichern/i }),
    )

    // The list entry was added
    expect(screen.getByText("abc")).toBeInTheDocument()
    // When the entry is saved, the input fields should be cleared
    expect(
      screen.getByRole("textbox", { name: /definierter begriff/i }),
    ).toHaveValue("")
    expect(
      screen.getByRole("spinbutton", { name: /definition des begriffs/i }),
    ).toHaveValue("")
  })

  test("Existing data can be edited", async () => {
    renderComponent([
      new Definition({ definedTerm: "abc", definingBorderNumber: 2 }),
      new Definition({ definedTerm: "def", definingBorderNumber: 3 }),
    ])

    expect(screen.getAllByLabelText("Listen Eintrag")[0]).toHaveTextContent(
      "abc | 2",
    )
    expect(screen.getAllByLabelText("Listen Eintrag")[1]).toHaveTextContent(
      "def | 3",
    )

    // Edit second entry
    await userEvent.click(screen.getByTestId("list-entry-1"))

    await userEvent.clear(
      screen.getByRole("textbox", { name: /definierter begriff/i }),
    )
    await userEvent.type(
      screen.getByRole("textbox", { name: /definierter begriff/i }),
      "ghi",
    )

    await userEvent.dblClick(
      screen.getByRole("spinbutton", { name: /definition des begriffs/i }),
    )
    await userEvent.keyboard("1")

    await userEvent.click(
      screen.getByRole("button", { name: /definition speichern/i }),
    )

    // The list entry was added
    expect(screen.getAllByLabelText("Listen Eintrag")[1]).toHaveTextContent(
      "ghi | 1",
    )
    // When the entry is saved, the input fields should be cleared
    expect(
      screen.getByRole("textbox", { name: /definierter begriff/i }),
    ).toHaveValue("")
    expect(
      screen.getByRole("spinbutton", { name: /definition des begriffs/i }),
    ).toHaveValue("")
  })
})
