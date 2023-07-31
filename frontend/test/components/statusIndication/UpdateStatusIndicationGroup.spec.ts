import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { describe, test } from "vitest"
import UpdateStatusIndicationGroup from "@/components/statusIndication/UpdateStatusIndicationGroup.vue"
import { MetadataSectionName } from "@/domain/Norm"

type UpdateStatusIndicationGroupProps = InstanceType<
  typeof UpdateStatusIndicationGroup
>["$props"]

function renderComponent(props: Partial<UpdateStatusIndicationGroupProps>) {
  const defaultProps: UpdateStatusIndicationGroupProps = {
    modelValue: {},
    ...props,
  }

  return render(UpdateStatusIndicationGroup, { props: defaultProps })
}

describe("UpdateStatusIndicationGroup in status mode", () => {
  test("should render", () => {
    renderComponent({ type: MetadataSectionName.STATUS })
  })

  test("should render all inputs and correct model value", () => {
    renderComponent({
      type: MetadataSectionName.STATUS,
      modelValue: {
        NOTE: ["foo"],
        DESCRIPTION: ["bar"],
        DATE: ["2023-06-29"],
        REFERENCE: ["baz", "qux"],
      },
    })

    const noteInput = screen.getByRole("textbox", { name: "Änderungshinweis" })
    expect(noteInput).toHaveValue("foo")

    const descriptionInput = screen.getByRole("textbox", {
      name: "Bezeichnung der Änderungsvorschrift",
    })
    expect(descriptionInput).toHaveValue("bar")

    const dateInput = screen.getByRole("textbox", {
      name: "Datum der Änderungsvorschrift",
    })
    expect(dateInput).toHaveValue("29.06.2023")

    const references = screen.getAllByRole("listitem")
    expect(references).toHaveLength(2)
    expect(references[0]).toHaveTextContent("baz")
    expect(references[1]).toHaveTextContent("qux")
  })

  test("should change the model value when updating the inputs", async () => {
    const user = userEvent.setup()
    const modelValue = {
      NOTE: ["foo"],
      DESCRIPTION: ["bar"],
      DATE: ["2023-06-29"],
      REFERENCE: ["baz", "qux"],
    }
    renderComponent({ type: MetadataSectionName.STATUS, modelValue })

    const noteInput = screen.getByRole("textbox", { name: "Änderungshinweis" })
    expect(noteInput).toHaveValue("foo")
    await user.type(noteInput, "bar")
    expect(modelValue.NOTE).toEqual(["foobar"])

    const descriptionInput = screen.getByRole("textbox", {
      name: "Bezeichnung der Änderungsvorschrift",
    })
    expect(descriptionInput).toHaveValue("bar")
    await user.type(descriptionInput, "bar")
    expect(modelValue.DESCRIPTION).toEqual(["barbar"])

    const dateInput = screen.getByRole("textbox", {
      name: "Datum der Änderungsvorschrift",
    })
    expect(dateInput).toHaveValue("29.06.2023")
    await userEvent.clear(dateInput)
    await user.type(dateInput, "30.05.2022")
    expect(modelValue.DATE).toEqual(["2022-05-30T00:00:00.000Z"])

    const referencesInput = screen.getByRole("textbox", {
      name: "Fundstellen der Änderungsvorschrift",
    })
    await user.click(referencesInput)
    await user.type(referencesInput, "test{enter}")
    expect(modelValue.REFERENCE).toEqual(["baz", "qux", "test"])
  })

  test("should change the model value when clearing the inputs", async () => {
    const user = userEvent.setup()
    const modelValue = {
      NOTE: ["foo"],
      DESCRIPTION: ["bar"],
      DATE: ["2023-06-29"],
      REFERENCE: ["baz", "qux"],
    }
    renderComponent({ type: MetadataSectionName.STATUS, modelValue })

    const noteInput = screen.getByRole("textbox", { name: "Änderungshinweis" })
    expect(noteInput).toHaveValue("foo")
    await user.clear(noteInput)
    expect(modelValue.NOTE).toBeUndefined()

    const descriptionInput = screen.getByRole("textbox", {
      name: "Bezeichnung der Änderungsvorschrift",
    })
    expect(descriptionInput).toHaveValue("bar")
    await user.clear(descriptionInput)
    expect(modelValue.DESCRIPTION).toBeUndefined()

    const dateInput = screen.getByRole("textbox", {
      name: "Datum der Änderungsvorschrift",
    })
    expect(dateInput).toHaveValue("29.06.2023")
    await userEvent.clear(dateInput)
    expect(modelValue.DATE).toBeUndefined()

    const references = screen.getAllByRole("listitem")
    expect(references.length).toBe(2)
    await user.click(references[0])
    await user.type(references[0], "{enter}")
    await user.click(references[0])
    await user.type(references[0], "{enter}")
    expect(modelValue.REFERENCE).toBeUndefined()
  })
})

