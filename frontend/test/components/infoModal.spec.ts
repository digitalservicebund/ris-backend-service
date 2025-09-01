import { render, screen } from "@testing-library/vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModal from "@/components/InfoModal.vue"

describe("InfoModal", () => {
  test("renders infomodal: error", () => {
    render(InfoModal, {
      props: {
        title: "foo",
        description: "bar",
      },
    })

    screen.getByText("foo")
    screen.getByText("bar")
    expect(screen.getByLabelText("foo icon")).toBeInTheDocument()
    expect(screen.queryByText("zum externen Link")).not.toBeInTheDocument()
  })

  test("renders infomodal: succeed", () => {
    render(InfoModal, {
      props: {
        title: "foo",
        description: "bar",
        status: InfoStatus.SUCCEED,
      },
    })

    screen.getByText("foo")
    screen.getByText("bar")
    expect(screen.getByLabelText("foo icon")).toBeInTheDocument()
    expect(screen.queryByText("zum externen Link")).not.toBeInTheDocument()
  })

  test("renders info modal with link", () => {
    render(InfoModal, {
      props: {
        title: "foo",
        description: "bar",
        link: {
          displayText: "zum externen Link",
          url: "https://example.com",
        },
      },
    })

    screen.getByText("foo")
    screen.getByText("bar")
    expect(screen.getByText("zum externen Link")).toBeInTheDocument()
    const linkEl = screen.getByRole("link", { name: "zum externen Link" })
    expect(linkEl).toBeInTheDocument()
    expect(linkEl).toHaveAttribute("href", "https://example.com")
    expect(linkEl).toHaveAttribute("target", "_blank")
    expect(linkEl).toHaveAttribute("rel", "noopener noreferrer")
  })

  test("renders info modal with array description and without link", () => {
    render(InfoModal, {
      props: {
        title: "foo",
        description: ["eins", "zwei"],
        link: {
          displayText: "zum externen Link",
          url: "https://example.com",
        },
      },
    })
    screen.getByText("foo")
    screen.getByText("eins")
    screen.getByText("zwei")

    expect(screen.queryByText("zum externen Link")).not.toBeInTheDocument()
  })
})
