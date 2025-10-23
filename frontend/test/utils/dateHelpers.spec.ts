import { describe, it, expect } from "vitest"
import { parseIsoDateToLocal, parseLocalDateToIso } from "@/utils/dateUtil"

describe("Date Helpers", () => {
  describe("parseIsoDateToLocal", () => {
    it("should correctly parse a valid ISO date to local format", () => {
      const isoDate = "2023-10-05"
      const expectedLocalDate = "05.10.2023"
      expect(parseIsoDateToLocal(isoDate)).toBe(expectedLocalDate)
    })

    it("should return null for an invalid ISO date", () => {
      const invalidIsoDate = "1970-00-00"
      expect(parseIsoDateToLocal(invalidIsoDate)).toBeNull()
    })
  })

  describe("parseLocalDateToIso", () => {
    it("should correctly parse a valid local date to ISO format", () => {
      const localDate = "05.10.2023"
      const expectedIsoDate = "2023-10-05"
      expect(parseLocalDateToIso(localDate)).toBe(expectedIsoDate)
    })

    it("should return null for an invalid local date", () => {
      const invalidLocalDate = "05-10-2023"
      expect(parseLocalDateToIso(invalidLocalDate)).toBeNull()
    })
  })
})
