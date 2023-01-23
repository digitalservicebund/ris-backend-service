import { InputField, InputType } from "@/domain"

export const otherFootnote: InputField[] = [
  {
    name: "otherFootnote",
    type: InputType.TEXT,
    label: "Sonstige Fußnote",
    inputAttributes: {
      ariaLabel: "Sonstige Fußnote",
    },
  },
  {
    name: "footnoteChange",
    type: InputType.TEXT,
    label: "Änderungsfßnote",
    inputAttributes: {
      ariaLabel: "Änderungsfßnote",
    },
  },
  {
    name: "footnoteComment",
    type: InputType.TEXT,
    label: "Kommentierende Fußnote",
    inputAttributes: {
      ariaLabel: "Kommentierende Fußnote",
    },
  },
  {
    name: "footnoteDecision",
    type: InputType.TEXT,
    label: "BVerfG-Entscheidung",
    inputAttributes: {
      ariaLabel: "BVerfG-Entscheidung",
    },
  },
  {
    name: "footnoteStateLaw",
    type: InputType.TEXT,
    label: "Landesrecht",
    inputAttributes: {
      ariaLabel: "Landesrecht",
    },
  },
  {
    name: "footnoteEuLaw",
    type: InputType.TEXT,
    label: "EU/EG-Recht",
    inputAttributes: {
      ariaLabel: "EU/EG-Recht",
    },
  },
]
