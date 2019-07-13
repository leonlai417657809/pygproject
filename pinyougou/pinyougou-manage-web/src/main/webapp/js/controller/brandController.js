var app = new Vue({
    el:"#app",
    data:{
        //列表数据
        entityList:[],
        //总记录数
        total:0,
        //页号
        pageNum:1,
        //页大小
        pageSize:10,
        //实体
        entity:{},
        //选择的品牌id
        ids:[],
        //搜索条件对象
        searchEntity:{},
        //被选中对象
        checked:false
    },
    methods:{
        searchList:function (pageNum) {
            this.pageNum = pageNum;

            /*axios.get("../brand/findPage.do?pageNum=" + this.pageNum + "&pageSize="+this.pageSize).then(function (response) {
                //总记录数
                app.total = response.data.total;
                app.entityList = response.data.list;
            });*/

            axios.post("../brand/search.do?pageNum="+this.pageNum+"&pageSize="+this.pageSize,this.searchEntity).then(function (response) {
                //总记录数
                app.total = response.data.total;
                app.entityList = response.data.list;
            });
        },
        //根据id查询
        //请求地址：../brand/findOne/123.do
        findOne:function(id){
            axios.get("../brand/findOne/"+id+".do").then(function (response){
                app.entity = response.data;
            });
        },
        //保存数据
        save:function(){
            //新增
            var method = "add";
            //如果 id 存在则说明是 修改
            if(this.entity.id !=null){
                //修改
                method = "update"
            }

            axios.post("../brand/"+method+".do",this.entity).then(function (response){
                if(response.data.success){
                    //刷新列表
                    app.searchList(app.pageNum)
                }else{
                    alert(response.data.message);
                }
            });
        },

        //删除数据；方法名不能直接使用 vue 关键字 delete
        deleteList:function(){
            if(this.ids.length ==0){
                alert("请先选择要删除的记录");
                return;
            }
            //确认是否要删除；confirm 当点击弹出来的框中的确定时返回true否则false
            if(confirm("您确认要删除选择了的记录吗？")){
                //删除
                axios.get("../brand/delete.do?ids="+this.ids).then(function (response){
                    if(response.data.success){
                        //删除完毕后，删除ids，定义为空数组
                        app.ids = [];
                        app.searchList(1);
                    }else{
                        alert(response.data.message);
                    }
                });
            }
        },

        //全选功能
        checkedAll:function () {
            //实现反选
            if(this.checked){
                this.ids = [];
                //实现反选
            } else{
                this.ids = [];
                this.entityList.forEach((item)=>{
                    this.ids.push(item.id);
                });
            }
        }

    },

    watch:{
        ids:{
            handler:function (val,oldVal) {
                if(this.ids.length === this.entityList.length){
                    this.checked = true;
                }else{
                    this.checked = false;
                }
            },
            deep:true
        }
    },

    created(){
        /*axios.get("../brand/findAll.do").then(function (response) {
            //response里面的属性有：data,status,statusText,headers,config
            console.log(response);
            //this表示窗口对象，期望的是vue实例,app就是vue实例变量
            app.entityList = response.data;
        }).catch(function () {
            //如果网络中断，服务器宕机
            alert("获取数据失败!");
        });*/
        this.searchList(this.pageNum);
    }
});