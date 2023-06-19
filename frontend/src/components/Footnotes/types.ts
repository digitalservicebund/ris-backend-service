import {MetadatumType} from "@/domain/Norm";

export type FootnoteSection = {
    type?: MetadatumType
    content?: string
}

export type Footnote = { FOOTNOTE: {[key:string] : string[]}[] }

export const FOOTNOTE_LABELS = {
    [MetadatumType.FOOTNOTE_CHANGE]: "Änderungsfußnote",
    [MetadatumType.FOOTNOTE_COMMENT]: "Kommentierende Fußnote",
    [MetadatumType.FOOTNOTE_DECISION]: "BVerfG-Entscheidung",
    [MetadatumType.FOOTNOTE_STATE_LAW]: "Landesrecht",
    [MetadatumType.FOOTNOTE_EU_LAW]: "EU/EG-Recht",
    [MetadatumType.FOOTNOTE_OTHER]: "Sonstige Fußnote",
    [MetadatumType.FOOTNOTE_REFERENCE]: "",
}
