<template>
  <div class="app-container">
    <el-card class="dept-select-card">
      <div slot="header" class="clearfix">
        <span>选择洗车门店</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="refreshList">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>

      <!-- 门店列表 -->
      <div v-loading="loading" class="dept-list">
        <el-row :gutter="20">
          <el-col :span="8" v-for="dept in deptList" :key="dept.deptId">
            <el-card class="dept-item" :body-style="{ padding: '0px' }" shadow="hover" @click.native="goToDetail(dept)">
              <div class="dept-image">
                <!-- 修改点：图片地址改为动态获取 -->
                <img :src="getDeptImageUrl(dept)" :alt="dept.deptName" @error="handleImageError(dept)">
                <div class="dept-status" :class="dept.status === '0' ? 'active' : 'inactive'">
                  {{ dept.status === '0' ? '营业中' : '休息中' }}
                </div>
              </div>
              <div class="dept-info">
                <h3>{{ dept.deptName }}</h3>

                <!-- 评分显示 -->
                <div class="dept-rating" v-if="dept.ratingStats">
                  <el-rate v-model="dept.ratingStats.avgRating" disabled></el-rate>
                  <span class="review-count">{{ dept.ratingStats.totalReviews }}条评价</span>
                </div>
                <div class="dept-rating" v-else>
                  <el-rate v-model="defaultRating" disabled></el-rate>
                  <span class="review-count">0条评价</span>
                </div>

                <p class="address">
                  <i class="el-icon-location"></i> {{ dept.address || '暂无地址' }}
                </p>
                <p class="phone">
                  <i class="el-icon-phone"></i> {{ dept.phone || '暂无电话' }}
                </p>
                <div class="dept-footer">
                  <span class="car-space">
                    <i class="el-icon-s-grid"></i> {{ dept.carSpaceCount || 0 }}个车位
                  </span>
                  <el-button type="primary" size="small" @click.stop="selectDept(dept)">立即预约</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 空状态 -->
        <el-empty v-if="deptList.length === 0 && !loading" description="暂无可用门店"></el-empty>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getDeptList, getDeptRatingStats } from '@/api/booking/booking'
import { mapActions } from 'vuex'

export default {
  name: 'DeptSelect',
  data() {
    return {
      loading: false,
      deptList: [],
      // 使用后端地址，不是 8000 端口
      imageBaseUrl: process.env.VUE_APP_BASE_API + '/profile',
      // 默认图片路径
      defaultShopImage: '/dept/default.jpg',
      defaultRating: 0
    }
  },
  created() {
    this.loadDeptList()
  },
  methods: {
    ...mapActions('booking', ['setCurrentDept']),

    // 获取门店图片完整URL
    getDeptImageUrl(dept) {
      // 如果数据库有图片地址，直接使用
      if (dept.imageUrl) {
        // 如果地址以 http 开头，直接返回
        if (dept.imageUrl.startsWith('http')) {
          return dept.imageUrl
        }
        // 否则拼接图片服务器地址
        return this.imageBaseUrl + dept.imageUrl
      }
      // 没有图片地址，使用默认图片
      return this.imageBaseUrl + this.defaultShopImage
    },

    // 图片加载失败处理
    handleImageError(dept) {
      // 如果已经尝试过默认图片，就不重复设置了
      if (dept.imageUrl === this.imageBaseUrl + this.defaultShopImage) {
        return
      }
      // 设置默认图片
      dept.imageUrl = this.imageBaseUrl + this.defaultShopImage
    },

    // 加载门店列表
    loadDeptList() {
      this.loading = true
      getDeptList({ status: 0 }).then(res => {
        this.deptList = res.data || []

        // 为每个门店加载评分统计
        this.deptList.forEach(dept => {
          getDeptRatingStats(dept.deptId).then(stats => {
            this.$set(dept, 'ratingStats', stats.data)
          }).catch(() => {
            this.$set(dept, 'ratingStats', { avgRating: 0, totalReviews: 0 })
          })
        })
      }).catch(err => {
        console.error('加载门店列表失败', err)
        this.$message.error('加载门店列表失败')
      }).finally(() => {
        this.loading = false
      })
    },

    // 刷新列表
    refreshList() {
      this.loadDeptList()
    },

    // 跳转到门店详情
    goToDetail(dept) {
      this.$router.push({
        path: `/booking/deptDetail/${dept.deptId}`,
        query: {
          deptName: dept.deptName,
          address: dept.address,
          phone: dept.phone
        }
      })
    },

    // 立即预约 - 存门店到 Vuex 并跳转
    selectDept(dept) {
      this.setCurrentDept({
        deptId: dept.deptId,
        deptName: dept.deptName,
        address: dept.address,
        phone: dept.phone,
        carSpaceCount: dept.carSpaceCount || 0,
        imageUrl: dept.imageUrl  // 新增：传递图片地址
      })
      this.$router.push('/booking/calendar')
    }
  }
}
</script>

<style scoped>
.dept-select-card {
  min-height: 600px;
}

.dept-list {
  padding: 20px;
}

.dept-item {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.dept-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
}

.dept-image {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.dept-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dept-status {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #fff;
}

.dept-status.active {
  background: #67c23a;
}

.dept-status.inactive {
  background: #909399;
}

.dept-info {
  padding: 15px;
}

.dept-info h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #333;
}

.dept-rating {
  display: flex;
  align-items: center;
  margin: 8px 0;
  gap: 10px;
}

.dept-rating .el-rate {
  flex: 1;
}

.review-count {
  color: #909399;
  font-size: 12px;
  white-space: nowrap;
}

.dept-info p {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}

.dept-info i {
  margin-right: 5px;
  color: #409eff;
}

.dept-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.car-space {
  color: #e6a23c;
  font-size: 13px;
}

.car-space i {
  color: #e6a23c;
}
</style>
