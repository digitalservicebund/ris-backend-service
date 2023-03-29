import { InputField, InputType } from "@/shared/components/input/types"

export const printAnnouncement: InputField[] = [
  {
    name: "printAnnouncementGazette",
    type: InputType.TEXT,
    label: "Verkündungsblatt",
    inputAttributes: {
      ariaLabel: "Verkündungsblatt",
    },
  },
  {
    name: "printAnnouncementYear",
    type: InputType.TEXT,
    label: "Jahr",
    inputAttributes: {
      ariaLabel: "Jahr",
    },
  },
  {
    name: "printAnnouncementNumber",
    type: InputType.TEXT,
    label: "Nummer",
    inputAttributes: {
      ariaLabel: "Nummer",
    },
  },
  {
    name: "printAnnouncementPage",
    type: InputType.TEXT,
    label: "Seitenzahl",
    inputAttributes: {
      ariaLabel: "Seitenzahl",
    },
  },
  {
    name: "printAnnouncementInfo",
    type: InputType.TEXT,
    label: "Zusatzangaben",
    inputAttributes: {
      ariaLabel: "Zusatzangaben",
    },
  },
  {
    name: "printAnnouncementExplanations",
    type: InputType.TEXT,
    label: "Erläuterungen",
    inputAttributes: {
      ariaLabel: "Erläuterungen",
    },
  },
]
