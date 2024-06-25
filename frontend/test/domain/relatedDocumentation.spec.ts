import { PublicationState } from "@/domain/publicationStatus"
import RelatedDocumentation from "@/domain/relatedDocumentation"

describe("RelatedDocumentation", () => {
  it("renders decision with UNPUBLISHED status and without error", () => {
    const relatedDocumentation = new RelatedDocumentation({
      ...{
        court: {
          label: "courtLabel",
        },
        fileNumber: "fileNumber",
        decisionDate: "01.01.1998",
        documentType: {
          label: "Beschluss",
          jurisShortcut: "shortCut",
        },
        status: {
          publicationStatus: PublicationState.UNPUBLISHED,
          withError: false,
        },
      },
    })
    const decision = relatedDocumentation.renderDecision
    expect(decision).toEqual(
      "courtLabel, 01.01.1998, fileNumber, Beschluss, Unveröffentlicht",
    )
  })

  it("renders decision with UNPUBLISHED status and with error", () => {
    const relatedDocumentation = new RelatedDocumentation({
      ...{
        court: {
          label: "courtLabel",
        },
        fileNumber: "fileNumber",
        decisionDate: "01.01.1998",
        documentType: {
          label: "Beschluss",
          jurisShortcut: "shortCut",
        },
        status: {
          publicationStatus: PublicationState.UNPUBLISHED,
          withError: true,
        },
      },
    })
    const decision = relatedDocumentation.renderDecision
    expect(decision).toEqual(
      "courtLabel, 01.01.1998, fileNumber, Beschluss, Nicht veröffentlicht",
    )
  })

  it("renders decision without status", () => {
    const relatedDocumentation = new RelatedDocumentation({
      ...{
        court: {
          label: "courtLabel",
        },
        fileNumber: "fileNumber",
        decisionDate: "01.01.1998",
        documentType: {
          label: "Beschluss",
          jurisShortcut: "shortCut",
        },
      },
    })
    const decision = relatedDocumentation.renderDecision
    expect(decision).toEqual("courtLabel, 01.01.1998, fileNumber, Beschluss")
  })
})
