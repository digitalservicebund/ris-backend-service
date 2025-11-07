import { fireEvent, render, screen } from "@testing-library/vue"
import { MockInstance } from "vitest"
import BulkAssignProcedure from "@/components/BulkAssignProcedure.vue"
import { PublicationState } from "@/domain/publicationStatus"
import DocumentUnitService from "@/services/documentUnitService"
import { ServiceResponse } from "@/services/httpClient"

const AssignProcedureStub = {
  template: `<span @click="$emit('assign-procedure', {label: 'Vorgangsname'})">AssignProcedureMock</span>`,
}

const addToastMock = vi.fn()
vi.mock("primevue/usetoast", () => ({
  useToast: () => ({ add: addToastMock }),
}))

describe("BulkAssignProcedure", () => {
  afterEach(() => {
    vi.clearAllMocks()
  })
  let bulkAssignServiceSpy: MockInstance<
    (
      procedureLabel: string,
      documentationUnitIds: string[],
    ) => Promise<ServiceResponse<unknown>>
  >
  beforeEach(() => {
    bulkAssignServiceSpy = vi.spyOn(DocumentUnitService, "bulkAssignProcedure")
  })

  it("should emit an error when no docUnits are selected", async () => {
    const { emitted } = render(BulkAssignProcedure, {
      props: { documentationUnits: undefined },
      global: { stubs: { AssignProcedure: AssignProcedureStub } },
    })

    await fireEvent.click(screen.getByText("AssignProcedureMock"))

    expect(emitted().updateSelectionErrors[0]).toEqual([
      "Wählen Sie mindestens eine Dokumentationseinheit aus.",
      [],
    ])
    expect(emitted().procedureAssigned).toBeUndefined()
    expect(bulkAssignServiceSpy).not.toHaveBeenCalled()
  })

  it("should emit an error when an empty list of docUnits is selected", async () => {
    const { emitted } = render(BulkAssignProcedure, {
      props: { documentationUnits: [] },
      global: { stubs: { AssignProcedure: AssignProcedureStub } },
    })

    await fireEvent.click(screen.getByText("AssignProcedureMock"))

    expect(emitted().updateSelectionErrors[0]).toEqual([
      "Wählen Sie mindestens eine Dokumentationseinheit aus.",
      [],
    ])
    expect(emitted().procedureAssigned).toBeUndefined()
    expect(bulkAssignServiceSpy).not.toHaveBeenCalled()
  })

  it("should emit an error when a docUnit with status Fremdanlage is selected", async () => {
    const { emitted } = render(BulkAssignProcedure, {
      props: {
        documentationUnits: [
          {
            uuid: "1",
            status: {
              publicationStatus: PublicationState.EXTERNAL_HANDOVER_PENDING,
            },
          },
          {
            uuid: "2",
            status: {
              publicationStatus: PublicationState.UNPUBLISHED,
            },
          },
        ],
      },
      global: { stubs: { AssignProcedure: AssignProcedureStub } },
    })

    await fireEvent.click(screen.getByText("AssignProcedureMock"))

    expect(emitted().updateSelectionErrors[0]).toEqual([
      "Nehmen Sie die Fremdanlage an, um sie zu einem Vorgang hinzuzufügen",
      ["1"],
    ])
    expect(emitted().procedureAssigned).toBeUndefined()
    expect(bulkAssignServiceSpy).not.toHaveBeenCalled()
  })

  it("should call the service with valid input and not show an error on success", async () => {
    bulkAssignServiceSpy.mockResolvedValue({ data: {}, status: 200 })

    const { emitted } = render(BulkAssignProcedure, {
      props: {
        documentationUnits: [
          {
            uuid: "8123",
            status: {
              publicationStatus: PublicationState.UNPUBLISHED,
            },
          },
        ],
      },
      global: { stubs: { AssignProcedure: AssignProcedureStub } },
    })

    await fireEvent.click(screen.getByText("AssignProcedureMock"))

    expect(emitted().updateSelectionErrors[0]).toEqual([undefined, []])
    expect(emitted().procedureAssigned[0]).toEqual([])
    expect(bulkAssignServiceSpy).toHaveBeenCalledExactlyOnceWith(
      "Vorgangsname",
      ["8123"],
    )
    expect(addToastMock).toHaveBeenCalledExactlyOnceWith({
      detail: "Die Dokumentationseinheit ist jetzt im Vorgang Vorgangsname.",
      life: 5000,
      severity: "success",
      summary: "Hinzufügen erfolgreich",
    })
    expect(
      screen.queryByTestId("bulk-assign-procedure-success"),
    ).not.toBeInTheDocument()
  })

  it("should call the service with valid input and show an error on failure", async () => {
    bulkAssignServiceSpy.mockResolvedValue({
      status: 400,
      error: { title: "Fehler" },
    })
    const { emitted } = render(BulkAssignProcedure, {
      props: {
        documentationUnits: [
          {
            uuid: "4321",
            status: {
              publicationStatus: PublicationState.UNPUBLISHED,
            },
          },
          {
            uuid: "1234",
            status: {
              publicationStatus: PublicationState.PUBLISHED,
            },
          },
        ],
      },
      global: { stubs: { AssignProcedure: AssignProcedureStub } },
    })

    await fireEvent.click(screen.getByText("AssignProcedureMock"))

    expect(emitted().updateSelectionErrors[0]).toEqual([undefined, []])
    expect(emitted().procedureAssigned).toBeUndefined()
    expect(bulkAssignServiceSpy).toHaveBeenCalledExactlyOnceWith(
      "Vorgangsname",
      ["4321", "1234"],
    )
    expect(screen.getByTestId("bulk-assign-procedure-error")).toHaveTextContent(
      "Die Dokumentationseinheit(en) konnten nicht zum Vorgang hinzugefügt werden.",
    )
  })
})
