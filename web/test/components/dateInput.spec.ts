import userEvent from "@testing-library/user-event"
import { fireEvent, render } from "@testing-library/vue"
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
  const renderResult = render(DateInput, { props })
  return { user, props, ...renderResult }
}

const mockValidationError: ValidationError = {
  defaultMessage: "wrong date",
  field: "coreData.decisionDate",
}

describe("DateInput", () => {
  it("shows an date input element", () => {
    const { container } = renderComponent()
    const input = container.querySelector("input") as HTMLInputElement

    expect(input).toBeInTheDocument()
    expect(input?.type).toBe("date")
  })

  it("shows input with an aria label", () => {
    const { queryByLabelText } = renderComponent({
      ariaLabel: "test-label",
    })
    const input = queryByLabelText("test-label")

    expect(input).toBeInTheDocument()
  })

  it("allows to type date inside input", async () => {
    const { container } = renderComponent({ modelValue: "2022-02-03" })
    const input = container.querySelector("input") as HTMLInputElement

    expect(input).toHaveValue("2022-02-03")

    await userEvent.clear(input)
    await userEvent.type(input, "2020-05-12")
    await userEvent.tab()

    expect(input).toHaveValue("2020-05-12")
  })

  it("emits model update event when input changes", async () => {
    const { container, emitted } = renderComponent({
      value: "2022-02-03",
    })
    const input = container.querySelector("input") as HTMLInputElement
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
    const { container, emitted } = renderComponent()
    const input = container.querySelector("input") as HTMLInputElement

    Object.defineProperty(input, "target", {
      value: "2024-02-10",
    })

    await fireEvent.update(input)
    await nextTick()

    expect(input.className).toContain("input__error")
    expect(emitted()["update:modelValue"]).not.toBeTruthy()
  })

  it("does not allow invalid dates", async () => {
    const { container, emitted } = renderComponent()
    const input = container.querySelector("input") as HTMLInputElement

    Object.defineProperty(input, "target", {
      value: "2020-02-31",
    })

    await fireEvent.update(input)
    await nextTick()

    expect(input.className).toContain("input__error")
    expect(emitted()["update:modelValue"]).not.toBeTruthy()
  })

  it("show validation error coming from the backend", async () => {
    const { container } = renderComponent({
      validationError: mockValidationError,
    })
    const input = container.querySelector("input") as HTMLInputElement

    // The date is valid, so the backend validation error is "artificially" added here
    // to trigger the error. Invalid dates would be also caught by the component directly,
    // so we couldn't test if the backend mechanism works.
    Object.defineProperty(input, "target", {
      value: "2020-05-12",
    })

    await fireEvent.update(input)
    await nextTick()

    expect(input.className).toContain("input__error")
  })

  it("does not allow letters", async () => {
    const { container, emitted } = renderComponent()
    const input = container.querySelector("input") as HTMLInputElement

    Object.defineProperty(input, "target", {
      value: "20HA-02-31",
    })

    await fireEvent.update(input)
    await nextTick()

    expect(emitted()["update:modelValue"]).not.toBeTruthy()
  })
})
