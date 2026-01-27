import { Decision } from "@/domain/decision"
import { Kind } from "@/domain/documentationUnitKind"
import { DuplicateRelationStatus } from "@/domain/managementData"
import PendingProceeding from "@/domain/pendingProceeding"
import { RisJsonPatch } from "@/domain/risJsonPatch"
import errorMessages from "@/i18n/errors.json"
import service from "@/services/documentUnitService"
import HttpClient, {
  FailedValidationServerResponse,
} from "@/services/httpClient"

describe("documentUnitService", () => {
  describe("createNew", () => {
    it("should return error message on failure", async () => {
      const httpMock = vi.spyOn(HttpClient, "put").mockResolvedValue({
        status: 400,
        data: "error",
      })

      const response = await service.createNew()

      expect(response.error?.title).toBe(
        errorMessages.DOCUMENT_UNIT_CREATION_FAILED.title,
      )
      expect(httpMock).toHaveBeenCalled()
    })

    it("should return Decision on success", async () => {
      const responseData = new Decision("uuid")
      const httpMock = vi.spyOn(HttpClient, "put").mockResolvedValue({
        status: 200,
        data: responseData,
      })

      const response = await service.createNew()

      expect(response.data).toBeInstanceOf(Decision)
      expect(response.data?.uuid).toBe("uuid")
      expect(httpMock).toHaveBeenCalled()
    })

    it("should return PendingProceeding on success", async () => {
      const responseData = new PendingProceeding("uuid")
      const httpMock = vi.spyOn(HttpClient, "put").mockResolvedValue({
        status: 200,
        data: responseData,
      })

      const response = await service.createNew(undefined, {
        kind: Kind.PENDING_PROCEEDING,
      })

      expect(response.data).toBeInstanceOf(PendingProceeding)
      expect(response.data?.uuid).toBe("uuid")
      expect(httpMock).toHaveBeenCalled()
    })
  })

  it("appends correct error message if status 500", async () => {
    vi.spyOn(HttpClient, "get").mockResolvedValue({
      status: 500,
      data: "foo",
    })

    const result = await service.searchByDocumentUnitSearchInput({
      pg: "0",
      sz: "20",
    })
    expect(result.error?.title).toEqual(
      "Die Suchergebnisse konnten nicht geladen werden.",
    )
    expect(result.error?.description).toEqual(
      "Bitte versuchen Sie es später erneut.",
    )
  })

  describe("setDuplicateRelationStatus", () => {
    it("should set error flag with unsuccessful status", async () => {
      const httpMock = vi.spyOn(HttpClient, "put").mockResolvedValue({
        status: 400,
        data: "error",
      })

      const response = await service.setDuplicateRelationStatus(
        "123",
        "abc",
        DuplicateRelationStatus.IGNORED,
      )

      expect(response).toEqual({ error: true })

      expect(httpMock).toHaveBeenCalledWith(
        "caselaw/documentunits/123/duplicate-status/abc",
        {},
        { status: DuplicateRelationStatus.IGNORED },
      )
    })

    it("should set error flag to false on success", async () => {
      const httpMock = vi.spyOn(HttpClient, "put").mockResolvedValue({
        status: 200,
        data: "success",
      })

      const response = await service.setDuplicateRelationStatus(
        "abc",
        "123",
        DuplicateRelationStatus.PENDING,
      )

      expect(response).toEqual({ error: false })

      expect(httpMock).toHaveBeenCalledWith(
        "caselaw/documentunits/abc/duplicate-status/123",
        {},
        { status: DuplicateRelationStatus.PENDING },
      )
    })
  })

  describe("assign Documentation office", () => {
    it("should return error with unsuccessful status", async () => {
      const httpMock = vi.spyOn(HttpClient, "put").mockResolvedValue({
        status: 400,
        data: "error",
      })

      const response = await service.assignDocumentationOffice(
        "documentationUnitId",
        "documentationOfficeId",
      )

      expect(response).toEqual({
        data: "error",
        error: {
          description: "Laden Sie die Seite neu.",
          title: "Die Dokumentationseinheit konnte nicht zugewiesen werden.",
        },
        status: 400,
      })

      expect(httpMock).toHaveBeenCalledWith(
        `caselaw/documentunits/documentationUnitId/assign/documentationOfficeId`,
      )
    })

    it("should return data on success", async () => {
      const httpMock = vi.spyOn(HttpClient, "put").mockResolvedValue({
        status: 200,
        data: "success",
      })

      const response = await service.assignDocumentationOffice(
        "documentationUnitId",
        "documentationOfficeId",
      )

      expect(response).toEqual({
        data: "success",
        status: 200,
      })

      expect(httpMock).toHaveBeenCalledWith(
        `caselaw/documentunits/documentationUnitId/assign/documentationOfficeId`,
      )
    })
  })

  describe("bulk assign process step", () => {
    it("should return error with unsuccessful status", async () => {
      const httpMock = vi.spyOn(HttpClient, "patch").mockResolvedValue({
        status: 400,
        data: "error",
      })

      const response = await service.bulkAssignProcessStep(
        { processStep: { uuid: "1234", abbreviation: "T", name: "Test" } },
        ["documentationOfficeId"],
      )

      expect(response).toEqual({
        data: "error",
        error: {
          description: "Versuchen Sie es erneut.",
          title:
            "Die Dokumentationseinheit(en) konnten nicht weitergegeben werden.",
        },
        status: 400,
      })

      expect(httpMock).toHaveBeenCalledWith(
        "caselaw/documentunits/bulk-assign-process-step",
        {},
        {
          documentationUnitIds: ["documentationOfficeId"],
          documentationUnitProcessStep: {
            processStep: {
              abbreviation: "T",
              name: "Test",
              uuid: "1234",
            },
          },
        },
      )
    })

    it("should return data on success", async () => {
      const httpMock = vi.spyOn(HttpClient, "patch").mockResolvedValue({
        status: 200,
        data: "success",
      })

      const response = await service.bulkAssignProcessStep(
        { processStep: { uuid: "1234", abbreviation: "T", name: "Test" } },
        ["documentationOfficeId"],
      )

      expect(response).toEqual({
        data: "success",
        status: 200,
      })

      expect(httpMock).toHaveBeenCalledWith(
        "caselaw/documentunits/bulk-assign-process-step",
        {},
        {
          documentationUnitIds: ["documentationOfficeId"],
          documentationUnitProcessStep: {
            processStep: {
              abbreviation: "T",
              name: "Test",
              uuid: "1234",
            },
          },
        },
      )
    })
  })

  describe("get by document number", () => {
    it("should return error with could not be loaded", async () => {
      const httpMock = vi.spyOn(HttpClient, "get").mockResolvedValue({
        status: 400,
        data: "error",
      })

      const response = await service.getByDocumentNumber("documentNumber")

      expect(response).toEqual({
        data: undefined,
        error: {
          title: errorMessages.DOCUMENT_UNIT_COULD_NOT_BE_LOADED.title,
        },
        status: 400,
      })

      expect(httpMock).toHaveBeenCalledWith(
        `caselaw/documentunits/documentNumber`,
      )
    })

    it("should return error with not allowed", async () => {
      const httpMock = vi.spyOn(HttpClient, "get").mockResolvedValue({
        status: 403,
        data: "error",
      })

      const response = await service.getByDocumentNumber("documentNumber")

      expect(response).toEqual({
        data: undefined,
        error: {
          title: errorMessages.DOCUMENT_UNIT_NOT_ALLOWED.title,
        },
        status: 403,
      })

      expect(httpMock).toHaveBeenCalledWith(
        `caselaw/documentunits/documentNumber`,
      )
    })

    it("should return document unit on success", async () => {
      const data = new Decision("uuid")
      const httpMock = vi.spyOn(HttpClient, "get").mockResolvedValue({
        status: 200,
        data: data,
      })

      const response = await service.getByDocumentNumber("documentNumber")

      expect(response).toEqual({
        data: data,
        status: 200,
      })

      expect(httpMock).toHaveBeenCalledWith(
        `caselaw/documentunits/documentNumber`,
      )
    })

    it("should return pending proceeding on success", async () => {
      const data = new PendingProceeding("uuid")
      const httpMock = vi.spyOn(HttpClient, "get").mockResolvedValue({
        status: 200,
        data: data,
      })

      const response = await service.getByDocumentNumber("documentNumber")

      expect(response).toEqual({
        data: data,
        status: 200,
      })

      expect(httpMock).toHaveBeenCalledWith(
        `caselaw/documentunits/documentNumber`,
      )
    })
  })

  describe("update", () => {
    it("should throw PATCH_SIZE_TOO_BIG error when patch exceeds 12 MB", async () => {
      const patchWithOver12Mb: RisJsonPatch = {
        documentationUnitVersion: 1,
        patch: [
          {
            op: "replace",
            path: "/managementData/hugeField",
            value: "x".repeat(13000000), // ~12.4 MB
          },
        ],
        errorPaths: [],
      }

      await expect(
        service.update("documentUnitUuid", patchWithOver12Mb),
      ).rejects.toThrow(errorMessages.PATCH_SIZE_TOO_BIG.title)

      const patchJson = JSON.stringify(patchWithOver12Mb)
      const totalBytes = new Blob([patchJson]).size
      expect(totalBytes).toBeGreaterThan(12 * 1024 * 1024)
    })

    it("should return validation error", async () => {
      vi.spyOn(HttpClient, "patch").mockResolvedValue({
        status: 400,
        data: "Validation failed",
      })
      const risJsonPatch: RisJsonPatch = {
        documentationUnitVersion: 1,
        patch: [
          {
            op: "replace",
            path: "/managementData/lastUpdatedAtDateTime",
            value: "2026-01-16T09:30:00.352783Z",
          },
        ],
        errorPaths: [],
      }

      const response = await service.update("documentNumber", risJsonPatch)

      expect(response.error?.validationErrors).toEqual(
        (response.data as FailedValidationServerResponse).errors,
      )
      expect(response.data).toEqual("Validation failed")
    })

    it("should return DOCUMENT_UNIT_UPDATE_FAILED with 400 and missing validation text", async () => {
      vi.spyOn(HttpClient, "patch").mockResolvedValue({
        status: 400,
        data: "Other text",
      })
      const risJsonPatch: RisJsonPatch = {
        documentationUnitVersion: 1,
        patch: [
          {
            op: "replace",
            path: "/managementData/lastUpdatedAtDateTime",
            value: "2026-01-16T09:30:00.352783Z",
          },
        ],
        errorPaths: [],
      }

      const response = await service.update("documentNumber", risJsonPatch)

      expect(response.error?.title).toEqual(
        errorMessages.DOCUMENT_UNIT_UPDATE_FAILED.title,
      )
      expect(response.error?.validationErrors).toEqual(undefined)
      expect(response.data).toEqual(undefined)
    })

    it("should return NOT_ALLOWED with 403", async () => {
      vi.spyOn(HttpClient, "patch").mockResolvedValue({
        status: 403,
        data: "Some data",
      })
      const risJsonPatch: RisJsonPatch = {
        documentationUnitVersion: 1,
        patch: [
          {
            op: "replace",
            path: "/managementData/lastUpdatedAtDateTime",
            value: "2026-01-16T09:30:00.352783Z",
          },
        ],
        errorPaths: [],
      }

      const response = await service.update("documentNumber", risJsonPatch)

      expect(response.error?.title).toEqual(errorMessages.NOT_ALLOWED.title)
      expect(response.data).toEqual(undefined)
    })

    it("should return DOCUMENT_UNIT_UPDATE_FAILED with 404", async () => {
      vi.spyOn(HttpClient, "patch").mockResolvedValue({
        status: 404,
        data: "Some data",
      })
      const risJsonPatch: RisJsonPatch = {
        documentationUnitVersion: 1,
        patch: [
          {
            op: "replace",
            path: "/managementData/lastUpdatedAtDateTime",
            value: "2026-01-16T09:30:00.352783Z",
          },
        ],
        errorPaths: [],
      }

      const response = await service.update("documentNumber", risJsonPatch)

      expect(response.error?.title).toEqual(
        errorMessages.DOCUMENT_UNIT_UPDATE_FAILED.title,
      )
      expect(response.data).toEqual(undefined)
    })
  })
})
