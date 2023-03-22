import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DocumentUnitPreviousDecisions from "@/components/DocumentUnitProceedingDecisions.vue"
import type { ProceedingDecision } from "@/domain/documentUnit"

function renderComponent(options?: { modelValue?: ProceedingDecision[] }) {
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
        court: {
          type: "BGH",
          location: "Karlsruhe",
          label: "BGH Karlsruhe",
        },
        date: "2022-02-03",
        fileNumber: "fileNumber",
      },
      {
        court: undefined,
        date: undefined,
        fileNumber: undefined,
      },
    ]
    renderComponent({ modelValue })

    const courts = screen.getAllByLabelText(
      "Gericht Rechtszug"
    ) as HTMLInputElement[]

    const dates = screen.getAllByLabelText(
      "Datum Rechtszug"
    ) as HTMLInputElement[]

    const fileNumbers = screen.getAllByLabelText(
      "Aktenzeichen Rechtszug"
    ) as HTMLInputElement[]

    expect(courts).toHaveLength(2)
    expect(dates).toHaveLength(2)
    expect(fileNumbers).toHaveLength(2)

    expect(courts[0]).toHaveDisplayValue("BGH Karlsruhe")
    expect(dates[0]).toHaveDisplayValue("2022-02-03")
    expect(fileNumbers[0]).toHaveDisplayValue("fileNumber")
  })

  it("emits update model value event when input value changes", async () => {
    const { emitted, user } = renderComponent()
    const input = screen.getByLabelText("fileNumber")
    await user.type(input, "abc")
    await userEvent.tab()

    expect(emitted()["update:modelValue"]).toHaveLength(1)
    expect(emitted()["update:modelValue"][0]).toEqual([
      [
        {
          court: {
            type: "",
            location: "",
            label: "",
            revoked: "",
          },
          date: "",
          fileNumber: "abc",
        },
      ],
    ])
  })

  it("does not emit update model event when inputs are empty and model is empty too", async () => {
    const { emitted, user } = renderComponent({
      modelValue: undefined,
    })
    const input = screen.getByLabelText("fileNumber")

    // Do anything without changing the inputs.
    await user.click(input)

    expect(emitted()["update:modelValue"]).toBeUndefined()
  })

  it("always shows at least one input group despite empty model list", () => {
    renderComponent({ modelValue: [] })

    const courtInput = screen.queryByLabelText(
      "Gericht Rechtszug"
    ) as HTMLInputElement
    const dateInput = screen.queryByLabelText(
      "Datum Rechtszug"
    ) as HTMLInputElement
    const identifierInput = screen.queryByLabelText(
      "Aktenzeichen Rechtszug"
    ) as HTMLInputElement

    expect(courtInput).toBeInTheDocument()
    expect(courtInput).toHaveDisplayValue("")
    expect(dateInput).toBeInTheDocument()
    expect(dateInput).toHaveDisplayValue("")
    expect(identifierInput).toBeInTheDocument()
    expect(identifierInput).toHaveDisplayValue("")
  })
})
