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
    "onUpdate:validationError": props?.["onUpdate:validationError"],
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

    expect(onUpdate).toHaveBeenCalledWith([])
  })

  it("does not add chips if input already exists", async () => {
    const id = "id"
    const ariaLabel = "chip"

    const onError = vi.fn()
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      id: id,
      ariaLabel: ariaLabel,
      "onUpdate:modelValue": onUpdate,
      "onUpdate:validationError": onError,
      modelValue: ["foo", "bar"],
    })

    const input = screen.getByRole("textbox")
    await user.type(input, "bar{enter}")

    expect(onUpdate).not.toHaveBeenCalled()
    expect(onError).toHaveBeenCalledWith({
      message: "bar bereits vorhanden",
      instance: id,
    })
  })

  it("deletes the chip on button click", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      modelValue: ["foo", "bar"],
    })

    const chips = screen.getAllByLabelText("Eintrag löschen")
    await user.click(chips[1])
    await user.keyboard("{enter}")
    expect(onUpdate).toHaveBeenCalledWith(["foo"])
  })

  it("edits the chip on double click", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      modelValue: ["f"],
    })

    const editButton = screen.getByRole("button", {
      name: /eintrag bearbeiten/i,
    })
    await user.dblClick(editButton)
    const input = screen.getByRole("textbox")
    await user.keyboard("{backspace}")
    await user.type(input, "b")
    await user.keyboard("{enter}")
    expect(onUpdate).toHaveBeenCalledWith(["b"])
  })

  it("validates the first chip after editing", async () => {
    const id = "id"
    const ariaLabel = "chip"
    const onUpdate = vi.fn()
    const onError = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      "onUpdate:validationError": onError,
      modelValue: ["foo", "bar"],
      id: id,
      ariaLabel: ariaLabel,
    })

    const editButtons = screen.getAllByRole("button", {
      name: /eintrag bearbeiten/i,
    })
    await user.dblClick(editButtons[0])
    const input = screen.getByRole("textbox")
    await user.keyboard("{backspace}{backspace}{backspace}")
    await user.type(input, "bar")
    await user.keyboard("{enter}")
    expect(onUpdate).not.toHaveBeenCalledWith(["bar"])
    expect(onError).toHaveBeenCalledWith({
      message: "bar bereits vorhanden",
      instance: id,
    })
  })
})
