import nl.captcha.servlet.CaptchaServletUtil

class SpringController {
    static navigation = true

    def defaultCaptchaBean
    def chineseCaptchaBean

    def index = {
        render(view:"index")
    }

    def simple = {
        defaultCaptchaBean.build()
        CaptchaServletUtil.writeImage(response, defaultCaptchaBean.image)
    }

    def chinese = {
        chineseCaptchaBean.build()
        CaptchaServletUtil.writeImage(response, chineseCaptchaBean.image)
    }
}
