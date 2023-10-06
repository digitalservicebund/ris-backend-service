import { useRouter, useRoute } from "vue-router"

function truncateQuery(query: Query<string>): Query<string> {
  const truncatedQuery: Query<string> = {}

  for (const key in query) {
    if (query[key] != "") truncatedQuery[key] = query[key]
  }
  return truncatedQuery
}

export type Query<T extends string> = { [key in T]?: string }

export default function useQuery<T extends string>() {
  const route = useRoute()
  const router = useRouter()

  function getQueriesFromRoute(): Query<T> {
    const query: Query<T> = {}

    for (const parameter in route.query) {
      query[parameter as T] = route.query[parameter] as string
    }

    return query
  }

  const pushQueriesToRoute = (currentQuerry: Query<T>) =>
    void router.push({ query: truncateQuery(currentQuerry) })

  return { getQueriesFromRoute, pushQueriesToRoute, route }
}
