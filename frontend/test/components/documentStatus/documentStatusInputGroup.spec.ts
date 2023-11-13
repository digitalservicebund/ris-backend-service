import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import DocumentStatusInputGroup from "@/components/documentStatus/DocumentStatusInputGroup.vue"
import { Metadata } from "@/domain/norm"

type DocumentStatusInputGroupProps = InstanceType<
  typeof DocumentStatusInputGroup
>["$props"]

function renderComponent(props: Partial<DocumentStatusInputGroupProps>) {
  const defaultProps: DocumentStatusInputGroupProps = {
    modelValue: {},
    ...props,
  }

  return render(DocumentStatusInputGroup, { props: defaultProps })
}

describe("DocumentStatusInputGroup", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  test("should render", () => {
    renderComponent({})
  })

  test("should render all inputs and correct model value", () => {
    renderComponent({
      modelValue: {
        WORK_NOTE: ["foo"],
        DESCRIPTION: ["bar"],
        DATE: ["2021-01-01"],
        REFERENCE: ["baz"],
        ENTRY_INTO_FORCE_DATE_NOTE: ["qux", "quux"],
        PROOF_INDICATION: ["quuz"],
      },
    })

    const noteInput = screen.getByRole("textbox", {
      name: "Bearbeitungshinweis",
    })
    expect(noteInput).toHaveValue("foo")

    const descriptionInput = screen.getByRole("textbox", {
      name: "Bezeichnung der Änderungsvorschrift",
    })
    expect(descriptionInput).toHaveValue("bar")

    const dateInput = screen.getByRole("textbox", {
      name: "Datum der Änderungsvorschrift",
    })
    expect(dateInput).toHaveValue("01.01.2021")

    const referenceInput = screen.getByRole("textbox", {
      name: "Fundstelle der Änderungsvorschrift",
    })
    expect(referenceInput).toHaveValue("baz")

    const entryIntoForceDateChips = screen.getAllByTestId("chip")
    expect(entryIntoForceDateChips).toHaveLength(2)
    expect(entryIntoForceDateChips[0]).toHaveTextContent("qux")
    expect(entryIntoForceDateChips[1]).toHaveTextContent("quux")

    const proofIndicationInput = screen.getByRole("textbox", {
      name: "Angaben zum textlichen und/oder dokumentarischen Nachweis",
    })
    expect(proofIndicationInput).toHaveValue("quuz")
  })

  test("should change the model value when updating the inputs", async () => {
    const user = userEvent.setup()
    let modelValue: Metadata = {
      WORK_NOTE: ["foo"],
      DESCRIPTION: ["bar"],
      DATE: ["2021-01-01"],
      REFERENCE: ["baz"],
      ENTRY_INTO_FORCE_DATE_NOTE: ["qux", "quux"],
      PROOF_INDICATION: ["quuz"],
    }
    const updateModelValue = vi.fn().mockImplementation((value: Metadata) => {
      modelValue = value
    })

    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const noteInput = screen.getByRole("textbox", {
      name: "Bearbeitungshinweis",
    })
    expect(noteInput).toHaveValue("foo")
    await user.type(noteInput, "bar")
    expect(modelValue.WORK_NOTE).toEqual(["foobar"])

    const descriptionInput = screen.getByRole("textbox", {
      name: "Bezeichnung der Änderungsvorschrift",
    })
    expect(descriptionInput).toHaveValue("bar")
    await user.type(descriptionInput, "baz")
    expect(modelValue.DESCRIPTION).toEqual(["barbaz"])

    const dateInput = screen.getByRole("textbox", {
      name: "Datum der Änderungsvorschrift",
    })
    expect(dateInput).toHaveValue("01.01.2021")
    await user.clear(dateInput)
    await rerender({ modelValue })
    await user.type(dateInput, "02.02.2021")
    expect(modelValue.DATE).toEqual(["2021-02-02"])

    const referenceInput = screen.getByRole("textbox", {
      name: "Fundstelle der Änderungsvorschrift",
    })
    expect(referenceInput).toHaveValue("baz")
    await user.type(referenceInput, "qux")
    expect(modelValue.REFERENCE).toEqual(["bazqux"])

    const entryIntoForceDateChips = screen.getAllByTestId("chip")
    expect(entryIntoForceDateChips).toHaveLength(2)
    const entryIntoForceDateInput = screen.getByRole("textbox", {
      name: "Datum des Inkrafttretens der Änderung",
    })
    await user.type(entryIntoForceDateInput, "quuux{enter}")
    expect(modelValue.ENTRY_INTO_FORCE_DATE_NOTE).toEqual([
      "qux",
      "quux",
      "quuux",
    ])

    const proofIndicationInput = screen.getByRole("textbox", {
      name: "Angaben zum textlichen und/oder dokumentarischen Nachweis",
    })
    expect(proofIndicationInput).toHaveValue("quuz")
    await user.type(proofIndicationInput, "quuz")
    expect(modelValue.PROOF_INDICATION).toEqual(["quuzquuz"])
  })

  test("should change the model value when clearing the inputs", async () => {
    const user = userEvent.setup()
    let modelValue: Metadata = {
      WORK_NOTE: ["foo"],
      DESCRIPTION: ["bar"],
      DATE: ["2021-01-01"],
      REFERENCE: ["baz"],
      ENTRY_INTO_FORCE_DATE_NOTE: ["qux", "quux"],
      PROOF_INDICATION: ["quuz"],
    }
    const updateModelValue = vi.fn().mockImplementation((value: Metadata) => {
      modelValue = value
    })

    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const noteInput = screen.getByRole("textbox", {
      name: "Bearbeitungshinweis",
    })
    expect(noteInput).toHaveValue("foo")
    await user.clear(noteInput)
    expect(modelValue.WORK_NOTE).toBeUndefined()

    const descriptionInput = screen.getByRole("textbox", {
      name: "Bezeichnung der Änderungsvorschrift",
    })
    expect(descriptionInput).toHaveValue("bar")
    await user.clear(descriptionInput)
    expect(modelValue.DESCRIPTION).toBeUndefined()

    const dateInput = screen.getByRole("textbox", {
      name: "Datum der Änderungsvorschrift",
    })
    expect(dateInput).toHaveValue("01.01.2021")
    await user.clear(dateInput)
    await rerender({ modelValue })
    await user.clear(dateInput)
    expect(modelValue.DATE).toBeUndefined()

    const referenceInput = screen.getByRole("textbox", {
      name: "Fundstelle der Änderungsvorschrift",
    })
    expect(referenceInput).toHaveValue("baz")
    await user.clear(referenceInput)
    expect(modelValue.REFERENCE).toBeUndefined()

    const entryIntoForceDateChips = screen.getAllByTestId("chip")
    expect(entryIntoForceDateChips.length).toBe(2)
    await user.click(entryIntoForceDateChips[0])
    await user.type(entryIntoForceDateChips[0], "{enter}")
    await rerender({ modelValue })
    await user.click(entryIntoForceDateChips[0])
    await user.type(entryIntoForceDateChips[0], "{enter}")
    await user.click(entryIntoForceDateChips[0])
    expect(modelValue.ENTRY_INTO_FORCE_DATE_NOTE).toBeUndefined()

    const proofIndicationInput = screen.getByRole("textbox", {
      name: "Angaben zum textlichen und/oder dokumentarischen Nachweis",
    })
    expect(proofIndicationInput).toHaveValue("quuz")
    await user.clear(proofIndicationInput)
    expect(modelValue.PROOF_INDICATION).toBeUndefined()
  })
})
