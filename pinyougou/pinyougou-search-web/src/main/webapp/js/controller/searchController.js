var app = new Vue({
    el:"#app",
    data:{
        //查询条件对象
        searchMap:{"keyword":""},
        //返回结果对象
        resultMap:{"itemList":[]}
    },
    methods: {
        //查询
        search:function () {
            axios.post("itemSearch/search.do", this.searchMap).then(function (response) {
                app.resultMap = response.data;
            });

        }
    },
    created(){
        //查询
        this.search();
    }
});