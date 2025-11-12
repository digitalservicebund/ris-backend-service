import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { setActivePinia, storeToRefs } from "pinia"
import { beforeEach } from "vitest"
import ChipsBorderNumberInput from "@/components/input/ChipsBorderNumberInput.vue"
import { Decision } from "@/domain/decision"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

type ChipsBorderNumberInputProps = InstanceType<
  typeof ChipsBorderNumberInput
>["$props"]

function renderComponent(props?: Partial<ChipsBorderNumberInputProps>) {
  const user = userEvent.setup()

  let modelValue: number[] | undefined = props?.modelValue ?? []

  const effectiveProps: ChipsBorderNumberInputProps = {
    id: props?.id ?? "identifier",
    ariaLabel: props?.ariaLabel ?? "border-number-input",
    modelValue,
    "onUpdate:modelValue":
      props?.["onUpdate:modelValue"] ??
      ((val?: number[]) => (modelValue = val)),
    "onUpdate:validationError": props?.["onUpdate:validationError"],
    hasError: props?.hasError,
    readOnly: props?.readOnly,
  }

  return { user, ...render(ChipsBorderNumberInput, { props: effectiveProps }) }
}

describe("ChipsBorderNumberInput", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
  })
  it("renders input with correct aria-label", () => {
    renderComponent({ ariaLabel: "randnummer" })

    const input = screen.getByRole("textbox")

    expect(input).toHaveAttribute("aria-label", "randnummer")
  })

  it("displays existing border numbers as chips", () => {
    renderComponent({ modelValue: [13, 24] })

    const chips = screen.getAllByRole("listitem")

    expect(chips).toHaveLength(2)
    expect(chips[0]).toHaveTextContent("13")
    expect(chips[1]).toHaveTextContent("24")
  })

  it("accepts valid border number input and emits update", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({ "onUpdate:modelValue": onUpdate })
    const store = useDocumentUnitStore()
    const { documentUnit } = storeToRefs(store)
    documentUnit.value = {
      ...store.documentUnit,
      managementData: {
        borderNumbers: ["42"],
      },
    } as Decision

    const input = screen.getByRole("textbox")
    await user.type(input, "42{enter}")

    expect(onUpdate).toHaveBeenCalledWith([42])
  })

  it("rejects invalid border number and emits validation error", async () => {
    const onUpdate = vi.fn()
    const onError = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      "onUpdate:validationError": onError,
    })

    const input = screen.getByRole("textbox")
    await user.type(input, "9999{enter}")

    expect(onUpdate).not.toHaveBeenCalled()
    expect(onError).toHaveBeenCalledWith({
      message: "Randnummer existiert nicht",
      instance: "identifier",
    })
  })

  it("does not accept duplicates and emits validation error", async () => {
    const onUpdate = vi.fn()
    const onError = vi.fn()

    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      "onUpdate:validationError": onError,
      modelValue: [13],
    })

    const input = screen.getByRole("textbox")
    await user.type(input, "13{enter}")

    expect(onUpdate).not.toHaveBeenCalled()
    expect(onError).toHaveBeenCalledWith({
      message: "13 bereits vorhanden",
      instance: "identifier",
    })
  })

  it("does not allow input in readOnly mode", () => {
    renderComponent({ modelValue: [1, 2], readOnly: true })
    const chipsInput = screen.getByTestId("chips-input-wrapper_identifier")
    expect(chipsInput).toBeInTheDocument()
    expect(chipsInput).toHaveTextContent("12")
    expect(screen.queryByRole("textbox")).not.toBeInTheDocument()
  })
})
