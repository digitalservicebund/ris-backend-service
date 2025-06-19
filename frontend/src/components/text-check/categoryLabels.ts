import { longTextLabels, shortTextLabels } from "@/domain/decision"

const textCheckCategoriesGermanLabels: Record<string, string | undefined> = {
  ...longTextLabels,
  ...shortTextLabels,
}

export function getCategoryLabel(
  category: string | undefined,
): string | undefined {
  if (category) {
    return textCheckCategoriesGermanLabels[category]
  }
  return undefined
}
