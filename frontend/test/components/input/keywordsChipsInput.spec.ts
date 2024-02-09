import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import KeywordsChipsInput from "@/components/input/KeywordsChipsInput.vue"

type KeywordsChipsInputProps = InstanceType<typeof KeywordsChipsInput>["$props"]

function renderComponent(props?: Partial<KeywordsChipsInputProps>) {
  const user = userEvent.setup()

  let modelValue: string[] | undefined = props?.modelValue ?? []

  const effectiveProps: KeywordsChipsInputProps = {
    id: props?.id ?? "identifier",
    modelValue,
    "onUpdate:modelValue":
      props?.["onUpdate:modelValue"] ??
      ((val: string[] | undefined) => (modelValue = val)),
    ariaLabel: props?.ariaLabel ?? "aria-label",
  }

  return { user, ...render(KeywordsChipsInput, { props: effectiveProps }) }
}

describe("Keywords Chips Input", () => {
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
    const { user } = renderComponent({ "onUpdate:modelValue": onUpdate })

    const input = screen.getByRole("textbox")
    await user.type(input, "foo{enter}")
    expect(onUpdate).toHaveBeenCalledWith(["foo"])
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

  it("focuses the input when pressing arrow on the first chip", async () => {
    const { user } = renderComponent({ modelValue: ["foo", "bar", "baz"] })

    const chips = screen.getAllByRole("listitem")
    await user.click(chips[0])
    await user.keyboard("{arrowleft}")
    const input = screen.getByRole<HTMLInputElement>("textbox")
    expect(input).toHaveFocus()
  })

  it("focuses the first chip when pressing arrow on the input", async () => {
    const { user } = renderComponent({ modelValue: ["foo", "bar", "baz"] })

    const input = screen.getByRole<HTMLInputElement>("textbox")
    await user.click(input)
    await user.type(input, "abc")
    expect(input).toHaveFocus()
    await user.keyboard("{arrowright}")
    const chips = screen.getAllByRole("listitem")
    expect(chips[0]).toHaveFocus()
  })

  it("focuses chips with arrow keys", async () => {
    const { user } = renderComponent({ modelValue: ["foo", "bar", "baz"] })

    const input = screen.getByRole<HTMLInputElement>("textbox")
    await user.click(input)
    await user.tab()

    const chips = screen.getAllByRole("listitem")
    expect(chips[0]).toHaveFocus()
    await user.keyboard("{arrowright}")
    expect(chips[1]).toHaveFocus()
    await user.keyboard("{arrowright}")
    expect(chips[2]).toHaveFocus()
    await user.keyboard("{arrowright}")
    expect(chips[2]).toHaveFocus()
    await user.keyboard("{arrowleft}")
    expect(chips[1]).toHaveFocus()
    await user.keyboard("{arrowleft}")
    expect(chips[0]).toHaveFocus()
    await user.keyboard("{arrowleft}")
    expect(input).toHaveFocus()
  })

  it("focuses chips with tab", async () => {
    const { user } = renderComponent({ modelValue: ["foo", "bar", "baz"] })

    const input = screen.getByRole<HTMLInputElement>("textbox")
    await user.click(input)
    await user.tab()

    const chips = screen.getAllByRole("listitem")
    expect(chips[0]).toHaveFocus()
    await user.tab({ shift: true })
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

  it("shows an error message when adding a chip that already exists", async () => {
    const onUpdate = vi.fn()
    const value = "one"
    const errorMessage = value + " bereits vorhanden."

    const { user } = renderComponent({
      modelValue: [value],
      "onUpdate:modelValue": onUpdate,
    })

    const input = screen.getByRole<HTMLInputElement>("textbox")
    await user.type(input, value + "{enter}")
    expect(screen.getByText(errorMessage)).toBeInTheDocument()
    expect(onUpdate).not.toHaveBeenCalled()
  })

  it("clears the error message on blur", async () => {
    const onUpdate = vi.fn()
    const value = "one"
    const errorMessage = value + " bereits vorhanden."

    const { user } = renderComponent({
      modelValue: [value],
      "onUpdate:modelValue": onUpdate,
    })

    const input = screen.getByRole<HTMLInputElement>("textbox")
    await user.type(input, value + "{enter}")
    expect(screen.getByText(errorMessage)).toBeInTheDocument()
    await user.tab()
    expect(screen.queryByText(errorMessage)).not.toBeInTheDocument()
  })

  it("clears the error message on input", async () => {
    const onUpdate = vi.fn()
    const value = "one"
    const errorMessage = value + " bereits vorhanden."
    const { user } = renderComponent({
      modelValue: [value],
      "onUpdate:modelValue": onUpdate,
    })

    const input = screen.getByRole<HTMLInputElement>("textbox")
    await user.type(input, value + "{enter}")
    expect(screen.getByText(errorMessage)).toBeInTheDocument()
    await user.type(input, "two")
    expect(screen.queryByText(errorMessage)).not.toBeInTheDocument()
  })
})
