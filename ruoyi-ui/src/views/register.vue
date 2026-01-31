<template>
  <div class="register">
    <el-form ref="registerForm" :model="registerForm" :rules="registerRules" class="register-form">
      <h3 class="title">{{ title }}</h3>

      <!-- 身份选择 -->
      <el-form-item prop="userType">
        <el-radio-group v-model="registerForm.userType" style="width:100%;text-align:center">
          <el-radio label="3">普通用户</el-radio>
          <el-radio label="2">员工</el-radio>
          <el-radio label="1">老板</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 通用字段 -->
      <el-form-item prop="username">
        <el-input v-model="registerForm.username" placeholder="账号"/>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="registerForm.password" type="password" placeholder="密码"/>
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码"/>
      </el-form-item>
      <el-form-item prop="phonenumber">
        <el-input v-model="registerForm.phonenumber" placeholder="手机号码" maxlength="11"/>
      </el-form-item>


      <!-- 员工：输入公司名称 -->
      <template v-if="registerForm.userType==='2'">
        <el-form-item prop="companyName">
          <el-input v-model="registerForm.companyName" placeholder="请输入所属公司名称"/>
        </el-form-item>
      </template>

      <!-- 老板：填公司资料 -->
      <template v-if="registerForm.userType==='1'">
        <el-form-item prop="companyName">
          <el-input v-model="registerForm.companyName" placeholder="公司名称（审核后生效）"/>
        </el-form-item>
        <el-form-item prop="legalPerson">
          <el-input v-model="registerForm.legalPerson" placeholder="法人姓名"/>
        </el-form-item>
        <!-- 放在手机号码下方即可 -->
        <el-form-item prop="email">
          <el-input v-model="registerForm.email" placeholder="邮箱"/>
        </el-form-item>
        <el-form-item prop="license">
          <el-input v-model="registerForm.license" placeholder="营业执照编号"/>
        </el-form-item>
        <el-form-item prop="address">
          <el-input v-model="registerForm.address" placeholder="详细地址"/>
        </el-form-item>
        <el-form-item prop="longitude">
          <el-input v-model.number="registerForm.longitude" placeholder="经度"/>
        </el-form-item>
        <el-form-item prop="latitude">
          <el-input v-model.number="registerForm.latitude" placeholder="纬度"/>
        </el-form-item>
      </template>

      <!-- 验证码 -->
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input v-model="registerForm.code" placeholder="验证码" style="width:63%"/>
        <div class="register-code">
          <img :src="codeUrl" @click="getCode" class="register-code-img"/>
        </div>
      </el-form-item>

      <!-- 提交 -->
      <el-form-item>
        <el-button :loading="loading" type="primary" style="width:100%" @click="handleRegister">
          <span v-if="!loading">注 册</span>
          <span v-else>注 册 中...</span>
        </el-button>
        <div style="float:right;">
          <router-link class="link-type" to="/login">使用已有账户登录</router-link>
        </div>
      </el-form-item>
    </el-form>

    <div class="el-register-footer"><span>{{ footerContent }}</span></div>
  </div>
</template>

<script>
import { getCodeImg, register } from '@/api/login'

export default {
  name: 'Register',
  data() {
    const equalToPassword = (rule, value, callback) => {
      if (this.registerForm.password !== value) callback(new Error('两次密码不一致'))
      else callback()
    }
    return {
      title: process.env.VUE_APP_TITLE,
      footerContent: '版权所有',
      codeUrl: '',
      captchaEnabled: true,
      loading: false,
      registerForm: {
        username: '',
        password: '',
        confirmPassword: '',
        phonenumber: '',
        userType: '3',
        code: '',
        uuid: '',
        companyName: '',
        legalPerson: '',
        license: '',
        address: '',
        longitude: null,
        latitude: null,
        email:'',
        status: '0'
      },
      registerRules: {
        username: [{ required: true, trigger: 'blur', message: '请输入账号' }, { min: 2, max: 20, message: '长度 2-20', trigger: 'blur' }],
        password: [{ required: true, trigger: 'blur', message: '请输入密码' }, { min: 5, max: 20, message: '长度 5-20', trigger: 'blur' }],
        confirmPassword: [{ required: true, validator: equalToPassword, trigger: 'blur' }],
        phonenumber: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }],
        userType: [{ required: true, message: '请选择身份', trigger: 'change' }],
        companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
        legalPerson: [{ required: true, message: '请输入法人姓名', trigger: 'blur' }],
        license: [{ required: true, message: '请输入营业执照编号', trigger: 'blur' }],
        address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
        longitude: [{ required: true, message: '请输入经度', trigger: 'blur' }],
        latitude: [{ required: true, message: '请输入纬度', trigger: 'blur' }],
        code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getCode()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled !== false
        if (this.captchaEnabled) {
          this.codeUrl = 'data:image/gif;base64,' + res.img
          this.registerForm.uuid = res.uuid
        }
      })
    },
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (!valid) return
        this.loading = true
        const api = register
        api(this.registerForm).then(() => {
          this.$alert('<font color=red>提交成功！</font>', '提示', { dangerouslyUseHTMLString: true, type: 'success' })
            .then(() => this.$router.push('/login'))
        }).catch(res => {
          this.$message.error(res.msg || '注册失败')
          this.loading = false
          if (this.captchaEnabled) this.getCode()
        })
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.register {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/login-background.jpg");
  background-size: cover;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}

.register-form {
  border-radius: 6px;
  background: #ffffff;
  width: 420px;
  padding: 25px 25px 5px 25px;
  .el-input {
    height: 38px;
    input {
      height: 38px;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 2px;
  }
}
.register-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.register-code {
  width: 33%;
  height: 38px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-register-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.register-code-img {
  height: 38px;
}
</style>
