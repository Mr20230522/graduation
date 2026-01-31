<template>
  <div class="app-container">
    <!-- 1. 过期日期不展示 -->
    <el-row :gutter="20">
      <!-- 左侧 7 天日期栏（只显示 ≥ 今天） -->
      <el-col :span="4">
        <div class="date-bar">
          <div v-for="d in validDates" :key="d.date"
               :class="{active: selectedDate === d.date}"
               class="date-item" @click="selectDate(d.date)">
            <div class="day">{{ d.day }}</div>
            <div class="date">{{ d.dateStr }}</div>
          </div>
        </div>
      </el-col>

      <!-- 右侧车位-时段矩阵 -->
      <el-col :span="20">
        <div class="matrix-wrapper">
          <div v-for="space in spaces" :key="space" class="space-row">
            <div class="space-title">{{ space }}</div>
            <div class="slot-list">
              <div v-for="(slot, idx) in slots" :key="slot"
                   :class="matrix[space] && matrix[space][idx] ? matrix[space][idx] : 'free'"
                   class="slot-item"
                   @click="matrix[space] && matrix[space][idx] === 'free' && openBook(space, slot)">
                {{ slot }}
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 预约弹窗 -->
    <el-dialog title="确认预约" :visible.sync="bookVisible" width="400px">
      <el-form :model="bookForm" label-width="80px">
        <el-form-item label="日期"><el-input v-model="bookForm.workDate" readonly /></el-form-item>
        <el-form-item label="车位"><el-input v-model="bookForm.spaceNo" readonly /></el-form-item>
        <el-form-item label="时段"><el-input v-model="bookForm.slot" readonly /></el-form-item>
        <el-form-item label="车牌"><el-input v-model="bookForm.carNumber" placeholder="例：京A12345" /></el-form-item>
        <el-form-item label="车型"><el-input v-model="bookForm.carModel" placeholder="例：宝马3系" /></el-form-item>
        <el-form-item label="颜色"><el-input v-model="bookForm.carColor" placeholder="例：白色" /></el-form-item>
        <el-form-item label="套餐">
          <el-select v-model="bookForm.comboMinutes" placeholder="请选择">
            <el-option label="标准洗车 30min" :value="30"/>
            <el-option label="精洗+打蜡 60min" :value="60"/>
            <el-option label="全套护理 90min" :value="90"/>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="bookVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitBook">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 我的预约列表（近期） -->
    <el-card class="box-card" style="margin-top: 20px">
      <div slot="header" class="clearfix">
        <span>我的预约</span>
      </div>
      <el-table :data="myBookList" style="width: 100%" empty-text="暂无预约">
        <el-table-column prop="workDate" label="日期" width="120"/>
        <el-table-column prop="spaceNo" label="车位" width="80"/>
        <el-table-column prop="slot" label="时段" width="100"/>
        <el-table-column prop="carNumber" label="车牌"/>
        <el-table-column prop="code" label="验证码" width="100"/>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.status === 0"
              type="text"
              style="color: #f56c6c"
              @click="cancelBook(scope.row)">取消</el-button>
            <el-tag v-else type="info">{{ statusMap[scope.row.status] }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 历史记录 -->
    <el-card class="box-card" style="margin-top: 20px">
      <div slot="header" class="clearfix">
        <span>历史记录</span>
      </div>
      <el-table :data="myHistoryList" style="width: 100%" empty-text="暂无历史记录">
        <el-table-column prop="workDate" label="日期" width="120"/>
        <el-table-column prop="spaceNo" label="车位" width="80"/>
        <el-table-column prop="slot" label="时段" width="100"/>
        <el-table-column prop="carNumber" label="车牌"/>
        <el-table-column prop="comboName" label="套餐" width="120"/>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag type="info">{{ statusMap[scope.row.status] }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页 -->
      <el-pagination
        v-if="historyTotal > 0"
        :current-page="historyPageNum"
        :page-size="historyPageSize"
        :total="historyTotal"
        layout="total, prev, pager, next"
        @current-change="handleHistoryPageChange"
        style="margin-top: 10px; text-align: right"/>
    </el-card>
  </div>
</template>

<script>
import {
  getCalendar,
  createBooking,
  cancelBooking,
  listMyBooking,
  listMyHistory
} from '@/api/booking/booking'

export default {
  name: 'BookingIndex',
  data() {
    return {
      deptId: 1,
      dateList: [],
      validDates: [],
      slots: [],
      spaces: [],
      matrix: {},
      selectedDate: '',
      bookVisible: false,
      bookForm: {
        workDate: '',
        spaceNo: '',
        slot: '',
        carNumber: '',
        carModel: '',
        carColor: '',
        comboMinutes: 30
      },
      myBookList: [],
      statusMap: { 0: '待到店', 1: '服务中', 2: '已完成', 3: '已取消' },
      myHistoryList: [],
      historyPageNum: 1,
      historyPageSize: 10,
      historyTotal: 0
    }
  },
  created() {
    console.log('@@===== 页面创建，开始加载数据 =====')
    this.loadCalendar()
    this.loadMyBook()
    this.loadMyHistory()
  },
  methods: {
    /** 1. 加载日历（过滤过期） */
    loadCalendar() {
      console.log('@@开始加载日历, deptId:', this.deptId)

      getCalendar(this.deptId).then(res => {
        console.log('@@loadCalendar响应:', res)

        const data = res.data
        console.log('@@dates:', data.dates)
        console.log('@@schedule所有key:', Object.keys(data.schedule))

        const today = new Date().toISOString().slice(0, 10)
        console.log('@@today:', today)

        this.dateList = data.dates.map(d => ({
          date: d,
          dateStr: d.substr(5),
          day: this.getDayDesc(new Date(d))
        }))

        this.validDates = this.dateList.filter(d => d.date >= today)
        console.log('@@validDates:', this.validDates)

        if (this.validDates.length === 0) {
          console.warn('@@没有有效日期')
          return
        }

        this.selectedDate = this.validDates[0].date
        console.log('@@选中日期:', this.selectedDate)

        const dayData = data.schedule[this.selectedDate]
        console.log('@@dayData:', dayData)

        if (!dayData) {
          console.error('@@错误：找不到日期', this.selectedDate, '的数据')
          console.log('@@尝试查找相近的key...')
          const allKeys = Object.keys(data.schedule)
          allKeys.forEach(key => {
            console.log('  key:', key, '匹配:', key === this.selectedDate)
          })
          return
        }

        this.slots = dayData.slots || []
        this.spaces = dayData.spaces || []
        this.matrix = dayData.matrix || {}

        console.log('@@slots:', this.slots)
        console.log('@@spaces:', this.spaces)
        console.log('@@matrix:', this.matrix)
        console.log('@@matrix[A1]:', this.matrix['A1'])
      }).catch(err => {
        console.error('@@loadCalendar失败:', err)
      })
    },

    /** 2. 切换日期 */
    selectDate(date) {
      console.log('@@===== 切换日期:', date, '=====')
      this.selectedDate = date

      getCalendar(this.deptId, date).then(res => {
        console.log('@@日历响应:', res)
        console.log('@@res.data.schedule:', res.data.schedule)
        console.log('@@可用日期keys:', Object.keys(res.data.schedule))
        console.log('@@查找key:', date)

        const data = res.data.schedule[date]
        console.log('@@找到的数据:', data)

        if (!data) {
          console.error('@@错误：找不到日期', date, '的数据')
          this.slots = []
          this.spaces = []
          this.matrix = {}
          return
        }

        this.slots = data.slots || []
        this.spaces = data.spaces || []
        this.matrix = data.matrix || {}

        console.log('@@slots:', this.slots)
        console.log('@@spaces:', this.spaces)
        console.log('@@matrix:', this.matrix)
      }).catch(err => {
        console.error('@@获取日历失败:', err)
      })
    },

    /** 3. 打开预约弹窗 */
    openBook(space, slot) {
      console.log('@@打开预约弹窗, space:', space, 'slot:', slot)
      this.bookForm = {
        workDate: this.selectedDate,
        spaceNo: space,
        slot: slot,
        carNumber: '',
        carModel: '',
        carColor: '',
        comboMinutes: 30
      }
      this.bookVisible = true
    },

    /** 4. 提交预约 */
    submitBook() {
      console.log('@@===== 开始提交预约 =====')
      console.log('@@点击的slot:', this.bookForm.slot)

      // 直接用slot拼接，不要split
      const timeStr = this.bookForm.slot + ':00'
      console.log('@@组合timeStr:', timeStr)

      const startTimeStr = this.bookForm.workDate + ' ' + timeStr
      console.log('@@完整startTimeStr:', startTimeStr)

      const startTime = new Date(startTimeStr)
      console.log('@@Date对象:', startTime)
      console.log('@@toISOString:', startTime.toISOString())
      console.log('@@getHours:', startTime.getHours())
      console.log('@@getMinutes:', startTime.getMinutes())

      // 检查是否是有效日期
      if (isNaN(startTime.getTime())) {
        console.error('@@无效的日期:', startTimeStr)
        this.$message.error('时间格式错误')
        return
      }

      const param = {
        deptId: this.deptId,
        spaceNo: this.bookForm.spaceNo,
        workDate: this.bookForm.workDate,
        startTime: startTime,
        comboMinutes: this.bookForm.comboMinutes,
        carNumber: this.bookForm.carNumber,
        carModel: this.bookForm.carModel,
        carColor: this.bookForm.carColor
      }

      console.log('@@提交参数:', JSON.stringify({
        ...param,
        startTime: startTime.toISOString()
      }, null, 2))

      createBooking(param).then(res => {
        console.log('@@预约成功响应:', res)
        this.$message.success('预约成功，验证码：' + res.data.code)
        this.bookVisible = false
        console.log('@@准备刷新日历...')
        this.selectDate(this.selectedDate)
        this.loadMyBook()
      }).catch(err => {
        console.error('@@预约失败:', err)
        this.$message.error('该时段已被预约')
      })
    },

    /** 5. 取消预约 */
    cancelBook(row) {
      console.log('@@取消预约:', row)
      this.$confirm('确认取消预约？').then(() => {
        cancelBooking(row.id).then(() => {
          console.log('@@取消成功')
          this.$message.success('已取消')
          this.selectDate(this.selectedDate)
          this.loadMyBook()
        }).catch(err => {
          console.error('@@取消失败:', err)
        })
      })
    },

    /** 6. 加载我的预约（近期，主表） */
    loadMyBook() {
      console.log('@@加载我的预约')
      listMyBooking({ status: 0 }).then(res => {
        console.log('@@我的预约响应:', res)
        this.myBookList = res.data.map(b => ({
          ...b,
          slot: b.startTime ? new Date(b.startTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : ''
        }))
        console.log('@@myBookList:', this.myBookList)
      }).catch(err => {
        console.error('@@加载我的预约失败:', err)
      })
    },

    /** 7. 加载历史记录（历史表） */
    loadMyHistory() {
      console.log('@@加载历史记录')
      listMyHistory({
        pageNum: this.historyPageNum,
        pageSize: this.historyPageSize
      }).then(res => {
        console.log('@@历史记录响应:', res)
        this.myHistoryList = res.data.list.map(b => ({
          ...b,
          slot: b.startTime ? new Date(b.startTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : ''
        }))
        this.historyTotal = res.data.total
        console.log('@@myHistoryList:', this.myHistoryList)
      }).catch(err => {
        console.error('@@加载历史记录失败:', err)
      })
    },

    /** 8. 历史记录分页 */
    handleHistoryPageChange(val) {
      console.log('@@切换历史记录页码:', val)
      this.historyPageNum = val
      this.loadMyHistory()
    },

    /** 9. 星期描述 */
    getDayDesc(date) {
      const days = ['日', '一', '二', '三', '四', '五', '六']
      return '周' + days[date.getDay()]
    }
  }
}
</script>

<style scoped>
.date-bar {
  border-right: 1px solid #ebeef5;
  padding-right: 10px;
}
.date-item {
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 4px;
  cursor: pointer;
  text-align: center;
  background: #f5f7fa;
}
.date-item.active {
  background: #409eff;
  color: #fff;
}
.date-item .day {
  font-size: 12px;
}
.date-item .date {
  font-weight: bold;
  margin-top: 2px;
}

.matrix-wrapper {
  padding-left: 10px;
}
.space-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.space-title {
  width: 60px;
  font-weight: bold;
  text-align: center;
  background: #f5f7fa;
  padding: 8px 0;
  margin-right: 10px;
  border-radius: 4px;
}
.slot-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.slot-item {
  width: 70px;
  padding: 6px 0;
  text-align: center;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}
.slot-item.free {
  background: #67c23a;
  color: #fff;
}
.slot-item.busy {
  background: #f56c6c;
  color: #fff;
}
</style>
