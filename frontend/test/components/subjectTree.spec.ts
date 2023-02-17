/* eslint-disable jest-dom/prefer-in-document */
import { render } from "@testing-library/vue"
import SubjectTree from "@/components/SubjectTree.vue"
import { SubjectNode } from "@/domain/SubjectTree"
import SubjectService from "@/services/subjectsService"

function renderComponent(
  options: {
    selectedSubjects?: SubjectNode[]
  } = {}
) {
  return render(SubjectTree, {
    props: {
      selectedSubjects: options.selectedSubjects ?? [],
      selectedSubjectFieldNumber: "",
    },
  })
}

describe("SubjectTree", () => {
  const fetchSpy = vi
    .spyOn(SubjectService, "getChildrenOf")
    .mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: [
          {
            subjectFieldNumber: "branch",
            subjectFieldText: "Alle Sachgebiete",
            children: [],
            depth: 1,
            isExpanded: false,
            isLeaf: true,
          },
        ],
      })
    )

  it("Is closed", async () => {
    renderComponent()
    expect(fetchSpy).toBeCalledTimes(0)
    // expect(screen.queryByText("Alle Sachgebiete anzeigen")).toBeInTheDocument()
    // expect(screen.queryByText("TE-Subject")).not.toBeInTheDocument()
    // expect(screen.queryByText("TE-Subject-01")).not.toBeInTheDocument()
  })
})
