import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { setActivePinia, storeToRefs } from "pinia"
import { InputText } from "primevue"
import ChipsBorderNumberInput from "@/components/input/ChipsBorderNumberInput.vue"
import { Decision } from "@/domain/decision"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

type BorderNumberInputProps = InstanceType<
  typeof ChipsBorderNumberInput
>["$props"]

function renderComponent(props?: Partial<BorderNumberInputProps>) {
  const user = userEvent.setup()

  let modelValue: number[] | undefined = props?.modelValue ?? []

  const effectiveProps: BorderNumberInputProps = {
    id: props?.id ?? "identifier",
    modelValue,
    "onUpdate:modelValue":
      props?.["onUpdate:modelValue"] ??
      ((val: number[] | undefined) => (modelValue = val)),
    "onUpdate:validationError": props?.["onUpdate:validationError"],
    ariaLabel: props?.ariaLabel ?? "aria-label",
  }

  return {
    user,
    ...render(ChipsBorderNumberInput, {
      props: effectiveProps,
      global: {
        stubs: { InputMask: InputText },
      },
    }),
  }
}

describe("ChipsBorderNumberInput", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
    const store = useDocumentUnitStore()
    const { documentUnit } = storeToRefs(store)
    documentUnit.value = {
      ...store.documentUnit,
      managementData: {
        borderNumbers: ["1", "2"],
      },
    } as Decision
  })

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
      modelValue: [1, 2],
    })

    const button = screen.getAllByLabelText("Eintrag löschen")[0]
    await user.click(button)
    expect(onUpdate).toHaveBeenCalledWith([2])
  })

  it("does not add chips if input already exists", async () => {
    const id = "id"

    const onUpdate = vi.fn()
    const { user } = renderComponent({
      id: id,
      modelValue: [1, 2],
      "onUpdate:modelValue": onUpdate,
    })

    const input = screen.getByRole<HTMLInputElement>("textbox")
    await user.type(input, "2{enter}")

    expect(onUpdate).not.toHaveBeenCalled()
  })

  it("shows border number in correct format", () => {
    renderComponent({ modelValue: [1] })

    const chips = screen.getAllByRole("listitem")
    expect(chips).toHaveLength(1)
    expect(chips[0]).toHaveTextContent("1")
  })

  it("converts format of user input", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({ "onUpdate:modelValue": onUpdate })

    const input = screen.getByRole("textbox")
    await user.type(input, "1{enter}")
    expect(onUpdate).toHaveBeenCalledWith([1])
  })

  it("does not accept incorrect border number", async () => {
    const id = "id"
    const onError = vi.fn()
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      id: id,
      "onUpdate:modelValue": onUpdate,
      "onUpdate:validationError": onError,
    })

    const input = screen.getByRole("textbox")
    await user.type(input, "999{enter}")

    expect(onUpdate).not.toHaveBeenCalled()
    expect(onError).toHaveBeenCalledWith({
      message: "Randnummer existiert nicht",
      instance: id,
    })
  })

  it("does not accept existing number", async () => {
    const id = "id"
    const ariaLabel = "chip"

    const onError = vi.fn()
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      id: id,
      ariaLabel: ariaLabel,
      "onUpdate:modelValue": onUpdate,
      "onUpdate:validationError": onError,
      modelValue: [1, 2],
    })

    const input = screen.getByRole("textbox")
    await user.type(input, "2{enter}")

    expect(onUpdate).not.toHaveBeenCalled()
    expect(onError).toHaveBeenCalledWith({
      message: "2 bereits vorhanden",
      instance: id,
    })
  })

  it("deletes the chip on button click", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      modelValue: [1, 2],
    })

    const chips = screen.getAllByLabelText("Eintrag löschen")
    await user.click(chips[1])
    await user.keyboard("{enter}")
    expect(onUpdate).toHaveBeenCalledWith([1])
  })

  it("edits the chip on double click", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      modelValue: [1],
    })

    const editButton = screen.getByRole("button", {
      name: /eintrag bearbeiten/i,
    })
    await user.dblClick(editButton)
    const input = screen.getByRole("textbox")
    await user.keyboard("{backspace}")
    await user.type(input, "2")
    await user.keyboard("{enter}")
    expect(onUpdate).toHaveBeenCalledWith([2])
  })

  it("validates the first chip after editing", async () => {
    const id = "id"
    const ariaLabel = "chip"
    const onUpdate = vi.fn()
    const onError = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      "onUpdate:validationError": onError,
      modelValue: [1, 2],
      id: id,
      ariaLabel: ariaLabel,
    })

    const editButtons = screen.getAllByRole("button", {
      name: /eintrag bearbeiten/i,
    })
    await user.dblClick(editButtons[0])
    const input = screen.getByRole("textbox")
    await user.keyboard("{backspace}")
    await user.type(input, "9")
    await user.keyboard("{enter}")
    expect(onUpdate).not.toHaveBeenCalledWith([9])
    expect(onError).toHaveBeenCalledWith({
      message: "Randnummer existiert nicht",
      instance: id,
    })
  })

  it("does not add a chip when input is only whitespaces", async () => {
    const onUpdate = vi.fn()
    const { user } = renderComponent({
      "onUpdate:modelValue": onUpdate,
      modelValue: [1, 2],
    })
    const input = screen.getByRole<HTMLInputElement>("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "   {enter}")

    expect(onUpdate).toHaveBeenCalledWith([1, 2])
  })

  it("does not accept valid number when documentationUnit is undefined", async () => {
    const store = useDocumentUnitStore()
    const { documentUnit } = storeToRefs(store)
    documentUnit.value = undefined
    const id = "id"
    const onError = vi.fn()
    const onUpdate = vi.fn()

    const { user } = renderComponent({
      id: id,
      "onUpdate:modelValue": onUpdate,
      "onUpdate:validationError": onError,
    })
    const input = screen.getByRole<HTMLInputElement>("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "1{enter}")

    expect(onUpdate).not.toHaveBeenCalledWith([1])
    expect(onError).toHaveBeenCalledWith({
      message: "Randnummer existiert nicht",
      instance: id,
    })
  })

  it("does not accept number when documentationUnit doesn't contain borderNumbers", async () => {
    const store = useDocumentUnitStore()
    const { documentUnit } = storeToRefs(store)
    documentUnit.value = {
      ...store.documentUnit,
      managementData: {
        borderNumbers: [],
      },
    } as unknown as Decision
    const onError = vi.fn()
    const onUpdate = vi.fn()

    const { user } = renderComponent({
      id: "id",
      "onUpdate:modelValue": onUpdate,
      "onUpdate:validationError": onError,
    })
    const input = screen.getByRole<HTMLInputElement>("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "1{enter}")

    expect(onUpdate).not.toHaveBeenCalledWith([1])
    expect(onError).toHaveBeenCalledWith({
      message: "Randnummer existiert nicht",
      instance: "id",
    })
  })
})
