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

    <!-- 营收看板 -->
    <el-card class="tab-card">
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
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  getDeptInfo,
  getStatistics,
  getRevenueTrend
} from '@/api/booking/booking'

export default {
  name: "BossRevenue",
  data() {
    return {
      deptInfo: {},
      statistics: {
        totalRevenue: 0,
        totalOrders: 0,
        totalReviews: 0,
        avgRating: 0
      },
      trendDays: 7,
      revenueChart: null
    }
  },
  created() {
    this.loadDeptInfo()
    this.loadStatistics()
    this.loadRevenueTrend()
  },
  methods: {
    loadDeptInfo() {
      getDeptInfo().then(res => {
        this.deptInfo = res.data
      }).catch(() => {
        this.$message.error('获取门店信息失败')
      })
    },
    loadStatistics() {
      getStatistics().then(res => {
        this.statistics = res.data
      }).catch(() => {
        this.$message.error('获取统计数据失败')
      })
    },
    loadRevenueTrend() {
      getRevenueTrend(this.trendDays).then(res => {
        this.$nextTick(() => {
          this.initChart(res.data)
        })
      }).catch(() => {
        this.$message.error('获取营收趋势失败')
      })
    },
    initChart(data) {
      if (this.revenueChart) {
        this.revenueChart.dispose()
      }
      this.revenueChart = echarts.init(this.$refs.revenueChart)
      const option = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { data: ['营收金额', '订单数'], top: 0 },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: data.map(item => item.date) },
        yAxis: [
          { type: 'value', name: '营收(元)', nameTextStyle: { color: '#409EFF' } },
          { type: 'value', name: '订单数', nameTextStyle: { color: '#67C23A' } }
        ],
        series: [
          { name: '营收金额', type: 'line', data: data.map(item => item.revenue), smooth: true, itemStyle: { color: '#409EFF' }, lineStyle: { width: 3 }, areaStyle: { opacity: 0.1 } },
          { name: '订单数', type: 'bar', data: data.map(item => item.orderCount), yAxisIndex: 1, itemStyle: { color: '#67C23A' }, barWidth: '40%' }
        ]
      }
      this.revenueChart.setOption(option)
      window.addEventListener('resize', () => {
        if (this.revenueChart) this.revenueChart.resize()
      })
    },
    formatMoney(value) {
      if (!value) return '0.00'
      return parseFloat(value).toFixed(2)
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
      .stat-title { font-size: 14px; opacity: 0.9; margin-bottom: 10px; }
      .stat-value { font-size: 28px; font-weight: bold; }
    }
    .revenue-card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
    .order-card { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
    .review-card { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
    .rating-card { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
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
      i { margin-right: 8px; color: #409EFF; }
    }
  }
}
</style>

