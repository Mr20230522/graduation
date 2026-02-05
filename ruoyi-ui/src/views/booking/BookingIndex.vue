<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 左侧 7 天日期栏 -->
      <el-col :span="4">
        <div class="date-bar">
          <div v-for="d in dateList" :key="d.date"
               :class="{active: selectedDate === d.date, expired: d.isExpired}"
               class="date-item" @click="!d.isExpired && selectDate(d.date)">
            <div class="day">{{ d.day }}</div>
            <div class="date">{{ d.dateStr }}</div>
            <div v-if="d.isExpired" class="expired-tag">已过期</div>
          </div>
        </div>
      </el-col>

      <!-- 右侧车位-时段矩阵 -->
      <el-col :span="20">
        <div class="matrix-wrapper">
          <div v-if="spaces.length === 0" class="empty-tip">
            该日期暂无可用车位或排班信息
          </div>
          <div v-for="space in spaces" :key="space" class="space-row">
            <div class="space-title">{{ space }}</div>
            <div class="slot-list">
              <div v-for="(slot, idx) in slots" :key="idx"
                   :class="getSlotClass(space, slot)"
                   class="slot-item"
                   @click="handleSlotClick(space, slot)">
                {{ formatSlotDisplay(slot) }}
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 预约弹窗 -->
    <el-dialog title="确认预约" :visible.sync="bookVisible" width="420px">
      <el-form :model="bookForm" label-width="90px">
        <el-form-item label="预约日期">
          <div class="display-text date-text">{{ formatDisplayDate(bookForm.workDate) }}</div>
        </el-form-item>
        <el-form-item label="车位编号">
          <div class="display-text space-text">{{ bookForm.spaceNo }}</div>
        </el-form-item>
        <el-form-item label="预约时段">
          <div class="display-text time-text">
            {{ bookForm.slot }} - {{ calculateEndTime(bookForm.slot, bookForm.comboMinutes) }}
          </div>
        </el-form-item>
        <el-form-item label="服务套餐">
          <el-select v-model="bookForm.comboMinutes" placeholder="请选择服务套餐" style="width: 100%">
            <el-option label="标准洗车 30分钟" :value="30"/>
            <el-option label="精洗+打蜡 60分钟" :value="60"/>
            <el-option label="全套护理 90分钟" :value="90"/>
          </el-select>
        </el-form-item>
        <el-form-item label="结束时间">
          <div class="display-text end-time-text">{{ calculateEndTime(bookForm.slot, bookForm.comboMinutes) }}</div>
        </el-form-item>
        <el-form-item label="车牌号码">
          <el-input v-model="bookForm.carNumber" placeholder="例：京A12345" maxlength="10"/>
        </el-form-item>
        <el-form-item label="车辆型号">
          <el-input v-model="bookForm.carModel" placeholder="例：宝马3系" maxlength="20"/>
        </el-form-item>
        <el-form-item label="车辆颜色">
          <el-input v-model="bookForm.carColor" placeholder="例：白色" maxlength="10"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="bookVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitBook">确 定 预 约</el-button>
      </div>
    </el-dialog>

    <!-- 我的预约（进行中） -->
    <el-card class="box-card" style="margin-top: 20px">
      <div slot="header" class="clearfix">
        <span>我的预约（进行中）</span>
      </div>
      <el-table :data="myBookList" style="width: 100%" empty-text="暂无进行中的预约">
        <el-table-column prop="workDate" label="日期" width="120"/>
        <el-table-column prop="spaceNo" label="车位" width="80"/>
        <el-table-column label="时段" width="150">
          <template slot-scope="scope">
            {{ scope.row.slot }} - {{ scope.row.endSlot }}
          </template>
        </el-table-column>
        <el-table-column prop="carNumber" label="车牌" width="100"/>
        <el-table-column prop="comboName" label="套餐" width="120"/>
        <el-table-column prop="code" label="验证码" width="90"/>
        <el-table-column label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 0 ? 'warning' : 'success'" size="small">
              {{ statusMap[scope.row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 0" type="text" style="color: #f56c6c" @click="cancelBook(scope.row)">取消</el-button>
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
        <el-table-column label="时段" width="150">
          <template slot-scope="scope">
            {{ scope.row.slot }} - {{ scope.row.endSlot }}
          </template>
        </el-table-column>
        <el-table-column prop="carNumber" label="车牌" width="100"/>
        <el-table-column prop="comboName" label="套餐" width="120"/>
        <el-table-column label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="getHistoryStatusType(scope.row.status)" size="small">
              {{ statusMap[scope.row.status] }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="historyTotal > 0" :current-page="historyPageNum" :page-size="historyPageSize" :total="historyTotal" layout="total, prev, pager, next" @current-change="handleHistoryPageChange" style="margin-top: 10px; text-align: right"/>
    </el-card>
  </div>
</template>

<script>
import { getCalendar, createBooking, cancelBooking, listMyBooking, listMyHistory } from '@/api/booking/booking'
import request from '@/utils/request'

export default {
  name: 'BookingIndex',
  data() {
    return {
      refreshTimer: null,
      lastRefreshDate: '',
      deptId: 1,
      dateList: [],
      slots: [],
      spaces: [],
      bookingList: [],
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
      statusMap: {0: '待到店', 1: '服务中', 2: '已完成', 3: '已取消'},
      myHistoryList: [],
      historyPageNum: 1,
      historyPageSize: 10,
      historyTotal: 0
    }
  },
  created() {
    this.initDeptId()
    this.startDailyRefresh()
  },
  beforeDestroy() {
    this.stopDailyRefresh()
  },
  methods: {
    /** 设置下一个0点的定时刷新 */
    scheduleNextDayRefresh() {
      const now = new Date()
      const tomorrow = new Date(now)
      tomorrow.setDate(tomorrow.getDate() + 1)
      tomorrow.setHours(0, 0, 0, 0)

      const msUntilMidnight = tomorrow - now

      setTimeout(() => {
        console.log('@@凌晨0点，自动刷新日历')
        this.loadCalendar()
        this.scheduleNextDayRefresh()
      }, msUntilMidnight)
    },

    stopDailyRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
      }
    },

    /** 启动每日刷新 */
    startDailyRefresh() {
      this.refreshTimer = setInterval(() => {
        const today = this.getLocalDateStr(new Date())
        if (this.lastRefreshDate && this.lastRefreshDate !== today) {
          console.log('@@日期变了，重新加载日历')
          this.loadCalendar()
        }
      }, 60000)

      this.scheduleNextDayRefresh()
    },

    /** 获取用户所属门店 */
    initDeptId() {
      const userInfo = this.$store.state.user.userInfo || {}
      if (userInfo.deptId && userInfo.deptId !== 0) {
        this.deptId = userInfo.deptId
        this.loadCalendar()
        this.loadMyBook()
        this.loadMyHistory()
      } else {
        this.loadDeptList()
      }
    },

    /** 加载门店列表（普通用户） */
    loadDeptList() {
      request({
        url: '/system/dept/list',
        method: 'get',
        params: { status: 0 }
      }).then(res => {
        if (res.data && res.data.length > 0) {
          this.deptId = res.data[0].deptId
          this.loadCalendar()
          this.loadMyBook()
          this.loadMyHistory()
        }
      }).catch(err => {
        console.error('加载门店列表失败', err)
        this.$message.error('加载门店列表失败')
      })
    },

    /** 加载7天日历 - 修复版：使用本地日期，避免时区问题 */
    loadCalendar() {
      const now = new Date()
      // 使用本地日期格式，避免 toISOString 的 UTC 转换问题
      const todayStr = this.getLocalDateStr(now)
      this.lastRefreshDate = todayStr

      console.log('@@加载日历，今天:', todayStr, 'deptId:', this.deptId)

      getCalendar(this.deptId, todayStr).then(res => {
        const data = res.data

        // 生成7天日期列表（从今天开始的7天）- 使用本地时间计算
        this.dateList = []
        for (let i = 0; i < 7; i++) {
          const d = new Date(now)
          d.setDate(now.getDate() + i)
          // 重置时间为0点，避免时间累积误差
          d.setHours(0, 0, 0, 0)

          const dateStr = this.getLocalDateStr(d)

          // 判断是否是今天/明天
          let dayLabel
          if (i === 0) dayLabel = '今天'
          else if (i === 1) dayLabel = '明天'
          else dayLabel = this.getDayDesc(d)

          this.dateList.push({
            date: dateStr,
            dateStr: dateStr.substr(5), // MM-DD 格式
            day: dayLabel,
            isExpired: false,
            isToday: i === 0
          })
        }

        console.log('@@生成的日期列表:', this.dateList.map(d => d.date))

        // 默认选中今天
        this.selectedDate = this.dateList[0].date

        // 渲染今天的数据
        const todayData = data.schedule[this.selectedDate]
        this.renderDayData(todayData)

        // 加载今天的预约列表
        this.loadDayBookings(this.selectedDate)

      }).catch(err => {
        this.$message.error('加载日历失败')
        console.error('@@加载日历失败:', err)
      })
    },

    /** 切换日期 */
    selectDate(date) {
      this.selectedDate = date
      getCalendar(this.deptId, date).then(res => {
        const dayData = res.data.schedule[date]
        this.renderDayData(dayData)
        this.loadDayBookings(date)
      })
    },

    /** 加载指定日期的所有预约 */
    loadDayBookings(date) {
      request({
        url: '/booking/list',
        method: 'get',
        params: {deptId: this.deptId, workDate: date}
      }).then(res => {
        this.bookingList = res.data || []
      }).catch(() => {
        this.bookingList = []
      })
    },

    /** 渲染单日数据 */
    renderDayData(dayData) {
      if (!dayData) {
        this.slots = []
        this.spaces = []
        return
      }
      this.slots = dayData.slots || []
      this.spaces = dayData.spaces || []
    },

    /** 判断时段状态 */
    getSlotClass(space, slot) {
      const isBooked = this.isSlotBooked(space, slot)
      if (isBooked) return 'busy'

      if (this.isSlotExpired(slot)) return 'expired'

      return 'free'
    },

    /** 检查时段是否已被预约 */
    isSlotBooked(space, slot) {
      if (!this.bookingList || this.bookingList.length === 0) return false

      const slotStart = new Date(this.selectedDate + ' ' + slot + ':00')
      const slotEnd = new Date(slotStart.getTime() + 30 * 60000)

      return this.bookingList.some(booking => {
        if (booking.spaceNo !== space) return false
        if (booking.status !== 0 && booking.status !== 1) return false

        const bookingStart = new Date(booking.startTime)
        const bookingEnd = new Date(booking.endTime)

        return bookingStart < slotEnd && bookingEnd > slotStart
      })
    },

    /** 检查时段是否已过期 */
    isSlotExpired(slot) {
      const now = new Date()
      const slotTime = new Date(this.selectedDate + ' ' + slot + ':00')
      return slotTime < now
    },

    /** 点击时段 */
    handleSlotClick(space, slot) {
      const slotClass = this.getSlotClass(space, slot)
      if (slotClass === 'free') {
        this.openBook(space, this.formatSlotDisplay(slot))
      } else if (slotClass === 'busy') {
        this.$message.info('该时段已被预约')
      } else if (slotClass === 'expired') {
        this.$message.warning('该时段已过期')
      }
    },

    /** 打开预约弹窗 */
    openBook(space, slot) {
      if (!this.selectedDate) {
        this.$message.error('请先选择日期')
        return
      }
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

    /** 提交预约 */
    submitBook() {
      if (!this.bookForm.carNumber.trim()) {
        this.$message.error('请输入车牌号码')
        return
      }

      const timeStr = this.bookForm.slot + ':00'
      const startTimeStr = this.bookForm.workDate + ' ' + timeStr
      const startTime = new Date(startTimeStr)

      if (isNaN(startTime.getTime())) {
        this.$message.error('时间格式错误')
        return
      }

      const param = {
        deptId: this.deptId,
        spaceNo: this.bookForm.spaceNo,
        workDate: this.bookForm.workDate,
        startTime: startTime,
        comboMinutes: this.bookForm.comboMinutes,
        carNumber: this.bookForm.carNumber.trim(),
        carModel: this.bookForm.carModel.trim(),
        carColor: this.bookForm.carColor.trim()
      }

      createBooking(param).then(res => {
        this.$message.success('预约成功，验证码：' + res.data.code)
        this.bookVisible = false
        this.loadDayBookings(this.selectedDate)
        this.loadMyBook()
      }).catch(err => {
        this.$message.error(err.msg || '该时段已被预约')
      })
    },

    /** 取消预约 */
    cancelBook(row) {
      this.$confirm('确认取消该预约？', '提示', {type: 'warning'}).then(() => {
        cancelBooking(row.id).then(() => {
          this.$message.success('预约已取消')
          this.loadDayBookings(this.selectedDate)
          this.loadMyBook()
          this.loadMyHistory()
        })
      })
    },

    /** 加载我的预约 */
    loadMyBook() {
      listMyBooking({statusList: [0, 1], onlyNotExpired: true}).then(res => {
        this.myBookList = res.data.map(b => ({
          ...b,
          slot: b.startTime ? this.formatTime(b.startTime) : '',
          endSlot: b.endTime ? this.formatTime(b.endTime) : '',
          comboName: this.getComboName(b.comboMinutes)
        }))
      })
    },

    /** 加载历史记录 */
    loadMyHistory() {
      listMyHistory({pageNum: this.historyPageNum, pageSize: this.historyPageSize}).then(res => {
        this.myHistoryList = res.data.list.map(b => ({
          ...b,
          slot: b.startTime ? this.formatTime(b.startTime) : '',
          endSlot: b.endTime ? this.formatTime(b.endTime) : '',
          comboName: this.getComboName(b.comboMinutes)
        }))
        this.historyTotal = res.data.total
      })
    },

    handleHistoryPageChange(val) {
      this.historyPageNum = val
      this.loadMyHistory()
    },

    getHistoryStatusType(status) {
      const map = {0: 'info', 1: 'success', 2: 'success', 3: 'danger'}
      return map[status] || 'info'
    },

    formatTime(dateStr) {
      const date = new Date(dateStr)
      return date.toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit', hour12: false})
    },

    formatDisplayDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const weekDays = ['日', '一', '二', '三', '四', '五', '六']
      return `${dateStr}（星期${weekDays[date.getDay()]}）`
    },

    calculateEndTime(startSlot, minutes) {
      if (!startSlot || !minutes) return '--:--'
      const [hours, mins] = startSlot.split(':').map(Number)
      const startDate = new Date()
      startDate.setHours(hours, mins, 0, 0)
      const endDate = new Date(startDate.getTime() + minutes * 60000)
      return endDate.toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit', hour12: false})
    },

    getComboName(minutes) {
      const map = {30: '标准洗车', 60: '精洗+打蜡', 90: '全套护理'}
      return map[minutes] || '未知套餐'
    },

    getDayDesc(date) {
      const days = ['日', '一', '二', '三', '四', '五', '六']
      return '周' + days[date.getDay()]
    },

    formatSlotDisplay(slot) {
      return typeof slot === 'string' ? slot : slot.time || slot.label
    },

    /** 获取本地日期字符串（修复时区问题） */
    getLocalDateStr(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.date-bar {
  border-right: 1px solid #ebeef5;
  padding-right: 10px;
  max-height: 600px;
  overflow-y: auto;
}

.date-item {
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 4px;
  cursor: pointer;
  text-align: center;
  background: #f5f7fa;
  transition: all 0.3s;
  position: relative;
}

.date-item:hover {
  background: #e4e7ed;
}

.date-item.active {
  background: #409eff;
  color: #fff;
}

.date-item.expired {
  opacity: 0.6;
  cursor: not-allowed;
  background: #dcdcdc;
}

.date-item .day {
  font-size: 12px;
  margin-bottom: 4px;
}

.date-item .date {
  font-weight: bold;
  font-size: 14px;
}

.expired-tag {
  font-size: 10px;
  color: #f56c6c;
  margin-top: 2px;
}

.matrix-wrapper {
  padding-left: 10px;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 40px;
}

.space-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 5px 0;
  border-bottom: 1px solid #f0f0f0;
}

.space-title {
  width: 60px;
  font-weight: bold;
  text-align: center;
  background: #409eff;
  color: #fff;
  padding: 10px 0;
  margin-right: 15px;
  border-radius: 4px;
  font-size: 14px;
}

.slot-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.slot-item {
  min-width: 70px;
  padding: 8px 12px;
  text-align: center;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
  font-weight: 500;
}

.slot-item:hover {
  transform: scale(1.05);
}

.slot-item.free {
  background: #67c23a;
  color: #fff;
}

.slot-item.busy {
  background: #f56c6c;
  color: #fff;
  cursor: not-allowed;
  opacity: 0.8;
}

.slot-item.expired {
  background: #909399;
  color: #fff;
  cursor: not-allowed;
  opacity: 0.6;
}

.display-text {
  padding: 0 15px;
  height: 40px;
  line-height: 40px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
  border: 1px solid #dcdfe6;
  background: #f5f7fa;
}

.date-text {
  color: #409eff;
}

.space-text {
  color: #67c23a;
}

.time-text {
  color: #e6a23c;
}

.end-time-text {
  color: #909399;
}

.box-card {
  margin-top: 20px;
}

.clearfix {
  font-weight: bold;
}

.dialog-footer {
  text-align: right;
}
</style>
