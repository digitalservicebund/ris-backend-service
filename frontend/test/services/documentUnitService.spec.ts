import { DocumentUnit, DuplicateRelationStatus } from "@/domain/documentUnit"
import PendingProceeding from "@/domain/pendingProceeding"
import errorMessages from "@/i18n/errors.json"
import service from "@/services/documentUnitService"
import HttpClient from "@/services/httpClient"

describe("documentUnitService", () => {
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
      const data = new DocumentUnit("uuid")
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
})
