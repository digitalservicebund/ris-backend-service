export enum FootnoteSectionType {
    FOOTNOTE_REFERENCE = "FOOTNOTE_REFERENCE",
    FOOTNOTE_CHANGE = "FOOTNOTE_CHANGE",
    FOOTNOTE_COMMENT = "FOOTNOTE_COMMENT",
    FOOTNOTE_DECISION = "FOOTNOTE_DECISION",
    FOOTNOTE_STATE_LAW = "FOOTNOTE_STATE_LAW",
    FOOTNOTE_EU_LAW = "FOOTNOTE_EU_LAW",
    FOOTNOTE_OTHER = "FOOTNOTE_OTHER",
}

export type FootnoteSection = {
    type?: FootnoteSectionType
    content?: string
}

export type Footnote = {
    prefix?: string
    parts: FootnoteSection[]
}

export const FOOTNOTE_TYPE_TO_LABEL_MAPPING: Record<FootnoteSectionType, string> = {
    [FootnoteSectionType.FOOTNOTE_CHANGE]: "Änderungsfußnote",
    [FootnoteSectionType.FOOTNOTE_COMMENT]: "Kommentierende Fußnote",
    [FootnoteSectionType.FOOTNOTE_DECISION]: "BVerfG-Entscheidung",
    [FootnoteSectionType.FOOTNOTE_STATE_LAW]: "Landesrecht",
    [FootnoteSectionType.FOOTNOTE_EU_LAW]: "EU/EG-Recht",
    [FootnoteSectionType.FOOTNOTE_OTHER]: "Sonstige Fußnote",
    [FootnoteSectionType.FOOTNOTE_REFERENCE]: "",
}
