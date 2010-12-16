import nl.captcha.servlet.CaptchaServletUtil

class SpringController {
    def defaultCaptchaBean
    def chineseCaptchaBean

    def simple = {
        defaultCaptchaBean.build()
        CaptchaServletUtil.writeImage(response, defaultCaptchaBean.image)
    }

    def chinese = {
        chineseCaptchaBean.build()
        CaptchaServletUtil.writeImage(response, chineseCaptchaBean.image)
    }
}
