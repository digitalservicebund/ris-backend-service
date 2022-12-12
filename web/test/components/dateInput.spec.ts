import userEvent from "@testing-library/user-event"
import { fireEvent, render, screen } from "@testing-library/vue"
import { nextTick } from "vue"
import DateInput from "@/components/DateInput.vue"
import { ValidationError } from "@/domain"

function renderComponent(options?: {
  ariaLabel?: string
  value?: string
  modelValue?: string
  placeholder?: string
  validationError?: ValidationError
}) {
  const user = userEvent.setup()
  const props = {
    id: "identifier",
    value: options?.value,
    modelValue: options?.modelValue,
    ariaLabel: options?.ariaLabel ?? "aria-label",
    placeholder: options?.placeholder,
    validationError: options?.validationError,
  }
  const utils = render(DateInput, { props })
  return { user, props, ...utils }
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
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement

    expect(input).toHaveValue("2022-02-03")

    await userEvent.clear(input)
    await userEvent.type(input, "2020-05-12")
    await userEvent.tab()

    expect(input).toHaveValue("2020-05-12")
  })

  it("emits model update event when input changes", async () => {
    const { emitted } = renderComponent({
      value: "2022-02-03",
    })
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement
    Object.defineProperty(input, "target", {
      value: "2020-05-12",
    })
    await userEvent.clear(input)
    await userEvent.type(input, "2020-05-12")
    await userEvent.tab()
    await nextTick()

    expect(input).toHaveValue("2020-05-12")
    expect(emitted()["update:modelValue"]).toBeTruthy()
  })

  it("does not allow dates in the future", async () => {
    const { emitted } = renderComponent()
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement

    Object.defineProperty(input, "target", {
      value: "2024-02-10",
    })

    await fireEvent.update(input)
    await nextTick()

    expect(emitted()["update:modelValue"]).not.toBeTruthy()
  })

  it("does not allow invalid dates", async () => {
    const { emitted } = renderComponent()
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement

    Object.defineProperty(input, "target", {
      value: "2020-02-31",
    })

    await fireEvent.update(input)
    await nextTick()

    expect(emitted()["update:modelValue"]).not.toBeTruthy()
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
    await nextTick()
  })

  it("does not allow letters", async () => {
    const { emitted } = renderComponent()
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement

    Object.defineProperty(input, "target", {
      value: "20HA-02-31",
    })

    await fireEvent.update(input)
    await nextTick()

    expect(emitted()["update:modelValue"]).not.toBeTruthy()
  })
})
