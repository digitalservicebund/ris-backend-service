export default class StringsUtil {
  public static isEmpty(value: string | undefined): boolean {
    return value == null || value.trim().length == 0
  }

  public static mergeNonBlankStrings(
    words: (string | undefined)[],
    separator: string = ", ",
  ): string {
    return words.filter((item) => !this.isEmpty(item)).join(separator)
  }

  public static readonly countWords = (text: string): number => {
    return text.trim().split(/\s+/).length
  }
}
