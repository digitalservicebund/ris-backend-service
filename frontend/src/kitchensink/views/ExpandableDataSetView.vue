<script lang="ts" setup>
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
</script>

<template>
  <ExpandableDataSet
    :data-set="['First Value', 'SecondValue']"
    title="Short Section"
  >
    <div class="h-64">Content</div>
  </ExpandableDataSet>

  <ExpandableDataSet :data-set="['Single Value']" title="Long Section Name">
    <div class="h-64">Content</div>
  </ExpandableDataSet>
</template>
