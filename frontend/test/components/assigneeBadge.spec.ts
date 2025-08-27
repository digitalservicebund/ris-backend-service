import { render, screen } from "@testing-library/vue"
import { describe, expect, it } from "vitest"
import AssigneeBadge from "@/components/AssigneeBadge.vue"

describe("AssigneeBadge", () => {
  it("display name", () => {
    render(AssigneeBadge, {
      props: { name: "CH" },
    })
    expect(screen.getByText("CH")).toBeInTheDocument()
    expect(screen.getByTestId("assigned-person")).toBeInTheDocument()
  })

  it.each([
    { case: "undefined", props: {} },
    { case: "blank string", props: { name: "" } },
  ])("display '-' when name is %s", ({ props }) => {
    render(AssigneeBadge, { props })

    expect(screen.getByText("-")).toBeInTheDocument()
    expect(screen.getByTestId("assigned-person")).toBeInTheDocument()
  })
})
