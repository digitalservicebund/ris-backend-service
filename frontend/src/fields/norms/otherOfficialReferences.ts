import { InputField, InputType } from "@/shared/components/input/types"

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
