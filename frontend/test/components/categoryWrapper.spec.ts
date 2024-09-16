import { render, screen } from "@testing-library/vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"

describe("category wrapper", () => {
  test("should display button instead of category component", async () => {
    const label = "Test label"
    render(CategoryWrapper, {
      props: {
        label: label,
        shouldShowButton: true,
      },
    })

    expect(screen.getByTestId("category-wrapper-button")).toBeInTheDocument()
    expect(
      screen.queryByTestId("category-wrapper-component"),
    ).not.toBeInTheDocument()
  })

  test("should display category component instead of button", async () => {
    const label = "Test label"
    render(CategoryWrapper, {
      props: {
        label: label,
        shouldShowButton: false,
      },
      slots: {
        default: "Just any category component",
      },
    })

    expect(
      screen.queryByTestId("category-wrapper-button"),
    ).not.toBeInTheDocument()
    expect(screen.getByTestId("category-wrapper-component")).toBeInTheDocument()
  })
})
