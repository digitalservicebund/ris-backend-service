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
  })
})
