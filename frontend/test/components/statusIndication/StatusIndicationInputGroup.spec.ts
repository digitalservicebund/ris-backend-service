import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { describe, test } from "vitest"
import StatusIndicationInputGroup from "@/components/statusIndication/StatusIndicationInputGroup.vue"

type StatusIndicationInputGroup = InstanceType<
  typeof StatusIndicationInputGroup
>["$props"]

function renderComponent(props: Partial<StatusIndicationInputGroup>) {
  const defaultProps: StatusIndicationInputGroup = {
    modelValue: {},
    ...props,
  }

  return render(StatusIndicationInputGroup, { props: defaultProps })
}

describe("StatusIndicationInputGroup", () => {
  test("should render", () => {
    renderComponent({})
  })

  it("defaults to the repeal group", () => {
    renderComponent({})
    const typeRadio = screen.getByRole("radio", { name: "Aufhebung" })
    expect(typeRadio).toBeChecked()
  })

  it("selects the status group", () => {
    renderComponent({ modelValue: { STATUS: [{}] } })
    const typeRadio = screen.getByRole("radio", { name: "Stand" })
    expect(typeRadio).toBeChecked()
  })

  it("renders the status details", async () => {
    renderComponent({})
    const user = userEvent.setup()
    const typeRadio = screen.getByRole("radio", { name: "Stand" })
    await user.click(typeRadio)

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
    renderComponent({})
    const user = userEvent.setup()
    const typeRadio = screen.getByRole("radio", { name: "Neufassung" })
    await user.click(typeRadio)

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
    renderComponent({ modelValue: { REISSUE: [{}] } })
    const user = userEvent.setup()
    const typeRadio = screen.getByRole("radio", { name: "Aufhebung" })
    await user.click(typeRadio)

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
    renderComponent({})
    const user = userEvent.setup()
    const typeRadio = screen.getByRole("radio", { name: "Sonstiger Hinweis" })
    await user.click(typeRadio)

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

  it("resets the data model when the type is changed", async () => {
    const { emitted } = renderComponent({
      modelValue: {
        REISSUE: [
          {
            NOTE: ["foo"],
            ARTICLE: ["bar"],
            DATE: ["baz"],
            REFERENCE: ["qux"],
          },
        ],
      },
    })

    const user = userEvent.setup()

    const otherStatusRadio = screen.getByRole("radio", {
      name: "Sonstiger Hinweis",
    })
    await user.click(otherStatusRadio)
    expect(emitted("update:modelValue")[1]).toEqual([{ OTHER_STATUS: [{}] }])

    const repealRadio = screen.getByRole("radio", { name: "Neufassung" })
    await user.click(repealRadio)
    expect(emitted("update:modelValue")[2]).toEqual([{ REISSUE: [{}] }])
  })
})
