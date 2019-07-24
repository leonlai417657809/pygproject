<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Freemaker Test</title>
</head>
<body>
<h2>${name}---${message}</h2>
<br><hr><br>
<#-- 注释；在freemarker中注释的内容是不会出现在输出的文件中的 -->
assign定义变量<br>
<#assign contracts="Leon"/>
${contracts}<br>
<#assign info={"mobile":"132554686","address":"Birmingham"}/>
${info.mobile}---${info.address}
<br><hr><br>
include引入命令<br>
<#include "header.ftl"/>

<br><hr><br>
条件控制语句if<br>
<#assign  bool=true/>
<#if bool>
    bool is true
    <#else>
    bool is false
</#if>

<br><hr><br>
list循环控制语句<br>
<#list goodsList as goods>
    ${goods_index} -- ${goods.name} -- ${goods.price}<br>
</#list>
<br><hr><br>

?size可以获取集合的大小<br>
goodsList的集合大小为：${goodsList?size}<br>
<hr>
?eval将json字符串转换为对象<br>
<#assign objStr='{"name":"Leon","age":16}'/>
<#assign obj=objStr?eval/>

${obj.name} --- ${obj.age}<br>

<hr>
.now表示当前日期:${.now}<br>
当前的日期为：${today?date}<br>
当前的时间为：${today?time}<br>
当前的日期时间为：${today?datetime}<br>
格式化显示日期：${today?string("yyyy年MM月dd日 HH:mm:ss SSSS")}

<hr>
如果是长整型的数据直接显示：${number}；可以使用?c将数值转换为字符串： ${number?c}

<hr>
在freemarker中可以使用！处理空值：<br>
如果为空则什么都不显示：${emp!}；如果值为空或者不存在的时候需要显示特别的内容可以使用!"要显示的内容" ：${emp!"emp's value is empty"}

<hr>
??? 前面两个??表示变量是否存在；如果存在则返回true，否则返回false：第3个?表示函数的调用<br>



<#assign bool5=false/>
${bool5???string}

<br>
<#if str10??>
    str10存在
    <#else >
    str10不存在
</#if>

</body>
</html>