describe("UpdateStatusIndicationGroup in reissue mode", () => {
  test("should render", () => {
    renderComponent({ type: MetadataSectionName.REISSUE })
  })

  test("should render all inputs and correct model value", () => {
    renderComponent({
      type: MetadataSectionName.REISSUE,
      modelValue: {
        NOTE: ["foo"],
        ARTICLE: ["bar"],
        DATE: ["2023-06-29"],
        REFERENCE: ["baz"],
      },
    })

    const noteInput = screen.getByRole("textbox", {
      name: "Neufassungshinweis",
    })
    expect(noteInput).toHaveValue("foo")

    const articleInput = screen.getByRole("textbox", {
      name: "Bezeichnung der Bekanntmachung",
    })
    expect(articleInput).toHaveValue("bar")

    const dateInput = screen.getByRole("textbox", {
      name: "Datum der Bekanntmachung",
    })
    expect(dateInput).toHaveValue("29.06.2023")

    const referencesInput = screen.getByRole("textbox", {
      name: "Fundstelle der Bekanntmachung",
    })
    expect(referencesInput).toHaveValue("baz")
  })

  test("should change the model value when updating the inputs", async () => {
    const user = userEvent.setup()
    const modelValue = {
      NOTE: ["foo"],
      ARTICLE: ["bar"],
      DATE: ["2023-06-29"],
      REFERENCE: ["baz", "qux"],
    }
    renderComponent({ type: MetadataSectionName.REISSUE, modelValue })

    const noteInput = screen.getByRole("textbox", {
      name: "Neufassungshinweis",
    })
    expect(noteInput).toHaveValue("foo")
    await user.type(noteInput, "bar")
    expect(modelValue.NOTE).toEqual(["foobar"])

    const articleInput = screen.getByRole("textbox", {
      name: "Bezeichnung der Bekanntmachung",
    })
    expect(articleInput).toHaveValue("bar")
    await user.type(articleInput, "bar")
    expect(modelValue.ARTICLE).toEqual(["barbar"])

    const dateInput = screen.getByRole("textbox", {
      name: "Datum der Bekanntmachung",
    })
    expect(dateInput).toHaveValue("29.06.2023")
    await userEvent.clear(dateInput)
    await user.type(dateInput, "30.05.2022")
    expect(modelValue.DATE).toEqual(["2022-05-30T00:00:00.000Z"])

    const referencesInput = screen.getByRole("textbox", {
      name: "Fundstelle der Bekanntmachung",
    })
    expect(referencesInput).toHaveValue("baz")
    await user.type(referencesInput, "qux")
    expect(modelValue.REFERENCE).toEqual(["bazqux"])
  })

  test("should change the model value when clearing the inputs", async () => {
    const user = userEvent.setup()
    const modelValue = {
      NOTE: ["foo"],
      ARTICLE: ["bar"],
      DATE: ["2023-06-29"],
      REFERENCE: ["baz", "qux"],
    }
    renderComponent({ type: MetadataSectionName.REISSUE, modelValue })

    const noteInput = screen.getByRole("textbox", {
      name: "Neufassungshinweis",
    })
    expect(noteInput).toHaveValue("foo")
    await user.clear(noteInput)
    expect(modelValue.NOTE).toBeUndefined()

    const articleInput = screen.getByRole("textbox", {
      name: "Bezeichnung der Bekanntmachung",
    })
    expect(articleInput).toHaveValue("bar")
    await user.clear(articleInput)
    expect(modelValue.ARTICLE).toBeUndefined()

    const dateInput = screen.getByRole("textbox", {
      name: "Datum der Bekanntmachung",
    })
    expect(dateInput).toHaveValue("29.06.2023")
    await userEvent.clear(dateInput)
    expect(modelValue.DATE).toBeUndefined()

    const referencesInput = screen.getByRole("textbox", {
      name: "Fundstelle der Bekanntmachung",
    })
    expect(referencesInput).toHaveValue("baz")
    await user.clear(referencesInput)
    expect(modelValue.REFERENCE).toBeUndefined()
  })
})
