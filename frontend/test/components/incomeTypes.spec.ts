import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import IncomeTypes from "@/components/IncomeTypes.vue"
import { Decision } from "@/domain/decision"
import IncomeType, { TypeOfIncome } from "@/domain/incomeType"
import routes from "~/test-helper/routes"

function renderComponent(incomeTypes?: IncomeType[]) {
  const user = userEvent.setup()
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(IncomeTypes, {
      props: { label: "Einkunftsart" },
      global: {
        plugins: [
          [
            createTestingPinia({
              initialState: {
                docunitStore: {
                  documentUnit: new Decision("foo", {
                    contentRelatedIndexing: { incomeTypes: incomeTypes },
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

describe("income types", () => {
  beforeEach(() => {
    window.HTMLElement.prototype.scrollIntoView = vi.fn()
  })
  test("disables save button without data", async () => {
    renderComponent()
    expect(
      screen.getByRole("combobox", { name: "Bitte auswählen" }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole("textbox", { name: "Begrifflichkeit" }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole("button", { name: "Einkunftsart speichern" }),
    ).toBeDisabled()
  })

  test("enables save button if type of income is filled", async () => {
    const { user } = renderComponent()
    expect(
      screen.getByRole("button", { name: "Einkunftsart speichern" }),
    ).toBeDisabled()

    await user.click(screen.getByRole("combobox", { name: "Bitte auswählen" }))
    expect(
      screen.getByLabelText("Land- und Forstwirtschaft"),
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Gewerbebetrieb")).toBeInTheDocument()
    expect(screen.getByLabelText("Selbständige Arbeit")).toBeInTheDocument()
    expect(
      screen.getByLabelText("Nichtselbständige Arbeit"),
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Kapitalvermögen")).toBeInTheDocument()
    expect(
      screen.getByLabelText("Vermietung und Verpachtung"),
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Sonstige Einkünfte")).toBeInTheDocument()
    expect(screen.getByLabelText("EStG")).toBeInTheDocument()
    expect(screen.getByLabelText("GewStG")).toBeInTheDocument()
    expect(screen.getByLabelText("UStG")).toBeInTheDocument()
    await user.click(
      screen.getByRole("option", { name: "Land- und Forstwirtschaft" }),
    )

    expect(
      screen.getByRole("button", { name: "Einkunftsart speichern" }),
    ).toBeEnabled()
  })

  test("enables save button if terminology is filled", async () => {
    const { user } = renderComponent()
    expect(
      screen.getByRole("button", { name: "Einkunftsart speichern" }),
    ).toBeDisabled()

    await user.type(
      screen.getByRole("textbox", { name: "Begrifflichkeit" }),
      "foo",
    )

    expect(
      screen.getByRole("button", { name: "Einkunftsart speichern" }),
    ).toBeEnabled()
  })

  test("add new entry", async () => {
    const { user } = renderComponent()
    expect(
      screen.getByRole("button", { name: "Einkunftsart speichern" }),
    ).toBeDisabled()
    await user.click(screen.getByRole("combobox", { name: "Bitte auswählen" }))
    await user.click(screen.getByRole("option", { name: "Gewerbebetrieb" }))
    await user.type(
      screen.getByRole("textbox", { name: "Begrifflichkeit" }),
      "foo",
    )

    await user.click(
      screen.getByRole("button", { name: "Einkunftsart speichern" }),
    )

    expect(screen.getAllByLabelText("Listen Eintrag")[0]).toHaveTextContent(
      "Gewerbebetrieb, foo",
    )
  })

  test("edit entry", async () => {
    const { user } = renderComponent([
      new IncomeType({
        terminology: "foo",
        typeOfIncome: TypeOfIncome.GEWERBEBETRIEB,
      }),
      new IncomeType({
        terminology: "bar",
        typeOfIncome: TypeOfIncome.ESTG,
      }),
    ])

    expect(screen.getAllByLabelText("Listen Eintrag")[0]).toHaveTextContent(
      "Gewerbebetrieb, foo",
    )
    expect(screen.getAllByLabelText("Listen Eintrag")[1]).toHaveTextContent(
      "EStG, bar",
    )

    await user.click(screen.getByTestId("list-entry-1"))
    await user.click(screen.getByRole("combobox", { name: "EStG" }))
    await user.click(screen.getByRole("option", { name: "UStG" }))

    await user.clear(screen.getByRole("textbox", { name: "Begrifflichkeit" }))
    await user.type(
      screen.getByRole("textbox", { name: "Begrifflichkeit" }),
      "baz",
    )
    await user.click(
      screen.getByRole("button", { name: "Einkunftsart speichern" }),
    )

    expect(screen.getAllByLabelText("Listen Eintrag")[1]).toHaveTextContent(
      "UStG, baz",
    )
  })

  test("delete entry", async () => {
    const { user } = renderComponent([
      new IncomeType({
        terminology: "foo",
        typeOfIncome: TypeOfIncome.GEWERBEBETRIEB,
      }),
      new IncomeType({
        terminology: "bar",
        typeOfIncome: TypeOfIncome.ESTG,
      }),
    ])

    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(2)
    expect(screen.getAllByLabelText("Listen Eintrag")[0]).toHaveTextContent(
      "Gewerbebetrieb, foo",
    )
    expect(screen.getAllByLabelText("Listen Eintrag")[1]).toHaveTextContent(
      "EStG, bar",
    )

    await user.click(screen.getByTestId("list-entry-1"))

    await user.click(screen.getByRole("button", { name: "Eintrag löschen" }))

    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })
})
