# 通用导出
在开发过程中，我们经常会遇到需要导出数据的需求，比如将列表的数据导出到Excel中，那么这时候就需要开发一个接口。这个库是一个通用的导出库，在有导出需求时，无需编写导出接口即可非常方便的实现导出需求。

## 快速入门

### 1. 引入依赖
```xml
<dependency>
    <groupId>com.github.houbb</groupId>
    <artifactId>export</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 2. 使用示例
2.1 在列表接口上添加注解 `@Export`
```java
@RestController
public class TestController {
    
    // 在列表接口上添加注解
    @Export
    @GetMapping("/list")
    public List<Test> list() {
        return testService.list();
    }

    // 在列表接口上添加注解
    @Export(responseData = "data")
    @GetMapping("/list2")
    public ResultVO<?> list2(ParamDTO paramDTO) {
        List<Test> list = testService.list();
        return ResultUtil.success(list);
    }
}
```

2.2 接口的返回类中的需要添加 `@ExcelProperty` 注解，底层使用了阿里的 `EasyExcel` 来实现导出功能
```java
@Data
public class Test {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("年龄")
    private Integer age;

    @ExcelProperty("创建时间")
    private Date createTime;
}
```

### 3. 访问接口
http://localhost:8080/list?export=1&exportAll=1
</br>
http://localhost:8080/list2?export=1&exportAll=1

在访问接口时，请求参数中添加 `export=1` 即可达到导出的效果，也可以传 `export=true`
</br>
如果需要导出全部数据，那么需要额外在请求参数中添加 `exportAll=1` ，同样也可以传 `exportAll=true`