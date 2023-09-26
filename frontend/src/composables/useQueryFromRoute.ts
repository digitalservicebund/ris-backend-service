import { ref, watch } from "vue"
import { useRouter, useRoute } from "vue-router"

export type Query<T extends string> = { [key in T]?: string }

export default function useQueries<T extends string>(
  searchCallback: (page: number, query: Query<T>) => Promise<void>,
) {
  const route = useRoute()
  const router = useRouter()

  function getQueriesFromRoute(): Query<T> {
    const query: Partial<Query<T>> = {}

    for (const parameter in route.query) {
      if (parameter in route.query)
        query[parameter as T] = route.query[parameter] as string
    }

    return query
  }

  const query = ref<Query<T>>(getQueriesFromRoute())

  watch(
    query,
    async () => {
      await searchCallback(0, query.value as Query<T>)
      await router.push(query.value ? { query: query.value } : {})
    },
    { deep: true },
  )

  watch(route, () => ((query.value as Query<T>) = getQueriesFromRoute()))

  return query
}
