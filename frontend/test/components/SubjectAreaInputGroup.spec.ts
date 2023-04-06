import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import SubjectAreaInputGroup from "@/components/SubjectAreaInputGroup.vue"

function renderComponent(options?: {
  modelValue?: {
    fna?: string
    previousFna?: string
    gesta?: string
    bgb3?: string
  }
}) {
  const props = {
    modelValue: options?.modelValue ?? {
      fna: "",
      previousFna: "",
      gesta: "",
      bgb3: "",
    },
  }

  return render(SubjectAreaInputGroup, { props })
}

describe("SubjectAreaInputGroup", () => {
  it("renders an input field for the fna value", async () => {
    renderComponent({
      modelValue: { fna: "test value" },
    })

    const input = screen.queryByRole("textbox", {
      name: "FNA-Nummer",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })
  it("renders an input field for the previousFna value", async () => {
    renderComponent({
      modelValue: { previousFna: "test value" },
    })

    const input = screen.queryByRole("textbox", {
      name: "Frühere FNA-Nummer",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })
  it("renders an input field for the gesta value", async () => {
    renderComponent({
      modelValue: {
        gesta: "test value",
      },
    })

    const input = screen.queryByRole("textbox", {
      name: "GESTA-Nummer",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })
  it("renders an input field for the bgb3 value", async () => {
    renderComponent({
      modelValue: {
        bgb3: "test value",
      },
    })

    const input = screen.queryByRole("textbox", {
      name: "Bundesgesetzblatt Teil III",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })

  it("updates the model value when user types into the input fields", async () => {
    const user = userEvent.setup()
    const modelValue = { fna: "", previousFna: "", gesta: "", bgb3: "" }
    renderComponent({ modelValue })

    const fnaInput = screen.queryByRole("textbox", {
      name: "FNA-Nummer",
    }) as HTMLInputElement

    const previousFnaInput = screen.queryByRole("textbox", {
      name: "Frühere FNA-Nummer",
    }) as HTMLInputElement

    const gestaInput = screen.queryByRole("textbox", {
      name: "GESTA-Nummer",
    }) as HTMLInputElement

    const bgb3input = screen.queryByRole("textbox", {
      name: "Bundesgesetzblatt Teil III",
    }) as HTMLInputElement

    await user.type(fnaInput, "foo")
    await user.type(previousFnaInput, "bar")
    await user.type(gestaInput, "baz")
    await user.type(bgb3input, "ban")

    expect(modelValue).toEqual({
      fna: "foo",
      previousFna: "bar",
      gesta: "baz",
      bgb3: "ban",
    })
  })
})
