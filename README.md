#Simple Netty Source

本项目是netty.4.1.16.Final源码的精简版本，删除掉netty中不必要的功能，只保留了核心模块，方便进行源码学习。

##Change log

* **0.1.0**
    * 在保证interface的方法完整性的前提下，删除了核心逻辑package下的所有不必要的class
    * 重新组织了channel包下的类，添加了event, exception和future包，使每个package下的类的职能更明确，方便理解

* **0.0.1**
    * 以packge为粒度，删除了所有不必要的包
    * 确保netty的网络部分的核心逻辑的完整性


##模块变更

*以下的表格列出了保留的模块*

| Modules | Function |
|-----------------|-----------------------------------------------------------------------|
| bootstrap | 组织netty模块工作并启动相关模块的工具类 |
| buffer | Buffer的具体相关实现 |
| channel | 对java nio中的channel的重新封装的相关实现 |
| channel-socket | netty的网络核心模块，包括Boss、Worker、Selector、核心主循环等相关实现 |
| dodec | 编解码模块 |
| logging | 相关的log工具类实现 |
| example | 测试实例 |
| util | 相关的工具类实现，包括Timer、StringUtil、ByteBufferUtil等 |   

**注：其中logging和util并不重要，阅读源码时可以略过，这里由于一些依赖关系所以保留**

*以下表格列出了删除掉的模块*

| Modules | Function |
|---------------------|------------------------------------|
| channel-group | Channel Group相关实现 |
| channel-local | local transport的相关支持 |
| channel-socket-http | http handler的相关实现 |
| channel-socket-oio | oio模式下的核心网络模块 |
| container | 一些无关紧要的适配 |
| example | Netty官方自带的一些例子 |
| handler | Netty异常强大功能全面的handler实现 |
| netty-test |  Netty的单元测试集|  

##License
Apache License v2









