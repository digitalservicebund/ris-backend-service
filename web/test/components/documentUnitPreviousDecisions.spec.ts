import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import { createVuetify } from "vuetify"
import DocUnitPreviousDecisions from "@/components/DocumentUnitPreviousDecisions.vue"
import type { PreviousDecision } from "@/domain/documentUnit"

function renderComponent(options?: { modelValue?: PreviousDecision[] }) {
  const vuetify = createVuetify()
  const global = { plugins: [vuetify] }
  const props = { modelValue: options?.modelValue }
  const renderResult = render(DocUnitPreviousDecisions, { global, props })
  const user = userEvent.setup()
  return { user, ...renderResult }
}

describe("DocUnitPreviousDecisions", async () => {
  it("shows all necessary input fields with their value", () => {
    const modelValue = [
      {
        courtType: "type one",
        courtPlace: "location one",
        date: "date one",
        fileNumber: "identifier one",
      },
      {
        courtType: "type two",
        courtPlace: "location two",
        date: "date two",
        fileNumber: "identifier two",
      },
    ]
    const { queryByDisplayValue } = renderComponent({ modelValue })

    expect(queryByDisplayValue("type one")).toBeInTheDocument()
    expect(queryByDisplayValue("location one")).toBeInTheDocument()
    expect(queryByDisplayValue("date one")).toBeInTheDocument()
    expect(queryByDisplayValue("identifier one")).toBeInTheDocument()
    expect(queryByDisplayValue("type two")).toBeInTheDocument()
    expect(queryByDisplayValue("location two")).toBeInTheDocument()
    expect(queryByDisplayValue("date two")).toBeInTheDocument()
    expect(queryByDisplayValue("identifier two")).toBeInTheDocument()
  })

  it("emits update model value event when user types into input", async () => {
    const modelValue = [
      {
        courtType: "ab",
        courtPlace: "test location",
        date: "test date",
        fileNumber: "test identifier",
      },
    ]
    const { emitted, user, getByDisplayValue } = renderComponent({ modelValue })
    const input = getByDisplayValue("ab")

    await user.type(input, "c")

    expect(emitted()["update:modelValue"]).toHaveLength(1)
    expect(emitted()["update:modelValue"][0]).toEqual([
      [
        {
          courtType: "abc",
          courtPlace: "test location",
          date: "test date",
          fileNumber: "test identifier",
        },
      ],
    ])
  })

  it("does not emit update model event when inputs are empty and model is empty too", async () => {
    const { emitted, user, getByLabelText } = renderComponent({
      modelValue: undefined,
    })
    const input = getByLabelText("Gerichtstyp Rechtszug")

    // Do anything without changing the inputs.
    await user.click(input)

    expect(emitted()["update:modelValue"]).toBeUndefined()
  })

  it("always shows at least one input group despite empty model list", () => {
    const { queryByLabelText } = renderComponent({ modelValue: [] })

    const typeInput = queryByLabelText("Gerichtstyp Rechtszug")
    const locationInput = queryByLabelText("Gerichtsort Rechtszug")
    const dateInput = queryByLabelText("Datum Rechtszug")
    const identifierInput = queryByLabelText("Aktenzeichen Rechtszug")

    expect(typeInput).toBeInTheDocument()
    expect(typeInput).toHaveDisplayValue("")
    expect(locationInput).toBeInTheDocument()
    expect(locationInput).toHaveDisplayValue("")
    expect(dateInput).toBeInTheDocument()
    expect(dateInput).toHaveDisplayValue("")
    expect(identifierInput).toBeInTheDocument()
    expect(identifierInput).toHaveDisplayValue("")
  })
})
