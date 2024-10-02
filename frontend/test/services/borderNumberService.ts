import { createTestingPinia } from "@pinia/testing"
import { setActivePinia } from "pinia"
import DocumentUnit, { LongTexts, ShortTexts } from "@/domain/documentUnit"
import ParticipatingJudge from "@/domain/participatingJudge"
import borderNumberService from "@/services/borderNumberService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

function mockDocUnitStore({
  shortTexts = {},
  longTexts = {},
}: {
  shortTexts?: ShortTexts
  longTexts?: LongTexts
}) {
  const mockedSessionStore = useDocumentUnitStore()
  mockedSessionStore.documentUnit = new DocumentUnit("q834", {
    shortTexts,
    longTexts,
  })

  return mockedSessionStore
}

function borderNumber(number: number) {
  return `<border-number><number>${number}</number><content></content></border-number>`
}

function borderNumberLink(number: number) {
  return `<border-number-link nr="${number}">${number}</border-number-link>`
}

describe("borderNumberService", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
  })

  it("should handle empty long and short texts", () => {
    mockDocUnitStore({})
    expect(() =>
      borderNumberService.makeBorderNumbersSequential(),
    ).not.toThrow()
  })

  it("should start border numbers with 1", () => {
    const store = mockDocUnitStore({
      longTexts: {
        reasons: borderNumber(2),
      },
    })
    borderNumberService.makeBorderNumbersSequential()
    expect(store.documentUnit?.longTexts.reasons).toEqual(borderNumber(1))
  })

  it("should handle nested border numbers", () => {
    const store = mockDocUnitStore({
      longTexts: {
        reasons: `<p><span>${borderNumber(0)}</span></p>`,
      },
    })
    borderNumberService.makeBorderNumbersSequential()
    expect(store.documentUnit?.longTexts.reasons).toEqual(
      `<p><span>${borderNumber(1)}</span></p>`,
    )
  })

  it("should handle multiple long texts and border numbers", () => {
    const store = mockDocUnitStore({
      longTexts: {
        reasons: `${borderNumber(0)}${borderNumber(10)}`,
        caseFacts: `${borderNumber(4)}${borderNumber(8)}`,
        decisionReasons: `${borderNumber(4)}${borderNumber(8)}`,
        otherLongText: `${borderNumber(2)}${borderNumber(5)}`,
        dissentingOpinion: `${borderNumber(2)}${borderNumber(5)}`,
      },
    })
    borderNumberService.makeBorderNumbersSequential()

    // Normal long texts are sequential
    expect(store.documentUnit?.longTexts.reasons).toEqual(
      `${borderNumber(1)}${borderNumber(2)}`,
    )
    expect(store.documentUnit?.longTexts.caseFacts).toEqual(
      `${borderNumber(3)}${borderNumber(4)}`,
    )
    expect(store.documentUnit?.longTexts.decisionReasons).toEqual(
      `${borderNumber(5)}${borderNumber(6)}`,
    )
    expect(store.documentUnit?.longTexts.otherLongText).toEqual(
      `${borderNumber(7)}${borderNumber(8)}`,
    )

    // dissenting opinion starts from 1 again
    expect(store.documentUnit?.longTexts.dissentingOpinion).toEqual(
      `${borderNumber(1)}${borderNumber(2)}`,
    )
  })

  it("should update existing border links if referenced border number changed", () => {
    const store = mockDocUnitStore({
      longTexts: {
        reasons: `${borderNumber(1)}${borderNumber(3)}`,
        otherLongText: `${borderNumber(1)}${borderNumberLink(3)}`,
      },
      shortTexts: {
        headline: `${borderNumberLink(3)}`,
      },
    })
    borderNumberService.makeBorderNumbersSequential()

    expect(store.documentUnit?.shortTexts.headline).toEqual(
      `${borderNumberLink(2)}`,
    )

    expect(store.documentUnit?.longTexts.otherLongText).toEqual(
      `${borderNumber(3)}${borderNumberLink(2)}`,
    )
  })

  it("should not update an existing border link if unchanged", () => {
    const store = mockDocUnitStore({
      longTexts: {
        reasons: `${borderNumber(1)}${borderNumber(3)}`,
      },
      shortTexts: {
        headline: `${borderNumberLink(1)}${borderNumberLink(2)}`,
      },
    })
    borderNumberService.makeBorderNumbersSequential()

    expect(store.documentUnit?.shortTexts.headline).toEqual(
      `${borderNumberLink(1)}${borderNumberLink(2)}`,
    )
  })

  it("should not change border numbers in other longtexts", () => {
    const judge = new ParticipatingJudge({ name: "judge" })
    const store = mockDocUnitStore({
      longTexts: {
        reasons: `${borderNumber(1)}${borderNumber(3)}`,
        tenor: `${borderNumber(13)}${borderNumber(30)}`,
        participatingJudges: [judge],
      },
    })
    borderNumberService.makeBorderNumbersSequential()

    expect(store.documentUnit?.longTexts.reasons).toEqual(
      `${borderNumber(1)}${borderNumber(2)}`,
    )
    expect(store.documentUnit?.longTexts.tenor).toEqual(
      `${borderNumber(13)}${borderNumber(30)}`,
    )
    expect(store.documentUnit?.longTexts.participatingJudges).toEqual([judge])
  })
})
