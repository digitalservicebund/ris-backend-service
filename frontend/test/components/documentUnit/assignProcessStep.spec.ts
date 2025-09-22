import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { UseFetchReturn } from "@vueuse/core"
import Tooltip from "primevue/tooltip"
import { beforeEach } from "vitest"
import { computed, nextTick, Ref, ref } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import AssignProcessStep from "@/components/AssignProcessStep.vue"
import { Decision } from "@/domain/decision"
import { DocumentationUnit } from "@/domain/documentationUnit"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
import errorMessages from "@/i18n/errors.json"
import comboboxItemService from "@/services/comboboxItemService"
import { ResponseError } from "@/services/httpClient"
import processStepService from "@/services/processStepService"
import routes from "~/test-helper/routes"

const currentProcessStep: DocumentationUnitProcessStep = {
  id: "c-id",
  createdAt: new Date(),
  processStep: { uuid: "fertig-id", name: "Fertig", abbreviation: "F" },
}

const docUnitProcessSteps: DocumentationUnitProcessStep[] = [
  currentProcessStep,
  {
    id: "b-id",
    createdAt: new Date(),
    processStep: {
      uuid: "blockiert-id",
      name: "Blockiert",
      abbreviation: "B",
    },
  },
  {
    id: "a-id",
    createdAt: new Date(),
    processStep: { uuid: "neu-id", name: "Neu", abbreviation: "N" },
  },
]

const users = [
  {
    label: "T_U_1",
    value: { id: "user-id-1", externalId: "user-id-1", label: "T_U_1" },
  },
  {
    label: "T_U_2",
    value: { id: "user-id-2", externalId: "user-id-2", label: "T_U_2" },
  },
  {
    label: "T_U_3",
    value: { id: "user-id-3", externalId: "user-id-3", label: "T_U_3" },
  },
]

function mockUseFetchReturn<T>(data: T): UseFetchReturn<T> {
  return {
    isFetching: ref(false),
    isFinished: ref(true),
    statusCode: ref(200),
    response: ref(new Response()),
    error: ref(null),
    data: ref(data) as Ref<T | null>,
    json: vi.fn().mockImplementation(() => mockUseFetchReturn(data)),
    text: vi.fn(),
    onFetchError: vi.fn(),
    onFetchResponse: vi.fn(),
    onFetchFinally: vi.fn(),
    execute: vi.fn(),
    abort: vi.fn(),
    aborted: ref(false),
    get: function (): UseFetchReturn<T> & PromiseLike<UseFetchReturn<T>> {
      throw new Error("Function not implemented.")
    },
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
    head: vi.fn(),
    options: vi.fn(),
    blob: function (): UseFetchReturn<Blob> &
      PromiseLike<UseFetchReturn<Blob>> {
      throw new Error("Function not implemented.")
    },
    arrayBuffer: function (): UseFetchReturn<ArrayBuffer> &
      PromiseLike<UseFetchReturn<ArrayBuffer>> {
      throw new Error("Function not implemented.")
    },
    formData: function (): UseFetchReturn<FormData> &
      PromiseLike<UseFetchReturn<FormData>> {
      throw new Error("Function not implemented.")
    },
    canAbort: computed(() => false),
  }
}

