import { fireEvent, render, screen } from "@testing-library/vue"
import { defineComponent } from "vue"
import AssignProcedure from "@/components/AssignProcedure.vue"

describe("AssignProcedure", () => {
  it("should initially show combobox and button without error message", () => {
    render(AssignProcedure)

    expect(screen.getByLabelText("Vorgang auswählen")).toBeVisible()
    expect(
      screen.getByRole("button", { name: "Zu Vorgang hinzufügen" }),
    ).toBeVisible()
    expect(
      screen.queryByText("Wählen Sie einen Vorgang aus"),
    ).not.toBeInTheDocument()
  })

  it("Clicking the button without selecting a procedure should show an error", async () => {
    render(AssignProcedure)

    await fireEvent.click(
      screen.getByRole("button", { name: "Zu Vorgang hinzufügen" }),
    )
    // Error message should be shown
    expect(screen.getByText("Wählen Sie einen Vorgang aus")).toBeVisible()
  })

  it("Clicking the button with a selected procedure should emit an event", async () => {
    const { emitted } = render(AssignProcedure, {
      global: {
        stubs: {
          ComboboxInput: defineComponent({
            // eslint-disable-next-line vue/require-prop-types
            props: ["modelValue"],
            emits: ["update:modelValue"],
            template: `
              <button @click="$emit('update:modelValue', {label: 'Vorgangsname'})">Vorgang wählen</button>
          `,
          }),
        },
      },
    })

    // Simulate selecting a procedure from the combobox
    await fireEvent.click(screen.getByText("Vorgang wählen"))

    await fireEvent.click(
      screen.getByRole("button", { name: "Zu Vorgang hinzufügen" }),
    )

    expect(emitted().assignProcedure[0]).toEqual([{ label: "Vorgangsname" }])
    // No error message should be shown
    expect(
      screen.queryByText("Wählen Sie einen Vorgang aus"),
    ).not.toBeInTheDocument()
  })
})
