import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { InputText } from "primevue"
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
    ariaLabel: props?.ariaLabel ?? "aria-label",
    readOnly: props?.readOnly,
  }

  return {
    user,
    ...render(ChipsInput, {
      props: effectiveProps,
      global: {
        stubs: { InputMask: InputText },
      },
    }),
  }
}

describe("Chips Input", () => {
  it("shows the list of chips", () => {
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
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
    })

    const input = screen.getByRole("textbox")
    await user.type(input, "foo{enter}")
    expect(onUpdate).toHaveBeenCalledWith(["foo"])
  })

  it("emits model update when a chip is removed", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      modelValue: ["foo", "bar"],
    })

    const button = screen.getAllByLabelText("Eintrag löschen")[0]
    await user.click(button)
    expect(onUpdate).toHaveBeenCalledWith(["bar"])
  })

  it("emits model update when last chip is removed", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      modelValue: ["foo"],
    })

    const button = screen.getAllByLabelText("Eintrag löschen")[0]
    await user.click(button)
    expect(onUpdate).toHaveBeenCalledWith([])
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

    const onUpdate = vi.fn()
    const { user } = renderComponent({
      id: id,
      modelValue: modelValue,
      "onUpdate:modelValue": onUpdate,
    })

    const input = screen.getByRole<HTMLInputElement>("textbox")
    await user.type(input, "foo{enter}")

    expect(onUpdate).not.toHaveBeenCalled()
  })
})
