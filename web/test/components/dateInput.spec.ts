import userEvent from "@testing-library/user-event"
import { fireEvent, render } from "@testing-library/vue"
import { nextTick } from "vue"
import DateInput from "@/components/DateInput.vue"

function renderComponent(options?: {
  ariaLabel?: string
  value?: string
  modelValue?: string
  placeholder?: string
}) {
  const user = userEvent.setup()
  const props = {
    id: "identifier",
    value: options?.value,
    modelValue: options?.modelValue,
    ariaLabel: options?.ariaLabel ?? "aria-label",
    placeholder: options?.placeholder,
  }
  const renderResult = render(DateInput, { props })
  return { user, props, ...renderResult }
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
    const { container } = renderComponent({ value: "2022-02-03" })
    const input = container.querySelector("input") as HTMLInputElement

    expect(input).toHaveValue("2022-02-03")

    fireEvent.input(input, { target: { value: "2020-05-12" } })

    expect(input).toHaveValue("2020-05-12")
  })

  it("emits model update event when user types into input", async () => {
    const { container, emitted } = renderComponent({
      value: "2022-02-03",
    })
    const input = container.querySelector("input") as HTMLInputElement
    fireEvent.input(input, { target: { value: "2020-05-12" } })
    await nextTick()

    expect(input).toHaveValue("2020-05-12")
    expect(emitted()["update:modelValue"]).toEqual([["2020-05-12"]])
  })

  it("does not allow dates in the future", async () => {
    const { container, emitted } = renderComponent()
    const input = container.querySelector("input") as HTMLInputElement

    Object.defineProperty(input, "target", {
      value: "2024-02-10",
    })

    await fireEvent.update(input)
    await nextTick()

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

    expect(emitted()["update:modelValue"]).not.toBeTruthy()
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
