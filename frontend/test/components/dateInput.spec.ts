import userEvent from "@testing-library/user-event"
import { fireEvent, render, screen } from "@testing-library/vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import { ValidationError } from "@/shared/components/input/types"

function renderComponent(options?: {
  id?: string
  ariaLabel?: string
  isFutureDate?: boolean
  value?: string
  modelValue?: string
  placeholder?: string
  validationError?: ValidationError
}) {
  const props = {
    id: options?.id ?? "identifier",
    value: options?.value,
    modelValue: options?.modelValue,
    ariaLabel: options?.ariaLabel ?? "aria-label",
    isFutureDate: options?.isFutureDate ?? false,
    placeholder: options?.placeholder,
    validationError: options?.validationError,
  }
  return render(DateInput, { props })
}

const mockValidationError: ValidationError = {
  defaultMessage: "wrong date",
  field: "coreData.decisionDate",
}

describe("DateInput", () => {
  it("shows an date input element", () => {
    renderComponent()
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement

    expect(input).toBeInTheDocument()
    expect(input?.type).toBe("date")
  })

  it("shows input with an aria label", () => {
    renderComponent({
      ariaLabel: "test-label",
    })
    const input = screen.queryByLabelText("test-label") as HTMLInputElement

    expect(input).toBeInTheDocument()
  })

  it("allows to type date inside input", async () => {
    renderComponent({ modelValue: "2022-02-03" })
    const input = screen.getByLabelText("aria-label") as HTMLInputElement

    expect(input).toHaveValue("2022-02-03")

    await userEvent.clear(input)
    await userEvent.type(input, "2020-05-12")

    expect(input).toHaveValue("2020-05-12")
  })

  it("emits model update event when input changes", async () => {
    const { emitted } = renderComponent()
    const input = screen.getByLabelText("aria-label") as HTMLInputElement

    await userEvent.type(input, "2020-05-12")

    expect(input).toHaveValue("2020-05-12")
    expect(emitted("update:modelValue")).toHaveLength(2)
    expect(emitted("update:modelValue")[1]).toEqual(["2020-05-12"])
  })

  it("clears input and emits undefined on backspace delete", async () => {
    const { emitted } = renderComponent({
      value: "2022-02-03",
    })
    const input = screen.getByLabelText("aria-label") as HTMLInputElement

    await userEvent.type(input, "{backspace}")

    expect(input).toHaveValue("")
    expect(emitted("update:modelValue")).toEqual([["2022-02-03"], [undefined]])
  })

  it("does not allow dates in the future", async () => {
    const { emitted } = renderComponent({
      id: "test-id",
      value: "2099-02-10",
      ariaLabel: "Testdatum",
    })

    expect(emitted("update:modelValue")).toBeUndefined()
    expect(emitted("update:validationError")).toEqual([
      [
        {
          defaultMessage: "Das Testdatum darf nicht in der Zukunft liegen",
          field: "test-id",
        },
      ],
    ])
  })

  it("it allows dates in the future if flag is set", async () => {
    const { emitted } = renderComponent({ isFutureDate: true })
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement

    await userEvent.type(input, "2024-02-10")

    expect(emitted("update:modelValue")).toEqual([[undefined], ["2024-02-10"]])
    expect(emitted("update:validationError")).toEqual([
      [undefined],
      [undefined],
      [undefined],
      [undefined],
    ])
  })

  it("does not allow invalid dates", async () => {
    const { emitted } = renderComponent({ id: "test-id" })
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement

    Object.defineProperty(input, "target", {
      value: "2020-02-31",
    })
    await fireEvent.update(input)

    expect(emitted("update:modelValue")).toEqual([[undefined], [undefined]])
    expect(emitted("update:validationError")).toEqual([
      [undefined],
      [undefined],
      [
        {
          defaultMessage: "Kein valides Datum",
          field: "test-id",
        },
      ],
    ])
  })

  it("show validation error coming from the backend", async () => {
    renderComponent({
      validationError: mockValidationError,
    })
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement

    // The date is valid, so the backend validation error is "artificially" added here
    // to trigger the error. Invalid dates would be also caught by the component directly,
    // so we couldn't test if the backend mechanism works.
    Object.defineProperty(input, "target", {
      value: "2020-05-12",
    })

    await fireEvent.update(input)
  })

  it("does not allow letters", async () => {
    const { emitted } = renderComponent()
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement

    await userEvent.type(input, "20HA-02-31")

    expect(emitted("update:modelValue")).toEqual([[undefined]])
  })
})
