import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/pages/IndexPage.vue'),
    },
    {
      path: '/:category',
      component: () => import('@/pages/IndexPage.vue'),
    },
  ],
})

export default router
