import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { describe } from "vitest"
import CourtBranchLocationComponent from "@/components/CourtBranchLocation.vue"
import { CourtBranchLocation } from "@/domain/courtBranchLocation"

function renderComponent(
  courtBranchLocations?: CourtBranchLocation[],
  modelValue?: CourtBranchLocation,
) {
  const user = userEvent.setup()

  return {
    user,
    ...render(CourtBranchLocationComponent, {
      props: {
        courtBranchLocations: courtBranchLocations,
        modelValue: modelValue,
      },
    }),
  }
}

describe("court branch location", () => {
  describe("with branch locations", () => {
    describe("with model value", () => {
      it("display model value without warning", async () => {
        renderComponent([{ value: "Augsburg", id: "1" }], {
          value: "Augsburg",
          id: "1",
        })
        expect(
          screen.getByRole("combobox", { name: "Sitz der Außenstelle" }),
        ).toHaveTextContent("Augsburg")
      })

      it("display model value with warning if not part of branch locations", async () => {
        renderComponent([{ value: "Ingolstadt", id: "2" }], {
          value: "Augsburg",
          id: "1",
        })
        expect(
          screen.getByRole("combobox", { name: "Sitz der Außenstelle" }),
        ).toHaveTextContent("Augsburg")
        expect(
          screen.getByText("Gehört nicht zum ausgewählten Gericht"),
        ).toBeVisible()
      })
    })

    describe("without model value", () => {
      it("display options in dropdown and select one", async () => {
        const { user, emitted } = renderComponent([
          { value: "Augsburg", id: "1" },
          { value: "Kammer Ingolstadt", id: "2" },
        ])

        expect(
          screen.getByRole("combobox", { name: "Sitz der Außenstelle" }),
        ).toHaveTextContent("Bitte auswählen")

        await user.click(
          screen.getByRole("combobox", { name: "Sitz der Außenstelle" }),
        )
        expect(screen.getByLabelText("Augsburg")).toBeInTheDocument()
        expect(screen.getByLabelText("Kammer Ingolstadt")).toBeInTheDocument()

        await user.click(screen.getByLabelText("Augsburg"))

        expect(
          screen.queryByLabelText("Sitz der Außenstelle"),
        ).toHaveTextContent("Augsburg")
        expect(emitted()["update:modelValue"]).toEqual([
          [{ value: "Augsburg", id: "1" }],
        ])
      })
    })
  })

  describe("without branch locations", () => {
    describe("with model value", () => {
      it("display model value with warning", async () => {
        renderComponent([], { value: "Augsburg", id: "1" })
        expect(
          screen.getByRole("combobox", { name: "Sitz der Außenstelle" }),
        ).toHaveTextContent("Augsburg")
        expect(
          screen.getByText("Gehört nicht zum ausgewählten Gericht"),
        ).toBeVisible()
      })
    })
    describe("without model value", () => {
      it("show placeholder, be disabled and emit undefined model value", async () => {
        const { emitted } = renderComponent()
        expect(
          screen.getByRole("combobox", { name: "Sitz der Außenstelle" }),
        ).toHaveTextContent("Bitte auswählen")

        expect(
          screen.getByRole("combobox", { name: "Sitz der Außenstelle" }),
        ).toHaveAttribute("aria-disabled", "true")

        expect(emitted()["update:modelValue"]).toBeUndefined()
      })
    })
  })
})
