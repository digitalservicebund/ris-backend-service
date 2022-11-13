<script lang="ts" setup>
import { computed, toRefs } from "vue"
import { useRoute } from "vue-router"
import InputGroup from "@/components/InputGroup.vue"
import { useScrollToHash } from "@/composables/useScrollToHash"
import { Norm } from "@/domain/Norm"
import { normFrameFields } from "@/domain/normFrameFields"

interface Props {
  norm: Norm
}

const props = defineProps<Props>()

const frameData = computed({
  get: () => ({
    longTitle: props.norm.longTitle,
  }),
  set: (data) => console.log(data),
})

const route = useRoute()
const { hash: routeHash } = toRefs(route)
useScrollToHash(routeHash)
</script>

<template>
  <div>
    <h1 id="headlines" class="heading-02-regular mb-[1rem]">Überschriften</h1>

    <InputGroup
      v-model="frameData"
      :column-count="2"
      :fields="normFrameFields"
    />
  </div>
</template>
