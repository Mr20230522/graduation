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
    params: orderNo ? { orderNo } : {},  // 如果有订单号就传
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
    30: 30,   // 标准洗车
    60: 80,   // 精洗+打蜡
    90: 120   // 全套护理
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
