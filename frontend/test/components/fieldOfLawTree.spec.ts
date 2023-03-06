/* eslint-disable jest-dom/prefer-in-document */
import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import FieldOfLawTreeVue from "@/components/FieldOfLawTree.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import FieldOfLawService from "@/services/fieldOfLawService"

function renderComponent(
  options: {
    selectedNodes?: FieldOfLawNode[]
  } = {}
) {
  return render(FieldOfLawTreeVue, {
    props: {
      selectedNodes: options.selectedNodes ?? [],
      clickedIdentifier: "",
      showNormsSignal: false,
    },
  })
}

describe("FieldOfLawTree", () => {
  const user = userEvent.setup()

  const fetchSpy = vi
    .spyOn(FieldOfLawService, "getChildrenOf")
    .mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: [
          {
            identifier: "AB-01",
            text: "Text for AB",
            children: [],
            childrenCount: 0,
            norms: [],
            isExpanded: false,
          },
          {
            identifier: "CD-02",
            text: "And text for CD with link to AB-01",
            children: [],
            childrenCount: 1,
            norms: [],
            linkedFields: ["AB-01"],
            isExpanded: false,
          },
        ],
      })
    )

  it("Tree is fully closed upon at start", async () => {
    renderComponent()
    expect(fetchSpy).toBeCalledTimes(0)
    expect(screen.getByText("Alle Sachgebiete anzeigen")).toBeInTheDocument()
    const expandIcons = screen.getAllByLabelText(
      "root Alle Sachgebiete anzeigen aufklappen"
    )
    expect(expandIcons).toHaveLength(1)
    expect(screen.queryByText("Text for AB")).not.toBeInTheDocument()
    expect(screen.queryByText("And text for CD")).not.toBeInTheDocument()
  })

  it("Tree opens top level nodes upon root click", async () => {
    renderComponent()

    await user.click(
      screen.getAllByLabelText(
        "root Alle Sachgebiete anzeigen aufklappen"
      )[0] as HTMLElement
    )

    expect(fetchSpy).toBeCalledTimes(1)
    expect(screen.getByText("Text for AB")).toBeInTheDocument()
    expect(screen.getByText("And text for CD with link to")).toBeInTheDocument()
    expect(screen.getByText("Alle Sachgebiete anzeigen")).toBeInTheDocument()
  })

  it("Linked node gets displayed as link in stext", async () => {
    renderComponent()

    await user.click(
      screen.getAllByLabelText(
        "root Alle Sachgebiete anzeigen aufklappen"
      )[0] as HTMLElement
    )

    const node1ids = screen.getAllByText("AB-01")
    const nonLinkText = screen.getByText("And text for CD with link to")

    expect(node1ids).toHaveLength(2)
    expect(node1ids[1] as HTMLElement).toHaveAttribute("class", "linked-field")
    expect(nonLinkText as HTMLElement).not.toHaveAttribute(
      "class",
      "linked-field"
    )
    // expect(emitted()["linkedField:clicked"]).toHaveLength(1)
  })
})
