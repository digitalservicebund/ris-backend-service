import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import StatusIndicationInputGroup from "@/components/statusIndication/StatusIndicationInputGroup.vue"
import { MetadataSections } from "@/domain/norm"

type StatusIndicationInputGroup = InstanceType<
  typeof StatusIndicationInputGroup
>["$props"]

function renderComponent(props: Partial<StatusIndicationInputGroup>) {
  let modelValue: MetadataSections = {}

  const defaultProps: StatusIndicationInputGroup = {
    modelValue,
    "onUpdate:modelValue": (value: MetadataSections) => (modelValue = value),
    ...props,
  }

  return render(StatusIndicationInputGroup, { props: defaultProps })
}

describe("StatusIndicationInputGroup", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  it("should render", () => {
    renderComponent({})
  })

  it("defaults to the status group", () => {
    renderComponent({})
    const typeRadio = screen.getByRole("radio", { name: "Stand" })
    expect(typeRadio).toBeChecked()
  })

  it("selects the status group", () => {
    renderComponent({ modelValue: { STATUS: [{}] } })
    const typeRadio = screen.getByRole("radio", { name: "Stand" })
    expect(typeRadio).toBeChecked()
  })

  it("renders the status details", async () => {
    let modelValue: MetadataSections = { REPEAL: [{}] }
    const updateModelValue = vi
      .fn()
      .mockImplementation((value: MetadataSections) => {
        modelValue = value
      })

    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const user = userEvent.setup()
    const typeRadio = screen.getByRole("radio", { name: "Stand" })
    await user.click(typeRadio)
    expect(updateModelValue).toHaveBeenCalledWith({ STATUS: [{}] })
    await rerender({ modelValue })

    const inputFromGroup = screen.getByRole("textbox", {
      name: "Änderungshinweis",
    })
    expect(inputFromGroup).toBeVisible()
  })

  it("emits a model update when status child section data is updated", async () => {
    const { emitted } = renderComponent({ modelValue: { STATUS: [{}] } })
    const user = userEvent.setup()

    const inputFromGroup = screen.getByRole("textbox", {
      name: "Änderungshinweis",
    })

    await user.type(inputFromGroup, "foo")
    expect(emitted("update:modelValue")).toBeTruthy()
  })

  it("selects the reissue group", () => {
    renderComponent({ modelValue: { REISSUE: [{}] } })
    const typeRadio = screen.getByRole("radio", { name: "Neufassung" })
    expect(typeRadio).toBeChecked()
  })

  it("renders the reissue details", async () => {
    let modelValue: MetadataSections = {}
    const updateModelValue = vi
      .fn()
      .mockImplementation((value: MetadataSections) => {
        modelValue = value
      })

    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const user = userEvent.setup()
    const typeRadio = screen.getByRole("radio", { name: "Neufassung" })
    await user.click(typeRadio)
    expect(updateModelValue).toHaveBeenCalledWith({ REISSUE: [{}] })
    await rerender({ modelValue })

    const inputFromGroup = screen.getByRole("textbox", {
      name: "Neufassungshinweis",
    })
    expect(inputFromGroup).toBeVisible()
  })

  it("emits a model update when reissue child section data is updated", async () => {
    const { emitted } = renderComponent({ modelValue: { REISSUE: [{}] } })
    const user = userEvent.setup()

    const inputFromGroup = screen.getByRole("textbox", {
      name: "Neufassungshinweis",
    })

    await user.type(inputFromGroup, "foo")
    expect(emitted("update:modelValue")).toBeTruthy()
  })

  it("selects the repeal group", () => {
    renderComponent({ modelValue: { REPEAL: [{}] } })
    const typeRadio = screen.getByRole("radio", { name: "Aufhebung" })
    expect(typeRadio).toBeChecked()
  })

  it("renders the repeal details", async () => {
    let modelValue: MetadataSections = {}
    const updateModelValue = vi
      .fn()
      .mockImplementation((value: MetadataSections) => {
        modelValue = value
      })

    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const user = userEvent.setup()
    const typeRadio = screen.getByRole("radio", { name: "Aufhebung" })
    await user.click(typeRadio)
    expect(updateModelValue).toHaveBeenCalledWith({ REPEAL: [{}] })
    await rerender({ modelValue })

    const inputFromGroup = screen.getByRole("textbox", {
      name: "Aufhebung",
    })
    expect(inputFromGroup).toBeVisible()
  })

  it("emits a model update when repeal child section data is updated", async () => {
    const { emitted } = renderComponent({ modelValue: { REPEAL: [{}] } })
    const user = userEvent.setup()

    const inputFromGroup = screen.getByRole("textbox", {
      name: "Aufhebung",
    })

    await user.type(inputFromGroup, "foo")
    expect(emitted("update:modelValue")).toBeTruthy()
  })

  it("selects the other status group", () => {
    renderComponent({ modelValue: { OTHER_STATUS: [{}] } })
    const typeRadio = screen.getByRole("radio", {
      name: "Sonstiger Hinweis",
    })
    expect(typeRadio).toBeChecked()
  })

  it("renders the other status details", async () => {
    let modelValue: MetadataSections = {}
    const updateModelValue = vi
      .fn()
      .mockImplementation((value: MetadataSections) => {
        modelValue = value
      })

    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const user = userEvent.setup()
    const typeRadio = screen.getByRole("radio", { name: "Sonstiger Hinweis" })
    await user.click(typeRadio)
    expect(updateModelValue).toHaveBeenCalledWith({ OTHER_STATUS: [{}] })
    await rerender({ modelValue })

    const inputFromGroup = screen.getByRole("textbox", {
      name: "Sonstiger Hinweis",
    })
    expect(inputFromGroup).toBeVisible()
  })

  it("emits a model update when other status child section data is updated", async () => {
    const { emitted } = renderComponent({ modelValue: { OTHER_STATUS: [{}] } })
    const user = userEvent.setup()

    const inputFromGroup = screen.getByRole("textbox", {
      name: "Sonstiger Hinweis",
    })

    await user.type(inputFromGroup, "foo")
    expect(emitted("update:modelValue")).toBeTruthy()
  })

  it("restores the original data after switching types", async () => {
    let modelValue: MetadataSections = {
      REISSUE: [
        {
          NOTE: ["foo"],
          ARTICLE: ["bar"],
          DATE: [""],
          REFERENCE: ["qux"],
        },
      ],
    }

    const updateModelValue = vi
      .fn()
      .mockImplementation((value: MetadataSections) => {
        modelValue = value
      })

    const { emitted, rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const user = userEvent.setup()

    const otherStatusRadio = screen.getByRole("radio", {
      name: "Sonstiger Hinweis",
    })
    await user.click(otherStatusRadio)
    await rerender({ modelValue })
    expect(emitted("update:modelValue")[0]).toEqual([{ OTHER_STATUS: [{}] }])

    const repealRadio = screen.getByRole("radio", { name: "Neufassung" })
    await user.click(repealRadio)
    await rerender({ modelValue })
    expect(emitted("update:modelValue")[1]).toEqual([
      {
        REISSUE: [
          {
            NOTE: ["foo"],
            ARTICLE: ["bar"],
            DATE: [""],
            REFERENCE: ["qux"],
          },
        ],
      },
    ])
  })
})
