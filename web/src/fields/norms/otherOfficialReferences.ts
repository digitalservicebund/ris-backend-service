import { InputField, InputType } from "@/domain"

export const otherOfficialReferences: InputField[] = [
  {
    name: "otherOfficialAnnouncement",
    type: InputType.TEXT,
    label: "Sonstige amtliche Fundstelle",
    inputAttributes: {
      ariaLabel: "Sonstige amtliche Fundstelle",
    },
  },
]
