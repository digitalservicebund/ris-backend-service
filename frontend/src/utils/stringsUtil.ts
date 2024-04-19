export default class StringsUtil {
  public static isEmpty(value: string | undefined): boolean {
    return value == null || value.trim().length == 0
  }
}
