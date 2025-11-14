import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import ChipsInput from "@/components/input/ChipsInput.vue"

type ChipsInputProps = InstanceType<typeof ChipsInput>["$props"]

function renderComponent(props?: Partial<ChipsInputProps>) {
  const user = userEvent.setup()

  let modelValue: string[] | undefined = props?.modelValue ?? []

  const effectiveProps: ChipsInputProps = {
    id: props?.id ?? "identifier",
    modelValue,
    "onUpdate:modelValue":
      props?.["onUpdate:modelValue"] ??
      ((val: string[] | undefined) => (modelValue = val)),
    "onUpdate:validationError": props?.["onUpdate:validationError"],
    ariaLabel: props?.ariaLabel ?? "aria-label",
    readOnly: props?.readOnly,
    placeholder: props?.placeholder,
  }

  return { user, ...render(ChipsInput, { props: effectiveProps }) }
}

describe("Chips Input", () => {
  it("shows a chips input element", () => {
    renderComponent()
    const input = screen.getByRole<HTMLInputElement>("textbox")
    expect(input).toBeInTheDocument()
  })

  it("shows the value", () => {
    renderComponent({ modelValue: ["foo", "bar"] })
    const chips = screen.getAllByRole("listitem")
    expect(chips).toHaveLength(2)
    expect(chips[0]).toHaveTextContent("foo")
    expect(chips[1]).toHaveTextContent("bar")
  })

  it("shows chips input with an aria label", () => {
    renderComponent({ ariaLabel: "test-label" })
    const input = screen.queryByLabelText("test-label")
    expect(input).toBeInTheDocument()
  })

  it("emits model update when a chip is added", async () => {
    const onUpdate = vi.fn()
    const onError = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      "onUpdate:validationError": onError,
    })

    const input = screen.getByRole("textbox")
    await user.type(input, "foo{enter}")
    expect(onUpdate).toHaveBeenCalledWith(["foo"])
    expect(onError).toHaveBeenCalledWith(undefined)
  })

  it("removes whitespace from chips when added", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      modelValue: ["bar"],
    })

    const input = screen.getByRole("textbox")
    await user.type(input, " foo {enter}")
    expect(onUpdate).toHaveBeenCalledWith(["bar", "foo"])
  })

  it("clears the input when a chip is added", async () => {
    const { user } = renderComponent()

    const input = screen.getByRole("textbox")
    await user.type(input, "foo{enter}")
    expect(input).toHaveValue("")
  })

  it("emits model update when a chip is removed", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      modelValue: ["foo", "bar"],
    })

    const button = screen.getAllByRole("button")[0]
    await user.click(button)
    expect(onUpdate).toHaveBeenCalledWith(["bar"])
  })

  it("does not render delete button if readOnly", async () => {
    renderComponent({ modelValue: ["foo", "bar"], readOnly: true })

    expect(screen.queryByRole("button")).not.toBeInTheDocument()
  })

  it("does not render input if readOnly", async () => {
    renderComponent({ modelValue: ["foo", "bar"], readOnly: true })

    expect(screen.queryByRole("textbox")).not.toBeInTheDocument()
  })

  it("does not add a chip when input is empty", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({ "onUpdate:modelValue": onUpdate })
    const input = screen.getByRole<HTMLInputElement>("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "{enter}")

    expect(onUpdate).not.toHaveBeenCalled()
  })

  it("does not add a chip when input is only whitespaces", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({ "onUpdate:modelValue": onUpdate })
    const input = screen.getByRole<HTMLInputElement>("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "   {enter}")

    expect(onUpdate).not.toHaveBeenCalled()
  })

  it("does not add chips if input already exists", async () => {
    const id = "id"
    const modelValue: ChipsInputProps["modelValue"] = ["foo", "bar"]

    const onError = vi.fn()
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      id: id,
      modelValue: modelValue,
      "onUpdate:modelValue": onUpdate,
      "onUpdate:validationError": onError,
    })

    const input = screen.getByRole<HTMLInputElement>("textbox")
    await user.type(input, "foo{enter}")

    expect(onUpdate).not.toHaveBeenCalled()
    expect(onError).toHaveBeenCalledWith({
      message: "foo bereits vorhanden",
      instance: id,
    })
  })

  it("focuses the input when clicking on the chips component", async () => {
    const { user } = renderComponent({ id: "test" })

    const chips = screen.getByTestId("chips-input-wrapper_test")
    await user.click(chips)

    const input = screen.getByRole<HTMLInputElement>("textbox")
    expect(input).toHaveFocus()
  })

  it("focuses the input when the last chip is deleted", async () => {
    let modelValue: ChipsInputProps["modelValue"] = ["foo", "bar"]
    const onUpdate: ChipsInputProps["onUpdate:modelValue"] = (val) => {
      modelValue = val
    }
    const { user, rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": onUpdate,
    })

    const deleteButtons = screen.getAllByRole("button")
    await user.click(deleteButtons[1])
    await rerender({ modelValue })
    await user.click(deleteButtons[0])
    await rerender({ modelValue })

    const input = screen.getByRole<HTMLInputElement>("textbox")
    expect(input).toHaveFocus()
  })

  it("focuses the input when pressing arrow on the last chip", async () => {
    const { user } = renderComponent({ modelValue: ["foo", "bar", "baz"] })

    const chips = screen.getAllByRole("listitem")
    await user.click(chips[2])
    await user.keyboard("{arrowright}")
    const input = screen.getByRole<HTMLInputElement>("textbox")
    expect(input).toHaveFocus()
  })

  it("focuses the last chip when pressing arrow on the input", async () => {
    const { user } = renderComponent({ modelValue: ["foo", "bar", "baz"] })

    const input = screen.getByRole<HTMLInputElement>("textbox")
    await user.click(input)
    await user.type(input, "abc")
    await user.keyboard("{arrowleft}{arrowleft}{arrowleft}")
    expect(input).toHaveFocus()
    await user.keyboard("{arrowleft}")
    const chips = screen.getAllByRole("listitem")
    expect(chips[2]).toHaveFocus()
  })

  it("focuses chips with arrow keys", async () => {
    const { user } = renderComponent({ modelValue: ["foo", "bar", "baz"] })

    const input = screen.getByRole<HTMLInputElement>("textbox")
    await user.click(input)
    await user.tab({ shift: true })

    const chips = screen.getAllByRole("listitem")
    expect(chips[2]).toHaveFocus()
    await user.keyboard("{arrowleft}")
    expect(chips[1]).toHaveFocus()
    await user.keyboard("{arrowleft}")
    expect(chips[0]).toHaveFocus()
    await user.keyboard("{arrowleft}")
    expect(chips[0]).toHaveFocus()
    await user.keyboard("{arrowright}")
    expect(chips[1]).toHaveFocus()
    await user.keyboard("{arrowright}")
    expect(chips[2]).toHaveFocus()
    await user.keyboard("{arrowright}")
    expect(input).toHaveFocus()
  })

  it("focuses chips with tab", async () => {
    const { user } = renderComponent({ modelValue: ["foo", "bar", "baz"] })

    const input = screen.getByRole<HTMLInputElement>("textbox")
    await user.click(input)
    await user.tab({ shift: true })

    const chips = screen.getAllByRole("listitem")
    expect(chips[2]).toHaveFocus()
    await user.tab()
    expect(input).toHaveFocus()
  })

  it("deletes the focused chip on enter", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      modelValue: ["foo", "bar"],
    })

    const chips = screen.getAllByRole("listitem")
    await user.click(chips[1])
    await user.keyboard("{enter}")
    expect(onUpdate).toHaveBeenCalledWith(["foo"])
  })

  it("shows placeholder when input is empty", () => {
    renderComponent({
      modelValue: [],
      placeholder: "placeholder",
    })

    const input = screen.getByRole("textbox")
    expect(input).toHaveAttribute("placeholder", "placeholder")
  })

  it("hdoes not show placeholder when input has values", () => {
    renderComponent({
      modelValue: ["foo", "bar"],
      placeholder: "placeholder",
    })

    const input = screen.getByRole("textbox")
    expect(input).not.toHaveAttribute("placeholder", "placeholder")
  })
})
