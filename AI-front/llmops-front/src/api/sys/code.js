import httpRequest from "@/utils/httpRequest";

const url = "/system/sys/security/code";
// 系统安全-验证码管理
export default {
  // 发送邮件验证码
  sendEmailCode(email) {
    return httpRequest({
      url: `${url}/mail`,
      method: "POST",
      params: { email }
    });
  },
  // 发送短信验证码
  sendSmsCode(phone) {
    return httpRequest({
      url: `${url}/sms`,
      method: "POST",
      params: { phone }
    });
  }
};
