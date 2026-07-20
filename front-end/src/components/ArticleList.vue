<template>
  <a-list item-layout="horizontal" :data-source="props.articleList">
    <template #renderItem="{ item }">
      <a-list-item>
        <a-list-item-meta :description="item.content">
          <template #title>
            <a href="https://www.antdv.com/" v-html="item.highlightTitle"></a>
          </template>
          <template #avatar>
            <a-avatar :src="gege" />
          </template>
        </a-list-item-meta>
      </a-list-item>
    </template>
  </a-list>
</template>

<script setup lang="ts">
import gege from '../assets/gege.jpg'
import {defineProps, withDefaults} from 'vue'

interface Props {
  articleList: any[]
}

const props = withDefaults(defineProps<Props>(), {
  articleList: () => [],
})
</script>

<style scoped>
.gege {
  width: 200px;
}

:deep(em) {
  background-color: #ffeb3b;
  font-weight: 600;
  font-style: normal;
  padding: 0 4px;
  border-radius: 2px;
}
</style>
