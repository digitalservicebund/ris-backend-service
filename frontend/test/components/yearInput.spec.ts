import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import YearInput from "@/shared/components/input/YearInput.vue"

type YearInputProps = InstanceType<typeof YearInput>["$props"]

function renderComponent(
  props?: Partial<YearInputProps>,
  attrs?: Record<string, unknown>,
) {
  let modelValue: string | undefined = ""

  const effectiveProps: YearInputProps = {
    id: "identifier",
    modelValue: modelValue,
    "onUpdate:modelValue": (value) => (modelValue = value),
    hasError: false,
    ...props,
  }

  return render(YearInput, { props: effectiveProps, attrs })
}

describe("Year Input", () => {
  it("renders a year input", () => {
    renderComponent()
    const input = screen.getByRole("textbox")
    expect(input).toBeInTheDocument()
  })

  it("renders the ID", () => {
    renderComponent({ id: "test-id" })
    const input = screen.getByRole("textbox")
    expect(input).toHaveAttribute("id", "test-id")
  })

  it("renders the name", () => {
    renderComponent(undefined, { name: "test-name" })
    const input = screen.getByRole("textbox")
    expect(input).toHaveAttribute("name", "test-name")
  })

  it("renders an aria label", () => {
    renderComponent(undefined, { ariaLabel: "test-label" })
    const input = screen.queryByLabelText("test-label")
    expect(input).toBeInTheDocument()
  })

  it("emits model update when a value is inserted", async () => {
    const { emitted } = renderComponent({ modelValue: "" })
    const input = screen.getByRole<HTMLInputElement>("textbox")

    expect(input).toHaveValue("")
    await userEvent.type(input, "2021")
    expect(emitted("update:modelValue")).toEqual([["2021"]])
  })

  it("emits model update when a value is cleared", async () => {
    const { emitted } = renderComponent({ modelValue: "2021" })
    const input = screen.getByRole<HTMLInputElement>("textbox")

    expect(input).toHaveValue("2021")
    await userEvent.clear(input)
    expect(emitted("update:modelValue")).toEqual([[undefined]])
  })

  it("does not update the model value when changing to a partial year", async () => {
    const { emitted } = renderComponent({ modelValue: "2021" })
    const input = screen.getByRole<HTMLInputElement>("textbox")

    expect(input).toHaveValue("2021")
    await userEvent.type(input, "{backspace}")
    expect(input).toHaveValue("202")
    expect(emitted("update:modelValue")).toBeUndefined()
  })

  it("does not update the model value when changing to an invalid year", async () => {
    const { emitted } = renderComponent({ modelValue: "" })
    const input = screen.getByRole<HTMLInputElement>("textbox")

    expect(input).toHaveValue("")
    await userEvent.type(input, "0000")
    expect(input).toHaveValue("0000")
    expect(emitted("update:modelValue")).toBeUndefined()
  })

  it("updates the model when changing from an invalid year to a valid year", async () => {
    const { emitted } = renderComponent({ modelValue: "2005" })
    const input = screen.getByRole<HTMLInputElement>("textbox")

    await userEvent.type(input, "{backspace}")
    expect(input).toHaveValue("200")
    await userEvent.type(input, "8")

    expect(emitted("update:modelValue")).toEqual([["2008"]])
  })

  it("user can enter only digits in the year input field", async () => {
    renderComponent()
    const input = screen.getByRole<HTMLInputElement>("textbox")
    await userEvent.type(input, "abcd")
    expect(input).toHaveValue("")
  })

  it("user can enter only 4 digits in the year input field", async () => {
    renderComponent()
    const input = screen.getByRole<HTMLInputElement>("textbox")
    await userEvent.type(input, "12345")
    expect(input).toHaveValue("1234")
  })

  it("renders a validation error via prop", async () => {
    renderComponent({ hasError: true })
    const input = screen.getByRole("textbox")
    expect(input).toHaveClass("has-error")
  })

  it("does not render a validation error for an empty field", async () => {
    renderComponent({ modelValue: "" })
    const input = screen.getByRole("textbox")
    expect(input).not.toHaveClass("has-error")
  })

  it("does not render a validation error for a valid year", async () => {
    renderComponent({ modelValue: "1989" })
    const input = screen.getByRole("textbox")
    expect(input).not.toHaveClass("has-error")
  })

  it("renders a validation error for an invalid year", async () => {
    renderComponent({ modelValue: "" })
    const input = screen.getByRole("textbox")
    await userEvent.type(input, "0000")
    expect(input).toHaveClass("has-error")
  })

  it("renders a validation error for a partial year on blur", async () => {
    renderComponent({ modelValue: "" })
    const input = screen.getByRole("textbox")

    await userEvent.type(input, "2020")
    expect(input).not.toHaveClass("has-error")

    await userEvent.type(input, "{backspace}")
    await userEvent.tab()
    expect(input).toHaveClass("has-error")
  })

  it("does not render a validation error for a partial year while editing", async () => {
    renderComponent({ modelValue: "" })
    const input = screen.getByRole("textbox")

    await userEvent.type(input, "2020")
    expect(input).not.toHaveClass("has-error")

    await userEvent.type(input, "{backspace}")
    expect(input).not.toHaveClass("has-error")
  })

  it("removes a validation error when a valid year is entered", async () => {
    renderComponent({ modelValue: "" })
    const input = screen.getByRole("textbox")
    await userEvent.type(input, "0000")
    expect(input).toHaveClass("has-error")

    await userEvent.type(input, "{backspace}")
    await userEvent.type(input, "{backspace}")
    await userEvent.type(input, "{backspace}")
    await userEvent.type(input, "{backspace}")
    await userEvent.type(input, "2020")

    expect(input).not.toHaveClass("has-error")
  })

  it("removes a validation error while editing", async () => {
    renderComponent({ modelValue: "" })
    const input = screen.getByRole("textbox")
    await userEvent.type(input, "0000")
    expect(input).toHaveClass("has-error")
    await userEvent.type(input, "{backspace}")
    expect(input).not.toHaveClass("has-error")
  })
})
