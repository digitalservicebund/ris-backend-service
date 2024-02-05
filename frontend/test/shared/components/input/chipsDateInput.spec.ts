import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import ChipsDateInput from "@/shared/components/input/ChipsDateInput.vue"

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

  return { user, ...render(ChipsDateInput, { props: effectiveProps }) }
}

describe("ChipsDateInput", () => {
  it("shows a chips input element", () => {
    renderComponent()
    const input = screen.getByRole<HTMLInputElement>("textbox")

    expect(input).toBeInTheDocument()
    expect(input?.type).toBe("text")
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

  it("uses date input maska", async () => {
    const { user } = renderComponent()

    const input = screen.getByRole("textbox")
    await user.type(input, "abc12d§07202099")
    expect(input).toHaveValue("12.07.2020")
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
    await user.type(input, "50022020{enter}")

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
    await user.type(input, "01012100{enter}")

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
    const deleteButtons = screen.getAllByRole("button")
    await user.click(deleteButtons[0])
    expect(onUpdate).toHaveBeenCalled()
  })

  it("deletes the focused chip on enter", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      modelValue: ["2020-01-01", "2021-01-01"],
    })

    const chips = screen.getAllByRole("listitem")
    await user.click(chips[1])
    await user.keyboard("{enter}")
    expect(onUpdate).toHaveBeenCalledWith(["2020-01-01"])
  })
})