function renderComponent(
  options: {
    documentationUnit?: DocumentationUnit
    handleAssignProcessStep?: (
      documentationUnitProcessStep: DocumentationUnitProcessStep,
    ) => Promise<ResponseError | undefined>
  } = {},
) {
  const user = userEvent.setup()
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })

  // Define a default document unit for single-edit tests
  const defaultDocumentUnit = new Decision("foo", {
    documentNumber: "1234567891234",
    currentDocumentationUnitProcessStep: currentProcessStep,
    processSteps: docUnitProcessSteps,
  })

  const visible = ref(true)

  return {
    user,
    ...render(AssignProcessStep, {
      props: {
        // Use the nullish coalescing operator to correctly handle the prop
        documentationUnit: Object.prototype.hasOwnProperty.call(
          options,
          "documentationUnit",
        )
          ? options.documentationUnit
          : defaultDocumentUnit,
        handleAssignProcessStep:
          options.handleAssignProcessStep ?? (() => Promise.resolve(undefined)),
        visible: visible.value,
        "onUpdate:visible": (value) => {
          visible.value = value
        },
        // Include any other props from the options object
      },
      global: {
        directives: { tooltip: Tooltip },
        plugins: [router],
      },
    }),
    visible,
  }
}
describe("AssignProcessStep component", () => {
  beforeEach(() => {
    vi.spyOn(processStepService, "getNextProcessStep").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {
          uuid: "qs-formal-id",
          name: "QS formal",
          abbreviation: "QS",
        },
      }),
    )
    vi.spyOn(processStepService, "getProcessSteps").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: [
          { uuid: "neu-id", name: "Neu", abbreviation: "N" },
          { uuid: "blockiert-id", name: "Blockiert", abbreviation: "B" },
          { uuid: "qs-formal-id", name: "QS formal", abbreviation: "QS" },
        ],
      }),
    )
    vi.spyOn(comboboxItemService, "getUsersForDocOffice").mockImplementation(
      () => mockUseFetchReturn(users),
    )
  })

  afterEach(() => {
    vi.resetAllMocks()
  })

  test("shows next step, and call handleAssignProcessStepMock on click 'Weitergeben'", async () => {
    const handleAssignProcessStepMock = vi.fn().mockResolvedValue(undefined)
    const { user } = renderComponent({
      handleAssignProcessStep: handleAssignProcessStepMock,
    })

    // Dropdown is preselected with next step
    expect(await screen.findByLabelText("Neuer Schritt")).toHaveTextContent(
      "QS formal",
    )

    // Buttons are visible and not disabled
    const weitergebenButton = await screen.findByLabelText("Weitergeben")
    expect(weitergebenButton).toBeVisible()
    expect(weitergebenButton).toBeEnabled()
    expect(await screen.findByLabelText("Abbrechen")).toBeVisible()

    // Data Table
    expect(await screen.findByRole("table")).toBeVisible()
    expect(screen.getByRole("cell", { name: "Neu" })).toBeVisible()
    expect(screen.getByRole("cell", { name: "Blockiert" })).toBeVisible()
    expect(screen.getByRole("cell", { name: "Fertig" })).toBeVisible()

    await user.click(weitergebenButton)
    expect(handleAssignProcessStepMock).toHaveBeenCalledTimes(1)
  })

  test("closes when the cancel button is clicked", async () => {
    const { user, emitted } = renderComponent()

    const cancelButton = await screen.findByLabelText("Abbrechen")
    await user.click(cancelButton)

    expect(emitted()["update:visible"]).toBeTruthy()
    expect(emitted()["update:visible"][0]).toEqual([false])
  })

  test("shows error message when handleAssignProcessStep call returns an error", async () => {
    const error = {
      title: "Die Dokumentationseinheit konnte nicht weitergegeben werden.",
      description: "Versuchen Sie es erneut.",
    }

    const handleAssignProcessStepMock = vi.fn().mockResolvedValue(error)
    const { user } = renderComponent({
      handleAssignProcessStep: handleAssignProcessStepMock,
    })

    await user.click(await screen.findByLabelText("Weitergeben"))

    await nextTick()

    expect(await screen.findByText(error.title)).toBeInTheDocument()
    expect(await screen.findByText(error.description)).toBeInTheDocument()
  })

  test("shows error modal by error on initial API fetch endpoints", async () => {
    vi.spyOn(processStepService, "getNextProcessStep").mockImplementation(() =>
      Promise.resolve({
        status: 500,
        error:
          errorMessages.NEXT_PROCESS_STEP_FOR_DOCUMENATION_UNIT_COULD_NOT_BE_LOADED,
      }),
    )
    vi.spyOn(processStepService, "getProcessSteps").mockImplementation(() =>
      Promise.resolve({
        status: 500,
        error:
          errorMessages.PROCESS_STEPS_OF_DOCUMENTATION_OFFICE_COULD_NOT_BE_LOADED,
      }),
    )

    renderComponent()

    await nextTick()

    expect(await screen.findByText("Weitergeben")).toBeInTheDocument()
    expect(await screen.findByText("Abbrechen")).toBeInTheDocument()
    expect(
      screen.getByText("Der nächste Schritt konnte nicht geladen werden."),
    ).toBeInTheDocument()
    expect(
      screen.getByText(
        "Wählen Sie einen Schritt aus der Liste aus oder öffnen Sie den Dialog erneut.",
      ),
    ).toBeInTheDocument()
    expect(
      screen.getByText("Die Schritte konnten nicht geladen werden."),
    ).toBeInTheDocument()
    expect(
      screen.getByText("Öffnen Sie den Dialog erneut."),
    ).toBeInTheDocument()
  })

  test("shows error when no process step is selected", async () => {
    vi.spyOn(processStepService, "getNextProcessStep").mockImplementation(() =>
      Promise.resolve({ status: 200, data: undefined }),
    )
    const { user } = renderComponent()

    expect(
      screen.queryByText("Pflichtfeld nicht befüllt"),
    ).not.toBeInTheDocument()

    await user.click(await screen.findByLabelText("Weitergeben"))

    expect(screen.getByText("Pflichtfeld nicht befüllt")).toBeInTheDocument()
  })

  describe("Multi-edit scenarios", () => {
    test("does not show history table and calls correct services in multi-edit scenario", async () => {
      // Arrange: Pass null to documentationUnit to simulate multi-edit
      const handleAssignProcessStepMock = vi.fn().mockResolvedValue(undefined)
      const { user } = renderComponent({
        documentationUnit: undefined,
        handleAssignProcessStep: handleAssignProcessStepMock,
      })

      expect(screen.queryByRole("table")).not.toBeInTheDocument()
      expect(processStepService.getNextProcessStep).not.toHaveBeenCalled()
      expect(await screen.findByLabelText("Weitergeben")).toBeDisabled()

      const select = await screen.findByLabelText("Neuer Schritt")
      await userEvent.click(select)
      await userEvent.click(screen.getByText("Neu"))
      await nextTick()

      const weitergebenButton = screen.getByLabelText("Weitergeben")
      expect(weitergebenButton).toBeEnabled()

      await user.click(weitergebenButton)
      expect(handleAssignProcessStepMock).toHaveBeenCalledTimes(1)
    })

    test("passes selected user to handleAssignProcessStep callback", async () => {
      let capturedArgs
      const handleAssignProcessStepMock = (args: unknown) => {
        capturedArgs = args
        return Promise.resolve(undefined)
      }

      const { user } = renderComponent({
        documentationUnit: undefined,
        handleAssignProcessStep: handleAssignProcessStepMock,
      })

      const processStepSelect = await screen.findByLabelText("Neuer Schritt")
      await user.click(processStepSelect)
      await user.click(screen.getByText("Neu"))

      const userSelect = await screen.findByLabelText("Neue Person")
      await user.click(userSelect)
      await user.click(screen.getByText("T_U_2"))

      await user.click(await screen.findByLabelText("Weitergeben"))

      // Assert that the callback was called with the correct user
      expect(capturedArgs).toBeDefined()
      expect(capturedArgs!.user.externalId).toBe("user-id-2")
      expect(capturedArgs!.processStep.name).toBe("Neu")
    })
  })
})
