import { InputField, InputType } from "@/shared/components/input/types"

export const otherFootnote: InputField[] = [
  {
    name: "footnoteChange",
    id: "footnoteChange",
    type: InputType.TEXT,
    label: "Änderungsfußnote",
    inputAttributes: {
      ariaLabel: "Änderungsfußnote",
    },
  },
  {
    name: "footnoteComment",
    id: "footnoteComment",
    type: InputType.TEXT,
    label: "Kommentierende Fußnote",
    inputAttributes: {
      ariaLabel: "Kommentierende Fußnote",
    },
  },
  {
    name: "footnoteDecision",
    id: "footnoteDecision",
    type: InputType.TEXT,
    label: "BVerfG-Entscheidung",
    inputAttributes: {
      ariaLabel: "BVerfG-Entscheidung",
    },
  },
  {
    name: "footnoteStateLaw",
    id: "footnoteStateLaw",
    type: InputType.TEXT,
    label: "Landesrecht",
    inputAttributes: {
      ariaLabel: "Landesrecht",
    },
  },
  {
    name: "footnoteEuLaw",
    id: "footnoteEuLaw",
    type: InputType.TEXT,
    label: "EU/EG-Recht",
    inputAttributes: {
      ariaLabel: "EU/EG-Recht",
    },
  },
  {
    name: "otherFootnote",
    id: "otherFootnote",
    type: InputType.TEXT,
    label: "Sonstige Fußnote",
    inputAttributes: {
      ariaLabel: "Sonstige Fußnote",
    },
  },
]
