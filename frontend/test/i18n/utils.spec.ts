import errors from "@/i18n/errors.json"
import { isErrorCode } from "@/i18n/utils"

describe("i18n utils", () => {
  describe("error code type guard", () => {
    it(`should return true for error codes`, () => {
      for (const code in errors) {
        expect(isErrorCode(code)).toBe(true)
      }
    })

    it("should return false for invalid error codes", () => {
      expect(isErrorCode("DUMMY_CODE")).toBe(false)
    })
  })
})
