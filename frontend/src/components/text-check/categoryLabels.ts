import { longTextLabels, shortTextLabels } from "@/domain/decision"
import { pendingProceedingLabels } from "@/domain/pendingProceeding"

const textCheckCategoriesGermanLabels: Record<string, string | undefined> = {
  ...longTextLabels,
  ...shortTextLabels,
  ...pendingProceedingLabels,
}

export function getCategoryLabel(
  category: string | undefined,
): string | undefined {
  if (category) {
    return textCheckCategoriesGermanLabels[category]
  }
  return undefined
}
