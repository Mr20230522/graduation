import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

// 公共路由（所有角色都能看到：登录页、首页、404等）
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect')
      }
    ]
  },
  {
    path: '/userLogin',
    component: () => import('@/views/userInterface/userLogin.vue'),
    hidden: true
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: '/booking/paymentReturn',
    component: () => import('@/views/booking/paymentReturn.vue'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: 'index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: '首页', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  },
  // 工具模块（admin 可见，通过 roles 控制）
  {
    path: '/tool',
    component: Layout,
    meta: {
      title: '工具',
      icon: 'tool',
      roles: ['admin']  // 只有 admin 能看到
    },
    children: [
      {
        path: 'build',
        component: () => import('@/views/tool/build'),
        name: 'Build',
        meta: { title: '表单构建', icon: 'build' }
      },
      {
        path: 'gen',
        component: () => import('@/views/tool/gen'),
        name: 'Gen',
        meta: { title: '代码生成', icon: 'code' }
      },
      {
        path: 'swagger',
        component: () => import('@/views/tool/swagger'),
        name: 'Swagger',
        meta: { title: '系统接口', icon: 'swagger' }
      },
      {
        path: 'amap',
        component: () => import('@/views/tool/amap'),
        name: 'Amap',
        meta: { title: '高德地图', icon: 'map' }
      }
    ]
  }
]

// 动态路由（根据角色权限动态加载）
export const dynamicRoutes = [
  // ==================== 车辆预约（common 角色可见） ====================
  {
    path: '/booking',
    component: Layout,
    meta: {
      title: '车辆预约',
      icon: 'dashboard',
      roles: ['common']  // ✅ 只有 common 角色能看到
    },
    children: [
      {
        path: 'deptSelect',
        component: () => import('@/views/booking/deptSelect'),
        name: 'DeptSelect',
        meta: {
          title: '门店选择',
          icon: 'dashboard'
        }
      },
      {
        path: 'calendar',
        component: () => import('@/views/booking/bookingCalendar'),
        name: 'BookingCalendar',
        meta: {
          title: '预约日历',
          icon: 'dashboard'
        }
      },
      {
        path: 'myBooking',
        component: () => import('@/views/booking/myBookingList'),
        name: 'MyBooking',
        meta: {
          title: '我的预约',
          icon: 'dashboard'
        }
      },
      {
        path: 'history',
        component: () => import('@/views/booking/bookingHistory'),
        name: 'BookingHistory',
        meta: {
          title: '历史记录',
          icon: 'dashboard'
        }
      },
      {
        path: '/booking/deptDetail/:deptId',
        component: () => import('@/views/booking/DeptDetail'),
        name: 'DeptDetail',
        meta: {
          title: '门店详情',
          icon: 'shop',
          hidden: true
        }
      },
      {
        path: 'myConsumption',
        component: () => import('@/views/booking/myConsumption'),
        name: 'myConsumption',
        meta: {
          title: '消费一览',
          icon: 'shop'
        }
      }
    ]
  },

  // ==================== 老板管理（boss 角色可见） ====================
  {
    path: '/boss',
    component: Layout,
    meta: {
      title: '老板管理',
      icon: 'el-icon-s-custom',
      roles: ['boss']  // ✅ 只有 boss 角色能看到
    },
    children: [
      {
        path: 'revenue',
        component: () => import('@/views/boss/revenue'),
        name: 'BossRevenue',
        meta: {
          title: '营收看板',
          icon: 'el-icon-data-line'
        }
      },
      {
        path: 'review',
        component: () => import('@/views/boss/review'),
        name: 'BossReview',
        meta: {
          title: '评价管理',
          icon: 'el-icon-star-on'
        }
      },
      {
        path: 'setting',
        component: () => import('@/views/boss/setting'),
        name: 'BossSetting',
        meta: {
          title: '门店设置',
          icon: 'el-icon-setting'
        }
      }
    ]
  },

  // ==================== 系统管理相关（admin 可见） ====================
  {
    path: '/system/user-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:user:edit'],
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/system/user/authRole'),
        name: 'AuthRole',
        meta: { title: '分配角色', activeMenu: '/system/user' }
      }
    ]
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: '分配用户', activeMenu: '/system/role' }
      }
    ]
  },
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    children: [
      {
        path: 'index.vue/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: '字典数据', activeMenu: '/system/dict' }
      }
    ]
  },
  {
    path: '/monitor/job-log',
    component: Layout,
    hidden: true,
    permissions: ['monitor:job:list'],
    children: [
      {
        path: 'index.vue/:jobId(\\d+)',
        component: () => import('@/views/monitor/job/log'),
        name: 'JobLog',
        meta: { title: '调度日志', activeMenu: '/monitor/job' }
      }
    ]
  },
  {
    path: '/tool/gen-edit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:edit'],
    children: [
      {
        path: 'index.vue/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable'),
        name: 'GenEdit',
        meta: { title: '修改生成配置', activeMenu: '/tool/gen' }
      }
    ]
  }
]

// 防止连续点击多次路由报错
let routerPush = Router.prototype.push
let routerReplace = Router.prototype.replace

Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(err => err)
}

Router.prototype.replace = function push(location) {
  return routerReplace.call(this, location).catch(err => err)
}

export default new Router({
  mode: 'history',
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})
