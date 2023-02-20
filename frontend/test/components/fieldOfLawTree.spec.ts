/* eslint-disable jest-dom/prefer-in-document */
import { render } from "@testing-library/vue"
import FieldOfLawTreeVue from "@/components/FieldOfLawTree.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLawTree"
import FieldOfLawService from "@/services/fieldOfLawService"

function renderComponent(
  options: {
    selectedSubjects?: FieldOfLawNode[]
  } = {}
) {
  return render(FieldOfLawTreeVue, {
    props: {
      selectedSubjects: options.selectedSubjects ?? [],
      clickedSubjectFieldNumber: "",
    },
  })
}

describe("SubjectTree", () => {
  const fetchSpy = vi
    .spyOn(FieldOfLawService, "getChildrenOf")
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
