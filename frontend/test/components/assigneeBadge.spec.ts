import { render, screen } from "@testing-library/vue"
import { describe, expect, it } from "vitest"
import AssigneeBadge from "@/components/AssigneeBadge.vue"

describe("AssigneeBadge", () => {
  it("display name", () => {
    render(AssigneeBadge, {
      props: { name: "CH" },
    })
    expect(screen.getByText("CH")).toBeInTheDocument()
    expect(screen.getByTestId("assignee-icon")).toBeInTheDocument()
  })

  it("display '-'  when name is undefined", () => {
    render(AssigneeBadge, {
      props: {},
    })
    expect(screen.getByText("-")).toBeInTheDocument()
    expect(screen.getByTestId("assignee-icon")).toBeInTheDocument()
  })

  it("display '-' when name is blank", () => {
    render(AssigneeBadge, {
      props: {},
    })
    expect(screen.getByText("-")).toBeInTheDocument()
    expect(screen.getByTestId("assignee-icon")).toBeInTheDocument()
  })
})
