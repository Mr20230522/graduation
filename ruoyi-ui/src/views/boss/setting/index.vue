<template>
  <div class="setting-management">
    <el-row :gutter="24">
      <!-- 左侧菜单 -->
      <el-col :span="5">
        <div class="menu-wrapper">
          <div class="menu-header">
            <i class="el-icon-setting"></i>
            <span>门店配置</span>
          </div>
          <el-menu
            :default-active="activeMenu"
            class="setting-menu"
            @select="handleMenuSelect"
            background-color="transparent"
            text-color="#5a5e66"
            active-text-color="#409EFF">
            <el-menu-item index="basic">
              <i class="el-icon-edit-outline"></i>
              <span slot="title">基本信息</span>
            </el-menu-item>
            <el-menu-item index="schedule">
              <i class="el-icon-time"></i>
              <span slot="title">营业时间</span>
            </el-menu-item>
            <el-menu-item index="parking">
              <i class="el-icon-place"></i>
              <span slot="title">车位管理</span>
            </el-menu-item>
          </el-menu>
        </div>
      </el-col>

      <!-- 右侧内容 -->
      <el-col :span="19">
        <div class="content-wrapper">
          <!-- 基本信息 -->
          <div v-show="activeMenu === 'basic'" class="setting-panel">
            <div class="panel-header">
              <div class="header-left">
                <i class="el-icon-edit-outline"></i>
                <h3>基本信息</h3>
              </div>
              <p>设置门店的基本信息，这些信息将展示给用户</p>
            </div>

            <!-- 门店图片上传区域 -->
            <div class="image-upload-section">
              <div class="section-title">
                <i class="el-icon-picture"></i>
                <span>门店图片</span>
              </div>
              <div class="image-container">
                <div class="image-preview">
                  <img :src="imagePreviewUrl" :alt="deptForm.deptName" class="shop-image">
                  <div class="image-overlay" v-if="imageUploadLoading">
                    <i class="el-icon-loading"></i>
                    <span>上传中...</span>
                  </div>
                </div>
                <div class="upload-actions">
                  <el-upload
                    class="upload-btn"
                    :show-file-list="false"
                    :before-upload="beforeImageUpload"
                    :http-request="handleImageUpload"
                    accept="image/jpeg,image/png,image/gif">
                    <el-button type="primary" size="small" :loading="imageUploadLoading">
                      <i class="el-icon-upload"></i> 上传图片
                    </el-button>
                  </el-upload>
                  <el-button
                    type="danger"
                    size="small"
                    :disabled="!hasImage"
                    @click="handleImageDelete"
                    :loading="imageDeleteLoading">
                    <i class="el-icon-delete"></i> 删除图片
                  </el-button>
                  <div class="upload-tip">
                    <i class="el-icon-info"></i>
                    <span>支持 jpg、png、gif 格式，建议尺寸 800x600，文件不超过 2MB</span>
                  </div>
                </div>
              </div>
            </div>

            <el-form :model="deptForm" :rules="deptRules" ref="deptForm" label-width="110px" class="setting-form">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="门店名称" prop="deptName">
                    <el-input v-model="deptForm.deptName" placeholder="请输入门店名称"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="负责人" prop="leader">
                    <el-input v-model="deptForm.leader" placeholder="请输入负责人"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="联系电话" prop="phone">
                    <el-input v-model="deptForm.phone" placeholder="请输入联系电话"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="邮箱" prop="email">
                    <el-input v-model="deptForm.email" placeholder="请输入邮箱"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="门店地址" prop="address">
                <el-input type="textarea" v-model="deptForm.address" :rows="3" placeholder="请输入门店地址"></el-input>
              </el-form-item>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="营业执照号" prop="license">
                    <el-input v-model="deptForm.license" placeholder="请输入营业执照号"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="车位数量" prop="carSpaceCount">
                    <el-input-number v-model="deptForm.carSpaceCount" :min="1" :max="50" controls-position="right"></el-input-number>
                    <span class="unit">个</span>
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 支付宝支付配置 -->
              <div class="alipay-divider">
                <el-divider content-position="left">
                  <i class="el-icon-money"></i> 支付宝支付配置
                </el-divider>
                <div class="alipay-tip">配置后用户支付时将使用该门店的支付宝账号</div>
              </div>

              <el-row :gutter="24">
                <el-col :span="24">
                  <el-form-item label="应用ID" prop="alipayAppId">
                    <el-input v-model="deptForm.alipayAppId" placeholder="请输入支付宝应用ID"></el-input>
                    <div class="form-tip">从支付宝开放平台沙箱获取，例如：9021000162652310</div>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="24">
                  <el-form-item label="商户私钥" prop="alipayPrivateKey">
                    <el-input type="textarea" v-model="deptForm.alipayPrivateKey" :rows="4" placeholder="请输入商户私钥（PKCS8格式）"></el-input>
                    <div class="form-tip">使用支付宝密钥生成工具生成的"应用私钥"</div>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="24">
                  <el-form-item label="支付宝公钥" prop="alipayPublicKey">
                    <el-input type="textarea" v-model="deptForm.alipayPublicKey" :rows="4" placeholder="请输入支付宝公钥"></el-input>
                    <div class="form-tip">上传应用公钥后系统生成的"支付宝公钥"</div>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="网关地址" prop="alipayGateway">
                    <el-select v-model="deptForm.alipayGateway" placeholder="请选择环境" style="width: 100%">
                      <el-option label="沙箱环境（测试用）" value="https://openapi.alipaydev.com/gateway.do"></el-option>
                      <el-option label="正式环境（上线用）" value="https://openapi.alipay.com/gateway.do"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <div class="form-actions">
                <el-button type="primary" @click="updateDeptInfo" :loading="saveLoading" size="medium">
                  <i class="el-icon-check"></i> 保存修改
                </el-button>
                <el-button @click="resetForm" size="medium">
                  <i class="el-icon-refresh-left"></i> 重置
                </el-button>
              </div>
            </el-form>
          </div>

          <!-- 营业时间 -->
          <div v-show="activeMenu === 'schedule'" class="setting-panel">
            <div class="panel-header">
              <div class="header-left">
                <i class="el-icon-time"></i>
                <h3>营业时间</h3>
              </div>
              <p>设置未来7天的营业时间，每天可独立设置</p>
            </div>
            <el-alert
              title="休息日将无法预约，请谨慎设置"
              type="info"
              :closable="false"
              show-icon
              class="schedule-alert">
            </el-alert>
            <el-table :data="scheduleList" border class="schedule-table" v-loading="scheduleLoading">
              <el-table-column prop="workDate" label="日期" width="130" align="center">
                <template slot-scope="scope">
                  <div class="date-cell">
                    <div class="weekday">{{ formatWeekday(scope.row.workDate) }}</div>
                    <div class="date-number">{{ formatDate(scope.row.workDate) }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="营业状态" width="110" align="center">
                <template slot-scope="scope">
                  <el-switch
                    v-model="scope.row.status"
                    :active-value="0"
                    :inactive-value="1"
                    active-color="#67C23A"
                    inactive-color="#F56C6C">
                  </el-switch>
                  <span class="status-text" :class="scope.row.status == 0 ? 'open' : 'closed'">
                    {{ scope.row.status == 0 ? '营业' : '休息' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="营业时段" min-width="280">
                <template slot-scope="scope">
                  <div v-if="scope.row.status == 0" class="time-range">
                    <el-time-select
                      v-model="scope.row.openTime"
                      :picker-options="{ start: '00:00', step: '00:30', end: '23:30' }"
                      placeholder="开门时间"
                      size="small"
                      style="width: 115px">
                    </el-time-select>
                    <span class="separator">—</span>
                    <el-time-select
                      v-model="scope.row.closeTime"
                      :picker-options="{ start: '00:00', step: '00:30', end: '23:30', minTime: scope.row.openTime }"
                      placeholder="关门时间"
                      size="small"
                      style="width: 115px">
                    </el-time-select>
                  </div>
                  <span v-else class="rest-day">今日休息</span>
                </template>
              </el-table-column>
              <el-table-column label="时段时长" width="130" align="center">
                <template slot-scope="scope">
                  <span v-if="scope.row.status == 0" class="time-value">{{ scope.row.slotMinutes }}分钟</span>
                  <span v-else class="gray-text">--</span>
                </template>
              </el-table-column>
              <el-table-column label="可用车位" width="130" align="center">
                <template slot-scope="scope">
                  <span v-if="scope.row.status == 0" class="space-value">{{ scope.row.carSpaces }}个</span>
                  <span v-else class="gray-text">--</span>
                </template>
              </el-table-column>
            </el-table>
            <div class="schedule-footer">
              <el-button type="primary" @click="saveSchedule" :loading="scheduleSaving" size="medium">
                <i class="el-icon-check"></i> 保存排班
              </el-button>
            </div>
          </div>

          <!-- 车位管理 -->
          <div v-show="activeMenu === 'parking'" class="setting-panel">
            <div class="panel-header">
              <div class="header-left">
                <i class="el-icon-place"></i>
                <h3>车位管理</h3>
              </div>
              <p>管理门店的车位，禁用的车位将无法被预约</p>
            </div>
            <div class="parking-grid" v-loading="carSpaceLoading">
              <div v-for="item in carSpaceList" :key="item.id" class="parking-card" :class="{ disabled: item.status == 1 }">
                <div class="parking-icon">
                  <i class="el-icon-parking"></i>
                </div>
                <div class="parking-info">
                  <div class="parking-number">{{ item.spaceNo }}</div>
                  <div class="parking-name">{{ item.spaceName }}</div>
                </div>
                <div class="parking-status">
                  <el-switch
                    v-model="item.status"
                    :active-value="0"
                    :inactive-value="1"
                    active-color="#67C23A"
                    inactive-color="#F56C6C"
                    @change="updateCarSpaceStatus(item)">
                  </el-switch>
                  <span class="status-badge" :class="item.status == 0 ? 'enabled' : 'disabled'">
                    {{ item.status == 0 ? '启用' : '禁用' }}
                  </span>
                </div>
              </div>
              <div v-if="carSpaceList.length === 0 && !carSpaceLoading" class="empty-parking">
                <i class="el-icon-parking"></i>
                <p>暂无车位信息</p>
                <span>请先在门店设置中添加车位</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>
<script>
import {
  getDeptInfo,
  updateDept,
  getSchedule,
  updateSchedule,
  getCarSpaces,
  updateCarSpace,
  uploadDeptImage,
  deleteDeptImage
} from '@/api/booking/booking'

export default {
  name: "BossSetting",
  data() {
    return {
      activeMenu: 'basic',
      deptForm: {},
      deptRules: {
        deptName: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
        leader: [{ required: true, message: '请输入负责人', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
      },
      saveLoading: false,
      scheduleList: [],
      scheduleLoading: false,
      scheduleSaving: false,
      carSpaceList: [],
      carSpaceLoading: false,
      // 图片相关
      imageUploadLoading: false,
      imageDeleteLoading: false,
      imageBaseUrl: process.env.VUE_APP_BASE_API + '/profile'
    }
  },
  computed: {
    // 图片预览地址
    imagePreviewUrl() {
      if (this.deptForm.imageUrl) {
        if (this.deptForm.imageUrl.startsWith('http')) {
          return this.deptForm.imageUrl
        }
        return this.imageBaseUrl + this.deptForm.imageUrl
      }
      // 没有图片时显示默认图片
      return this.imageBaseUrl + '/dept/default.jpg'
    },
    // 是否有图片
    hasImage() {
      return this.deptForm.imageUrl && this.deptForm.imageUrl !== ''
    }
  },
  created() {
    this.loadDeptInfo()
    this.loadSchedule()
    this.loadCarSpaces()
  },
  methods: {
    handleMenuSelect(index) {
      this.activeMenu = index
    },
    loadDeptInfo() {
      getDeptInfo().then(res => {
        this.deptForm = { ...res.data }
      }).catch(() => {
        this.$message.error('获取门店信息失败')
      })
    },
    updateDeptInfo() {
      this.$refs.deptForm.validate(valid => {
        if (valid) {
          this.saveLoading = true
          updateDept(this.deptForm).then(() => {
            this.$message.success('保存成功')
            this.loadDeptInfo()
            this.saveLoading = false
          }).catch(() => {
            this.saveLoading = false
            this.$message.error('保存失败')
          })
        }
      })
    },
    resetForm() {
      this.loadDeptInfo()
      this.$message.info('已重置')
    },
    loadSchedule() {
      this.scheduleLoading = true
      getSchedule().then(res => {
        this.scheduleList = res.data || []
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
    // ==================== 图片上传相关方法 ====================
    // 上传前校验
    beforeImageUpload(file) {
      const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$message.error('只能上传 JPG/PNG/GIF 格式的图片!')
        return false
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB!')
        return false
      }
      return true
    },
    // 上传图片
    handleImageUpload(params) {
      this.imageUploadLoading = true
      const file = params.file

      uploadDeptImage(file).then(res => {
        if (res.code === 200) {
          this.$message.success('图片上传成功')
          // 刷新门店信息，获取最新的图片地址
          this.loadDeptInfo()
        } else {
          this.$message.error(res.msg || '上传失败')
        }
        this.imageUploadLoading = false
      }).catch(() => {
        this.$message.error('上传失败，请重试')
        this.imageUploadLoading = false
      })
    },
    // 删除图片
    handleImageDelete() {
      this.$confirm('确定删除门店图片吗？删除后用户将看到默认图片', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.imageDeleteLoading = true
        deleteDeptImage().then(res => {
          if (res.code === 200) {
            this.$message.success('图片删除成功')
            // 刷新门店信息
            this.loadDeptInfo()
          } else {
            this.$message.error(res.msg || '删除失败')
          }
          this.imageDeleteLoading = false
        }).catch(() => {
          this.$message.error('删除失败，请重试')
          this.imageDeleteLoading = false
        })
      }).catch(() => {})
    },
    formatWeekday(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      return weekdays[date.getDay()]
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getMonth() + 1}月${date.getDate()}日`
    }
  }
}
</script>

<style scoped lang="scss">
.alipay-divider {
  margin: 20px 0 10px 0;

  .alipay-tip {
    font-size: 12px;
    color: #909399;
    margin-bottom: 20px;
    padding-left: 10px;
  }
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.4;
}
.setting-management {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;

  // 左侧菜单包装器
  .menu-wrapper {
    background: white;
    border-radius: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    overflow: hidden;

    .menu-header {
      padding: 20px 24px;
      border-bottom: 1px solid #f0f0f0;
      display: flex;
      align-items: center;
      gap: 8px;

      i {
        font-size: 20px;
        color: #409EFF;
      }

      span {
        font-size: 16px;
        font-weight: 600;
        color: #2c3e50;
      }
    }

    .setting-menu {
      border: none;
      padding: 8px 12px;

      .el-menu-item {
        border-radius: 12px;
        margin-bottom: 4px;
        height: 48px;
        line-height: 48px;
        font-size: 14px;

        i {
          margin-right: 12px;
          font-size: 18px;
          color: #8c8f9c;
        }

        &:hover {
          background-color: #f5f7fa;

          i {
            color: #409EFF;
          }
        }

        &.is-active {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

          i {
            color: white;
          }

          span {
            color: white;
          }
        }
      }
    }
  }

  // 右侧内容包装器
  .content-wrapper {
    background: white;
    border-radius: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    overflow: hidden;
  }

  .setting-panel {
    .panel-header {
      padding: 20px 28px;
      border-bottom: 1px solid #f0f0f0;
      background: #fafbfc;

      .header-left {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 8px;

        i {
          font-size: 22px;
          color: #409EFF;
        }

        h3 {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: #2c3e50;
        }
      }

      p {
        margin: 0;
        font-size: 13px;
        color: #8c8f9c;
        padding-left: 32px;
      }
    }

    // 图片上传区域样式（新增）
    .image-upload-section {
      padding: 24px 28px;
      border-bottom: 1px solid #f0f0f0;
      background: #fefef8;

      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 16px;

        i {
          font-size: 18px;
          color: #e6a23c;
        }

        span {
          font-size: 14px;
          font-weight: 500;
          color: #2c3e50;
        }
      }

      .image-container {
        display: flex;
        gap: 24px;
        flex-wrap: wrap;

        .image-preview {
          position: relative;
          width: 200px;
          height: 150px;
          border-radius: 12px;
          overflow: hidden;
          background: #f5f7fa;
          border: 1px solid #e4e7ed;

          .shop-image {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }

          .image-overlay {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.6);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: white;
            gap: 8px;

            i {
              font-size: 28px;
            }

            span {
              font-size: 12px;
            }
          }
        }

        .upload-actions {
          flex: 1;
          display: flex;
          flex-direction: column;
          gap: 12px;

          .upload-btn {
            display: inline-block;
          }

          .upload-tip {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 12px;
            color: #8c8f9c;
            margin-top: 8px;

            i {
              color: #909399;
            }
          }
        }
      }
    }

    .setting-form {
      padding: 28px;

      .unit {
        margin-left: 8px;
        color: #8c8f9c;
      }

      .form-actions {
        margin-top: 32px;
        padding-top: 24px;
        border-top: 1px solid #f0f0f0;

        .el-button {
          min-width: 100px;

          i {
            margin-right: 6px;
          }
        }
      }
    }

    .schedule-alert {
      margin: 20px 28px 0;
      border-radius: 12px;
    }

    .schedule-table {
      margin: 20px 28px;

      ::v-deep .el-table__header th {
        background-color: #fafbfc;
        color: #5a5e66;
        font-weight: 500;
      }

      .date-cell {
        text-align: center;

        .weekday {
          font-size: 14px;
          font-weight: 500;
          color: #2c3e50;
        }

        .date-number {
          font-size: 12px;
          color: #8c8f9c;
          margin-top: 4px;
        }
      }

      .status-text {
        margin-left: 8px;
        font-size: 12px;

        &.open {
          color: #67C23A;
        }

        &.closed {
          color: #F56C6C;
        }
      }

      .time-range {
        display: flex;
        align-items: center;
        gap: 12px;

        .separator {
          color: #8c8f9c;
          font-size: 12px;
        }
      }

      .rest-day {
        color: #F56C6C;
        font-weight: 500;
      }

      .time-value, .space-value {
        font-weight: 500;
        color: #409EFF;
      }

      .gray-text {
        color: #c0c4cc;
      }
    }

    .schedule-footer {
      padding: 0 28px 28px;
      text-align: center;
    }

    .parking-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 16px;
      padding: 28px;

      .parking-card {
        background: #fafbfc;
        border-radius: 16px;
        padding: 20px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        transition: all 0.3s;
        border: 1px solid #f0f0f0;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
          border-color: transparent;
        }

        &.disabled {
          background: #fef6f6;
          border-color: #fee2e2;

          .parking-number {
            color: #F56C6C;
          }
        }

        .parking-icon {
          width: 48px;
          height: 48px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 14px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;

          i {
            font-size: 24px;
            color: white;
          }
        }

        &.disabled .parking-icon {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }

        .parking-info {
          flex: 1;

          .parking-number {
            font-size: 18px;
            font-weight: bold;
            color: #409EFF;
            margin-bottom: 4px;
          }

          .parking-name {
            font-size: 12px;
            color: #8c8f9c;
          }
        }

        .parking-status {
          display: flex;
          align-items: center;
          gap: 12px;

          .status-badge {
            font-size: 12px;
            padding: 4px 8px;
            border-radius: 20px;

            &.enabled {
              background: #e8f8e8;
              color: #67C23A;
            }

            &.disabled {
              background: #fee8e8;
              color: #F56C6C;
            }
          }
        }
      }

      .empty-parking {
        grid-column: 1 / -1;
        text-align: center;
        padding: 80px 20px;

        i {
          font-size: 80px;
          color: #dcdfe6;
        }

        p {
          margin: 16px 0 8px;
          font-size: 16px;
          color: #5a5e66;
        }

        span {
          font-size: 13px;
          color: #8c8f9c;
        }
      }
    }
  }
}
</style>
