import { userEvent } from "@testing-library/user-event"
import { render, screen, waitFor } from "@testing-library/vue"
import { beforeEach, describe, expect, it, type MockInstance, vi } from "vitest"
import FieldOfLawTreeVue from "@/components/field-of-law/FieldOfLawTree.vue"
import { type FieldOfLaw } from "@/domain/fieldOfLaw"
import FieldOfLawService from "@/services/fieldOfLawService"

function renderComponent(
  options: {
    selectedNodes?: FieldOfLaw[]
    nodeOfInterest?: FieldOfLaw
  } = {},
) {
  const user = userEvent.setup()
  return {
    user,
    ...render(FieldOfLawTreeVue, {
      props: {
        selectedNodes: options.selectedNodes ?? [],
        nodeOfInterest: options.nodeOfInterest,
        showNorms: false,
      },
    }),
  }
}

describe("FieldOfLawTree", () => {
  const getChildrenOfRoot = () =>
    Promise.resolve({
      status: 200,
      data: [
        {
          hasChildren: true,
          identifier: "PR",
          text: "Phantasierecht",
          linkedFields: [],
          norms: [],
          children: [],
        },
        {
          hasChildren: true,
          identifier: "AV",
          text: "Allgemeines Verwaltungsrecht",
          linkedFields: [],
          norms: [],
          children: [],
        },
        {
          identifier: "AB-01",
          text: "Text for AB",
          children: [],
          norms: [],
          isExpanded: false,
          hasChildren: false,
        },
        {
          identifier: "CD-02",
          text: "And text for CD with link to AB-01",
          children: [],
          norms: [],
          linkedFields: ["AB-01"],
          isExpanded: false,
          hasChildren: false,
        },
      ],
    })
  const getChildrenOfPR = () =>
    Promise.resolve({
      status: 200,
      data: [
        {
          hasChildren: true,
          identifier: "PR-05",
          text: "Beendigung der Phantasieverhältnisse",
          linkedFields: [],
          norms: [
            {
              abbreviation: "PStG",
              singleNormDescription: "§ 99",
            },
          ],
          children: [],
          parent: {
            hasChildren: true,
            identifier: "PR",
            text: "Phantasierecht",
            linkedFields: [],
            norms: [],
            children: [],
            parent: undefined,
          },
        },
      ],
    })
  const getChildrenOfPRO5 = () =>
    Promise.resolve({
      status: 200,
      data: [
        {
          hasChildren: false,
          identifier: "PR-05-01",
          text: "Phantasie besonderer Art, Ansprüche anderer Art",
          norms: [],
          children: [],
          parent: {
            hasChildren: true,
            identifier: "PR-05",
            text: "Beendigung der Phantasieverhältnisse",
            linkedFields: [],
            norms: [],
            children: [],
            parent: {
              hasChildren: true,
              identifier: "PR",
              text: "Phantasierecht",
              norms: [],
              children: [],
            },
          },
        },
      ],
    })
  const getChildrenOfPR0501 = () =>
    Promise.resolve({
      status: 200,
      data: [
        {
          hasChildren: false,
          identifier: "PR-05-01",
          text: "Phantasie besonderer Art, Ansprüche anderer Art",
          norms: [],
          children: [],
          parent: {
            hasChildren: true,
            identifier: "PR-05",
            text: "Beendigung der Phantasieverhältnisse",
            linkedFields: [],
            norms: [],
            children: [],
            parent: {
              hasChildren: true,
              identifier: "PR",
              text: "Phantasierecht",
              norms: [],
              children: [],
            },
          },
        },
      ],
    })
  const getParentAndChildrenForIdentifierPR05 = () =>
    Promise.resolve({
      status: 200,
      data: {
        hasChildren: true,
        identifier: "PR-05",
        text: "Beendigung der Phantasieverhältnisse",
        norms: [
          {
            abbreviation: "PStG",
            singleNormDescription: "§ 99",
          },
        ],
        children: [
          {
            hasChildren: false,
            identifier: "PR-05-01",
            text: "Phantasie besonderer Art, Ansprüche anderer Art",
            norms: [],
            children: [],
            parent: {
              hasChildren: true,
              identifier: "PR-05",
              text: "Beendigung der Phantasieverhältnisse",
              linkedFields: [],
              norms: [],
              children: [],
              parent: {
                hasChildren: true,
                identifier: "PR",
                text: "Phantasierecht",
                norms: [],
                children: [],
              },
            },
          },
        ],
        parent: {
          id: "a785fb96-a45d-4d4c-8d9c-92d8a6592b22",
          hasChildren: true,
          identifier: "PR",
          text: "Phantasierecht",
          norms: [],
          children: [],
        },
      },
    })

  let fetchSpyGetChildrenOf: MockInstance
  let fetchSpyGetParentAndChildrenForIdentifier: MockInstance

  beforeEach(() => {
    fetchSpyGetChildrenOf = vi
      .spyOn(FieldOfLawService, "getChildrenOf")
      .mockImplementation((identifier: string) => {
        if (identifier == "root") return getChildrenOfRoot()
        else if (identifier == "PR") return getChildrenOfPR()
        else if (identifier == "PR-05") return getChildrenOfPRO5()
        return getChildrenOfPR0501()
      })
    fetchSpyGetParentAndChildrenForIdentifier = vi
      .spyOn(FieldOfLawService, "getParentAndChildrenForIdentifier")
      .mockImplementation(() => {
        return getParentAndChildrenForIdentifierPR05()
      })
  })

  it("Tree is fully closed upon at start", async () => {
    renderComponent()
    expect(fetchSpyGetChildrenOf).toBeCalledTimes(0)
    expect(screen.getByText("Alle Sachgebiete")).toBeInTheDocument()
    expect(
      screen.getByLabelText("Alle Sachgebiete aufklappen"),
    ).toBeInTheDocument()
    expect(screen.queryByText("Text for AB")).not.toBeInTheDocument()
    expect(screen.queryByText("And text for CD")).not.toBeInTheDocument()
  })

  it("Tree opens top level nodes upon root click", async () => {
    const { user } = renderComponent()

    await user.click(screen.getByLabelText("Alle Sachgebiete aufklappen"))

    expect(fetchSpyGetChildrenOf).toBeCalledTimes(1)
    expect(screen.getByText("Text for AB")).toBeInTheDocument()
    expect(
      screen.getByText("And text for CD with link to AB-01"),
    ).toBeInTheDocument()
    expect(screen.getByText("Alle Sachgebiete")).toBeInTheDocument()
  })

  it("Tree opens sub level nodes upon children click", async () => {
    const { user } = renderComponent()

    await user.click(screen.getByLabelText("Alle Sachgebiete aufklappen"))

    await user.click(screen.getByLabelText("Phantasierecht aufklappen"))

    expect(fetchSpyGetChildrenOf).toBeCalledWith("PR")

    expect(
      screen.getByText("Beendigung der Phantasieverhältnisse"),
    ).toBeInTheDocument()
  })

  it("Node of interest is set and corresponding nodes are opened in the tree (other nodes truncated)", async () => {
    renderComponent({
      nodeOfInterest: {
        hasChildren: true,
        identifier: "PR",
        text: "Phantasierecht",
        linkedFields: [],
        norms: [],
        children: [],
      },
    })
    expect(fetchSpyGetParentAndChildrenForIdentifier).toBeCalledTimes(1)

    await waitFor(() => {
      expect(
        screen.getByLabelText("Alle Sachgebiete einklappen"),
      ).toBeInTheDocument()
    })

    await waitFor(() => {
      expect(fetchSpyGetChildrenOf).toBeCalledTimes(2)
    })
    expect(fetchSpyGetChildrenOf).toBeCalledWith("root")
    expect(fetchSpyGetChildrenOf).toBeCalledWith("PR")
    expect(fetchSpyGetChildrenOf).not.toBeCalledWith("PR-05")
    expect(fetchSpyGetChildrenOf).not.toBeCalledWith("PR-05-01")

    await waitFor(() => {
      expect(
        screen.getByText("Phantasie besonderer Art, Ansprüche anderer Art"),
      ).toBeInTheDocument()
    })
    expect(
      screen.queryByText("Allgemeines Verwaltungsrecht"),
    ).not.toBeInTheDocument()
  })

  it("Node of interest is set and corresponding nodes are opened in the tree (other nodes truncated) - when root child node is collapsed all other root children shall be loaded", async () => {
    // given
    const { rerender, user } = renderComponent({
      nodeOfInterest: {
        hasChildren: true,
        identifier: "PR",
        text: "Phantasierecht",
        linkedFields: [],
        norms: [],
        children: [],
      },
    })
    await waitFor(() => {
      expect(
        screen.getByText("Phantasie besonderer Art, Ansprüche anderer Art"),
      ).toBeInTheDocument()
    })

    // when
    // Simulate executing the event 'node-of-interest:reset'
    await rerender({
      nodeOfInterest: undefined,
    })
    await user.click(screen.getByLabelText("Phantasierecht einklappen"))

    // this means one more call for children
    await waitFor(() => {
      expect(fetchSpyGetChildrenOf).toBeCalledTimes(3)
    })

    // then
    await waitFor(() => {
      expect(
        screen.getByText("Allgemeines Verwaltungsrecht"),
      ).toBeInTheDocument()
    })
  })
})
