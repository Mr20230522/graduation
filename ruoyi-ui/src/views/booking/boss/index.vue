<template>
  <div class="boss-container">
    <!-- 门店信息卡片 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover" class="dept-card">
          <div slot="header">
            <span><i class="el-icon-shop"></i> 门店信息</span>
          </div>
          <div class="dept-info">
            <el-descriptions :column="3" border>
              <el-descriptions-item label="门店名称">{{ deptInfo.deptName }}</el-descriptions-item>
              <el-descriptions-item label="负责人">{{ deptInfo.leader }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ deptInfo.phone }}</el-descriptions-item>
              <el-descriptions-item label="门店地址" :span="2">{{ deptInfo.address }}</el-descriptions-item>
              <el-descriptions-item label="车位数量">{{ deptInfo.carSpaceCount }}个</el-descriptions-item>
              <el-descriptions-item label="营业执照号">{{ deptInfo.license || '未填写' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ deptInfo.email || '未填写' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tab选项卡 -->
    <el-card class="tab-card">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <!-- 营收看板 -->
        <el-tab-pane label="营收看板" name="revenue">
          <div class="statistics-cards">
            <el-row :gutter="20">
              <el-col :span="6">
                <div class="stat-card revenue-card">
                  <div class="stat-title">总营收</div>
                  <div class="stat-value">¥{{ formatMoney(statistics.totalRevenue) }}</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-card order-card">
                  <div class="stat-title">总订单数</div>
                  <div class="stat-value">{{ statistics.totalOrders || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-card review-card">
                  <div class="stat-title">总评价数</div>
                  <div class="stat-value">{{ statistics.totalReviews || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-card rating-card">
                  <div class="stat-title">平均评分</div>
                  <div class="stat-value">
                    <span>{{ statistics.avgRating || 0 }}</span>
                    <el-rate
                      v-model="statistics.avgRating"
                      disabled
                      show-score
                      text-color="#ff9900"
                      score-template="{value}分"
                      style="display: inline-block; width: auto; margin-left: 10px;">
                    </el-rate>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>

          <!-- 营收趋势图 -->
          <div class="revenue-chart">
            <div class="chart-header">
              <span><i class="el-icon-data-line"></i> 营收趋势</span>
              <el-radio-group v-model="trendDays" size="small" @change="loadRevenueTrend">
                <el-radio-button :label="7">近7天</el-radio-button>
                <el-radio-button :label="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
            <div ref="revenueChart" style="height: 350px; margin-top: 20px;"></div>
          </div>
        </el-tab-pane>

        <!-- 评价管理 -->
        <el-tab-pane label="评价管理" name="review">
          <div class="review-filter">
            <el-form :inline="true" :model="reviewQuery" size="small">
              <el-form-item label="评分">
                <el-select v-model="reviewQuery.rating" placeholder="全部" clearable @change="loadReviews">
                  <el-option label="5星" value="5"></el-option>
                  <el-option label="4星" value="4"></el-option>
                  <el-option label="3星" value="3"></el-option>
                  <el-option label="2星" value="2"></el-option>
                  <el-option label="1星" value="1"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="有图评价">
                <el-select v-model="reviewQuery.hasImage" placeholder="全部" clearable @change="loadReviews">
                  <el-option label="有图" value="1"></el-option>
                  <el-option label="无图" value="0"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="el-icon-search" @click="loadReviews">查询</el-button>
                <el-button icon="el-icon-refresh" @click="resetReviewQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="reviewList" v-loading="reviewLoading" border stripe>
            <el-table-column prop="userName" label="用户" width="120">
              <template slot-scope="scope">
                <div class="user-info">
                  <el-avatar :src="scope.row.avatar" :size="32">
                    <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"/>
                  </el-avatar>
                  <span style="margin-left: 8px;">{{ scope.row.isAnonymous == 1 ? '匿名用户' : scope.row.userName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="rating" label="评分" width="140">
              <template slot-scope="scope">
                <el-rate v-model="scope.row.rating" disabled show-score text-color="#ff9900" score-template="{value}分"></el-rate>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="评价内容" min-width="250">
              <template slot-scope="scope">
                <div class="review-content">{{ scope.row.content }}</div>
                <div v-if="scope.row.images" class="review-images">
                  <el-image
                    v-for="(img, idx) in scope.row.images.split(',')"
                    :key="idx"
                    :src="img"
                    :preview-src-list="scope.row.images.split(',')"
                    fit="cover"
                    style="width: 50px; height: 50px; margin-right: 5px; cursor: pointer;">
                  </el-image>
                </div>
                <div class="review-time">{{ scope.row.createTime }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="carNumber" label="车牌号" width="120"></el-table-column>
            <el-table-column prop="replyContent" label="商家回复" width="200">
              <template slot-scope="scope">
                <div v-if="scope.row.replyContent" class="reply-content">
                  {{ scope.row.replyContent }}
                  <div class="reply-time">{{ scope.row.replyTime }}</div>
                </div>
                <div v-else class="reply-btn">
                  <el-button type="text" size="small" @click="showReplyDialog(scope.row)">回复</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <pagination
            v-show="reviewTotal > 0"
            :total="reviewTotal"
            :page.sync="reviewQuery.pageNum"
            :limit.sync="reviewQuery.pageSize"
            @pagination="loadReviews" />
        </el-tab-pane>

        <!-- 门店设置 -->
        <el-tab-pane label="门店设置" name="setting">
          <el-tabs type="border-card">
            <!-- 基本信息 -->
            <el-tab-pane label="基本信息">
              <el-form :model="deptForm" :rules="deptRules" ref="deptForm" label-width="100px">
                <el-form-item label="门店名称" prop="deptName">
                  <el-input v-model="deptForm.deptName" placeholder="请输入门店名称"></el-input>
                </el-form-item>
                <el-form-item label="负责人" prop="leader">
                  <el-input v-model="deptForm.leader" placeholder="请输入负责人"></el-input>
                </el-form-item>
                <el-form-item label="联系电话" prop="phone">
                  <el-input v-model="deptForm.phone" placeholder="请输入联系电话"></el-input>
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="deptForm.email" placeholder="请输入邮箱"></el-input>
                </el-form-item>
                <el-form-item label="门店地址" prop="address">
                  <el-input type="textarea" v-model="deptForm.address" :rows="3" placeholder="请输入门店地址"></el-input>
                </el-form-item>
                <el-form-item label="营业执照号" prop="license">
                  <el-input v-model="deptForm.license" placeholder="请输入营业执照号"></el-input>
                </el-form-item>
                <el-form-item label="车位数量" prop="carSpaceCount">
                  <el-input-number v-model="deptForm.carSpaceCount" :min="1" :max="50"></el-input-number>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="updateDeptInfo">保存修改</el-button>
                  <el-button @click="loadDeptInfo">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 营业时间 -->
            <el-tab-pane label="营业时间">
              <div class="schedule-tips">
                <el-alert title="设置未来7天的营业时间，每天可独立设置" type="info" :closable="false"></el-alert>
              </div>
              <el-table :data="scheduleList" border style="margin-top: 20px;" v-loading="scheduleLoading">
                <el-table-column prop="workDate" label="日期" width="120">
                  <template slot-scope="scope">
                    {{ formatDate(scope.row.workDate) }}
                  </template>
                </el-table-column>
                <el-table-column label="营业状态" width="100">
                  <template slot-scope="scope">
                    <el-switch
                      v-model="scope.row.status"
                      :active-value="0"
                      :inactive-value="1"
                      active-text="营业"
                      inactive-text="休息">
                    </el-switch>
                  </template>
                </el-table-column>
                <el-table-column label="开门时间" width="150">
                  <template slot-scope="scope">
                    <el-time-select
                      v-if="scope.row.status == 0"
                      v-model="scope.row.openTime"
                      :picker-options="{ start: '00:00', step: '00:30', end: '23:30' }"
                      placeholder="选择时间">
                    </el-time-select>
                    <span v-else class="gray-text">--:--</span>
                  </template>
                </el-table-column>
                <el-table-column label="关门时间" width="150">
                  <template slot-scope="scope">
                    <el-time-select
                      v-if="scope.row.status == 0"
                      v-model="scope.row.closeTime"
                      :picker-options="{ start: '00:00', step: '00:30', end: '23:30', minTime: scope.row.openTime }"
                      placeholder="选择时间">
                    </el-time-select>
                    <span v-else class="gray-text">--:--</span>
                  </template>
                </el-table-column>
                <el-table-column label="时段时长(分钟)" width="130">
                  <template slot-scope="scope">
                    <el-input-number
                      v-if="scope.row.status == 0"
                      v-model="scope.row.slotMinutes"
                      :min="15"
                      :max="120"
                      :step="15">
                    </el-input-number>
                    <span v-else class="gray-text">--</span>
                  </template>
                </el-table-column>
                <el-table-column label="可用车位数" width="120">
                  <template slot-scope="scope">
                    <el-input-number
                      v-if="scope.row.status == 0"
                      v-model="scope.row.carSpaces"
                      :min="1"
                      :max="deptForm.carSpaceCount">
                    </el-input-number>
                    <span v-else class="gray-text">--</span>
                  </template>
                </el-table-column>
              </el-table>
              <div style="margin-top: 20px; text-align: center;">
                <el-button type="primary" @click="saveSchedule" :loading="scheduleSaving">保存排班</el-button>
              </div>
            </el-tab-pane>

            <!-- 车位管理 -->
            <el-tab-pane label="车位管理">
              <el-table :data="carSpaceList" border v-loading="carSpaceLoading">
                <el-table-column prop="spaceNo" label="车位编号" width="150"></el-table-column>
                <el-table-column prop="spaceName" label="车位名称" width="200"></el-table-column>
                <el-table-column label="状态" width="150">
                  <template slot-scope="scope">
                    <el-switch
                      v-model="scope.row.status"
                      :active-value="0"
                      :inactive-value="1"
                      active-text="启用"
                      inactive-text="禁用"
                      @change="updateCarSpaceStatus(scope.row)">
                    </el-switch>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 回复评价对话框 -->
    <el-dialog title="回复评价" :visible.sync="replyDialogVisible" width="500px">
      <el-form :model="replyForm">
        <el-form-item label="回复内容">
          <el-input type="textarea" v-model="replyForm.replyContent" rows="5" placeholder="请输入回复内容..."></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  getDeptInfo,
  getStatistics,
  getRevenueTrend,
  getReviewList,
  bossReplyReview,
  updateDept,
  getSchedule,
  updateSchedule,
  getCarSpaces,
  updateCarSpace
} from '@/api/booking/booking'

export default {
  name: "BossIndex",
  data() {
    return {
      activeTab: 'revenue',
      deptInfo: {},
      deptForm: {},
      statistics: {
        totalRevenue: 0,
        totalOrders: 0,
        totalReviews: 0,
        avgRating: 0
      },
      trendDays: 7,
      revenueChart: null,
      reviewList: [],
      reviewTotal: 0,
      reviewLoading: false,
      reviewQuery: {
        rating: '',
        hasImage: '',
        pageNum: 1,
        pageSize: 10
      },
      replyDialogVisible: false,
      replyForm: {
        reviewId: null,
        replyContent: ''
      },
      scheduleList: [],
      scheduleLoading: false,
      scheduleSaving: false,
      carSpaceList: [],
      carSpaceLoading: false,
      deptRules: {
        deptName: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
        leader: [{ required: true, message: '请输入负责人', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadDeptInfo()
    this.loadStatistics()
    this.loadRevenueTrend()
  },
  methods: {
    // 加载门店信息
    loadDeptInfo() {
      getDeptInfo().then(res => {
        this.deptInfo = res.data
        this.deptForm = { ...res.data }
      }).catch(() => {
        this.$message.error('获取门店信息失败')
      })
    },

    // 加载统计数据
    loadStatistics() {
      getStatistics().then(res => {
        this.statistics = res.data
      }).catch(() => {
        this.$message.error('获取统计数据失败')
      })
    },

    // 加载营收趋势
    loadRevenueTrend() {
      getRevenueTrend(this.trendDays).then(res => {
        this.$nextTick(() => {
          this.initChart(res.data)
        })
      }).catch(() => {
        this.$message.error('获取营收趋势失败')
      })
    },

    // 初始化图表
    initChart(data) {
      if (this.revenueChart) {
        this.revenueChart.dispose()
      }
      this.revenueChart = echarts.init(this.$refs.revenueChart)
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        legend: {
          data: ['营收金额', '订单数'],
          top: 0
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: data.map(item => item.date),
          axisLabel: {
            rotate: data.length > 10 ? 45 : 0
          }
        },
        yAxis: [
          {
            type: 'value',
            name: '营收(元)',
            nameTextStyle: { color: '#409EFF' }
          },
          {
            type: 'value',
            name: '订单数',
            nameTextStyle: { color: '#67C23A' }
          }
        ],
        series: [
          {
            name: '营收金额',
            type: 'line',
            data: data.map(item => item.revenue),
            smooth: true,
            itemStyle: { color: '#409EFF' },
            lineStyle: { width: 3 },
            areaStyle: { opacity: 0.1 }
          },
          {
            name: '订单数',
            type: 'bar',
            data: data.map(item => item.orderCount),
            yAxisIndex: 1,
            itemStyle: { color: '#67C23A' },
            barWidth: '40%'
          }
        ]
      }
      this.revenueChart.setOption(option)
      window.addEventListener('resize', () => {
        if (this.revenueChart) {
          this.revenueChart.resize()
        }
      })
    },

    // 加载评价列表
    loadReviews() {
      this.reviewLoading = true
      getReviewList(this.reviewQuery).then(res => {
        this.reviewList = res.rows
        this.reviewTotal = res.total
        this.reviewLoading = false
      }).catch(() => {
        this.reviewLoading = false
        this.$message.error('获取评价列表失败')
      })
    },

    // 重置评价查询
    resetReviewQuery() {
      this.reviewQuery.rating = ''
      this.reviewQuery.hasImage = ''
      this.reviewQuery.pageNum = 1
      this.loadReviews()
    },

    // 显示回复对话框
    showReplyDialog(row) {
      this.replyForm.reviewId = row.reviewId
      this.replyForm.replyContent = ''
      this.replyDialogVisible = true
    },

    // 提交回复
    submitReply() {
      if (!this.replyForm.replyContent) {
        this.$message.warning('请输入回复内容')
        return
      }
      bossReplyReview(this.replyForm).then(() => {
        this.$message.success('回复成功')
        this.replyDialogVisible = false
        this.loadReviews()
      }).catch(() => {
        this.$message.error('回复失败')
      })
    },

    // 更新门店信息
    updateDeptInfo() {
      this.$refs.deptForm.validate(valid => {
        if (valid) {
          updateDept(this.deptForm).then(() => {
            this.$message.success('保存成功')
            this.loadDeptInfo()
          }).catch(() => {
            this.$message.error('保存失败')
          })
        }
      })
    },

    // 加载排班信息
    loadSchedule() {
      this.scheduleLoading = true
      getSchedule().then(res => {
        this.scheduleList = res.data || []
        // 如果不足7天，补全未来7天
        if (this.scheduleList.length < 7) {
          const existingDates = this.scheduleList.map(s => s.workDate.split(' ')[0])
          for (let i = 0; i < 7; i++) {
            const date = new Date()
            date.setDate(date.getDate() + i)
            const dateStr = date.toISOString().split('T')[0]
            if (!existingDates.includes(dateStr)) {
              this.scheduleList.push({
                workDate: dateStr,
                openTime: '09:00',
                closeTime: '18:00',
                slotMinutes: 30,
                carSpaces: this.deptForm.carSpaceCount || 5,
                status: 0
              })
            }
          }
          this.scheduleList.sort((a, b) => a.workDate.localeCompare(b.workDate))
        }
        this.scheduleLoading = false
      }).catch(() => {
        this.scheduleLoading = false
        this.$message.error('获取排班信息失败')
      })
    },

    // 保存排班
    saveSchedule() {
      this.scheduleSaving = true
      updateSchedule(this.scheduleList).then(() => {
        this.$message.success('保存成功')
        this.scheduleSaving = false
      }).catch(() => {
        this.scheduleSaving = false
        this.$message.error('保存失败')
      })
    },

    // 加载车位列表
    loadCarSpaces() {
      this.carSpaceLoading = true
      getCarSpaces().then(res => {
        this.carSpaceList = res.data || []
        this.carSpaceLoading = false
      }).catch(() => {
        this.carSpaceLoading = false
        this.$message.error('获取车位列表失败')
      })
    },

    // 更新车位状态
    updateCarSpaceStatus(row) {
      const params = {
        spaceNo: row.spaceNo,
        status: row.status
      }
      updateCarSpace(params).then(() => {
        this.$message.success('更新成功')
      }).catch(() => {
        row.status = row.status === 0 ? 1 : 0
        this.$message.error('更新失败')
      })
    },

    // Tab切换时加载数据
    handleTabClick(tab) {
      if (tab.name === 'review' && this.reviewList.length === 0) {
        this.loadReviews()
      } else if (tab.name === 'setting') {
        if (this.scheduleList.length === 0) {
          this.loadSchedule()
        }
        if (this.carSpaceList.length === 0) {
          this.loadCarSpaces()
        }
      }
    },

    // 格式化金额
    formatMoney(value) {
      if (!value) return '0.00'
      return parseFloat(value).toFixed(2)
    },

    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return ''
      return dateStr
    }
  }
}
</script>

<style scoped lang="scss">
.boss-container {
  padding: 20px;

  .dept-card {
    margin-bottom: 20px;

    ::v-deep .el-card__header {
      background-color: #f5f7fa;
      font-weight: bold;
    }

    .dept-info {
      ::v-deep .el-descriptions__body {
        background-color: #fafafa;
      }
    }
  }

  .tab-card {
    ::v-deep .el-card__body {
      padding: 20px;
    }
  }

  .statistics-cards {
    margin-bottom: 30px;

    .stat-card {
      border-radius: 10px;
      padding: 20px;
      color: white;
      text-align: center;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

      .stat-title {
        font-size: 14px;
        opacity: 0.9;
        margin-bottom: 10px;
      }

      .stat-value {
        font-size: 28px;
        font-weight: bold;
      }
    }

    .revenue-card {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    .order-card {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }

    .review-card {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }

    .rating-card {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    }
  }

  .revenue-chart {
    margin-top: 20px;

    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 16px;
      font-weight: bold;
      padding-bottom: 10px;
      border-bottom: 1px solid #e8e8e8;

      i {
        margin-right: 8px;
        color: #409EFF;
      }
    }
  }

  .review-filter {
    margin-bottom: 20px;
  }

  .user-info {
    display: flex;
    align-items: center;
  }

  .review-content {
    word-break: break-all;
    line-height: 1.5;
  }

  .review-images {
    margin-top: 10px;
    display: flex;
    flex-wrap: wrap;
  }

  .review-time {
    font-size: 12px;
    color: #999;
    margin-top: 8px;
  }

  .reply-content {
    color: #67C23A;
    font-size: 13px;

    .reply-time {
      font-size: 11px;
      color: #999;
      margin-top: 5px;
    }
  }

  .reply-btn {
    text-align: center;
  }

  .schedule-tips {
    margin-bottom: 20px;
  }

  .gray-text {
    color: #ccc;
  }
}
</style>
