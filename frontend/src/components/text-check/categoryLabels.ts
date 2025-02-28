import { longTextLabels, shortTextLabels } from "@/domain/documentUnit"

const germanLabels: Record<string, string | undefined> = {
  ...longTextLabels,
  ...shortTextLabels,
}

function getCategoryLabel(category: string | undefined): string | undefined {
  if (category) {
    return germanLabels[category]
  }
  return undefined
}

function getCategoryLabels(categories: string[]): string[] {
  const textCategories: string[] = []

  categories.forEach((category) => {
    const label = getCategoryLabel(category)
    if (label) {
      textCategories.push(label)
    }
  })

  return textCategories
}

export { getCategoryLabel, getCategoryLabels }
