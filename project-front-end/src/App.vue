<script setup lang="ts">
import HelloWorld from './components/HelloWorld.vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
dayjs.locale('zh-cn');
</script>

<template>
  <a-config-provider :locale="zhCN">
    <HelloWorld />
  </a-config-provider>
</template>
