import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { InputText } from "primevue"
import ChipsDateInput from "@/components/input/ChipsDateInput.vue"

type DateChipsInputProps = InstanceType<typeof ChipsDateInput>["$props"]

function renderComponent(props?: Partial<DateChipsInputProps>) {
  const user = userEvent.setup()

  let modelValue: string[] | undefined = props?.modelValue ?? []

  const effectiveProps: DateChipsInputProps = {
    id: props?.id ?? "identifier",
    modelValue,
    "onUpdate:modelValue":
      props?.["onUpdate:modelValue"] ??
      ((val: string[] | undefined) => (modelValue = val)),
    "onUpdate:validationError": props?.["onUpdate:validationError"],
    ariaLabel: props?.ariaLabel ?? "aria-label",
  }

  return {
    user,
    ...render(ChipsDateInput, {
      props: effectiveProps,
      global: {
        stubs: { InputMask: InputText },
      },
    }),
  }
}

describe("ChipsDateInput", () => {
  it("shows a chips input element", () => {
    renderComponent()
    const input = screen.getByRole<HTMLInputElement>("textbox")

    expect(input).toBeInTheDocument()
    expect(input?.type).toBe("text")
  })

  it("emits model update when a chip is removed", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      modelValue: ["2020-11-30", "2020-05-15"],
    })

    const button = screen.getAllByLabelText("Eintrag löschen")[0]
    await user.click(button)
    expect(onUpdate).toHaveBeenCalledWith(["2020-05-15"])
  })

  it("does not add chips if input already exists", async () => {
    const id = "id"

    const onUpdate = vi.fn()
    const { user } = renderComponent({
      id: id,
      modelValue: ["2020-11-30", "2020-05-15"],
      "onUpdate:modelValue": onUpdate,
    })

    const input = screen.getByRole<HTMLInputElement>("textbox")
    await user.type(input, "15.05.2020{enter}")

    expect(onUpdate).not.toHaveBeenCalled()
  })

  it("shows dates in correct format", () => {
    renderComponent({ modelValue: ["2020-11-30"] })

    const chips = screen.getAllByRole("listitem")
    expect(chips).toHaveLength(1)
    expect(chips[0]).toHaveTextContent("30.11.2020")
  })

  it("converts date format of user input", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({ "onUpdate:modelValue": onUpdate })

    const input = screen.getByRole("textbox")
    await user.type(input, "30.11.2020{enter}")
    expect(onUpdate).toHaveBeenCalledWith(["2020-11-30"])
  })

  it("does not accept incorrect dates", async () => {
    const id = "id"

    const onError = vi.fn()
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      id: id,
      "onUpdate:modelValue": onUpdate,
      "onUpdate:validationError": onError,
    })

    const input = screen.getByRole("textbox")
    await user.type(input, "50.02.2020{enter}")

    expect(onUpdate).not.toHaveBeenCalled()
    expect(onError).toHaveBeenCalledWith({
      message: "Kein valides Datum",
      instance: id,
    })
  })

  it("does not accept dates in future", async () => {
    const id = "id"
    const ariaLabel = "chip"

    const onError = vi.fn()
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      id: id,
      ariaLabel: ariaLabel,
      "onUpdate:modelValue": onUpdate,
      "onUpdate:validationError": onError,
    })

    const input = screen.getByRole("textbox")
    await user.type(input, "12.12.3000{enter}")

    expect(onUpdate).not.toHaveBeenCalled()
    expect(onError).toHaveBeenCalledWith({
      message: ariaLabel + " darf nicht in der Zukunft liegen",
      instance: id,
    })
  })

  it("deletes on button click", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      modelValue: ["2020-01-01"],
      "onUpdate:modelValue": onUpdate,
    })
    const deleteButtons = screen.getAllByLabelText("Eintrag löschen")
    await user.click(deleteButtons[0])
    expect(onUpdate).toHaveBeenCalledWith([])
  })

  it("edits the chip on double click", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      modelValue: ["2020-01-01"],
    })

    const editButton = screen.getByRole("button", {
      name: /eintrag bearbeiten/i,
    })
    await user.dblClick(editButton)
    const input = screen.getByRole("textbox")
    await user.keyboard("{backspace}")
    await user.type(input, "2")
    await user.keyboard("{enter}")
    expect(onUpdate).toHaveBeenCalledWith(["2022-01-01"])
  })

  it("validates the first chip after editing", async () => {
    const id = "id"
    const ariaLabel = "chip"
    const onUpdate = vi.fn()
    const onError = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      "onUpdate:validationError": onError,
      modelValue: ["2020-01-01", "2020-01-02", "2020-01-03"],
      id: id,
      ariaLabel: ariaLabel,
    })

    const editButtons = screen.getAllByRole("button", {
      name: /eintrag bearbeiten/i,
    })
    await user.dblClick(editButtons[0])
    const input = screen.getByRole("textbox")
    await user.keyboard("{backspace}{backspace}")
    await user.type(input, "99")
    await user.keyboard("{enter}")
    expect(onUpdate).not.toHaveBeenCalledWith(["2022-01-99"])
    expect(onError).toHaveBeenCalledWith({
      message: ariaLabel + " darf nicht in der Zukunft liegen",
      instance: id,
    })
  })
})
