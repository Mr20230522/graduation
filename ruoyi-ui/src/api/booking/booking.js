import request from '@/utils/request'

// ==================== 原有预约相关接口 ====================

/** 获取日历数据（7天排班 + 车位占用矩阵） */
export function getCalendar(deptId, startDate) {
  return request({
    url: '/booking/calendar',
    method: 'get',
    params: {
      deptId,
      startDate: startDate || new Date()
    }
  })
}

/** 创建预约（支付成功后调用，兼容旧版） */
export function createBooking(data, orderNo) {
  return request({
    url: '/booking/create',
    method: 'post',
    params: orderNo ? { orderNo } : {},
    data: data
  })
}

/** 核销预约 */
export function verifyBooking(code) {
  return request({
    url: '/booking/verify',
    method: 'post',
    params: { code }
  })
}

/** 取消预约 */
export function cancelBooking(bookingId) {
  return request({
    url: '/booking/cancel/' + bookingId,
    method: 'post'
  })
}

/** 查询我的预约（进行中） */
export function listMyBooking(params) {
  return request({
    url: '/booking/my/list',
    method: 'get',
    params: params
  })
}

/** 查询历史记录 */
export function listMyHistory(params) {
  return request({
    url: '/booking/my/history',
    method: 'get',
    params: params
  })
}

// ==================== 新增支付相关接口 ====================

/** 创建订单（预约前，返回订单号） */
export function createOrder(data) {
  return request({
    url: '/booking/order/create',
    method: 'post',
    data: data
  })
}

/** 发起支付 */
export function payOrder(data) {
  return request({
    url: '/booking/order/pay',
    method: 'post',
    data: data
  })
}

/** 查询支付状态 */
export function getPayStatus(orderNo) {
  return request({
    url: '/booking/order/status/' + orderNo,
    method: 'get'
  })
}

/** 取消订单 */
export function cancelOrder(orderNo) {
  return request({
    url: '/booking/order/cancel/' + orderNo,
    method: 'post'
  })
}

// ==================== 套餐价格工具函数（可选）====================

/** 根据套餐分钟数获取价格 */
export function getComboPrice(minutes) {
  const priceMap = {
    30: 30,
    60: 80,
    90: 120
  }
  return priceMap[minutes] || 30
}

/** 根据套餐分钟数获取名称 */
export function getComboName(minutes) {
  const nameMap = {
    30: '标准洗车',
    60: '精洗+打蜡',
    90: '全套护理'
  }
  return nameMap[minutes] || '未知套餐'
}

/** 获取门店列表 */
export function getDeptList(params) {
  return request({
    url: '/system/dept/list',
    method: 'get',
    params: params
  })
}

/** 获取门店详情 */
export function getDeptDetail(deptId) {
  return request({
    url: '/system/dept/' + deptId,
    method: 'get'
  })
}

// ==================== 评价相关接口 ====================

/** 创建评价 */
export function createReview(data) {
  return request({
    url: '/booking/review/create',
    method: 'post',
    data: data
  })
}

/** 获取门店评价列表 */
export function getDeptReviews(deptId, pageNum, pageSize) {
  return request({
    url: '/booking/review/list',
    method: 'get',
    params: {
      deptId,
      pageNum,
      pageSize
    }
  })
}

/** 获取门店评分统计 */
export function getDeptRatingStats(deptId) {
  return request({
    url: '/booking/review/stats/' + deptId,
    method: 'get'
  })
}

/** 商家回复评价 */
export function replyReview(reviewId, replyContent) {
  return request({
    url: '/booking/review/reply/' + reviewId,
    method: 'post',
    data: replyContent
  })
}

/** 删除评价 */
export function deleteReview(reviewId) {
  return request({
    url: '/booking/review/delete/' + reviewId,
    method: 'delete'
  })
}

/** 获取我的评价列表 */
export function getMyReviews(pageNum, pageSize) {
  return request({
    url: '/booking/review/my',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

// ==================== 老板管理接口 ====================

/** 获取当前老板的门店信息 */
export function getDeptInfo() {
  return request({
    url: '/boss/deptInfo',
    method: 'get'
  })
}

/** 获取门店统计数据（总营收、总订单数、总评价数、平均评分） */
export function getStatistics() {
  return request({
    url: '/boss/statistics',
    method: 'get'
  })
}

/** 获取营收趋势（近7天/30天） */
export function getRevenueTrend(days) {
  return request({
    url: '/boss/revenueTrend',
    method: 'get',
    params: { days }
  })
}

/** 获取评价列表（老板视角） */
export function getReviewList(params) {
  return request({
    url: '/boss/reviewList',
    method: 'get',
    params
  })
}

/** 回复评价（老板视角） */
export function bossReplyReview(data) {
  return request({
    url: '/boss/replyReview',
    method: 'post',
    data
  })
}

/** 更新门店信息（老板视角） */
export function updateDept(data) {
  return request({
    url: '/boss/updateDept',
    method: 'put',
    data
  })
}

/** 获取门店排班信息（老板视角） */
export function getSchedule() {
  return request({
    url: '/boss/schedule',
    method: 'get'
  })
}

/** 更新门店排班（老板视角） */
export function updateSchedule(data) {
  return request({
    url: '/boss/updateSchedule',
    method: 'post',
    data
  })
}

/** 获取车位列表（老板视角） */
export function getCarSpaces() {
  return request({
    url: '/boss/carSpaces',
    method: 'get'
  })
}

/** 更新车位状态（老板视角） */
export function updateCarSpace(data) {
  return request({
    url: '/boss/updateCarSpace',
    method: 'put',
    data
  })
}


// 上传门店图片（不需要传deptId，后端自动识别当前登录老板的门店）
export function uploadDeptImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/system/dept/uploadImage',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
