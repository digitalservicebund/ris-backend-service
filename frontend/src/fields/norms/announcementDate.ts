import { InputField, InputType } from "@/shared/components/input/types"

export const announcementDate: InputField[] = [
  {
    name: "announcementDate",
    type: InputType.DATE,
    label: "Verkündungsdatum",
    inputAttributes: {
      ariaLabel: "Verkündungsdatum",
      isFutureDate: true,
    },
  },
  {
    name: "publicationDate",
    type: InputType.DATE,
    label: "Veröffentlichungsdatum",
    inputAttributes: {
      ariaLabel: "Veröffentlichungsdatum",
      isFutureDate: true,
    },
  },
]
