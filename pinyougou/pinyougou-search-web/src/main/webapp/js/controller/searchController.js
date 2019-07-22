var app = new Vue({
    el:"#app",
    data:{
        //查询条件对象
        searchMap:{"keywords":"","category":"","brand":"","spec":{}, "price":"","pageNo":1,"pageSize":20,"sortField":"","sort":""},//sortField代表排序结果，sort代表排序类型
        //返回结果对象
        resultMap:{"itemList":[]},
        //分页导航条中的页号数组
        pageNoList:[],
        //前面3个点
        frontDot: false,
        //后面3个点
        backDot: false
    },
    methods: {
        //查询
        search:function () {
            axios.post("itemSearch/search.do", this.searchMap).then(function (response) {
                app.resultMap = response.data;

                //构建分页导航条
                app.buildPagination();
            });

        },

        //根据页号查询
        queryByPageNo: function(pageNo){
            if(1<= pageNo && pageNo<=this.resultMap.totalPages){
                this.searchMap.pageNo = pageNo;
                this.search();
            }
        },

        //排序查询
        sortSearch:function(sortField,sort){
            this.searchMap.sortField = sortField;
            this.searchMap.sort =sort;

            //改变了条件之后应该重置页号为 1
            this.searchMap.pageNo = 1;
            //改变了过滤条件后需要重新查询
            this.search();
        },

        //构建分页导航条
        buildPagination: function () {
            this.pageNoList = [];
            //起始页号
            var startPageNo = 1;
            //结束页号
            var endPageNo = this.resultMap.totalPages;
            //要显示的总页号数
            var showPageNoTotal = 5;

            //总页数大于 要显示的总页号数
            if (this.resultMap.totalPages > showPageNoTotal) {
                //当前页左右间隔
                var interval = Math.floor(showPageNoTotal/2);
                startPageNo = this.searchMap.pageNo - interval;
                endPageNo = this.searchMap.pageNo + interval;

                if(startPageNo > 0){
                    if (endPageNo > this.resultMap.totalPages) {
                        endPageNo = this.resultMap.totalPages;
                        startPageNo = endPageNo - showPageNoTotal + 1;
                    }
                } else {
                    //起始页必须要从1开始
                    startPageNo = 1;
                    endPageNo = showPageNoTotal;
                }
            }

            this.frontDot = false;
            if (startPageNo > 1) {
                this.frontDot = true;
            }
            this.backDot = false;
            if (endPageNo < this.resultMap.totalPages) {
                this.backDot = true;
            }

            for (var i = startPageNo; i <= endPageNo; i++) {
                this.pageNoList.push(i);
            }

        },

        //添加过滤条件
        addSearchItem:function (key,value) {
            if("brand"== key || "category" == key || "price" == key){
                this.searchMap[key] = value;
            }else {
                //规格
                //this.searchMap.spec[key] = value;
                //设置对象的属性值；参数1：对象，参数2：属性名，参数3：属性值
                this.$set(this.searchMap.spec,key,value);
            }
            //修改了查询、过滤条件需要将页号重置为1
            this.searchMap.pageNo = 1;

            this.search();
        },
        //移除过滤条件
        removeSearchItem:function (key) {
            if("brand"==key || "category" ==key || "price" == key){
                this.searchMap[key] = "";
            }else{
                //规格
                //this.searchMap.spec[key] = value;
                //更新视图数据;设置对象的属性值；参数1：对象，参数2：属性名，参数3：属性值
                this.$set(this.searchMap.spec,key,null);
                //并且删除属性
                delete this.searchMap.spec[key];
            }
            //修改了查询、过滤条件需要将页号重置为1
            this.searchMap.pageNo = 1;

            this.search();
        },
        //根据参数名字获取参数
        getParameterByName: function (name) {
            return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ""])[1].replace(/\+/g, '%20')) || null
        }

    },
    created(){
        var keywords = this.getParameterByName("keywords");
        if(keywords !=null && keywords !=""){
            this.searchMap.keywords = keywords;
        }
        //查询
        this.search();
    }
});