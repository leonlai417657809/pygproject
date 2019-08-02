var app = new Vue({
    el: "#app",
    data: {
        //用户名
        username: "",
        //交易订单号
        outTradeNo: "",
        //应付金额
        totalFee: 0
    },
    methods: {
        //查询支付状态
        queryPayStatus: function (outTradeNo) {
            axios.get("pay/queryPayStatus.do?outTradeNo=" + this.outTradeNo+ "&r=" + Math.random()).then(function (response) {

                if(response.data.success){
                    location.href = "paysuccess.html?totalFee=" + app.totalFee;
                } else {
                    if (response.data.message == "支付超时") {
                        //支付超时；重新生成二维码
                        app.createNative();
                    } else {
                        location.href = "payfail.html";
                    }
                }
            });
        },
        //生成二维码
        createNative: function () {
            //接收浏览器地址栏中的交易订单号
            this.outTradeNo = this.getParameterByName("outTradeNo");
            axios.get("pay/createNative.do?outTradeNo=" + this.outTradeNo).then(function (response) {
                if ("SUCCESS" == response.data.result_code) {
                    //设置总金额
                    app.totalFee = (response.data.totalFee / 100).toFixed(2);
                    //生成二维码
                    var qr = new QRious({
                        element: document.getElementById("qrious"),
                        size: 250,
                        level: "M",
                        value: response.data.code_url
                    });

                    //查询支付状态
                    app.queryPayStatus(app.outTradeNo);
                } else {
                    alert(response.data.message);
                }
            });

        },
        //查询用户名的方法
        getUsername: function () {
            axios.get("cart/getUsername.do").then(function (response) {
                app.username = response.data.username;
            });

        },
        //根据参数名字获取参数
        getParameterByName: function (name) {
            return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ""])[1].replace(/\+/g, '%20')) || null
        }
    },
    created() {
        //查询用户名
        this.getUsername();
        //生成二维码
        this.createNative();
    }
});