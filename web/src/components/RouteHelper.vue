<script lang="ts" setup>
import { onMounted } from "vue"
import { useRoute } from "vue-router"
import { useDocUnitsStore } from "../store"

const route = useRoute()
const docUnitsStore = useDocUnitsStore()

onMounted(() => {
  if (!route.params.id) return
  docUnitsStore.setSelected(route.params.id)
})
</script>
