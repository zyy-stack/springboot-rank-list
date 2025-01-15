## 基于Redis实现用户活跃度日、月排行榜

功能：

1.  访问接口，添加用户活跃度
2.  活跃度日、月排行榜
3.  通过AOP+Redis实现防刷
4.  通过分布式锁、lua脚本解决并发问题，以及非原子的redis操作

参考：<br>
https://paicoding.com/column/6/9 <br>
https://blog.csdn.net/qq_48721706/article/details/130312794
