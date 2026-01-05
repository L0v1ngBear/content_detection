import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            redirect: '/front/home'
        },
        {
            path: "/front",
            component: () => import('../views/Front.vue'),
            children:
            [
                { path: 'home', name: 'fr_home', meta: {title: '首页'}, component: () => import('../views/front/Home.vue')},
                { path: 'picture', name: 'picture', meta: {title: '图片检测'}, component: () => import('../views/front/Picture.vue')},
                { path: 'history', name: 'history', meta: {title: '历史记录'}, component: () => import('../views/front/History.vue')},
                { path: 'setting', name: 'setting', meta: {title: '设置'}, component: () => import('../views/front/Setting.vue')}
                ]
        },
        {   path: '/home', name: 'home', meta: {title: '首页'}, component: () => import('../views/Home.vue')
        },
        {
            path: '/login', name: 'login', meta:{title: '登录'}, component: () =>import('../views/Login.vue')
        },
        {
            path: '/register', name: 'register', meta:{title: '注册'}
        }
    ],
})

const publicRoutes = ["login", "404", 'register', 'home', 'fr_home', 'picture', 'history', 'setting'];

router.beforeEach((to, from, next) => {
    // 4.1 提取本地存储的登录凭证（accessToken）
    const accessToken = localStorage.getItem("accessToken");

    // 4.2 判断当前要跳转的路由是否为公开路由
    const isPublicRoute = publicRoutes.includes(to.name);

    document.title = to.meta.title

    // 4.3 授权校验逻辑分支
    if (isPublicRoute) {
        next();
    } else {
        if (accessToken) {
            next();
        } else {
            next({
                name: "login",
                query: { redirect: to.fullPath }
            });
        }
    }
});
export default router