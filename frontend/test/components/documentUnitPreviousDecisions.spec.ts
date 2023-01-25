import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DocumentUnitPreviousDecisions from "@/components/DocumentUnitPreviousDecisions.vue"
import type { PreviousDecision } from "@/domain/documentUnit"

function renderComponent(options?: { modelValue?: PreviousDecision[] }) {
  const props = { modelValue: options?.modelValue }
  const utils = render(DocumentUnitPreviousDecisions, { props })
  const user = userEvent.setup()
  return { user, ...utils }
}

describe("DocumentUnitPreviousDecisions", async () => {
  global.ResizeObserver = require("resize-observer-polyfill")
  it("shows all necessary input fields with their value", () => {
    const modelValue = [
      {
        courtType: "type one",
        courtPlace: "location one",
        date: "",
        fileNumber: "identifier one",
      },
      {
        courtType: "type two",
        courtPlace: "location two",
        date: "",
        fileNumber: "identifier two",
      },
    ]
    renderComponent({ modelValue })

    expect(screen.getByDisplayValue("type one")).toBeInTheDocument()
    expect(screen.getByDisplayValue("location one")).toBeInTheDocument()
    expect(screen.getByDisplayValue("identifier one")).toBeInTheDocument()
    expect(screen.getByDisplayValue("type two")).toBeInTheDocument()
    expect(screen.getByDisplayValue("location two")).toBeInTheDocument()
    expect(screen.getByDisplayValue("identifier two")).toBeInTheDocument()
  })

  it("emits update model value event when input value changes", async () => {
    const modelValue = [
      {
        courtType: "ab",
        courtPlace: "test location",
        date: "test date",
        fileNumber: "test identifier",
      },
    ]
    const { emitted, user } = renderComponent({ modelValue })
    const input = screen.getByDisplayValue("ab")
    await user.type(input, "c")
    await userEvent.tab()

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
    const { emitted, user } = renderComponent({
      modelValue: undefined,
    })
    const input = screen.getByLabelText("Gerichtstyp Rechtszug")

    // Do anything without changing the inputs.
    await user.click(input)

    expect(emitted()["update:modelValue"]).toBeUndefined()
  })

  it("always shows at least one input group despite empty model list", () => {
    renderComponent({ modelValue: [] })

    const typeInput = screen.queryByLabelText("Gerichtstyp Rechtszug")
    const locationInput = screen.queryByLabelText("Gerichtsort Rechtszug")
    const dateInput = screen.queryByLabelText("Datum Rechtszug")
    const identifierInput = screen.queryByLabelText("Aktenzeichen Rechtszug")

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
