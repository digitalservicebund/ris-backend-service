import {
  TranslationType,
  TranslationTypeLabels,
} from "@/domain/originOfTranslation"

export const translationType = [
  { label: TranslationTypeLabels.AMTLICH, value: TranslationType.AMTLICH },
  {
    label: TranslationTypeLabels.NICHT_AMTLICH,
    value: TranslationType.NICHT_AMTLICH,
  },
  {
    label: TranslationTypeLabels.KEINE_ANGABE,
    value: TranslationType.KEINE_ANGABE,
  },
]
