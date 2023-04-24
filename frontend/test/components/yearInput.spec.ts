import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import YearInput from "@/shared/components/input/YearInput.vue"

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
  const utils = render(YearInput, { props })
  return { user, props, ...utils }
}

describe("YearInput", () => {
  it("shows an text input element", () => {
    renderComponent()
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement

    expect(input).toBeInTheDocument()
    expect(input?.type).toBe("text")
  })

  it("shows input with an aria label", () => {
    renderComponent({
      ariaLabel: "test-label",
    })
    const input = screen.queryByLabelText("test-label") as HTMLInputElement

    expect(input).toBeInTheDocument()
  })

  it("emits model update event when input changes", async () => {
    const { emitted } = renderComponent({
      value: "2022",
    })
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement
    Object.defineProperty(input, "target", {
      value: "2022",
    })
    await userEvent.clear(input)
    await userEvent.type(input, "2021")
    await userEvent.tab()

    expect(input).toHaveValue("2021")
    expect(emitted()["update:modelValue"]).toBeTruthy()
  })

  it("user can enter only digits in the year input field", async () => {
    renderComponent()
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement

    expect(input).toBeInTheDocument()
    expect(input).toBeVisible()

    await userEvent.type(input, "abcd")

    expect(input.value).toBe("")
    expect(input.value.length).toBe(0)
  })

  it("user can enter only 4 digits in the year input field", async () => {
    renderComponent()
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement

    expect(input).toBeInTheDocument()
    expect(input).toBeVisible()

    await userEvent.type(input, "12345")
    expect(input.value).toBe("1234")
    expect(input.value.length).toBe(4)
  })
})
