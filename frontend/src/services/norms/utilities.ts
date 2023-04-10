/**
 * Group a collection of objects based on a shared key value. The result is a mapping
 * of the existing key values to a list of all objects which share this value.
 *
 * @example
 * ```ts
 * groupBy(
 *   [{ a: 1, b: 1 }, { a: 2, b: 2 }, { a: 1, b: 3 }],
 *   (entry) => entry.a,
 * )
 * // => { 1: [{ a: 1, b: 2 }, { a: 1, b: 3 }], 2: [{ a: 2, b: 2 }]}
 * ```
 *
 * @param collection of object to group by a selected key value
 * @param keySelector callback function to get the key value of each object
 * @returns map with the different key values as entry keys and list of matching
 * objects as entry value
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function groupBy<
  Key extends PropertyKey,
  Entry,
  Selector extends (entry: Entry) => Key
>(collection: Entry[], keySelector: Selector): Record<Key, Entry[]> {
  return collection.reduce((grouped, entry) => {
    const key = keySelector(entry)
    const group = grouped[key] ?? []
    group.push(entry)
    return { ...grouped, [key]: group }
  }, {} as Record<Key, Entry[]>)
}

/**
 * This works somewhat similar to mapping of arrays. Therefore it applies the
 * callback on the value of each object entry.
 * Limiting to strict key value types for strong type constraints in usage.
 *
 * @example
 * ```ts
 * mapValues({ a: 1, b: 2 }, (value: number) => value + 1)
 * // => { a: 2, b: 3 }
 * ```
 *
 * @param object to which the transformer can be applied to
 * @param transformer callback to apply on each object entry value
 * @returns object with transformed/mapped values
 */
export function mapValues<Key extends PropertyKey, Value, Transformed>(
  object: Partial<Record<Key, Value>>,
  transformer: (value: Value, key: Key) => Transformed
): Record<Key, Transformed> {
  return Object.entries(object)
    .map(([key, value]) => [key, transformer(value as Value, key as Key)])
    .reduce(
      (merged, [key, value]) => ({ ...merged, [key as Key]: value }),
      {} as Record<Key, Transformed>
    )
}

/**
 * Merge the values of an object together as a single collection. The difference
 * to `Object.values()` is that if a value is a collection of itself, the values
 * entries get merged too.
 * Limiting to strict value type for strong type constraints in usage.
 *
 * @example
 * ```ts
 * mergeValues({ a: [1, 2], b: 3, c: [4, 5] })
 * // => [1, 2, 3, 4, 5]
 * ```
 *
 * @param object to merge value from
 * @returns collection of all values
 */
export function mergeValues<Type>(
  object: Record<PropertyKey, Type | Type[]>
): Type[] {
  return Object.values(object).reduce(
    (collection: Type[], entry) => [
      ...collection,
      ...(Array.isArray(entry) ? entry : [entry]),
    ],
    []
  )
}

/**
 * Filters the key value entries of an object by a predicate.
 * Limiting to strict key value types for strong type constraints in usage.
 *
 * @example
 * ```ts
 * filterEntries(
 *   { a: 1, b: 2, c: 3, d: 4 },
 *   (key, value) => key == 'a' || value > 3
 * )
 * // => { a: 1, d: 3 }
 * ```
 *
 * @param object to filter the entries for
 * @param predicate callback function
 */
export function filterEntries<Key extends PropertyKey, Value>(
  object: Partial<Record<Key, Value>>,
  predicate: (value: Value, key: Key) => boolean
): Record<Key, Value> {
  return Object.entries(object)
    .filter(([key, value]) => predicate(value as Value, key as Key))
    .reduce(
      (merged, [key, value]) => ({ ...merged, [key]: value }),
      {} as Record<Key, Value>
    )
}

/**
 * Comparator function that compares two objects based on their order property.
 * Intended to be used for sorting where a lowest order comes first.
 *
 * @example
 * ```ts
 * [{ order: 8 }, { order: 2 }].sort(compareOrder)
 * // => [{ order: 2 }, { order: 8 }]
 * ```
 *
 * @param left element
 * @param right element
 * @returns negative if left is comes first, zero if equal, positive if right comes first
 */
export function compareOrder(
  left: { order: number },
  right: { order: number }
): number {
  return left.order - right.order
}
