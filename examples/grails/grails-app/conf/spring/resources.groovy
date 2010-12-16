import nl.captcha.CaptchaBean

beans = {
    println "in resources.groovy..."

    defaultText(nl.captcha.text.producer.DefaultTextProducer) { bean ->
        bean.singleton = false
    }

    chineseText(nl.captcha.text.producer.ChineseTextProducer, 5) { bean ->
        bean.singleton = false
    }

    defaultCaptchaBean(CaptchaBean, 200, 50) { bean ->
        txtProd = defaultText
        bean.singleton = false
    }

    chineseCaptchaBean(CaptchaBean, 230, 50) { bean ->
        txtProd = chineseText
        addBorder = true
    }
}
