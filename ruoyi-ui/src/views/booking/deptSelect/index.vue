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
            <el-card class="dept-item" :body-style="{ padding: '0px' }" shadow="hover" @click.native="selectDept(dept)">
              <div class="dept-image">
                <img :src="dept.image || '@/assets/images/shop-default.png'" alt="门店图片">
                <div class="dept-status" :class="dept.status === '0' ? 'active' : 'inactive'">
                  {{ dept.status === '0' ? '营业中' : '休息中' }}
                </div>
              </div>
              <div class="dept-info">
                <h3>{{ dept.deptName }}</h3>
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
                  <el-button type="primary" size="small" @click.stop="selectDept(dept)">去预约</el-button>
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
import { getDeptList } from '@/api/booking/booking'
import { mapActions } from 'vuex'

export default {
  name: 'DeptSelect',
  data() {
    return {
      loading: false,
      deptList: []
    }
  },
  created() {
    this.loadDeptList()
  },
  methods: {
    ...mapActions('booking', ['setCurrentDept']),

    // 加载门店列表
    loadDeptList() {
      this.loading = true
      getDeptList({ status: 0 }).then(res => {
        this.deptList = res.data || []
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

    // 选择门店
    selectDept(dept) {
      // 保存到 Vuex
      this.setCurrentDept({
        deptId: dept.deptId,
        deptName: dept.deptName,
        address: dept.address,
        phone: dept.phone,
        carSpaceCount: dept.carSpaceCount
      })

      // 跳转到预约日历页面
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
